package com.palyrobotics.frc2019.robot;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.elevator.ElevatorCustomPositioningRoutine;
import com.palyrobotics.frc2019.behavior.routines.fingers.FingersCloseRoutine;
import com.palyrobotics.frc2019.behavior.routines.fingers.FingersOpenRoutine;
import com.palyrobotics.frc2019.behavior.routines.intake.IntakeBeginCycleRoutine;
import com.palyrobotics.frc2019.behavior.routines.intake.IntakeLevelOneRocketRoutine;
import com.palyrobotics.frc2019.behavior.routines.intake.IntakeSetRoutine;
import com.palyrobotics.frc2019.behavior.routines.intake.IntakeUpRoutine;
import com.palyrobotics.frc2019.behavior.routines.pusher.PusherInRoutine;
import com.palyrobotics.frc2019.behavior.routines.shooter.ShooterExpelRoutine;
import com.palyrobotics.frc2019.behavior.routines.shovel.ShovelDownRoutine;
import com.palyrobotics.frc2019.behavior.routines.shovel.ShovelUpRoutine;
import com.palyrobotics.frc2019.behavior.routines.shovel.WaitForHatchIntakeCurrentSpike;
import com.palyrobotics.frc2019.behavior.routines.waits.WaitForArmCanTuck;
import com.palyrobotics.frc2019.behavior.routines.waits.WaitForElevatorCanMove;
import com.palyrobotics.frc2019.behavior.routines.waits.WaitForHatchIntakeUp;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.Constants.DrivetrainConstants;
import com.palyrobotics.frc2019.config.Constants.OtherConstants;
import com.palyrobotics.frc2019.config.ElevatorConfig;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.subsystems.*;
import com.palyrobotics.frc2019.subsystems.Intake.IntakeMacroState;
import com.palyrobotics.frc2019.util.JoystickInput;
import com.palyrobotics.frc2019.util.MathUtil;
import com.palyrobotics.frc2019.util.XboxInput;
import com.palyrobotics.frc2019.util.config.Configs;
import com.palyrobotics.frc2019.vision.Limelight;
import com.palyrobotics.frc2019.vision.LimelightControlMode;

/**
 * Used to produce Commands {@link Commands} from human input Singleton class. Should only be used in robot package.
 *
 * @author Nihar
 */
public class OperatorInterface {

    private static OperatorInterface sInstance = new OperatorInterface();

    public static OperatorInterface getInstance() {
        return sInstance;
    }

    private final Limelight mLimelight = Limelight.getInstance();

    private JoystickInput
            mDriveStick = Robot.getRobotState().leftStickInput,
            mTurnStick = Robot.getRobotState().rightStickInput;
//    private JoystickInput mClimbStick = Robot.getRobotState().backupStickInput;
    private XboxInput mOperatorXboxController;

//    public static boolean demandIntakeUp = true; // force intake to come up
//    public static boolean intakeRunning = false; // force intake to come up
    private static double
            intakeStartTime, // force intake to come up
            lastCancelTime;

    // Timestamp when a vision routine was last activated; helps us know when to turn LEDs off
    private static double visionStartTimeMs;

