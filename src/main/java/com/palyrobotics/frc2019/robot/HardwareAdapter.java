package com.palyrobotics.frc2019.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.ctre.phoenix.sensors.PigeonIMU;
import com.palyrobotics.frc2019.config.Constants.OtherConstants;
import com.palyrobotics.frc2019.config.Constants.PortConstants;
import com.palyrobotics.frc2019.util.XboxController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import edu.wpi.first.wpilibj.*;

/**
 * Represents all hardware components of the robot. Singleton class. Should only be used in robot package, and 254lib. Subdivides hardware into subsystems.
 * Example call: HardwareAdapter.getInstance().getDrivetrain().getLeftMotor()
 *
 * @author Nihar
 */
public class HardwareAdapter {
    //Hardware components at the top for maintenance purposes, variables and getters at bottom

    /**
     * DRIVETRAIN - 6 CANSparkMax, 1 Gyro
     */
    public static class DrivetrainHardware {
        private static DrivetrainHardware instance = new DrivetrainHardware();

        private static DrivetrainHardware getInstance() {
            return instance;
        }

        final CANSparkMax leftMasterSpark;
        final CANSparkMax leftSlave1Spark;
        final CANSparkMax leftSlave2Spark;
        final CANSparkMax rightMasterSpark;
        final CANSparkMax rightSlave1Spark;
        final CANSparkMax rightSlave2Spark;

        final PigeonIMU gyro;

        public void resetSensors() {
            instance.gyro.setYaw(0, 0);
            instance.gyro.setFusedHeading(0, 0);
            instance.gyro.setAccumZAngle(0, 0);
            instance.leftMasterSpark.getEncoder().setPosition(0);
            instance.leftSlave1Spark.getEncoder().setPosition(0);
            instance.leftSlave2Spark.getEncoder().setPosition(0);
            instance.rightMasterSpark.getEncoder().setPosition(0);
            instance.rightSlave1Spark.getEncoder().setPosition(0);
            instance.rightSlave2Spark.getEncoder().setPosition(0);
        }

        DrivetrainHardware() {
            leftMasterSpark = new CANSparkMax(PortConstants.kVidarLeftDriveMasterDeviceID, CANSparkMaxLowLevel.MotorType.kBrushless);
            leftSlave1Spark = new CANSparkMax(PortConstants.kVidarLeftDriveSlave1DeviceID, CANSparkMaxLowLevel.MotorType.kBrushless);
            leftSlave2Spark = new CANSparkMax(PortConstants.kVidarLeftDriveSlave2DeviceID, CANSparkMaxLowLevel.MotorType.kBrushless);
            rightMasterSpark = new CANSparkMax(PortConstants.kVidarRightDriveMasterDeviceID, CANSparkMaxLowLevel.MotorType.kBrushless);
            rightSlave1Spark = new CANSparkMax(PortConstants.kVidarRightDriveSlave1DeviceID, CANSparkMaxLowLevel.MotorType.kBrushless);
            rightSlave2Spark = new CANSparkMax(PortConstants.kVidarRightDriveSlave2DeviceID, CANSparkMaxLowLevel.MotorType.kBrushless);
            gyro = new PigeonIMU(ShovelHardware.getInstance().shovelTalon);
        }
    }

    /**
     * Elevator - 2 CANSparkMax, 1 Encoder, 1 DoubleSolenoid
     */
    public static class ElevatorHardware {
        private static ElevatorHardware instance = new ElevatorHardware();

        private static ElevatorHardware getInstance() {
            return instance;
        }

        public final CANSparkMax elevatorMasterSpark;
        final CANSparkMax elevatorSlaveSpark;
        final DoubleSolenoid elevatorShifter;

        void resetSensors() {
            instance.elevatorMasterSpark.getEncoder().setPosition(0);
            instance.elevatorSlaveSpark.getEncoder().setPosition(0);
        }

