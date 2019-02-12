package com.palyrobotics.frc2019.robot;

import com.ctre.phoenix.motorcontrol.*;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.ctre.phoenix.sensors.PigeonIMU;
import com.palyrobotics.frc2019.config.Constants.*;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.subsystems.*;
import com.palyrobotics.frc2019.util.SparkMaxOutput;
import com.palyrobotics.frc2019.util.TalonSRXOutput;
import com.palyrobotics.frc2019.util.logger.Logger;
import com.palyrobotics.frc2019.util.trajectory.Kinematics;
import com.palyrobotics.frc2019.util.trajectory.RigidTransform2d;
import com.palyrobotics.frc2019.util.trajectory.Rotation2d;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import com.revrobotics.ControlType;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;

import java.util.Optional;
import java.util.logging.Level;

/**
 * Should only be used in robot package.
 */

class HardwareUpdater {

	//Subsystem references
	private Drive mDrive;
	private Intake mIntake;
	private Elevator mElevator;
	private Shooter mShooter;
	private Pusher mPusher;
	private Shovel mShovel;
	private Fingers mFingers;

	/**
	 * Hardware Updater for Vidar
	 */
	protected HardwareUpdater(Drive drive, Elevator elevator, Shooter shooter, Pusher pusher, Shovel shovel, Fingers fingers, Intake intake) {
		this.mDrive = drive;
		this.mElevator = elevator;
		this.mShooter = shooter;
		this.mPusher = pusher;
		this.mShovel = shovel;
		this.mFingers = fingers;
		this.mIntake = intake;
	}

	/**
	 * Initialize all hardware
	 */
	void initHardware() {
		Logger.getInstance().logRobotThread(Level.INFO, "Init hardware");
		configureHardware();
		startIntakeArm(); // just sensor wise
		startUltrasonics();
	}

	void disableSpeedControllers() {
		Logger.getInstance().logRobotThread(Level.INFO, "Disabling sparks");

		//Disable drivetrain sparks
		HardwareAdapter.getInstance().getDrivetrain().leftMasterSpark.disable();
		HardwareAdapter.getInstance().getDrivetrain().leftSlave1Spark.disable();
		HardwareAdapter.getInstance().getDrivetrain().leftSlave2Spark.disable();

		HardwareAdapter.getInstance().getDrivetrain().rightMasterSpark.disable();
		HardwareAdapter.getInstance().getDrivetrain().rightSlave1Spark.disable();
		HardwareAdapter.getInstance().getDrivetrain().rightSlave2Spark.disable();

		//Disable elevator sparks
        HardwareAdapter.getInstance().getElevator().elevatorMasterSpark.disable();
        HardwareAdapter.getInstance().getElevator().elevatorSlaveSpark.disable();

		//Disable intake sparks
		HardwareAdapter.getInstance().getIntake().intakeMasterSpark.disable();
		HardwareAdapter.getInstance().getIntake().intakeSlaveSpark.disable();
		HardwareAdapter.getInstance().getIntake().intakeVictor.set(ControlMode.Disabled, 0);

		//Disable pusher sparks
		HardwareAdapter.getInstance().getPusher().pusherSpark.disable();

		Logger.getInstance().logRobotThread(Level.INFO, "Disabling victors");

		// Disable shooter victors
		HardwareAdapter.getInstance().getShooter().shooterMasterVictor.set(ControlMode.Disabled, 0);
		HardwareAdapter.getInstance().getShooter().shooterSlaveVictor.set(ControlMode.Disabled, 0);
	}

	void configureHardware() {
		System.out.println("0");
		configureShovelHardware();
		System.out.println("1");
		configureDriveHardware();
		System.out.println("2");
		configureElevatorHardware();
		System.out.println("3");
		configureIntakeHardware();
		System.out.println("4");
		configureShooterHardware();
		System.out.println("5");
		configurePusherHardware();
		System.out.println("6");
	}

