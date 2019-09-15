package com.palyrobotics.frc2019.behavior.routines.drive;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.subsystems.Drive;
import com.palyrobotics.frc2019.subsystems.Subsystem;
import com.palyrobotics.frc2019.vision.Limelight;
import com.palyrobotics.frc2019.vision.LimelightControlMode;

public class BBTurnAngleRoutineRequireVision extends Routine {

	@Override
	public Subsystem[] getRequiredSubsystems() {
		return new Subsystem[] { drive };
	}

	private double mAngle;

	private GyroBBState mState = GyroBBState.START;
	private double startTime;

	private enum GyroBBState {
		START, TURNING, TIMED_OUT, DONE
	}

	public BBTurnAngleRoutineRequireVision(double angle) {
		this.mAngle = angle;
	}

	@Override
	public void start() {
		drive.setNeutral();
		mState = GyroBBState.START;
		startTime = System.currentTimeMillis();
		Limelight.getInstance().setCamMode(LimelightControlMode.CamMode.VISION);
		Limelight.getInstance().setLEDMode(LimelightControlMode.LedMode.FORCE_ON); // Limelight LED on
	}

	@Override
	public Commands update(Commands commands) {
		if(mState != GyroBBState.TIMED_OUT && (System.currentTimeMillis() - startTime > 5000)) {
//			Logger.getInstance().logRobotThread(Level.WARNING, "Timed Out!");
			mState = GyroBBState.TIMED_OUT;
		}
		switch(mState) {
			case START:
//				Logger.getInstance().logRobotThread(Level.FINE, "Set setpoint", mAngle);
				drive.setTurnAngleSetpoint(mAngle);
				commands.wantedDriveState = Drive.DriveState.ON_BOARD_CONTROLLER;
				mState = GyroBBState.TURNING;
				break;
			case TURNING:
				if(drive.controllerOnTarget()) {
					mState = GyroBBState.DONE;
				}
				break;
			case TIMED_OUT:
				drive.setNeutral();
				break;
			case DONE:
				drive.resetController();
				break;
		}

		return commands;
	}

	@Override
	public Commands cancel(Commands commands) {
		mState = GyroBBState.DONE;
		commands.wantedDriveState = Drive.DriveState.NEUTRAL;
		drive.setNeutral();
		return commands;
	}

	@Override
	public boolean finished() {
		System.out.println(Limelight.getInstance().isTargetFound());
		return mState == GyroBBState.DONE || ((System.currentTimeMillis() - startTime > 450) && Limelight.getInstance().isTargetFound());
	}

	@Override
	public String getName() {
		return "BangBangGyroTurnAngleRoutine";
	}

}
