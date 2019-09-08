package com.palyrobotics.frc2019.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.palyrobotics.frc2019.config.Constants.DrivetrainConstants;
import com.palyrobotics.frc2019.config.Constants.IntakeConstants;
import com.palyrobotics.frc2019.config.Constants.OtherConstants;
import com.palyrobotics.frc2019.config.Constants.PusherConstants;
import com.palyrobotics.frc2019.config.Gains;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.config.configv2.ElevatorConfig;
import com.palyrobotics.frc2019.subsystems.*;
import com.palyrobotics.frc2019.util.SparkMaxOutput;
import com.palyrobotics.frc2019.util.TalonSRXOutput;
import com.palyrobotics.frc2019.util.configv2.Configs;
import com.palyrobotics.frc2019.util.logger.Logger;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Ultrasonic;

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
        HardwareAdapter.getInstance().getIntake().intakeTalon.set(ControlMode.Disabled, 0);

        //Disable pusher sparks
        HardwareAdapter.getInstance().getPusher().pusherSpark.disable();

        Logger.getInstance().logRobotThread(Level.INFO, "Disabling victors");

        // Disable shooter victors
        HardwareAdapter.getInstance().getShooter().shooterMasterVictor.set(ControlMode.Disabled, 0);
        HardwareAdapter.getInstance().getShooter().shooterSlaveVictor.set(ControlMode.Disabled, 0);
    }

    void configureHardware() {
        configureShovelHardware();
        configureDriveHardware();
        configureElevatorHardware();
        configureIntakeHardware();
        configureShooterHardware();
        configurePusherHardware();
        startIntakeArm();
    }

    void configureDriveHardware() {

        HardwareAdapter.getInstance().getDrivetrain().resetSensors();

        CANSparkMax leftMasterSpark = HardwareAdapter.getInstance().getDrivetrain().leftMasterSpark;
        CANSparkMax leftSlave1Spark = HardwareAdapter.getInstance().getDrivetrain().leftSlave1Spark;
        CANSparkMax leftSlave2Spark = HardwareAdapter.getInstance().getDrivetrain().leftSlave2Spark;

        CANSparkMax rightMasterSpark = HardwareAdapter.getInstance().getDrivetrain().rightMasterSpark;
        CANSparkMax rightSlave1Spark = HardwareAdapter.getInstance().getDrivetrain().rightSlave1Spark;
        CANSparkMax rightSlave2Spark = HardwareAdapter.getInstance().getDrivetrain().rightSlave2Spark;

//		leftMasterSpark.restoreFactoryDefaults();
//		leftSlave1Spark.restoreFactoryDefaults();
//		leftSlave2Spark.restoreFactoryDefaults();
//		rightMasterSpark.restoreFactoryDefaults();
//		rightSlave1Spark.restoreFactoryDefaults();
//		rightSlave2Spark.restoreFactoryDefaults();


//		leftMasterSpark.enableVoltageCompensation(12);
//		leftSlave1Spark.enableVoltageCompensation(12);
//		leftSlave2Spark.enableVoltageCompensation(12);
//		rightMasterSpark.enableVoltageCompensation(12);
//		rightSlave1Spark.enableVoltageCompensation(12);
//		rightSlave2Spark.enableVoltageCompensation(12);

        leftMasterSpark.getEncoder().setPositionConversionFactor(DrivetrainConstants.kDriveInchesPerRotation);
        leftSlave1Spark.getEncoder().setPositionConversionFactor(DrivetrainConstants.kDriveInchesPerRotation);
        leftSlave2Spark.getEncoder().setPositionConversionFactor(DrivetrainConstants.kDriveInchesPerRotation);
        rightMasterSpark.getEncoder().setPositionConversionFactor(DrivetrainConstants.kDriveInchesPerRotation);
        rightSlave1Spark.getEncoder().setPositionConversionFactor(DrivetrainConstants.kDriveInchesPerRotation);
        rightSlave2Spark.getEncoder().setPositionConversionFactor(DrivetrainConstants.kDriveInchesPerRotation);

        leftMasterSpark.getEncoder().setVelocityConversionFactor(DrivetrainConstants.kDriveSpeedUnitConversion);
        leftSlave1Spark.getEncoder().setVelocityConversionFactor(DrivetrainConstants.kDriveSpeedUnitConversion);
        leftSlave2Spark.getEncoder().setVelocityConversionFactor(DrivetrainConstants.kDriveSpeedUnitConversion);
        rightMasterSpark.getEncoder().setVelocityConversionFactor(DrivetrainConstants.kDriveSpeedUnitConversion);
        rightSlave1Spark.getEncoder().setVelocityConversionFactor(DrivetrainConstants.kDriveSpeedUnitConversion);
        rightSlave2Spark.getEncoder().setVelocityConversionFactor(DrivetrainConstants.kDriveSpeedUnitConversion);

        leftMasterSpark.getPIDController().setOutputRange(-DrivetrainConstants.kDriveMaxClosedLoopOutput, DrivetrainConstants.kDriveMaxClosedLoopOutput);
        leftSlave1Spark.getPIDController().setOutputRange(-DrivetrainConstants.kDriveMaxClosedLoopOutput, DrivetrainConstants.kDriveMaxClosedLoopOutput);
        leftSlave2Spark.getPIDController().setOutputRange(-DrivetrainConstants.kDriveMaxClosedLoopOutput, DrivetrainConstants.kDriveMaxClosedLoopOutput);

        rightMasterSpark.getPIDController().setOutputRange(-DrivetrainConstants.kDriveMaxClosedLoopOutput, DrivetrainConstants.kDriveMaxClosedLoopOutput);
        rightSlave1Spark.getPIDController().setOutputRange(-DrivetrainConstants.kDriveMaxClosedLoopOutput, DrivetrainConstants.kDriveMaxClosedLoopOutput);
        rightSlave2Spark.getPIDController().setOutputRange(-DrivetrainConstants.kDriveMaxClosedLoopOutput, DrivetrainConstants.kDriveMaxClosedLoopOutput);

        // leftMasterSpark.setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus0, 3);
        // rightMasterSpark.setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus0, 3);

        //Reverse right side
        leftMasterSpark.setInverted(false);
        leftSlave1Spark.setInverted(false);
        leftSlave2Spark.setInverted(false);

        rightMasterSpark.setInverted(true);
        rightSlave1Spark.setInverted(true);
        rightSlave2Spark.setInverted(true);


//		leftMasterSpark.setOpenLoopRampRate(.2);
//		leftSlave1Spark.setOpenLoopRampRate(.2);
//		leftSlave2Spark.setOpenLoopRampRate(.2);
//
//		rightMasterSpark.setOpenLoopRampRate(.2);
//		rightSlave1Spark.setOpenLoopRampRate(.2);
//		rightSlave2Spark.setOpenLoopRampRate(.2);

        leftMasterSpark.setSmartCurrentLimit(DrivetrainConstants.kCurrentLimit);
        leftSlave1Spark.setSmartCurrentLimit(DrivetrainConstants.kCurrentLimit);
        leftSlave2Spark.setSmartCurrentLimit(DrivetrainConstants.kCurrentLimit);

        rightMasterSpark.setSmartCurrentLimit(DrivetrainConstants.kCurrentLimit);
        rightSlave1Spark.setSmartCurrentLimit(DrivetrainConstants.kCurrentLimit);
        rightSlave2Spark.setSmartCurrentLimit(DrivetrainConstants.kCurrentLimit);

        // Set slave sparks to follower mode
        leftSlave1Spark.follow(leftMasterSpark);
        leftSlave2Spark.follow(leftMasterSpark);
        rightSlave1Spark.follow(rightMasterSpark);
        rightSlave2Spark.follow(rightMasterSpark);
    }

    void configureElevatorHardware() {

        HardwareAdapter.getInstance().getElevator().resetSensors();

        CANSparkMax masterSpark = HardwareAdapter.getInstance().getElevator().elevatorMasterSpark;
        CANSparkMax slaveSpark = HardwareAdapter.getInstance().getElevator().elevatorSlaveSpark;

        masterSpark.restoreFactoryDefaults();
        slaveSpark.restoreFactoryDefaults();

        slaveSpark.follow(masterSpark);

        masterSpark.enableVoltageCompensation(12);
        slaveSpark.enableVoltageCompensation(12);

        masterSpark.setInverted(false);
        slaveSpark.setInverted(false);

        masterSpark.setIdleMode(CANSparkMax.IdleMode.kBrake);
        slaveSpark.setIdleMode(CANSparkMax.IdleMode.kBrake);

//		masterSpark.setIdleMode(CANSparkMax.IdleMode.kCoast);
//		slaveSpark.setIdleMode(CANSparkMax.IdleMode.kCoast);

        // TODO refactor into constants

        masterSpark.enableSoftLimit(CANSparkMax.SoftLimitDirection.kForward, false);
        masterSpark.enableSoftLimit(CANSparkMax.SoftLimitDirection.kReverse, false);
        masterSpark.setSoftLimit(CANSparkMax.SoftLimitDirection.kForward, 0.0f);
        masterSpark.setSoftLimit(CANSparkMax.SoftLimitDirection.kReverse, ElevatorConfig.kMaxHeightInches);

        masterSpark.getEncoder().setPositionConversionFactor(1.0 / ElevatorConfig.kElevatorRotationsPerInch);
        masterSpark.getEncoder().setVelocityConversionFactor(ElevatorConfig.kElevatorSpeedUnitConversion);
//        masterSpark.getEncoder().setInverted(true);

        masterSpark.getPIDController().setOutputRange(-0.6, 0.6, 1);
//		masterSpark.getPIDController().setSmartMotionAccelStrategy(CANPIDController.AccelStrategy.kSCurve, 1);
        masterSpark.getPIDController().setSmartMotionAllowedClosedLoopError(0.0, 1);

        ElevatorConfig elevatorConfig = Configs.get(ElevatorConfig.class);
        masterSpark.getPIDController().setSmartMotionMaxAccel(elevatorConfig.a, 1);
        masterSpark.getPIDController().setSmartMotionMaxVelocity(elevatorConfig.v, 1);

        updateSparkGains(masterSpark, Gains.elevatorPosition, 0);
//        updateSparkGains(masterSpark, Gains.elevatorSmartMotion, 1);
        Gains smartMotionGains = new Gains(elevatorConfig.p, elevatorConfig.i, elevatorConfig.f, elevatorConfig.ff, 0, 0.0);
        System.out.println(smartMotionGains);
        updateSparkGains(masterSpark, smartMotionGains, 1);
    }

    void configureIntakeHardware() {

        HardwareAdapter.getInstance().getIntake().resetSensors();

        CANSparkMax intakeMasterSpark = HardwareAdapter.getInstance().getIntake().intakeMasterSpark;
        CANSparkMax intakeSlaveSpark = HardwareAdapter.getInstance().getIntake().intakeSlaveSpark;
        WPI_TalonSRX intakeTalon = HardwareAdapter.getInstance().getIntake().intakeTalon;

        intakeMasterSpark.restoreFactoryDefaults();
        intakeSlaveSpark.restoreFactoryDefaults();

        intakeSlaveSpark.follow(intakeMasterSpark);

        intakeMasterSpark.enableVoltageCompensation(12);
        intakeSlaveSpark.enableVoltageCompensation(12);

        intakeMasterSpark.getEncoder().setPositionConversionFactor(IntakeConstants.kArmDegreesPerRevolution);
        intakeMasterSpark.getEncoder().setVelocityConversionFactor(IntakeConstants.kArmEncoderSpeedUnitConversion);

        intakeMasterSpark.setInverted(false);
        intakeSlaveSpark.setInverted(false);

        intakeMasterSpark.getPIDController().setOutputRange(-0.50, 0.50, 1);
        intakeMasterSpark.getPIDController().setSmartMotionAccelStrategy(CANPIDController.AccelStrategy.kSCurve, 1);
        intakeMasterSpark.getPIDController().setSmartMotionAllowedClosedLoopError(2.0, 1);

        intakeTalon.setInverted(true);

        intakeTalon.setNeutralMode(NeutralMode.Brake);

        intakeTalon.enableVoltageCompensation(true);
        intakeTalon.configVoltageCompSaturation(14, 0);
        intakeTalon.configForwardSoftLimitEnable(false, 0);
        intakeTalon.configReverseSoftLimitEnable(false, 0);

        intakeTalon.configPeakOutputForward(1, 0);
        intakeTalon.configPeakOutputReverse(-1, 0);

        updateSparkGains(intakeMasterSpark, Gains.intakeSmartMotion, 1);
        updateSparkGains(intakeMasterSpark, Gains.intakePosition, 0);
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

        HardwareAdapter.getInstance().getPusher().resetSensors();

        CANSparkMax pusherSpark = HardwareAdapter.getInstance().getPusher().pusherSpark;

        pusherSpark.restoreFactoryDefaults();

        pusherSpark.enableVoltageCompensation(12);

        pusherSpark.getEncoder().setPositionConversionFactor(PusherConstants.kPusherInchesPerRotation);
        pusherSpark.getEncoder().setVelocityConversionFactor(PusherConstants.kPusherEncSpeedUnitConversion);
        pusherSpark.getPIDController().setOutputRange(-0.7, 0.7);

        pusherSpark.setSmartCurrentLimit(56);
        pusherSpark.setInverted(true);
        pusherSpark.setIdleMode(CANSparkMax.IdleMode.kBrake);


        updateSparkGains(pusherSpark, Gains.pusherPosition);
    }

    void startUltrasonics() {
        Ultrasonic intakeUltrasonicLeft = HardwareAdapter.getInstance().getIntake().intakeUltrasonicLeft;
        Ultrasonic intakeUltrasonicRight = HardwareAdapter.getInstance().getIntake().intakeUltrasonicRight;
        Ultrasonic pusherUltrasonic = HardwareAdapter.getInstance().getPusher().pusherUltrasonic;
//		Ultrasonic pusherSecondaryUltrasonic = HardwareAdapter.getInstance().getPusher().pusherSecondaryUltrasonic;


        intakeUltrasonicLeft.setAutomaticMode(true);
        intakeUltrasonicRight.setAutomaticMode(true);
        pusherUltrasonic.setAutomaticMode(true);
//		pusherSecondaryUltrasonic.setAutomaticMode(true);

        intakeUltrasonicLeft.setEnabled(true);
        intakeUltrasonicRight.setEnabled(true);
        pusherUltrasonic.setEnabled(true);
//		pusherSecondaryUltrasonic.setEnabled(true);
    }

    void configureShovelHardware() {
        WPI_TalonSRX shovelTalon = HardwareAdapter.getInstance().getShovel().shovelTalon;

        shovelTalon.setNeutralMode(NeutralMode.Brake);
        shovelTalon.configOpenloopRamp(0.09, 0);
        shovelTalon.enableVoltageCompensation(true);
        shovelTalon.configVoltageCompSaturation(14, 0);
        shovelTalon.configForwardSoftLimitEnable(false, 0);
        shovelTalon.configReverseSoftLimitEnable(false, 0);
    }

    /**
     * Updates all the sensor data taken from the hardware
     */
    void updateState(RobotState robotState) {
//        CANSparkMax leftMasterSpark = HardwareAdapter.getInstance().getDrivetrain().leftMasterSpark;
//        CANSparkMax rightMasterSpark = HardwareAdapter.getInstance().getDrivetrain().rightMasterSpark;
//
//        robotState.leftStickInput.update(HardwareAdapter.getInstance().getJoysticks().driveStick);
//        robotState.rightStickInput.update(HardwareAdapter.getInstance().getJoysticks().turnStick);

        robotState.operatorXboxControllerInput.update(HardwareAdapter.getInstance().getJoysticks().operatorXboxController);
//		robotState.backupStickInput.update(HardwareAdapter.getInstance().getJoysticks().backupStick);

//        robotState.hatchIntakeUp = !HardwareAdapter.getInstance().getShovel().upDownHFX.get();
//        robotState.shovelCurrentDraw = HardwareAdapter.getInstance().getMiscellaneousHardware().pdp.getCurrent(PortConstants.kVidarShovelPDPPort);
//        robotState.hasHatch = (robotState.shovelCurrentDraw > ShovelConstants.kMaxShovelCurrentDraw);

        CANEncoder elevatorEncoder = HardwareAdapter.getInstance().getElevator().elevatorMasterSpark.getEncoder();
        robotState.elevatorPosition = elevatorEncoder.getPosition();
        robotState.elevatorVelocity = elevatorEncoder.getVelocity();

//        PigeonIMU gyro = HardwareAdapter.getInstance().getDrivetrain().gyro;
//        if (gyro != null) {
//            robotState.drivePose.lastHeading = robotState.drivePose.heading;
//            robotState.drivePose.heading = gyro.getFusedHeading();
//            robotState.drivePose.headingVelocity = (robotState.drivePose.heading - robotState.drivePose.lastHeading) /
//                    DrivetrainConstants.kNormalLoopsDt;
//        } else {
//            robotState.drivePose.heading = -0;
//            robotState.drivePose.headingVelocity = -0;
//        }
//
//        robotState.drivePose.lastLeftEnc = robotState.drivePose.leftEnc;
//        robotState.drivePose.leftEnc = leftMasterSpark.getEncoder().getPosition();
//        robotState.drivePose.leftEncVelocity = leftMasterSpark.getEncoder().getVelocity();
//        robotState.drivePose.lastRightEnc = robotState.drivePose.rightEnc;
//        robotState.drivePose.rightEnc = rightMasterSpark.getEncoder().getPosition();
//        robotState.drivePose.rightEncVelocity = rightMasterSpark.getEncoder().getVelocity();
//
//        double robotVelocity = (robotState.drivePose.leftEncVelocity + robotState.drivePose.rightEncVelocity) / 2;
//
//        double[] accelerometer_angle = new double[3];
//        HardwareAdapter.getInstance().getDrivetrain().gyro.getAccelerometerAngles(accelerometer_angle);
//        robotState.robotAccel = accelerometer_angle[0];
//        robotState.robotVelocity = robotVelocity;
//
//        robotState.intakeVelocity = HardwareAdapter.getInstance().getIntake().intakeMasterSpark.getEncoder().getVelocity();
//
//        double time = Timer.getFPGATimestamp();
//
//        if (!robotState.cancelAuto && robotState.rightStickInput.getButtonPressed(7)) {
//            robotState.cancelAuto = true;
//        }
//
//        //Rotation2d gyro_angle = Rotation2d.fromRadians((right_distance - left_distance) * Constants.kTrackScrubFactor
//        ///Constants.kTrackEffectiveDiameter);
//        Rotation2d gyro_angle = Rotation2d.fromDegrees(robotState.drivePose.heading);
//        Rotation2d gyro_velocity = Rotation2d.fromDegrees(robotState.drivePose.headingVelocity);
//
//        RigidTransform2d odometry = robotState.generateOdometryFromSensors(robotState.drivePose.leftEnc -
//                robotState.drivePose.lastLeftEnc, robotState.drivePose.rightEnc - robotState.drivePose.lastRightEnc, gyro_angle);
//
//        RigidTransform2d.Delta velocity = Kinematics.forwardKinematics(
//                robotState.drivePose.leftEncVelocity, robotState.drivePose.rightEncVelocity, gyro_velocity.getRadians());
//
//        robotState.addObservations(time, odometry, velocity);
//
//        //Update pusher sensors
//        robotState.pusherPosition = HardwareAdapter.getInstance().getPusher().pusherSpark.getEncoder().getPosition();
//        robotState.pusherVelocity = HardwareAdapter.getInstance().getPusher().pusherSpark.getEncoder().getVelocity();
////		System.out.println(robotState.pusherPosition);
//
////		System.out.println(robotState.hatchIntakeUp);
//
//        updateIntakeSensors();
//        updateUltrasonicSensors(robotState);
    }

    void startIntakeArm() {
        Robot.getRobotState().intakeStartAngle = IntakeConstants.kMaxAngle -
                1 / IntakeConstants.kArmPotentiometerTicksPerDegree * Math.abs(HardwareAdapter.getInstance().getIntake().potentiometer.get() -
                        IntakeConstants.kMaxAngleTicks);
        HardwareAdapter.getInstance().getIntake().intakeMasterSpark.getEncoder().setPosition(Robot.getRobotState().intakeStartAngle);

    }

    void updateIntakeSensors() {
        Robot.getRobotState().intakeAngle = HardwareAdapter.getInstance().getIntake().intakeMasterSpark.getEncoder().getPosition();
    }

    void updateUltrasonicSensors(RobotState robotState) {
        // HAS CARGO IN INTAKE

        // left side
        Ultrasonic mUltrasonicLeft = HardwareAdapter.getInstance().getIntake().intakeUltrasonicLeft;
        robotState.mLeftReadings.add(mUltrasonicLeft.getRangeInches());
        if (robotState.mLeftReadings.size() > 10) {
            robotState.mLeftReadings.remove(0);
        }
        // right side
        Ultrasonic mUltrasonicRight = HardwareAdapter.getInstance().getIntake().intakeUltrasonicRight;
        robotState.mRightReadings.add(mUltrasonicRight.getRangeInches());
        if (robotState.mRightReadings.size() > 10) {
            robotState.mRightReadings.remove(0);
        }

        int leftTotal = (int) robotState.mLeftReadings.stream().filter(i -> (i < IntakeConstants.kCargoInchTolerance)).count();
        int rightTotal = (int) robotState.mRightReadings.stream().filter(i -> (i < IntakeConstants.kCargoInchTolerance)).count();
        robotState.hasCargo = (leftTotal >= IntakeConstants.kCargoCountRequired || rightTotal >= IntakeConstants.kCargoCountRequired);
        robotState.cargoDistance = Math.min(mUltrasonicLeft.getRangeInches(), mUltrasonicRight.getRangeInches());


        // HAS CARGO IN CARRIAGE

        //Cargo Distance from Pusher
        Ultrasonic mPusherUltrasonic = HardwareAdapter.getInstance().getPusher().pusherUltrasonic;
        robotState.mPusherReadings.add(mPusherUltrasonic.getRangeInches());
        if (robotState.mPusherReadings.size() > 10) {
            robotState.mPusherReadings.remove(0);
        }


        int pusherTotalClose = (int) robotState.mPusherReadings.stream().filter(i -> i < PusherConstants.kVidarCargoTolerance).count();

        int pusherTotalFar = (int) robotState.mPusherReadings.stream().filter(i -> i < PusherConstants.kVidarCargoToleranceFar).count();

        robotState.hasPusherCargo = (pusherTotalClose > OtherConstants.kRequiredUltrasonicCount + 1);
        robotState.hasPusherCargoFar = (pusherTotalFar > OtherConstants.kRequiredUltrasonicCount);

        robotState.cargoPusherDistance = (mPusherUltrasonic.getRangeInches());
//		System.out.println(robotState.cargoPusherDistance);
    }

    /**
     * Updates the hardware to run with output values of subsystems
     */
    void updateHardware() {
//        updateDrivetrain();
        updateElevator();
//        updateShooter();
//        updatePusher();
//        updateShovel();
//        updateFingers();
//		updateIntake();
        updateMiscellaneousHardware();
    }

    /**
     * Updates the drivetrain Uses SparkOutput and can run off-board control loops through SRX
     */
    private void updateDrivetrain() {
        updateSparkMax(HardwareAdapter.getInstance().getDrivetrain().leftMasterSpark, mDrive.getDriveSignal().leftMotor);
        updateSparkMax(HardwareAdapter.getInstance().getDrivetrain().rightMasterSpark, mDrive.getDriveSignal().rightMotor);

//		SparkMaxOutput c = new SparkMaxOutput();
//		c.setPercentOutput(HardwareAdapter.getInstance().getJoysticks().driveStick.getY());
//
////		c.setPercentOutput(throttle);
//
//		updateSparkMax(HardwareAdapter.getInstance().getDrivetrain().leftMasterSpark, c);
//		updateSparkMax(HardwareAdapter.getInstance().getDrivetrain().rightMasterSpark, c);

//		mSignal.leftMotor.setPercentOutput(throttle);
//		mSignal.rightMotor.setPercentOutput(throttle);
    }

    /**
     * Checks if the compressor should compress and updates it accordingly
     */
    private void updateMiscellaneousHardware() {
        HardwareAdapter.getInstance().getMiscellaneousHardware().compressor.stop();
//		if(shouldCompress()) {
//	        HardwareAdapter.getInstance().getMiscellaneousHardware().compressor.start();
//        } else {
//            HardwareAdapter.getInstance().getMiscellaneousHardware().compressor.stop();
//        }

        HardwareAdapter.getInstance().getJoysticks().operatorXboxController.setRumble(shouldRumble());
    }

    /**
     * Runs the compressor only when the pressure too low or the current draw is
     * low enough
     */
    private boolean shouldCompress() {
        return !(RobotState.getInstance().gamePeriod == RobotState.GamePeriod.AUTO || RobotState.getInstance().isQuickturning);
    }

    /**
     * Determines when the rumble for the xbox controller should be on
     */
    private boolean shouldRumble() {
        boolean rumble;
        double intakeRumbleLength = mIntake.getRumbleLength();
        double shovelRumbleLength = mShovel.getRumbleLength();
        double shooterRumbleLength = mShooter.getRumbleLength();

        if (intakeRumbleLength > 0) {
            rumble = true;
            mIntake.decreaseRumbleLength();
        } else if (shovelRumbleLength > 0) {
            rumble = true;
            mShovel.decreaseRumbleLength();
        } else if (shooterRumbleLength > 0) {
            rumble = true;
            mShooter.decreaseRumbleLength();
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
        updateSparkMax(HardwareAdapter.getInstance().getElevator().elevatorMasterSpark, mElevator.getOutput());
        HardwareAdapter.getInstance().getElevator().elevatorShifter.set(mElevator.getSolenoidOutput());
    }

    /**
     * Updates the pusher
     */
    private void updatePusher() {
        updateSparkMax(HardwareAdapter.getInstance().getPusher().pusherSpark, mPusher.getPusherOutput());
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
        HardwareAdapter.getInstance().getFingers().pusherSolenoid.set(mFingers.getExpelOutput());
    }

    /**
     * Updates intake
     */
    private void updateIntake() {
        updateSparkMax(HardwareAdapter.getInstance().getIntake().intakeMasterSpark, mIntake.getSparkOutput());
        HardwareAdapter.getInstance().getIntake().intakeTalon.set(mIntake.getTalonOutput());
//		System.out.println(HardwareAdapter.getInstance().getIntake().intakeMasterSpark.getAppliedOutput());
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
        if (output.getControlMode().equals(ControlMode.Position) || output.getControlMode().equals(ControlMode.Velocity)
                || output.getControlMode().equals(ControlMode.MotionMagic)) {
            talon.config_kP(output.profile, output.gains.P, 0);
            talon.config_kI(output.profile, output.gains.I, 0);
            talon.config_kD(output.profile, output.gains.D, 0);
            talon.config_kF(output.profile, output.gains.F, 0);
            talon.config_IntegralZone(output.profile, output.gains.izone, 0);
            talon.configClosedloopRamp(output.gains.rampRate, 0);
        }
        if (output.getControlMode().equals(ControlMode.MotionMagic)) {
            talon.configMotionAcceleration(output.accel, 0);
            talon.configMotionCruiseVelocity(output.cruiseVel, 0);
        }
        if (output.getControlMode().equals(ControlMode.Velocity)) {
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
     *
     * @param spark
     * @param output
     */
    private void updateSparkMax(CANSparkMax spark, SparkMaxOutput output) {
        if (output.getControlType().equals(ControlType.kSmartMotion)) {
            spark.getPIDController().setReference(output.getSetpoint(), output.getControlType(), 1, output.getArbitraryFF(), CANPIDController.ArbFFUnits.kPercentOut);
        } else {
            spark.getPIDController().setReference(output.getSetpoint(), output.getControlType(), 0, output.getArbitraryFF());
        }
    }

    private void updateSparkGains(CANSparkMax spark, Gains gains) {
        updateSparkGains(spark, gains, 0);
    }

    private void updateSparkGains(CANSparkMax spark, Gains gains, int slotID) {
        spark.getPIDController().setP(gains.P, slotID);
        spark.getPIDController().setD(gains.D, slotID);
        spark.getPIDController().setI(gains.I, slotID);
        spark.getPIDController().setFF(gains.F, slotID);
        spark.getPIDController().setIZone(gains.izone, slotID);
        spark.setClosedLoopRampRate(gains.rampRate);
    }
}