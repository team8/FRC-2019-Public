package com.palyrobotics.frc2019.behavior.routines.drive;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.subsystems.Drive;
import com.palyrobotics.frc2019.subsystems.Subsystem;
import com.palyrobotics.frc2019.util.SparkDriveSignal;

public class DriveTimeRoutine extends Routine {
    private long mEndTime;
    private SparkDriveSignal mDrivePower;

    /**
     * Constructs with a specified time set point and velocity
     *
     * @param time       How long to drive (seconds)
     * @param drivePower LegacyDrive signal to output (left/right speeds -1 to 1)
     */
    public DriveTimeRoutine(double time, SparkDriveSignal drivePower) {
        //Keeps the offset prepared, when routine starts, will add System.currentTime
        mEndTime = (long) (1000 * time);
        mDrivePower = drivePower;
    }

    @Override
    public void start() {
        mDrive.resetController();
        //mEndTime already has the desired drive time
        mEndTime += System.currentTimeMillis();
    }

    // Routines just change the states of the robot set points, which the behavior manager then moves the physical subsystems based on.
    @Override
    public Commands update(Commands commands) {
        commands.wantedDriveState = Drive.DriveState.OPEN_LOOP;
        commands.robotSetPoints.drivePowerSetPoint = mDrivePower;
        return commands;
    }

    @Override
    public Commands cancel(Commands commands) {
//		Logger.getInstance().logRobotThread(Level.FINE, "Cancelling");
        commands.wantedDriveState = Drive.DriveState.NEUTRAL;
        mDrive.resetController();
        mDrive.setNeutral();
        return commands;
    }

    @Override
    public boolean finished() {
        //Finish after the time is up
        return (System.currentTimeMillis() >= mEndTime);
    }

    @Override
    public String getName() {
        return "DriveTimeRoutine";
    }

    @Override
    public Subsystem[] getRequiredSubsystems() {
        return new Subsystem[]{mDrive};
    }
}