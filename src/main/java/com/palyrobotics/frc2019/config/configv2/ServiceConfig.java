package com.palyrobotics.frc2019.config.configv2;

import com.palyrobotics.frc2019.util.configv2.AbstractConfig;

public class ServiceConfig extends AbstractConfig {

    public boolean competitionMode;
    public boolean enableLogger, enableCommandReceiver, enableDashboard;
    // Subsystems
    public boolean enableDrive, enableElevator, enableShooter, enablePusher, enableShovel, enableFingers, enableIntake;
}
