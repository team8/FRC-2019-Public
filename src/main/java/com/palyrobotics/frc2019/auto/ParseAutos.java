// package com.palyrobotics.frc2019.auto;
//
// import java.io.*;
//
// import org.json.JSONArray;
// import org.json.JSONException;
// import org.json.JSONObject;
//
// import com.palyrobotics.frc2019.util.config.AbstractConfig;
// import com.palyrobotics.frc2019.util.config.Configs;
//
// public class ParseAutos {
//
// private static ParseAutos sInstance = new ParseAutos();
//
// public static ParseAutos getInstance() {
// return sInstance;
// }
//
// public ParseAutos() {
//
// }
//
// public String parseAuto(String jsonClassName) {
//
// Class<? extends AbstractConfig> configClass =
// Configs.getClassFromName(jsonClassName);
// AbstractConfig config = Configs.get(configClass);
// JSONObject jsonObject = new JSONObject();
// JSONArray seqRoutine;
// try {
// jsonObject = new JSONObject(config.toString());
// seqRoutine = jsonObject.getJSONArray("sequentialRoutine");
// } catch (JSONException e) {
// return "JSONException 1";
// }
// String getRoutineFunc = "";
// String routineSetup = "";
// String routineVars = "";
// String allRoutinesString = "";
// String imports = "";
// imports += "package com.palyrobotics.frc2019.auto.modes;\n" + "\n"
// + "import com.palyrobotics.frc2019.auto.AutoModeBase;\n"
// + "import com.palyrobotics.frc2019.behavior.Routine;\n"
// + "import com.palyrobotics.frc2019.behavior.SequentialRoutine;\n";
// for (var i = 0; i < seqRoutine.length(); i++) {
// try {
// String[] valueStringArray = seqRoutine.get(i).toString()
// .substring(1, seqRoutine.get(i).toString().length() - 1).split(",");
// String classString = valueStringArray[0].substring(1,
// valueStringArray[0].length() - 1);
// String[] classStringSplit = classString.split("\\.");
// // return Class.forName(classString.split("\\.")[0]).toString();
// File routineFile;
// String test;
// if (classStringSplit.length > 1) {
// test = "/src/main/java/com/palyrobotics/frc2019/behavior/routines/" +
// classStringSplit[1] + "/"
// + classStringSplit[0] + ".java";
// } else {
// test = "/src/main/java/com/palyrobotics/frc2019/behavior/routines/" +
// classStringSplit[0] + ".java";
// }
//
// routineFile = new File(System.getProperty("user.dir") + test);
// String parameterLine = "";
// try {
// BufferedReader bR = new BufferedReader(new FileReader(routineFile));
// String line = bR.readLine();
// while (line != null) {
// if (line.contains("public " + classStringSplit[0])) {
// parameterLine = line;
// break;
// }
// line = bR.readLine();
// }
//
// // return line;
// } catch (FileNotFoundException e) {
// e.printStackTrace();
// return "fileexception";
// } catch (IOException e) {
// e.printStackTrace();
// return "IOException";
// }
// String[] splitStepOne = parameterLine.split("\\(");
// String[] splitStepTwo = splitStepOne[splitStepOne.length - 1].split("\\)");
// String parameter = splitStepTwo[0];
// String[] parameters = parameter.split(", ");
//
// String routineParameterString = "";
// for (var k = 0; k < parameters.length; k++) {
// String[] parameterSplit = parameters[k].split(" ");
//
// switch (parameterSplit[0]) {
// case "double":
// case "String":
// case "boolean":
// case "int": {
// routineParameterString += valueStringArray[k + 1];
// if (k < parameters.length - 1) {
// routineParameterString += ", ";
// }
// continue;
// }
// case "SparkDriveSignal": {
//
// String[] outputs = valueStringArray[k + 1]
// .substring(1, valueStringArray[k + 1].length() - 1).split("/");
// routineVars += "\t\tSparkMaxOutput left" + i + k + " = new SparkMaxOutput();
// \n";
// routineSetup += "\t\tleft" + i + k + ".setPercentOutput(" + outputs[0] +
// ");\n";
// routineVars += "\t\tSparkMaxOutput right" + i + k + " = new SparkMaxOutput();
// \n";
// routineSetup += "\t\tright" + i + k + ".setPercentOutput(" + outputs[1] +
// ");\n";
// routineParameterString += "new SparkDriveSignal(left" + i + k + ", right" + i
// + k + ")";
// imports += "import com.palyrobotics.frc2019.util.SparkDriveSignal;\n"
// + "import com.palyrobotics.frc2019.util.SparkMaxOutput;\n";
// if (k < parameters.length - 1) {
// routineParameterString += ", ";
// }
// continue;
// }
// case "Path": {
// String noQuotes = valueStringArray[k + 1].substring(1,
// valueStringArray[k + 1].length() - 1);
// routineVars += "\tList<Path.Waypoint> path" + i + k + " = new
// ArrayList<>();\n";
// String[] pathSplit = noQuotes.split("\\.");
// for (var l = 0; l < pathSplit.length; l++) {
// String[] waypointSplit = pathSplit[l].split("/");
// // format
// // StartToCargoShip.add(new Waypoint(
// // new Translation2d(kHabLineX + PhysicalConstants.kRobotLengthInches +
// // kOffsetX, 0), kRunSpeed));
// routineSetup += "\t\tpath" + i + k + ".add(new Path.Waypoint(\n";
// routineSetup += "\t\t\t\t\t\tnew Translation2d(" + waypointSplit[0] + ", "
// + waypointSplit[1] + "), " + waypointSplit[2] + "));\n";
// }
// routineParameterString += "new Path(path" + i + k + ")";
// imports += "import com.palyrobotics.frc2019.util.trajectory.*;\n";
// imports += "import java.util.ArrayList;\n" + "import java.util.List;\n";
// if (k < parameters.length - 1) {
// routineParameterString += ", ";
// }
// continue;
//
// }
// }
// if (parameterSplit[0].contains(".")) {
// routineParameterString += parameterSplit[0] + "."
// + valueStringArray[k + 1].substring(1, valueStringArray[k + 1].length() - 1);
// imports += "import com.palyrobotics.frc2019.subsystems." +
// parameterSplit[0].split("\\.")[0]
// + ";\n";
// if (k < parameters.length - 1) {
// routineParameterString += ", ";
// }
// continue;
// }
//
// }
// allRoutinesString += "new " + classStringSplit[0] + "(" +
// routineParameterString + ")";
// if (classStringSplit.length > 1) {
// imports += "import com.palyrobotics.frc2019.behavior.routines." +
// classStringSplit[1] + "."
// + classStringSplit[0] + ";\n";
// } else {
// imports += "import com.palyrobotics.frc2019.behavior.routines." +
// classStringSplit[0] + ";\n";
// }
//
// if (i < seqRoutine.length() - 1) {
// allRoutinesString += ", ";
// }
//
// // return parameterLine;
//
// } catch (JSONException e) {
// return "JSONException 2";
// }
//
// }
//
// try {
// FileWriter fW = new FileWriter(new File(System.getProperty("user.dir")
// + "\\src\\main\\java\\com\\palyrobotics\\frc2019\\auto\\modes\\test1.java"));
//
// String toStringFunc = "";
// toStringFunc += "@Override\n" + "\tpublic String toString() {\n"
// + "\t\treturn sAlliance + this.getClass().toString();\n" + "\t}\n";
// String preStartFunc = "";
// preStartFunc += "\t@Override\n" + "\tpublic void preStart() {}\n" + "\n";
//
// getRoutineFunc += "\t@Override\n" + "\tpublic Routine getRoutine() {\n" +
// routineVars + routineSetup
// + "\t\treturn new SequentialRoutine(" + allRoutinesString + ");\n" + "\t}\n";
// String getKeyFunc = "";
// getKeyFunc += "\t@Override\n" + "\tpublic String getKey() {\n" + "\t\treturn
// sAlliance.toString();\n"
// + "\t}";
// fW.write(imports + "public class test1 extends AutoModeBase{\n" +
// toStringFunc + preStartFunc
// + getRoutineFunc + getKeyFunc + "\n}");
// fW.close();
// } catch (IOException e) {
// return "FileNotFound";
// }
// return "test";
//
// }
// }
