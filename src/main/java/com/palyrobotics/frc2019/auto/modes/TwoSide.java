package com.palyrobotics.frc2019.auto.modes;

import java.util.ArrayList;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.*;
import com.palyrobotics.frc2019.config.constants.PhysicalConstants;
import com.palyrobotics.frc2019.util.trajectory.Path;
import com.palyrobotics.frc2019.util.trajectory.Path.Waypoint;
import com.palyrobotics.frc2019.util.trajectory.Translation2d;

@SuppressWarnings ("Duplicates")

public class TwoSide extends AutoModeBase {
	// right start > rocket ship close > loading station > rocket ship far > depot >
	// rocket ship mid

	public static int kRunSpeed = 60;
	public static double kOffsetX = 10;
	public static double kOffsetY = PhysicalConstants.kLevel3Width * .5 + PhysicalConstants.kLevel2Width * .5;
	public static double kCargoShipRightFrontX = sDistances.level1CargoX + PhysicalConstants.kLowerPlatformLength
			+ PhysicalConstants.kUpperPlatformLength;
	public static double kCargoShipRightFrontY = -(sDistances.fieldWidth * .5
			- (sDistances.cargoRightY + sDistances.cargoOffsetY));
	public static double kHabLineX = PhysicalConstants.kUpperPlatformLength + PhysicalConstants.kLowerPlatformLength;
	public static double kRightLoadingStationX = 0;
	public static double kRightLoadingStationY = -(sDistances.fieldWidth * .5 - sDistances.rightLoadingY);
	public static double kRightDepotX = PhysicalConstants.kUpperPlatformLength;
	public static double kRightDepotY = -(sDistances.fieldWidth * .5 - sDistances.depotFromRightY);
	public static double kRightRocketShipCloseX = sDistances.habRightRocketCloseX + kHabLineX;
	public static double kRightRocketShipCloseY = -(sDistances.fieldWidth * .5 - sDistances.rightRocketCloseY);
	public static double kRightRocketShipMidX = kHabLineX + sDistances.habRightRocketMidX;
	public static double kRightRocketShipMidY = -(sDistances.fieldWidth * .5 - sDistances.rightRocketMidY);
	public static double kRightRocketShipFarX = sDistances.fieldWidth - sDistances.midLineRightRocketFarX;
	public static double kRightRocketShipFarY = -(sDistances.fieldWidth * .5 - sDistances.rightRocketFarY);
	public static double kRightFirstCargoShipX = kCargoShipRightFrontX + sDistances.cargoOffsetY;
	public static double kRightFirstCargoShipY = sDistances.fieldWidth * .5 - sDistances.cargoRightY;

	public Translation2d kCargoShipRightFront = new Translation2d(
			kCargoShipRightFrontX + PhysicalConstants.kRobotWidthInches * .2 + kOffsetX,
			kCargoShipRightFrontY + PhysicalConstants.kRobotLengthInches * .05 + kOffsetY);
	public Translation2d kRightLoadingStation = new Translation2d(
			kRightLoadingStationX + PhysicalConstants.kRobotLengthInches + kOffsetX,
			kRightLoadingStationY + PhysicalConstants.kRobotLengthInches * .2 + kOffsetY);
	public Translation2d kRightRocketShipFar = new Translation2d(
			kRightRocketShipFarX + PhysicalConstants.kRobotLengthInches * 1 + kOffsetX,
			kRightRocketShipFarY + PhysicalConstants.kRobotLengthInches * .05 + kOffsetY);
	public Translation2d kRightDepot = new Translation2d(
			kRightDepotX + PhysicalConstants.kRobotLengthInches * 1.1 + kOffsetX,
			kRightDepotY + PhysicalConstants.kRobotLengthInches * .25 + kOffsetY);
	public Translation2d kRightRocketShipClose = new Translation2d(
			kRightRocketShipCloseX + PhysicalConstants.kRobotLengthInches * .3 + kOffsetX,
			kRightRocketShipCloseY + PhysicalConstants.kRobotLengthInches * .5 + kOffsetY);
	public Translation2d kRightRocketShipMid = new Translation2d(kRightRocketShipMidX + kOffsetX,
			kRightRocketShipMidY + kOffsetY);

	@Override
	public String toString() {
		return sAlliance + this.getClass().toString();
	}

	@Override
	public void preStart() {

	}

	@Override
	public Routine getRoutine() {
		return new SequentialRoutine(new DriveSensorResetRoutine(1), placeHatch1(), takeHatch(), placeHatch2());
	}

