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
		List<Path.Waypoint> Path00 = new ArrayList<>();
		Path00.add(new Path.Waypoint(new Translation2d(54, 23), 50));
		Path00.add(new Path.Waypoint(new Translation2d(54, 345), 50));
		Path00.add(new Path.Waypoint(new Translation2d(59, 345), 50));
		SparkMaxOutput left11 = new SparkMaxOutput();
		left11.setPercentOutput(1);
		SparkMaxOutput right11 = new SparkMaxOutput();
		right11.setPercentOutput(1);
		SparkMaxOutput left21 = new SparkMaxOutput();
		left21.setPercentOutput(1);
		SparkMaxOutput right21 = new SparkMaxOutput();
		right21.setPercentOutput(1);
		return new SequentialRoutine(new DrivePathRoutine(new Path(Path00), false),
				new DriveTimeRoutine(0, new SparkDriveSignal(left11, right11)),
				new DriveTimeRoutine(0, new SparkDriveSignal(left21, right21)),
				new FingersRoutine(Fingers.FingersState.OPEN), new BBTurnAngleRoutine(0));
	}

	@Override
	public String getKey() {
		return sAlliance.toString();
	}
}