	void configureDriveHardware() {
		PigeonIMU gyro = HardwareAdapter.getInstance().getDrivetrain().gyro;
		gyro.setYaw(0, 0);
		gyro.setFusedHeading(0, 0);

		CANSparkMax leftMasterSpark = HardwareAdapter.getInstance().getDrivetrain().leftMasterSpark;
		CANSparkMax leftSlave1Spark = HardwareAdapter.getInstance().getDrivetrain().leftSlave1Spark;
		CANSparkMax leftSlave2Spark = HardwareAdapter.getInstance().getDrivetrain().leftSlave2Spark;

		CANSparkMax rightMasterSpark = HardwareAdapter.getInstance().getDrivetrain().rightMasterSpark;
		CANSparkMax rightSlave1Spark = HardwareAdapter.getInstance().getDrivetrain().rightSlave1Spark;
		CANSparkMax rightSlave2Spark = HardwareAdapter.getInstance().getDrivetrain().rightSlave2Spark;


		leftMasterSpark.getPIDController().setOutputRange(-DrivetrainConstants.kDriveMaxClosedLoopOutput, DrivetrainConstants.kDriveMaxClosedLoopOutput);
		leftSlave1Spark.getPIDController().setOutputRange(-DrivetrainConstants.kDriveMaxClosedLoopOutput, DrivetrainConstants.kDriveMaxClosedLoopOutput);
		leftSlave2Spark.getPIDController().setOutputRange(-DrivetrainConstants.kDriveMaxClosedLoopOutput, DrivetrainConstants.kDriveMaxClosedLoopOutput);

		rightMasterSpark.getPIDController().setOutputRange(-DrivetrainConstants.kDriveMaxClosedLoopOutput, DrivetrainConstants.kDriveMaxClosedLoopOutput);
		rightSlave1Spark.getPIDController().setOutputRange(-DrivetrainConstants.kDriveMaxClosedLoopOutput, DrivetrainConstants.kDriveMaxClosedLoopOutput);
        rightSlave2Spark.getPIDController().setOutputRange(-DrivetrainConstants.kDriveMaxClosedLoopOutput, DrivetrainConstants.kDriveMaxClosedLoopOutput);

        leftMasterSpark.setControlFramePeriod(5);
        rightMasterSpark.setControlFramePeriod(5);

		leftMasterSpark.setRampRate(0.2);
		rightMasterSpark.setRampRate(0.2);


		//Reverse right side
		leftMasterSpark.setInverted(true);
		leftSlave1Spark.setInverted(true);
		leftSlave2Spark.setInverted(true);

		rightMasterSpark.setInverted(false);
		rightSlave1Spark.setInverted(false);
		rightSlave2Spark.setInverted(false);

		//Set slave victors to follower mode
		leftSlave1Spark.follow(leftMasterSpark);
        leftSlave2Spark.follow(leftMasterSpark);
        rightSlave1Spark.follow(rightMasterSpark);
        rightSlave2Spark.follow(rightMasterSpark);
    }

    void configureElevatorHardware() {
	    CANSparkMax masterSpark = HardwareAdapter.getInstance().getElevator().elevatorMasterSpark;
	    CANSparkMax slaveSpark = HardwareAdapter.getInstance().getElevator().elevatorSlaveSpark;

	    masterSpark.setInverted(true);
	    slaveSpark.setInverted(false);

	    slaveSpark.follow(masterSpark);

        masterSpark.setRampRate(0.4);
        slaveSpark.setRampRate(0.4);
	}

