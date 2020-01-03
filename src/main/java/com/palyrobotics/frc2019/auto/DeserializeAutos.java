package com.palyrobotics.frc2019.auto;

import java.io.*;
import java.lang.annotation.Annotation;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
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
		JSONArray autoArray = autoObject.getJSONArray("routines");
		System.out.println(getParamNames("DrivePathRoutine", "drive") + autoObject.toString());

		// setup of class
		String key = "";
		String nameString = "";
		String imports = "package com.palyrobotics.frc2019.auto.modes;\n";
		imports += "import com.palyrobotics.frc2019.auto.AutoModeBase;\n"
				+ "import com.palyrobotics.frc2019.behavior.ParallelRoutine;\n"
				+ "import com.palyrobotics.frc2019.behavior.Routine;\n"
				+ "import com.palyrobotics.frc2019.behavior.SequentialRoutine;";
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
		String routineSetup = "";
		String routineString = "return new SequentialRoutine(";
		for (var i = 0; i < autoArray.length(); i++) {
			JSONObject routineObject = autoArray.getJSONObject(i);
			String type = (String) routineObject.get("@type");
			if (type.equals("ParallelRoutine")) {
				JSONArray autoArrayParallel = routineObject.getJSONArray("routines");
				String routineStringParallel = "new ParallelRoutine(";
				for (var l = 0; l < autoArrayParallel.length(); l++) {
					JSONObject routineObjectParallel = autoArrayParallel.getJSONObject(l);
					String typeParallel = (String) routineObjectParallel.get("@type");
					String[] typeSplit = typeParallel.split("\\.");
					String[] paramNames;
					String[] paramTypes;
					String[] paramTypesChanon;
					String newRoutineString = "new ";
					if (typeSplit.length > 1) {
						paramNames = getParamNames(typeSplit[1], typeSplit[0]).split(",");
						paramTypes = getParamTypes(typeSplit[1], typeSplit[0]).split(",");
						paramTypesChanon = getParamTypesChanonical(typeSplit[1], typeSplit[0]).split(",");
						imports += "import com.palyrobotics.frc2019.behavior.routines." + typeSplit[0] + "."
								+ typeSplit[1] + ";\n";
						newRoutineString += typeSplit[1] + "(";
					} else {
						paramNames = getParamNames(typeSplit[0], "null").split(",");
						paramTypes = getParamTypes(typeSplit[0], "null").split(",");
						paramTypesChanon = getParamTypesChanonical(typeSplit[0], "null").split(",");
						newRoutineString += typeSplit[0] + "(";
						imports += "import com.palyrobotics.frc2019.behavior.routines." + typeSplit[0] + ";\n";
					}

					for (var j = 0; j < paramTypes.length; j++) {
						switch (paramTypes[j]) {
							case "double":
							case "boolean":
							case "int": {
								newRoutineString += routineObjectParallel.get(paramNames[j]);
								if (j != paramTypes.length - 1) {
									newRoutineString += ",";
								}
								continue;
							}
							case "String": {
								newRoutineString += "\"" + routineObjectParallel.get(paramNames[j]) + "\"";
								if (j != paramTypes.length - 1) {
									newRoutineString += ",";
								}
								continue;
							}
							case "Path": {
								JSONArray pathArray = routineObjectParallel.getJSONArray(paramNames[j]);
								String makePathVar = "List<Path.Waypoint> path" + i + l + j + " = new ArrayList<>();\n";
								imports += "import java.util.ArrayList;\n" + "import java.util.List;\n";
								imports += "import com.palyrobotics.frc2019.util.trajectory.*;\n";
								routineSetup += makePathVar;
								for (var k = 0; k < pathArray.length(); k++) {
									JSONArray waypointArray = pathArray.getJSONObject(k).getJSONArray("Waypoint");
									String addWayPoint = "path" + i + l + j
											+ ".add(new Path.Waypoint(new Translation2d("
											+ waypointArray.getJSONObject(0).get("x") + ","
											+ waypointArray.getJSONObject(1).get("y") + "),"
											+ waypointArray.getJSONObject(2).get("speed") + "));\n";
									routineSetup += addWayPoint;
								}
								newRoutineString += "new Path(path" + i + l + j + ")\n";
								if (j != paramTypes.length - 1) {
									newRoutineString += ",";
								}
								continue;
							}
							case "SparkDriveSignal": {
								JSONArray sparkDriveArray = routineObjectParallel.getJSONArray(paramNames[j]);
								double leftOut = sparkDriveArray.getJSONObject(0).getDouble("leftOutput");
								double rightOut = sparkDriveArray.getJSONObject(1).getDouble("rightOutput");
								routineSetup += "SparkMaxOutput left" + i + l + j + " = new SparkMaxOutput();\n";
								routineSetup += "left" + i + l + j + ".setPercentOutput(" + leftOut + ");\n";
								routineSetup += "SparkMaxOutput right" + i + l + j + " = new SparkMaxOutput();\n";
								routineSetup += "right" + i + l + j + ".setPercentOutput(" + rightOut + ");\n";
								newRoutineString += "new SparkDriveSignal(left" + i + l + j + ", right" + i + l + j
										+ ")\n";
								imports += "import com.palyrobotics.frc2019.util.SparkDriveSignal;\n"
										+ "import com.palyrobotics.frc2019.util.SparkMaxOutput;\n";
								if (j != paramTypes.length - 1) {
									newRoutineString += ",";
								}
								continue;
							}

						}
						String[] chanonSplit = paramTypesChanon[j].split("\\.");
						String enumName = chanonSplit[chanonSplit.length - 1];
						String subsystemName = chanonSplit[chanonSplit.length - 2];
						newRoutineString += subsystemName + "." + enumName + "."
								+ routineObjectParallel.get(paramNames[j]);
						imports += "import com.palyrobotics.frc2019.subsystems." + subsystemName + ";\n";
						if (j != paramTypes.length - 1) {
							newRoutineString += ",";
						}

					}
					newRoutineString += ")";
					if (l == autoArrayParallel.length() - 1) {
						routineStringParallel += newRoutineString + ")";
					} else {
						newRoutineString += ",";
						routineStringParallel += newRoutineString;
					}
				}
				if (i == autoArray.length() - 1) {
					routineString += routineStringParallel + ");";
				} else {
					routineStringParallel += ",";
					routineString += routineStringParallel;
				}

			} else {
				String[] typeSplit = type.split("\\.");
				String[] paramNames;
				String[] paramTypes;
				String[] paramTypesChanon;
				String newRoutineString = "new ";
				if (typeSplit.length > 1) {
					paramNames = getParamNames(typeSplit[1], typeSplit[0]).split(",");
					paramTypes = getParamTypes(typeSplit[1], typeSplit[0]).split(",");
					paramTypesChanon = getParamTypesChanonical(typeSplit[1], typeSplit[0]).split(",");
					imports += "import com.palyrobotics.frc2019.behavior.routines." + typeSplit[0] + "." + typeSplit[1]
							+ ";\n";
					newRoutineString += typeSplit[1] + "(";
				} else {
					paramNames = getParamNames(typeSplit[0], "null").split(",");
					paramTypes = getParamTypes(typeSplit[0], "null").split(",");
					paramTypesChanon = getParamTypesChanonical(typeSplit[0], "null").split(",");
					newRoutineString += typeSplit[0] + "(";
					imports += "import com.palyrobotics.frc2019.behavior.routines." + typeSplit[0] + ";\n";
				}

				for (var j = 0; j < paramTypes.length; j++) {
					switch (paramTypes[j]) {
						case "double":
						case "boolean":
						case "int": {
							newRoutineString += routineObject.get(paramNames[j]);
							if (j != paramTypes.length - 1) {
								newRoutineString += ",";
							}
							continue;
						}
						case "String": {
							newRoutineString += "\"" + routineObject.get(paramNames[j]) + "\"";
							if (j != paramTypes.length - 1) {
								newRoutineString += ",";
							}
							continue;
						}
						case "Path": {
							JSONArray pathArray = routineObject.getJSONArray(paramNames[j]);
							String makePathVar = "List<Path.Waypoint> path" + i + j + " = new ArrayList<>();\n";
							imports += "import java.util.ArrayList;\n" + "import java.util.List;\n";
							imports += "import com.palyrobotics.frc2019.util.trajectory.*;\n";
							routineSetup += makePathVar;
							for (var k = 0; k < pathArray.length(); k++) {
								JSONArray waypointArray = pathArray.getJSONObject(k).getJSONArray("Waypoint");
								String addWayPoint = "path" + i + j + ".add(new Path.Waypoint(new Translation2d("
										+ waypointArray.getJSONObject(0).get("x") + ","
										+ waypointArray.getJSONObject(1).get("y") + "),"
										+ waypointArray.getJSONObject(2).get("speed") + "));\n";
								routineSetup += addWayPoint;
							}
							newRoutineString += "new Path(path" + i + j + ")\n";
							if (j != paramTypes.length - 1) {
								newRoutineString += ",";
							}
							continue;
						}
						case "SparkDriveSignal": {
							JSONArray sparkDriveArray = routineObject.getJSONArray(paramNames[j]);
							double leftOut = sparkDriveArray.getJSONObject(0).getDouble("leftOutput");
							double rightOut = sparkDriveArray.getJSONObject(1).getDouble("rightOutput");
							routineSetup += "SparkMaxOutput left" + i + j + " = new SparkMaxOutput();\n";
							routineSetup += "left" + i + j + ".setPercentOutput(" + leftOut + ");\n";
							routineSetup += "SparkMaxOutput right" + i + j + " = new SparkMaxOutput();\n";
							routineSetup += "right" + i + j + ".setPercentOutput(" + rightOut + ");\n";
							newRoutineString += "new SparkDriveSignal(left" + i + j + ", right" + i + j + ")\n";
							imports += "import com.palyrobotics.frc2019.util.SparkDriveSignal;\n"
									+ "import com.palyrobotics.frc2019.util.SparkMaxOutput;\n";
							if (j != paramTypes.length - 1) {
								newRoutineString += ",";
							}
							continue;
						}

					}
					String[] chanonSplit = paramTypesChanon[j].split("\\.");
					String enumName = chanonSplit[chanonSplit.length - 1];
					String subsystemName = chanonSplit[chanonSplit.length - 2];
					newRoutineString += subsystemName + "." + enumName + "." + routineObject.get(paramNames[j]);
					imports += "import com.palyrobotics.frc2019.subsystems." + subsystemName + ";\n";
					if (j != paramTypes.length - 1) {
						newRoutineString += ",";
					}

				}
				newRoutineString += ")";
				if (i == autoArray.length() - 1) {
					routineString += newRoutineString + ");";
				} else {
					newRoutineString += ",";
					routineString += newRoutineString;
				}
			}
			System.out.println(routineString);
			System.out.println(routineSetup);
		}
		//
		// putting all together
		try {
			FileWriter fW = new FileWriter(new File(System.getProperty("user.dir")
					+ "\\src\\main\\java\\com\\palyrobotics\\frc2019\\auto\\modes\\" + autoClassName + ".java"));

			String toStringFunc = "";
			String getRoutineFunc = "";
			imports += "\n";
			toStringFunc += "@Override\n" + "\tpublic String toString() {\n"
					+ "\t\treturn sAlliance + this.getClass().toString();\n" + "\t}\n";
			String preStartFunc = "";
			preStartFunc += "\t@Override\n" + "\tpublic void preStart() {}\n" + "\n";

			getRoutineFunc += "\t@Override\n" + "\tpublic Routine getRoutine() {\n" + routineSetup + routineString
					+ "}";
			String getKeyFunc = "";
			getKeyFunc += "\t@Override\n" + "\tpublic String getKey() {" + key + "\t}";
			fW.write(imports + "public class test1 extends AutoModeBase{\n" + toStringFunc + preStartFunc
					+ getRoutineFunc + getKeyFunc + "\n}");
			fW.close();
		} catch (IOException e) {
		}

		//
	}

	String getParamTypes(String routineName, String routinePackage) {
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
					paramTypes += aClass.getConstructors()[constructorIndex].getParameterTypes()[i].getSimpleName()
							+ ",";
				}
				return paramTypes;
			} else {
				return "constructor not found";
			}

		} catch (ClassNotFoundException e) {
			return "e";
		}
	}

	String getParamTypesChanonical(String routineName, String routinePackage) {
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
					paramTypes += aClass.getConstructors()[constructorIndex].getParameterTypes()[i].getCanonicalName()
							+ ",";
				}
				return paramTypes;
			} else {
				return "constructor not found";
			}

		} catch (ClassNotFoundException e) {
			return "e";
		}
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
