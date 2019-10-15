package com.palyrobotics.frc2019.config;

import com.palyrobotics.frc2019.util.config.AbstractSubsystemConfig;

public class DriveConfig extends AbstractSubsystemConfig {

    // On-board velocity-based follower
    // kV = (gear ratio) / (pi * free speed * wheel diameter)
    // kA = (wheel radius * robot mass) / (total number of motors * gear reduction * motor stall torque)
    // kV ~ 1.1 times theoretical, kA ~ 1.4 times theoretical, kS ~ 1.3V = .11
    // presentation has a typo for kA, should be wheel radius because T = Fr

    public Gains.TrajectoryGains trajectoryGains;

    public Gains cascadingTurnGains = new Gains(65, 0, 5, 0, 0, 0);
}
