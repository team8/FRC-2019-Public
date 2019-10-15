package com.palyrobotics.frc2019.subsystems.controllers;

import com.palyrobotics.frc2019.config.Gains.TrajectoryGains;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.subsystems.Drive;
import com.palyrobotics.frc2019.util.MathUtil;
import com.palyrobotics.frc2019.util.Pose;
import com.palyrobotics.frc2019.util.SparkDriveSignal;

/**
 * Used to run on-board position or velocity control of the drivetrain
 * This controller is intended to be used in a cascading manner, with a parent controller that generates realtime set points
 */
public class OnBoardDriveController implements Drive.DriveController {
    private RobotState mCachedState;
    private OnBoardControlType mControlType;
    private TrajectoryGains mGains;

    private TrajectorySegment mLeftSetPoint;
    private TrajectorySegment mRightSetPoint;

    private double mLastLeftError, mLastRightError;

    private boolean reset = true;
    private SparkDriveSignal mDriveSignal = new SparkDriveSignal();

    public enum OnBoardControlType {
        kPosition,
        kVelocity,
        kVelocityWithArbitraryDemand
    }

    OnBoardDriveController(OnBoardControlType controlType, TrajectoryGains gains) {
        mLeftSetPoint = new TrajectorySegment();
        mRightSetPoint = new TrajectorySegment();
        mGains = gains;
        mControlType = controlType;
    }

    @Override
    public SparkDriveSignal update(RobotState state) {
        mCachedState = state;
        switch (mControlType) {
            case kPosition:
                mDriveSignal = getPositionOutput(state.drivePose);
                break;
            case kVelocity:
                mDriveSignal = getVelocityOutput(state.drivePose);
                break;
            case kVelocityWithArbitraryDemand:
                mDriveSignal = getVelocityWithArbitraryDemand();
                break;
            default:
                throw new RuntimeException("Unexpected control type!");
        }
        return mDriveSignal;
    }

    /**
     * Should only be called by a parent controller
     */
    void updateSetPoint(TrajectorySegment leftSetPoint, TrajectorySegment rightSetPoint, Object handle) throws IllegalAccessException {
        if (!(handle instanceof Drive.DriveController)) {
            throw new IllegalAccessException();
        }
        mLeftSetPoint = leftSetPoint;
        mRightSetPoint = rightSetPoint;
    }

    private SparkDriveSignal getPositionOutput(Pose drivePose) {
        double left_sp = mLeftSetPoint.position;
        double left_pv = drivePose.leftEncoderPosition;
        double right_sp = mRightSetPoint.position;
        double right_pv = drivePose.rightEncoderPosition;

        return updateClosedLoopOutputs(left_sp, left_pv, right_sp, right_pv);
    }

    private SparkDriveSignal getVelocityOutput(Pose drivePose) {
        double leftSetPoint = mLeftSetPoint.velocity;
        double leftPosition = drivePose.leftEncoderVelocity;
        double rightSetPoint = mRightSetPoint.velocity;
        double rightPosition = drivePose.rightEncoderVelocity;
        return updateClosedLoopOutputs(leftSetPoint, leftPosition, rightSetPoint, rightPosition);
    }

    private SparkDriveSignal getVelocityWithArbitraryDemand() {
        SparkDriveSignal signal = updateClosedLoopOutputs(0, 0, 0, 0);
        signal.leftOutput.setTargetVelocity(mLeftSetPoint.velocity, signal.leftOutput.getReference());
        signal.rightOutput.setTargetVelocity(mRightSetPoint.velocity, signal.rightOutput.getReference());
        return signal;
    }

    private SparkDriveSignal updateClosedLoopOutputs(double leftSetPoint, double leftPosition, double rightSetPoint, double rightPosition) {

        // Calculate error
        double leftError = leftSetPoint - leftPosition;
        double rightError = rightSetPoint - rightPosition;

        // Calculate d_error
        double dLeftError = (leftError - mLastLeftError) / mLeftSetPoint.dt;
        double dRightError = (rightError - mLastRightError) / mRightSetPoint.dt;

        // Ignore d error on the first cycle
        if (reset) {
            dLeftError = 0.0;
            dRightError = 0.0;
            reset = false;
        }

        // Calculate output
        // output = kP * error + kD * d_error + kV * velocity + kA * acceleration + sign(velocity)*kS
        double leftOutput = mGains.p * leftError
                + mGains.d * dLeftError
                + mGains.v * mLeftSetPoint.velocity
                + mGains.a * mLeftSetPoint.acc
                + Math.signum(mLeftSetPoint.velocity) * mGains.s;
        double rightOutput = mGains.p * rightError
                + mGains.d * dRightError
                + mGains.v * mRightSetPoint.velocity
                + mGains.a * mRightSetPoint.acc
                + Math.signum(mRightSetPoint.velocity) * mGains.s;

        leftOutput = MathUtil.clamp01(leftOutput);
        rightOutput = MathUtil.clamp01(rightOutput);

        // Dead band output within (-kS-0.02, kS+0.02) to avoid jitter with kS
        leftOutput = Math.abs(leftOutput) - mGains.s < 0.02 ? 0.0 : leftOutput;
        rightOutput = Math.abs(rightOutput) - mGains.s < 0.02 ? 0.0 : rightOutput;

        mLastLeftError = leftError;
        mLastRightError = rightError;

        mDriveSignal.leftOutput.setPercentOutput(leftOutput);
        mDriveSignal.rightOutput.setPercentOutput(rightOutput);

        return mDriveSignal;
    }

    @Override
    public Pose getSetPoint() {
        return new Pose();
    }

    @Override
    public boolean onTarget() {
        // Unimplemented, not used with cascading control
        return mCachedState != null;
    }

    /**
     * Helper class to represent a set point
     */
    public static class TrajectorySegment {

        double position, velocity, acc, dt;

        TrajectorySegment() {
        }

        TrajectorySegment(double vel, double acc, double dt) {
            this(0, vel, acc, dt);
        }

        TrajectorySegment(double position, double vel, double acc, double dt) {
            this.position = position;
            this.velocity = vel;
            this.acc = acc;
            this.dt = dt;
        }

        public TrajectorySegment(TrajectorySegment to_copy) {
            position = to_copy.position;
            velocity = to_copy.velocity;
            acc = to_copy.acc;
            dt = to_copy.dt;
        }

        public String toString() {
            return String.format("pos: %s; vel: %s; acc: %s; dt: %s", position, velocity, acc, dt);
        }
    }

}