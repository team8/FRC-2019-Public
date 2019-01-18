package com.palyrobotics.frc2019.auto.modes;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.ParallelRoutine;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.TimeoutRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.CascadingGyroEncoderTurnAngleRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DriveSensorResetRoutine;
import com.palyrobotics.frc2019.config.Constants;
import com.palyrobotics.frc2019.util.trajectory.Path;
import com.palyrobotics.frc2019.util.trajectory.Path.Waypoint;
import com.palyrobotics.frc2019.util.trajectory.Translation2d;

import java.util.ArrayList;
import java.util.List;

public class FullSend extends AutoModeBase { //center start > cargo ship front > loading station > rocket ship far > depot > close rocket ship

    public static int SPEED = 100; //speed does not work at the moment due to a new version of pure pursuit
    public static double kOffsetX = -Constants.kLowerPlatformLength - Constants.kRobotLengthInches;
    public static double kOffsetY = 0; //0 at the moment because the auto starts at the center - change if start on right or left
    public static double kCargoShipRightFrontX = mDistances.kLevel1CargoX + Constants.kLowerPlatformLength + Constants.kUpperPlatformLength;
    public static double kCargoShipRightFrontY = -(mDistances.kFieldWidth * .5 - (mDistances.kCargoRightY + mDistances.kCargoOffsetY));
    public static double kHabLineX = Constants.kUpperPlatformLength + Constants.kLowerPlatformLength;
    public static double kRightLoadingStationX = 0;
    public static double kRightLoadingStationY = -(mDistances.kFieldWidth * .5 - mDistances.kRightLoadingY);
    public static double kRightDepotX = Constants.kUpperPlatformLength;
    public static double kRightDepotY = -(mDistances.kFieldWidth * .5 - mDistances.kDepotFromRightY);
    public static double kRightRocketShipCloseX = mDistances.kHabRightRocketCloseX + kHabLineX;
    public static double kRightRocketShipCloseY = -(mDistances.kFieldWidth * .5 - mDistances.kRightRocketCloseY);
    public static double kRightRocketShipMidX = kHabLineX + mDistances.kHabRightRocketMidX;
    public static double kRightRocketShipMidY = -(mDistances.kFieldWidth * .5 - mDistances.kRightRocketMidY);
    public static double kRightRocketShipFarX = mDistances.kFieldWidth - mDistances.kMidlineRightRocketFarX;
    public static double kRightRocketShipFarY = -(mDistances.kFieldWidth * .5 - mDistances.kRightRocketFarY);

    @Override
    public String toString() {
        return mAlliance + this.getClass().toString();
    }

    @Override
    public void prestart() {

    }

    @Override
    public Routine getRoutine() {
        return new SequentialRoutine(new DriveSensorResetRoutine(2), placeHatch1(), takeHatch(), placeHatch2(), takeCargo(), placeCargoClose());
    }

    public Routine placeHatch1() { //start to cargo ship front
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> StartToCargoShip = new ArrayList<>();
        StartToCargoShip.add(new Waypoint(new Translation2d(-(kHabLineX + Constants.kRobotLengthInches + kOffsetX), 0), SPEED)); //goes straight at the start so the robot doesn't get messed up over the ramp
        StartToCargoShip.add(new Waypoint(new Translation2d(-(kCargoShipRightFrontX - Constants.kRobotWidthInches + kOffsetX), -(kCargoShipRightFrontY + kOffsetY)), SPEED)); //lines up in front of cargo ship front
        StartToCargoShip.add(new Waypoint(new Translation2d(-(kCargoShipRightFrontX - Constants.kRobotWidthInches * .1 + kOffsetX), -(kCargoShipRightFrontY + kOffsetY)), 0));
        routines.add(new DrivePathRoutine(new Path(StartToCargoShip), true));

//        routines.add(new ReleaseHatchRoutine()); //routine not made yet
//        routines.add(new TimeoutRoutine(1));

        return new SequentialRoutine(routines);
    }

    public Routine takeHatch() { //cargo ship front to loading station
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> CargoShipToLoadingStation = new ArrayList<>();
        CargoShipToLoadingStation.add(new Waypoint(new Translation2d(-(kCargoShipRightFrontX - Constants.kRobotLengthInches + kOffsetX), -(kCargoShipRightFrontY + kOffsetY)), SPEED)); //backs out of the cargo ship
        CargoShipToLoadingStation.add(new Waypoint(new Translation2d(-(kCargoShipRightFrontX * .5 + kOffsetX), -(kRightLoadingStationY * .5 + kOffsetY)), SPEED));
        CargoShipToLoadingStation.add(new Waypoint(new Translation2d(-(kHabLineX - Constants.kRobotLengthInches * .5 + kOffsetX), -(kRightLoadingStationY + Constants.kRobotWidthInches * .3 + kOffsetY)), SPEED)); //lines up with loading station
        CargoShipToLoadingStation.add(new Waypoint(new Translation2d(-(kRightLoadingStationX + kOffsetX + Constants.kRobotLengthInches), -(kRightLoadingStationY + Constants.kRobotWidthInches * .3 + kOffsetY)), 0));
        routines.add(new DrivePathRoutine(new Path(CargoShipToLoadingStation), false));

//        routines.add(new TakeHatchRoutine()); //routine not made yet
//        routines.add(new TimeoutRoutine(1)); //placeholder

        return new SequentialRoutine(routines);
    }

