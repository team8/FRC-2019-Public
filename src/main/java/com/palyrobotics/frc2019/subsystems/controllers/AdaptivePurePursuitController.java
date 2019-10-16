package com.palyrobotics.frc2019.subsystems.controllers;

import com.palyrobotics.frc2019.config.Constants.DrivetrainConstants;
import com.palyrobotics.frc2019.config.subsystem.DriveConfig;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.subsystems.Drive;
import com.palyrobotics.frc2019.subsystems.controllers.OnBoardDriveController.OnBoardControlType;
import com.palyrobotics.frc2019.subsystems.controllers.OnBoardDriveController.TrajectorySegment;
import com.palyrobotics.frc2019.util.Pose;
import com.palyrobotics.frc2019.util.SparkDriveSignal;
import com.palyrobotics.frc2019.util.config.Configs;
import com.palyrobotics.frc2019.util.csvlogger.CSVWriter;
import com.palyrobotics.frc2019.util.trajectory.*;
import edu.wpi.first.wpilibj.Timer;

import java.util.Optional;
import java.util.Set;

/**
 * Implements an adaptive pure pursuit controller. See: https://www.ri.cmu.edu/pub_files/pub1/kelly_alonzo_1994_4/kelly_alonzo_1994_4 .pdf
 * <p>
 * Basically, we find a spot on the path we'd like to follow and calculate the wheel speeds necessary to make us land on that spot. The target spot is a
 * specified distance ahead of us, and we look further ahead the greater our tracking error.
 */
public class AdaptivePurePursuitController implements Drive.DriveController {

    private static final double kEpsilon = 1E-9;

    private double mFixedLookahead;
    private Path mPath;
    private RigidTransform2d.Delta mLastCommand;
    private Kinematics.DriveVelocity mLastDriveVelocity;
    private double mLastTime;
    private double mMaxAcceleration;
    private double mDt;
    private boolean mReversed;
    private double mPathCompletionTolerance;

    private OnBoardDriveController mOnBoardController;

    public AdaptivePurePursuitController(double fixed_lookahead, double max_accel, double nominal_dt, Path path, boolean reversed,
                                         double path_completion_tolerance) {
        mFixedLookahead = fixed_lookahead;
        mMaxAcceleration = max_accel;
        mPath = path;
        mDt = nominal_dt;
        mLastCommand = null;
        mReversed = reversed;
        mPathCompletionTolerance = path_completion_tolerance;
        mOnBoardController = new OnBoardDriveController(OnBoardControlType.kVelocityWithArbitraryDemand, Configs.get(DriveConfig.class).trajectoryGains);
    }

