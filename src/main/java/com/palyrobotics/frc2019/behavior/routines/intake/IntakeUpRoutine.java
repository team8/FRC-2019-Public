package com.palyrobotics.frc2019.behavior.routines.intake;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.subsystems.Intake;
import com.palyrobotics.frc2019.subsystems.Subsystem;

public class IntakeUpRoutine extends Routine {
    private boolean mAlreadyRan;

    @Override
    public void start() {
        mAlreadyRan = false;
    }

    @Override
    public Commands update(Commands commands) {
        commands.wantedIntakeState = Intake.IntakeMacroState.HOLDING_MID;
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
        return new Subsystem[] {mIntake};
    }

    @Override
    public String getName() {
        return "Intake Up Routine";
    }
}