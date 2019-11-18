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

public class CenterStartRightFrontCargoAutoMode extends AutoModeBase {

	public static int kRunSpeed = 50; // speed can be faster
	public static double kOffsetX = -PhysicalConstants.kLowerPlatformLength
			- PhysicalConstants.kRobotLengthInches * 0.6;
	public static double kOffsetY = 0; // starts at center so the offset is 0
	public static double kCargoShipRightFrontX = sDistances.level1CargoX + PhysicalConstants.kLowerPlatformLength
			+ PhysicalConstants.kUpperPlatformLength;
	public static double kCargoShipRightFrontY = -(sDistances.fieldWidth * .5
			- (sDistances.cargoRightY + sDistances.cargoOffsetY));
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
				new Translation2d(kHabLineX + PhysicalConstants.kRobotLengthInches + kOffsetX, 0), kRunSpeed)); // go
																												// straight
																												// so
																												// the
																												// robot
																												// doesn't
																												// get
																												// messed
																												// up
																												// going
																												// down
																												// a
																												// level
		StartToCargoShip.add(new Waypoint(
				new Translation2d(kCargoShipRightFrontX * .6 + kOffsetX, kCargoShipRightFrontY + kOffsetY), kRunSpeed)); // lines
																															// up
																															// with
																															// cargo
																															// ship
		StartToCargoShip.add(new Waypoint(
				new Translation2d(kCargoShipRightFrontX - PhysicalConstants.kRobotLengthInches * .6 + kOffsetX,
						kCargoShipRightFrontY + kOffsetY),
				0));
		routines.add(new DrivePathRoutine(new Path(StartToCargoShip), true));

		// move elevator up while driving
		routines.add(new ParallelRoutine(new DrivePathRoutine(new Path(StartToCargoShip), false),
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
