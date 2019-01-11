package com.palyrobotics.frc2019.config.driveteam;

import com.palyrobotics.frc2019.config.Constants;

public class OperatorProfiles {
	public static void configureConstants() {
		switch(Constants.kOperatorName) {
			case JACOB:
				Constants.operatorXBoxController = true;
				break;
		}
	}
}
