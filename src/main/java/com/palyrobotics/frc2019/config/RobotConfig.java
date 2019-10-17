package com.palyrobotics.frc2019.config;

import com.palyrobotics.frc2019.util.config.AbstractConfig;

import java.util.List;

public class RobotConfig extends AbstractConfig {

    public boolean
            coastDriveIfDisabled, coastElevatorIfDisabled, coastArmIfDisabled,
            disableSparkOutput;
    public double sendMultiplier;
    public List<String> enabledServices, enabledSubsystems;
}