    public Routine placeHatch2() { //loading station to rocket ship far
        ArrayList<Routine> routines = new ArrayList<>();

        /*
        The robot starts backwards at the loading station after loading a hatch. It then goes around the rocket ship over shoots it a bit. Then, it lines up with the rocket ship far and places the hatch.
         */

        List<Path.Waypoint> backLoadingStationToRocketShip = new ArrayList<>(); //robot starts going backwards
        backLoadingStationToRocketShip.add(new Waypoint(new Translation2d(-(kRightLoadingStationX + Constants.kRobotLengthInches + kOffsetX), -(kRightLoadingStationY + kOffsetY)), SPEED)); //backs up a bit
        backLoadingStationToRocketShip.add(new Waypoint(new Translation2d(-(kRightRocketShipMidX + kOffsetX), -(findLineFar(kRightRocketShipMidX + Constants.kRobotLengthInches * 2.2) + kOffsetY)), SPEED)); //goes around the rocket ship
        backLoadingStationToRocketShip.add(new Waypoint(new Translation2d(-(kRightRocketShipFarX + Constants.kRobotLengthInches * 2.2 + kOffsetX), -(findLineFar(kRightRocketShipMidX + Constants.kRobotLengthInches * 2.2) - Constants.kRobotLengthInches * .3 + kOffsetY)), 0));
        routines.add(new DrivePathRoutine(new Path(backLoadingStationToRocketShip), true));

        List<Path.Waypoint> forwardLoadingStationToRocketShip = new ArrayList<>(); //robot turns and then moves forward to the rocket ship
        forwardLoadingStationToRocketShip.add(new Waypoint(new Translation2d(-(kRightRocketShipFarX + Constants.kRobotLengthInches * 1.8 + kOffsetX), -(findLineFar(kRightRocketShipMidX + Constants.kRobotLengthInches * 1.8) - Constants.kRobotLengthInches * .3 + kOffsetY)), SPEED)); //line up with rocket ship
        forwardLoadingStationToRocketShip.add(new Waypoint(new Translation2d(-(kRightRocketShipFarX + Constants.kRobotLengthInches * 1.2 + kOffsetX), -(kRightRocketShipFarY + Constants.kRobotLengthInches * .1 + kOffsetY)), 0)); //ends in front of the rocket ship far
        routines.add(new DrivePathRoutine(new Path(forwardLoadingStationToRocketShip), false));

//        routines.add(new ReleaseHatchRoutine()); //routine not made yet
//        routines.add(new TimeoutRoutine(1)); //placeholder

        return new SequentialRoutine(routines);
    }

    public Routine takeCargo() { //rocket ship far to depot
        ArrayList<Routine> routines = new ArrayList<>();

        /*
        The robot starts at rocket ship far and goes around the rocket backwards. Ends at the depot and loads a cargo.
         */

        List<Path.Waypoint> RocketShipToDepot = new ArrayList<>();
        RocketShipToDepot.add(new Waypoint(new Translation2d(-(kRightRocketShipFarX + Constants.kRobotLengthInches * 1.6 + kOffsetX), -(findLineFar(kRightRocketShipMidX + Constants.kRobotLengthInches * 1.6) + Constants.kRobotLengthInches * 0.5 + kOffsetY)), SPEED));
        RocketShipToDepot.add(new Waypoint(new Translation2d(-(kHabLineX + kOffsetX), -(kRightDepotY + kOffsetY)), SPEED));
        RocketShipToDepot.add(new Waypoint(new Translation2d(-(kRightDepotX + Constants.kRobotLengthInches + kOffsetX), -(kRightDepotY + Constants.kRobotLengthInches * .2 + kOffsetY)), 0));
        routines.add(new DrivePathRoutine(new Path(RocketShipToDepot), true));

//        routines.add(new TakeCargoRoutine); //not made yet
//        routines.add(new TimeoutRoutine(1)); //placeholder

        return new SequentialRoutine(routines);
    }

    public Routine placeCargoClose() { //depot to rocket ship close
        ArrayList<Routine> routines = new ArrayList<>();

        /*
        The robot starts at the depot after loading a cargo. It then goes to rocket ship close and shoots a cargo into rocket ship far.
         */

        List<Path.Waypoint> DepotToRocketShip = new ArrayList<>();
        DepotToRocketShip.add(new Waypoint(new Translation2d(-(kRightRocketShipCloseX * .8 + kOffsetX), -(findLineClose(kRightRocketShipCloseX * .8) + kOffsetY)), SPEED));
        DepotToRocketShip.add(new Waypoint(new Translation2d(-(kRightRocketShipCloseX + kOffsetX), -(kRightRocketShipCloseY + Constants.kRobotLengthInches * .4 + kOffsetY)), 0));
        routines.add(new DrivePathRoutine(new Path(DepotToRocketShip), false));

//        routines.add(new ReleaseCargoRoutine()); //routine not made yet
//        routines.add(new TimeoutRoutine(1)); //placeholder

        return new SequentialRoutine(routines);
    }

    public double findLineClose(double cordX) {
        return cordX * -.55 + 10; //will make less sketch maybe
    } //the y cord of an invisible line extending from rocket ship close

    public double findLineFar(double cordX) {
        return cordX * .55 - 277.5; //will make less sketch maybe
    } //the y cord of an invisible line extending from rocket ship close

    @Override
    public String getKey() {
        return mAlliance.toString();
    }
}