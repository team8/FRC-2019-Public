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
		List<Path.Waypoint> path10 = new ArrayList<>();
		path10.add(new Path.Waypoint(new Translation2d(0, 0), 50));
		path10.add(new Path.Waypoint(new Translation2d(30, 0), 100));
		SparkMaxOutput left211 = new SparkMaxOutput();
		left211.setPercentOutput(0.3);
		SparkMaxOutput right211 = new SparkMaxOutput();
		right211.setPercentOutput(0.3);
		return new SequentialRoutine(new ParallelRoutine(new ShooterExpelRoutine(Shooter.ShooterState.SPIN_UP, 2),
				new DriveTimeRoutine(2, new SparkDriveSignal(left211, right211))), new DrivePathRoutine(new Path(path10), false)
				);
		//return new SequentialRoutine(new ParallelRoutine(new ShooterExpelRoutine(Shooter.ShooterState.SPIN_UP, 2), new DriveTimeRoutine(2, new SparkDriveSignal(left211, right211))));
	}

	@Override
	public String getKey() {
		return sAlliance.toString();
	}
}