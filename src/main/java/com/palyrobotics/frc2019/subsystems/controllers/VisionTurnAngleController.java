package com.palyrobotics.frc2019.subsystems.controllers;

import com.palyrobotics.frc2019.config.Gains;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.subsystems.Drive;
import com.palyrobotics.frc2019.util.DriveSignal;
import com.palyrobotics.frc2019.util.Pose;
import com.palyrobotics.frc2019.util.SynchronousPID;
import com.palyrobotics.frc2019.vision.Limelight;

/**
 * Turns drivetrain using the gyroscope and bang-bang control loop
 *
 * @author Robbie, Nihar
 *
 */
public class VisionTurnAngleController implements Drive.DriveController {

    private double mPower;
    private double mTargetHeading;
    private Pose mCachedPose;
    private SynchronousPID pidController;

    /**
     * @param currentPose
     *            Pass in the latest robot state
     * @param
     */
    public VisionTurnAngleController(Pose currentPose) {
        this.mCachedPose = currentPose;
        Gains turnGains = new Gains(.017, 0, 0, 0, 200, 0);
        pidController = new SynchronousPID(turnGains.P, turnGains.I, turnGains.D, turnGains.izone);
        pidController.setOutputRange(-1,1);
        pidController.setSetpoint(0);
        pidController.calculate(Limelight.getInstance().getYawToTarget());
    }

    @Override
    public DriveSignal update(RobotState state) {
        if(this.onTarget()) {
            return DriveSignal.getNeutralSignal();
        }

        mCachedPose = state.drivePose;
        double error = Limelight.getInstance().getYawToTarget();
        double turn = pidController.calculate(error);
        DriveSignal output = DriveSignal.getNeutralSignal();
        output.leftMotor.setPercentOutput(turn);
        output.rightMotor.setPercentOutput(-turn);

        return output;
    }

    @Override
    public Pose getSetpoint() {
        mCachedPose.heading = 0;
        Pose setpoint = new Pose(0, 0, 0, 0, 0, 0, 0, 0);
        return mCachedPose;
    }

    @Override
    public boolean onTarget() {
        System.out.println("a " + Limelight.getInstance().getYawToTarget());
        System.out.println(Limelight.getInstance().isConnected());
        double tolerance = 1;
        return Math.abs(Limelight.getInstance().getYawToTarget()) < tolerance && Math.abs(mCachedPose.headingVelocity) < 1;
    }

}
