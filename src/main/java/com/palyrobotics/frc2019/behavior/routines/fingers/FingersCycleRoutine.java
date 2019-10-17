package com.palyrobotics.frc2019.behavior.routines.fingers;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.subsystems.Fingers;
import com.palyrobotics.frc2019.subsystems.Subsystem;

public class FingersCycleRoutine extends Routine {

    private boolean mAlreadyRan;
    private double mTimeout, mStartTime;

    public FingersCycleRoutine(double timeout) {
        mTimeout = timeout * 1000;
    }

    @Override
    public void start() {
        mStartTime = System.currentTimeMillis();
        mAlreadyRan = false;
    }

    @Override
    public Commands update(Commands commands) {
        commands.wantedFingersOpenCloseState = Fingers.FingersState.CLOSE;
        commands.wantedFingersExpelState = Fingers.PushingState.CLOSED;
        if (System.currentTimeMillis() > mTimeout + mStartTime) {
            commands.wantedFingersOpenCloseState = Fingers.FingersState.OPEN;
            commands.wantedFingersExpelState = Fingers.PushingState.CLOSED;
            mAlreadyRan = true;
        }
        return commands;
    }

    @Override
    public Commands cancel(Commands commands) {
        return commands;
    }

    @Override
    public boolean finished() {
        return mAlreadyRan;
    }

    @Override
    public Subsystem[] getRequiredSubsystems() {
        return new Subsystem[]{mFingers};
    }

    @Override
    public String getName() {
        return "Fingers Close Routine";
    }
}