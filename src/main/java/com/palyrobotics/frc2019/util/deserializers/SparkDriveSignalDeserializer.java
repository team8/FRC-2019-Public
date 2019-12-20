package com.palyrobotics.frc2019.util.deserializers;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.StdDeserializer;

import com.palyrobotics.frc2019.util.SparkDriveSignal;
import com.palyrobotics.frc2019.util.SparkMaxOutput;

public class SparkDriveSignalDeserializer extends StdDeserializer<SparkDriveSignal> {

	public SparkDriveSignalDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public SparkDriveSignal deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		System.out.println(jp.getCurrentToken());
		JsonToken temp = jp.nextValue();

		String jsonString = "";
		while (temp != null) {
			jsonString += (jp.getText() + "#" + jp.getCurrentName() + "#");

			temp = jp.nextValue();
		}
		String[] jsonSplit = jsonString.split("#");
		double leftOutput = 0;
		double rightOutput = 0;
		for (var i = 0; i < jsonSplit.length; i++) {
			if (jsonSplit[i].equals("rightOutput")) {
				rightOutput = Double.parseDouble(jsonSplit[i - 1]);
			}
			if (jsonSplit[i].equals("leftOutput")) {
				leftOutput = Double.parseDouble(jsonSplit[i - 1]);
			}

		}
		SparkMaxOutput lOutput = new SparkMaxOutput();
		SparkMaxOutput rOutput = new SparkMaxOutput();
		lOutput.setPercentOutput(leftOutput);
		rOutput.setPercentOutput(rightOutput);
		return new SparkDriveSignal(lOutput, rOutput);
	}
}
