package com.palyrobotics.frc2019.behavior.routines.drive;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.Constants.DrivetrainConstants;
import com.palyrobotics.frc2019.config.Constants.PhysicalConstants;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.robot.Robot;
import com.palyrobotics.frc2019.subsystems.Drive;
import com.palyrobotics.frc2019.subsystems.Subsystem;
import com.palyrobotics.frc2019.util.csvlogger.CSVWriter;
import com.palyrobotics.frc2019.util.trajectory.Path;
import com.palyrobotics.frc2019.util.trajectory.Translation2d;
import com.palyrobotics.frc2019.vision.Limelight;
import com.palyrobotics.frc2019.vision.LimelightControlMode;

import java.util.ArrayList;

public class VisionDrivePathRoutine extends Routine {
    private Path mPath;
    private ArrayList<Path.Waypoint> pathList;
    private double mLookAhead;
    private double mStartSpeed;
    private boolean mInverted;
    private double mTolerance = DrivetrainConstants.kPathFollowingTolerance;
    private boolean mRelative;
    private State mState;
    private double mTargetHeading;

    private enum State{
        START, LOOKING, FOLLOW, DONE
    }

    public VisionDrivePathRoutine() {
        this.pathList = new ArrayList<Path.Waypoint>();
        this.mInverted = false;
        this.mLookAhead = DrivetrainConstants.kPathFollowingLookahead;
        this.mStartSpeed = 0.0;
        this.mRelative = true;
        mState = State.START;
    }

    @Override
    public void start() {
        drive.setNeutral();
        Limelight.getInstance().setCamMode(LimelightControlMode.CamMode.VISION);
        Limelight.getInstance().setLEDMode(LimelightControlMode.LedMode.FORCE_ON); // Limelight LED on
    }

    @Override
    public Commands update(Commands commands) {
        switch (mState) {
            case START:
                mState = State.LOOKING;
                break;
            case LOOKING:
                if (Limelight.getInstance().isTargetFound()) {
                    makePath();
                    System.out.println("target heading: " + mTargetHeading);
                    mState = State.DONE; //for testing target selection
//                    drive.setTrajectoryController(mPath, mLookAhead, mInverted, mTolerance);
//                    mState = State.FOLLOW;
                }
                break;
            case FOLLOW: // I think this is right controller
                commands.wantedDriveState = Drive.DriveState.ON_BOARD_CONTROLLER;
                if (drive.controllerOnTarget()) {
                    mState = State.DONE;
                }
                break;
            case DONE:
                drive.resetController();
        }

        return commands;
    }

    @Override
    public Commands cancel(Commands commands) { //idk
        mState = State.DONE;
        commands.wantedDriveState = Drive.DriveState.NEUTRAL;
        drive.setNeutral();
        return commands;
    }

    @Override
    public boolean finished() {
        return mState == State.DONE;
    }

