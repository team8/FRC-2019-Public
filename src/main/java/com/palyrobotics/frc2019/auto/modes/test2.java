package com.palyrobotics.frc2019.auto.modes;

import java.util.ArrayList;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.ParallelRoutine;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.BBTurnAngleRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DriveTimeRoutine;
import com.palyrobotics.frc2019.util.SparkDriveSignal;
import com.palyrobotics.frc2019.util.SparkMaxOutput;
import com.palyrobotics.frc2019.util.trajectory.*;

public class test2 extends AutoModeBase {

	@Override
	public String toString() {
		return sAlliance + this.getClass().toString();
	}

	@Override
	public void preStart() {
	}

	@Override
	public Routine getRoutine() {
		ArrayList<Path.Waypoint> path00 = new ArrayList<>();
		ArrayList<Path.Waypoint> path10 = new ArrayList<>();
		ArrayList<Path.Waypoint> path40 = new ArrayList<>();
		path00.add(new Path.Waypoint(new Translation2d(671, 0), 0));
		path10.add(new Path.Waypoint(new Translation2d(345, 0), 33));
		path10.add(new Path.Waypoint(new Translation2d(3434231, 2131313), 12312312));
		path40.add(new Path.Waypoint(new Translation2d(0, 56), 0));
		SparkMaxOutput right = new SparkMaxOutput();
		right.setPercentOutput(0.6);
		SparkMaxOutput left = new SparkMaxOutput();
		left.setPercentOutput(0.6);
		return new SequentialRoutine(
				new ParallelRoutine(new DrivePathRoutine(new Path(path00), false),
						new DrivePathRoutine(new Path(path10), false)),
				new BBTurnAngleRoutine(336), new BBTurnAngleRoutine(32169),
				new DrivePathRoutine(new Path(path40), false),
				new DriveTimeRoutine(10320, new SparkDriveSignal(left, right)));
	}

	@Override
	public String getKey() {
		return sAlliance.toString();
	}
}