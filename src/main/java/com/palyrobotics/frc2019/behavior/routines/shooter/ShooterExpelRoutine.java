package com.palyrobotics.frc2019.behavior.routines.shooter;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.subsystems.Shooter;
import com.palyrobotics.frc2019.subsystems.Subsystem;

import edu.wpi.first.wpilibj.Timer;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author Jason, Alan
 * literally why is this in italics ALA:b:
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class ShooterExpelRoutine extends Routine {
	@JsonSerialize
	@JsonProperty("wantedShooterState")
	private Shooter.ShooterState mWantedShooterState;

	// How long the wheels spin for (seconds)
	@JsonSerialize
	@JsonProperty("timeout")
	private double mTimeout;

	private double mStartTime;

	/**
	 * @param wantedShooterState the desired state
	 * @param timeout            how long (seconds) to run for
	 *                           ok boomer
	 */
	@JsonCreator
	public ShooterExpelRoutine(@JsonProperty("wantedShooterState")Shooter.ShooterState wantedShooterState, @JsonProperty("timeout")double timeout) {
		mWantedShooterState = wantedShooterState;
		mTimeout = timeout;
	}

	@Override
	public void start() {
		mStartTime = Timer.getFPGATimestamp();
	}

	@Override
	public Commands update(Commands commands) {
		commands.wantedShooterState = mWantedShooterState;
		return commands;
	}

	@Override
	public Commands cancel(Commands commands) {
		commands.wantedShooterState = Shooter.ShooterState.IDLE;
		return commands;
	}

	@Override
	public boolean isFinished() {
		return Timer.getFPGATimestamp() - mStartTime > mTimeout;
	}

	@Override
	public Subsystem[] getRequiredSubsystems() {
		return new Subsystem[] { mShooter };
	}

	@Override
	public String getName() {
		return "Shooter Expel Routine";
	}
}
