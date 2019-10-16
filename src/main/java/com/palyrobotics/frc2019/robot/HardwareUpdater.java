package com.palyrobotics.frc2019.robot;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.ctre.phoenix.sensors.PigeonIMU;
import com.palyrobotics.frc2019.config.Constants.DrivetrainConstants;
import com.palyrobotics.frc2019.config.Constants.OtherConstants;
import com.palyrobotics.frc2019.config.*;
import com.palyrobotics.frc2019.subsystems.*;
import com.palyrobotics.frc2019.util.SparkMaxOutput;
import com.palyrobotics.frc2019.util.config.Configs;
import com.palyrobotics.frc2019.util.controllers.LazySparkMax;
import com.palyrobotics.frc2019.util.trajectory.Kinematics;
import com.palyrobotics.frc2019.util.trajectory.RigidTransform2d;
import com.palyrobotics.frc2019.util.trajectory.Rotation2d;
import com.palyrobotics.frc2019.vision.Limelight;
import com.revrobotics.CANEncoder;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMax.SoftLimitDirection;
import com.revrobotics.ControlType;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;

/**
 * Should only be used in robot package.
 */

class HardwareUpdater {

    /* Subsystems */
    private Drive mDrive;
    private Intake mIntake;
    private Elevator mElevator;
    private Shooter mShooter;
    private Pusher mPusher;
    private Shovel mShovel;
    private Fingers mFingers;

    HardwareUpdater(Drive drive, Elevator elevator, Shooter shooter, Pusher pusher, Shovel shovel, Fingers fingers, Intake intake) {
        mDrive = drive;
        mElevator = elevator;
        mShooter = shooter;
        mPusher = pusher;
        mShovel = shovel;
        mFingers = fingers;
        mIntake = intake;
    }

    void initHardware() {
        configureHardware();
        startUltrasonics();
    }

    private void configureHardware() {
        configureShovelHardware();
        configureDriveHardware();
        configureElevatorHardware();
        configureIntakeHardware();
        configureShooterHardware();
        configurePusherHardware();
        HardwareAdapter.getInstance().getIntake().calibrateIntakeEncoderWithPotentiometer();
    }

    private void configureDriveHardware() {
        HardwareAdapter.DrivetrainHardware driveHardware = HardwareAdapter.getInstance().getDrivetrain();

        for (CANSparkMax spark : driveHardware.sparks) {
            spark.restoreFactoryDefaults();
            spark.enableVoltageCompensation(11.0);
            spark.setSecondaryCurrentLimit(200);
            spark.setSmartCurrentLimit(DrivetrainConstants.kCurrentLimit);
            spark.setOpenLoopRampRate(0.05);
            CANEncoder encoder = spark.getEncoder();
            encoder.setPositionConversionFactor(DrivetrainConstants.kDriveInchesPerRotation);
            encoder.setVelocityConversionFactor(DrivetrainConstants.kDriveSpeedUnitConversion);
            CANPIDController controller = spark.getPIDController();
            controller.setOutputRange(-DrivetrainConstants.kDriveMaxClosedLoopOutput, DrivetrainConstants.kDriveMaxClosedLoopOutput);
        }

        driveHardware.resetSensors();

        // leftMasterSpark.setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus0, 3);
        // rightMasterSpark.setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus0, 3);

        // Invert right side
        driveHardware.leftMasterSpark.setInverted(false);
        driveHardware.leftSlave1Spark.setInverted(false);
        driveHardware.leftSlave2Spark.setInverted(false);

        driveHardware.rightMasterSpark.setInverted(true);
        driveHardware.rightSlave1Spark.setInverted(true);
        driveHardware.rightSlave2Spark.setInverted(true);

        // Set slave sparks to follower mode
        driveHardware.leftSlave1Spark.follow(driveHardware.leftMasterSpark);
        driveHardware.leftSlave2Spark.follow(driveHardware.leftMasterSpark);
        driveHardware.rightSlave1Spark.follow(driveHardware.rightMasterSpark);
        driveHardware.rightSlave2Spark.follow(driveHardware.rightMasterSpark);
    }

