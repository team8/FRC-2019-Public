package com.palyrobotics.frc2019.auto.modes;
import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.ParallelRoutine;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import java.util.ArrayList;
import java.util.List;

import com.palyrobotics.frc2019.subsystems.Shooter;
import com.palyrobotics.frc2019.util.trajectory.*;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import java.util.ArrayList;
import java.util.List;
import com.palyrobotics.frc2019.util.trajectory.*;
import com.palyrobotics.frc2019.behavior.routines.drive.BBTurnAngleRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.BBTurnAngleRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import java.util.ArrayList;
import java.util.List;
import com.palyrobotics.frc2019.util.trajectory.*;
import com.palyrobotics.frc2019.behavior.routines.drive.DriveTimeRoutine;
import com.palyrobotics.frc2019.util.SparkDriveSignal;
import com.palyrobotics.frc2019.util.SparkMaxOutput;
import com.palyrobotics.frc2019.behavior.routines.shooter.ShooterExpelRoutine;

public class test1 extends AutoModeBase{
@Override
	public String toString() {
		return sAlliance + this.getClass().toString();
	}
	@Override
	public void preStart() {}

	@Override
	public Routine getRoutine() {
List<Path.Waypoint> path000 = new ArrayList<>();
path000.add(new Path.Waypoint(new Translation2d(51,0),0));
path000.add(new Path.Waypoint(new Translation2d(51,0),0));
path000.add(new Path.Waypoint(new Translation2d(51,0),0));
path000.add(new Path.Waypoint(new Translation2d(51,0),0));
path000.add(new Path.Waypoint(new Translation2d(51,0),0));
path000.add(new Path.Waypoint(new Translation2d(51,0),0));
path000.add(new Path.Waypoint(new Translation2d(51,0),0));
path000.add(new Path.Waypoint(new Translation2d(51,0),0));
path000.add(new Path.Waypoint(new Translation2d(51,0),0));
path000.add(new Path.Waypoint(new Translation2d(51,0),0));
List<Path.Waypoint> path010 = new ArrayList<>();
path010.add(new Path.Waypoint(new Translation2d(0.0,0.0),0.0));
List<Path.Waypoint> path30 = new ArrayList<>();
path30.add(new Path.Waypoint(new Translation2d(0.0,56.0),0.0));
SparkMaxOutput left41 = new SparkMaxOutput();
left41.setPercentOutput(0.6);
SparkMaxOutput right41 = new SparkMaxOutput();
right41.setPercentOutput(0.6);
return new SequentialRoutine(new ParallelRoutine(new DrivePathRoutine(new Path(path000)
,false),new DrivePathRoutine(new Path(path010)
,false)),new BBTurnAngleRoutine(36.0),new BBTurnAngleRoutine(369.0),new DrivePathRoutine(new Path(path30)
,false),new DriveTimeRoutine(100000000,new SparkDriveSignal(left41, right41)
),new ShooterExpelRoutine(Shooter.ShooterState.IDLE,100.0));}	@Override
	public String getKey() {		return sAlliance.toString();	}
}