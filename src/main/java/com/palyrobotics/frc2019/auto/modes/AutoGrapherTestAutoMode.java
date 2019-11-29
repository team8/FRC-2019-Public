package com.palyrobotics.frc2019.auto.modes;

import java.util.ArrayList;
import java.util.List;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import com.palyrobotics.frc2019.config.constants.PhysicalConstants;
import com.palyrobotics.frc2019.util.trajectory.Path;
import com.palyrobotics.frc2019.util.trajectory.Translation2d;

public class AutoGrapherTestAutoMode extends AutoModeBase {

	final double kOffsetX = -PhysicalConstants.kLowerPlatformLength - PhysicalConstants.kRobotLengthInches * 0.6;
	final double kOffsetY = 0; // starts at center so the offset is 0
	final double kCargoShipLeftFrontX = sDistances.level1CargoX + PhysicalConstants.kLowerPlatformLength
			+ PhysicalConstants.kUpperPlatformLength;
	final double kCargoShipLeftFrontY = sDistances.fieldWidth * .5 - (sDistances.cargoLeftY + sDistances.cargoOffsetY);
	final double kHabLineX = PhysicalConstants.kUpperPlatformLength + PhysicalConstants.kLowerPlatformLength;

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
		StartToCargoShip.add(new Path.Waypoint(new Translation2d(0, 0), 50)); // spotless test aowiejfoawiejfoiawejf
		StartToCargoShip.add(
				new Path.Waypoint(new Translation2d(kCargoShipLeftFrontX * .5 + kOffsetX, kCargoShipLeftFrontY), 50));
		StartToCargoShip
				.add(new Path.Waypoint(new Translation2d(kCargoShipLeftFrontX + kOffsetX, kCargoShipLeftFrontY), 0)); // test
		routines.add(new DrivePathRoutine(new Path(StartToCargoShip), false));

		return new SequentialRoutine(routines);
	}

	@Override
	public String getKey() {
		return null;
	}
}