	void configureIntakeHardware() {
		CANSparkMax intakeMasterSpark = HardwareAdapter.getInstance().getIntake().intakeMasterSpark;
		CANSparkMax intakeSlaveSpark = HardwareAdapter.getInstance().getIntake().intakeSlaveSpark;
		WPI_VictorSPX intakeVictor = HardwareAdapter.getInstance().getIntake().intakeVictor;

		intakeMasterSpark.setInverted(true);
		intakeSlaveSpark.setInverted(true);
		intakeVictor.setInverted(true);

		intakeVictor.setNeutralMode(NeutralMode.Brake);

		intakeMasterSpark.setRampRate(0.4);
		intakeSlaveSpark.setRampRate(0.4);

		intakeVictor.enableVoltageCompensation(true);
		intakeVictor.configVoltageCompSaturation(14, 0);
		intakeVictor.configForwardSoftLimitEnable(false, 0);
		intakeVictor.configReverseSoftLimitEnable(false, 0);

		intakeVictor.configPeakOutputForward(1, 0);
		intakeVictor.configPeakOutputReverse(-1, 0);

		//Set slave sparks to follower mode
		intakeSlaveSpark.follow(intakeMasterSpark);

	}

	void configureShooterHardware() {
		WPI_VictorSPX masterVictor = HardwareAdapter.getInstance().getShooter().shooterMasterVictor;
		WPI_VictorSPX slaveVictor = HardwareAdapter.getInstance().getShooter().shooterSlaveVictor;

		masterVictor.setInverted(false);
		slaveVictor.setInverted(false);

		slaveVictor.follow(masterVictor);

		masterVictor.setNeutralMode(NeutralMode.Brake);
		slaveVictor.setNeutralMode(NeutralMode.Brake);

		masterVictor.configOpenloopRamp(0.09, 0);
		slaveVictor.configOpenloopRamp(0.09, 0);

		masterVictor.enableVoltageCompensation(true);
		slaveVictor.enableVoltageCompensation(true);

		masterVictor.configVoltageCompSaturation(14, 0);
		slaveVictor.configVoltageCompSaturation(14, 0);

		masterVictor.configForwardSoftLimitEnable(false, 0);
		masterVictor.configReverseSoftLimitEnable(false, 0);
		slaveVictor.configForwardSoftLimitEnable(false, 0);
		slaveVictor.configReverseSoftLimitEnable(false, 0);
	}

	void configurePusherHardware() {
		CANSparkMax pusherSpark = HardwareAdapter.getInstance().getPusher().pusherSpark;

		pusherSpark.setInverted(false);
		pusherSpark.setIdleMode(CANSparkMax.IdleMode.kCoast);
		pusherSpark.setRampRate(0.09);

	}

		void startUltrasonics() {
		Ultrasonic pusherUltrasonicRight = HardwareAdapter.getInstance().getPusher().pusherUltrasonicRight;
		Ultrasonic pusherUltrasonicLeft = HardwareAdapter.getInstance().getPusher().pusherUltrasonicLeft;


		Ultrasonic ultrasonic1 = HardwareAdapter.getInstance().getIntake().ultrasonic1;
		Ultrasonic ultrasonic2 = HardwareAdapter.getInstance().getIntake().ultrasonic2;

		pusherUltrasonicRight.setAutomaticMode(true);
		pusherUltrasonicRight.setEnabled(true);
		pusherUltrasonicLeft.setAutomaticMode(true);
		pusherUltrasonicLeft.setEnabled(true);

		ultrasonic1.setAutomaticMode(true);
		ultrasonic1.setEnabled(true);
		ultrasonic2.setAutomaticMode(true);
		ultrasonic2.setAutomaticMode(true);
	}

	void configureShovelHardware() {
		WPI_TalonSRX shovelVictor = HardwareAdapter.getInstance().getShovel().shovelTalon;

		shovelVictor.setNeutralMode(NeutralMode.Brake);
		shovelVictor.configOpenloopRamp(0.09, 0);
		shovelVictor.enableVoltageCompensation(true);
		shovelVictor.configVoltageCompSaturation(14, 0);
		shovelVictor.configForwardSoftLimitEnable(false, 0);
		shovelVictor.configReverseSoftLimitEnable(false, 0);
	}

