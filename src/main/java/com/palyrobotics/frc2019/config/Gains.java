package com.palyrobotics.frc2019.config;

import java.util.Objects;

public class Gains {

    // region Gains

    // Drive Distance PID control loop
    public static final double kVidarDriveStraightTurnP = -0.06;
    private static final double kVidarDriveDistanceP = 0.5;
    private static final double kVidarDriveDistanceI = 0.0025;
    private static final double kVidarDriveDistanceD = 12.0;
    private static final int kVidarDriveDistanceIZone = 125;
    private static final double kVidarDriveDistanceRampRate = 0.0;
    public static final Gains vidarDriveDistance = new Gains(
            kVidarDriveDistanceP, kVidarDriveDistanceI, kVidarDriveDistanceD, 0,
            kVidarDriveDistanceIZone, kVidarDriveDistanceRampRate
    );

    // Drive Motion Magic off-board control loop
    // Short distance max speed 45 in/s Max acceleration 95 in/s^2
    public static final double kVidarShortDriveMotionMagicCruiseVelocity = 60;
    public static final double kVidarShortDriveMotionMagicMaxAcceleration = 120;
    private static final double kVidarShortDriveMotionMagicP = .5;
    private static final double kVidarShortDriveMotionMagicI = 0; //0.00040 / 2;
    private static final double kVidarShortDriveMotionMagicD = 0; //275 / 2;
    private static final double kVidarShortDriveMotionMagicF = .1821; //2.075 / 2;
    private static final int kVidarShortDriveMotionMagicIZone = 0; //150 / 2;
    private static final double kVidarShortDriveMotionMagicRampRate = 0.0;
    public static final Gains vidarShortDriveMotionMagicGains = new Gains(
            kVidarShortDriveMotionMagicP, kVidarShortDriveMotionMagicI,
            kVidarShortDriveMotionMagicD, kVidarShortDriveMotionMagicF, kVidarShortDriveMotionMagicIZone, kVidarShortDriveMotionMagicRampRate
    );

    // Drive Motion Magic turn angle gains
    public static final double kVidarTurnMotionMagicCruiseVelocity = 72;
    public static final double kVidarTurnMotionMagicMaxAcceleration = 36;
    private static final double kVidarTurnMotionMagickP = 6.0;
    private static final double kVidarTurnMotionMagickI = 0.01;
    private static final double kVidarTurnMotionMagickD = 210;
    private static final double kVidarTurnMotionMagickF = 2.0;
    private static final int kVidarTurnMotionMagickIzone = 50;
    private static final double kVidarTurnMotionMagickRampRate = 0.0;
    public static final Gains vidarTurnMotionMagicGains = new Gains(
            kVidarTurnMotionMagickP, kVidarTurnMotionMagickI, kVidarTurnMotionMagickD,
            kVidarTurnMotionMagickF, kVidarTurnMotionMagickIzone, kVidarTurnMotionMagickRampRate
    );

    private static final double
            kVidarIntakePositionP = 0.27, // 0.3;
            kVidarIntakePositionI = 0.0,
            kVidarIntakePositionD = 0.0, // 2.2;
            kVidarIntakePositionF = 0.0,
            kVidarIntakePositionIZone = 0.0,
            kVidarIntakePositionRampRate = 1.0;
    public static final Gains intakePosition = new Gains(
            kVidarIntakePositionP, kVidarIntakePositionI, kVidarIntakePositionD, kVidarIntakePositionF,
            kVidarIntakePositionIZone, kVidarIntakePositionRampRate
    );

    private static final double
            kVidarElevatorPositionP = 0.5, // 0.7;
            kVidarElevatorPositionI = 0.0,
            kVidarElevatorPositionD = 3.4, // 2.0;
            kVidarElevatorPositionF = 0.0,
            kVidarElevatorPositionIZone = 0.0,
            kVidarElevatorPositionRampRate = 0.0;
    public static final Gains elevatorPosition = new Gains(
            kVidarElevatorPositionP, kVidarElevatorPositionI, kVidarElevatorPositionD, kVidarElevatorPositionF,
            kVidarElevatorPositionIZone, kVidarElevatorPositionRampRate
    );

    //endregion

    public double p, i, d, f, rampRate, iZone;

    public Gains() {
    }

    public Gains(double p, double i, double d, double f, double iZone, double rampRate) {
        this.p = p;
        this.i = i;
        this.d = d;
        this.f = f;
        this.iZone = iZone;
        this.rampRate = rampRate;
    }

    @Override // Auto-generated
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Gains gains = (Gains) other;
        return Double.compare(gains.p, p) == 0 &&
                Double.compare(gains.i, i) == 0 &&
                Double.compare(gains.d, d) == 0 &&
                Double.compare(gains.f, f) == 0 &&
                Double.compare(gains.rampRate, rampRate) == 0 &&
                iZone == gains.iZone;
    }

    @Override // Auto-generated
    public int hashCode() {
        return Objects.hash(p, i, d, f, rampRate, iZone);
    }

    @Override // Auto-generated
    public String toString() {
        return String.format("Gains{p=%f, i=%f, d=%f, f=%f, rampRate=%f, iZone=%f}", p, i, d, f, rampRate, iZone);
    }
}
