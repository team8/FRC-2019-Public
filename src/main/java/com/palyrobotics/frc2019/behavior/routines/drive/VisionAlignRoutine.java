package com.palyrobotics.frc2019.behavior.routines.drive;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.subsystems.Drive;
import com.palyrobotics.frc2019.subsystems.Subsystem;
import com.palyrobotics.frc2019.util.logger.Logger;
import com.palyrobotics.frc2019.vision.Limelight;

import java.util.logging.Level;

public class VisionAlignRoutine extends Routine {
    @Override
    public Subsystem[] getRequiredSubsystems() {
        return new Subsystem[] { drive };
    }

    private double mAngle;

    private VisionAlignRoutine.GyroBBState mState = VisionAlignRoutine.GyroBBState.START;
    private double startTime;

    private enum GyroBBState {
        START, TURNING, TIMED_OUT, DONE
    }

    public VisionAlignRoutine() {
        this.mAngle = Limelight.getInstance().getYawToTarget();
    }

    @Override
    public void start() {
        drive.setNeutral();
        mState = VisionAlignRoutine.GyroBBState.START;
        startTime = System.currentTimeMillis();
    }

    @Override
    public Commands update(Commands commands) {
        if(mState != VisionAlignRoutine.GyroBBState.TIMED_OUT && (System.currentTimeMillis() - startTime > 5000)) {
            Logger.getInstance().logRobotThread(Level.WARNING, "Timed Out!");
            mState = VisionAlignRoutine.GyroBBState.TIMED_OUT;
        }
        switch(mState) {
            case START:
                Logger.getInstance().logRobotThread(Level.FINE, "Set setpoint", mAngle);
                drive.setVisionAngleSetpoint();
                commands.wantedDriveState = Drive.DriveState.ON_BOARD_CONTROLLER;
                mState = VisionAlignRoutine.GyroBBState.TURNING;
                break;
            case TURNING:
                if(drive.controllerOnTarget()) {
                    mState = VisionAlignRoutine.GyroBBState.DONE;
                }
                break;
            case TIMED_OUT:
                drive.setNeutral();
                break;
            case DONE:
                drive.resetController();
                break;
        }

        return commands;
    }

    @Override
    public Commands cancel(Commands commands) {
        mState = VisionAlignRoutine.GyroBBState.DONE;
        commands.wantedDriveState = Drive.DriveState.NEUTRAL;
        drive.setNeutral();
        return commands;
    }

    @Override
    public boolean finished() {
        return mState == VisionAlignRoutine.GyroBBState.DONE;
    }

    @Override
    public String getName() {
        return "BangBangGyroTurnAngleRoutine";
    }
}
