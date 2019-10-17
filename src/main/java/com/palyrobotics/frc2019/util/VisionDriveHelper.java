package com.palyrobotics.frc2019.util;

import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.config.VisionConfig;
import com.palyrobotics.frc2019.config.constants.DrivetrainConstants;
import com.palyrobotics.frc2019.util.config.Configs;
import com.palyrobotics.frc2019.util.control.SynchronousPID;
import com.palyrobotics.frc2019.vision.Limelight;

/**
 * {@link CheesyDriveHelper} implements the calculations used for operator control.
 * Returns a {@link SparkDriveSignal} for the motor output.
 */
public class VisionDriveHelper {

    private static final double kMaxAngularPower = 0.6;

    private final Limelight mLimelight = Limelight.getInstance();
    private final SparkDriveSignal mSignal = new SparkDriveSignal();
    private final SynchronousPID mPidController = new SynchronousPID(Configs.get(VisionConfig.class).gains);
    private final CheesyDriveHelper mCDH = new CheesyDriveHelper();
    private boolean mInitialBrake;
    private double mLastThrottle, mBrakeRate;

    public SparkDriveSignal visionDrive(Commands commands, RobotState robotState) {

        double throttle = commands.driveThrottle;

        // Braking if left trigger is pressed
        boolean isBraking = commands.isBraking;

        throttle = MathUtil.handleDeadBand(throttle, DrivetrainConstants.kDeadband);

        double leftOutput, rightOutput;

        double angularPower;

        // Linear power is what's actually sent to motor, throttle is input
        double linearPower = throttle;

        // Handle braking
        if (isBraking) {
            // Set up braking rates for linear deceleration in a set amount of time
            if (mInitialBrake) {
                mInitialBrake = false;
                // Old throttle initially set to throttle
                mLastThrottle = linearPower;
                // Braking rate set
                mBrakeRate = mLastThrottle / DrivetrainConstants.kCyclesUntilStop;
            }

            // If braking is not complete, decrease by the brake rate
            if (Math.abs(mLastThrottle) >= Math.abs(mBrakeRate)) {
                // reduce throttle
                mLastThrottle -= mBrakeRate;
                linearPower = mLastThrottle;
            } else {
                linearPower = 0;
            }
        } else {
            mInitialBrake = true;
        }

        boolean hasFoundTarget;
        if (mLimelight.isTargetFound()) {
            angularPower = mPidController.calculate(mLimelight.getYawToTarget());
            // |angularPower| should be at most 0.6
            if (angularPower > kMaxAngularPower) angularPower = kMaxAngularPower;
            if (angularPower < -kMaxAngularPower) angularPower = -kMaxAngularPower;
            hasFoundTarget = true;
            if (mLimelight.getCorrectedEstimatedDistanceZ() < DrivetrainConstants.kVisionTargetThreshold) {
                robotState.atVisionTargetThreshold = true;
            }
        } else {
            hasFoundTarget = false;
            angularPower = 0.0;
        }

        rightOutput = leftOutput = mLastThrottle = linearPower;

        angularPower *= -1.0;
//        angularPower *= mOldThrottle;
        leftOutput *= (1 + angularPower);
        rightOutput *= (1 - angularPower);

        if (leftOutput > 1.0) {
            leftOutput = 1.0;
        } else if (rightOutput > 1.0) {
            rightOutput = 1.0;
        } else if (leftOutput < -1.0) {
            leftOutput = -1.0;
        } else if (rightOutput < -1.0) {
            rightOutput = -1.0;
        }

        if (throttle < 0.0 || (!hasFoundTarget && !robotState.atVisionTargetThreshold)) {
            return mCDH.cheesyDrive(commands, robotState);
        } else {
            mSignal.leftOutput.setPercentOutput(leftOutput);
            mSignal.rightOutput.setPercentOutput(rightOutput);
        }
        return mSignal;
    }

//    /**
//     * Throttle tuning functions
//     */
//    public double remapThrottle(double initialThrottle) {
//        double x = Math.abs(initialThrottle);
//        switch (OtherConstants.kDriverName) {
//            case BRYAN:
//                //Reversal of directions
//                //Stick a 0 cycle in between
//                if (initialThrottle * mLastThrottle < 0) {
//                    return 0.0;
//                }
//
//                //Increase in magnitude, deceleration is fine. This misses rapid direction switches, but that's up to driver
//                if (x > Math.abs(mLastThrottle)) {
//                    x = mLastThrottle + Math.signum(initialThrottle) * DrivetrainConstants.kMaxAccelRate;
//                } else {
//                    x = initialThrottle;
//                }
//
//                //				x = initialThrottle;
//                break;
//        }
//        return x;
//    }
//
//    /**
//     * Limits the given input to the given magnitude.
//     */
//    public double limit(double v, double limit) {
//        return (Math.abs(v) < limit) ? v : limit * (v < 0 ? -1 : 1);
//    }
}