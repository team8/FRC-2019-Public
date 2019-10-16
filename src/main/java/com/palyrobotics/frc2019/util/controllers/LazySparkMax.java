package com.palyrobotics.frc2019.util.controllers;

import com.palyrobotics.frc2019.config.Gains;
import com.palyrobotics.frc2019.config.RobotConfig;
import com.palyrobotics.frc2019.config.SmartGains;
import com.palyrobotics.frc2019.util.config.Configs;
import com.revrobotics.CANPIDController;
import com.revrobotics.CANPIDController.ArbFFUnits;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;

import java.util.HashMap;
import java.util.Map;

public class LazySparkMax extends CANSparkMax {

    private static Map<ControlType, Integer> sControlTypeToSlot = Map.of(
            ControlType.kSmartMotion, 1,
            ControlType.kSmartVelocity, 2
    );

    private double mLastReference, mLastArbitraryPercentOutput;
    private int mLastSlot;
    private ControlType mLastControlType;
    private Map<Integer, Gains> mLastGains = new HashMap<>();
    private RobotConfig mRobotConfig = Configs.get(RobotConfig.class);

    public LazySparkMax(int deviceNumber) {
        super(deviceNumber, MotorType.kBrushless);
    }

    public void set(ControlType type, double reference, double arbitraryPercentOutput, Gains gains) {
        int slot = sControlTypeToSlot.getOrDefault(type, 0);
        updateGainsIfNeeded(gains, slot);
        if (!gains.equals(mLastGains.get(slot)) || slot != mLastSlot || type != mLastControlType || reference != mLastReference || arbitraryPercentOutput != mLastArbitraryPercentOutput) {
            mLastSlot = slot;
            mLastControlType = type;
            mLastReference = reference;
            mLastArbitraryPercentOutput = arbitraryPercentOutput;
            mLastGains.put(slot, gains);
            getPIDController().setReference(reference, type, slot, arbitraryPercentOutput, ArbFFUnits.kPercentOut);
        }
    }

    private void updateGainsIfNeeded(Gains gains, int slot) {
        CANPIDController controller = getPIDController();
        boolean firstInitialization = !mLastGains.containsKey(slot);
        if (firstInitialization) {
            mLastGains.put(slot, (slot == 1 || slot == 2) ? new SmartGains() : new Gains());
        }
        Gains lastGains = mLastGains.get(slot);
        if (lastGains.p != gains.p) controller.setP(gains.p, slot);
        if (lastGains.i != gains.i) controller.setI(gains.i, slot);
        if (lastGains.d != gains.d) controller.setD(gains.d, slot);
        if (lastGains.f != gains.f) controller.setFF(gains.f, slot);
        if (lastGains.iZone != gains.iZone) controller.setIZone(gains.iZone, slot);
        if (gains instanceof SmartGains) { // TODO maybe we could set this up such that we do not check type
            SmartGains lastSmartGains = (SmartGains) lastGains, smartGains = (SmartGains) gains;
            if (lastSmartGains.acceleration != smartGains.acceleration)
                controller.setSmartMotionMaxAccel(smartGains.acceleration * mRobotConfig.sendMultiplier, slot);
            if (lastSmartGains.velocity != smartGains.velocity)
                controller.setSmartMotionMaxVelocity(smartGains.velocity * mRobotConfig.sendMultiplier, slot);
            if (lastSmartGains.allowableError != smartGains.allowableError)
                controller.setSmartMotionAllowedClosedLoopError(smartGains.allowableError, slot);
            if (lastSmartGains.minimumOutputVelocity != smartGains.minimumOutputVelocity)
                controller.setSmartMotionMinOutputVelocity(smartGains.minimumOutputVelocity, slot);
            if (firstInitialization) {
                controller.setOutputRange(-1.0, 1.0, slot);
                controller.setSmartMotionAccelStrategy(CANPIDController.AccelStrategy.kSCurve, slot); // TODO this does not even do anything as of 1.40
            }
        }
    }
}
