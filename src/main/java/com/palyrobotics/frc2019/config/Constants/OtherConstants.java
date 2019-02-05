package com.palyrobotics.frc2019.config.Constants;

public class OtherConstants {
    public enum RobotName {
        VIDAR
    }

    public enum DriverName {
        BRYAN
    }

    public enum OperatorName {
        GRIFFIN
    }

    public enum FieldName {
        //we goin to cmp bois <- for sure :send-it:
        TEAM_8, TEAM_254, SVR, SVR_PRACTICE, GNR, GNR_PRACTICE, DMR, DMR_PRACTICE, CMP, CMP_PRACTICE
    }

    public static boolean operatorXBoxController = true;

    /**
     * Initialization constants
     */
    public static final RobotName kRobotName = RobotName.VIDAR;
    public static final DriverName kDriverName = DriverName.BRYAN;
    public static final OperatorName kOperatorName = OperatorName.GRIFFIN;
    public static final FieldName kFieldName = FieldName.TEAM_8;

    // Time Constants (these might exist elsewhere but whatever)
    public static final double updatesPerSecond = 50;
    public static final double deltaTime = 1/ updatesPerSecond;

    /**
     * Physical field constants
     */
    public static double kUpperPlatformLength =  48.0;
    public static double kLevel1Width = 150.0;
    public static double kLevel2Width = 40.0;
    public static double kLevel3Width = 48.0;
    public static double kLowerPlatformLength = 48.0;
    public static double kCargoLineGap = 21.5;

    /**
     * Physical robot Constants
     */
    public static final double kRobotWidthInches = 34.0;
    public static final double kRobotLengthInches = 39.0;
    public static final double kCenterOfRotationOffsetFromFrontInches = 13.0;

    public static double kGroundToCarriageInches = 10.5625; // Distance from ground to the bottom of the carriage in the lowest position
    public static double kCarriageToHatchCenterInches = 6.5625; // Distance from the bottom of the carriage to the center of the hatch expel
    public static double kCarriageToCargoCenterInches = 17.1744; // Distance from the bottom of the carriage to the center of the cargo expel

    /**
     * Ultrasonics
     */
    public static final int kLeftUltrasonicPing = 0;
    public static final int kLeftUltrasonicEcho = 1;
    public static final int kRightUltrasonicPing = 2;
    public static final int kRightUltrasonicEcho = 3;
    public static final double kIntakeCloseRoutineCloseNow = 5.8;
    public static final int kRequiredUltrasonicCount = 6;
    public static final int kVidarPusherRightUltrasonicPing = 0;
    public static final int kVidarPusherRightUltrasonicEcho = 0;
    public static final int kVidarPusherLeftUltrasonicPing = 0;
    public static final int kVidarPusherLeftUltrasonicEcho = 0;

    //!!! Physical constants
    public static final double kSquareCubeLength = 13.0;
}