	/**
	 * Updates all the sensor data taken from the hardware
	 */
	void updateState(RobotState robotState) {

		CANSparkMax leftMasterSpark = HardwareAdapter.getInstance().getDrivetrain().leftMasterSpark;
		CANSparkMax rightMasterSpark = HardwareAdapter.getInstance().getDrivetrain().rightMasterSpark;

		Optional<Integer> left = leftMasterSpark.getParameterInt(CANSparkMaxLowLevel.ConfigParameter.kCtrlType);
		Optional<Integer> right = rightMasterSpark.getParameterInt(CANSparkMaxLowLevel.ConfigParameter.kCtrlType);

//		int lcm = left.isPresent() ? left.get() : 0;
//		int rcm = right.isPresent() ? right.get() : 0;

		int lcm = 0, rcm = 0;

		robotState.leftControlMode = ControlType.values()[lcm];
		robotState.rightControlMode = ControlType.values()[rcm];

		robotState.leftStickInput.update(HardwareAdapter.getInstance().getJoysticks().driveStick);
		robotState.rightStickInput.update(HardwareAdapter.getInstance().getJoysticks().turnStick);

		robotState.operatorXboxControllerInput.update(HardwareAdapter.getInstance().getJoysticks().operatorXboxController);
		robotState.backupStickInput.update(HardwareAdapter.getInstance().getJoysticks().backupStick);

		robotState.hatchIntakeUp = HardwareAdapter.getInstance().getShovel().upDownHFX.get();
//		robotState.shovelCurrentDraw = HardwareAdapter.getInstance().getMiscellaneousHardware().pdp.getCurrent(PortConstants.kVidarShovelPDPPort);

		robotState.leftSetpoint = leftMasterSpark.getAppliedOutput();
		robotState.rightSetpoint = rightMasterSpark.getAppliedOutput();

		robotState.elevatorPosition = HardwareAdapter.getInstance().getElevator().elevatorMasterSpark.getEncoder().getPosition();
		robotState.elevatorVelocity = HardwareAdapter.getInstance().getElevator().elevatorSlaveSpark.getEncoder().getVelocity();

		// Change HFX Talon location
		robotState.elevatorHFX = HardwareAdapter.getInstance().getElevator().elevatorHFX.get();

		PigeonIMU gyro = HardwareAdapter.getInstance().getDrivetrain().gyro;
		if(gyro != null) {
			robotState.drivePose.heading = gyro.getFusedHeading();
			robotState.drivePose.headingVelocity = (robotState.drivePose.heading - robotState.drivePose.lastHeading) /
                    DrivetrainConstants.kNormalLoopsDt;
			robotState.drivePose.lastHeading = gyro.getFusedHeading();
		} else {
			robotState.drivePose.heading = -0;
			robotState.drivePose.headingVelocity = -0;
		}

		robotState.drivePose.lastLeftEnc = robotState.drivePose.leftEnc;
		robotState.drivePose.leftEnc = leftMasterSpark.getEncoder().getPosition();
		robotState.drivePose.leftEncVelocity = leftMasterSpark.getEncoder().getVelocity();
		robotState.drivePose.lastRightEnc = robotState.drivePose.rightEnc;
		robotState.drivePose.rightEnc = rightMasterSpark.getEncoder().getPosition();
		robotState.drivePose.rightEncVelocity = rightMasterSpark.getEncoder().getVelocity();

		double robotVelocity = (robotState.drivePose.leftEncVelocity + robotState.drivePose.rightEncVelocity) /
                (2 * DrivetrainConstants.kDriveSpeedUnitConversion);

		double[] accelerometer_angle = new double[3];
		HardwareAdapter.getInstance().getDrivetrain().gyro.getAccelerometerAngles(accelerometer_angle);
		robotState.robotAccel = accelerometer_angle[0];
		robotState.robotVelocity = robotVelocity;

		double time = Timer.getFPGATimestamp();

		//Rotation2d gyro_angle = Rotation2d.fromRadians((right_distance - left_distance) * Constants.kTrackScrubFactor
		///Constants.kTrackEffectiveDiameter);
		Rotation2d gyro_angle = Rotation2d.fromDegrees(robotState.drivePose.heading);
		Rotation2d gyro_velocity = Rotation2d.fromDegrees(robotState.drivePose.headingVelocity);

		RigidTransform2d odometry = robotState.generateOdometryFromSensors(
		        (robotState.drivePose.leftEnc - robotState.drivePose.lastLeftEnc) /
                        DrivetrainConstants.kDriveTicksPerInch,
				(robotState.drivePose.rightEnc - robotState.drivePose.lastRightEnc) /
                        DrivetrainConstants.kDriveTicksPerInch, gyro_angle);

		RigidTransform2d.Delta velocity = Kinematics.forwardKinematics(
		        robotState.drivePose.leftEncVelocity / DrivetrainConstants.kDriveSpeedUnitConversion,
				robotState.drivePose.rightEncVelocity / DrivetrainConstants.kDriveSpeedUnitConversion, gyro_velocity.getRadians());

		robotState.addObservations(time, odometry, velocity);

		//Update pusher sensors
		robotState.pusherPosition = HardwareAdapter.getInstance().getPusher().pusherPotentiometer.get() /
				PusherConstants.kTicksPerInch;
		robotState.pusherVelocity = (robotState.pusherPosition - robotState.pusherCachePosition) / DrivetrainConstants.kNormalLoopsDt;
		StickyFaults pusherStickyFaults = new StickyFaults();
		HardwareAdapter.getInstance().getPusher().pusherSpark.clearFaults();
		robotState.hasPusherStickyFaults = false;
		robotState.pusherCachePosition = robotState.pusherPosition;


		CANSparkMax.FaultID intakeStickyFaults = CANSparkMax.FaultID.kSensorFault;
		HardwareAdapter.getInstance().getIntake().intakeMasterSpark.clearFaults();
		HardwareAdapter.getInstance().getIntake().intakeMasterSpark.getStickyFault(intakeStickyFaults);

        robotState.intakeVelocity = HardwareAdapter.getInstance().getIntake().intakeMasterSpark.getEncoder().getVelocity();
        robotState.intakeAngle = HardwareAdapter.getInstance().getIntake().intakeMasterSpark.getEncoder().getPosition();

		updateUltrasonicSensors(robotState);
	}

