package com.palyrobotics.frc2019.config;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.subsystems.*;
import com.palyrobotics.frc2019.util.SparkDriveSignal;

import java.util.ArrayList;

/**
 * Commands represent the desired set points and subsystem states for the robot. <br />
 * Store Requests (enum) for each subsystem and set points {@link SetPoints} <br />
 * Variables are public and have default values to prevent NullPointerExceptions
 *
 * @author Nihar
 */
public class Commands {

    private static Commands sInstance = new Commands();

    public static Commands getInstance() {
        return sInstance;
    }

    public ArrayList<Routine> wantedRoutines = new ArrayList<>();

    // Store WantedStates for each subsystem state machine
    public Drive.DriveState wantedDriveState = Drive.DriveState.NEUTRAL;

    public Shooter.ShooterState wantedShooterState = Shooter.ShooterState.IDLE;

    public Pusher.PusherState wantedPusherInOutState = Pusher.PusherState.START;

    public Shovel.WheelState wantedShovelWheelState = Shovel.WheelState.IDLE;
    public Shovel.UpDownState wantedShovelUpDownState = Shovel.UpDownState.UP;

    public Fingers.FingersState wantedFingersOpenCloseState = Fingers.FingersState.OPEN;
    public Fingers.PushingState wantedFingersExpelState = Fingers.PushingState.CLOSED;
    public boolean blockFingers;

    public Intake.IntakeMacroState wantedIntakeState = Intake.IntakeMacroState.HOLDING;

    public boolean disableIntakeScaling = true;

    public boolean customShooterSpeed, customIntakeSpeed;

    public Elevator.ElevatorState wantedElevatorState = Elevator.ElevatorState.IDLE;
    public double customElevatorPercentOutput;

    public boolean customShovelSpeed;
    public boolean autoPlacerOutput;

    public boolean intakeHFX, intakeHasHatch;
    public boolean hasCargo, hasPusherCargo;

    public boolean shooterSpinning;

    public void addWantedRoutine(Routine wantedRoutine) {
        for (Routine routine : wantedRoutines) {
            if (routine.getClass().equals(wantedRoutine.getClass())) {
//                Logger.getInstance().logRobotThread(Level.WARNING, "tried to add duplicate routine", routine.getName());
                return;
            }
        }
        wantedRoutines.add(wantedRoutine);
    }

    public static Commands reset() {
        sInstance = new Commands();
        return sInstance;
    }

    /**
     * Stores numeric set points
     *
     * @author Nihar
     */
    public static class SetPoints {
        public SparkDriveSignal drivePowerSetPoint;
        public Double
                elevatorPositionSetPoint,
                climberPositionSetPoint,
                pusherPositionSetPoint,
                intakePositionSetPoint,
                climbRateSetPoint;

        /**
         * Resets all the set points
         */
        public void reset() {
            drivePowerSetPoint = null;
            elevatorPositionSetPoint = null;
            climberPositionSetPoint = null;
            pusherPositionSetPoint = null;
            intakePositionSetPoint = null;
            climbRateSetPoint = null;
        }
    }

    // All robot set points
    public SetPoints robotSetPoints = new SetPoints();

    // Allows you to cancel all running routines
    public boolean cancelCurrentRoutines;

    public void copyTo(Commands other) {
        other.wantedDriveState = this.wantedDriveState;
        other.wantedShooterState = this.wantedShooterState;
        other.wantedElevatorState = this.wantedElevatorState;
        other.cancelCurrentRoutines = this.cancelCurrentRoutines;
        other.wantedPusherInOutState = this.wantedPusherInOutState;
        other.wantedShovelWheelState = this.wantedShovelWheelState;
        other.wantedShovelUpDownState = this.wantedShovelUpDownState;
        other.wantedFingersOpenCloseState = this.wantedFingersOpenCloseState;
        other.wantedFingersExpelState = this.wantedFingersExpelState;
        other.customShooterSpeed = this.customShooterSpeed;
        other.customIntakeSpeed = this.customIntakeSpeed;
        other.customShovelSpeed = this.customShovelSpeed;
        other.autoPlacerOutput = this.autoPlacerOutput;
        other.wantedIntakeState = this.wantedIntakeState;
        other.disableIntakeScaling = this.disableIntakeScaling;
        other.hasPusherCargo = this.hasPusherCargo;
        other.shooterSpinning = this.shooterSpinning;

        other.wantedRoutines.addAll(this.wantedRoutines);

        // Copy optionals that are present
        other.robotSetPoints.drivePowerSetPoint = robotSetPoints.drivePowerSetPoint;
        other.robotSetPoints.elevatorPositionSetPoint = robotSetPoints.elevatorPositionSetPoint;
        other.robotSetPoints.climberPositionSetPoint = robotSetPoints.climberPositionSetPoint;
        other.robotSetPoints.intakePositionSetPoint = robotSetPoints.intakePositionSetPoint;
        other.robotSetPoints.pusherPositionSetPoint = robotSetPoints.pusherPositionSetPoint;
        other.robotSetPoints.climbRateSetPoint = robotSetPoints.climbRateSetPoint;
    }

    @Override
    public String toString() {
        StringBuilder log = new StringBuilder();
        log.append("Wanted routines: ");
        for (Routine r : this.wantedRoutines) {
            log.append(r.getName()).append(" ");
        }
        return log.append("\n").toString();
    }
}