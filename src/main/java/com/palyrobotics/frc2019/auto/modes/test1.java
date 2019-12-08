package com.palyrobotics.frc2019.auto.modes;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.TimeoutRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DriveTimeRoutine;
import com.palyrobotics.frc2019.util.SparkDriveSignal;
import com.palyrobotics.frc2019.util.SparkMaxOutput;

public class test1 extends AutoModeBase {

	@Override
	public String toString() {
		return sAlliance + this.getClass().toString();
	}

	@Override
	public void preStart() {
	}

	@Override
	public Routine getRoutine() {
		SparkMaxOutput left21 = new SparkMaxOutput();
		SparkMaxOutput right21 = new SparkMaxOutput();
		left21.setPercentOutput(0.35982657857023914);
		right21.setPercentOutput(1.0);
		return new SequentialRoutine(new TimeoutRoutine(230), new TimeoutRoutine(0),
				new DriveTimeRoutine(0, new SparkDriveSignal(left21, right21)));
	}

	@Override
	public String getKey() {
		return sAlliance.toString();
	}
}