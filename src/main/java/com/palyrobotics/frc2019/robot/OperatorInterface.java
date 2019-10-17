package com.palyrobotics.frc2019.robot;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.elevator.ElevatorCustomPositioningRoutine;
import com.palyrobotics.frc2019.behavior.routines.intake.IntakeBeginCycleRoutine;
import com.palyrobotics.frc2019.behavior.routines.intake.IntakeLevelOneRocketRoutine;
import com.palyrobotics.frc2019.behavior.routines.intake.IntakeSetRoutine;
import com.palyrobotics.frc2019.behavior.routines.intake.IntakeUpRoutine;
import com.palyrobotics.frc2019.behavior.routines.pusher.PusherInRoutine;
import com.palyrobotics.frc2019.behavior.routines.shooter.ShooterExpelRoutine;
import com.palyrobotics.frc2019.behavior.routines.waits.WaitForArmCanTuck;
import com.palyrobotics.frc2019.behavior.routines.waits.WaitForElevatorCanMove;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.config.constants.DrivetrainConstants;
import com.palyrobotics.frc2019.config.constants.OtherConstants;
import com.palyrobotics.frc2019.config.subsystem.ElevatorConfig;
import com.palyrobotics.frc2019.subsystems.*;
import com.palyrobotics.frc2019.subsystems.Intake.IntakeMacroState;
import com.palyrobotics.frc2019.util.config.Configs;
import com.palyrobotics.frc2019.util.input.Joystick;
import com.palyrobotics.frc2019.util.input.XboxController;
import com.palyrobotics.frc2019.vision.Limelight;
import com.palyrobotics.frc2019.vision.LimelightControlMode;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.Timer;

/**
 * Used to produce {@link Commands}'s from human input. Should only be used in robot package.
 *
 * @author Nihar
 */
public class OperatorInterface {

    private static OperatorInterface sInstance = new OperatorInterface();

    public static OperatorInterface getInstance() {
        return sInstance;
    }

    private final Limelight mLimelight = Limelight.getInstance();

    private final Joystick mDriveStick = new Joystick(0), mTurnStick = new Joystick(1);
    private final XboxController mOperatorXboxController = new XboxController(2);

    // Timestamp when a vision routine was last activated; helps us know when to turn LEDs off
    private double mVisionStartTimeSeconds;

    private OperatorInterface() {
    }

    /**
     * Helper method to only add routines that aren't already in wantedRoutines
     *
     * @param commands      Current set of commands being modified
     * @param wantedRoutine Routine to add to the commands
     * @return whether or not wantedRoutine was successfully added
     */
    private boolean addWantedRoutine(Commands commands, Routine wantedRoutine) {
        for (Routine routine : commands.wantedRoutines) {
            if (routine.getClass().equals(wantedRoutine.getClass())) {
                return false;
            }
        }
        commands.wantedRoutines.add(wantedRoutine);
        return true;
    }

