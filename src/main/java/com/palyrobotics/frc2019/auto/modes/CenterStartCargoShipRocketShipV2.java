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

public class CenterStartCargoShipRocketShipV2 extends AutoModeBase { //starts at center (lvl 3) - robot will probably start on lvl 2

    public static int SPEED = 50; //start from level 1
    public static double kOffsetX = Constants.kLowerPlatformLength;
    public static double kCargoShipRightFrontX = mDistances.kLevel1CargoX + Constants.kLowerPlatformLength + Constants.kUpperPlatformLength - Constants.kRobotLengthInches * 2;
    public static double kCargoShipRightFrontY = -(mDistances.kFieldWidth - (mDistances.kCargoLeftY + mDistances.kCargoOffsetY));
    public static double kHabLineX = Constants.kUpperPlatformLength + Constants.kLowerPlatformLength;
    public static double kRightLoadingStationX = 0;
    public static double kRightLoadingStationY = mDistances.kFieldWidth * .5 - mDistances.kRightLoadingY;
    public static double kRightRocketShipCloseX = mDistances.kHabRightRocketCloseX + kHabLineX;
    public static double kRightRocketShipCloseY = -(mDistances.kFieldWidth * .5 - mDistances.kRightRocketCloseY);
    public static double kRightRocketShipFarX = mDistances.kFieldWidth - mDistances.kMidlineRightRocketFarX;
    public static double kRightRocketShipFarY = -(mDistances.kFieldWidth - mDistances.kRightRocketFarY);

    @Override
    public String toString() {
        return mAlliance + this.getClass().toString();
    }

    @Override
    public void prestart() {

    }

    @Override
    public Routine getRoutine() {
        return new SequentialRoutine(new DriveSensorResetRoutine(2), placeHatch1(), takeHatch(), placeHatch2(), takeCargo(), placeCargoFar());
    }

    public Routine placeHatch1() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> StartToCargoShip = new ArrayList<>();
        StartToCargoShip.add(new Waypoint(new Translation2d(-(Constants.kUpperPlatformLength + Constants.kLowerPlatformLength + Constants.kRobotLengthInches + kOffsetX), 0), SPEED * .5));
        StartToCargoShip.add(new Waypoint(new Translation2d(-(kCargoShipRightFrontX + kOffsetX) * .75, -(kCargoShipRightFrontY)), SPEED));
        StartToCargoShip.add(new Waypoint(new Translation2d(-(kCargoShipRightFrontX + kOffsetX), -(kCargoShipRightFrontY)), 0));
        routines.add(new DrivePathRoutine(new Path(StartToCargoShip), true));

        //routines.add(new ReleaseHatchRoutine()); //routine not made yet
        routines.add(new TimeoutRoutine(1));

        return new SequentialRoutine(routines);
    }

    public Routine takeHatch() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> backCargoShipToLoadingStation = new ArrayList<>();
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(-(kCargoShipRightFrontX - Constants.kRobotLengthInches * 2 + kOffsetX), -(kCargoShipRightFrontY)), SPEED));
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(-(kCargoShipRightFrontX + kOffsetX) * .5, -(kCargoShipRightFrontY) * .5), SPEED));
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(-(kCargoShipRightFrontX * .5 - Constants.kRobotLengthInches + kOffsetX), -(kCargoShipRightFrontY * .5 + Constants.kRobotLengthInches)), SPEED));
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(-(kHabLineX - Constants.kRobotLengthInches + kOffsetX), kRightLoadingStationY), SPEED * .8));
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(-(kRightLoadingStationX + kOffsetX), -(kRightLoadingStationY)), 0));
        routines.add(new DrivePathRoutine(new Path(backCargoShipToLoadingStation), false));

        return new SequentialRoutine(routines);
    }

    public Routine placeHatch2() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> backLoadingStationToRocketShip = new ArrayList<>();
        backLoadingStationToRocketShip.add(new Waypoint(new Translation2d(-(kRightLoadingStationX + Constants.kRobotLengthInches + kOffsetX), -(kRightLoadingStationY + Constants.kRobotLengthInches)), SPEED));
        backLoadingStationToRocketShip.add(new Waypoint(new Translation2d(-(kHabLineX + kOffsetX), 85.4), 0));
        routines.add(new DrivePathRoutine(new Path(backLoadingStationToRocketShip), true));

        routines.add(new CascadingGyroEncoderTurnAngleRoutine(120));

        List<Path.Waypoint> forwardLoadingStationToRocketShip = new ArrayList<>();
        forwardLoadingStationToRocketShip.add(new Waypoint(new Translation2d(-(kCargoShipRightFrontX * .5 + kOffsetX), 93.5), SPEED)); //line up with RS
        forwardLoadingStationToRocketShip.add(new Waypoint(new Translation2d(-(kRightRocketShipCloseX + kOffsetX), -(kRightRocketShipCloseY)), 0));
        routines.add(new DrivePathRoutine(new Path(forwardLoadingStationToRocketShip), false));

        //routines.add(new ReleaseHatchRoutine()); //routine not made yet
        routines.add(new TimeoutRoutine(1)); //placeholder

        return new SequentialRoutine(routines);
    }

    public Routine takeCargo() { //could be more accurate - test later
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> RocketShipToDepot = new ArrayList<>();
        RocketShipToDepot.add(new Waypoint(new Translation2d(-(kCargoShipRightFrontX * .5 + kOffsetX), 103.5), SPEED));
        RocketShipToDepot.add(new Waypoint(new Translation2d(-(kHabLineX + kOffsetX), -(Constants.kLevel1Width * .5 - Constants.kRobotLengthInches * .5)), SPEED));
        RocketShipToDepot.add(new Waypoint(new Translation2d(-(Constants.kUpperPlatformLength + kOffsetX), -(Constants.kLevel1Width * .5 - Constants.kRobotLengthInches * .5)), 0));
        routines.add(new DrivePathRoutine(new Path(RocketShipToDepot), true));

        //routines.add(new TakeCargoRoutine); //not made yet
        routines.add(new TimeoutRoutine(1)); //placeholder

        return new SequentialRoutine(routines);
    }

