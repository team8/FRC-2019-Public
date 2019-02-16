package com.palyrobotics.frc2019.config.Constants;

public class IntakeConstants {
    /**
     * Intake Constants
     */
    public static final double kMotorVelocity = .4;
    public static final double kFastIntakingVelocity = 0.8;
    public static final double kDroppingVelocity = -0.225;

    public static final double kMaxAngle = 120;
    public static final double kMaxAngleTicks = 2000;

    // FEED FORWARD CONSTANTS

    public static final double kGravityFF = 0;
    public static final double kAccelComp = 0;
    public static final double kCentripetalCoeff = 0;

    // INTAKING POSITIONS

    public static final double kIntakingPosition = 10; // degrees relatively to the plane of the field.
    public static final double kHoldingPosition = 65; // same relative angle as above
    public static final double kHandoffPosition = 90; // place where the drop to the elevator occurs

    /**
     * Tolerances
     */
    public static final double kAcceptableAngularError = 3;
    public static final double kAngularVelocityError = .05;
    public static final double kCargoInchTolerance = 3;

    /**
     * Unit Conversions
     */
    public static final double kArmPotentiometerTicksPerDegree = 12.0/54.0 * 1/360 * 5;
    // Using the NEO built in Encoder, so we must account for reduction.  ~= 42 / 360 * 118
    public static final double kArmDegreesPerRevolution = (360.0) * (68.0/14.0) * (38.0/18.0) * (36.0/14.0) * (54.0/12.0);
    public static final double kArmEncoderSpeedUnitConversion = kArmDegreesPerRevolution / 60.0; // RPM -> Degrees per Second
    //TODO: Fix below
    public static final double kIntakeTicksPerInch = 0;

}