        ElevatorHardware() {

            elevatorMasterSpark = new CANSparkMax(PortConstants.kVidarElevatorMasterSparkID, CANSparkMaxLowLevel.MotorType.kBrushless);
            elevatorSlaveSpark = new CANSparkMax(PortConstants.kVidarElevatorSlaveSparkID, CANSparkMaxLowLevel.MotorType.kBrushless);
            elevatorShifter = new DoubleSolenoid(0, PortConstants.kVidarElevatorDoubleSolenoidForwardsID, PortConstants.kVidarElevatorDoubleSolenoidReverseID);
//            elevatorHolderSolenoid = new Solenoid(1,PortConstants.kVidarElevatorHolderSolenoidID);
        }
    }

    /**
     * Intake - 2 CANSparkMax, 1 WPI_TalonSRX, 2 Ultrasonics
     */
    public static class IntakeHardware {
        private static IntakeHardware instance = new IntakeHardware();

        private static IntakeHardware getInstance() {
            return instance;
        }

        final WPI_TalonSRX intakeTalon;
        final CANSparkMax intakeMasterSpark;
        final CANSparkMax intakeSlaveSpark;
        final Ultrasonic intakeUltrasonicLeft;
        final Ultrasonic intakeUltrasonicRight;
        final AnalogPotentiometer potentiometer;

        void resetSensors() {
            instance.intakeMasterSpark.getEncoder().setPosition(0);
            instance.intakeSlaveSpark.getEncoder().setPosition(0);
        }

        IntakeHardware() {


            intakeTalon = new WPI_TalonSRX(PortConstants.kVidarIntakeTalonDeviceID);
            intakeMasterSpark = new CANSparkMax(PortConstants.kVidarIntakeMasterDeviceID, CANSparkMaxLowLevel.MotorType.kBrushless);
            intakeSlaveSpark = new CANSparkMax(PortConstants.kVidarIntakeSlaveDeviceID, CANSparkMaxLowLevel.MotorType.kBrushless);
            intakeUltrasonicLeft = new Ultrasonic(PortConstants.kVidarIntakeLeftUltrasonicPing, PortConstants.kVidarIntakeLeftUltrasonicEcho);
            intakeUltrasonicRight = new Ultrasonic(PortConstants.kVidarIntakeRightUltrasonicPing, PortConstants.kVidarIntakeRightUltrasonicEcho);
            potentiometer = new AnalogPotentiometer(PortConstants.kVidarAnalogPot);

        }
    }

    /**
     * Pusher - 1 WPI_VictorSPX, 2 Ultrasonics
     */
    public static class PusherHardware {
        private static PusherHardware instance = new PusherHardware();

        private static PusherHardware getInstance() {
            return instance;
        }

        final CANSparkMax pusherSpark;
        final Ultrasonic pusherUltrasonic;
//		public final Ultrasonic pusherSecondaryUltrasonic;
//		public final AnalogPotentiometer pusherPotentiometer;

        public void resetSensors() {
            instance.pusherSpark.getEncoder().setPosition(0);
        }

        PusherHardware() {

            pusherSpark = new CANSparkMax(PortConstants.kVidarPusherSparkID, CANSparkMaxLowLevel.MotorType.kBrushless);
            pusherUltrasonic = new Ultrasonic(PortConstants.kVidarPusherUltrasonicPing, PortConstants.kVidarPusherUltrasonicEcho);
//			pusherSecondaryUltrasonic = new Ultrasonic(PortConstants.kVidarBackupUltrasonicPing, PortConstants.kVidarBackupUltrasonicEcho);
//			pusherPotentiometer = new AnalogPotentiometer(PortConstants.kVidarPusherPotID, 360, 0);

        }
    }

    public static class ShooterHardware {
        private static ShooterHardware instance = new ShooterHardware();

        private static ShooterHardware getInstance() {
            return instance;
        }

