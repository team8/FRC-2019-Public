package com.palyrobotics.frc2019.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.ctre.phoenix.sensors.PigeonIMU;
import com.palyrobotics.frc2019.config.Constants;
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
	/*
	 * DRIVETRAIN - 2 WPI_TalonSRX's and 4 WPI_VictorSPX's
	 */
	public static class DrivetrainHardware {
		private static DrivetrainHardware instance = new DrivetrainHardware();

		private static DrivetrainHardware getInstance() {
			return instance;
		}

		public final WPI_TalonSRX leftMasterTalon;
		public final WPI_VictorSPX leftSlave1Victor;
		public final WPI_VictorSPX leftSlave2Victor;
        public final WPI_TalonSRX rightMasterTalon;
		public final WPI_VictorSPX rightSlave1Victor;
		public final WPI_VictorSPX rightSlave2Victor;
		
		public final PigeonIMU gyro;

		public static void resetSensors() {
			instance.gyro.setYaw(0, 0);
			instance.gyro.setFusedHeading(0, 0);
			instance.gyro.setAccumZAngle(0, 0);
			instance.leftMasterTalon.setSelectedSensorPosition(0, 0, 0);
			instance.rightMasterTalon.setSelectedSensorPosition(0, 0, 0);
		}

		protected DrivetrainHardware() {
			leftMasterTalon = new WPI_TalonSRX(Constants.kVidarLeftDriveMasterDeviceID);
			leftSlave1Victor = new WPI_VictorSPX(Constants.kVidarLeftDriveSlave1DeviceID);
			leftSlave2Victor = new WPI_VictorSPX(Constants.kVidarLeftDriveSlave2DeviceID);
            rightMasterTalon = new WPI_TalonSRX(Constants.kVidarRightDriveMasterDeviceID);
			rightSlave1Victor = new WPI_VictorSPX(Constants.kVidarRightDriveSlave1DeviceID);
			rightSlave2Victor = new WPI_VictorSPX(Constants.kVidarRightDriveSlave2DeviceID);

			gyro = new PigeonIMU(0);
		}
	}

    /**
     *  Elevator - 2 CANSparkMax, 1 HFX, 1 Encoder, 1 DoubleSolenoid
     */
    public static class ElevatorHardware {
        private static ElevatorHardware instance = new ElevatorHardware();

        private static ElevatorHardware getInstance() {
            return instance;
        }

        public final CANSparkMax elevatorMasterSpark;
        public final CANSparkMax elevatorSlaveSpark;
        public final DoubleSolenoid elevatorDoubleSolenoid;

        protected ElevatorHardware() {
            elevatorMasterSpark = new CANSparkMax(Constants.kVidarElevatorMasterSparkID, CANSparkMaxLowLevel.MotorType.kBrushless);
            elevatorSlaveSpark = new CANSparkMax(Constants.kVidarElevatorSlaveSparkID, CANSparkMaxLowLevel.MotorType.kBrushless);
            elevatorDoubleSolenoid = new DoubleSolenoid(Constants.kVidarElevatorDoubleSolenoidForwardsID, Constants.kVidarElevatorDoubleSolenoidReverseID);
        }
    }

	/**
	 * Arm - 1 WPI_TalonSRX, 1 WPI_VictorSPX
	 */
	public static class ArmHardware {
		private static ArmHardware instance = new ArmHardware(); 

		private static ArmHardware getInstance() {
			return instance; 
		}

		public final WPI_TalonSRX armMasterTalon; 
		public final WPI_VictorSPX armSlaveVictor; 
		public final AnalogPotentiometer armPot;

		protected ArmHardware() {
			armMasterTalon = new WPI_TalonSRX(Constants.kForesetiArmMasterTalonID); 
			armSlaveVictor = new WPI_VictorSPX(Constants.kVidarArmSlaveVictorID);
			armPot = new AnalogPotentiometer(Constants.kVidarArmPotID, 360, 0);
		}
	}
	/**
	 * Intake - 2 WPI_TalonSRX's, 2 DoubleSolenoids, 1 Distance Sensor (AnalogInput)
	 */
	public static class IntakeHardware {
		private static IntakeHardware instance = new IntakeHardware();

		private static IntakeHardware getInstance() {
			return instance;
		}

		public final WPI_VictorSPX masterTalon;
		public final WPI_VictorSPX slaveTalon;
		public final DoubleSolenoid inOutSolenoid;
		public final Ultrasonic ultrasonic1;
		public final Ultrasonic ultrasonic2;
		public final Spark LED;

		protected IntakeHardware() {
			masterTalon = new WPI_VictorSPX(Constants.kVidarIntakeMasterDeviceID);
			slaveTalon = new WPI_VictorSPX(Constants.kVidarIntakeSlaveDeviceID);
			if (Constants.kRobotName == Constants.RobotName.VIDAR) {
				inOutSolenoid = new DoubleSolenoid(0,Constants.kInOutSolenoidA, Constants.kInOutSolenoidB);
			}
			else {
				inOutSolenoid = null;
			}
			ultrasonic1 = new Ultrasonic(Constants.kLeftUltrasonicPing,Constants.kLeftUltrasonicEcho);
			ultrasonic2 = new Ultrasonic(Constants.kRightUltrasonicPing,Constants.kRightUltrasonicEcho);

			LED = new Spark(0);
		}
	}

	public static class ShooterHardware{
		private static ShooterHardware instance = new ShooterHardware();

		private static ShooterHardware getInstance() { return instance; }

		public final WPI_VictorSPX shooterMasterVictor;
		public final WPI_VictorSPX shooterSlaveVictor;

		protected ShooterHardware() {
			shooterMasterVictor = new WPI_VictorSPX(Constants.kVidarShooterMasterVictorDeviceID);
			shooterSlaveVictor = new WPI_VictorSPX(Constants.kVidarShooterSlaveVictorDeviceID);
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
		public Joystick climberStick = null;
		public Joystick operatorJoystick = null;
		public XboxController operatorXboxController = null;

		protected Joysticks() {
			if(Constants.operatorXBoxController) {
				operatorXboxController = new XboxController(2, false);
			} else {
				operatorJoystick = new Joystick(3);
				climberStick = new Joystick(2);
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

        public final Compressor compressor;
		public final PowerDistributionPanel pdp;

        protected MiscellaneousHardware() {
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

	public ArmHardware getArm(){
		return ArmHardware.getInstance(); 
	}

	public IntakeHardware getIntake() {
		return IntakeHardware.getInstance();
	}

	public ShooterHardware getShooter() { return ShooterHardware.getInstance(); }

	public Joysticks getJoysticks() {
		return Joysticks.getInstance();
	}

	public MiscellaneousHardware getMiscellaneousHardware() {
	    return MiscellaneousHardware.getInstance();
    }

	//Singleton set up
	private static final HardwareAdapter instance = new HardwareAdapter();

	public static HardwareAdapter getInstance() {
		return instance;
	}
}