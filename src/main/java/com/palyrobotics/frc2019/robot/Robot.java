package com.palyrobotics.frc2019.robot;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.auto.AutoModeSelector;
import com.palyrobotics.frc2019.behavior.RoutineManager;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.RobotConfig;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.config.dashboard.LiveGraph;
import com.palyrobotics.frc2019.config.driveteam.DriveTeam;
import com.palyrobotics.frc2019.subsystems.*;
import com.palyrobotics.frc2019.util.commands.CommandReceiver;
import com.palyrobotics.frc2019.util.config.Configs;
import com.palyrobotics.frc2019.util.csvlogger.CSVWriter;
import com.palyrobotics.frc2019.util.service.RobotService;
import com.palyrobotics.frc2019.util.trajectory.RigidTransform2d;
import com.palyrobotics.frc2019.vision.Limelight;
import com.palyrobotics.frc2019.vision.LimelightControlMode;
import com.revrobotics.CANSparkMax.IdleMode;

import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;

public class Robot extends TimedRobot {

	private static final RobotState sRobotState = RobotState.getInstance();
	private static Commands sCommands = Commands.getInstance();
	private final Limelight mLimelight = Limelight.getInstance();
	private final RobotConfig mConfig = Configs.get(RobotConfig.class);
	private LiveGraph mLiveGraph = LiveGraph.getInstance();
	private OperatorInterface mOperatorInterface = OperatorInterface.getInstance();
	private RoutineManager mRoutineManager = RoutineManager.getInstance();
	boolean mAutoStarted = false;
	/* Subsystems */
	private Drive mDrive = Drive.getInstance();
	private Elevator mElevator = Elevator.getInstance();
	private Shooter mShooter = Shooter.getInstance();
	private Pusher mPusher = Pusher.getsInstance();
	private Fingers mFingers = Fingers.getInstance();
	private Intake mIntake = Intake.getInstance();
	private List<Subsystem> mSubsystems = List.of(mDrive, mElevator, mShooter, mPusher, mFingers, mIntake),
			mEnabledSubsystems;
	private HardwareUpdater mHardwareUpdater = new HardwareUpdater(mDrive, mElevator, mShooter, mPusher, mFingers,
			mIntake);
	private List<RobotService> mEnabledServices;
	private int mTick;

	public static RobotState getRobotState() {
		return sRobotState;
	}

	public static Commands getCommands() {
		return sCommands;
	}

	@Override
	public void robotInit() {
		System.out.println(AutoModeSelector.getInstance().getAutoMode().getRoutine());
		// ParseAutos pA = new ParseAutos();
		// System.out.println(pA.parseAuto("RoutineTest"));
		ObjectMapper mapper = new ObjectMapper().configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

		setupSubsystemsAndServices();

		mHardwareUpdater.initHardware();

		DriveTeam.configConstants();

		mEnabledServices.forEach(RobotService::start);

		if (RobotBase.isSimulation())
			sRobotState.matchStartTimeSeconds = Timer.getFPGATimestamp();

		Configs.listen(RobotConfig.class, config -> setIdleModes());
	}

	private void setIdleModes() {
		Function<Boolean, IdleMode> f = isEnabled() ? c -> IdleMode.kBrake // Always brake if enabled
				: c -> c ? IdleMode.kCoast : IdleMode.kBrake; // Set to config when disabled
		mHardwareUpdater.setDriveIdleMode(f.apply(mConfig.coastDriveIfDisabled));
		mHardwareUpdater.setElevatorIdleMode(f.apply(mConfig.coastElevatorIfDisabled));
		mHardwareUpdater.setArmIdleMode(f.apply(mConfig.coastArmIfDisabled));
	}

	@Override
	public void autonomousInit() {

		sRobotState.gamePeriod = RobotState.GamePeriod.AUTO;
		setupSubsystemsAndServices();

		mHardwareUpdater.initHardware();

		DriveTeam.configConstants();

		mEnabledServices.forEach(RobotService::start);

			sRobotState.matchStartTimeSeconds = Timer.getFPGATimestamp();

		Configs.listen(RobotConfig.class, config -> setIdleModes());
		mHardwareUpdater.updateState(sRobotState);
		mRoutineManager.reset(sCommands);
	}

