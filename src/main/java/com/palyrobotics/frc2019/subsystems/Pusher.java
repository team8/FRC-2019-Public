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

    private SparkMaxOutput mOutput = new SparkMaxOutput();

    public enum PusherState {
        IN, MIDDLE, OUT, COMPRESSION
    }

    private PusherState mState = PusherState.IN;

    protected Pusher(){
        super("Pusher");
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
        if(commands.elevatorMoving) {
            mState = PusherState.IN;
            System.out.println("Running");
        } else {
            mState = commands.wantedPusherInOutState;
        }
        commands.hasPusherCargo = robotState.hasPusherCargo;
        System.out.println(robotState.pusherPosition);
        switch(mState) {
            case IN:
                target = PusherConstants.kVidarDistanceIn;
                mOutput.setTargetPosition(target, Gains.pusherPosition);
                break;
            case COMPRESSION:
                target = PusherConstants.kVidarDistanceCompress;
                mOutput.setTargetPosition(target, Gains.pusherPosition);
            case MIDDLE:
                target = PusherConstants.kVidarDistanceMiddle;
                mOutput.setTargetPosition(target, Gains.pusherPosition);
                break;
            case OUT:
                target = PusherConstants.kVidarDistanceOut;
                mOutput.setTargetPosition(target, Gains.pusherPosition);
                break;
        }

        if (onTarget()) {
            mOutput.setPercentOutput(0.0);
            System.out.println("On target");
        }

        System.out.println("Target: " + target);
        mOutput.setTargetPosition(target, Gains.pusherPosition);

        mWriter.addData("pusherPos", robotState.pusherPosition);
        mWriter.addData("pusherEncVelocity", robotState.pusherEncVelocity);
        mWriter.addData("pusherPotPosition", robotState.pusherPosition);
        mWriter.addData("pusherPotPositionInches", robotState.pusherPosition / PusherConstants.kTicksPerInch);
        mWriter.addData("pusherPotVelocity", robotState.pusherVelocity * PusherConstants.kPusherPotSpeedUnitConversion);
    }

    public boolean onTarget() {
        return Math.abs((Robot.getRobotState().pusherPosition - target)) < PusherConstants.kAcceptablePositionError
                && Math.abs(Robot.getRobotState().pusherVelocity) < 0.15;
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
