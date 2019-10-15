package com.palyrobotics.frc2019.util.controllers;

import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;

import java.util.Map;

public class LazySparkMax extends CANSparkMax {

    private static Map<ControlType, Integer> sControlTypeToSlot = Map.of(
            ControlType.kSmartMotion, 1,
            ControlType.kSmartVelocity, 2
    );

    private double mLastReference, mLastArbitraryPercentOutput;
    private int mLastSlot;
    private ControlType mLastControlType;

    public LazySparkMax(int deviceNumber) {
        super(deviceNumber, MotorType.kBrushless);
    }

    public void set(ControlType type, double reference, double arbitraryPercentOutput) {
        int slot = sControlTypeToSlot.getOrDefault(type, 0);
        if (slot != mLastSlot || type != mLastControlType || reference != mLastReference || arbitraryPercentOutput != mLastArbitraryPercentOutput) {
            mLastSlot = slot;
            mLastControlType = type;
            mLastReference = reference;
            mLastArbitraryPercentOutput = arbitraryPercentOutput;
            super.getPIDController().setReference(reference, type);
        }
    }
}