	public Routine placeHatch1() {
		ArrayList<Routine> routines = new ArrayList<>();

		ArrayList<Waypoint> StartToCargoShip = new ArrayList<>();
		StartToCargoShip.add(new Waypoint(new Translation2d(kHabLineX + kOffsetX, 0), kRunSpeed));
		StartToCargoShip.add(new Waypoint(
				new Translation2d(kRightFirstCargoShipX + PhysicalConstants.kRobotLengthInches * .55 + kOffsetX,
						0 + kOffsetY),
				kRunSpeed));
		StartToCargoShip.add(new Waypoint(
				new Translation2d(kRightFirstCargoShipX + PhysicalConstants.kRobotLengthInches * .85 + kOffsetX,
						kRightFirstCargoShipY - PhysicalConstants.kRobotLengthInches + kOffsetY),
				kRunSpeed, "visionStart")); // line up in front of cargo bay
		StartToCargoShip.add(new Waypoint(
				new Translation2d(kRightFirstCargoShipX + PhysicalConstants.kRobotLengthInches * .85 + kOffsetX,
						kRightFirstCargoShipY - PhysicalConstants.kRobotLengthInches * .2 + kOffsetY),
				0));

		routines.add(new VisionAssistedDrivePathRoutine(StartToCargoShip, false, false, "visionStart"));

		return new SequentialRoutine(routines);

	}

	public Routine takeHatch() { // cargo ship front to loading station
		ArrayList<Routine> routines = new ArrayList<>();

		ArrayList<Waypoint> BackCargoShipToLoadingStation = new ArrayList<>();
		BackCargoShipToLoadingStation.add(new Waypoint(
				new Translation2d(kRightFirstCargoShipX + PhysicalConstants.kRobotLengthInches * .85 + kOffsetX,
						kRightFirstCargoShipY - PhysicalConstants.kRobotLengthInches + kOffsetY),
				kRunSpeed)); // backs out of the cargo ship
		BackCargoShipToLoadingStation.add(new Waypoint(
				new Translation2d(kRightFirstCargoShipX * .5 + PhysicalConstants.kRobotLengthInches + kOffsetX,
						kRightLoadingStationY * .5 + kOffsetY),
				kRunSpeed));
		BackCargoShipToLoadingStation.add(new Waypoint(
				new Translation2d(kRightFirstCargoShipX * .5 + kOffsetX, kRightLoadingStationY + kOffsetY), 0));
		routines.add(new DrivePathRoutine(new Path(BackCargoShipToLoadingStation), true));

		// turn toward the loading station
		routines.add(new CascadingGyroEncoderTurnAngleRoutine(90));

		ArrayList<Waypoint> ForwardCargoShipToLoadingStation = new ArrayList<>();
		ForwardCargoShipToLoadingStation
				.add(new Waypoint(
						new Translation2d(kHabLineX - PhysicalConstants.kRobotLengthInches * .5 + kOffsetX,
								kRightLoadingStationY + PhysicalConstants.kRobotWidthInches * .4 + kOffsetY),
						kRunSpeed));
		ForwardCargoShipToLoadingStation.add(new Waypoint(
				kRightLoadingStation.translateBy(new Translation2d(PhysicalConstants.kRobotLengthInches, 0)), kRunSpeed,
				"visionStart"));
		ForwardCargoShipToLoadingStation.add(new Waypoint(kRightLoadingStation, 0));

		routines.add(new VisionAssistedDrivePathRoutine(ForwardCargoShipToLoadingStation, false, false, "visionStart"));

		return new SequentialRoutine(routines);
	}

	public Routine placeHatch2() {
		ArrayList<Routine> routines = new ArrayList<>();

		routines.add(new DriveSensorResetRoutine(1));

		ArrayList<Waypoint> BackLoadingStationToCargoShip = new ArrayList<>();
		BackLoadingStationToCargoShip.add(new Waypoint(new Translation2d(0, 0), kRunSpeed));
		BackLoadingStationToCargoShip.add(new Waypoint(new Translation2d(-kHabLineX, 0), kRunSpeed));
		BackLoadingStationToCargoShip.add(new Waypoint(new Translation2d(-kHabLineX, 0), kRunSpeed));
		BackLoadingStationToCargoShip
				.add(new Waypoint(new Translation2d(-kRightFirstCargoShipX * 0.8, -40), kRunSpeed));
		BackLoadingStationToCargoShip.add(new Waypoint(
				new Translation2d(-kRightFirstCargoShipX - PhysicalConstants.kRobotLengthInches * 0.5, -40)));

		routines.add(new DrivePathRoutine(new Path(BackLoadingStationToCargoShip), true));

		routines.add(new BBTurnAngleRoutine(-90));

		routines.add(new DriveSensorResetRoutine(1));

		ArrayList<Waypoint> ForwardLoadingStationToCargoShip = new ArrayList<>();
		ForwardLoadingStationToCargoShip.add(new Waypoint(new Translation2d(0, 0), kRunSpeed, "visionStart")); // line
																												// up in
																												// front
																												// of
																												// cargo
																												// bay
		ForwardLoadingStationToCargoShip.add(new Waypoint(new Translation2d(0, 50), 0));

		routines.add(new VisionAssistedDrivePathRoutine(ForwardLoadingStationToCargoShip, false, false, "visionStart"));

		return new SequentialRoutine(routines);
	}

	@Override
	public String getKey() {
		return sAlliance.toString();
	}
}