package com.palyrobotics.frc2019.config.Constants;

public class PusherConstants {
    /**
     * Pusher
     */
    //TODO: Add values
    public static final int kVidarRequiredUltrasonicCount = 0;
    public static final double kVidarDistanceIn = 0;
    public static final double kVidarDistanceMiddle = 0;
    public static final double kVidarDistanceOut = 0;
    public static final double kVidarCargoTolerance = 0;

    /**
     * Tolerances
     */
    public static final double kAcceptablePositionError = 0;

    /**
     * Unit Conversions
     */
    public static final double kPusherRotationsPerInch = 1.0 / (1.0 * Math.PI); // TODO: change the 1 to the actual sprocket size
    public static final double kPusherEncSpeedUnitConversion = (1.0 / kPusherRotationsPerInch) / 60; // RPM -> in/s
    public static final double kTicksPerInch = 42.0 / (1.0 * Math.PI); // todo: change the 1 to the actual sprocket size
    public static final double kPusherPotSpeedUnitConversion = (1.0 / kTicksPerInch) / OtherConstants.updatesPerSecond; // ticks/20ms -> in/s

}
