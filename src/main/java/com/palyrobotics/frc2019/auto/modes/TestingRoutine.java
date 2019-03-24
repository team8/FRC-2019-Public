package com.palyrobotics.frc2019.auto.modes;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.auto.modes.RightStartRightFrontCargoAutoMode;
import com.palyrobotics.frc2019.behavior.ParallelRoutine;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.BBTurnAngleRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.CascadingGyroEncoderTurnAngleRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DriveSensorResetRoutine;
import com.palyrobotics.frc2019.behavior.routines.elevator.ElevatorCustomPositioningRoutine;
import com.palyrobotics.frc2019.behavior.routines.fingers.FingersCloseRoutine;
import com.palyrobotics.frc2019.behavior.routines.fingers.FingersCycleRoutine;
import com.palyrobotics.frc2019.behavior.routines.fingers.FingersOpenRoutine;
import com.palyrobotics.frc2019.behavior.routines.intake.*;
import com.palyrobotics.frc2019.behavior.routines.pusher.PusherInRoutine;
import com.palyrobotics.frc2019.behavior.routines.pusher.PusherOutRoutine;
import com.palyrobotics.frc2019.behavior.routines.shooter.ShooterExpelRoutine;
import com.palyrobotics.frc2019.behavior.routines.waits.WaitForCargoElevator;
import com.palyrobotics.frc2019.config.Constants.*;
import com.palyrobotics.frc2019.config.Constants.PhysicalConstants;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.subsystems.Drive;
import com.palyrobotics.frc2019.subsystems.Shooter;
import com.palyrobotics.frc2019.util.trajectory.Path;
import com.palyrobotics.frc2019.util.trajectory.Path.Waypoint;
import com.palyrobotics.frc2019.util.trajectory.Translation2d;

import javax.print.attribute.standard.MediaSize;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")

public class TestingRoutine extends AutoModeBase {

    public static int kRunSpeed = 110;
    public static double kOffsetX = -PhysicalConstants.kLowerPlatformLength - PhysicalConstants.kRobotLengthInches * 0.6;
    public static double kOffsetY = PhysicalConstants.kLevel3Width * .5 + PhysicalConstants.kLevel2Width * .5;
    public static double kCargoShipRightFrontX = mDistances.kLevel1CargoX + PhysicalConstants.kLowerPlatformLength + PhysicalConstants.kUpperPlatformLength;
    public static double kCargoShipRightFrontY = -(mDistances.kFieldWidth * .5 - (mDistances.kCargoRightY + mDistances.kCargoOffsetY));
    public static double kHabLineX = PhysicalConstants.kUpperPlatformLength + PhysicalConstants.kLowerPlatformLength;
    public static double kRightLoadingStationX = 0;
    public static double kRightLoadingStationY = -(mDistances.kFieldWidth * .5 - mDistances.kRightLoadingY);
    public RobotState robotState = RobotState.getInstance();

    @Override
    public String toString() {
        return mAlliance + this.getClass().toString();
    }

    @Override
    public void prestart() {

    }

    @Override
    public Routine getRoutine() {
        return new SequentialRoutine(new DriveSensorResetRoutine(1), takeHatch());
    }

    public Routine takeHatch() { //cargo ship front to loading station
        ArrayList<Routine> routines = new ArrayList<>();

        RobotState robotState = RobotState.getInstance();

        ArrayList<Waypoint> BackCargoShipToLoadingStation = new ArrayList<>();

        BackCargoShipToLoadingStation.add(new Waypoint(
                robotState.getLatestFieldToVehicle().getValue().getTranslation(), kRunSpeed));

        //back up
        BackCargoShipToLoadingStation.add(new Waypoint(
                robotState.getLatestFieldToVehicle().getValue().getTranslation().translateBy(new Translation2d(
                        -50, 0)), kRunSpeed));

        BackCargoShipToLoadingStation.add(new Waypoint(
                robotState.getLatestFieldToVehicle().getValue().getTranslation().translateBy(new Translation2d(
                        -90, -90)), kRunSpeed));

        BackCargoShipToLoadingStation.add(new Waypoint(
                robotState.getLatestFieldToVehicle().getValue().getTranslation().translateBy(new Translation2d(
                        -100, -110)), 0));


//        BackCargoShipToLoadingStation.add(new Waypoint(
//                robotState.getLatestFieldToVehicle().getValue().getTranslation().translateBy(new Translation2d(
//                        0, -PhysicalConstants.kRobotLengthInches)), 0));

//        BackCargoShipToLoadingStation.add(new Waypoint(
//                robotState.getLatestFieldToVehicle().getValue().getTranslation().translateBy(new Translation2d(
//                        -kCargoShipRightFrontX * 0.5, (kRightLoadingStationY - kCargoShipRightFrontY) * 0.6)), 0));

        routines.add(new DrivePathRoutine(new Path(BackCargoShipToLoadingStation), true));

        //turn toward the loading station
        routines.add(new CascadingGyroEncoderTurnAngleRoutine(100));

        ArrayList<Waypoint> ForwardCargoShipToLoadingStation = new ArrayList<>();
        ForwardCargoShipToLoadingStation.add(new Waypoint(
                robotState.getLatestFieldToVehicle().getValue().getTranslation().translateBy(new Translation2d(
                -130, -110)), kRunSpeed * 0.3));
        ForwardCargoShipToLoadingStation.add(new Waypoint(
                robotState.getLatestFieldToVehicle().getValue().getTranslation().translateBy(new Translation2d(
                        -140, -110)), 0));

//        routines.add(new DrivePathRoutine(new Path(ForwardCargoShipToLoadingStation), false));

//        //get fingers ready for hatch intake
//        ArrayList<Routine> getIntakeReady = new ArrayList<>();
//        getIntakeReady.add(new PusherOutRoutine());
//        getIntakeReady.add(new FingersCloseRoutine());
//
//        //drive and ready fingers at the same time
//        routines.add(new ParallelRoutine(new DrivePathRoutine(new Path(ForwardCargoShipToLoadingStation), false),
//                new SequentialRoutine(getIntakeReady)));
//
//        ArrayList<Waypoint> goForwardABit = new ArrayList<>();
//        goForwardABit.add(new Waypoint(kRightLoadingStation.translateBy
//                (new Translation2d(PhysicalConstants.kRobotLengthInches, 0)), 20));
//        goForwardABit.add(new Waypoint(kRightLoadingStation, 0));
//
//        //drive slowly forward and intake hatch
//        routines.add(new SequentialRoutine(new DrivePathRoutine(new Path(goForwardABit), false),
//                new FingersOpenRoutine(), new PusherInRoutine()));

        return new SequentialRoutine(routines);
    }

    @Override
    public String getKey() {
        return mAlliance.toString();
    }
}