	void startIntakeArm() {
		Robot.getRobotState().intakeStartAngle = IntakeConstants.kMaxAngle -
				1/IntakeConstants.kArmPotentiometerTicksPerDegree * (IntakeConstants.kMaxAngleTicks -
                        HardwareAdapter.getInstance().getIntake().potentiometer.get());

	}

	void updateIntakeSensors() {
		Robot.getRobotState().intakeAngle = Robot.getRobotState().intakeStartAngle -
				HardwareAdapter.getInstance().getIntake().intakeMasterSpark.getEncoder().getPosition() / IntakeConstants.kArmEncoderRevolutionsPerDegree;
	}

	void updateUltrasonicSensors(RobotState robotState) {
		// HAS CARGO IN INTAKE

		// left side
		Ultrasonic mUltrasonicLeft = HardwareAdapter.getInstance().getIntake().ultrasonic1;
		robotState.mLeftReadings.add(mUltrasonicLeft.getRangeInches());
		if(robotState.mLeftReadings.size() > 10) {
			robotState.mLeftReadings.remove(0);
		}
		// right side
		Ultrasonic mUltrasonicRight = HardwareAdapter.getInstance().getIntake().ultrasonic2;
		robotState.mRightReadings.add(mUltrasonicRight.getRangeInches());
		if(robotState.mRightReadings.size() > 10) {
			robotState.mRightReadings.remove(0);
		}

		int leftTotal = (int) robotState.mLeftReadings.stream().filter(i -> (i < IntakeConstants.kCargoInchTolerance)).count();
		int rightTotal = (int) robotState.mRightReadings.stream().filter(i -> (i < IntakeConstants.kCargoInchTolerance)).count();

		robotState.hasCargo = (leftTotal > OtherConstants.kRequiredUltrasonicCount && rightTotal > OtherConstants.kRequiredUltrasonicCount);
		robotState.cargoDistance = (mUltrasonicLeft.getRangeInches() + mUltrasonicRight.getRangeInches()) / 2;

		// HAS CARGO IN CARRIAGE

		//Left Side Cargo Distance from Pusher
		Ultrasonic mPusherUltrasonicLeft = HardwareAdapter.getInstance().getPusher().pusherUltrasonicLeft;
		robotState.mLeftPusherReadings.add(mPusherUltrasonicLeft.getRangeInches());
		if(robotState.mLeftPusherReadings.size() > 10) {
			robotState.mLeftPusherReadings.remove(0);
		}

		//Right Side Cargo Distance from Pusher
		Ultrasonic mPusherUltrasonicRight = HardwareAdapter.getInstance().getPusher().pusherUltrasonicRight;
		robotState.mRightPusherReadings.add(mPusherUltrasonicRight.getRangeInches());
		if(robotState.mRightPusherReadings.size() > 10) {
			robotState.mRightPusherReadings.remove(0);
		}

		int leftPusherTotal = (int) robotState.mLeftPusherReadings.stream().filter(i -> i < PusherConstants.kVidarCargoTolerance).count();
		int rightPusherTotal = (int) robotState.mRightPusherReadings.stream().filter(i -> i < PusherConstants.kVidarCargoTolerance).count();
		robotState.hasPusherCargo = (leftPusherTotal > OtherConstants.kRequiredUltrasonicCount && rightPusherTotal > OtherConstants.kRequiredUltrasonicCount);
		robotState.cargoPusherDistance = (mPusherUltrasonicLeft.getRangeInches() + mPusherUltrasonicRight.getRangeInches())/2;
	}

