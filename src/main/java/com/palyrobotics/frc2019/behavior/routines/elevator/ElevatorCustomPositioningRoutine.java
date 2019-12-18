package com.palyrobotics.frc2019.behavior.routines.elevator;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.subsystems.Elevator;
import com.palyrobotics.frc2019.subsystems.Subsystem;

import edu.wpi.first.wpilibj.Timer;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class ElevatorCustomPositioningRoutine extends Routine {
	@JsonSerialize
	@JsonProperty("position")
	private double mPosition;
	@JsonSerialize
	@JsonProperty("timeout")
	private double mTimeout;
	private double mStartTime;
	private boolean mHasSetAllCommands;


	@JsonCreator
	public ElevatorCustomPositioningRoutine(@JsonProperty("position")double position, @JsonProperty("double timeout")double timeout) {
		mPosition = position;
		mTimeout = timeout;
	}

	@Override
	public void start() {
		mStartTime = Timer.getFPGATimestamp();
	}

	@Override
	public Commands update(Commands commands) {
		mHasSetAllCommands = true;
		commands.wantedElevatorState = Elevator.ElevatorState.CUSTOM_POSITIONING;
		commands.robotSetPoints.elevatorPositionSetPoint = mPosition;
		return commands;
	}

	@Override
	public Commands cancel(Commands commands) {
		commands.wantedElevatorState = Elevator.ElevatorState.CUSTOM_POSITIONING;
		return commands;
	}

	@Override
	public boolean isFinished() {
		if (Timer.getFPGATimestamp() - mStartTime > mTimeout) {
			return true;
		}
		return mHasSetAllCommands && mElevator.elevatorOnTarget();
	}

	@Override
	public Subsystem[] getRequiredSubsystems() {
		return new Subsystem[] { mElevator };
	}

	@Override
	public String getName() {
		return "Elevator Custom Positioning Routine";
	}
}
