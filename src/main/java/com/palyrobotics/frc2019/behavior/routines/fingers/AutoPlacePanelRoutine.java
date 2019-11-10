package com.palyrobotics.frc2019.behavior.routines.fingers;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.subsystem.FingerConfig;
import com.palyrobotics.frc2019.subsystems.Fingers;
import com.palyrobotics.frc2019.subsystems.Pusher;
import com.palyrobotics.frc2019.subsystems.Subsystem;
import com.palyrobotics.frc2019.util.config.Configs;
import edu.wpi.first.wpilibj.Timer;

public class AutoPlacePanelRoutine extends Routine {
    private FingerConfig mFingerConfig = Configs.get(FingerConfig.class);
    private double mInitialPusherEncPos;
    private double mStartingTime;

    private mStates mState;

    private enum mStates {
        START, COMPRESSED, EXTENDED, DONE
    }

    @Override
    public void start() {
        mInitialPusherEncPos = mRobotState.pusherPosition;
        mState = mStates.START;
    }

    @Override
    public Commands update(Commands commands) {
        if (mState == mStates.START) { //runs only once
            mInitialPusherEncPos = mRobotState.pusherPosition;
            commands.wantedFingersOpenCloseState = Fingers.FingersState.OPEN;
            commands.wantedFingersExpelState = Fingers.PushingState.EXPELLING;
            mState = mStates.EXTENDED;
            commands.wantedPusherInOutState = Pusher.PusherState.OUT;
        }

        double currentPusherEncoder = mRobotState.pusherPosition;
        //if pusher has been compressed for more than 250 ms, then get the panel.
        if (currentPusherEncoder - mInitialPusherEncPos > mFingerConfig.compressionError) { //abs val may not be necessary; testing needs to be done first
            if (mState == mStates.EXTENDED) {
                mStartingTime = Timer.getFPGATimestamp();
                mState = mStates.COMPRESSED;
                System.out.println("Started wait loop");
            }
            if (Timer.getFPGATimestamp() - mStartingTime > 0.25 && currentPusherEncoder - mInitialPusherEncPos > mFingerConfig.compressionError) {
                commands.wantedFingersOpenCloseState = Fingers.FingersState.OPEN;
                commands.wantedFingersExpelState = Fingers.PushingState.EXPELLING;
                mState = mStates.DONE;
                System.out.println("Compressed, picking up panel");
            }
        } else {
            if (mState == mStates.COMPRESSED) {
                System.out.println("Ended wait loop");
            }
            mState = mStates.EXTENDED;
        }
        return commands;
    }

    @Override
    public Commands cancel(Commands commands) {
//        commands.wantedFingersOpenCloseState = Fingers.FingersState.OPEN;
//        commands.wantedFingersExpelState = Fingers.PushingState.CLOSED;
        return commands;
    }

    @Override
    public boolean isFinished() {
        return mState == mStates.DONE;
    }

    @Override
    public Subsystem[] getRequiredSubsystems() {
        return new Subsystem[]{mFingers};
    }

    @Override
    public String getName() {
        return "Fingers Auto Routine";
    }
}