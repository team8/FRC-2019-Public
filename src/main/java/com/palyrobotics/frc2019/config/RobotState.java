package com.palyrobotics.frc2019.config;

import com.palyrobotics.frc2019.util.JoystickInput;
import com.palyrobotics.frc2019.util.Pose;
import com.palyrobotics.frc2019.util.XboxInput;
import com.palyrobotics.frc2019.util.trajectory.*;

import java.util.ArrayList;
import java.util.Map;

/**
 * Holds all hardware input, such as sensors. <br />
 * Can be simulated
 *
 * @author Nihar
 */
public class RobotState {

    public enum GamePeriod {
        AUTO, TELEOP, DISABLED
    }

    private static RobotState instance = new RobotState();

    public double matchStartTimeSeconds;

    public static RobotState getInstance() {
        return instance;
    }

    protected RobotState() {
    }

    // Updated by autoInit, teleopInit, disabledInit
    public GamePeriod gamePeriod = GamePeriod.DISABLED;

    public double robotVelocity, robotAcceleration;

    public boolean isQuickTurning;

    public boolean cancelAuto;

    // Intake
    public boolean hasCargo;
    public double cargoDistance;
    public double intakeStartAngle;  // Angle in degrees
    public double intakeAngle;  // Angle in degrees
    public double intakeAppliedOutput;
    public double intakeVelocity; // RPM
    public ArrayList<Double>
            leftReadings = new ArrayList<>(), // TODO make queue?
            rightReadings = new ArrayList<>();

    // Pusher
    public boolean hasPusherCargo, hasPusherCargoFar;

    public double cargoPusherDistance;
    public ArrayList<Double> mPusherReadings = new ArrayList<>();

    // Hatch Intake
    public boolean hasHatch, hatchIntakeUp = true;

    // Tracks total current from kPDP
    public double shovelCurrentDraw = 0;

    // Pose stores drivetrain sensor data
    public Pose drivePose = new Pose();

    // Pusher sensor data
    public double pusherPosition, pusherVelocity, pusherAppliedOutput;

    // Elevator sensor data
    public double elevatorPosition, elevatorVelocity, elevatorAppliedOutput;

    // Vision drive data
    public boolean atVisionTargetThreshold;

    //Robot position
    private final int kObservationBufferSize = 100;

    //FPGATimestamp -> RigidTransform2d or Rotation2d
    private RigidTransform2d.Delta vehicleVelocity;
    private InterpolatingTreeMap<InterpolatingDouble, RigidTransform2d> fieldToVehicle;

    // Joystick input
    public JoystickInput leftStickInput = new JoystickInput(), rightStickInput = new JoystickInput();
	public XboxInput operatorXboxControllerInput = new XboxInput();
    public JoystickInput operatorJoystickInput = new JoystickInput();

    public synchronized void reset(double startTime, RigidTransform2d initialFieldToVehicle) {
        fieldToVehicle = new InterpolatingTreeMap<>(kObservationBufferSize);
        fieldToVehicle.put(new InterpolatingDouble(startTime), initialFieldToVehicle);
        vehicleVelocity = new RigidTransform2d.Delta(0, 0, 0);
    }

    public synchronized RigidTransform2d getFieldToVehicle(double timestamp) {
        return fieldToVehicle.getInterpolated(new InterpolatingDouble(timestamp));
    }

    public synchronized Map.Entry<InterpolatingDouble, RigidTransform2d> getLatestFieldToVehicle() {
        return fieldToVehicle.lastEntry();
    }

    public synchronized RigidTransform2d getPredictedFieldToVehicle(double lookAheadTime) {
        return getLatestFieldToVehicle().getValue().transformBy(
                RigidTransform2d.fromVelocity(new RigidTransform2d.Delta(vehicleVelocity.dx * lookAheadTime, vehicleVelocity.dy * lookAheadTime, vehicleVelocity.dtheta * lookAheadTime)));
    }

    public synchronized void addFieldToVehicleObservation(double timestamp, RigidTransform2d observation) {
        fieldToVehicle.put(new InterpolatingDouble(timestamp), observation);
    }

    public synchronized void addObservations(double timestamp, RigidTransform2d fieldToVehicle, RigidTransform2d.Delta velocity) {
        addFieldToVehicleObservation(timestamp, fieldToVehicle);
        vehicleVelocity = velocity;
    }

    public RigidTransform2d generateOdometryFromSensors(double leftEncoderDelta, double rightEncoderDelta, Rotation2d gyroAngle) {
        RigidTransform2d lastMeasurement = getLatestFieldToVehicle().getValue();
        return Kinematics.integrateForwardKinematics(lastMeasurement, leftEncoderDelta, rightEncoderDelta, gyroAngle);
    }

    public int getNumObservations() {
        return fieldToVehicle.size();
    }
}