    private void configureElevatorHardware() {
        HardwareAdapter.ElevatorHardware elevatorHardware = HardwareAdapter.getInstance().getElevator();
        CANSparkMax masterElevatorSpark = elevatorHardware.elevatorMasterSpark;
        CANSparkMax slaveElevatorSpark = elevatorHardware.elevatorSlaveSpark;

        masterElevatorSpark.restoreFactoryDefaults();
        slaveElevatorSpark.restoreFactoryDefaults();
        elevatorHardware.resetSensors();

        slaveElevatorSpark.follow(masterElevatorSpark);

        masterElevatorSpark.enableVoltageCompensation(12);
        slaveElevatorSpark.enableVoltageCompensation(12);

        masterElevatorSpark.setInverted(true); // Makes it so that upwards is positive ticks. Flips motor and encoder.

        masterElevatorSpark.setIdleMode(IdleMode.kBrake);
        slaveElevatorSpark.setIdleMode(IdleMode.kBrake);

        // TODO refactor into constants
        masterElevatorSpark.setSoftLimit(SoftLimitDirection.kForward, 0.0f);
        masterElevatorSpark.setSoftLimit(SoftLimitDirection.kReverse, ElevatorConfig.kMaxHeightInches);
        masterElevatorSpark.enableSoftLimit(SoftLimitDirection.kForward, false);
        masterElevatorSpark.enableSoftLimit(SoftLimitDirection.kReverse, false);

        masterElevatorSpark.getEncoder().setPositionConversionFactor(ElevatorConfig.kElevatorInchesPerRevolution);
        masterElevatorSpark.getEncoder().setVelocityConversionFactor(ElevatorConfig.kElevatorInchesPerMinutePerRpm);
    }

    private void configureIntakeHardware() {
        HardwareAdapter.IntakeHardware intakeHardware = HardwareAdapter.getInstance().getIntake();
        CANSparkMax
                intakeMasterSpark = intakeHardware.intakeMasterSpark,
                intakeSlaveSpark = intakeHardware.intakeSlaveSpark;
        WPI_TalonSRX intakeTalon = intakeHardware.intakeTalon;

        intakeMasterSpark.restoreFactoryDefaults();
        intakeSlaveSpark.restoreFactoryDefaults();

        intakeSlaveSpark.follow(intakeMasterSpark);

        intakeMasterSpark.enableVoltageCompensation(12);
        intakeSlaveSpark.enableVoltageCompensation(12);

        intakeMasterSpark.getEncoder().setPositionConversionFactor(IntakeConfig.kArmDegreesPerRevolution);
        intakeMasterSpark.getEncoder().setVelocityConversionFactor(IntakeConfig.kArmDegreesPerMinutePerRpm);

        intakeTalon.setInverted(true);

        intakeTalon.setNeutralMode(NeutralMode.Brake);

        intakeTalon.enableVoltageCompensation(true);
        intakeTalon.configVoltageCompSaturation(14, 0);
        intakeTalon.configForwardSoftLimitEnable(false, 0);
        intakeTalon.configReverseSoftLimitEnable(false, 0);

        intakeTalon.configPeakOutputForward(1.0, 0);
        intakeTalon.configPeakOutputReverse(-1.0, 0);
    }

