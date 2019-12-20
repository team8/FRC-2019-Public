package com.palyrobotics.frc2019.behavior.routines.drive;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.subsystems.Drive;
import com.palyrobotics.frc2019.subsystems.Subsystem;
import com.palyrobotics.frc2019.util.SparkDriveSignal;

import edu.wpi.first.wpilibj.Timer;

@JsonPropertyOrder ({ "time", "drivePower" })
@JsonAutoDetect (fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class DriveTimeRoutine extends Routine {

	@JsonSerialize
	@JsonProperty ("time")
	private long mEndTime;
	@JsonSerialize
	@JsonProperty ("drivePower")
	private SparkDriveSignal mDrivePower;

	/**
	 * Constructs with a specified time set point and velocity
	 *
	 * @param time       How long to drive (seconds)
	 * @param drivePower LegacyDrive signal to output (left/right speeds -1 to 1)
	 */
	@JsonCreator
	public DriveTimeRoutine(@JsonProperty ("time") double time, @JsonProperty ("drivePower") SparkDriveSignal drivePower) {
		// Keeps the offset prepared, when routine starts, will add FPGA timestamp
		mEndTime = (long) (1000 * time);
		mDrivePower = drivePower;
	}

	@Override
	public void start() {
		mDrive.resetController();
		// mEndTime already has the desired drive time
		mEndTime += Timer.getFPGATimestamp();
	}

	// Routines just change the states of the robot set points, which the behavior
	// manager then moves the physical subsystems based on.
	@Override
	public Commands update(Commands commands) {
		commands.wantedDriveState = Drive.DriveState.OPEN_LOOP;
		commands.robotSetPoints.drivePowerSetPoint = mDrivePower;
		return commands;
	}

	@Override
	public Commands cancel(Commands commands) {
		// Logger.getInstance().logRobotThread(Level.FINE, "Cancelling");
		commands.wantedDriveState = Drive.DriveState.NEUTRAL;
		mDrive.resetController();
		mDrive.setNeutral();
		return commands;
	}

	@Override
	public boolean isFinished() {
		// Finish after the time is up
		return (Timer.getFPGATimestamp() >= mEndTime);
	}

	@Override
	public String getName() {
		return "Drive Time Routine";
	}

	@Override
	public Subsystem[] getRequiredSubsystems() {
		return new Subsystem[] { mDrive };
	}
}