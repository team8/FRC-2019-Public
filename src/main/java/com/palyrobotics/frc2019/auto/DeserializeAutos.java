package com.palyrobotics.frc2019.auto;

import java.io.*;
import java.lang.annotation.Annotation;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * lul im finna do that thing that nihar does with his @author
 * 
 * @author Vedanth, based off my own shitty brain ideas, 1/1/2020 - ayyy new
 *         decade we finna get the blue banners
 */

public class DeserializeAutos {

	private static DeserializeAutos sInstance = new DeserializeAutos();

	public static DeserializeAutos getInstance() {
		return sInstance;
	}

	public void deserialize(String autoClassName, JsonNode jsonNode) throws JSONException, IOException {
		JSONObject autoObject = new JSONObject(jsonNode.toString());
		System.out.println(getParamNames("DrivePathRoutine", "drive") + autoObject.toString());

		// setup of class
		String key = "";
		String nameString = "";
		String imports = "package com.palyrobotics.frc2019.auto.modes;\n";
		String filePath = "/src/main/java/com/palyrobotics/frc2019/auto/modes/" + autoClassName + ".java";
		File autoFile = new File(System.getProperty("user.dir") + filePath);
		BufferedReader autoReader = new BufferedReader(new FileReader(autoFile));
		String currentLine = autoReader.readLine();
		while (currentLine != null) {
			if (currentLine.contains("public String toString()")) {
				if (currentLine.contains("}")) {
					String[] temp1 = currentLine.split("\\{");
					String[] temp2 = temp1[1].split("}");
					nameString = temp2[0];
					currentLine = autoReader.readLine();
				} else {
					currentLine = autoReader.readLine();
					nameString = currentLine;
				}
			}
			if (currentLine.contains("public String getKey()")) {
				if (currentLine.contains("}")) {
					String[] temp1 = currentLine.split("\\{");
					String[] temp2 = temp1[1].split("}");
					key = temp2[0];
					currentLine = autoReader.readLine();
				} else {
					currentLine = autoReader.readLine();
					key = currentLine;
				}
			}
			currentLine = autoReader.readLine();
		}
		System.out.println(nameString + " nameString");
		System.out.println(key + " key");
		//
		// routine and imports
		String routineSetup;
		String routineString;
		//
		// putting all together

		//
	}

	String getParamNames(String routineName, String routinePackage) {
		try {
			Class aClass;
			if (routinePackage != "null") {
				aClass = Class
						.forName("com.palyrobotics.frc2019.behavior.routines." + routinePackage + "." + routineName);
			} else {
				aClass = Class.forName("com.palyrobotics.frc2019.behavior.routines." + routineName);
			}

			String paramTypes = "";
			int constructorIndex = -1;
			for (var j = 0; j < aClass.getConstructors().length; j++) {
				if (aClass.getConstructors()[j].getDeclaredAnnotations().length > 0) {
					Annotation annot = aClass.getConstructors()[j].getAnnotation(JsonCreator.class);
					if (annot instanceof JsonCreator) {
						constructorIndex = j;
					}
				}
			}
			if (constructorIndex != -1) {
				for (var i = 0; i < aClass.getConstructors()[constructorIndex].getParameterTypes().length; i++) {
					paramTypes += aClass.getConstructors()[constructorIndex].getParameters()[i]
							.getAnnotation(JsonProperty.class).value() + ",";
				}
				return paramTypes;
			} else {
				return "constructor not found";
			}

		} catch (ClassNotFoundException e) {
			return "e";
		}
	}
}