	@Override
	public void autonomousPeriodic() {
		if (!this.mAutoStarted) {

			// Get the selected auto mode
			System.out.println("auto not started");
			AutoModeBase mode = AutoModeSelector.getInstance().getAutoMode();

			// Prestart and run the auto mode
			mode.preStart();
			mRoutineManager.addNewRoutine(mode.getRoutine());

			this.mAutoStarted = true;
		}
		if (this.mAutoStarted) {
			System.out.println(sCommands.wantedShooterState);
			sCommands = mRoutineManager.update((sCommands));
			mHardwareUpdater.updateState(sRobotState);
			for (Subsystem subsystem : mEnabledSubsystems) {
				subsystem.update(sCommands, sRobotState);
			}
			mHardwareUpdater.updateHardware();
		}
	}

	@Override
	public void testInit() {
		mEnabledSubsystems.forEach(Subsystem::reset);
		mHardwareUpdater.updateHardware();
		mTick = 0;
	}

	@Override
	public void testPeriodic() {
		mTick++;
		if (mTick % 2 == 0) {
			mLiveGraph.add("Left Ultrasonic",
					HardwareAdapter.getInstance().getIntake().intakeUltrasonicLeft.getRangeInches());
			mLiveGraph.add("Right Ultrasonic",
					HardwareAdapter.getInstance().getIntake().intakeUltrasonicRight.getRangeInches());
			mLiveGraph.add("Pusher Ultrasonic",
					HardwareAdapter.getInstance().getPusher().pusherUltrasonic.getRangeInches());
			mLiveGraph.add("Arm Potentiometer", HardwareAdapter.getInstance().getIntake().potentiometer.get());
		}
	}

	@Override
	public void teleopInit() {
		sRobotState.gamePeriod = RobotState.GamePeriod.TELEOP;
		mRoutineManager.reset(sCommands);
		sCommands.wantedDriveState = Drive.DriveState.CHEZY; // Switch to chezy after auto ends
		CSVWriter.cleanFile();
		mEnabledSubsystems.forEach(Subsystem::start);
		setIdleModes();
		sRobotState.matchStartTimeSeconds = Timer.getFPGATimestamp();

		// Set limelight to driver camera mode - redundancy for testing purposes
		mLimelight.setCamMode(LimelightControlMode.CamMode.DRIVER);
	}

	@Override
	public void teleopPeriodic() {
		sCommands = mRoutineManager.update(mOperatorInterface.updateCommands(sCommands));
		mHardwareUpdater.updateState(sRobotState);
		for (Subsystem subsystem : mEnabledSubsystems) {
			subsystem.update(sCommands, sRobotState);
		}
		mHardwareUpdater.updateHardware();
	}

	@Override
	public void robotPeriodic() {
		mEnabledServices.forEach(RobotService::update);
	}

	@Override
	public void disabledInit() {
		sRobotState.reset(0.0, new RigidTransform2d());
		sRobotState.resetUltrasonics();
		// Stops updating routines
		mRoutineManager.reset(sCommands);

		// Creates a new Commands instance in place of the old one
		sCommands = Commands.reset();

		sRobotState.gamePeriod = RobotState.GamePeriod.DISABLED;
		// Stop subsystems and reset their states
		mEnabledSubsystems.forEach(Subsystem::stop);
		mHardwareUpdater.updateHardware();

		mLimelight.setCamMode(LimelightControlMode.CamMode.DRIVER);
		mLimelight.setLEDMode(LimelightControlMode.LedMode.FORCE_OFF);

		HardwareAdapter.getInstance().getJoysticks().operatorXboxController.setRumble(false);
		setIdleModes();

		CSVWriter.write();

		// Manually run garbage collector
		System.gc();
	}

	@Override
	public void disabledPeriodic() {

	}

	private void setupSubsystemsAndServices() {
		Map<String, Supplier<RobotService>> configToService = Map.of("commandReceiver", CommandReceiver::new);
		mEnabledServices = mConfig.enabledServices.stream().map(serviceName -> configToService.get(serviceName).get())
				.collect(Collectors.toList());
		Map<String, Subsystem> configToSubsystem = mSubsystems.stream()
				.collect(Collectors.toMap(Subsystem::getConfigName, Function.identity()));
		mEnabledSubsystems = mConfig.enabledSubsystems.stream().map(configToSubsystem::get)
				.collect(Collectors.toList());
		System.out.println("Enabled subsystems: ");
		mEnabledSubsystems.forEach(System.out::println);
	}
}
