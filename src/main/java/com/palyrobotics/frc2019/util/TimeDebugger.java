package com.palyrobotics.frc2019.util;

import java.util.ArrayList;

public class TimeDebugger {

    private long m_Reference;
    private int m_Index;
//    private ArrayList<Double> m_Measurements = new ArrayList<Double>(8);

    public TimeDebugger() {
        m_Reference = System.nanoTime();
    }

    public void addPoint() {
        long now = System.nanoTime();
        double deltaSeconds = (now - m_Reference) / 1e9;
        System.out.printf("[Time Debugger] %d %f\n", m_Index++, deltaSeconds);
        m_Reference = now;
//        m_Measurements.add(deltaSeconds);
    }
}
