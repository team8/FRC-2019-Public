package com.palyrobotics.frc2019.auto.modes;

import java.util.ArrayList;
import java.util.List;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.ParallelRoutine;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import com.palyrobotics.frc2019.behavior.routines.elevator.ElevatorCustomPositioningRoutine;
import com.palyrobotics.frc2019.behavior.routines.fingers.FingersCycleRoutine;
import com.palyrobotics.frc2019.config.constants.OtherConstants;
import com.palyrobotics.frc2019.config.constants.PhysicalConstants;
import com.palyrobotics.frc2019.util.trajectory.Path;
import com.palyrobotics.frc2019.util.trajectory.Path.Waypoint;
import com.palyrobotics.frc2019.util.trajectory.Translation2d;

@SuppressWarnings ("Duplicates")

public class LeftStartLeftFrontCargoAutoMode extends AutoModeBase {

	public static int kRunSpeed = 60; // can be faster
	public static double kOffsetX = -PhysicalConstants.kLowerPlatformLength
			- PhysicalConstants.kRobotLengthInches * 0.6;
	public static double kOffsetY = -(PhysicalConstants.kLevel3Width * .5 + PhysicalConstants.kLevel2Width * .5);
	public static double kCargoShipLeftFrontX = sDistances.level1CargoX + PhysicalConstants.kLowerPlatformLength
			+ PhysicalConstants.kUpperPlatformLength;
	public static double kCargoShipLeftFrontY = sDistances.fieldWidth * .5
			- (sDistances.cargoLeftY + sDistances.cargoOffsetY);
	public static double kHabLineX = PhysicalConstants.kUpperPlatformLength + PhysicalConstants.kLowerPlatformLength;

	@Override
	public String toString() {
		return sAlliance + this.getClass().toString();
	}

	@Override
	public void preStart() {

	}

	@Override
	public Routine getRoutine() {
		return new SequentialRoutine(placeHatch());
	}

	public Routine placeHatch() {

		ArrayList<Routine> routines = new ArrayList<>();

		// rezero
		routines.add(new ReZeroSubAutoMode().ReZero(false));

		List<Path.Waypoint> StartToCargoShip = new ArrayList<>();
		StartToCargoShip.add(new Waypoint(
				new Translation2d(kHabLineX + PhysicalConstants.kRobotLengthInches + kOffsetX, 0), kRunSpeed));
		StartToCargoShip.add(new Waypoint(
				new Translation2d(kCargoShipLeftFrontX * .6 + kOffsetX, kCargoShipLeftFrontY + kOffsetY), kRunSpeed));
		StartToCargoShip.add(new Waypoint(
				new Translation2d(kCargoShipLeftFrontX - PhysicalConstants.kRobotLengthInches * 2 + kOffsetX,
						kCargoShipLeftFrontY + kOffsetY),
				0));
		routines.add(new DrivePathRoutine(new Path(StartToCargoShip), true));

		// move elevator up while driving
		routines.add(new ParallelRoutine(new DrivePathRoutine(new Path(StartToCargoShip), true),
				new ElevatorCustomPositioningRoutine(OtherConstants.kCargoHatchTargetHeight, 1)));

		// place hatch on cargo ship
		routines.add(new FingersCycleRoutine(1));

		return new SequentialRoutine(routines);
	}

	@Override
	public String getKey() {
		return sAlliance.toString();
	}
}
