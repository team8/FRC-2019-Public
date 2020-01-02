package com.palyrobotics.frc2019.auto.modes;

import java.util.ArrayList;
import java.util.List;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.BBTurnAngleRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DriveTimeRoutine;
import com.palyrobotics.frc2019.util.SparkDriveSignal;
import com.palyrobotics.frc2019.util.SparkMaxOutput;
import com.palyrobotics.frc2019.util.trajectory.*;

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
		List<Path.Waypoint> path30 = new ArrayList<>();
		path30.add(new Path.Waypoint(new Translation2d(0.0, 56.0), 0.0));
		SparkMaxOutput left41 = new SparkMaxOutput();
		left41.setPercentOutput(0.6);
		SparkMaxOutput right41 = new SparkMaxOutput();
		right41.setPercentOutput(0.6);
		return new SequentialRoutine(new BBTurnAngleRoutine(36.0), new BBTurnAngleRoutine(369.0),
				new DrivePathRoutine(new Path(path30), false),
				new DriveTimeRoutine(100000, new SparkDriveSignal(left41, right41)));
	}

	@Override
	public String getKey() {
		return sAlliance.toString();
	}
}