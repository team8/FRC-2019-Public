package com.palyrobotics.frc2019.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.ctre.phoenix.sensors.PigeonIMU;
import com.palyrobotics.frc2019.config.Constants;
import com.palyrobotics.frc2019.util.XboxController;
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

		public final WPI_TalonSRX masterTalon;
		public final WPI_TalonSRX slaveTalon;

		protected ShooterHardware() {
			masterTalon = new WPI_TalonSRX(Constants.kShooterMasterDeviceID);
			slaveTalon = new WPI_TalonSRX(Constants.kShooterSlaveDeviceID);
		}
	}

	public static class FingersHardware{
		private static FingersHardware instance = new FingersHardware();

		public static FingersHardware getInstance() { return instance; }

		public final DoubleSolenoid openCloseSolenoid;
		public final DoubleSolenoid expelSolenoid1;
		public final DoubleSolenoid expelSolenoid2;
		public final DoubleSolenoid expelSolenoid3;

		public final WPI_VictorSPX pusherVictor;
		public final Ultrasonic pusherUltrasonic1;
		public final Ultrasonic pusherUltrasonic2;
		public final AnalogPotentiometer pusherPotentiometer;

		protected FingersHardware() {
			openCloseSolenoid = new DoubleSolenoid(Constants.kVidarOpenCloseSolenoidForwardID, Constants.kVidarOpenCloseSolenoidReverseID);
			expelSolenoid1 = new DoubleSolenoid(Constants.kVidarExpelSolenoid1ForwardID, Constants.kVidarExpelSolenoid1ReverseID);
			expelSolenoid2 = new DoubleSolenoid(Constants.kVidarExpelSolenoid2ForwardID, Constants.kVidarExpelSolenoid2ReverseID);
			expelSolenoid3 = new DoubleSolenoid(Constants.kVidarExpelSolenoid3ForwardID, Constants.kVidarExpelSolenoid3ReverseID);

			pusherVictor = new WPI_VictorSPX(Constants.kVidarPusherVictorID);
			pusherUltrasonic1 = new Ultrasonic(Constants.kVidarPusherRightUltrasonicPing, Constants.kVidarPusherRightUltrasonicEcho);
			pusherUltrasonic2 = new Ultrasonic(Constants.kVidarPusherLeftUltrasonicPing, Constants.kVidarPusherLeftUltrasonicEcho);
			pusherPotentiometer = new AnalogPotentiometer(Constants.kVidarPusherPotID, 360, 0);
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

	public ArmHardware getArm(){
		return ArmHardware.getInstance(); 
	}

	public IntakeHardware getIntake() {
		return IntakeHardware.getInstance();
	}

	public ShooterHardware getShooter() { return ShooterHardware.getInstance(); }

	public FingersHardware getFingers() { return FingersHardware.getInstance(); }

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