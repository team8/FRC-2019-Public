package com.palyrobotics.frc2019.behavior.routines.pusher;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.subsystems.Pusher;
import com.palyrobotics.frc2019.subsystems.Subsystem;

public class PusherOutRoutine extends Routine {
    private boolean mAlreadyRan;

    @Override
    public void start() {
        mAlreadyRan = false;
    }

    @Override
    public Commands update(Commands commands) {
        commands.wantedPusherInOutState = Pusher.PusherState.OUT;
        mAlreadyRan = true;
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
        return new Subsystem[]{mPusher};
    }

    @Override
    public String getName() {
        return "Pusher Out Routine";
    }
}