//    public Routine placeCargoMid() {
//        ArrayList<Routine> routines = new ArrayList<>();
//
//        List<Path.Waypoint> DepotToRocketShip1 = new ArrayList<>();
//        DepotToRocketShip1.add(new Waypoint(new Translation2d(kHabLineX, kLevel1Width / 2 + Constants.kRobotLengthInches / 2), SPEED));
//        DepotToRocketShip1.add(new Waypoint(new Translation2d(kRSMX + Constants.kRobotLengthInches - 11, kLevel1Width / 2 + Constants.kRobotLengthInches / 2), 0));
//        routines.add(new DrivePathRoutine(new Path(DepotToRocketShip1), false));
//
//        routines.add(new CascadingGyroEncoderTurnAngleRoutine(-90));
//
//        List<Path.Waypoint> DepotToRocketShip2 = new ArrayList<>();
//        DepotToRocketShip2.add(new Waypoint(new Translation2d(kRSMX + Constants.kRobotLengthInches - 11, kLevel1Width / 2 + Constants.kRobotLengthInches / 2 + 5), SPEED / 2));
//        DepotToRocketShip2.add(new Waypoint(new Translation2d(kRSMX + Constants.kRobotLengthInches - 11, kLevel1Width / 2 + Constants.kRobotLengthInches / 2 + 15), 0));
//        routines.add(new DrivePathRoutine(new Path(DepotToRocketShip2), false));
//
//        //routines.add(new ReleaseCargoRoutine(short)); //routine not made yet
//        routines.add(new TimeoutRoutine(1)); //placeholder
//
//        return new SequentialRoutine(routines);
//    }

    public Routine placeCargoFar() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> DepotToRocketShip1 = new ArrayList<>();
        DepotToRocketShip1.add(new Waypoint(new Translation2d(-(kHabLineX + kOffsetX), -(Constants.kLevel1Width * .5 - Constants.kRobotLengthInches * .5)), SPEED));
        DepotToRocketShip1.add(new Waypoint(new Translation2d(-(kRightRocketShipFarX - Constants.kRobotLengthInches + kOffsetX), -(Constants.kLevel1Width * .5 - Constants.kRobotLengthInches * .5)), 0));
        routines.add(new DrivePathRoutine(new Path(DepotToRocketShip1), false));

        routines.add(new CascadingGyroEncoderTurnAngleRoutine(-90));

        List<Path.Waypoint> DepotToRocketShip2 = new ArrayList<>();
        DepotToRocketShip2.add(new Waypoint(new Translation2d(-(kRightRocketShipFarX - Constants.kRobotLengthInches + kOffsetX), -(Constants.kLevel1Width * .5 - Constants.kRobotLengthInches / 2 - 15)), SPEED));
        routines.add(new DrivePathRoutine(new Path(DepotToRocketShip2), false));

        routines.add(new CascadingGyroEncoderTurnAngleRoutine(-30));

        List<Path.Waypoint> DepotToRocketShip3 = new ArrayList<>();
        DepotToRocketShip3.add(new Waypoint(new Translation2d(-(kRightRocketShipFarX - Constants.kRobotLengthInches - 5 + kOffsetX), -(Constants.kLevel1Width / 2 - Constants.kRobotLengthInches / 2 - 20)), SPEED));
        DepotToRocketShip3.add(new Waypoint(new Translation2d(-(kRightRocketShipFarX - Constants.kRobotLengthInches - 5 + kOffsetX), -(Constants.kLevel1Width / 2 - Constants.kRobotLengthInches / 2 - 30)), 0));
        routines.add(new DrivePathRoutine(new Path(DepotToRocketShip3), false));

        //routines.add(new ReleaseCargoRoutine(short)); //routine not made yet
        routines.add(new TimeoutRoutine(1)); //placeholder

        return new SequentialRoutine(routines);
    }

    @Override
    public String getKey() {
        return mAlliance.toString();
    }
}


