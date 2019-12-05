package com.palyrobotics.frc2019.auto.modes;

import java.util.ArrayList;
import java.util.List;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.BBTurnAngleRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DriveTimeRoutine;
import com.palyrobotics.frc2019.behavior.routines.fingers.FingersRoutine;
import com.palyrobotics.frc2019.subsystems.Fingers;
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
		List<Path.Waypoint> path00 = new ArrayList<>();
		SparkMaxOutput left11 = new SparkMaxOutput();
		SparkMaxOutput right11 = new SparkMaxOutput();
		SparkMaxOutput left21 = new SparkMaxOutput();
		SparkMaxOutput right21 = new SparkMaxOutput();
		path00.add(new Path.Waypoint(new Translation2d(54, 23), 50));
		path00.add(new Path.Waypoint(new Translation2d(54, 345), 50));
		path00.add(new Path.Waypoint(new Translation2d(59, 345), 50));
		left11.setPercentOutput(1);
		right11.setPercentOutput(1);
		left21.setPercentOutput(1);
		right21.setPercentOutput(1);
		return new SequentialRoutine(new DrivePathRoutine(new Path(path00), false),
				new DriveTimeRoutine(0, new SparkDriveSignal(left11, right11)),
				new DriveTimeRoutine(0, new SparkDriveSignal(left21, right21)),
				new FingersRoutine(Fingers.FingersState.OPEN), new BBTurnAngleRoutine(0));
	}

	@Override
	public String getKey() {
		return sAlliance.toString();
	}
}