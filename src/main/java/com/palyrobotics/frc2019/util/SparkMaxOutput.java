package com.palyrobotics.frc2019.util;

import com.revrobotics.ControlType;

public class SparkMaxOutput {
    private ControlType mSparkMode;

    // Output Reference
    private double mSparkReference;

    private double mArbitraryDemand;

    public SparkMaxOutput() {
        this(ControlType.kDutyCycle);
    }

    public SparkMaxOutput(ControlType controlType) {
        mSparkMode = controlType;
    }

    public void setTargetSmartVelocity(double targetVelocity, double arbitraryDemand) {
        mSparkMode = ControlType.kSmartVelocity;
        mSparkReference = targetVelocity;
        mArbitraryDemand = arbitraryDemand;
    }

    public void setTargetVelocity(double targetVelocity) {
        setTargetVelocity(targetVelocity, 0.0);
    }

    public void setTargetVelocity(double targetVelocity, double arbitraryDemand) {
        mSparkMode = ControlType.kVelocity;
        mSparkReference = targetVelocity;
        mArbitraryDemand = arbitraryDemand;
    }

    public void setTargetPosition(double positionSetPoint) {
        setTargetPosition(positionSetPoint, 0.0);
    }

    public void setTargetPosition(double positionSetPoint, double arbitraryDemand) {
        mSparkMode = ControlType.kPosition;
        mSparkReference = positionSetPoint;
        mArbitraryDemand = arbitraryDemand;
    }

    public void setTargetPositionSmartMotion(double positionSetPoint) {
        setTargetPositionSmartMotion(positionSetPoint, 0.0);
    }

    public void setTargetPositionSmartMotion(double positionSetPoint, double arbitraryDemand) {
        mSparkMode = ControlType.kSmartMotion;
        mSparkReference = positionSetPoint;
        mArbitraryDemand = arbitraryDemand;
    }

    public void setIdle() {
        setPercentOutput(0.0);
    }

    public void setPercentOutput(double output) {
        mSparkMode = ControlType.kDutyCycle;
        mSparkReference = output;
        mArbitraryDemand = 0.0;
    }

    public void setVoltage(double output) {
        mSparkMode = ControlType.kVoltage;
        mSparkReference = output;
        mArbitraryDemand = 0.0;
    }

    public double getReference() {
        return mSparkReference;
    }

    public double getArbitraryDemand() {
        return mArbitraryDemand;
    }

    public ControlType getControlType() {
        return mSparkMode;
    }
}
