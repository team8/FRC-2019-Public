package com.palyrobotics.frc2019.subsystems;

import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.config.constants.OtherConstants;
import com.palyrobotics.frc2019.config.subsystem.IntakeConfig;
import com.palyrobotics.frc2019.util.SparkMaxOutput;
import com.palyrobotics.frc2019.util.config.Configs;
import com.palyrobotics.frc2019.util.csvlogger.CSVWriter;
import edu.wpi.first.wpilibj.Timer;

public class Intake extends Subsystem {

    private static Intake sInstance = new Intake();

    public static Intake getInstance() {
        return sInstance;
    }

    private IntakeConfig mConfig = Configs.get(IntakeConfig.class);

    private SparkMaxOutput mOutput = new SparkMaxOutput();
    private double mTalonOutput, mRumbleLength;

    private Double mIntakeWantedAngle;
    private RobotState mRobotState;

    private boolean cachedCargoState;

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
        ZERO_VELOCITY,
        IDLE
    }

    public enum IntakeMacroState {
        STOWED, // Stowed at the start of the match
        GROUND_INTAKING, // Getting the cargo off the ground
        LIFTING, // Lifting the cargo into the intake
        DROPPING, // Dropping the cargo into the intake
        HOLDING_MID, // Moving the arm to the mid hold position and keeping it there
        DOWN,
        TUCK,
        HOLDING_ROCKET,
        INTAKING_ROCKET,
        EXPELLING_ROCKET,
        HOLDING,
        IDLE
    }

    private WheelState mWheelState;
    private UpDownState mUpDownState;
    private IntakeMacroState mMacroState;

    private final static double kRequiredCancelSeconds = 0.2;
    private double mLastIntakeQueueTime;

    protected Intake() {
        super("intake");
    }

    @Override
    public void reset() {
        mMacroState = IntakeMacroState.IDLE;
        mOutput = new SparkMaxOutput();
    }

    @Override
    public void update(Commands commands, RobotState robotState) {
        mRobotState = robotState;

        // The intake macro state has eight possible states.  Any state can be transferred to automatically or manually,
        // but some states need to set auxiliary variables, such as the queue times.

        if (commands.wantedIntakeState == IntakeMacroState.HOLDING_MID && mMacroState == IntakeMacroState.GROUND_INTAKING) {
            // note: this needs to be nested so that the if/else can be exited
            if (mLastIntakeQueueTime + kRequiredCancelSeconds < Timer.getFPGATimestamp()) {
                // move the intake back up from the ground
                mMacroState = IntakeMacroState.HOLDING_MID;
            }
        } else if (mMacroState == IntakeMacroState.GROUND_INTAKING && robotState.hasCargo) {
            mMacroState = IntakeMacroState.LIFTING;
            commands.wantedIntakeState = IntakeMacroState.LIFTING;
        } else if (commands.wantedIntakeState == IntakeMacroState.GROUND_INTAKING && mMacroState != IntakeMacroState.LIFTING) {
            mMacroState = IntakeMacroState.GROUND_INTAKING;
            mLastIntakeQueueTime = Timer.getFPGATimestamp();
        } else if (mMacroState == IntakeMacroState.LIFTING && intakeOnTarget()) {
            mMacroState = IntakeMacroState.DROPPING;
            commands.wantedIntakeState = IntakeMacroState.DROPPING;
        } else if (commands.wantedIntakeState == IntakeMacroState.DROPPING && robotState.hasPusherCargo) {
            mMacroState = IntakeMacroState.HOLDING_MID;
            commands.wantedIntakeState = IntakeMacroState.HOLDING_MID; // reset it
        } else if (mMacroState != IntakeMacroState.DROPPING
                && !(mMacroState == IntakeMacroState.GROUND_INTAKING && commands.wantedIntakeState == IntakeMacroState.HOLDING_ROCKET)) {
            mMacroState = commands.wantedIntakeState;
        }

//        System.out.println(mMacroState);
//        System.out.println(commands.wantedIntakeState);
//        System.out.println(robotState.intakeAngle);

        commands.hasCargo = robotState.hasCargo;

        // FEED FORWARD MODEL:
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
                mIntakeWantedAngle = mConfig.maxAngle - 2.5;
                break;
            case GROUND_INTAKING:
                mWheelState = WheelState.INTAKING;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.intakeAngle;
                break;
            case LIFTING:
                mWheelState = WheelState.SLOW;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.handOffAngle;
                break;
            case DROPPING:
                mWheelState = WheelState.DROPPING;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.handOffAngle;
                // todo: add some sort of timeout so this doesn't finish immediately
                break;
            case HOLDING_MID:
                mWheelState = WheelState.IDLE;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.holdAngle;
                break;
            case HOLDING_ROCKET:
            case TUCK:
                mWheelState = WheelState.SLOW;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.rocketExpelAngle;
                break;
            case INTAKING_ROCKET:
                mWheelState = WheelState.MEDIUM;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.rocketExpelAngle;
                break;
            case EXPELLING_ROCKET:
                mWheelState = WheelState.EXPELLING;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.rocketExpelAngle;
                break;
            case DOWN:
                mWheelState = WheelState.IDLE;
                mUpDownState = UpDownState.CUSTOM_ANGLE;
                mIntakeWantedAngle = mConfig.intakeAngle;
                break;
            case HOLDING:
                mWheelState = WheelState.IDLE;
                mUpDownState = UpDownState.ZERO_VELOCITY;
                break;
            default:
            case IDLE:
                mWheelState = WheelState.IDLE;
                mUpDownState = UpDownState.IDLE;
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
            default:
            case IDLE:
                mTalonOutput = 0.0;
                break;
        }

//        System.out.println(mMacroState);

        switch (mUpDownState) {
            case CUSTOM_ANGLE:
//                boolean
//                        inClosedLoopZone = mRobotState.intakeAngle >= IntakeConstants.kLowestAngle && mRobotState.intakeAngle <= IntakeConstants.kHighestAngle,
//                        wantedAngleInClosedLoopZone = mIntakeWantedAngle >= IntakeConstants.kLowestAngle && mIntakeWantedAngle <= IntakeConstants.kHighestAngle;
//                if (inClosedLoopZone || wantedAngleInClosedLoopZone) {
//                    mSparkOutput.setTargetPositionSmartMotion(mIntakeWantedAngle, IntakeConstants.kArmDegreesPerRevolution, arbitraryDemand);
//                } else {
//                    mSparkOutput.setIdle();
//                }
                mOutput.setTargetPositionSmartMotion(mIntakeWantedAngle, arbitraryDemand, mConfig.gains);
                break;
            case ZERO_VELOCITY:
                mOutput.setTargetSmartVelocity(0.0, arbitraryDemand, mConfig.holdGains);
            default:
            case IDLE:
                mIntakeWantedAngle = null;
                mOutput.setIdle();
                break;
        }

        if (!cachedCargoState && robotState.hasCargo) {
            mRumbleLength = 0.75;
        } else if (mRumbleLength <= 0) {
            mRumbleLength = -1;
        }

        cachedCargoState = robotState.hasCargo;

        CSVWriter.addData("intakeAngle", mRobotState.intakeAngle);
        CSVWriter.addData("intakeAppliedOut", mRobotState.intakeAppliedOutput);
        if (mIntakeWantedAngle != null) CSVWriter.addData("intakeWantedAngle", mIntakeWantedAngle);
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
}