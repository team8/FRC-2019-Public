package com.palyrobotics.frc2019.subsystems;

import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.Constants;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.robot.Robot;
import com.palyrobotics.frc2019.util.SynchronousPID;
import edu.wpi.first.wpilibj.DoubleSolenoid;

public class Fingers extends Subsystem {
    public static Fingers instance = new Fingers();

    public static Fingers getInstance() { return instance; }

    public static void resetInstance() {instance = new Fingers(); }

    private DoubleSolenoid.Value mOpenCloseValue = DoubleSolenoid.Value.kForward;
    private DoubleSolenoid.Value mExpelValue = DoubleSolenoid.Value.kReverse;

    private double target;
    private final double kTolerance;
    private SynchronousPID pusherPID;

    private double mVictorOutput = pusherPID.calculate(Constants.kVidarPusherDistanceIn);

    public enum PusherState {
        IN, MIDDLE, OUT
    }

    private PusherState mPusherState = PusherState.IN;


    /**
     * State of Fingers. OPEN = fingers open, etc
     */
    public enum FingersState {
        OPEN, CLOSE
    }

    private FingersState mOpenCloseState = FingersState.CLOSE;
    private FingersState mExpelState = FingersState.CLOSE;

    protected Fingers() {
        super("Fingers");
        kTolerance = Constants.kAcceptablePusherPositionError;
        pusherPID = new SynchronousPID(Constants.kVidarPusherPositionkP, Constants.kVidarPusherPositionkI, Constants.kVidarPusherPositionkD);
        pusherPID.setOutputRange(-1, 1);
    }

    @Override
    public void start() {
        mOpenCloseState = FingersState.CLOSE;
        mExpelState = FingersState.CLOSE;
        mPusherState = PusherState.IN;
    }

    @Override
    public void stop() {
        mOpenCloseState = FingersState.CLOSE;
        mExpelState = FingersState.CLOSE;
        mPusherState = PusherState.IN;
    }

    @Override
    public void update(Commands commands, RobotState robotState) {
        mOpenCloseState = commands.wantedFingersOpenCloseState;
        mExpelState = commands.wantedFingersExpelState;
        mPusherState = commands.wantedPusherState;

        switch(mOpenCloseState) {
            case OPEN:
                mOpenCloseValue = DoubleSolenoid.Value.kForward;
                break;
            case CLOSE:
                mOpenCloseValue = DoubleSolenoid.Value.kReverse;
                break;
        }

        switch(mExpelState) {
            case OPEN:
                mExpelValue = DoubleSolenoid.Value.kForward;
                break;
            case CLOSE:
                mExpelValue = DoubleSolenoid.Value.kReverse;
                break;
        }

        pusherPID.setSetpoint(robotState.pusherPosition);
        switch(mPusherState) {
            case IN:
                target = Constants.kVidarPusherDistanceIn;
                mVictorOutput = pusherPID.calculate(Constants.kVidarPusherDistanceIn);
                break;
            case MIDDLE:
                target = Constants.kVidarPusherDistanceMiddle;
                mVictorOutput = pusherPID.calculate(Constants.kVidarPusherDistanceMiddle);
                break;
            case OUT:
                target = Constants.kVidarPusherDistanceOut;
                mVictorOutput = pusherPID.calculate(Constants.kVidarPusherDistanceOut);
                break;
        }
    }

    public boolean onTarget() {
        return Math.abs((Robot.getRobotState().pusherPosition - target)) < kTolerance
                && Math.abs(Robot.getRobotState().pusherVelocity) < 0.05;
    }

    public DoubleSolenoid.Value getOpenCloseOutput() {
        return mOpenCloseValue;
    }

    public DoubleSolenoid.Value getExpelOutput() {
        return mExpelValue;
    }

    public PusherState getPusherState() {
        return mPusherState;
    }

    public double getPusherOutput() {
        return mVictorOutput;
    }

    @Override
    public String getStatus() {
        return "Fingers State: " + mOpenCloseState + "\nExpel State: " + mExpelState + "\nPusher State: " + mPusherState + "\nPusher output" + mVictorOutput;
    }
}