        final WPI_VictorSPX shooterMasterVictor;
        final WPI_VictorSPX shooterSlaveVictor;

        ShooterHardware() {

            shooterMasterVictor = new WPI_VictorSPX(PortConstants.kVidarShooterMasterVictorDeviceID);
            shooterSlaveVictor = new WPI_VictorSPX(PortConstants.kVidarShooterSlaveVictorDeviceID);

        }
    }

    /**
     * Hatch Intake - 1 WPI_VictorSPX, 1 SingleSolenoid
     */
    public static class ShovelHardware {
        private static ShovelHardware instance = new ShovelHardware();

        private static ShovelHardware getInstance() {
            return instance;
        }

        final WPI_TalonSRX shovelTalon;
        final DoubleSolenoid upDownSolenoid;
        final DigitalInput upDownHFX;

        ShovelHardware() {

            shovelTalon = new WPI_TalonSRX(PortConstants.kVidarShovelDeviceID);
            upDownSolenoid = new DoubleSolenoid(0, PortConstants.kVidarShovelSolenoidUpDownID, PortConstants.kVidarShovelSolenoidUpDownID2);
            upDownHFX = new DigitalInput(PortConstants.kVidarShovelHFXPort);
        }
    }

    public static class FingersHardware {
        private static FingersHardware instance = new FingersHardware();

        public static FingersHardware getInstance() {
            return instance;
        }

        final DoubleSolenoid openCloseSolenoid;
        final DoubleSolenoid pusherSolenoid;

        FingersHardware() {
            openCloseSolenoid = new DoubleSolenoid(0, PortConstants.kVidarOpenCloseSolenoidForwardID, PortConstants.kVidarOpenCloseSolenoidReverseID);
            pusherSolenoid = new DoubleSolenoid(0, PortConstants.kVidarExpelSolenoidForwardID, PortConstants.kVidarExpelSolenoidReverseID);
        }

    }

    //Joysticks for operator interface
    public static class Joysticks {
        private static Joysticks instance = new Joysticks();

        private static Joysticks getInstance() {
            return instance;
        }

        public final Joystick driveStick = new Joystick(0);
        public final Joystick turnStick = new Joystick(1);
        //		public final Joystick backupStick = new Joystick(3);
		XboxController operatorXboxController = null;

        Joysticks() {
            if (OtherConstants.operatorXBoxController) {
                operatorXboxController = new XboxController(2, false);
            }
        }
    }

    /**
     * Miscellaneous Hardware - Compressor sensor(Analog Input), Compressor, PDP
     */
    public static class MiscellaneousHardware {
        private static MiscellaneousHardware instance = new MiscellaneousHardware();

        private static MiscellaneousHardware getInstance() {
            return instance;
        }

        final Compressor compressor;
        final PowerDistributionPanel pdp;

        MiscellaneousHardware() {
            compressor = new Compressor();
            pdp = new PowerDistributionPanel();
        }

    }

    //Wrappers to access hardware groups
    public DrivetrainHardware getDrivetrain() {
        return DrivetrainHardware.getInstance();
    }

    public ElevatorHardware getElevator() {
        return ElevatorHardware.getInstance();
    }

    public IntakeHardware getIntake() {
        return IntakeHardware.getInstance();
    }

    public ShooterHardware getShooter() {
        return ShooterHardware.getInstance();
    }

    public ShovelHardware getShovel() {
        return ShovelHardware.getInstance();
    }

    public PusherHardware getPusher() {
        return PusherHardware.getInstance();
    }

    public FingersHardware getFingers() {
        return FingersHardware.getInstance();
    }

    Joysticks getJoysticks() {
        return Joysticks.getInstance();
    }

    MiscellaneousHardware getMiscellaneousHardware() {
        return MiscellaneousHardware.getInstance();
    }

    //Singleton set up
    private static final HardwareAdapter instance = new HardwareAdapter();

    public static HardwareAdapter getInstance() {
        return instance;
    }
}