    /**
     * Calculate the next Delta and convert it to a drive signal
     *
     * @param state - the current robot state, currently unused
     * @return the left and right drive voltage outputs needed to move in the calculated Delta
     */
    @Override
    public SparkDriveSignal update(RobotState state) {

        //Get estimated robot position
        RigidTransform2d robot_pose = state.getLatestFieldToVehicle().getValue();

        //Reverse the rotation if the path is reversed
        if (mReversed) {
            robot_pose = new RigidTransform2d(robot_pose.getTranslation(), robot_pose.getRotation().rotateBy(Rotation2d.fromRadians(Math.PI)));
        }

        double distance_from_path = mPath.update(robot_pose.getTranslation());
        PathSegment.Sample lookahead_point = mPath.getLookaheadPoint(robot_pose.getTranslation(), distance_from_path + mFixedLookahead);
        Optional<Circle> circle = joinPath(robot_pose, lookahead_point.translation);

        double speed = lookahead_point.speed;
        if (mReversed) {
            speed *= -1;
        }

        //If we are just starting the controller, use the robot's current velocity and the default dt
        double now = Timer.getFPGATimestamp();
        double dt = now - mLastTime;
        if (mLastDriveVelocity == null || mLastCommand == null) {
            mLastDriveVelocity = new Kinematics.DriveVelocity(state.drivePose.leftEncoderVelocity, state.drivePose.rightEncoderVelocity);
            mLastCommand = Kinematics.forwardKinematics(mLastDriveVelocity);
            dt = mDt;
        }

        //Ensure we don't accelerate too fast from the previous command
        double accel = (speed - mLastCommand.dX) / dt;
        if (accel < -mMaxAcceleration) {
            speed = mLastCommand.dX - mMaxAcceleration * dt;
        } else if (accel > mMaxAcceleration) {
            speed = mLastCommand.dX + mMaxAcceleration * dt;
        }

        //Ensure we slow down in time to stop
        //vf^2 = v^2 + 2*a*d
        //0 = v^2 + 2*a*d
        double remaining_distance = mPath.getRemainingLength();
        double max_allowed_speed = Math.sqrt(2 * mMaxAcceleration * remaining_distance);

        //Ensure we don't go faster than the maximum path following speed
        max_allowed_speed = Math.min(max_allowed_speed, DrivetrainConstants.kPathFollowingMaxVel);

        //Bound speed by constraints
        if (Math.abs(speed) > max_allowed_speed) {
            speed = max_allowed_speed * Math.signum(speed);
        }

        //Obtain command (linear / angular velocity)
        RigidTransform2d.Delta command;
        if (circle.isPresent()) {
            command = new RigidTransform2d.Delta(speed, 0, (circle.get().turn_right ? -1 : 1) * Math.abs(speed) / circle.get().radius);
        } else {
            command = new RigidTransform2d.Delta(speed, 0, 0);
        }

        //Convert command to set point (left / right velocity)
        Kinematics.DriveVelocity setPoint = Kinematics.inverseKinematics(command);

        //Calculate acceleration of each side based on last setPoint
        double leftAcc = (setPoint.left - mLastDriveVelocity.left) / dt;
        double rightAcc = (setPoint.right - mLastDriveVelocity.right) / dt;

        CSVWriter.addData("lastDriveVelocityLeft", mLastDriveVelocity.left);
        CSVWriter.addData("lastDriveVelocityRight", mLastDriveVelocity.right);

        //Pass velocity and acceleration set points into onboard controller which
        //Returns a SparkSignal with velocity setPoint and arbitrary feedforward
        TrajectorySegment left_segment = new TrajectorySegment(setPoint.left, leftAcc, dt);
        TrajectorySegment right_segment = new TrajectorySegment(setPoint.right, rightAcc, dt);
        try {
            mOnBoardController.updateSetPoint(left_segment, right_segment, this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        mLastTime = now;
        mLastCommand = command;
        mLastDriveVelocity = setPoint;

        return mOnBoardController.update(state);

    }

    /**
     * Returns the list of labeled waypoints (markers) that the robot has passed
     *
     * @return a set of Strings representing each of the crossed markers
     */
    public Set<String> getMarkersCrossed() {
        return mPath.getMarkersCrossed();
    }

    //An abstraction of a circular arc used for turning motion
    public static class Circle {
        final Translation2d center;
        final double radius;
        final boolean turn_right;

        Circle(Translation2d center, double radius, boolean turn_right) {
            this.center = center;
            this.radius = radius;
            this.turn_right = turn_right;
        }
    }

    /**
     * Connect the current position and lookahead point with a circular arc
     *
     * @param robot_pose      - the current translation and rotation of the robot
     * @param lookahead_point - the coordinates of the lookahead point
     * @return - A circular arc representing the path, null if the arc is degenerate
     */
    private static Optional<Circle> joinPath(RigidTransform2d robot_pose, Translation2d lookahead_point) {
        double x1 = robot_pose.getTranslation().getX();
        double y1 = robot_pose.getTranslation().getY();
        double x2 = lookahead_point.getX();
        double y2 = lookahead_point.getY();

        Translation2d pose_to_lookahead = robot_pose.getTranslation().inverse().translateBy(lookahead_point);
        double cross_product = pose_to_lookahead.getX() * robot_pose.getRotation().sin() - pose_to_lookahead.getY() * robot_pose.getRotation().cos();
        if (Math.abs(cross_product) < kEpsilon) {
            return Optional.empty();
        }

        double dx = x1 - x2;
        double dy = y1 - y2;
        double my = (cross_product > 0 ? -1 : 1) * robot_pose.getRotation().cos();
        double mx = (cross_product > 0 ? 1 : -1) * robot_pose.getRotation().sin();

        double cross_term = mx * dx + my * dy;

        if (Math.abs(cross_term) < kEpsilon) {
            //Points are collinear
            return Optional.empty();
        }

        return Optional.of(new Circle(
                new Translation2d((mx * (x1 * x1 - x2 * x2 - dy * dy) + 2 * my * x1 * dy) / (2 * cross_term),
                        (-my * (-y1 * y1 + y2 * y2 + dx * dx) + 2 * mx * y1 * dx) / (2 * cross_term)),
                .5 * Math.abs((dx * dx + dy * dy) / cross_term), cross_product > 0));
    }

    //HOPING THIS METHOD NEVER GETS CALLED
    @Override
    public Pose getSetPoint() {
        return new Pose();
    }

    // Really only used for checking point injection, ignore otherwise
    public Path getPath() {
        return mPath;
    }

    @Override
    public boolean onTarget() {
        return mPath.getRemainingLength() <= mPathCompletionTolerance;
    }

}