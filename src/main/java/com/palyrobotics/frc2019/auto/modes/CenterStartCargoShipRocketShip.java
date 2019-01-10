package com.palyrobotics.frc2019.auto.modes;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.ParallelRoutine;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.WaypointTriggerRoutine;
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

public class CenterStartCargoShipRocketShip extends AutoModeBase {  //starts at center (lvl 3) - robot will probably start on lvl 2
//    public static int SPEED = 100;
//    public static double kCSX = 220.25;
//    public static double kRightCSY = -21.75;
//    public static double kLSX = 0;
//    public static double kLSY = -135; //not completely accurate
//    public static double kHabLineX = 95.28;

    public static int SPEED = 50; //start from level 1
    public static double kOffset = -44.125;
    public static double kLevel3Width = -47;
    public static double kCSX = -220.25 - kLevel3Width;
    public static double kRightCSY = 21.75;
    public static double kLSX = 0 - kLevel3Width;
    public static double kLSY = 135; //not completely accurate
    public static double kHabLineX = -95.28 - kLevel3Width;
    public static double kRSX = -164 - kLevel3Width;
    public static double kRSY = 118.2;
    public static double kLevel1Width = 147.25;

    @Override
    public String toString() {
        return mAlliance + this.getClass().toString();
    }

    @Override
    public void prestart() {

    }

    @Override
    public Routine getRoutine() {
        return new SequentialRoutine(new DriveSensorResetRoutine(2), placeHatch1(), takeCargo(), placeCargo(), takeHatch(), placeHatch2());
    }

    public Routine placeHatch1() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> StartToCargoShip = new ArrayList<>();
        StartToCargoShip.add(new Waypoint(new Translation2d(kCSX / 2, kRightCSY), SPEED * .4));
        StartToCargoShip.add(new Waypoint(new Translation2d(kCSX + Constants.kRobotLengthInches * 2, kRightCSY), SPEED));
        StartToCargoShip.add(new Waypoint(new Translation2d(kCSX + (Constants.kRobotLengthInches + 10), kRightCSY), 0)); //might replace with drivetillhatchon routine + little far
        routines.add(new DrivePathRoutine(new Path(StartToCargoShip), true));

        //routines.add(new ReleaseHatchRoutine()); //routine not made yet
        routines.add(new TimeoutRoutine(1)); //placeholder

        return new SequentialRoutine(routines);
    }

    public Routine takeCargo() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> CargoShipToDepot = new ArrayList<>();
        CargoShipToDepot.add(new Waypoint(new Translation2d(kCSX + Constants.kRobotLengthInches * 2, kLSY), SPEED));
        CargoShipToDepot.add(new Waypoint(new Translation2d(kCSX / 2, kLSY / 2), SPEED));
        CargoShipToDepot.add(new Waypoint(new Translation2d(kHabLineX, kLevel1Width / 2 + Constants.kRobotLengthInches / 2), SPEED));
        CargoShipToDepot.add(new Waypoint(new Translation2d(10, kLevel1Width / 2 + Constants.kRobotLengthInches / 2), 0));
        routines.add(new DrivePathRoutine(new Path(CargoShipToDepot), false));

        //routines.add(new TakeCargoRoutine); //not made yet
        routines.add(new TimeoutRoutine(1)); //placeholder

        return new SequentialRoutine(routines);
    }

    public Routine placeCargo() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> backDepotToRocketShip = new ArrayList<>();
        backDepotToRocketShip.add(new Waypoint(new Translation2d(kHabLineX, kLevel1Width / 2 + Constants.kRobotLengthInches / 2), SPEED));
        backDepotToRocketShip.add(new Waypoint(new Translation2d(kHabLineX - Constants.kRobotLengthInches, kLevel1Width / 2 + Constants.kRobotLengthInches / 2), 0));
        routines.add(new DrivePathRoutine(new Path(backDepotToRocketShip), true));

        routines.add(new CascadingGyroEncoderTurnAngleRoutine(140));

        List<Path.Waypoint> forwardDepotToRocketShip = new ArrayList<>();
        forwardDepotToRocketShip.add(new Waypoint(new Translation2d(-140 - kLevel1Width, 105), SPEED));
        forwardDepotToRocketShip.add(new Waypoint(new Translation2d(kRSX, kRSY - 15), 0));
        routines.add(new DrivePathRoutine(new Path(forwardDepotToRocketShip), false));

        //routines.add(new PlaceCargoRoutine(short));
        routines.add(new TimeoutRoutine(1)); //placeholder

        return new SequentialRoutine(routines);
    }

    public Routine takeHatch() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> backRocketShipToLoadingStation = new ArrayList<>();
        backRocketShipToLoadingStation.add(new Waypoint(new Translation2d(kCSX / 2, 103.5), SPEED));
        backRocketShipToLoadingStation.add(new Waypoint(new Translation2d(kHabLineX, 95.4), 0));
        routines.add(new DrivePathRoutine(new Path(backRocketShipToLoadingStation), true));

        routines.add(new CascadingGyroEncoderTurnAngleRoutine(-120));

        List<Path.Waypoint> forwardRocketShipToLoadingStation = new ArrayList<>();
        forwardRocketShipToLoadingStation.add(new Waypoint(new Translation2d(kLSX - Constants.kRobotLengthInches, kLSY - Constants.kRobotLengthInches + 15), SPEED));
        forwardRocketShipToLoadingStation.add(new Waypoint(new Translation2d(kLSX, kLSY), 0));
        routines.add(new DrivePathRoutine(new Path(forwardRocketShipToLoadingStation), false));

        return new SequentialRoutine(routines);
    }

    public Routine placeHatch2() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> backLoadingStationToRocketShip = new ArrayList<>();
        backLoadingStationToRocketShip.add(new Waypoint(new Translation2d(kLSX - Constants.kRobotLengthInches, kLSY - Constants.kRobotLengthInches + 15), SPEED));
        backLoadingStationToRocketShip.add(new Waypoint(new Translation2d(kHabLineX, 95.4), 0));
        routines.add(new DrivePathRoutine(new Path(backLoadingStationToRocketShip), true));

        routines.add(new CascadingGyroEncoderTurnAngleRoutine(120));

        List<Path.Waypoint> forwardLoadingStationToRocketShip = new ArrayList<>();
        forwardLoadingStationToRocketShip.add(new Waypoint(new Translation2d(kCSX / 2, 103.5), SPEED)); //line up with RS
        forwardLoadingStationToRocketShip.add(new Waypoint(new Translation2d(kRSX, kRSY + 15), 0));
        routines.add(new DrivePathRoutine(new Path(forwardLoadingStationToRocketShip), false));

        return new SequentialRoutine(routines);
    }

    @Override
    public String getKey() {
        return mAlliance.toString();
    }
}






