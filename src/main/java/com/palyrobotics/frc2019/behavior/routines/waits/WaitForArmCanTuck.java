package com.palyrobotics.frc2019.behavior.routines.waits;

import com.palyrobotics.frc2019.config.ElevatorConfig;
import com.palyrobotics.frc2019.subsystems.Subsystem;
import com.palyrobotics.frc2019.util.config.Configs;

public class WaitForArmCanTuck extends WaitRoutine {
    @Override
    public boolean isCompleted() {
        return robotState.elevatorPosition > Configs.get(ElevatorConfig.class).secondStageCanStartMovingArm;
    }

    @Override
    public Subsystem[] getRequiredSubsystems() {
        return new Subsystem[]{intake};
    }

    @Override
    public String getName() {
        return "Can Tuck Routine";
    }
}
