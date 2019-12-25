package com.palyrobotics.frc2019.auto.modes;

import java.util.ArrayList;
import java.util.List;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.BBTurnAngleRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
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
		List<Path.Waypoint> path00 = new ArrayList<>();
		List<Path.Waypoint> path10 = new ArrayList<>();
		List<Path.Waypoint> path40 = new ArrayList<>();
		path00.add(new Path.Waypoint(new Translation2d(51, 0), 0));
		path10.add(new Path.Waypoint(new Translation2d(0, 0), 33));
		path40.add(new Path.Waypoint(new Translation2d(0, 56), 0));
		return new SequentialRoutine(new DrivePathRoutine(new Path(path00), false),
				new DrivePathRoutine(new Path(path10), false), new BBTurnAngleRoutine(36), new BBTurnAngleRoutine(369),
				new DrivePathRoutine(new Path(path40), false));
	}

	@Override
	public String getKey() {
		return sAlliance.toString();
	}
}