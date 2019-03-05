package com.palyrobotics.frc2019.behavior.routines.waits;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.subsystems.Pusher;
import com.palyrobotics.frc2019.subsystems.Subsystem;

public class WaitForHatchIntakeUp extends WaitRoutine {


    @Override
    public boolean isCompleted() {
        return robotState.hatchIntakeUp;
    }

    @Override
    public Subsystem[] getRequiredSubsystems() {
        return new Subsystem[]{shovel};
    }

    @Override
    public String getName() {
        return null;
    }
}
