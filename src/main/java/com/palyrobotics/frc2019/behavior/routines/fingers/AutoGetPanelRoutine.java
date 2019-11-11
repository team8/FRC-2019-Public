package com.palyrobotics.frc2019.behavior.routines.fingers;

import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.config.Commands;
import com.palyrobotics.frc2019.config.subsystem.FingerConfig;
import com.palyrobotics.frc2019.subsystems.Fingers;
import com.palyrobotics.frc2019.subsystems.Pusher;
import com.palyrobotics.frc2019.subsystems.Subsystem;
import com.palyrobotics.frc2019.util.config.Configs;
import com.palyrobotics.frc2019.util.csvlogger.CSVWriter;
import edu.wpi.first.wpilibj.Timer;

public class AutoGetPanelRoutine extends Routine {
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
        CSVWriter.addData("EnteredRoutine", 1);
        if (mState == mStates.START) { //runs only once
            mInitialPusherEncPos = mRobotState.pusherPosition;
            commands.wantedFingersOpenCloseState = Fingers.FingersState.OPEN;
            commands.wantedFingersExpelState = Fingers.PushingState.EXPELLING;
            mState = mStates.EXTENDED;
        }

        double currentPusherEncoder = mRobotState.pusherPosition;
        //CSVWriter.addData("FingerState",  Fingers.getInstance().FingersState == Fingers.FingersState.OPEN ? 1 : 0 );
        CSVWriter.addData("Encoder", currentPusherEncoder);
        //if pusher has been compressed for more than 250 ms, then get the panel.
        if (currentPusherEncoder - mInitialPusherEncPos > mFingerConfig.compressionError) {
            System.out.println("FingerStateCompressed");
            if (mState == mStates.EXTENDED) {
                mState = mStates.COMPRESSED;
                mStartingTime = Timer.getFPGATimestamp(); //gets time only first time compression was found to be true
                System.out.println("Started wait loop");
            } else {
                CSVWriter.addData("TimeSinceStart", Timer.getFPGATimestamp() - mStartingTime);
            }
            if ((Timer.getFPGATimestamp() - mStartingTime) > 0.2) { //checks if 200 ms elapsed
                commands.wantedFingersOpenCloseState = Fingers.FingersState.OPEN;
                commands.wantedFingersExpelState = Fingers.PushingState.EXPELLING;
                mState = mStates.DONE;
                System.out.println("Compressed, picking up panel");
            }
        }
        return commands;
    }

    @Override
    public Commands cancel(Commands commands) {
          mState = mStates.EXTENDED;
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