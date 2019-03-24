package com.palyrobotics.frc2019.auto.modes;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.ParallelRoutine;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.TimeoutRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.*;
import com.palyrobotics.frc2019.behavior.routines.elevator.ElevatorCustomPositioningRoutine;
import com.palyrobotics.frc2019.behavior.routines.fingers.FingersCloseRoutine;
import com.palyrobotics.frc2019.behavior.routines.fingers.FingersCycleRoutine;
import com.palyrobotics.frc2019.behavior.routines.fingers.FingersExpelRoutine;
import com.palyrobotics.frc2019.behavior.routines.fingers.FingersOpenRoutine;
import com.palyrobotics.frc2019.behavior.routines.intake.*;
import com.palyrobotics.frc2019.behavior.routines.pusher.*;
import com.palyrobotics.frc2019.behavior.routines.shooter.ShooterExpelRoutine;
import com.palyrobotics.frc2019.behavior.routines.waits.WaitForCargoElevator;
import com.palyrobotics.frc2019.config.Constants.*;
import com.palyrobotics.frc2019.subsystems.Shooter;
import com.palyrobotics.frc2019.util.trajectory.Path;
import com.palyrobotics.frc2019.util.trajectory.Path.Waypoint;
import com.palyrobotics.frc2019.util.trajectory.Translation2d;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")

public class Hecker extends AutoModeBase {
    //right start > rocket ship close > loading station > rocket ship far > depot > rocket ship mid

    public static int kRunSpeed = 90;
    public static double kOffsetX = -20;
    public static double kOffsetY = PhysicalConstants.kLevel3Width * .5 + PhysicalConstants.kLevel2Width * .5;
    public static double kCargoShipRightFrontX = mDistances.kLevel1CargoX + PhysicalConstants.kLowerPlatformLength + PhysicalConstants.kUpperPlatformLength;
    public static double kCargoShipRightFrontY = -(mDistances.kFieldWidth * .5 - (mDistances.kCargoRightY + mDistances.kCargoOffsetY));
    public static double kHabLineX = PhysicalConstants.kUpperPlatformLength + PhysicalConstants.kLowerPlatformLength;
    public static double kRightLoadingStationX = 0;
    public static double kRightLoadingStationY = -(mDistances.kFieldWidth * .5 - mDistances.kRightLoadingY);
    public static double kRightDepotX = PhysicalConstants.kUpperPlatformLength;
    public static double kRightDepotY = -(mDistances.kFieldWidth * .5 - mDistances.kDepotFromRightY);
    public static double kRightRocketShipCloseX = mDistances.kHabRightRocketCloseX + kHabLineX;
    public static double kRightRocketShipCloseY = -(mDistances.kFieldWidth * .5 - mDistances.kRightRocketCloseY);
    public static double kRightRocketShipMidX = kHabLineX + mDistances.kHabRightRocketMidX;
    public static double kRightRocketShipMidY = -(mDistances.kFieldWidth * .5 - mDistances.kRightRocketMidY);
    public static double kRightRocketShipFarX = mDistances.kFieldWidth - mDistances.kMidlineRightRocketFarX;
    public static double kRightRocketShipFarY = -(mDistances.kFieldWidth * .5 - mDistances.kRightRocketFarY);