    private void configureShooterHardware() {
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

    private void configurePusherHardware() {
        HardwareAdapter.getInstance().getPusher().resetSensors();

        CANSparkMax pusherSpark = HardwareAdapter.getInstance().getPusher().pusherSpark;

        pusherSpark.restoreFactoryDefaults();

        pusherSpark.enableVoltageCompensation(12);

        pusherSpark.getEncoder().setPositionConversionFactor(PusherConfig.kPusherInchesPerRotation);
        pusherSpark.getEncoder().setVelocityConversionFactor(PusherConfig.kPusherEncSpeedUnitConversion);
        pusherSpark.getPIDController().setOutputRange(-0.7, 0.7);

        pusherSpark.setSmartCurrentLimit(56);
        pusherSpark.setInverted(true);
        pusherSpark.setIdleMode(IdleMode.kBrake);
    }

    private void startUltrasonics() {
        Ultrasonic
                intakeUltrasonicLeft = HardwareAdapter.getInstance().getIntake().intakeUltrasonicLeft,
                intakeUltrasonicRight = HardwareAdapter.getInstance().getIntake().intakeUltrasonicRight,
                pusherUltrasonic = HardwareAdapter.getInstance().getPusher().pusherUltrasonic;
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

    private void configureShovelHardware() {
        WPI_TalonSRX shovelTalon = HardwareAdapter.getInstance().getShovel().shovelTalon;

        shovelTalon.setNeutralMode(NeutralMode.Brake);
        shovelTalon.configOpenloopRamp(0.09, 0);
        shovelTalon.enableVoltageCompensation(true);
        shovelTalon.configVoltageCompSaturation(14, 0);
        shovelTalon.configForwardSoftLimitEnable(false, 0);
        shovelTalon.configReverseSoftLimitEnable(false, 0);
    }

    private double[] mAccelerometerAngles = new double[3]; // Cached array to prevent more garbage

    /**
     * Takes all of the sensor data from the hardware, and unwraps it into the current {@link RobotState}.
     */
    void updateState(RobotState robotState) {
        CANSparkMax
                leftMasterSpark = HardwareAdapter.getInstance().getDrivetrain().leftMasterSpark,
                rightMasterSpark = HardwareAdapter.getInstance().getDrivetrain().rightMasterSpark;

        robotState.leftStickInput.update(HardwareAdapter.getInstance().getJoysticks().driveStick);
        robotState.rightStickInput.update(HardwareAdapter.getInstance().getJoysticks().turnStick);

        robotState.operatorXboxControllerInput.update(HardwareAdapter.getInstance().getJoysticks().operatorXboxController);

        robotState.hatchIntakeUp = !HardwareAdapter.getInstance().getShovel().upDownHFX.get();
        robotState.shovelCurrentDraw = HardwareAdapter.getInstance().getMiscellaneousHardware().pdp.getCurrent(Configs.get(PortConstants.class).vidarShovelPDPPort);
        robotState.hasHatch = (robotState.shovelCurrentDraw > Configs.get(ShovelConfig.class).maxShovelCurrentDraw);

        LazySparkMax elevatorSpark = HardwareAdapter.getInstance().getElevator().elevatorMasterSpark;
        CANEncoder elevatorEncoder = elevatorSpark.getEncoder();
        robotState.elevatorPosition = elevatorEncoder.getPosition();
        robotState.elevatorVelocity = elevatorEncoder.getVelocity();
        robotState.elevatorAppliedOutput = elevatorSpark.getAppliedOutput();

        PigeonIMU gyro = HardwareAdapter.getInstance().getDrivetrain().gyro;
        robotState.drivePose.lastHeading = robotState.drivePose.heading;
        robotState.drivePose.heading = gyro.getFusedHeading();
        robotState.drivePose.headingVelocity = (robotState.drivePose.heading - robotState.drivePose.lastHeading) / DrivetrainConstants.kNormalLoopsDt;

        robotState.drivePose.lastLeftEncoderPosition = robotState.drivePose.leftEncoderPosition;
        robotState.drivePose.leftEncoderPosition = leftMasterSpark.getEncoder().getPosition();
        robotState.drivePose.leftEncoderVelocity = leftMasterSpark.getEncoder().getVelocity();
        robotState.drivePose.lastRightEncoderPosition = robotState.drivePose.rightEncoderPosition;
        robotState.drivePose.rightEncoderPosition = rightMasterSpark.getEncoder().getPosition();
        robotState.drivePose.rightEncoderVelocity = rightMasterSpark.getEncoder().getVelocity();

        double robotVelocity = (robotState.drivePose.leftEncoderVelocity + robotState.drivePose.rightEncoderVelocity) / 2;

        HardwareAdapter.getInstance().getDrivetrain().gyro.getAccelerometerAngles(mAccelerometerAngles);
        robotState.robotAcceleration = mAccelerometerAngles[0];
        robotState.robotVelocity = robotVelocity;

        LazySparkMax intakeSpark = HardwareAdapter.getInstance().getIntake().intakeMasterSpark;
        CANEncoder armEncoder = intakeSpark.getEncoder();
        robotState.intakeAngle = armEncoder.getPosition();
        robotState.intakeVelocity = armEncoder.getVelocity();
        robotState.intakeAppliedOutput = intakeSpark.getAppliedOutput();

        double time = Timer.getFPGATimestamp();

        if (!robotState.cancelAuto && robotState.rightStickInput.getButtonPressed(7)) {
            robotState.cancelAuto = true;
        }

        //Rotation2d gyro_angle = Rotation2d.fromRadians((right_distance - left_distance) * Constants.kTrackScrubFactor
        ///Constants.kTrackEffectiveDiameter);
        Rotation2d
                gyroAngle = Rotation2d.fromDegrees(robotState.drivePose.heading),
                gyroVelocity = Rotation2d.fromDegrees(robotState.drivePose.headingVelocity);
        RigidTransform2d odometry = robotState.generateOdometryFromSensors(
                robotState.drivePose.leftEncoderPosition - robotState.drivePose.lastLeftEncoderPosition,
                robotState.drivePose.rightEncoderPosition - robotState.drivePose.lastRightEncoderPosition,
                gyroAngle
        );
        RigidTransform2d.Delta velocity = Kinematics.forwardKinematics(
                robotState.drivePose.leftEncoderVelocity,
                robotState.drivePose.rightEncoderVelocity,
                gyroVelocity.getRadians()
        );

        robotState.addObservations(time, odometry, velocity);

        LazySparkMax pusherSpark = HardwareAdapter.getInstance().getPusher().pusherSpark;
        CANEncoder pusherEncoder = pusherSpark.getEncoder();
        robotState.pusherPosition = pusherEncoder.getPosition();
        robotState.pusherVelocity = pusherEncoder.getVelocity();
        robotState.pusherAppliedOutput = pusherSpark.getAppliedOutput();

        updateUltrasonicSensors(robotState);
    }

    private void updateUltrasonicSensors(RobotState robotState) {
        /* Test for cargo in intake */

        Ultrasonic mUltrasonicLeft = HardwareAdapter.getInstance().getIntake().intakeUltrasonicLeft;
        robotState.leftReadings.add(mUltrasonicLeft.getRangeInches());
        while (robotState.leftReadings.size() > 10) {
            robotState.leftReadings.remove(0);
        }
        Ultrasonic mUltrasonicRight = HardwareAdapter.getInstance().getIntake().intakeUltrasonicRight;
        robotState.rightReadings.add(mUltrasonicRight.getRangeInches());
        while (robotState.rightReadings.size() > 10) {
            robotState.rightReadings.remove(0);
        }

        IntakeConfig intakeConfig = Configs.get(IntakeConfig.class);
        int leftTotal = (int) robotState.leftReadings.stream().filter(i -> (i < intakeConfig.cargoInchTolerance)).count();
        int rightTotal = (int) robotState.rightReadings.stream().filter(i -> (i < intakeConfig.cargoInchTolerance)).count();
        robotState.hasCargo = (leftTotal >= intakeConfig.cargoCountRequired || rightTotal >= intakeConfig.cargoCountRequired);

        robotState.cargoDistance = Math.min(mUltrasonicLeft.getRangeInches(), mUltrasonicRight.getRangeInches());

        /* Test for cargo in carriage */

        // Cargo Distance from Pusher
        Ultrasonic mPusherUltrasonic = HardwareAdapter.getInstance().getPusher().pusherUltrasonic;
        robotState.mPusherReadings.add(mPusherUltrasonic.getRangeInches());
        if (robotState.mPusherReadings.size() > 10) {
            robotState.mPusherReadings.remove(0);
        }

        int pusherTotalClose = (int) robotState.mPusherReadings.stream().filter(i -> i < Configs.get(PusherConfig.class).cargoTolerance).count();
        int pusherTotalFar = (int) robotState.mPusherReadings.stream().filter(i -> i < Configs.get(PusherConfig.class).cargoToleranceFar).count();

        boolean lastHasPusherCargoFar = robotState.hasPusherCargoFar;
        robotState.hasPusherCargo = (pusherTotalClose > OtherConstants.kRequiredUltrasonicCount + 1);
        robotState.hasPusherCargoFar = (pusherTotalFar > OtherConstants.kRequiredUltrasonicCount);

        if (lastHasPusherCargoFar != robotState.hasPusherCargoFar) {
            int properPipeline = robotState.hasCargo ? OtherConstants.kLimelightCargoPipeline : OtherConstants.kLimelightHatchPipeline;
            Limelight.getInstance().setPipeline(properPipeline);
        }

        robotState.cargoPusherDistance = mPusherUltrasonic.getRangeInches();
//		System.out.println(robotState.cargoPusherDistance);
    }

    /**
     * Updates the hardware to run with output values of subsystems
     */
    void updateHardware() {
        updateDrivetrain();
        updateElevator();
        updateShooter();
        updatePusher();
        updateShovel();
        updateFingers();
        updateIntake();
        updateMiscellaneousHardware();
    }

    /**
     * Updates the drivetrain Uses SparkOutput and can run off-board control loops through SRX
     */
    private void updateDrivetrain() {
        updateSparkMax(HardwareAdapter.getInstance().getDrivetrain().leftMasterSpark, mDrive.getDriveSignal().leftOutput);
        updateSparkMax(HardwareAdapter.getInstance().getDrivetrain().rightMasterSpark, mDrive.getDriveSignal().rightOutput);

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
//        HardwareAdapter.getInstance().getMiscellaneousHardware().compressor.stop();
        if (shouldCompress()) {
            HardwareAdapter.getInstance().getMiscellaneousHardware().compressor.start();
        } else {
            HardwareAdapter.getInstance().getMiscellaneousHardware().compressor.stop();
        }
        HardwareAdapter.getInstance().getJoysticks().operatorXboxController.setRumble(shouldRumble());
    }

    /**
     * Runs the compressor only when the pressure too low or the current draw is low enough
     */
    private boolean shouldCompress() {
        return RobotState.getInstance().gamePeriod != RobotState.GamePeriod.AUTO && !RobotState.getInstance().isQuickTurning;
    }

    private boolean shouldRumble() {
        boolean rumble;
        double
                intakeRumbleLength = mIntake.getRumbleLength(),
                shovelRumbleLength = mShovel.getRumbleLength(),
                shooterRumbleLength = mShooter.getRumbleLength();

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

    private void updateShooter() {
        HardwareAdapter.getInstance().getShooter().shooterMasterVictor.set(mShooter.getOutput());
    }

    private void updateElevator() {
        updateSparkMax(HardwareAdapter.getInstance().getElevator().elevatorMasterSpark, mElevator.getOutput());
        HardwareAdapter.getInstance().getElevator().elevatorShifter.set(mElevator.getSolenoidOutput());
    }

    private void updatePusher() {
        updateSparkMax(HardwareAdapter.getInstance().getPusher().pusherSpark, mPusher.getPusherOutput());
    }

    private void updateShovel() {
        HardwareAdapter.getInstance().getShovel().shovelTalon.set(mShovel.getPercentOutput());
        HardwareAdapter.getInstance().getShovel().upDownSolenoid.set(mShovel.getUpDownOutput() ? DoubleSolenoid.Value.kForward : DoubleSolenoid.Value.kReverse);
    }

    private void updateFingers() {
        HardwareAdapter.getInstance().getFingers().openCloseSolenoid.set(mFingers.getOpenCloseOutput());
        HardwareAdapter.getInstance().getFingers().pusherSolenoid.set(mFingers.getExpelOutput());
    }

    private void updateIntake() {
        updateSparkMax(HardwareAdapter.getInstance().getIntake().intakeMasterSpark, mIntake.getSparkOutput());
        HardwareAdapter.getInstance().getIntake().intakeTalon.set(mIntake.getTalonOutput());
//		System.out.println(HardwareAdapter.getInstance().getIntake().intakeMasterSpark.getAppliedOutput());
    }

    void setIdleMode(IdleMode idleMode) {
        HardwareAdapter.getInstance().getDrivetrain().sparks.forEach(spark -> spark.setIdleMode(idleMode));
        HardwareAdapter.getInstance().getIntake().intakeMasterSpark.setIdleMode(idleMode);
        HardwareAdapter.getInstance().getIntake().intakeSlaveSpark.setIdleMode(idleMode);
        HardwareAdapter.getInstance().getElevator().elevatorMasterSpark.setIdleMode(idleMode);
        HardwareAdapter.getInstance().getElevator().elevatorSlaveSpark.setIdleMode(idleMode);
    }

//    private void updateTalonSRX(WPI_TalonSRX talon, TalonSRXOutput output) {
//        if (output.getControlMode().equals(ControlMode.Position) || output.getControlMode().equals(ControlMode.Velocity)
//                || output.getControlMode().equals(ControlMode.MotionMagic)) {
//            talon.config_kP(output.profile, output.gains.p, 0);
//            talon.config_kI(output.profile, output.gains.i, 0);
//            talon.config_kD(output.profile, output.gains.d, 0);
//            talon.config_kF(output.profile, output.gains.f, 0);
//            talon.config_IntegralZone(output.profile, (int) Math.round(output.gains.iZone), 0);
//            talon.configClosedloopRamp(output.gains.rampRate, 0);
//        }
//        if (output.getControlMode().equals(ControlMode.MotionMagic)) {
//            talon.configMotionAcceleration(output.acceleration, 0);
//            talon.configMotionCruiseVelocity(output.cruiseVelocity, 0);
//        }
//        if (output.getControlMode().equals(ControlMode.Velocity)) {
//            talon.configAllowableClosedloopError(output.profile, 0, 0);
//        }
//        if (output.getArbitraryFF() != 0.0 && output.getControlMode().equals(ControlMode.Position)) {
//            talon.set(output.getControlMode(), output.getSetPoint(), DemandType.ArbitraryFeedForward, output.getArbitraryFF());
//        } else {
//            talon.set(output.getControlMode(), output.getSetPoint(), DemandType.Neutral, 0.0);
//        }
//    }

    private void updateSparkMax(LazySparkMax spark, SparkMaxOutput output) {
        ControlType controlType = output.getControlType();
        if (!Configs.get(RobotConfig.class).disableSparkOutput) {
            spark.set(controlType, output.getReference(), output.getArbitraryDemand(), output.getGains());
//            System.out.printf("%s,%s%n", output.getControlType(), output.getReference());
        }
    }
}