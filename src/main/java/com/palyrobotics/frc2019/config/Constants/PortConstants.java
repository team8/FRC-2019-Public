package com.palyrobotics.frc2019.config.Constants;

public class PortConstants {
    //DRIVETRAIN
    //CAN BUS slots for drivetrain
    public static final int kVidarLeftDriveMasterDeviceID = 15;
    public static final int kVidarLeftDriveSlave1DeviceID = 14;
    public static final int kVidarLeftDriveSlave2DeviceID = 13;

    public static final int kVidarRightDriveMasterDeviceID = 0;
    public static final int kVidarRightDriveSlave1DeviceID = 1;
    public static final int kVidarRightDriveSlave2DeviceID = 2;

    //ELEVATOR
    public static final int kVidarElevatorMasterSparkID = 3;
    public static final int kVidarElevatorSlaveSparkID = 12;

    public static final int kVidarElevatorDoubleSolenoidForwardsID = 2;
    public static final int kVidarElevatorDoubleSolenoidReverseID = 5;
    public static final int kVidarElevatorHolderSolenoidID = 0;

    //INTAKE
    public static final int kVidarIntakeMasterDeviceID = 4;
    public static final int kVidarIntakeSlaveDeviceID = 5;
    public static final int kVidarIntakeVictorDeviceID = 6;
    public static final int kVidarAnalogPot = 1;

    //SHOOTER
    public static final int kVidarShooterMasterVictorDeviceID = 10;
    public static final int kVidarShooterSlaveVictorDeviceID = 7;

    //SHOVEL
    public static final int kVidarShovelDeviceID = 9;
    public static final int kVidarShovelSolenoidUpDownID = 0;
    public static final int kVidarShovelSolenoidUpDownID2 = 7;

    public static final int kVidarShovelHFXPort = 2;
    public static final int kVidarShovelPDPPort = 0;

    //PUSHER
    public static final int kVidarPusherSparkID = 8;
    public static final int kVidarPusherPotID = 0;

    //FINGERS
    public static final int kVidarOpenCloseSolenoidForwardID = 1;
    public static final int kVidarOpenCloseSolenoidReverseID = 6;
    public static final int kVidarExpelSolenoidForwardID = 3;
    public static final int kVidarExpelSolenoidReverseID = 4;

    /**
     * Ultrasonics
     */

    public static final int kLeftUltrasonicPing = 10;
    public static final int kLeftUltrasonicEcho = 11;
    public static final int kRightUltrasonicPing = 12;
    public static final int kRightUltrasonicEcho = 13;

    public static final int kVidarPusherRightUltrasonicPing = 5;
    public static final int kVidarPusherRightUltrasonicEcho = 6;
    public static final int kVidarPusherLeftUltrasonicPing = 7;
    public static final int kVidarPusherLeftUltrasonicEcho = 8;
}