    private OperatorInterface() {
        mOperatorXboxController = Robot.getRobotState().operatorXboxControllerInput;
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
     * @param lastCommands Last commands
     */
    Commands updateCommands(Commands lastCommands) {
        Commands newCommands = lastCommands.copy();

        newCommands.cancelCurrentRoutines = false;

        /*
         * Drivetrain controls
         */
        if (lastCommands.wantedDriveState != Drive.DriveState.OFF_BOARD_CONTROLLER && lastCommands.wantedDriveState != Drive.DriveState.ON_BOARD_CONTROLLER) {
            newCommands.wantedDriveState = Drive.DriveState.CHEZY;
        }

        //More safety
        if (Math.abs(MathUtil.handleDeadBand(mDriveStick.getY(), DrivetrainConstants.kDeadband)) > 0.0
                || Math.abs(MathUtil.handleDeadBand(mTurnStick.getX(), DrivetrainConstants.kDeadband)) > 0.0) {
            newCommands.wantedDriveState = Drive.DriveState.CHEZY;
        }

        if (mTurnStick.getButtonPressed(3)) {
            visionStartTimeMs = System.currentTimeMillis();
            // Limelight vision tracking on
            if (mLimelight.getCamMode() != LimelightControlMode.CamMode.VISION) {
                mLimelight.setCamMode(LimelightControlMode.CamMode.VISION);
                mLimelight.setLEDMode(LimelightControlMode.LedMode.FORCE_ON); // Limelight LED on
            }

            newCommands.wantedDriveState = Drive.DriveState.VISION_ASSIST;
        } else {
            if (!mTurnStick.getButtonPressed(4)) {
                RobotState.getInstance().atVisionTargetThreshold = false;
            }
            if (System.currentTimeMillis() - visionStartTimeMs > OtherConstants.kVisionLEDTimeoutMillis) {
                mLimelight.setCamMode(LimelightControlMode.CamMode.DRIVER); // Limelight LED off
                mLimelight.setLEDMode(LimelightControlMode.LedMode.FORCE_OFF);
            }
        }

        if (mTurnStick.getButtonPressed(4)) {
            visionStartTimeMs = System.currentTimeMillis();
            // Limelight vision tracking on
            if (mLimelight.getCamMode() != LimelightControlMode.CamMode.VISION) {
                mLimelight.setCamMode(LimelightControlMode.CamMode.VISION);
                mLimelight.setLEDMode(LimelightControlMode.LedMode.FORCE_ON); // Limelight LED on
            }
            Drive.getInstance().setVisionClosedDriveController();
            newCommands.wantedDriveState = Drive.DriveState.CLOSED_VISION_ASSIST;
        } else {
            if (!mTurnStick.getButtonPressed(3)) {
                RobotState.getInstance().atVisionTargetThreshold = false;
            }
            if (System.currentTimeMillis() - visionStartTimeMs > OtherConstants.kVisionLEDTimeoutMillis) {
                mLimelight.setCamMode(LimelightControlMode.CamMode.DRIVER); // Limelight LED off
                mLimelight.setLEDMode(LimelightControlMode.LedMode.FORCE_OFF);
            }
        }

        /*
         * Hatch Ground Intake/Shovel Control
         */
//		if(mOperatorXboxController.getButtonX()) {
//			if(prevCommands.wantedShovelUpDownState == Shovel.UpDownState.UP) {
//				newCommands.wantedShovelUpDownState = Shovel.UpDownState.DOWN;
//				newCommands.cancelCurrentRoutines = true;
//			} else if (prevCommands.wantedShovelUpDownState == Shovel.UpDownState.DOWN) {
//				newCommands.wantedShovelUpDownState = Shovel.UpDownState.UP;
//				newCommands.cancelCurrentRoutines = true;
//			}
//		}

        if (mOperatorXboxController.getButtonX() && newCommands.wantedShovelUpDownState == Shovel.UpDownState.UP && (lastCancelTime + 200) < System.currentTimeMillis()) {
            intakeStartTime = System.currentTimeMillis();
            newCommands.addWantedRoutine(new SequentialRoutine(new ShovelDownRoutine(),
                    new FingersCloseRoutine(),
                    new PusherInRoutine(),
                    new WaitForHatchIntakeCurrentSpike(Shovel.WheelState.INTAKING),
                    new ShovelUpRoutine(),
                    new WaitForHatchIntakeUp(),
                    new FingersOpenRoutine()));
        } else if (mOperatorXboxController.getButtonX() && (System.currentTimeMillis() - 450 > OperatorInterface.intakeStartTime) && newCommands.wantedShovelUpDownState == Shovel.UpDownState.DOWN) {
            intakeStartTime = System.currentTimeMillis();
            newCommands.cancelCurrentRoutines = true;
            newCommands.wantedShovelUpDownState = Shovel.UpDownState.UP;
            lastCancelTime = System.currentTimeMillis();
        }

//        if(mOperatorXboxController.getButtonB()) {
//            Routine hatchCycle = new ShovelExpelCycle(FingerConstants.kFingersCycleTime);
//            newCommands.cancelCurrentRoutines = false;
//            newCommands.addWantedRoutine(hatchCycle);
//        }

//		newCommands.wantedElevatorState = Elevator.ElevatorState.MANUAL_POSITIONING;

        /*
         * Elevator Control
         */
        if (mOperatorXboxController.getButtonA()) { // Level 1
            Routine elevatorLevel1 = new ElevatorCustomPositioningRoutine(Configs.get(ElevatorConfig.class).elevatorHeight1, .1);
            newCommands.cancelCurrentRoutines = false;
            newCommands.addWantedRoutine(new SequentialRoutine(new PusherInRoutine(), new IntakeUpRoutine(), new WaitForElevatorCanMove(), elevatorLevel1));
        } else if (mOperatorXboxController.getButtonB()) { // Level 2
            double levelHeight;
            if (Robot.getRobotState().hasPusherCargoFar) { // Has cargo -> cargo height 2
                levelHeight = Configs.get(ElevatorConfig.class).elevatorCargoHeight2;
            } else { // hatch height 2
                levelHeight = Configs.get(ElevatorConfig.class).elevatorHatchHeight2;
            }
            Routine elevatorLevel2 = new ElevatorCustomPositioningRoutine(levelHeight, .1);
            newCommands.cancelCurrentRoutines = false;
            newCommands.addWantedRoutine(new SequentialRoutine(new PusherInRoutine(), new IntakeUpRoutine(), new WaitForElevatorCanMove(), elevatorLevel2, new WaitForArmCanTuck(), new IntakeSetRoutine()));
        } else if (mOperatorXboxController.getButtonY()) {
            Routine elevatorLevel3 = new ElevatorCustomPositioningRoutine(Configs.get(ElevatorConfig.class).elevatorHeight3, .1);
            newCommands.cancelCurrentRoutines = false;
//			newCommands.addWantedRoutine(elevatorLevel3);
//			newCommands.addWantedRoutine(new PusherInRoutine());
            newCommands.addWantedRoutine(new SequentialRoutine(new PusherInRoutine(), new IntakeUpRoutine(), new WaitForElevatorCanMove(), elevatorLevel3, new WaitForArmCanTuck(), new IntakeSetRoutine()));
        }


        /*
         * Cargo Intake Control
         */
        if (mOperatorXboxController.getdPadDown()) {
            newCommands.cancelCurrentRoutines = false;
            // newCommands.wantedIntakeState = Intake.IntakeMacroState.DOWN;
            newCommands.addWantedRoutine(new IntakeBeginCycleRoutine());
        } else if (mOperatorXboxController.getdPadUp()) {
            newCommands.cancelCurrentRoutines = false;
            newCommands.addWantedRoutine(new IntakeUpRoutine());
        } else if (mOperatorXboxController.getdPadRight()) {
            newCommands.cancelCurrentRoutines = false;
            newCommands.addWantedRoutine(new IntakeLevelOneRocketRoutine());
        } else if (mOperatorXboxController.getdPadLeft()) {
            newCommands.cancelCurrentRoutines = false;
            newCommands.wantedIntakeState = Intake.IntakeMacroState.STOWED;
        }

        if (mOperatorXboxController.getRightTriggerPressed() && newCommands.wantedIntakeState == Intake.IntakeMacroState.HOLDING_ROCKET) {
            newCommands.wantedIntakeState = Intake.IntakeMacroState.EXPELLING_ROCKET;
        } else if (!mOperatorXboxController.getRightTriggerPressed() && newCommands.wantedIntakeState == Intake.IntakeMacroState.EXPELLING_ROCKET) {
            newCommands.wantedIntakeState = Intake.IntakeMacroState.HOLDING_ROCKET;
        }

        if (mOperatorXboxController.getLeftTriggerPressed() && newCommands.wantedIntakeState == Intake.IntakeMacroState.HOLDING_ROCKET) {
            newCommands.wantedIntakeState = Intake.IntakeMacroState.INTAKING_ROCKET;
        } else if (!mOperatorXboxController.getLeftTriggerPressed() && newCommands.wantedIntakeState == IntakeMacroState.INTAKING_ROCKET) {
            newCommands.wantedIntakeState = Intake.IntakeMacroState.HOLDING_ROCKET;
        }

//		/*
//		 * Climber Control
//		 */
//		if(mClimbStick.getButtonPressed(6)) {
//			newCommands.wantedClimberState = Elevator.ClimberState.ON_MANUAL;
//		}
//		else {
//			newCommands.wantedClimberState = Elevator.ClimberState.IDLE;
//		}

        /*
         * Pusher Control
         */
        if (mOperatorXboxController.getLeftBumper()) {
            newCommands.wantedPusherInOutState = Pusher.PusherState.IN;
        } else if (mOperatorXboxController.getRightBumper()) {
            newCommands.wantedPusherInOutState = Pusher.PusherState.OUT;
        } else if (mDriveStick.getButtonPressed(7)) {
            newCommands.wantedPusherInOutState = Pusher.PusherState.IN;
        }

        /*
         * Pneumatic Hatch Pusher Control
         */
        if (mOperatorXboxController.getRightTriggerPressed() && newCommands.wantedIntakeState != IntakeMacroState.EXPELLING_ROCKET && !newCommands.blockFingers) {
//			Routine hatchCycle = new FingersCycleRoutine(FingerConstants.kFingersCycleTime);
//			newCommands.cancelCurrentRoutines = false;
//			newCommands.addWantedRoutine(hatchCycle);
            newCommands.wantedFingersOpenCloseState = Fingers.FingersState.CLOSE;
            newCommands.wantedFingersExpelState = Fingers.PushingState.EXPELLING;
        } else if (newCommands.wantedShovelUpDownState != Shovel.UpDownState.DOWN) {
            newCommands.wantedFingersOpenCloseState = Fingers.FingersState.OPEN;
            newCommands.wantedFingersExpelState = Fingers.PushingState.CLOSED;
        }

        if (mOperatorXboxController.getLeftTriggerPressed()) {
            newCommands.addWantedRoutine(new ShooterExpelRoutine(Shooter.ShooterState.SPIN_UP, 3));
        }

        /*
         * Cancel all Routines
         */
        if (mDriveStick.getTriggerPressed()) {
            newCommands.cancelCurrentRoutines = true;
        }
        return newCommands;
    }
}