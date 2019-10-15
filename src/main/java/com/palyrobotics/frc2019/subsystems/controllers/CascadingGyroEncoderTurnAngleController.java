package com.palyrobotics.frc2019.subsystems.controllers;

import com.palyrobotics.frc2019.config.Constants.DrivetrainConstants;
import com.palyrobotics.frc2019.config.Gains;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.subsystems.Drive.DriveController;
import com.palyrobotics.frc2019.util.Pose;
import com.palyrobotics.frc2019.util.SparkDriveSignal;
import com.palyrobotics.frc2019.util.SynchronousPID;

public class CascadingGyroEncoderTurnAngleController implements DriveController {

    private Pose mCachedPose;

    private SparkDriveSignal mOutput = new SparkDriveSignal();

    private SynchronousPID mPidController = new SynchronousPID(Gains.kVidarCascadingTurnP, 0.0, Gains.kVidarCascadingTurnD, 0.0);

    public CascadingGyroEncoderTurnAngleController(Pose lastSetPoint, double angle) {
        mPidController.setSetPoint(lastSetPoint.heading + angle);
        mCachedPose = lastSetPoint;
    }

    @Override
    public SparkDriveSignal update(RobotState state) {
        mCachedPose = state.drivePose;
        if (mCachedPose == null) {
            mOutput.leftOutput.setIdle();
            mOutput.rightOutput.setIdle();
        } else {
            double targetVelocity = mPidController.calculate(mCachedPose.heading);
            mOutput.leftOutput.setTargetVelocity(-targetVelocity);
            mOutput.rightOutput.setTargetVelocity(targetVelocity);
        }
        return mOutput;
    }

    @Override
    public Pose getSetPoint() {
        return null;
    }

    @Override
    public boolean onTarget() {
        return mCachedPose != null
                && Math.abs(mPidController.getError()) < DrivetrainConstants.kAcceptableTurnAngleError
                && Math.abs(mCachedPose.leftEncoderVelocity) < DrivetrainConstants.kAcceptableDriveVelocityError
                && Math.abs(mCachedPose.rightEncoderVelocity) < DrivetrainConstants.kAcceptableDriveVelocityError;
    }
}
