package com.palyrobotics.frc2019.behavior.routines.drive;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.subsystems.Drive;
import com.palyrobotics.frc2019.subsystems.Subsystem;

public class HoldHeadingRoutine extends Routine {

    private double mAngle;
    private TurnState mState;

    private enum TurnState {
        START, TURNING, DONE
    }

    @Override
    public void start() {
        drive.setNeutral();
        mAngle = robotState.drivePose.heading;
        mState = TurnState.START;
    }

    @Override
    public Commands update(Commands commands) {

        if(!commands.holdHeading) {
            mState = TurnState.DONE;
        }

        switch(mState) {
            case START:
                drive.setTurnAngleSetpoint(mAngle);
                commands.wantedDriveState = Drive.DriveState.ON_BOARD_CONTROLLER;
                mState = TurnState.TURNING;
                break;
            case TURNING:
                if(drive.controllerOnTarget()) {
                    mState = TurnState.DONE;
                }
                break;
            case DONE:
                drive.resetController();
                break;
        }

        return commands;
    }

    @Override
    public Commands cancel(Commands commands) {
        mState = TurnState.DONE;
        drive.setNeutral();
        return commands;
    }

    @Override
    public boolean finished() {
        return mState == TurnState.DONE;
    }

    @Override
    public Subsystem[] getRequiredSubsystems() {
        return new Subsystem[] { drive };
    }

    @Override
    public String getName() {
        return "HoldHeadingRoutine";
    }
}
