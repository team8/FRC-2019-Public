package com.palyrobotics.frc2019.robot;

import com.palyrobotics.frc2019.behavior.RoutineManager;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.config.dashboard.DashboardManager;
import com.palyrobotics.frc2019.config.driveteam.DriveTeam;
import com.palyrobotics.frc2019.subsystems.*;
import com.palyrobotics.frc2019.util.commands.CommandReceiver;
import com.palyrobotics.frc2019.util.csvlogger.CSVWriter;
import com.palyrobotics.frc2019.util.loops.Looper;
import com.palyrobotics.frc2019.util.trajectory.RigidTransform2d;
import com.palyrobotics.frc2019.vision.Limelight;
import com.palyrobotics.frc2019.vision.LimelightControlMode;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.TimedRobot;

public class Robot extends TimedRobot {
    //Instantiate singleton classes
    private static RobotState robotState = RobotState.getInstance();

    public static RobotState getRobotState() {
        return robotState;
    }

    //Single instance to be passed around
    private static Commands commands = Commands.getInstance();

    public static Commands getCommands() {
        return commands;
    }

    private OperatorInterface operatorInterface = OperatorInterface.getInstance();
    private RoutineManager mRoutineManager = RoutineManager.getInstance();

    //Subsystem controllers
    private Drive mDrive = Drive.getInstance();
    private Elevator mElevator = Elevator.getInstance();
    private Shovel mShovel = Shovel.getInstance();
    private Shooter mShooter = Shooter.getInstance();
    private Pusher mPusher = Pusher.getInstance();
    private Fingers mFingers = Fingers.getInstance();
    private Intake mIntake = Intake.getInstance();

    //Hardware Updater
    private HardwareUpdater mHardwareUpdater = new HardwareUpdater(mDrive, mElevator, mShooter, mPusher, mShovel, mFingers, mIntake);

    // Started boolean for if auto has been started.
    private boolean mAutoStarted = false;

    private int disabledCycles;

    private boolean breakoutTeleopInitCalled = false;

    public Looper looper;

    private CommandReceiver mCommandReceiver = new CommandReceiver();

    @Override
    public void robotInit() {

//        Logger.getInstance().setFileName("3-2-Testing");
//        DataLogger.getInstance().setFileName("3-2-Testing");
//
//        Logger.getInstance().start();
//        DataLogger.getInstance().start();
//
//        Logger.getInstance().logRobotThread(Level.INFO, "Start robotInit()");

        DashboardManager.getInstance().robotInit();

        mHardwareUpdater.initHardware();

        mElevator.resetWantedPosition();
        this.looper = new Looper();
//		looper.register(mHardwareUpdater.logLoop);

        CSVWriter.cleanFile();

        DriveTeam.configConstants();

        mCommandReceiver.start();

        if (RobotBase.isSimulation()) robotState.matchStartTimeMs = System.currentTimeMillis();

//        Logger.getInstance().logRobotThread(Level.INFO, "End robotInit()");

    }


    @Override
    public void autonomousInit() {

        teleopInit();

//        if(robotState.cancelAuto) {
//            robotState.gamePeriod = RobotState.GamePeriod.TELEOP;
//            teleopInit();
//            breakoutTeleopInitCalled = true;
//        } else {
//
//            Logger.getInstance().start();
//            DataLogger.getInstance().start();
//
//            Logger.getInstance().logRobotThread(Level.INFO, "Start autoInit()");
//
//            looper.start();
//
//            DashboardManager.getInstance().toggleCANTable(true);
//            robotState.gamePeriod = RobotState.GamePeriod.AUTO;
//
//            robotState.matchStartTime = System.currentTimeMillis();
//
//            mHardwareUpdater.updateState(robotState);
//            mRoutineManager.reset(commands);
//            robotState.reset(0, new RigidTransform2d());
//            //		commands.wantedIntakeUpDownState = Intake.UpDownState.UP;
//
//            // Limelight LED on
////         Limelight.getInstance().setLEDMode(LimelightControlMode.LedMode.FORCE_ON);
//
//            mWriter.cleanFile();
//
//            AutoDistances.updateAutoDistances();
//
//            mWriter.cleanFile();
//
//            startSubsystems();
//            mHardwareUpdater.enableBrakeMode();
//
//            Logger.getInstance().logRobotThread(Level.INFO, "End autoInit()");
//        }

    }


