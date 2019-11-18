package com.palyrobotics.frc2019.auto.modes;

import java.util.ArrayList;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.CascadingGyroEncoderTurnAngleRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DriveSensorResetRoutine;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.config.constants.PhysicalConstants;
import com.palyrobotics.frc2019.util.trajectory.Path;
import com.palyrobotics.frc2019.util.trajectory.Path.Waypoint;
import com.palyrobotics.frc2019.util.trajectory.Translation2d;

@SuppressWarnings ("Duplicates")

public class TestingRoutine extends AutoModeBase {

	public static int kRunSpeed = 110;
	public static double kOffsetX = -PhysicalConstants.kLowerPlatformLength
			- PhysicalConstants.kRobotLengthInches * 0.6;
	public static double kOffsetY = PhysicalConstants.kLevel3Width * .5 + PhysicalConstants.kLevel2Width * .5;
	public static double kCargoShipRightFrontX = sDistances.level1CargoX + PhysicalConstants.kLowerPlatformLength
			+ PhysicalConstants.kUpperPlatformLength;
	public static double kCargoShipRightFrontY = -(sDistances.fieldWidth * .5
			- (sDistances.cargoRightY + sDistances.cargoOffsetY));
	public static double kHabLineX = PhysicalConstants.kUpperPlatformLength + PhysicalConstants.kLowerPlatformLength;
	public static double kRightLoadingStationX = 0;
	public static double kRightLoadingStationY = -(sDistances.fieldWidth * .5 - sDistances.rightLoadingY);
	public RobotState robotState = RobotState.getInstance();

	@Override
	public String toString() {
		return sAlliance + this.getClass().toString();
	}

	@Override
	public void preStart() {

	}

	@Override
	public Routine getRoutine() {
		return new SequentialRoutine(new DriveSensorResetRoutine(1), takeHatch());
	}

	public Routine takeHatch() { // cargo ship front to loading station
		ArrayList<Routine> routines = new ArrayList<>();

		RobotState robotState = RobotState.getInstance();

		ArrayList<Waypoint> BackCargoShipToLoadingStation = new ArrayList<>();

		BackCargoShipToLoadingStation
				.add(new Waypoint(robotState.getLatestFieldToVehicle().getValue().getTranslation(), kRunSpeed));

		// back up
		BackCargoShipToLoadingStation.add(new Waypoint(
				robotState.getLatestFieldToVehicle().getValue().getTranslation().translateBy(new Translation2d(-50, 0)),
				kRunSpeed));

		BackCargoShipToLoadingStation.add(new Waypoint(robotState.getLatestFieldToVehicle().getValue().getTranslation()
				.translateBy(new Translation2d(-90, -90)), kRunSpeed));

		BackCargoShipToLoadingStation.add(new Waypoint(robotState.getLatestFieldToVehicle().getValue().getTranslation()
				.translateBy(new Translation2d(-100, -110)), 0));

		routines.add(new DrivePathRoutine(new Path(BackCargoShipToLoadingStation), true));

		// turn toward the loading station
		routines.add(new CascadingGyroEncoderTurnAngleRoutine(100));

		ArrayList<Waypoint> ForwardCargoShipToLoadingStation = new ArrayList<>();
		ForwardCargoShipToLoadingStation.add(new Waypoint(robotState.getLatestFieldToVehicle().getValue()
				.getTranslation().translateBy(new Translation2d(-130, -110)), kRunSpeed * 0.3));
		ForwardCargoShipToLoadingStation.add(new Waypoint(robotState.getLatestFieldToVehicle().getValue()
				.getTranslation().translateBy(new Translation2d(-140, -110)), 0));

		return new SequentialRoutine(routines);
	}

	@Override
	public String getKey() {
		return sAlliance.toString();
	}
}