package com.palyrobotics.frc2019.subsystems;

import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.RobotState;

public class Fingers extends Subsystem {
    public static Fingers instance = new Fingers();

    public static Fingers getInstance() { return instance; }

    public static void resetInstance() {instance = new Fingers(); }

    private boolean inOut = false;


    /**
     * State of Fingers. OPEN = fingers open, etc
     */
    public enum FingersState {
        OPEN, CLOSE
    }

    private FingersState mState = FingersState.CLOSE;

    protected Fingers() { super("Fingers"); }

    @Override
    public void start() { mState = FingersState.CLOSE; }

    @Override
    public void stop() { mState = FingersState.CLOSE; }

    @Override
    public void update(Commands commands, RobotState robotState) {
        mState = commands.wantedFingersState;

        switch(mState) {
            case OPEN:
                inOut = true;
                break;
            case CLOSE:
                inOut = false;
                break;
        }
    }

    public boolean getOpenCloseOutput() { return inOut; }

    @Override
    public String getStatus() { return "Fingers State: " + mState; }
}
