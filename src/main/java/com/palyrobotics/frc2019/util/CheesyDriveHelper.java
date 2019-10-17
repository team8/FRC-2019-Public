package com.palyrobotics.frc2019.util;

import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.config.constants.DrivetrainConstants;
import com.palyrobotics.frc2019.subsystems.Drive;

/**
 * CheesyDriveHelper implements the calculations used in CheesyDrive for teleop control. Returns a DriveSignal for the motor output
 */
public class CheesyDriveHelper {
    private double mOldWheel, mQuickStopAccumulator;
    private boolean mInitialBrake;
    private double mBrakeRate;
    private SparkDriveSignal mSignal = new SparkDriveSignal();

    public SparkDriveSignal cheesyDrive(Commands commands, RobotState robotState) {
        double throttle = commands.driveThrottle, wheel = commands.driveWheel;

        if (commands.wantedDriveState == Drive.DriveState.CHEZY) {
            wheel *= 0.75;
        }
        // Quick-turn if right trigger is pressed
        boolean isQuickTurn = robotState.isQuickTurning = commands.isQuickTurn;

//        //Braking if left trigger is pressed
//        boolean isBraking = robotState.leftStickInput.getTriggerPressed();

        double wheelNonLinearity;

        wheel = MathUtil.handleDeadBand(wheel, DrivetrainConstants.kDeadband);
        throttle = MathUtil.handleDeadBand(throttle, DrivetrainConstants.kDeadband);

        double negInertia = wheel - mOldWheel;
        mOldWheel = wheel;

        wheelNonLinearity = 0.5;

        // Map linear wheel input onto a sin wave, three passes
        for (int i = 0; i < 3; i++)
            wheel = applyWheelNonLinearPass(wheel, wheelNonLinearity);

        double leftPower, rightPower, overPower;
        double sensitivity;

        double angularPower;

        //linear power is what's actually sent to motor, throttle is input
        double linearPower = throttle;

        //Negative inertia
        double negInertiaAccumulator = 0.0;
        double negInertiaScalar;

        if (wheel * negInertia > 0) {
            negInertiaScalar = 2.5;
        } else {
            if (Math.abs(wheel) > 0.65) {
                negInertiaScalar = 5.0;
            } else {
                negInertiaScalar = 3.0;
            }
        }

        //neginertia is difference in wheel
        double negInertiaPower = negInertia * negInertiaScalar;
        negInertiaAccumulator += negInertiaPower;

        //possible source of occasional overturn
        wheel = wheel + negInertiaAccumulator;

//        // Handle braking
//        if (isBraking) {
//            //Set up braking rates for linear deceleration in a set amount of time
//            if (mInitialBrake) {
//                mInitialBrake = false;
//                //Old throttle initially set to throttle
//                mOldThrottle = linearPower;
//                //Braking rate set
//                mBrakeRate = mOldThrottle / DrivetrainConstants.kCyclesUntilStop;
//            }
//
//            //If braking is not complete, decrease by the brake rate
//            if (Math.abs(mOldThrottle) >= Math.abs(mBrakeRate)) {
//                //reduce throttle
//                mOldThrottle -= mBrakeRate;
//                linearPower = mOldThrottle;
//            } else {
//                linearPower = 0;
//            }
//        } else {
//            mInitialBrake = true;
//        }

        // Quick-turn
        if (isQuickTurn) {
            if (Math.abs(commands.driveWheel) < DrivetrainConstants.kQuickTurnSensitivityThreshold) {
                sensitivity = DrivetrainConstants.kPreciseQuickTurnSensitivity;
            } else {
                sensitivity = DrivetrainConstants.kQuickTurnSensitivity;
            }
            angularPower = wheel * sensitivity;
            mQuickStopAccumulator = (1 - DrivetrainConstants.kAlpha) * mQuickStopAccumulator + DrivetrainConstants.kAlpha * angularPower * 6.5;
            overPower = 1.0;
        } else {
            overPower = 0.0;
            // Sets turn amount
            angularPower = Math.abs(throttle) * wheel - mQuickStopAccumulator;
            if (mQuickStopAccumulator > DrivetrainConstants.kQuickStopAccumulatorDecreaseThreshold) {
                mQuickStopAccumulator -= DrivetrainConstants.kQuickStopAccumulatorDecreaseRate;
            } else if (mQuickStopAccumulator < -DrivetrainConstants.kQuickStopAccumulatorDecreaseThreshold) {
                mQuickStopAccumulator += DrivetrainConstants.kQuickStopAccumulatorDecreaseRate;
            } else {
                mQuickStopAccumulator = 0.0;
            }
        }

        rightPower = leftPower = linearPower;
        leftPower += angularPower;
        rightPower -= angularPower;

        if (leftPower > 1.0) {
            rightPower -= overPower * (leftPower - 1.0);
            leftPower = 1.0;
        } else if (rightPower > 1.0) {
            leftPower -= overPower * (rightPower - 1.0);
            rightPower = 1.0;
        } else if (leftPower < -1.0) {
            rightPower += overPower * (-1.0 - leftPower);
            leftPower = -1.0;
        } else if (rightPower < -1.0) {
            leftPower += overPower * (-1.0 - rightPower);
            rightPower = -1.0;
        }

        mSignal.leftOutput.setPercentOutput(leftPower);
        mSignal.rightOutput.setPercentOutput(rightPower);
        return mSignal;
    }

    private double applyWheelNonLinearPass(double wheel, double wheelNonLinearity) {
        return Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel) / Math.sin(Math.PI / 2.0 * wheelNonLinearity);
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
//                if (initialThrottle * mOldThrottle < 0) {
//                    return 0.0;
//                }
//
//                //Increase in magnitude, deceleration is fine. This misses rapid direction switches, but that's up to driver
//                if (x > Math.abs(mOldThrottle)) {
//                    x = mOldThrottle + Math.signum(initialThrottle) * DrivetrainConstants.kMaxAccelRate;
//                } else {
//                    x = initialThrottle;
//                }
//
////				x = initialThrottle;
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