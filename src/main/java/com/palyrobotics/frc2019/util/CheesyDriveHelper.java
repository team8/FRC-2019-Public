package com.palyrobotics.frc2019.util;

import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.config.constants.DrivetrainConstants;
import com.palyrobotics.frc2019.config.dashboard.LiveGraph;
import com.palyrobotics.frc2019.config.subsystem.DriveConfig;
import com.palyrobotics.frc2019.util.config.Configs;
import com.palyrobotics.frc2019.util.csvlogger.CSVWriter;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Timer;

/**
 * Implements constant curvature driving. Yoinked from 254 code
 */
public class CheesyDriveHelper {
    private double
            mOldWheel, mPreviousWheelForRamp, mPreviousThrottleForRamp,
            mQuickStopAccumulator, mNegativeInertiaAccumulator,
            mBrownOutTimeSeconds;
    private final DriveConfig mDriveConfig = Configs.get(DriveConfig.class);
    private SparkDriveSignal mSignal = new SparkDriveSignal();

    public SparkDriveSignal cheesyDrive(Commands commands, RobotState robotState) {

        // Quick-turn if right trigger is pressed
        boolean isQuickTurn = robotState.isQuickTurning = commands.isQuickTurn;

        double totalPowerMultiplier;
        if (RobotController.isBrownedOut()) {
            totalPowerMultiplier = mDriveConfig.brownOutInitialNerfMultiplier;
            mBrownOutTimeSeconds = Timer.getFPGATimestamp();
        } else {
            totalPowerMultiplier = MathUtil.clamp(
                    mDriveConfig.brownOutInitialNerfMultiplier
                            + (Timer.getFPGATimestamp() - mBrownOutTimeSeconds)
                            * (1 / mDriveConfig.brownOutRecoverySeconds) * (1 - mDriveConfig.brownOutInitialNerfMultiplier),
                    mDriveConfig.brownOutInitialNerfMultiplier, 1.0
            );
        }
//        CSVWriter.addData("totalPowerMultiplier", totalPowerMultiplier);

        double throttle = commands.driveThrottle, wheel = commands.driveWheel;

        boolean underVelocity = Math.abs(robotState.rightDriveVelocity) < mDriveConfig.velocityUnlock &&
                Math.abs(robotState.leftDriveVelocity) < mDriveConfig.velocityUnlock;

//        CSVWriter.addData("underVelocity", underVelocity);
//        CSVWriter.addData("leftVelocity", robotState.leftDriveVelocity);
//        CSVWriter.addData("rightVelocity", robotState.rightDriveVelocity);

        if (Math.abs(throttle) > mDriveConfig.initialLock && underVelocity) {
            throttle = Math.signum(throttle) * mDriveConfig.initialLock;
        }

        if (isQuickTurn && Math.abs(wheel) > mDriveConfig.initialLock && underVelocity) {
            wheel = Math.signum(wheel) * mDriveConfig.initialLock;
        }

        double absoluteThrottle = Math.abs(throttle);
        if (absoluteThrottle > mDriveConfig.throttleAccelerationThreshold && absoluteThrottle > Math.abs(mPreviousThrottleForRamp)) {
            throttle = mPreviousThrottleForRamp + Math.signum(throttle) * mDriveConfig.throttleAccelerationLimit;
            absoluteThrottle = Math.abs(throttle);
        }
        mPreviousThrottleForRamp = throttle;

        double absoluteWheel = Math.abs(wheel);
        if (isQuickTurn && absoluteWheel > mDriveConfig.wheelAccelerationThreshold && absoluteWheel > Math.abs(mPreviousWheelForRamp)) {
            wheel = mPreviousWheelForRamp + Math.signum(wheel) * mDriveConfig.wheelAccelerationLimit;
            absoluteWheel = Math.abs(wheel);
        }
        mPreviousWheelForRamp = wheel;

        wheel = MathUtil.handleDeadBand(wheel, DrivetrainConstants.kDeadband);
        throttle = MathUtil.handleDeadBand(throttle, DrivetrainConstants.kDeadband);

        double negativeWheelInertia = wheel - mOldWheel;
        mOldWheel = wheel;

        // Map linear wheel input onto a sin wave, three passes
        for (int i = 0; i < mDriveConfig.nonlinearPasses; i++)
            wheel = applyWheelNonLinearPass(wheel, mDriveConfig.wheelNonLinearity);

        // Negative inertia
        double negativeInertiaScalar;
        if (wheel * negativeWheelInertia > 0) {
            // If we are moving away from zero - trying to get more wheel
            negativeInertiaScalar = mDriveConfig.lowNegativeInertiaTurnScalar;
        } else {
            // Going back to zero
            if (absoluteWheel > mDriveConfig.lowNegativeInertiaThreshold) {
                negativeInertiaScalar = mDriveConfig.lowNegativeInertiaFarScalar;
            } else {
                negativeInertiaScalar = mDriveConfig.lowNegativeInertiaCloseScalar;
            }
        }

        double negativeInertiaPower = negativeWheelInertia * negativeInertiaScalar;
        mNegativeInertiaAccumulator += negativeInertiaPower;

        wheel += mNegativeInertiaAccumulator;
        if (mNegativeInertiaAccumulator > 1.0) {
            mNegativeInertiaAccumulator -= 1.0;
        } else if (mNegativeInertiaAccumulator < -1.0) {
            mNegativeInertiaAccumulator += 1.0;
        } else {
            mNegativeInertiaAccumulator = 0.0;
        }

        // Quick-turn allows us to turn in place without having to be moving forward or backwards
        double angularPower, overPower;
        if (isQuickTurn) {
            if (absoluteThrottle < mDriveConfig.quickStopDeadBand) {
                double alpha = mDriveConfig.quickStopWeight;
                mQuickStopAccumulator = (1 - alpha) * mQuickStopAccumulator + alpha * MathUtil.clamp01(wheel) * mDriveConfig.quickStopScalar;
            }
            overPower = 1.0;
            angularPower = wheel * mDriveConfig.quickTurnScalar;
        } else {
            overPower = 0.0;
            angularPower = absoluteThrottle * wheel * mDriveConfig.turnSensitivity - mQuickStopAccumulator;
            if (mQuickStopAccumulator > 1.0) {
                mQuickStopAccumulator -= 1.0;
            } else if (mQuickStopAccumulator < -1.0) {
                mQuickStopAccumulator += 1.0;
            } else {
                mQuickStopAccumulator = 0.0;
            }
        }

        double linearPower = throttle;

        double rightPower, leftPower;
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

//        CSVWriter.addData("leftWantedPower", leftPower * totalPowerMultiplier);
//        CSVWriter.addData("rightWantedPower", rightPower * totalPowerMultiplier);

        mSignal.leftOutput.setPercentOutput(leftPower * totalPowerMultiplier);
        mSignal.rightOutput.setPercentOutput(rightPower * totalPowerMultiplier);
        return mSignal;
    }

    private double applyWheelNonLinearPass(double wheel, double wheelNonLinearity) {
        return Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel) / Math.sin(Math.PI / 2.0 * wheelNonLinearity);
    }

    public void reset() {
        mNegativeInertiaAccumulator = mQuickStopAccumulator = 0.0;
        mOldWheel = mPreviousWheelForRamp = mPreviousThrottleForRamp = 0.0;
    }
}
