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

public class FullSend extends AutoModeBase { //starts at center (lvl 3) - robot will probably start on lvl 2

    public static int SPEED = 100; //start from level 1
    public static double kOffsetX = -Constants.kLowerPlatformLength - Constants.kRobotLengthInches;
    public static double kOffsetY = 0;
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
        return new SequentialRoutine(new ParallelRoutine(new DriveSensorResetRoutine(2)), placeHatch1(), takeHatch(), placeHatch2(), takeCargo(), placeCargoClose()); //right start close rocketship loading station far rocketship depot
    }

    public Routine placeHatch1() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> StartToCargoShip = new ArrayList<>();
        StartToCargoShip.add(new Waypoint(new Translation2d(-(Constants.kUpperPlatformLength + Constants.kLowerPlatformLength + Constants.kRobotLengthInches + kOffsetX), 0), SPEED));
        StartToCargoShip.add(new Waypoint(new Translation2d(-(kCargoShipRightFrontX - Constants.kRobotWidthInches + kOffsetX), -(kCargoShipRightFrontY)), SPEED));
        StartToCargoShip.add(new Waypoint(new Translation2d(-(kCargoShipRightFrontX - Constants.kRobotWidthInches * .1 + kOffsetX), -(kCargoShipRightFrontY)), 0));
        routines.add(new DrivePathRoutine(new Path(StartToCargoShip), true));

        //routines.add(new ReleaseHatchRoutine()); //routine not made yet
        routines.add(new TimeoutRoutine(1));

        return new SequentialRoutine(routines);
    }

    public Routine takeHatch() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> backCargoShipToLoadingStation = new ArrayList<>();
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(-(kCargoShipRightFrontX - Constants.kRobotLengthInches + kOffsetX), -(kCargoShipRightFrontY)), SPEED));
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(-((kCargoShipRightFrontX + kOffsetX) * .5), -(kRightLoadingStationY * .5)), SPEED));
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(-(kHabLineX - Constants.kRobotLengthInches * .5 + kOffsetX), -(kRightLoadingStationY)), SPEED));
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(-(kRightLoadingStationX + kOffsetX + Constants.kRobotWidthInches * 1.3), -(kRightLoadingStationY)), 0));
        routines.add(new DrivePathRoutine(new Path(backCargoShipToLoadingStation), false));

        routines.add(new TimeoutRoutine(1));

        return new SequentialRoutine(routines);
    }

    public Routine placeHatch2() { //
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> backLoadingStationToRocketShip = new ArrayList<>();
        backLoadingStationToRocketShip.add(new Waypoint(new Translation2d(-(kRightLoadingStationX + Constants.kRobotLengthInches + kOffsetX), -(kRightLoadingStationY + kOffsetY)), SPEED));
        backLoadingStationToRocketShip.add(new Waypoint(new Translation2d(-(kRightRocketShipMidX + kOffsetX), -(findLineFar(kRightRocketShipMidX + Constants.kRobotLengthInches * 2.2) + kOffsetY)), SPEED));
        backLoadingStationToRocketShip.add(new Waypoint(new Translation2d(-(kRightRocketShipFarX + Constants.kRobotLengthInches * 2.2 + kOffsetX), -(findLineFar(kRightRocketShipMidX + Constants.kRobotLengthInches * 2.2) - Constants.kRobotLengthInches * .3 + kOffsetY)), 0));
        routines.add(new DrivePathRoutine(new Path(backLoadingStationToRocketShip), true));

        List<Path.Waypoint> forwardLoadingStationToRocketShip = new ArrayList<>();
        forwardLoadingStationToRocketShip.add(new Waypoint(new Translation2d(-(kRightRocketShipFarX + Constants.kRobotLengthInches * 1.8 + kOffsetX), -(findLineFar(kRightRocketShipMidX + Constants.kRobotLengthInches * 1.8) - Constants.kRobotLengthInches * .3 + kOffsetY)), SPEED)); //line up with RS
        forwardLoadingStationToRocketShip.add(new Waypoint(new Translation2d(-(kRightRocketShipFarX + Constants.kRobotLengthInches * 1 + kOffsetX), -(kRightRocketShipFarY + Constants.kRobotLengthInches * .1 + kOffsetY)), 0));
        routines.add(new DrivePathRoutine(new Path(forwardLoadingStationToRocketShip), false));

        //routines.add(new ReleaseHatchRoutine()); //routine not made yet
        routines.add(new TimeoutRoutine(1)); //placeholder

        return new SequentialRoutine(routines);
    }

    public Routine takeCargo() { //could be more accurate - test later
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> RocketShipToDepot = new ArrayList<>();
        RocketShipToDepot.add(new Waypoint(new Translation2d(-(kRightRocketShipFarX + Constants.kRobotLengthInches * 1.6 + kOffsetX), -(findLineFar(kRightRocketShipMidX + Constants.kRobotLengthInches * 1.6) + Constants.kRobotLengthInches * 0.5 + kOffsetY)), SPEED)); //line up with RS
        RocketShipToDepot.add(new Waypoint(new Translation2d(-(kRightRocketShipFarX + kOffsetX), -(kRightDepotY + kOffsetY)), SPEED));
        RocketShipToDepot.add(new Waypoint(new Translation2d(-(kHabLineX + kOffsetX), -(kRightDepotY + kOffsetY)), SPEED));
        RocketShipToDepot.add(new Waypoint(new Translation2d(-(kRightDepotX + Constants.kRobotLengthInches + kOffsetX), -(kRightDepotY + kOffsetY)), 0));
        routines.add(new DrivePathRoutine(new Path(RocketShipToDepot), true));

        //routines.add(new TakeCargoRoutine); //not made yet
        routines.add(new TimeoutRoutine(1)); //placeholder

        return new SequentialRoutine(routines);
    }

    public Routine placeCargoClose() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> StartToCargoShip = new ArrayList<>();
        StartToCargoShip.add(new Waypoint(new Translation2d(-(kRightRocketShipCloseX * .8 + kOffsetX), -(findLineClose(kRightRocketShipCloseX * .8) + kOffsetY)), SPEED));
        StartToCargoShip.add(new Waypoint(new Translation2d(-(kRightRocketShipCloseX + Constants.kRobotLengthInches * .1 + kOffsetX), -(kRightRocketShipCloseY + Constants.kRobotLengthInches * .5 + kOffsetY)), 0));
        routines.add(new DrivePathRoutine(new Path(StartToCargoShip), false));

        //routines.add(new ReleaseCargoRoutine(short)); //routine not made yet
        routines.add(new TimeoutRoutine(1)); //placeholder

        return new SequentialRoutine(routines);
    }

    public double findLineClose(double cordX) {
        return cordX * -.55 + 10;
    }

    public double findLineFar(double cordX) {
        return cordX * .55 - 277.5;
    }

    @Override
    public String getKey() {
        return mAlliance.toString();
    }
}


