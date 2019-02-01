package com.palyrobotics.frc2019.subsystems;

import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.Constants;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.config.Gains;
import com.palyrobotics.frc2019.util.SparkMaxOutput;

import java.util.Optional;

/**
 * @author Justin and Jason
 */
public class Intake extends Subsystem {
    public static Intake instance = new Intake();

    public static Intake getInstance() {
        return instance;
    }

    public static void resetInstance() { instance = new Intake(); }

    private SparkMaxOutput mSparkOutput = new SparkMaxOutput();
    private double mVictorOutput;

    private boolean movingDown = false;

    private Optional<Double> mIntakeWantedPosition = Optional.empty();
    private RobotState mRobotState;

    private enum WheelState {
        INTAKING,
        IDLE,
        DROPPING
    }

    private enum UpDownState {
        HOLD, //Keeping arm position fixed
        UP,
        CLIMBING,
        MANUAL_POSITIONING, //Moving elevator with joystick
        CUSTOM_POSITIONING, //Moving elevator with a control loop
        IDLE
    }

    public enum IntakeMacroState {
        STOWED, // stowed at the start of the match
        GROUND_INTAKING, // Getting the cargo off the ground
        LIFTING, // lifting the cargo into the intake
        DROPPING, // dropping the cargo into the intkae
        HOLDING_MID // moving the arm to the mid hold position and keeping it there
    }

    private WheelState mWheelState = WheelState.IDLE;
    private UpDownState mUpDownState = UpDownState.UP;
    private IntakeMacroState mMacroState = IntakeMacroState.STOWED;

    private double lastIntakeQueueTime = 0;
    private final double requiredMSCancel = 200;

    private double lastDropQueueTme = 0;
    private final double requiredMSDrop = 100;

    protected Intake() {
        super("Intake");
        mWheelState = WheelState.IDLE;
        mUpDownState = UpDownState.UP;
    }

    @Override
    public void start() {
        mWheelState = WheelState.IDLE;
        mUpDownState = UpDownState.UP;
    }

    @Override
    public void stop() {
        mWheelState = WheelState.IDLE;
        mUpDownState = UpDownState.UP;
    }

