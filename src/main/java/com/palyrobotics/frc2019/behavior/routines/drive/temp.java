package com.palyrobotics.frc2019.behavior.routines.drive;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect (fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
public class temp {

	public double angle;
	private double test = 50;

	@JsonCreator
	public temp(@JsonProperty ("angle") double inAngle) {
		angle = inAngle;
	}

}
