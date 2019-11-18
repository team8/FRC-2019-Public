package com.palyrobotics.frc2019.behavior.routines.fingers;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.subsystems.Fingers;
import com.palyrobotics.frc2019.subsystems.Subsystem;

import edu.wpi.first.wpilibj.Timer;

public class FingersExpelRoutine extends Routine {

	private double mTimeout, mStartTime;

	public FingersExpelRoutine(double timeout) {
		mTimeout = timeout;
	}

	@Override
	public void start() {
		mStartTime = Timer.getFPGATimestamp();
	}

	@Override
	public Commands update(Commands commands) {
		commands.wantedFingersExpelState = Fingers.PushingState.EXPELLING;
		return commands;
	}

	@Override
	public Commands cancel(Commands commands) {
		commands.wantedFingersExpelState = Fingers.PushingState.CLOSED;
		return commands;
	}

	@Override
	public boolean isFinished() {
		return Timer.getFPGATimestamp() - mStartTime > mTimeout;
	}

	@Override
	public Subsystem[] getRequiredSubsystems() {
		return new Subsystem[] { mFingers };
	}

	@Override
	public String getName() {
		return "FingersExpelRoutine";
	}
}