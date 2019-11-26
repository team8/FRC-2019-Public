package com.palyrobotics.frc2019.behavior.routines.vision;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.config.VisionConfig;
import com.palyrobotics.frc2019.subsystems.Intake;
import com.palyrobotics.frc2019.subsystems.Subsystem;
import com.palyrobotics.frc2019.util.SparkDriveSignal;
import com.palyrobotics.frc2019.util.config.Configs;
import com.palyrobotics.frc2019.util.csvlogger.CSVWriter;

public class CargoAutoAlign extends Routine {

	private SparkDriveSignal motor = new SparkDriveSignal(); // TODO Change to Lazy Spark Max and add forward movement
																// once aligned until cargo has been picked up

	@Override
	public void start() {

	}

	@Override
	public Commands update(Commands commands) {
		double pLoop = Configs.get(VisionConfig.class).autoAlignGains.f
				* (getError() * Configs.get(VisionConfig.class).autoAlignGains.p);
		if (Math.abs(pLoop) < 50) {
			System.out.println("Completely aligned");
			commands.wantedIntakeState = Intake.IntakeMacroState.DOWN_FOR_GROUND_INTAKE;
		} else if (pLoop < 0) {
			System.out.println("Turning left");
			// motor.leftOutput.setPercentOutput(pLoop);
			// motor.rightOutput.setPercentOutput(-pLoop);
		} else if (pLoop > 0) {
			System.out.println("Turning right");
			// motor.leftOutput.setPercentOutput(pLoop);
			// motor.rightOutput.setPercentOutput(-pLoop);
		}
		CSVWriter.addData("AutoAlignMotorOutput", pLoop);
		return commands;
	}

	@Override
	public Commands cancel(Commands commands) {
		return commands;
	}

	@Override
	public boolean isFinished() {
		return false;
	}

	@Override
	public Subsystem[] getRequiredSubsystems() {
		return new Subsystem[0];
	}

	@Override
	public String getName() {
		return "CargoAutoAlign";
	}

	/**
	 * Gets error from odroid sent UDP packet.
	 */
	public static int getError() {
		String errorStr = RobotState.getInstance().commandReceiver.executeCommand("get");
		return Integer.parseInt(errorStr);
	}
}
