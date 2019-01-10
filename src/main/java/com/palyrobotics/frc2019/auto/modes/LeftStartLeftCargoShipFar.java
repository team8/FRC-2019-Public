package com.palyrobotics.frc2019.auto.modes;

import com.palyrobotics.frc2019.auto.AutoModeBase;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.SequentialRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DrivePathRoutine;
import com.palyrobotics.frc2019.behavior.routines.drive.DriveSensorResetRoutine;
import com.palyrobotics.frc2019.config.Constants;
import com.palyrobotics.frc2019.util.trajectory.Path;
import com.palyrobotics.frc2019.util.trajectory.Path.Waypoint;
import com.palyrobotics.frc2019.util.trajectory.Translation2d;

import java.util.ArrayList;

public class LeftStartLeftCargoShipFar extends AutoModeBase{
    float speed = 40;

    @Override
    public String toString() {
        return mAlliance + this.getClass().toString();
    }

    @Override
    public void prestart() {}

    @Override
    public String getKey() {
        return mAlliance.toString();
    }

    @Override
    public Routine getRoutine() {
        return new SequentialRoutine(new DriveSensorResetRoutine(2), goToCargo());
    }

    public Routine goToCargo(){
        ArrayList<Waypoint> path = new ArrayList<>();
        path.add(new Waypoint(new Translation2d(0,0), speed));
        path.add(new Waypoint(new Translation2d(-(mDistances.kLevel1CargoX + mDistances.kCargoOffsetX + Constants.kLowerPlatformLength + Constants.kCargoLineGap*2), (mDistances.kCargoRightY - mDistances.kFieldWidth / 2 + (Constants.kLevel2Width/2 + Constants.kLevel3Width)/2 - Constants.kRobotLengthInches * 2)), speed));
        path.add(new Waypoint(new Translation2d(-(mDistances.kLevel1CargoX + mDistances.kCargoOffsetX + Constants.kLowerPlatformLength + Constants.kCargoLineGap*2), (mDistances.kCargoRightY - mDistances.kFieldWidth / 2 + (Constants.kLevel2Width/2 + Constants.kLevel3Width)/2 - Constants.kRobotLengthInches * 1.1 - Constants.kRobotWidthInches/2)), 0));
        return new DrivePathRoutine(new Path(path), true);
    }
}
