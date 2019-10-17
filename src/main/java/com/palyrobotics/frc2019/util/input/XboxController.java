package com.palyrobotics.frc2019.util.input;

import java.util.HashMap;
import java.util.Map;

public class XboxController extends edu.wpi.first.wpilibj.XboxController {

    private static final double kTriggerThreshold = 0.8;

    public XboxController(int port) {
        super(port);
    }

    private int mLastPOV = -1;
    private Map<Hand, Boolean> mLastTriggers = new HashMap<>(Map.of(
            Hand.kLeft, false,
            Hand.kRight, false
    ));

    public void updateLastInputs() {
        mLastPOV = getPOV();
        mLastTriggers.replaceAll((hand, b) -> getTriggerAxis(hand) > kTriggerThreshold);
    }

    public boolean getDPadRight() {
        return getPOV() != mLastPOV && getPOV() == 90;
    }

    public boolean getDPadUp() {
        return getPOV() != mLastPOV && getPOV() == 0;
    }

    public boolean getDPadDown() {
        return getPOV() != mLastPOV && getPOV() == 180;
    }

    public boolean getDPadLeft() {
        return getPOV() != mLastPOV && getPOV() == 270;
    }

    public boolean getTriggerPressed(Hand hand) {
        return mLastTriggers.get(hand) != getTriggerAxis(hand) > kTriggerThreshold && getTriggerAxis(hand) > kTriggerThreshold;
    }

    public boolean getRightTriggerPressed() {
        return getTriggerPressed(Hand.kRight);
    }

    public boolean getLeftTriggerPressed() {
        return getTriggerPressed(Hand.kLeft);
    }

    public boolean getLeftBumperPressed() {
        return getBumperPressed(Hand.kLeft);
    }

    public boolean getRightBumperPressed() {
        return getBumperPressed(Hand.kRight);
    }

    public void setRumble(boolean on) {
        setRumble(RumbleType.kRightRumble, on ? 1.0 : 0.0);
        setRumble(RumbleType.kLeftRumble, on ? 1.0 : 0.0);
    }
}
