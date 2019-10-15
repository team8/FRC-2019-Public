package com.palyrobotics.frc2019.util;

import edu.wpi.first.wpilibj.Joystick;

/**
 * Class to store Joystick input
 *
 * @author Nihar
 */
public class JoystickInput {

    private double x, y;
    private boolean[] buttons = new boolean[12];

    @Override
    public String toString() {
        return String.format("Joystick X: %s Y: %s", this.x, this.y);
    }

    public void update(Joystick joystick) {
        x = joystick.getX();
        y = joystick.getY();
        for (int i = 1; i < 12; i++) {
            //getRawButton(1) is the trigger
            buttons[i] = joystick.getRawButton(i);
        }
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public boolean getButtonPressed(int button) {
        return buttons[button];
    }

    public boolean getTriggerPressed() {
        return buttons[1];
    }
}
