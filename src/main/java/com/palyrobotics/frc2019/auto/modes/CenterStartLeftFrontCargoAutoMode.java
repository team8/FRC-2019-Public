package com.palyrobotics.frc2019.auto.modes;

import java.util.ArrayList;
import java.util.List;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import com.palyrobotics.frc2019.config.constants.PhysicalConstants;
import com.palyrobotics.frc2019.util.trajectory.Path;
import com.palyrobotics.frc2019.util.trajectory.Path.Waypoint;
import com.palyrobotics.frc2019.util.trajectory.Translation2d;

@SuppressWarnings ("Duplicates")

public class CenterStartLeftFrontCargoAutoMode extends AutoModeBase {

//	final int kRunSpeed = 50;
//
//	final double kOffsetX = -PhysicalConstants.kLowerPlatformLength - PhysicalConstants.kRobotLengthInches * 0.6;
//	final double kOffsetY = 0; // starts at center so the offset is 0
//	final double kCargoShipLeftFrontX = sDistances.level1CargoX + PhysicalConstants.kLowerPlatformLength
//			+ PhysicalConstants.kUpperPlatformLength;
//	final double kCargoShipLeftFrontY = sDistances.fieldWidth * .5 - (sDistances.cargoLeftY + sDistances.cargoOffsetY);
//	final double kHabLineX = PhysicalConstants.kUpperPlatformLength + PhysicalConstants.kLowerPlatformLength;

	@Override
	public String toString() {
		return sAlliance + this.getClass().toString();
	}

	@Override
	public void preStart() {

	}

	@Override
	public Routine getRoutine() {

		ArrayList<Routine> routines = new ArrayList<>();
		List<Path.Waypoint> StartToCargoShip = new ArrayList<>();
		StartToCargoShip.add(new Waypoint(new Translation2d(0, 0), 50));
		StartToCargoShip.add(new Waypoint(new Translation2d(100, 0), 50));
		StartToCargoShip.add(new Waypoint(new Translation2d(1000, 0), 0));
		routines.add(new DrivePathRoutine(new Path(StartToCargoShip), false));


		return new SequentialRoutine(routines);
	}

//	public Routine placeHatch() {
//		ArrayList<Routine> routines = new ArrayList<>();

		// rezero
		// routines.add(new ReZeroSubAutoMode().ReZero(false));

		// List<Path.Waypoint> StartToCargoShip = new ArrayList<>();
		// StartToCargoShip.add(new Waypoint(
		// new Translation2d(kHabLineX + PhysicalConstants.kRobotLengthInches +
		// kOffsetX, 0), 50));
		// StartToCargoShip.add(new Waypoint(
		// new Translation2d(kCargoShipLeftFrontX * .6 + kOffsetX, kCargoShipLeftFrontY
		// + kOffsetY), 50));
		// StartToCargoShip.add(new Waypoint(
		// new Translation2d(kCargoShipLeftFrontX - PhysicalConstants.kRobotLengthInches
		// * .6 + kOffsetX,
		// kCargoShipLeftFrontY + kOffsetY),
		// 0));
		//
		// // move elevator up while driving
		// routines.add(new ParallelRoutine(new DrivePathRoutine(new
		// Path(StartToCargoShip), false),
		// new ElevatorCustomPositioningRoutine(OtherConstants.kCargoHatchTargetHeight,
		// 1)));
		//
		// // place hatch on cargo ship
		// routines.add(new FingersCycleRoutine(1));

//		List<Path.Waypoint> StartToCargoShip = new ArrayList<>();
//		StartToCargoShip.add(new Waypoint(new Translation2d(0, 0), 50));
//		StartToCargoShip.add(new Waypoint(new Translation2d(100, 0), 50));
//		StartToCargoShip.add(new Waypoint(new Translation2d(1000, 0), 0));
//		routines.add(new DrivePathRoutine(new Path(StartToCargoShip), false));
//
//		return new SequentialRoutine(routines);
//	}

	@Override
	public String getKey() {
		return sAlliance.toString();
	}
}
