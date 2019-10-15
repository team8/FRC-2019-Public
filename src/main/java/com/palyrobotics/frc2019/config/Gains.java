package com.palyrobotics.frc2019.config;

import com.palyrobotics.frc2019.util.trajectory.Trajectory;

import java.util.Objects;

public class Gains {

    // region Gains

    // Use these for off-board following
    private static final double kVidarDriveVelocitykP = .01;
    private static final double kVidarDriveVelocitykI = 0;
    private static final double kVidarDriveVelocitykD = .005;
    private static final double kVidarDriveVelocitykF = 0;
    private static final int kVidarDriveVelocitykIzone = 0;
    private static final double kVidarDriveVelocitykRampRate = 0.0;
    public static final Gains vidarVelocity = new Gains(kVidarDriveVelocitykP, kVidarDriveVelocitykI, kVidarDriveVelocitykD, kVidarDriveVelocitykF,
            kVidarDriveVelocitykIzone, kVidarDriveVelocitykRampRate);

    //Drive Distance PID control loop
    public static final double kVidarDriveStraightTurnkP = -0.06;
    private static final double kVidarDriveDistancekP = 0.5;
    private static final double kVidarDriveDistancekI = 0.0025;
    private static final double kVidarDriveDistancekD = 12.0;
    private static final int kVidarDriveDistancekIzone = 125;
    private static final double kVidarDriveDistancekRampRate = 0.0;
    public static final Gains vidarDriveDistance = new Gains(kVidarDriveDistancekP, kVidarDriveDistancekI, kVidarDriveDistancekD, 0,
            kVidarDriveDistancekIzone, kVidarDriveDistancekRampRate);

    //Drive Motion Magic off-board control loop
    //Short distance max speed 45 in/s Max accel 95 in/s^2
    public static final double kVidarShortDriveMotionMagicCruiseVelocity = 60;
    public static final double kVidarShortDriveMotionMagicMaxAcceleration = 120;
    private static final double kVidarShortDriveMotionMagickP = .5;
    private static final double kVidarShortDriveMotionMagickI = 0; //0.00040 / 2;
    private static final double kVidarShortDriveMotionMagickD = 0; //275 / 2;
    private static final double kVidarShortDriveMotionMagickF = .1821; //2.075 / 2;
    private static final int kVidarShortDriveMotionMagickIzone = 0; //150 / 2;
    private static final double kVidarShortDriveMotionMagickRampRate = 0.0;
    public static final Gains vidarShortDriveMotionMagicGains = new Gains(kVidarShortDriveMotionMagickP, kVidarShortDriveMotionMagickI,
            kVidarShortDriveMotionMagickD, kVidarShortDriveMotionMagickF, kVidarShortDriveMotionMagickIzone, kVidarShortDriveMotionMagickRampRate);

    //Drive Motion Magic turn angle gains
    public static final double kVidarTurnMotionMagicCruiseVelocity = 72;
    public static final double kVidarTurnMotionMagicMaxAcceleration = 36;
    private static final double kVidarTurnMotionMagickP = 6.0;
    private static final double kVidarTurnMotionMagickI = 0.01;
    private static final double kVidarTurnMotionMagickD = 210;
    private static final double kVidarTurnMotionMagickF = 2.0;
    private static final int kVidarTurnMotionMagickIzone = 50;
    private static final double kVidarTurnMotionMagickRampRate = 0.0;
    public static final Gains vidarTurnMotionMagicGains = new Gains(kVidarTurnMotionMagickP, kVidarTurnMotionMagickI, kVidarTurnMotionMagickD,
            kVidarTurnMotionMagickF, kVidarTurnMotionMagickIzone, kVidarTurnMotionMagickRampRate);

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

    public static class TrajectoryGains {

        public double v, a, s, p, i, d;

        public TrajectoryGains() {

        }

        @Override
        public String toString() { // Auto-generated
            return String.format("TrajectoryGains{v=%f, a=%f, s=%f, p=%f, i=%f, d=%f}", v, a, s, p, i, d);
        }
    }

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