    @Override
    public void update(Commands commands, RobotState robotState) {
        mRobotState = robotState;

        if (commands.wantedIntakeState == IntakeMacroState.GROUND_INTAKING && this.mMacroState == IntakeMacroState.GROUND_INTAKING
        && this.lastIntakeQueueTime + this.requiredMSCancel < System.currentTimeMillis()) {
            this.mMacroState = IntakeMacroState.HOLDING_MID;
        }

        if (commands.wantedIntakeState == IntakeMacroState.GROUND_INTAKING && commands.wantedIntakeState == IntakeMacroState.HOLDING_MID) {
            this.mMacroState = IntakeMacroState.GROUND_INTAKING;
            this.lastIntakeQueueTime = System.currentTimeMillis();
        }

        if (commands.wantedIntakeState == IntakeMacroState.GROUND_INTAKING && robotState.hasCargo) {
            this.mMacroState = IntakeMacroState.LIFTING;
        }

        if (commands.wantedIntakeState == IntakeMacroState.LIFTING && intakeOnTarget()) {
            this.mMacroState = IntakeMacroState.DROPPING;
            lastDropQueueTme = System.currentTimeMillis();
        }

        if (commands.wantedIntakeState == IntakeMacroState.DROPPING && System.currentTimeMillis() > (this.lastDropQueueTme + this.requiredMSDrop)) {
            this.mMacroState = IntakeMacroState.HOLDING_MID;
        }

        commands.hasCargo = robotState.hasCargo;

        double arb_ff = Constants.kIntakeGravityFF * Math.cos(robotState.intakeAngle)
                + Constants.kIntakeAccelComp * robotState.robotAccel * Math.sin(robotState.intakeAngle);

        switch (mMacroState) {
            case STOWED:
                mWheelState = WheelState.IDLE;
                mUpDownState = UpDownState.IDLE;
            case GROUND_INTAKING:
                mWheelState = WheelState.INTAKING;
                mUpDownState = UpDownState.CUSTOM_POSITIONING;
                mIntakeWantedPosition = Optional.of(Constants.kIntakeIntakingPosition);
            case LIFTING:
                mWheelState = WheelState.IDLE;
                mUpDownState = UpDownState.CUSTOM_POSITIONING;
                mIntakeWantedPosition = Optional.of(Constants.kIntakeHandoffPosition);
            case DROPPING:
                mWheelState = WheelState.DROPPING;
                mUpDownState = UpDownState.CUSTOM_POSITIONING;
                mIntakeWantedPosition = Optional.of(Constants.kIntakeHandoffPosition);
                // todo: add some sort of timeout so this doesn't finish immediately
            case HOLDING_MID:
                mWheelState = WheelState.IDLE;
                mUpDownState = UpDownState.CUSTOM_POSITIONING;
                mIntakeWantedPosition = Optional.of(Constants.kIntakeHoldingPosition);
        }


        switch(mWheelState) {
            case INTAKING:
                if(commands.customIntakeSpeed) {
                    mVictorOutput = robotState.operatorXboxControllerInput.leftTrigger;
                } else {
                    mVictorOutput = Constants.kIntakingMotorVelocity;
                }
            case IDLE:
                mVictorOutput = 0;
                break;
            case DROPPING:
                mVictorOutput = Constants.kIntakeDroppingVelocity;
                break;
        }

        switch(mUpDownState) {
            case HOLD:
                mSparkOutput.setGains(Gains.intakeHold);
                mSparkOutput.setTargetPosition(mIntakeWantedPosition.get());
                break;
            case UP:
                mSparkOutput.setGains(Gains.intakeUp);
                mSparkOutput.setTargetPosition(mIntakeWantedPosition.get(), -arb_ff);
                break;
            case CLIMBING:
                mSparkOutput.setGains(Gains.intakeClimbing);
                mSparkOutput.setTargetPosition(mIntakeWantedPosition.get());
                //subtract component of gravity
                break;
            case MANUAL_POSITIONING:
                mSparkOutput.setPercentOutput(0); //TODO: Fix this based on what control method wanted
                break;
            case CUSTOM_POSITIONING:
                if(!mIntakeWantedPosition.equals(Optional.of(commands.robotSetpoints.intakePositionSetpoint.get() * Constants.kIntakeTicksPerInch))) {
                    mIntakeWantedPosition = Optional.of(commands.robotSetpoints.intakePositionSetpoint.get() * Constants.kIntakeTicksPerInch);
                    if(mIntakeWantedPosition.get() >= robotState.intakeAngle) {
                        movingDown = false;
                    } else {
                        movingDown = true;
                    }
                }

                if (movingDown) {
                    mSparkOutput.setGains(Gains.intakeDownwards);
                    mSparkOutput.setTargetPosition(mIntakeWantedPosition.get(), arb_ff);
                } else {
                    mSparkOutput.setGains(Gains.intakePosition);
                    mSparkOutput.setTargetPosition(mIntakeWantedPosition.get(), arb_ff);
                }
                break;
            case IDLE:
                if(mIntakeWantedPosition.isPresent()) {
                    mIntakeWantedPosition = Optional.empty();
                }
                mSparkOutput.setPercentOutput(0.0);
                break;
        }

    }

    public WheelState getWheelState() {
        return mWheelState;
    }

    public UpDownState getUpDownState() {
        return mUpDownState;
    }

    public Optional<Double> getIntakeWantedPosition() { return mIntakeWantedPosition; }

    public SparkMaxOutput getSparkOutput() {
        return mSparkOutput;
    }

    public double getVictorOutput() { return mVictorOutput; }

    public boolean intakeOnTarget() {
        if(mMacroState != IntakeMacroState.LIFTING) {
            return false;
        }

        return (Math.abs(mIntakeWantedPosition.get() - mRobotState.intakeAngle) < Constants.kIntakeAcceptableAngularError)
                && (Math.abs(mRobotState.elevatorVelocity) < Constants.kIntakeAngularVelocityError);
    }

    @Override
    public String getStatus() {
        return "Intake State: " + mWheelState + "\nOutput Control Mode: " + mSparkOutput.getControlType() + "\nTalon Output: "
                + mSparkOutput.getSetpoint() + "\nUp Down Output: " + mUpDownState;
    }
}