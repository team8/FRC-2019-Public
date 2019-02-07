package com.palyrobotics.frc2019.util;

import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.Constants;
import com.palyrobotics.frc2019.config.Gains;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.vision.Limelight;

/**
 * CheesyDriveHelper implements the calculations used in CheesyDrive for teleop control. Returns a DriveSignal for the motor output
 */
public class VisionDriveHelper {
	private double mOldWheel, mQuickStopAccumulator;
	private boolean mInitialBrake;
	private double mOldThrottle = 0.0, mBrakeRate;
	private boolean found = false;
	private SynchronousPID pidController;
	private SynchronousPID visionPidController;

	public DriveSignal visionDrive(Commands commands, RobotState robotState) {
		double throttle = -robotState.leftStickInput.getY();

		//Braking if left trigger is pressed
		boolean isBraking = robotState.leftStickInput.getTriggerPressed();

		double wheelNonLinearity;

		throttle = ChezyMath.handleDeadband(throttle, Constants.kDeadband);

		wheelNonLinearity = 0.5;

		double leftPower, rightPower, overPower;
		double sensitivity;

		double angularPower;

		//linear power is what's actually sent to motor, throttle is input
		double linearPower = remapThrottle(throttle);

		sensitivity = Constants.kDriveSensitivity;

		//Handle braking
		if(isBraking) {
			//Set up braking rates for linear deceleration in a set amount of time
			if(mInitialBrake) {
				mInitialBrake = false;
				//Old throttle initially set to throttle
				mOldThrottle = linearPower;
				//Braking rate set
				mBrakeRate = mOldThrottle / Constants.kCyclesUntilStop;
			}

			//If braking is not complete, decrease by the brake rate
			if(Math.abs(mOldThrottle) >= Math.abs(mBrakeRate)) {
				//reduce throttle
				mOldThrottle -= mBrakeRate;
				linearPower = mOldThrottle;
			} else {
				linearPower = 0;
			}
		} else {
			mInitialBrake = true;
		}

//		//Quickturn
//		if(isQuickTurn) {
//			if(Math.abs(robotState.rightStickInput.getX()) < Constants.kQuickTurnSensitivityThreshold) {
//				sensitivity = Constants.kPreciseQuickTurnSensitivity;
//			} else {
//				sensitivity = Constants.kQuickTurnSensitivity;
//			}
//
//			angularPower = wheel * sensitivity;
//
//			//Can be tuned
//			double alpha = Constants.kAlpha;
//			mQuickStopAccumulator = (1 - alpha) * mQuickStopAccumulator + alpha * angularPower * 6.5;
//
//			overPower = 1.0;
//		} else {
//			overPower = 0.0;
//
//			//Sets turn amount
//			angularPower = Math.abs(throttle) * wheel * sensitivity - mQuickStopAccumulator;
//
//			if(mQuickStopAccumulator > Constants.kQuickStopAccumulatorDecreaseThreshold) {
//				mQuickStopAccumulator -= Constants.kQuickStopAccumulatorDecreaseRate;
//			} else if(mQuickStopAccumulator < -Constants.kQuickStopAccumulatorDecreaseThreshold) {
//				mQuickStopAccumulator += Constants.kQuickStopAccumulatorDecreaseRate;
//			} else {
//				mQuickStopAccumulator = 0.0;
//			}
//		}

//		double aspectRatio = 0;
		if(Limelight.getInstance().isTargetFound() && Limelight.getInstance().getCornerX().length == 4) {
//			Old bangbang code
//			double tx = Limelight.getInstance().getdegRotationToTarget();
//			angularPower = (tx > 0) ? -0.2 : 0.2;

//			int target = Limelight.getInstance().getTarget();
			int target = 0;

			// Is this the first time detecting the target?
			if (!found){
				Gains turnGains = new Gains(0.5/Limelight.getInstance().getCorrectedEstimatedDistanceZ(), 0, 0, 0, 200, 0);
				pidController = new SynchronousPID(turnGains.P, turnGains.I, turnGains.D, turnGains.izone);
				pidController.setOutputRange(-0.2,0.2);

				pidController.setSetpoint(0);
				found = true;

//				Gains visionTargetGains = new Gains(.09375, 0, 0, 0, 200, 0);
//				visionPidController = new SynchronousPID(visionTargetGains.P, visionTargetGains.I, visionTargetGains.D, visionTargetGains.izone);
//				visionPidController.setOutputRange(-10,10);
//				visionPidController.setSetpoint(0);
			}
			pidController.setPID( 0.5/Limelight.getInstance().getCorrectedEstimatedDistanceZ(), 0, 0);
//			if(Limelight.getInstance().getYawToTarget() )
//			aspectRatio = Limelight.getInstance().getTargetAspectRatio();
//			Constants
//			System.out.println("Aspect:" + aspectRatio);
//			System.out.println("Rotation:" + robotState.drivePose.heading);
//			System.out.println("Skew:" + Limelight.getInstance().getSkew());
//			angularPower = 0;
//			System.out.println("Error: " + (robotState.drivePose.heading - Limelight.getInstance().getYawToTarget() - Constants.visionTargets[target].angle));
//			System.out.println("Heading: " + robotState.drivePose.heading);
//			System.out.println("tx: " + Limelight.getInstance().getYawToTarget());
//			double angleOffset = robotState.drivePose.heading - Limelight.getInstance().getYawToTarget() - Constants.visionTargets[target].angle;
//			System.out.println("Real: " + angleOffset);
//			double[] rotation = Limelight.getInstance().getEstimatedTargetAngle();
//			if(rotation != null) {
//				angleOffset = rotation[1];
//				System.out.println("SolvePnP: " + angleOffset);
//
////			angleOffset = Math.log(angleOffset * 5 + 1);
//				double L = 10;
//				double k = 1;
//				double x = 10;
//				angleOffset = -L / (1 + Math.exp(-k * (Math.abs(angleOffset) - x)));
//				angleOffset *= (angleOffset < 0) ? -1 : 1;
//				System.out.println("Go to angle: " + angleOffset);
//				angularPower = pidController.calculate(Limelight.getInstance().getYawToTarget()
//						- angleOffset);
//			} else {
//				found = false;
//				angularPower = 0;
//			}
			angularPower = pidController.calculate(Limelight.getInstance().getYawToTarget());
		} else {
			found = false;
			angularPower = 0;
		}

//		System.out.println("angular: " + angularPower);

		rightPower = leftPower = mOldThrottle = linearPower;
		leftPower += angularPower;
		rightPower -= angularPower;
		System.out.println(angularPower);
//		System.out.println("linear: " + linearPower);
//		System.out.println("left: " + leftPower);
//		System.out.println("right: " + rightPower);

		if(leftPower > 1.0) {
//			rightPower -= overPower * (leftPower - 1.0);
			leftPower = 1.0;
		} else if(rightPower > 1.0) {
//			leftPower -= overPower * (rightPower - 1.0);
			rightPower = 1.0;
		} else if(leftPower < -1.0) {
//			rightPower += overPower * (-1.0 - leftPower);
			leftPower = -1.0;
		} else if(rightPower < -1.0) {
//			leftPower += overPower * (-1.0 - rightPower);
			rightPower = -1.0;
		}

		DriveSignal mSignal = DriveSignal.getNeutralSignal();

		mSignal.leftMotor.setPercentOutput(leftPower);
		mSignal.rightMotor.setPercentOutput(rightPower);
		return mSignal;
	}

	/**
	 * Throttle tuning functions
	 */
	public double remapThrottle(double initialThrottle) {
		double x = Math.abs(initialThrottle);
		switch(Constants.kDriverName) {
			case BRYAN:
				//Reversal of directions
				//Stick a 0 cycle in between
				if(initialThrottle * mOldThrottle < 0) {
					return 0.0;
				}

				//Increase in magnitude, deceleration is fine. This misses rapid direction switches, but that's up to driver
				if(x > Math.abs(mOldThrottle)) {
					x = mOldThrottle + Math.signum(initialThrottle) * Constants.kMaxAccelRate;
				} else {
					x = initialThrottle;
				}

//				x = initialThrottle;
				break;
		}
		return x;
	}

	/**
	 * Limits the given input to the given magnitude.
	 */
	public double limit(double v, double limit) {
		return (Math.abs(v) < limit) ? v : limit * (v < 0 ? -1 : 1);
	}
}