	/**
	 * Updates the hardware to run with output values of subsystems
	 */
	void updateHardware() {
		System.out.println("A");
		updateDrivetrain();
		System.out.println("B");

		updateElevator();
		System.out.println("C");

		updateShooter();
		System.out.println("D");

		updatePusher();
		System.out.println("E");

		updateShovel();
		System.out.println("F");

		updateFingers();
		System.out.println("G");

		updateMiscellaneousHardware();
	}

	/**
	 * Updates the drivetrain Uses SparkOutput and can run off-board control loops through SRX
	 */
	private void updateDrivetrain() {
		updateSparkMax(HardwareAdapter.getInstance().getDrivetrain().leftMasterSpark, mDrive.getDriveSignal().leftMotor);
		updateSparkMax(HardwareAdapter.getInstance().getDrivetrain().rightMasterSpark, mDrive.getDriveSignal().rightMotor);
	}

    /**
     * Checks if the compressor should compress and updates it accordingly
     */
	private void updateMiscellaneousHardware() {
	    if(shouldCompress()) {
	        HardwareAdapter.getInstance().getMiscellaneousHardware().compressor.start();
        } else {
            HardwareAdapter.getInstance().getMiscellaneousHardware().compressor.stop();
        }

	    HardwareAdapter.getInstance().getJoysticks().operatorXboxController.setRumble(shouldRumble());
    }

    /**
     * Runs the compressor only when the pressure too low or the current draw is
     * low enough
     */
    private boolean shouldCompress() {
    	return !(RobotState.getInstance().gamePeriod == RobotState.GamePeriod.AUTO);
    }

    /**
     * Determines when the rumble for the xbox controller should be on
     */
    private boolean shouldRumble() {
        boolean rumble;
        double intakeRumbleLength = mIntake.getRumbleLength();
        double shovelRumbleLength = mShovel.getRumbleLength();
        double shooterRumbleLength = mShooter.getRumbleLength();

        if(intakeRumbleLength >= 0) {
            rumble = true;
            intakeRumbleLength -= OtherConstants.deltaTime;
        } else if(shovelRumbleLength >= 0) {
            rumble = true;
            shovelRumbleLength -= OtherConstants.deltaTime;
        } else if(shooterRumbleLength >= 0) {
            rumble = true;
            shooterRumbleLength -= OtherConstants.deltaTime;
        } else {
            rumble = false;
        }

        return rumble;
    }

