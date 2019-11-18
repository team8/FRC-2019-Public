package com.palyrobotics.frc2019.config;

import com.palyrobotics.frc2019.util.config.AbstractConfig;

/**
 * Contains the field distances for an alliance
 *
 * @author Jason
 */
public class AllianceDistances extends AbstractConfig {

	public String fieldName = "default";
	public double depotFromLeftY = 71.5, depotFromRightY = 72.0, level2FromRightY = 97.5, level2FromLeftY = 97.75,
			level1FromLeftY = 86.75, level1FromRightY = 89.75, cargoOffsetX = 40.0, cargoOffsetY = 14.0,
			level1CargoX = 126.75, cargoLeftY = 133.0, cargoRightY = 134.75, midLineLeftRocketFarX = 70.5,
			midLineRightRocketFarX = 73.25, habLeftRocketCloseX = 117.0, habRightRocketCloseX = 113.0,
			habLeftRocketMidX = 133.5, habRightRocketMidX = 134.5, leftRocketFarY = 12.25, rightRocketFarY = 22.5,
			leftRocketMidY = 24.75, rightRocketMidY = 34.5, leftRocketCloseY = 13.25, rightRocketCloseY = 21.75,
			leftLoadingY = 26.0, rightLoadingY = 25.5, fieldWidth = 324.0;
}