    public Translation2d kCargoShipRightFront = new Translation2d(kCargoShipRightFrontX + PhysicalConstants.kRobotWidthInches * .2 + kOffsetX,
            kCargoShipRightFrontY + PhysicalConstants.kRobotLengthInches * .05 + kOffsetY);
    public Translation2d kRightLoadingStation = new Translation2d(kRightLoadingStationX + PhysicalConstants.kRobotLengthInches * 0.5 + kOffsetX,
            kRightLoadingStationY + PhysicalConstants.kRobotLengthInches * .2 + kOffsetY);
    public Translation2d kRightRocketShipFar = new Translation2d(kRightRocketShipFarX + PhysicalConstants.kRobotLengthInches * 1 + kOffsetX,
            kRightRocketShipFarY + PhysicalConstants.kRobotLengthInches * .05 + kOffsetY);
    public Translation2d kRightDepot = new Translation2d(kRightDepotX + PhysicalConstants.kRobotLengthInches * 1.1 + kOffsetX,
            kRightDepotY + PhysicalConstants.kRobotLengthInches * .25 + kOffsetY);
    public Translation2d kRightRocketShipClose = new Translation2d(kRightRocketShipCloseX - PhysicalConstants.kRobotLengthInches * 0 + kOffsetX,
            kRightRocketShipCloseY + PhysicalConstants.kRobotLengthInches * .2 + kOffsetY);
    public Translation2d kRightRocketShipMid = new Translation2d(kRightRocketShipMidX + kOffsetX,
            kRightRocketShipMidY + kOffsetY);

    @Override
    public String toString() {
        return mAlliance + this.getClass().toString();
    }

    @Override
    public void prestart() {

    }

    @Override
    public Routine getRoutine() {
        return new SequentialRoutine(new DriveSensorResetRoutine(1), placeHatchClose1(), takeHatch(), placeHatchClose2());
    }

    public Routine placeHatchClose1() { //start to rocket ship close
        ArrayList<Routine> routines = new ArrayList<>();

        ArrayList<Waypoint> StartToRocketShip = new ArrayList<>();
        StartToRocketShip.add(new Waypoint(new Translation2d(0, 0), kRunSpeed));
        StartToRocketShip.add(new Waypoint(new Translation2d(kHabLineX + PhysicalConstants.kRobotLengthInches * 0.5 + kOffsetX,
                0), kRunSpeed)); //goes straight at the start so the robot doesn't get messed up over the ramp
        StartToRocketShip.add(new Waypoint(new Translation2d(kRightRocketShipCloseX * .6 + kOffsetX,
                findLineClose(kRightRocketShipCloseX * .8) + PhysicalConstants.kRobotLengthInches * .35 + kOffsetY), kRunSpeed, "visionStart")); //line up with rocket ship
        StartToRocketShip.add(new Waypoint(kRightRocketShipClose, 0));

        routines.add(new VisionAssistedDrivePathRoutine(StartToRocketShip, false, false, "visionStart"));

//        ArrayList<Waypoint> goForward = new ArrayList<>();
//        goForward.add(new Waypoint(new Translation2d(0, 0), 20, true));
//        //TODO: change translation cords
//        goForward.add(new Waypoint(new Translation2d(20, 0), 0, true));

        //pusher out while driving forward slowly
//        routines.add(new ParallelRoutine(new DrivePathRoutine(goForward, false, true),
//                new PusherOutRoutine()));

        routines.add(new PusherOutRoutine());

        //release hatch
        routines.add(new FingersCloseRoutine());
        routines.add(new FingersExpelRoutine(.05));

        routines.add(new TimeoutRoutine(.4));
        //pusher back in
        routines.add(new PusherInRoutine());

        return new SequentialRoutine(routines);
    }