    /**
     * Returns modified commands
     *
     * @param commands Last commands
     */
    Commands updateCommands(Commands commands) {

        commands.cancelCurrentRoutines = false;

        /*
         * Drivetrain controls
         */
        if (commands.wantedDriveState != Drive.DriveState.OFF_BOARD_CONTROLLER && commands.wantedDriveState != Drive.DriveState.ON_BOARD_CONTROLLER) {
            commands.wantedDriveState = Drive.DriveState.CHEZY;
        }
        // More safety
        if (Math.abs(mDriveStick.getY()) > DrivetrainConstants.kDeadband || Math.abs(mTurnStick.getX()) > DrivetrainConstants.kDeadband) {
            commands.wantedDriveState = Drive.DriveState.CHEZY;
        }
        commands.driveThrottle = -mDriveStick.getThrottle();
        commands.driveWheel = mDriveStick.getX();
        commands.isQuickTurn = mDriveStick.getTrigger();
        commands.isBraking = mTurnStick.getTrigger();

        if (mTurnStick.getRawButton(3)) {
            mVisionStartTimeSeconds = Timer.getFPGATimestamp();
            // Limelight vision tracking on
            setVision(true);
            commands.wantedDriveState = Drive.DriveState.VISION_ASSIST;
        } else {
            if (!mTurnStick.getRawButton(4)) {
                RobotState.getInstance().atVisionTargetThreshold = false;
            }
            if (Timer.getFPGATimestamp() - mVisionStartTimeSeconds > OtherConstants.kVisionLEDTimeoutSeconds) {
                setVision(false);
            }
        }

        if (mTurnStick.getRawButton(4)) {
            mVisionStartTimeSeconds = Timer.getFPGATimestamp();
            // Limelight vision tracking on
            setVision(true);
            Drive.getInstance().setVisionClosedDriveController();
            commands.wantedDriveState = Drive.DriveState.CLOSED_VISION_ASSIST;
        } else {
            if (!mTurnStick.getRawButton(3)) {
                RobotState.getInstance().atVisionTargetThreshold = false;
            }
            if (Timer.getFPGATimestamp() - mVisionStartTimeSeconds > OtherConstants.kVisionLEDTimeoutSeconds) {
                setVision(false);
            }
        }

//        /*
//         * Hatch Ground Intake/Shovel Control
//         */
//		if(mOperatorXboxController.getButtonX()) {
//			if(prevCommands.wantedShovelUpDownState == Shovel.UpDownState.UP) {
//				newCommands.wantedShovelUpDownState = Shovel.UpDownState.DOWN;
//				newCommands.cancelCurrentRoutines = true;
//			} else if (prevCommands.wantedShovelUpDownState == Shovel.UpDownState.DOWN) {
//				newCommands.wantedShovelUpDownState = Shovel.UpDownState.UP;
//				newCommands.cancelCurrentRoutines = true;
//			}
//		}
//
//        if (mOperatorXboxController.getButtonX() && commands.wantedShovelUpDownState == Shovel.UpDownState.UP && (mLastCancelTime + 200) < System.currentTimeMillis()) {
//            mIntakeStartTime = System.currentTimeMillis();
//            commands.addWantedRoutine(new SequentialRoutine(new ShovelDownRoutine(),
//                    new FingersRoutine(Fingers.FingersState.CLOSE),
//                    new PusherInRoutine(),
//                    new WaitForHatchIntakeCurrentSpike(Shovel.WheelState.INTAKING),
//                    new ShovelUpRoutine(),
//                    new WaitForHatchIntakeUp(),
//                    new FingersRoutine(Fingers.FingersState.OPEN)));
//        } else if (mOperatorXboxController.getButtonX() && (System.currentTimeMillis() - 450 > mIntakeStartTime) && commands.wantedShovelUpDownState == Shovel.UpDownState.DOWN) {
//            mIntakeStartTime = System.currentTimeMillis();
//            commands.cancelCurrentRoutines = true;
//            commands.wantedShovelUpDownState = Shovel.UpDownState.UP;
//            mLastCancelTime = System.currentTimeMillis();
//        }
//
//        if(mOperatorXboxController.getButtonB()) {
//            Routine hatchCycle = new ShovelExpelCycle(FingerConstants.kFingersCycleTime);
//            newCommands.cancelCurrentRoutines = false;
//            newCommands.addWantedRoutine(hatchCycle);
//        }

        /*
         * Elevator Control
         */
        ElevatorConfig elevatorConfig = Configs.get(ElevatorConfig.class);
        double rightStick = mOperatorXboxController.getY(Hand.kRight);
        if (Math.abs(rightStick) > 0.1) {
            commands.wantedElevatorState = Elevator.ElevatorState.MANUAL_VELOCITY;
            commands.customElevatorVelocity = rightStick * elevatorConfig.manualPowerMultiplier;
        }
        if (mOperatorXboxController.getAButtonPressed()) { // Level 1
            Routine elevatorLevel1 = new ElevatorCustomPositioningRoutine(elevatorConfig.elevatorHeight1, 1.0);
            commands.cancelCurrentRoutines = false;
            commands.addWantedRoutine(new SequentialRoutine(new PusherInRoutine(), new IntakeUpRoutine(), new WaitForElevatorCanMove(), elevatorLevel1));
        } else if (mOperatorXboxController.getBButtonPressed()) { // Level 2
            double levelHeight = Robot.getRobotState().hasPusherCargoFar
                    ? elevatorConfig.elevatorCargoHeight2
                    : elevatorConfig.elevatorHatchHeight2;
            // Has cargo -> cargo height 2
            Routine elevatorLevel2 = new ElevatorCustomPositioningRoutine(levelHeight, 1.0);
            commands.cancelCurrentRoutines = false;
            commands.addWantedRoutine(new SequentialRoutine(new PusherInRoutine(), new IntakeUpRoutine(), new WaitForElevatorCanMove(), elevatorLevel2, new WaitForArmCanTuck(), new IntakeSetRoutine()));
        } else if (mOperatorXboxController.getYButtonPressed()) { // Level 3
            Routine elevatorLevel3 = new ElevatorCustomPositioningRoutine(elevatorConfig.elevatorHeight3, 1.0);
            commands.cancelCurrentRoutines = false;
            commands.addWantedRoutine(new SequentialRoutine(new PusherInRoutine(), new IntakeUpRoutine(), new WaitForElevatorCanMove(), elevatorLevel3, new WaitForArmCanTuck(), new IntakeSetRoutine()));
        }

        /*
         * Cargo Intake Control
         */
        if (mOperatorXboxController.getDPadDown()) {
            commands.cancelCurrentRoutines = false;
            // newCommands.wantedIntakeState = Intake.IntakeMacroState.DOWN;
            commands.addWantedRoutine(new IntakeBeginCycleRoutine());
        } else if (mOperatorXboxController.getDPadUp()) {
            commands.cancelCurrentRoutines = false;
            commands.addWantedRoutine(new IntakeUpRoutine());
        } else if (mOperatorXboxController.getDPadRight()) {
            commands.cancelCurrentRoutines = false;
            commands.addWantedRoutine(new IntakeLevelOneRocketRoutine());
        } else if (mOperatorXboxController.getDPadLeft()) {
            commands.cancelCurrentRoutines = false;
            commands.wantedIntakeState = Intake.IntakeMacroState.STOWED;
        }

        if (mOperatorXboxController.getRightTriggerPressed() && commands.wantedIntakeState == Intake.IntakeMacroState.HOLDING_ROCKET) {
            commands.wantedIntakeState = Intake.IntakeMacroState.EXPELLING_ROCKET;
        } else if (!mOperatorXboxController.getRightTriggerPressed() && commands.wantedIntakeState == Intake.IntakeMacroState.EXPELLING_ROCKET) {
            commands.wantedIntakeState = Intake.IntakeMacroState.HOLDING_ROCKET;
        }

        if (mOperatorXboxController.getLeftTriggerPressed() && commands.wantedIntakeState == Intake.IntakeMacroState.HOLDING_ROCKET) {
            commands.wantedIntakeState = Intake.IntakeMacroState.INTAKING_ROCKET;
        } else if (!mOperatorXboxController.getLeftTriggerPressed() && commands.wantedIntakeState == IntakeMacroState.INTAKING_ROCKET) {
            commands.wantedIntakeState = Intake.IntakeMacroState.HOLDING_ROCKET;
        }

        /* Pusher control */
        if (mOperatorXboxController.getLeftBumperPressed()) {
            commands.wantedPusherInOutState = Pusher.PusherState.IN;
        } else if (mOperatorXboxController.getRightBumperPressed()) {
            commands.wantedPusherInOutState = Pusher.PusherState.OUT;
        } else if (mDriveStick.getRawButtonPressed(7)) {
            commands.wantedPusherInOutState = Pusher.PusherState.IN;
        }

        /* Pneumatic hatch pusher control */
        if (mOperatorXboxController.getRightTriggerPressed() && commands.wantedIntakeState != IntakeMacroState.EXPELLING_ROCKET) {
            commands.wantedFingersOpenCloseState = Fingers.FingersState.CLOSE;
            commands.wantedFingersExpelState = Fingers.PushingState.EXPELLING;
        }

        if (mOperatorXboxController.getLeftTriggerPressed()) {
            commands.addWantedRoutine(new ShooterExpelRoutine(Shooter.ShooterState.SPIN_UP, 3));
        }

        if (mDriveStick.getTriggerPressed()) {
            commands.cancelCurrentRoutines = true;
        }

        mOperatorXboxController.updateLastInputs();

        return commands;
    }

    private void setVision(boolean on) {
        mLimelight.setCamMode(on ? LimelightControlMode.CamMode.VISION : LimelightControlMode.CamMode.DRIVER); // Limelight LED off
        mLimelight.setLEDMode(on ? LimelightControlMode.LedMode.FORCE_ON : LimelightControlMode.LedMode.FORCE_OFF);
    }
}