    @Override
    public void autonomousPeriodic() {

//        System.out.println("Left Encoder: " + robotState.drivePose.leftEnc);
//        System.out.println("Right Encoder: " + robotState.drivePose.rightEnc);
//        System.out.println("Gyro Heading: " + robotState.drivePose.heading);

        teleopPeriodic();

//         if(robotState.cancelAuto) {
//			 robotState.gamePeriod = RobotState.GamePeriod.TELEOP;
//			 if(breakoutTeleopInitCalled) {
//			 	teleopInit();
//			 	breakoutTeleopInitCalled = true;
//			 }
//			 teleopPeriodic();
//			 mRoutineManager.reset(commands);
//			 System.out.println("CANCELING AUTO, MOVING TO TELE");
//         } else {
//             long start = System.nanoTime();
//             if (!this.mAutoStarted) {
//                 //Get the selected auto mode
//                 AutoModeBase mode = AutoModeSelector.getInstance().getAutoMode();
//
//                 //Prestart and run the auto mode
//                 mode.prestart();
//                 mRoutineManager.addNewRoutine(mode.getRoutine());
//
//                 this.mAutoStarted = true;
//             }
//             if (this.mAutoStarted) {
//                 commands = mRoutineManager.update(commands);
//                 mHardwareUpdater.updateState(robotState);
//                 updateSubsystems();
//                 mHardwareUpdater.updateHardware();
//             }
//
//             if (mWriter.getSize() > 10000) {
//                 mWriter.write();
//             }
//
//             DataLogger.getInstance().logData(Level.FINE, "loop_dt", (System.nanoTime() - start) / 1.0e6);
//             DataLogger.getInstance().cycle();
//         }
    }

    @Override
    public void teleopInit() {
//		System.out.println("TELE STARTED");
//        Logger.getInstance().start();
//        DataLogger.getInstance().start();
//
//        Logger.getInstance().logRobotThread(Level.INFO, "Start teleopInit()");

        looper.start();

        robotState.gamePeriod = RobotState.GamePeriod.TELEOP;
        robotState.reset(0.0, new RigidTransform2d());
        mHardwareUpdater.updateState(robotState);
        mHardwareUpdater.updateHardware();
        mRoutineManager.reset(commands);
        DashboardManager.getInstance().toggleCANTable(true);
        commands.wantedDriveState = Drive.DriveState.CHEZY; //switch to chezy after auto ends
        commands = operatorInterface.updateCommands(commands);
        CSVWriter.cleanFile();
        startSubsystems();
        mHardwareUpdater.enableBrakeMode();
        robotState.reset(0, new RigidTransform2d());
        robotState.matchStartTimeMs = System.currentTimeMillis();

        // Set limelight to driver camera mode - redundancy for testing purposes
        Limelight.getInstance().setCamMode(LimelightControlMode.CamMode.DRIVER);

//        Logger.getInstance().logRobotThread(Level.INFO, "End teleopInit()");

    }

    @Override
    public void teleopPeriodic() {
        long startTime = System.nanoTime();

        long startCommands = System.nanoTime();
        commands = mRoutineManager.update(operatorInterface.updateCommands(commands));
        double commandTime = (System.nanoTime() - startCommands) / 1e9;

        long startUpdateState = System.nanoTime();
        mHardwareUpdater.updateState(robotState);
        double updateStateTime = (System.nanoTime() - startUpdateState) / 1e9;

        long startUpdateSubsystem = System.nanoTime();
        updateSubsystems();
        double updateSubsystemTime = (System.nanoTime() - startUpdateSubsystem) / 1e9;

        //Update the hardware
        long startUpdateHardware = System.nanoTime();
        mHardwareUpdater.updateHardware();
        double updateHardwareTime = (System.nanoTime() - startUpdateHardware) / 1e9;

//        if (mWriter.getSize() > 2000) {
//            mWriter.write();
//        }

//        DataLogger.getInstance().logData(Level.FINE, "loop_dt", (System.nanoTime() - start) / 1.0e6);
//        DataLogger.getInstance().cycle();

        long endTime = System.nanoTime();
        double loopTimeSeconds = (endTime - startTime) / 1e9;
        if (loopTimeSeconds >= 0.02) {
            System.out.println("===== Overrun Report =====");
            System.out.printf("Loop time: %f\n", loopTimeSeconds);
            System.out.printf("Command time: %f\n", commandTime);
            System.out.printf("Update State time: %f\n", updateStateTime);
            System.out.printf("Update Subsystems time: %f\n", updateSubsystemTime);
            System.out.printf("Update Hardware time: %f\n", updateHardwareTime);
        }

//		System.out.println("Limelight zdist: " + Limelight.getInstance().getCorrectedEstimatedDistanceZ());
    }

