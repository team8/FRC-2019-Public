package com.palyrobotics.frc2019.subsystems;

import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.config.constants.OtherConstants;
import com.palyrobotics.frc2019.config.subsystem.IntakeConfig;
import com.palyrobotics.frc2019.util.SparkMaxOutput;
import com.palyrobotics.frc2019.util.config.Configs;
import com.palyrobotics.frc2019.util.csvlogger.CSVWriter;

public class Intake extends Subsystem {

    private static Intake sInstance = new Intake();
    private IntakeConfig mConfig = Configs.get(IntakeConfig.class);
    private SparkMaxOutput mOutput = new SparkMaxOutput();
    private double mTalonOutput, mRumbleLength;
    private Double mIntakeWantedAngle;
    private RobotState mRobotState;
    private boolean cachedCargoState;
    private WheelState mWheelState;
    private UpDownState mUpDownState;
    private IntakeMacroState mMacroState;

    protected Intake() {
        super("intake");
    }

    public static Intake getInstance() {
        return sInstance;
    }

    @Override
    public void reset() {
        mMacroState = IntakeMacroState.HOLDING_CURRENT_ANGLE;
        mOutput = new SparkMaxOutput();
        mTalonOutput = 0.0;
    }

    @Override
    public void update(Commands commands, RobotState robotState) {
        mRobotState = robotState;

        // The intake macro state has eight possible states.  Any state can be transferred to automatically or manually,
        // but some states need to set auxiliary variables, such as the queue times.

        switch (mMacroState) {
            case STOWED:
            case HOLDING_CURRENT_ANGLE:
            case INTAKING_CARGO:
            case EXPELLING_CARGO:
            case LIFTING_FROM_GROUND_INTAKE:
            case HOLDING_OUT_OF_WAY:
                mMacroState = commands.wantedIntakeState; // Allow us to do any state from here
                break;
            case HOLDING_CARGO:
                if (commands.wantedIntakeState == IntakeMacroState.HOLDING_OUT_OF_WAY) {
                    mMacroState = commands.wantedIntakeState = IntakeMacroState.LIFTING_FROM_GROUND_INTAKE;
                } else {
                    mMacroState = commands.wantedIntakeState;
                }
                break;
            case DOWN_FOR_GROUND_INTAKE:
                boolean isCloseEnoughToGround = Math.abs(mIntakeWantedAngle - mRobotState.intakeAngle) < mConfig.acceptableAngularError * 3.0;
                if (isCloseEnoughToGround) { // We are all the way on the ground and should start the intake wheels
                    mMacroState = commands.wantedIntakeState = IntakeMacroState.GROUND_INTAKE;
                } else if (commands.wantedIntakeState == IntakeMacroState.HOLDING_OUT_OF_WAY
                        || commands.wantedIntakeState == IntakeMacroState.HOLDING_CARGO) { // Cancel the intake and go back into stowed position
                    mMacroState = commands.wantedIntakeState = IntakeMacroState.STOWED;
                }
                break;
            case GROUND_INTAKE:
                if (robotState.hasIntakeCargo) { // Test if ball is all the way in carriage and stop spinning intake wheels
                    mMacroState = commands.wantedIntakeState = IntakeMacroState.LIFTING_FROM_GROUND_INTAKE;
                } else if (commands.wantedIntakeState == IntakeMacroState.HOLDING_OUT_OF_WAY
                        || commands.wantedIntakeState == IntakeMacroState.HOLDING_CARGO
                        || commands.wantedIntakeState == IntakeMacroState.DOWN_FOR_GROUND_INTAKE) { // Cancel the intake and go back into stowed position
                    mMacroState = commands.wantedIntakeState = IntakeMacroState.STOWED;
                }
                break;
            case DROPPING_INTO_CARRIAGE:
                if (robotState.hasPusherCargoFar) { // Move arm out of the way for the pusher if it is secured in the carriage
                    mMacroState = commands.wantedIntakeState = IntakeMacroState.HOLDING_OUT_OF_WAY;
                } else if (commands.wantedIntakeState == IntakeMacroState.HOLDING_OUT_OF_WAY
                        || commands.wantedIntakeState == IntakeMacroState.DOWN_FOR_GROUND_INTAKE) { // Cancel the hand-off and move arm to holding mid
                    mMacroState = commands.wantedIntakeState;
                }
                break;
        }

        // Feed Forward Model:
        // 1. Compensate for gravity on the CM.
        // 2. Compensate for robot acceleration.  Derivation is similar to that for an inverted pendulum,
        // and can be found on slack.
        // 3. Compensate for centripetal acceleration on the arm.
        double arbitraryDemand = mConfig.gravityFF * Math.cos(Math.toRadians(robotState.intakeAngle - mConfig.angleOffset))
                + mConfig.accelerationCompensation * robotState.robotAcceleration * Math.sin(Math.toRadians(robotState.intakeAngle - mConfig.angleOffset))
                + mConfig.centripetalCoefficient * robotState.drivePose.headingVelocity * robotState.drivePose.headingVelocity * Math.sin(Math.toRadians(robotState.intakeAngle - mConfig.angleOffset));

        switch (mMacroState) {
            case STOWED:
                mWheelState = WheelState.IDLE;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.handOffAngle;
                break;
            case GROUND_INTAKE:
                mWheelState = WheelState.INTAKING;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.intakeAngle;
                break;
            case LIFTING_FROM_GROUND_INTAKE:
                mWheelState = WheelState.SLOW;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.handOffAngle;
                break;
            case DROPPING_INTO_CARRIAGE:
                mWheelState = WheelState.DROPPING;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.handOffAngle;
                break;
            case HOLDING_OUT_OF_WAY:
                mWheelState = WheelState.IDLE;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.holdAngle;
                break;
            case HOLDING_CARGO:
                mWheelState = WheelState.SLOW;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.rocketExpelAngle;
                break;
            case INTAKING_CARGO:
                mWheelState = WheelState.MEDIUM;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.rocketExpelAngle;
                break;
            case EXPELLING_CARGO:
                mWheelState = WheelState.EXPELLING;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.rocketExpelAngle;
                break;
            case DOWN_FOR_GROUND_INTAKE:
                mWheelState = WheelState.IDLE;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.intakeAngle;
                break;
            case HOLDING_CURRENT_ANGLE:
                mWheelState = WheelState.IDLE;
                mUpDownState = UpDownState.ZERO_VELOCITY;
                break;
        }

        switch (mWheelState) {
            case INTAKING:
                mTalonOutput = mConfig.motorVelocity;
                break;
            case DROPPING:
                mTalonOutput = mConfig.droppingVelocity;
                break;
            case EXPELLING:
                mTalonOutput = mConfig.expellingVelocity;
                break;
            case SLOW:
                mTalonOutput = mConfig.verySlowly;
                break;
            case MEDIUM:
                mTalonOutput = mConfig.medium;
                break;
            case IDLE:
                mTalonOutput = 0.0;
                break;
        }

        switch (mUpDownState) {
            case CUSTOM_ANGLE:
                mOutput.setTargetPositionSmartMotion(mIntakeWantedAngle, arbitraryDemand, mConfig.gains);
                break;
            case ZERO_VELOCITY:
                mOutput.setTargetSmartVelocity(0.0, arbitraryDemand, mConfig.holdGains);
        }

        if (!cachedCargoState && robotState.hasIntakeCargo) {
            mRumbleLength = 0.75;
        } else if (mRumbleLength <= 0) {
            mRumbleLength = -1;
        }

        cachedCargoState = robotState.hasIntakeCargo;

        CSVWriter.addData("intakeAngle", mRobotState.intakeAngle);
        CSVWriter.addData("intakeAppliedOut", mRobotState.intakeAppliedOutput);
        if (mIntakeWantedAngle != null) {
            CSVWriter.addData("intakeWantedAngle", mIntakeWantedAngle);
        }
        CSVWriter.addData("intakeTargetAngle", mOutput.getReference());
    }

