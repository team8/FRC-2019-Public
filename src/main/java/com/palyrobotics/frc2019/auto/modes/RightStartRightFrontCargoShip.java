package com.palyrobotics.frc2019.auto.modes;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.TimeoutRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DriveSensorResetRoutine;
import com.palyrobotics.frc2019.config.Constants;
import com.palyrobotics.frc2019.util.trajectory.Path;
import com.palyrobotics.frc2019.util.trajectory.Translation2d;

import java.util.ArrayList;
import java.util.List;

public class RightStartRightFrontCargoShip extends AutoModeBase { //starts at center (lvl 3) - robot will probably start on lvl 2
//    public static int SPEED = 100;
//    public static double kCSX = 220.25;
//    public static double kRightCSY = -21.75;
//    public static double kLSX = 0;
//    public static double kLSY = -135; //not completely accurate
//    public static double kHabLineX = 95.28;

    public static int SPEED = 60;
    public static double kOffsetY = Constants.kLevel3Width * .5 + Constants.kLevel2Width * .5;
//    public static double kOffsetX = Constants.kLowerPlatformLength;

    @Override
    public String toString() {
        return mAlliance + this.getClass().toString();
    }

    @Override
    public void prestart() {

    }

    @Override
    public Routine getRoutine() {
        return new SequentialRoutine(new DriveSensorResetRoutine(2), placeHatch());
    }

    public Routine placeHatch() {
        ArrayList<Routine> routines = new ArrayList<>();

        List<Path.Waypoint> StartToCargoShip = new ArrayList<>();
        StartToCargoShip.add(new Path.Waypoint(new Translation2d(-(Constants.kUpperPlatformLength + Constants.kLowerPlatformLength + Constants.kRobotLengthInches), 0), SPEED * .75));
        StartToCargoShip.add(new Path.Waypoint(new Translation2d(-(mDistances.kLevel1CargoX + Constants.kLowerPlatformLength + Constants.kUpperPlatformLength - Constants.kRobotLengthInches * 2) * .75, -(-(mDistances.kFieldWidth * .5 - (mDistances.kCargoRightY + mDistances.kCargoOffsetY)) + kOffsetY)), SPEED));
        StartToCargoShip.add(new Path.Waypoint(new Translation2d(-(mDistances.kLevel1CargoX + Constants.kLowerPlatformLength + Constants.kUpperPlatformLength - Constants.kRobotLengthInches * 2), -(-(mDistances.kFieldWidth * .5 - (mDistances.kCargoRightY + mDistances.kCargoOffsetY)) + kOffsetY)), 0));
        routines.add(new DrivePathRoutine(new Path(StartToCargoShip), true));

        //routines.add(new ReleaseHatchRoutine()); //routine not made yet
        routines.add(new TimeoutRoutine(1));

        return new SequentialRoutine(routines);
    }

    @Override
    public String getKey() {
        return mAlliance.toString();
    }
}


