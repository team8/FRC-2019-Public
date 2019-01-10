package com.palyrobotics.frc2018.auto.modes;

import com.palyrobotics.frc2018.auto.AutoModeBase;
import com.palyrobotics.frc2018.behavior.ParallelRoutine;
import com.palyrobotics.frc2018.behavior.Routine;
import com.palyrobotics.frc2018.behavior.SequentialRoutine;
import com.palyrobotics.frc2018.behavior.routines.TimeoutRoutine;
import com.palyrobotics.frc2018.behavior.routines.drive.CascadingGyroEncoderTurnAngleRoutine;
import com.palyrobotics.frc2018.behavior.routines.drive.DrivePathRoutine;
import com.palyrobotics.frc2018.behavior.routines.drive.DriveSensorResetRoutine;
import com.palyrobotics.frc2018.behavior.routines.intake.IntakeDownRoutine;
import com.palyrobotics.frc2018.config.Constants;
import com.palyrobotics.frc2018.util.trajectory.Path;
import com.palyrobotics.frc2018.util.trajectory.Path.Waypoint;
import com.palyrobotics.frc2018.util.trajectory.Translation2d;

import java.util.ArrayList;
import java.util.List;

public class CenterStartExp extends AutoModeBase { //starts at center (lvl 3) - robot will probably start on lvl 2

    public static int SPEED = 90; //start from level 1
    public static double kLevel3Width = -47;
    public static double kCSX = -220.25 - kLevel3Width;
    public static double kRightCSY = 21.75;
    public static double kLSX = 0 - kLevel3Width;
    public static double kLSY = 135; //not completely accurate
    public static double kHabLineX = -95.28 - kLevel3Width;
    public static double kRSX = -164 - kLevel3Width;
    public static double kRSY = 118.2;

    @Override
    public String toString() {
        return mAlliance + this.getClass().toString();
    }

    @Override
    public void prestart() {

    }

    @Override
    public Routine getRoutine() {
        return new SequentialRoutine(new DriveSensorResetRoutine(2), placeHatch1(), takeHatch(), placeHatch2());
    }

    public Routine placeHatch1() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> StartToCargoShip = new ArrayList<>();
        StartToCargoShip.add(new Waypoint(new Translation2d(kCSX / 2, kRightCSY), SPEED * .4));
        StartToCargoShip.add(new Waypoint(new Translation2d(kCSX + Constants.kRobotLengthInches * 2, kRightCSY), SPEED));
        StartToCargoShip.add(new Waypoint(new Translation2d(kCSX + (Constants.kRobotLengthInches + 20), kRightCSY), 0)); //might replace with drivetillhatchon routine + little far
        routines.add(new DrivePathRoutine(new Path(StartToCargoShip), true));

        //routines.add(new ReleaseHatchRoutine()); //routine not made yet
        routines.add(new TimeoutRoutine(1));

        return new SequentialRoutine(routines);
    }

    public Routine takeHatch() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> backCargoShipToLoadingStation = new ArrayList<>();
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(kCSX + Constants.kRobotLengthInches * 2, kRightCSY), SPEED));
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(kCSX / 2, kLSY / 2), SPEED));
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(kCSX / 2 + Constants.kRobotLengthInches, kLSY / 2 + Constants.kRobotLengthInches), SPEED));
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(kHabLineX + Constants.kRobotLengthInches, kLSY), SPEED * .8));
        backCargoShipToLoadingStation.add(new Waypoint(new Translation2d(kLSX - 10, kLSY), 0));
        routines.add(new DrivePathRoutine(new Path(backCargoShipToLoadingStation), false));

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
        forwardLoadingStationToRocketShip.add(new Waypoint(new Translation2d(kCSX / 2, 103.5), SPEED * .75)); //line up with RS
        forwardLoadingStationToRocketShip.add(new Waypoint(new Translation2d(kRSX, kRSY + 15), 0));
        routines.add(new DrivePathRoutine(new Path(forwardLoadingStationToRocketShip), false));

        return new SequentialRoutine(routines);
    }

    @Override
    public String getKey() {
        return mAlliance.toString();
    }
}


