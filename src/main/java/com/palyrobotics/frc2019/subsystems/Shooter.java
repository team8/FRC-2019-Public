package com.palyrobotics.frc2019.subsystems;

import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.RobotState;
import com.palyrobotics.frc2019.config.Constants.*;

public class Shooter extends Subsystem{
    public static Shooter instance = new Shooter("Shooter");

    public static Shooter getInstance() { return instance; }

    public static void resetInstance() { instance = new Shooter("Shooter"); }

    private double mOutput;

    private double mRumbleLength;

    private boolean cachedCargoState;

    private double mExpellingCycles;
    private boolean readyToExpel;

    public enum ShooterState {
        EXPELLING,
        IDLE
    }

    private ShooterState mState = ShooterState.IDLE;

    protected Shooter(String name) {
        super(name);
    }

    @Override
    public void start() {
        mState = ShooterState.IDLE;
    }

    @Override
    public void stop() {
        mState = ShooterState.IDLE;
    }

    @Override
    public void update(Commands commands, RobotState robotState) {
        mState = commands.wantedShooterState;

        switch(mState) {
            case IDLE:
                mOutput = 0;
                mExpellingCycles = 0;
                break;
            case EXPELLING:
                if (commands.customShooterSpeed) {
                    mOutput = robotState.operatorXboxControllerInput.leftTrigger; //TODO: change control?
                } else {
                    mOutput = ShooterConstants.kExpellingMotorVelocity;
                }
                mExpellingCycles++;
                break;
        }

        if(1 / OtherConstants.deltaTime <= mExpellingCycles) { // Once enough time passes, ready to expel
            readyToExpel = true;
        } else {
            readyToExpel = false;
        }

        if(readyToExpel && robotState.hasCargo) { // Rumble until expelled
            mRumbleLength = 0.5;
        }
        if(cachedCargoState && !robotState.hasCargo) { // Stop rumbling once you go from cargo -> no cargo
            mRumbleLength = -1;
            mExpellingCycles = 0;
        }

        cachedCargoState = robotState.hasCargo;
    }

    public double getRumbleLength() {
        return mRumbleLength;
    }

    public double getOutput() {
        return mOutput;
    }

    @Override
    public String getStatus() {
        return "Shooter State: " + mState + "\nVictor Output: " + mOutput;
    }
}