    private void makePath() {
        double robotHeading = RobotState.getInstance().drivePose.heading;
        double yawToTarget = -Limelight.getInstance().getYawToTarget();
        double xDist;
        double yDist;
        double xDistShort;
        double absXDist;
        double absYDist;
        double absXDistShort;
        double absYDistShort;

        //making heading from -180deg to 180deg
        robotHeading %= 360;
        if (Math.abs(robotHeading) > 180) {
            robotHeading = ((robotHeading < 0) ? 180 + (robotHeading % 180) : -180 + (robotHeading % 180));
        }
        System.out.println("robot heading: " + robotHeading);

        selectTarget(robotHeading);

        //Converting to radians for math stuff
        robotHeading *= Math.PI / 180;
        yawToTarget *= Math.PI / 180;
        mTargetHeading *= Math.PI / 180;

        //TODO: test
        if ((robotHeading < 0 && yawToTarget > 0) || (robotHeading > 0 && yawToTarget < 0)) { //case 1, robot is over-turned
            yDist = Limelight.getInstance().getCorrectedEstimatedDistanceZ() * Math.sin(
                    Math.PI/2 - Math.abs(robotHeading - mTargetHeading));
            xDist = yDist * Math.tan(robotHeading - mTargetHeading - yawToTarget);
        } else /*if ((robotHeading < 0 && yawToTarget < 0) || (robotHeading > 0 && yawToTarget > 0)) */{ //case 2, robot is under-turned
            yDist = Limelight.getInstance().getCorrectedEstimatedDistanceZ() * Math.cos(robotHeading - mTargetHeading);
            xDist = yDist / Math.tan(Math.PI - Math.abs(robotHeading - mTargetHeading) - Math.abs(yawToTarget));
        }
        xDistShort = 0.3 * xDist;
        double angle = Math.atan(xDistShort/yDist); //Hopefully this math be right

        absXDistShort = xDistShort * Math.cos(robotHeading) - yDist * Math.sin(robotHeading);
        absYDistShort = ((robotHeading + yawToTarget > 0) ? yDist: -yDist) * Math.cos(robotHeading) + xDistShort * Math.sin(robotHeading);
        double distToPoint = Math.sqrt(Math.pow(absXDistShort, 2) + Math.pow(absYDistShort, 2));
        absXDistShort = distToPoint * Math.cos(robotHeading - yawToTarget);
        absYDistShort = distToPoint * Math.sin(robotHeading - yawToTarget);

        absYDist = ((robotHeading + yawToTarget > 0) ? yDist: -yDist) * Math.cos(robotHeading) + xDist * Math.sin(robotHeading);
        absXDist = xDist * Math.cos(robotHeading) - yDist * Math.sin(robotHeading);
        double distToTarget = Math.sqrt(Math.pow(absXDist, 2) + Math.pow(absYDist, 2));
        absYDist = distToTarget * Math.sin(robotHeading - yawToTarget);
        absXDist = distToTarget * Math.cos(robotHeading - yawToTarget);


        //TODO: tune speed and how far the first point should be from target
        //converting relative path to absolute path
        ArrayList<Path.Waypoint> relPathList = new ArrayList<>();
        relPathList.add(new Path.Waypoint(new Translation2d(absXDistShort, absYDistShort), 50, true));
        relPathList.add(new Path.Waypoint(new Translation2d(absXDist, absYDist), 0, true)); // idk if need to consider robot length/width
        for (Path.Waypoint point : relPathList) {
            pathList.add(new Path.Waypoint(robotState.getLatestFieldToVehicle().getValue().getTranslation().translateBy(point.position), point.speed, false));
        }
        mPath = new Path(pathList);
    }

    /**
     * Finds which target is being detected based on current heading
     * DOES NOT WORK
     */
    private void selectTarget(double robotHeading) { //TODO: use robot x and y position to make it work
        double[] targetHeadings = new double[] {
                PhysicalConstants.kCargoShipFrontHeading
                PhysicalConstants.kCargoShipLeftHeading,
                PhysicalConstants.kCargoShipRightHeading,
                PhysicalConstants.kLeftRocketCloseHeading,
                PhysicalConstants.kLeftRocketFarHeading,
                PhysicalConstants.kRightRocketCloseHeading,
                PhysicalConstants.kRightRocketFarHeading,
                PhysicalConstants.kLoadingStationHeading
        };

        for (double targetHeading : targetHeadings) { // check which target heading is being detected
            System.out.println(Math.abs(targetHeading - robotHeading - Limelight.getInstance().getYawToTarget()));
//            if (Math.abs(targetHeading - robotHeading - Limelight.getInstance().getYawToTarget()) < 10) { //TODO: tune threshold, check if works
                mTargetHeading = targetHeading;
                break;
//            }
        }
    }

    @Override
    public Subsystem[] getRequiredSubsystems() {
        return new Subsystem[] { drive };
    }

    @Override
    public String getName() {
        return "VisionDrivePathRoutine";
    }
}