    @Override
    public void disabledInit() {

//        Logger.getInstance().logRobotThread(Level.INFO, "Start disabledInit()");
//        Logger.getInstance().logRobotThread(Level.INFO, "Stopping logger...");
//
//        Logger.getInstance().cleanup();
//        DataLogger.getInstance().cleanup();

        mAutoStarted = false;

        looper.stop();

        robotState.reset(0, new RigidTransform2d());
        //Stops updating routines
        mRoutineManager.reset(commands);
        //Creates a new Commands instance in place of the old one
        Commands.reset();
        commands = Commands.getInstance();

        robotState.gamePeriod = RobotState.GamePeriod.DISABLED;
        //Stop controllers
        mDrive.setNeutral();
        stopSubsystems();

        // Set Limelight to vision pipeline to enable pit testing
        Limelight.getInstance().setCamMode(LimelightControlMode.CamMode.VISION);
        Limelight.getInstance().setLEDMode(LimelightControlMode.LedMode.CURRENT_PIPELINE_MODE);
        HardwareAdapter.getInstance().getJoysticks().operatorXboxController.setRumble(false);
        mHardwareUpdater.disableBrakeMode();

        CSVWriter.write();

        //Manually run garbage collector
        System.gc();
    }

    @Override
    public void disabledPeriodic() {
//		System.out.println("Pot: " + HardwareAdapter.getInstance().getIntake().potentiometer.get());
//        System.out.println("Pusher Ultrasonic: " + HardwareAdapter.getInstance().getPusher().pusherUltrasonic.getRangeInches());
//        System.out.println("Left Ultrasonic: " + HardwareAdapter.getInstance().getIntake().intakeUltrasonicLeft.getRangeInches());
//        System.out.println("Right Ultrasonic: " + HardwareAdapter.getInstance().getIntake().intakeUltrasonicRight.getRangeInches());
//		System.out.println("PusherBackup Ultrasonic: " + HardwareAdapter.getInstance().getPusher().pusherSecondaryUltrasonic.getRangeInches());

//		System.out.println(HardwareAdapter.getInstance().getIntake().intakeMasterSpark.getEncoder().getPosition());

//        System.out.println();
//        System.out.println();
    }

    private void startSubsystems() {
        mShovel.start();
        mDrive.start();
        mElevator.start();
        mShooter.start();
        mPusher.start();
        mFingers.start();
//		mIntake.start();
    }

    private void updateSubsystems() {
//        mDrive.update(commands, robotState);
        mElevator.update(commands, robotState);
//        mShooter.update(commands, robotState);
//        mPusher.update(commands, robotState);
//        mFingers.update(commands, robotState);
//        mShovel.update(commands, robotState);
//		mIntake.update(commands, robotState);
    }


    private void stopSubsystems() {
        mDrive.stop();
        mElevator.stop();
        mShooter.stop();
        mPusher.stop();
        mFingers.stop();
        mShovel.stop();
//		mIntake.stop();
    }

    @Override
    public void robotPeriodic() {

        mCommandReceiver.update();

        // System.out.println("intake_enc: " + HardwareAdapter.getInstance().getIntake().intakeMasterSpark.getEncoder().getPosition());
        // System.out.println("intake_pot: " + HardwareAdapter.getInstance().getIntake().potentiometer.get());
        // System.out.println("left ultrasonic: " + HardwareAdapter.getInstance().getIntake().intakeUltrasonicLeft.getRangeInches());
        // System.out.println("right ultrasonic: " + HardwareAdapter.getInstance().getIntake().intakeUltrasonicRight.getRangeInches());
    }
}
