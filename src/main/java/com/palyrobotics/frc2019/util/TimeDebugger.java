package com.palyrobotics.frc2019.util;

import edu.wpi.first.wpilibj.Timer;

import java.util.ArrayList;
import java.util.Optional;

/**
 * @author Quintin Dwight
 */
public class TimeDebugger {

    private static class Measurement {
        Measurement(String name, double durationSeconds) {
            this.name = name;
            this.durationSeconds = durationSeconds;
        }

        String name;
        double durationSeconds;
    }

    private String m_Name;
    private double m_ReferenceSeconds, m_RunningTimeSeconds;
    private Double m_PrintDuration;
    private ArrayList<Measurement> m_Measurements = new ArrayList<>(8);

    public TimeDebugger(String name) {
        m_Name = name;
        m_ReferenceSeconds = System.nanoTime();
    }

    public TimeDebugger(String name, double printDurationSeconds) {
        this(name);
        m_PrintDuration = printDurationSeconds;
    }

    public void addPoint(String name) {
        double now = Timer.getFPGATimestamp();
        double deltaSeconds = now - m_ReferenceSeconds;
        m_RunningTimeSeconds += deltaSeconds;
        m_ReferenceSeconds = now;
        m_Measurements.add(new Measurement(name, deltaSeconds));
    }

    public void finish() {
        Optional.ofNullable(m_PrintDuration)
                .filter(duration -> m_RunningTimeSeconds > duration)
                .ifPresent(duration -> printSummary());
    }

    public void finishAndPrint() {
        printSummary();
    }

    private void printSummary() {
        System.out.printf("[Time Summary] [%s]%n", m_Name);
        for (Measurement measurement : m_Measurements) {
            System.out.printf("    <%s> %f seconds%n", measurement.name, measurement.durationSeconds);
        }
    }
}