    public double getRumbleLength() {
        return mRumbleLength;
    }

    public void decreaseRumbleLength() {
        mRumbleLength -= OtherConstants.deltaTime;
    }

    public SparkMaxOutput getSparkOutput() {
        return mOutput;
    }

    public double getTalonOutput() {
        return mTalonOutput;
    }

    private boolean intakeOnTarget() {
        return mIntakeWantedAngle != null
                && (Math.abs(mIntakeWantedAngle - mRobotState.intakeAngle) < mConfig.acceptableAngularError)
                && (Math.abs(mRobotState.intakeVelocity) < mConfig.angularVelocityError);
    }

    @Override
    public String getStatus() {
        return String.format("Intake State: %s%nOutput Control Mode: %s%nSpark Output: %.2f%nUp Down Output: %s", mWheelState, mOutput.getControlType(), mOutput.getReference(), mUpDownState);
    }

    private enum WheelState {
        INTAKING,
        IDLE,
        EXPELLING,
        SLOW,
        MEDIUM,
        DROPPING
    }

    private enum UpDownState {
        CUSTOM_ANGLE,
        ZERO_VELOCITY
    }

    public enum IntakeMacroState {
        STOWED, // Stowed at the start of the match
        DOWN_FOR_GROUND_INTAKE,
        GROUND_INTAKE, // Getting the cargo off the ground
        LIFTING_FROM_GROUND_INTAKE, // Lifting the cargo into the intake
        DROPPING_INTO_CARRIAGE, // Dropping the cargo into the pusher carriage
        HOLDING_OUT_OF_WAY, // Hold out of the way of the pusher
        HOLDING_CARGO,
        INTAKING_CARGO,
        EXPELLING_CARGO,
        HOLDING_CURRENT_ANGLE
    }
}