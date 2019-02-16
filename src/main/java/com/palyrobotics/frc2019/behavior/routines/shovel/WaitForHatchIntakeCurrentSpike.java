package com.palyrobotics.frc2019.behavior.routines.shovel;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.subsystems.Pusher;
import com.palyrobotics.frc2019.subsystems.Shovel;
import com.palyrobotics.frc2019.subsystems.Subsystem;

public class WaitForHatchIntakeCurrentSpike extends Routine {

    private boolean done;
    private Shovel.WheelState wantedWheelState;


    public WaitForHatchIntakeCurrentSpike(Shovel.WheelState ws) {
        this.wantedWheelState = ws;

    }

    @Override
    public void start() {

    }

    @Override
    public Commands update(Commands commands) {
        done = commands.intakeHasHatch;
        commands.wantedShovelWheelState = wantedWheelState;
        return commands;
    }

    @Override
    public Commands cancel(Commands commands) {
        commands.wantedShovelWheelState = Shovel.WheelState.IDLE;
        return commands;
    }

    @Override
    public boolean finished() {
        return done;
    }

    @Override
    public Subsystem[] getRequiredSubsystems() {
        return new Subsystem[] { pusher };
    }

    @Override
    public String getName() {
        return "watch hatch intake";
    }
}
