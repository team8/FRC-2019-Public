package com.palyrobotics.frc2019.auto.modes;

import java.util.ArrayList;
import java.util.List;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.ParallelRoutine;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.BBTurnAngleRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DriveTimeRoutine;
import com.palyrobotics.frc2019.behavior.routines.shooter.ShooterExpelRoutine;
import com.palyrobotics.frc2019.subsystems.Shooter;
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
		List<Path.Waypoint> path000 = new ArrayList<>();
		path000.add(new Path.Waypoint(new Translation2d(5132, 0), 0));
		path000.add(new Path.Waypoint(new Translation2d(5145, 0), 0));
		path000.add(new Path.Waypoint(new Translation2d(54, 0), 0));
		path000.add(new Path.Waypoint(new Translation2d(51, 0), 0));
		path000.add(new Path.Waypoint(new Translation2d(0, 43), 0));
		path000.add(new Path.Waypoint(new Translation2d(5132, 0), 0));
		List<Path.Waypoint> path010 = new ArrayList<>();
		path010.add(new Path.Waypoint(new Translation2d(0, 0), 0));
		List<Path.Waypoint> path020 = new ArrayList<>();
		path020.add(new Path.Waypoint(new Translation2d(54, 0), 0));
		path020.add(new Path.Waypoint(new Translation2d(32, 0), 0));
		List<Path.Waypoint> path30 = new ArrayList<>();
		path30.add(new Path.Waypoint(new Translation2d(0, 5643), 0));
		SparkMaxOutput left41 = new SparkMaxOutput();
		left41.setPercentOutput(0.6);
		SparkMaxOutput right41 = new SparkMaxOutput();
		right41.setPercentOutput(0.6);
		return new SequentialRoutine(
				new ParallelRoutine(new DrivePathRoutine(new Path(path000), false),
						new DrivePathRoutine(new Path(path010), false), new DrivePathRoutine(new Path(path020), false),
						new BBTurnAngleRoutine(0.0)),
				new BBTurnAngleRoutine(36), new BBTurnAngleRoutine(369), new DrivePathRoutine(new Path(path30), false),
				new DriveTimeRoutine(10, new SparkDriveSignal(left41, right41)),
				new ShooterExpelRoutine(Shooter.ShooterState.SPIN_UP, 100));
	}

	@Override
	public String getKey() {
		return sAlliance.toString();
	}
}