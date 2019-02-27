package com.palyrobotics.frc2019.behavior.routines.shovel;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.routines.TimeoutRoutine;
import com.palyrobotics.frc2019.behavior.routines.elevator.ElevatorCustomPositioningRoutine;
import com.palyrobotics.frc2019.behavior.routines.fingers.FingersCloseRoutine;
import com.palyrobotics.frc2019.behavior.routines.fingers.FingersOpenRoutine;
import com.palyrobotics.frc2019.behavior.routines.intake.IntakeBeginCycleRoutine;
import com.palyrobotics.frc2019.behavior.routines.pusher.PusherOutRoutine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.Constants.ElevatorConstants;
import com.palyrobotics.frc2019.subsystems.Shovel;
import com.palyrobotics.frc2019.subsystems.Subsystem;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;

public class FullHatchIntakeCycle extends Routine {
    private boolean alreadyRan;
    private double timeout;
    private double startTime;

    public FullHatchIntakeCycle() { }
    @Override
    public void start() {
        startTime = System.currentTimeMillis();
        alreadyRan = false;
    }
    @Override
    public Commands update(Commands commands) {
        commands.addWantedRoutine(new SequentialRoutine(
                new ElevatorCustomPositioningRoutine(ElevatorConstants.kElevatorCargoHeight1Inches, 2),
                new FingersCloseRoutine(),
                new PusherOutRoutine(),
                new WaitForHatchIntakeCurrentSpike(Shovel.WheelState.INTAKING),
                new ShovelUpRoutine(),
                new TimeoutRoutine(1),
                new FingersOpenRoutine()));
                new ElevatorCustomPositioningRoutine(ElevatorConstants.kElevatorCargoHeight2Inches, 2);
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
        return new Subsystem[] { shovel };
    }

    @Override
    public String getName() {
        return "ShovelUpRoutine";
    }
}
