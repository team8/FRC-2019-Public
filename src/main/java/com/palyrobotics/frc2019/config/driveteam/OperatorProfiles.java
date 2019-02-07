package com.palyrobotics.frc2019.config.driveteam;

public class OperatorProfiles {
	public static void configureConstants() {
		switch(OtherConstants.kOperatorName) {
			case GRIFFIN:
				OtherConstants.operatorXBoxController = true;
				break;
		}
	}
}
