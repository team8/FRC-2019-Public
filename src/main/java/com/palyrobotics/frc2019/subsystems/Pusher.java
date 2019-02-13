package com.palyrobotics.frc2019.subsystems;

import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.Constants.PusherConstants;
import com.palyrobotics.frc2019.config.Gains;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.robot.Robot;
import com.palyrobotics.frc2019.util.SparkMaxOutput;
import com.palyrobotics.frc2019.util.SynchronousPID;

public class Pusher extends Subsystem {

    private static Pusher instance = new Pusher();

    public static Pusher getInstance() {
        return instance;
    }

    public static void resetInstance() {
        instance = new Pusher();
    }

    private double target;
    private final double kTolerance;

    private SparkMaxOutput mOutput = new SparkMaxOutput();

    public enum PusherState {
        IN, MIDDLE, OUT
    }

    private PusherState mState = PusherState.IN;

    protected Pusher(){
        super("Pusher");
        kTolerance = PusherConstants.kAcceptablePositionError;
    }

    @Override
    public void start(){
        mState = PusherState.IN;
    }

    @Override
    public void stop(){
        mState = PusherState.IN;
    }

    @Override
    public void update(Commands commands, RobotState robotState) {
        mState = commands.wantedPusherInOutState;
        mOutput.setTargetPosition(robotState.pusherPosition, Gains.pusherPosition);
        commands.hasPusherCargo = robotState.hasPusherCargo;
        switch(mState) {
            case IN:
                target = PusherConstants.kVidarDistanceIn;
                mOutput.setTargetPosition(PusherConstants.kVidarDistanceIn * PusherConstants.kPusherRotationsPerInch, Gains.pusherPosition);
                break;
            case MIDDLE:
                target = PusherConstants.kVidarDistanceMiddle;
                mOutput.setTargetPosition(PusherConstants.kVidarDistanceMiddle * PusherConstants.kPusherRotationsPerInch, Gains.pusherPosition);
                break;
            case OUT:
                target = PusherConstants.kVidarDistanceOut;
                mOutput.setTargetPosition(PusherConstants.kVidarDistanceOut * PusherConstants.kPusherRotationsPerInch, Gains.pusherPosition);
                break;
        }

        mWriter.addData("pusherEncPosition", robotState.pusherEncPosition);
        mWriter.addData("pusherEncPositionInches", robotState.pusherEncPosition / PusherConstants.kPusherRotationsPerInch);
        mWriter.addData("pusherEncVelocity", robotState.pusherEncVelocity);
        mWriter.addData("pusherEncVelocityInchPerSec", robotState.pusherEncVelocity * PusherConstants.kPusherEncSpeedUnitConversion);
        mWriter.addData("pusherPotPosition", robotState.pusherPosition);
        mWriter.addData("pusherPotPositionInches", robotState.pusherPosition / PusherConstants.kTicksPerInch);
        mWriter.addData("pusherPotVelocity", robotState.pusherVelocity);
        mWriter.addData("pusherPotVelocity", robotState.pusherVelocity * PusherConstants.kPusherPotSpeedUnitConversion);
    }

    public boolean onTarget() {
        return Math.abs((Robot.getRobotState().pusherPosition - target)) < kTolerance
                && Math.abs(Robot.getRobotState().pusherVelocity) < 0.05;
    }

    public PusherState getPusherState() {
        return mState;
    }

    public SparkMaxOutput getPusherOutput() {
        return mOutput;
    }
    @Override
    public String getStatus() {
        return "Pusher State: " + mState + "\nPusher output" + mOutput.getSetpoint();
    }
}
