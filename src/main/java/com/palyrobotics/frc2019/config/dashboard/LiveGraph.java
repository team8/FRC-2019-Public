package com.palyrobotics.frc2019.config.dashboard;

import com.palyrobotics.frc2019.util.service.RobotService;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class LiveGraph implements RobotService {

    private static LiveGraph sInstance = new LiveGraph();

    private static NetworkTable sLiveTable = NetworkTableInstance.getDefault().getTable("control-center-live");

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

    public void put(String key, double value) {
        sLiveTable.getEntry(key).setDouble(value);
    }
}