    public Routine takeHatch() { //rocket ship close to loading station
        ArrayList<Routine> routines = new ArrayList<>();

        ArrayList<Waypoint> BackCargoShipToLoadingStation = new ArrayList<>();
        BackCargoShipToLoadingStation.add(new Waypoint(kRightRocketShipClose, kRunSpeed));
        BackCargoShipToLoadingStation.add(new Waypoint(kRightLoadingStation.translateBy
                (new Translation2d(PhysicalConstants.kRobotLengthInches * 2, PhysicalConstants.kRobotLengthInches * 0.5)), 0));
        routines.add(new DrivePathRoutine(new Path(BackCargoShipToLoadingStation), true));

        //turn to face the loading station
        routines.add(new BBTurnAngleRoutine(160));

        ArrayList<Waypoint> ForwardCargoShipToLoadingStation = new ArrayList<>();
        ForwardCargoShipToLoadingStation.add(new Waypoint(kRightLoadingStation.translateBy
                (new Translation2d(PhysicalConstants.kRobotLengthInches * 1.5, 0)), kRunSpeed, "visionStart"));
        ForwardCargoShipToLoadingStation.add(new Waypoint(kRightLoadingStation, 0));

//        //get pusher ready for hatch intake
//        ArrayList<Routine> getIntakeReady = new ArrayList<>();
//        getIntakeReady.add(new PusherOutRoutine());

        //drive and ready pusher at the same time
//        routines.add(new ParallelRoutine(new DrivePathRoutine(new Path(ForwardCargoShipToLoadingStation), false),
//                new SequentialRoutine(getIntakeReady)));

        routines.add(new VisionAssistedDrivePathRoutine(ForwardCargoShipToLoadingStation,
                true, false, "visionStart"));
        routines.add(new PusherOutRoutine());
        routines.add(new FingersOpenRoutine());
        routines.add(new TimeoutRoutine(.5));

//        ArrayList<Waypoint> goForwardABit = new ArrayList<>();
//        goForwardABit.add(new Waypoint(new Translation2d(0, 0), 20, true));
//        goForwardABit.add(new Waypoint(new Translation2d(-20, 0), 0, true));
//
//        //drive slowly forward and intake hatch
//        routines.add(new SequentialRoutine(new DrivePathRoutine(goForwardABit, false, true),
//                new FingersOpenRoutine(), new PusherInRoutine()));

        return new SequentialRoutine(routines);
    }

    public Routine placeHatchClose2() { //loading station to rocket ship close
        ArrayList<Routine> routines = new ArrayList<>();

        /*
        The robot starts backwards at the loading station after loading a hatch. It then goes around the rocket ship and over shoots it a bit. Then, it lines up with the rocket ship far and places the hatch.
         */
        ArrayList<Waypoint> BackLoadingStationToCargoShip = new ArrayList<>();
        BackLoadingStationToCargoShip.add(new Waypoint(kRightLoadingStation, kRunSpeed));
        BackLoadingStationToCargoShip.add(new Waypoint(kRightLoadingStation.translateBy
                (new Translation2d(PhysicalConstants.kRobotLengthInches, 0)), kRunSpeed));
        BackLoadingStationToCargoShip.add(new Waypoint(new Translation2d(kRightRocketShipCloseX * 0.6,
                findLineClose(kRightRocketShipCloseX * 0.6) + PhysicalConstants.kRobotLengthInches * .7), 0));
        routines.add(new DrivePathRoutine(new Path(BackLoadingStationToCargoShip), true));

        //turn to face the loading station
        routines.add(new CascadingGyroEncoderTurnAngleRoutine(160));

        ArrayList<Waypoint> ForwardLoadingStationToRocketShip = new ArrayList<>();
        ForwardLoadingStationToRocketShip.add(new Waypoint(new Translation2d(kRightRocketShipCloseX * 0.65,
                findLineClose(kRightRocketShipCloseX * 0.65) + PhysicalConstants.kRobotLengthInches * .7), kRunSpeed));
        ForwardLoadingStationToRocketShip.add(new Waypoint(kRightRocketShipClose, 0));

        routines.add(new DrivePathRoutine(new Path(ForwardLoadingStationToRocketShip), false));

        //release hatch
//        routines.add(new FingersOpenRoutine());

        return new SequentialRoutine(routines);
    }

    public double findLineClose(double cordX) {
        return -0.54862 * cordX + 0.54862 * kRightRocketShipCloseX + kRightRocketShipCloseY; //slope is derived from the angle of the rocket ship sides - constants derived from math
    } //the y cord of an invisible line extending from rocket ship close

    public double findLineFar(double cordX) {
        return 0.54862 * cordX - 0.54862 * kRightRocketShipFarX + kRightRocketShipFarY; //slope is derived from the angle of the rocket ship sides - constants derived from math
    } //the y cord of an invisible line extending from rocket ship close

    @Override
    public String getKey() {
        return mAlliance.toString();
    }
}