package com.palyrobotics.frc2019.util.control;

import java.util.Objects;

public class SmartGains extends Gains {

    public double acceleration, velocity, allowableError, minimumOutputVelocity;

    public SmartGains() {

    }

    public SmartGains(double p, double i, double d, double f, int iZone, double rampRate, double acceleration, double velocity) {
        super(p, i, d, f, iZone, rampRate);
        this.acceleration = acceleration;
        this.velocity = velocity;
    }

    @Override // Auto-generated
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        if (!super.equals(other)) return false;
        SmartGains otherSmartGains = (SmartGains) other;
        return Double.compare(otherSmartGains.acceleration, acceleration) == 0 &&
                Double.compare(otherSmartGains.velocity, velocity) == 0;
    }

    @Override // Auto-generated
    public int hashCode() {
        return Objects.hash(super.hashCode(), acceleration, velocity);
    }

    @Override // Auto-generated
    public String toString() {
        return String.format("SmartGains{acceleration=%s, velocity=%s, p=%s, i=%s, d=%s, f=%s, rampRate=%s, iZone=%d}", acceleration, velocity, p, i, d, f, rampRate, iZone);
    }
}
