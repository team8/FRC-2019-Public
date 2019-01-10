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

public class CenterStartCargoShipHatchAutoMode extends AutoModeBase { //starts at center (lvl 3) - robot will probably start on lvl 2
//    public static int SPEED = 100;
//    public static double kCSX = 220.25;
//    public static double kRightCSY = -21.75;
//    public static double kLSX = 0;
//    public static double kLSY = -135; //not completely accurate
//    public static double kHabLineX = 95.28;

    public static int SPEED = 90; //start from level 1
    public static double kLevel3Width = 47;
    public static double kCSX = 220.25 - kLevel3Width;
    public static double kRightCSY = -21.75;
    public static double kLSX = 0 - kLevel3Width;
    public static double kLSY = -135; //not completely accurate
    public static double kHabLineX = 95.28 - kLevel3Width;

    @Override
    public String toString() {
        return mAlliance + this.getClass().toString();
    }

    @Override
    public void prestart() {

    }

    @Override
    public Routine getRoutine() {
        return new SequentialRoutine(new DriveSensorResetRoutine(2), placeHatch(), takeHatch());
    }

    public Routine placeHatch() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> StartToCargoShip = new ArrayList<>();
        StartToCargoShip.add(new Waypoint(new Translation2d(kCSX * .75, kRightCSY), SPEED));
        StartToCargoShip.add(new Waypoint(new Translation2d(kCSX - Constants.kRobotLengthInches * 2, kRightCSY), SPEED * .8));
        StartToCargoShip.add(new Waypoint(new Translation2d(kCSX - (Constants.kRobotLengthInches + 10), kRightCSY), 0)); //might replace with drivetillhatchon routine + little far
        routines.add(new DrivePathRoutine(new Path(StartToCargoShip), false));

        //routines.add(new ReleaseHatchRoutine()); //routine not made yet
        routines.add(new TimeoutRoutine(1));

        return new SequentialRoutine(routines);
    }

    public Routine takeHatch() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> backCargoShipToLoadingStation = new ArrayList<>();
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(kCSX - Constants.kRobotLengthInches * 2, kRightCSY), SPEED));
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(kCSX / 2, kLSY / 2), SPEED));
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(kCSX / 2 + Constants.kRobotLengthInches, kLSY), SPEED));
        routines.add(new DrivePathRoutine(new Path(backCargoShipToLoadingStation), true));

        routines.add(new CascadingGyroEncoderTurnAngleRoutine(70));

        List<Path.Waypoint> forwardCargoShipToLoadingStation = new ArrayList<>();
        forwardCargoShipToLoadingStation.add(new Waypoint(new Translation2d(kHabLineX, kLSY), SPEED));
        forwardCargoShipToLoadingStation.add(new Waypoint(new Translation2d(kHabLineX / 2, kLSY), SPEED * .8));
        forwardCargoShipToLoadingStation.add(new Waypoint(new Translation2d(kLSX, kLSY), 0));
        routines.add(new DrivePathRoutine(new Path(forwardCargoShipToLoadingStation), false));

        return new SequentialRoutine(routines);
    }

    @Override
    public String getKey() {
        return mAlliance.toString();
    }
}


