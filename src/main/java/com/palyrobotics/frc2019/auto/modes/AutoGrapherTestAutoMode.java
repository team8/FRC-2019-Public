package com.palyrobotics.frc2019.auto.modes;

import java.util.ArrayList;
import java.util.List;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import com.palyrobotics.frc2019.util.trajectory.Path;
import com.palyrobotics.frc2019.util.trajectory.Translation2d;

public class AutoGrapherTestAutoMode extends AutoModeBase {

	@Override
	public String toString() {
		return null;
	}

	@Override
	public void preStart() {

	}

	@Override
	public Routine getRoutine() {

		ArrayList<Routine> routines = new ArrayList<>();
		List<Path.Waypoint> StartToCargoShip = new ArrayList<>();
		StartToCargoShip.add(new Path.Waypoint(new Translation2d(0, 0), 50));
		StartToCargoShip.add(new Path.Waypoint(new Translation2d(100, 0), 50));
		StartToCargoShip.add(new Path.Waypoint(new Translation2d(1000, 0), 0));
		routines.add(new DrivePathRoutine(new Path(StartToCargoShip), false));

		return new SequentialRoutine(routines);
	}

	@Override
	public String getKey() {
		return null;
	}
}
