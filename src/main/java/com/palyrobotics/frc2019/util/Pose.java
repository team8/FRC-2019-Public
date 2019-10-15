package com.palyrobotics.frc2019.util;

/**
 * Represents the translational and rotational state of the robot.
 */
public class Pose {
	public double heading;
	public double lastHeading;
	public double headingVelocity;

	public double leftEncoderPosition, lastLeftEncoderPosition;
	public double leftEncoderVelocity, lastRightEncoderPosition;

	public double rightEncoderPosition;
	public double rightEncoderVelocity;

	public void copyTo(Pose other) {
		other.heading = this.heading;
		other.lastHeading = this.lastHeading;
		other.headingVelocity = this.headingVelocity;
		other.leftEncoderPosition = this.leftEncoderPosition;
		other.leftEncoderVelocity = this.leftEncoderVelocity;
		other.rightEncoderPosition = this.rightEncoderPosition;
		other.rightEncoderVelocity = this.rightEncoderVelocity;
	}
}