    /**
     * Updates the shooter
     */
    private void updateShooter() {
        HardwareAdapter.getInstance().getShooter().shooterMasterVictor.set(mShooter.getOutput());
    }

    /*
     * Updates the elevator
     */
    private void updateElevator() {
        if(mElevator.getmGearboxState() == Elevator.GearboxState.ELEVATOR) {
            if (mElevator.getIsAtTop() && mElevator.movingUpwards()) {
                SparkMaxOutput elevatorHoldOutput = new SparkMaxOutput();
                elevatorHoldOutput.setPercentOutput(ElevatorConstants.kHoldVoltage);
                updateSparkMax(HardwareAdapter.getInstance().getElevator().elevatorMasterSpark, elevatorHoldOutput);
            } else {
                updateSparkMax(HardwareAdapter.getInstance().getElevator().elevatorMasterSpark, mElevator.getOutput());
            }
        } else {
            updateSparkMax(HardwareAdapter.getInstance().getElevator().elevatorMasterSpark, mElevator.getOutput());
        }
        HardwareAdapter.getInstance().getElevator().elevatorDoubleSolenoid.set(mElevator.getSolenoidOutput());
        HardwareAdapter.getInstance().getElevator().elevatorHolderSolenoid.set(mElevator.getHolderSolenoidOutput());
    }

	/**
	 * Updates the pusher
	 */
	private void updatePusher() {
		HardwareAdapter.getInstance().getPusher().pusherSpark.set(mPusher.getPusherOutput());
	}

    /**
     * Updates the shovel
     */
	private void updateShovel() {
		HardwareAdapter.getInstance().getShovel().shovelTalon.set(mShovel.getPercentOutput());
		HardwareAdapter.getInstance().getShovel().upDownSolenoid.set(mShovel.getUpDownOutput() ? DoubleSolenoid.Value.kForward : DoubleSolenoid.Value.kReverse);
	}

	/**
	 * Updates fingers
	 */
	private void updateFingers() {
        HardwareAdapter.getInstance().getFingers().openCloseSolenoid.set(mFingers.getOpenCloseOutput());
        HardwareAdapter.getInstance().getFingers().expelSolenoid.set(mFingers.getExpelOutput());
    }

    /**
     * Updates intake
     */
    private void updateIntake() {
		updateSparkMax(HardwareAdapter.getInstance().getIntake().intakeMasterSpark, mIntake.getSparkOutput());
		HardwareAdapter.getInstance().getIntake().intakeVictor.set(mIntake.getVictorOutput());
	}

	void enableBrakeMode() {
		CANSparkMax leftMasterSpark = HardwareAdapter.getInstance().getDrivetrain().leftMasterSpark;
		CANSparkMax leftSlave1Spark = HardwareAdapter.getInstance().getDrivetrain().leftSlave1Spark;
		CANSparkMax leftSlave2Spark = HardwareAdapter.getInstance().getDrivetrain().leftSlave2Spark;

		CANSparkMax rightMasterSpark = HardwareAdapter.getInstance().getDrivetrain().rightMasterSpark;
		CANSparkMax rightSlave1Spark = HardwareAdapter.getInstance().getDrivetrain().rightSlave1Spark;
		CANSparkMax rightSlave2Spark = HardwareAdapter.getInstance().getDrivetrain().rightSlave2Spark;

		leftMasterSpark.setIdleMode(CANSparkMax.IdleMode.kBrake);
		leftSlave1Spark.setIdleMode(CANSparkMax.IdleMode.kBrake);
		leftSlave2Spark.setIdleMode(CANSparkMax.IdleMode.kBrake);
		rightMasterSpark.setIdleMode(CANSparkMax.IdleMode.kBrake);
		rightSlave1Spark.setIdleMode(CANSparkMax.IdleMode.kBrake);
		rightSlave2Spark.setIdleMode(CANSparkMax.IdleMode.kBrake);
	}

