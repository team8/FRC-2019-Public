package com.palyrobotics.frc2019.behavior.routines.pusher;

import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.subsystems.Fingers;
import com.palyrobotics.frc2019.subsystems.Subsystem;
import com.palyrobotics.frc2019.behavior.Routine;

public class PusherInRoutine extends Routine {
    private boolean alreadyRan;

    @Override
    public void start() {
        alreadyRan = false;
    }

    @Override
    public Commands update(Commands commands) {
        commands.wantedPusherState = Fingers.PusherState.IN;
        alreadyRan = true;
        return commands;
    }

    @Override
    public Commands cancel(Commands commands) {
        return commands;
    }

    @Override
    public boolean finished() {
        return alreadyRan;
    }

    @Override
    public Subsystem[] getRequiredSubsystems() {
        return new Subsystem[] { fingers };
    }

    @Override
    public String getName() {
        return "PusherInRoutine";
    }
}