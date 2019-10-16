package com.palyrobotics.frc2019.config.dashboard;

import com.palyrobotics.frc2019.util.service.RobotService;

public class LiveGraph implements RobotService {

    private static LiveGraph sInstance = new LiveGraph();

    public static LiveGraph getInstance() {
        return sInstance;
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {

    }

    @Override
    public String getConfigName() {
        return "liveGraph";
    }
}