	void disableBrakeMode() {
		CANSparkMax leftMasterSpark = HardwareAdapter.getInstance().getDrivetrain().leftMasterSpark;
		CANSparkMax leftSlave1Spark = HardwareAdapter.getInstance().getDrivetrain().leftSlave1Spark;
		CANSparkMax leftSlave2Spark = HardwareAdapter.getInstance().getDrivetrain().leftSlave2Spark;

		CANSparkMax rightMasterSpark = HardwareAdapter.getInstance().getDrivetrain().rightMasterSpark;
		CANSparkMax rightSlave1Spark = HardwareAdapter.getInstance().getDrivetrain().rightSlave1Spark;
		CANSparkMax rightSlave2Spark = HardwareAdapter.getInstance().getDrivetrain().rightSlave2Spark;

		leftMasterSpark.setIdleMode(CANSparkMax.IdleMode.kCoast);
		leftSlave1Spark.setIdleMode(CANSparkMax.IdleMode.kCoast);
		leftSlave2Spark.setIdleMode(CANSparkMax.IdleMode.kCoast);
		rightMasterSpark.setIdleMode(CANSparkMax.IdleMode.kCoast);
		rightSlave1Spark.setIdleMode(CANSparkMax.IdleMode.kCoast);
		rightSlave2Spark.setIdleMode(CANSparkMax.IdleMode.kCoast);
	}

	/**
	 * Helper method for processing a TalonSRXOutput for an SRX
	 */
	private void updateTalonSRX(WPI_TalonSRX talon, TalonSRXOutput output) {
		if(output.getControlMode().equals(ControlMode.Position) || output.getControlMode().equals(ControlMode.Velocity)
				|| output.getControlMode().equals(ControlMode.MotionMagic)) {
			talon.config_kP(output.profile, output.gains.P, 0);
			talon.config_kI(output.profile, output.gains.I, 0);
			talon.config_kD(output.profile, output.gains.D, 0);
			talon.config_kF(output.profile, output.gains.F, 0);
			talon.config_IntegralZone(output.profile, output.gains.izone, 0);
			talon.configClosedloopRamp(output.gains.rampRate, 0);
		}
		if(output.getControlMode().equals(ControlMode.MotionMagic)) {
			talon.configMotionAcceleration(output.accel, 0);
			talon.configMotionCruiseVelocity(output.cruiseVel, 0);
		}
		if(output.getControlMode().equals(ControlMode.Velocity)) {
			talon.configAllowableClosedloopError(output.profile, 0, 0);
		}
		if (output.getArbitraryFF() != 0.0 && output.getControlMode().equals(ControlMode.Position)) {
			talon.set(output.getControlMode(), output.getSetpoint(), DemandType.ArbitraryFeedForward, output.getArbitraryFF());
		} else {
			talon.set(output.getControlMode(), output.getSetpoint(), DemandType.Neutral, 0.0);
		}
	}

    /**
     * Helper method for processing a SparkMaxOutput
     * @param spark
     * @param output
     */
    private void updateSparkMax(CANSparkMax spark, SparkMaxOutput output) {
//        if(output.getControlType().equals(ControlType.kPosition) || output.getControlType().equals(ControlType.kVelocity)) {
//            updateSparkGains(spark, output);
//        }
//        if(output.getArbitraryFF() != 0.0 && output.getControlType().equals(ControlType.kPosition)) {
//            spark.getPIDController().setReference(output.getSetpoint(), output.getControlType(), 0, output.getArbitraryFF());
//        } else {
//            spark.getPIDController().setReference(output.getSetpoint(), output.getControlType());
//        }
    }

	private void updateSparkGains(CANSparkMax spark, SparkMaxOutput output) {
		spark.getPIDController().setP(output.getGains().P);
		spark.getPIDController().setD(output.getGains().D);
		spark.getPIDController().setI(output.getGains().I);
		spark.getPIDController().setFF(output.getGains().F);
		spark.getPIDController().setIZone(output.getGains().izone);
		spark.setRampRate(output.getGains().rampRate);
	}
}