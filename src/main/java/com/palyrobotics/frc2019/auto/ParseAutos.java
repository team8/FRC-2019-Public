package com.palyrobotics.frc2019.auto;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.palyrobotics.frc2019.util.config.AbstractConfig;
import com.palyrobotics.frc2019.util.config.Configs;

public class ParseAutos {

	private static ParseAutos sInstance = new ParseAutos();

	public static ParseAutos getInstance() {
		return sInstance;
	}

	public ParseAutos() {

	}

	public String parseAuto(String jsonClassName) {

		Class<? extends AbstractConfig> configClass = Configs.getClassFromName(jsonClassName);
		AbstractConfig config = Configs.get(configClass);
		JSONObject jsonObject = new JSONObject();
		JSONArray seqRoutine;
		try {
			jsonObject = new JSONObject(config.toString());
			seqRoutine = jsonObject.getJSONArray("sequentialRoutine");
		} catch (JSONException e) {
			return "JSONException 1";
		}
		for (var i = 0; i < seqRoutine.length(); i++) {
			try {
				String[] valueStringArray = seqRoutine.get(i).toString()
						.substring(1, seqRoutine.get(i).toString().length() - 1).split(",");
				String classString = valueStringArray[0].substring(1, valueStringArray[0].length() - 1);
				String[] classStringSplit = classString.split("\\.");
				// return Class.forName(classString.split("\\.")[0]).toString();
				FileWriter fW = new FileWriter(new File(
						"C:\\Users\\Nerdu\\Desktop\\git\\FRC-Public-2\\FRC-2019-Public\\src\\main\\java\\com\\palyrobotics\\frc2019\\auto\\modes\\test1.java"));
				String imports = "";
				imports += "package com.palyrobotics.frc2019.auto.modes;\n" + "\n"
						+ "import com.palyrobotics.frc2019.auto.AutoModeBase;\n"
						+ "import com.palyrobotics.frc2019.behavior.Routine;\n"
						+ "import com.palyrobotics.frc2019.behavior.SequentialRoutine;\n";

				String toStringFunc = "";
				toStringFunc += "@Override\n" + "\tpublic String toString() {\n"
						+ "\t\treturn sAlliance + this.getClass().toString();\n" + "\t}\n";
				String preStartFunc = "";
				preStartFunc += "\t@Override\n" + "\tpublic void preStart() {}\n" + "\n";
				String getRoutineFunc = "";
				getRoutineFunc += "\t@Override\n" + "\tpublic Routine getRoutine() {\n"
						+ "\t\treturn new SequentialRoutine();\n" + "\t}\n";
				String getKeyFunc = "";
				getKeyFunc += "\t@Override\n" + "\tpublic String getKey() {\n" + "\t\treturn sAlliance.toString();\n"
						+ "\t}";
				fW.write(imports + "public class test1 extends AutoModeBase{\n" + toStringFunc + preStartFunc
						+ getRoutineFunc + getKeyFunc + "\n}");
				fW.close();
				return "test";

			} catch (JSONException | IOException e) {
				return "JSONException 2";
			}

		}
		return "no routines";

	}
}
