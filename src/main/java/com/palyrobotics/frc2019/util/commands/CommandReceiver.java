package com.palyrobotics.frc2019.util.commands;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.palyrobotics.frc2019.auto.AutoModeSelector;
import com.palyrobotics.frc2019.auto.DeserializeAutos;
import com.palyrobotics.frc2019.behavior.Routine;
import com.palyrobotics.frc2019.behavior.RoutineManager;
import com.palyrobotics.frc2019.behavior.routines.elevator.ElevatorMeasureSpeedAtOutputRoutine;
import com.palyrobotics.frc2019.config.subsystem.ElevatorConfig;
import com.palyrobotics.frc2019.robot.HardwareAdapter;
import com.palyrobotics.frc2019.util.SparkDriveSignal;
import com.palyrobotics.frc2019.util.config.AbstractConfig;
import com.palyrobotics.frc2019.util.config.Configs;
import com.palyrobotics.frc2019.util.serializers.PathSerializer;
import com.palyrobotics.frc2019.util.serializers.SparkDriveSignalSerializer;
import com.palyrobotics.frc2019.util.service.RobotService;
import com.palyrobotics.frc2019.util.trajectory.Path;

public class CommandReceiver implements RobotService {

	private static final int PORT = 5808;
	private static ObjectMapper sMapper = Configs.getMapper();

	private final ArgumentParser mParser;
	private Server mServer;
	private AtomicString mResult = new AtomicString(), mCommand = new AtomicString();

	public CommandReceiver() {
		mParser = ArgumentParsers.newFor("rio-terminal").build();
		Subparsers subparsers = mParser.addSubparsers().dest("command");
		Subparser set = subparsers.addParser("set");
		set.addArgument("config_name");
		set.addArgument("config_field");
		set.addArgument("config_value");
		Subparser getAuto = subparsers.addParser("getAuto");
		getAuto.addArgument("auto_index");
		Subparser deserializeAuto = subparsers.addParser("deserializeAuto");
		deserializeAuto.addArgument("auto_string");
		deserializeAuto.addArgument("auto_name");
		Subparser getRawRoutineJson = subparsers.addParser("getRawJSON");
		getRawRoutineJson.addArgument("routine_package");
		getRawRoutineJson.addArgument("routine_name");
		Subparser getAutoCount = subparsers.addParser("getAutoCount");
		Subparser getAutoName = subparsers.addParser("getAutoName");
		getAutoName.addArgument("auto_index");
		Subparser editArray = subparsers.addParser("editArray");
		editArray.addArgument("config_name");
		editArray.addArgument("config_field");
		editArray.addArgument("config_value");
		editArray.addArgument("config_value_two");
		Subparser getRoutine = subparsers.addParser("getRoutine");
		getRoutine.addArgument("routine_name");
		getRoutine.addArgument("routine_package");
		Subparser getClassFromName = subparsers.addParser("getConfigFromName");
		getClassFromName.addArgument("config_name");
		Subparser getAllRoutines = subparsers.addParser("getAllRoutines");
		Subparser getEnumValues = subparsers.addParser("getEnumValues");
		getEnumValues.addArgument("class_name");
		getEnumValues.addArgument("enum_name");
		Subparser get = subparsers.addParser("get");
		get.addArgument("config_name");
		get.addArgument("config_field").nargs("?"); // "?" means this is optional, and will default to null if not
		// supplied
		get.addArgument("--raw").action(Arguments.storeTrue());
		subparsers.addParser("reload").addArgument("config_name");
		Subparser run = subparsers.addParser("run");
		run.addArgument("routine_name").setDefault("measure_elevator_speed");
		run.addArgument("parameters").nargs("*");
		subparsers.addParser("save").addArgument("config_name");
		subparsers.addParser("calibrate").addSubparsers().dest("subsystem").addParser("arm")
				.help("Resets the Spark encoder so it is in-line with the potentiometer");
	}

	@Override
	public void start() {
		mServer = new Server();
		mServer.getKryo().setRegistrationRequired(false);
		try {
			mServer.addListener(new Listener() {

				@Override
				public void connected(Connection connection) {
					System.out.println("Connected!");
				}

				@Override
				public void disconnected(Connection connection) {
					System.out.println("Disconnected!");
				}

				@Override
				public void received(Connection connection, Object message) {
					if (message instanceof String) {
						String command = (String) message;
						mCommand.set(command);
						try {
							String result = mResult.waitAndGet();
							mServer.sendToTCP(connection.getID(), result);
						} catch (InterruptedException interruptedException) {
							mServer.close();
						}
					}
				}
			});
			mServer.bind(PORT);
			mServer.start();
			System.out.println("Started command receiver server");
		} catch (IOException | IllegalMonitorStateException exception) {
			exception.printStackTrace();
		}
	}

	public void stop() {
		mServer.stop();
	}

	public void update() {
		mCommand.tryGetAndReset(command -> {
			if (command == null)
				return;
			String result = executeCommand(command);
			// System.out.println(String.format("Result: %s", result));
			mResult.setAndNotify(result);
		});
	}

	public String executeCommand(String command) {
		if (command == null)
			throw new IllegalArgumentException("Command can not be null!");
		String result;
		try {
			Namespace parse = mParser.parseArgs(command.trim().split("\\s+"));
			result = handleParsedCommand(parse);
		} catch (ArgumentParserException parseException) {
			StringWriter help = new StringWriter();
			PrintWriter printer = new PrintWriter(help);
			parseException.getParser().printHelp(printer);
			result = String.format("Error running command: %s%n%s", parseException.getMessage(), help.toString());
		}
		return result;
	}

	@Override
	public String getConfigName() {
		return "commandReceiver";
	}

	private Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
		Map<String, Field> fields = new HashMap<>();
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			fields.putAll(Arrays.stream(c.getDeclaredFields())
					.collect(Collectors.toMap(Field::getName, Function.identity())));
		}
		return Optional.ofNullable(fields.get(name)).orElseThrow(NoSuchFieldException::new);
	}

	private String handleParsedCommand(Namespace parse) throws ArgumentParserException {
		String commandName = parse.getString("command");
		switch (commandName) {
			case "getEnumValues": {
				String subsystemName = parse.getString("class_name");
				String enumName = parse.get("enum_name");
				try {
					Class subClass = Class.forName("com.palyrobotics.frc2019.subsystems." + subsystemName);

					// Field enumVar = subClass.getField(enumName);
					Class[] classes = subClass.getClasses();
					for (var i = 0; i < classes.length; i++) {
						String simpleName = classes[i].getSimpleName();
						if (simpleName.equals(enumName)) {
							String enumString = "";
							for (var x = 0; x < classes[i].getEnumConstants().length; x++) {
								enumString += classes[i].getEnumConstants()[x].toString() + " ";
							}
							return enumString;
						}

					}
					// return subClass.getEnumConstants().toString();
					// return enumVar.toString();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return "error";
				}
				return "enum not found";

			}
			case "getRawJSON": {
				try {
					String routinePackage = parse.getString("routine_package");
					String routineName = parse.getString("routine_name");
					if(routineName.equals("ParallelRoutine") == false){
						Class aClass = null;
						if(routineName != "ParallelRoutine"){
							if (routinePackage != "null") {
								aClass = Class.forName(
										"com.palyrobotics.frc2019.behavior.routines." + routinePackage + "." + routineName);
							} else {
								aClass = Class.forName("com.palyrobotics.frc2019.behavior.routines." + routineName);
							}
						}


						String paramTypes = "";
						String paramNames = "";
						String canonicalParamTypes = "";
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
							for (var i = 0; i < aClass.getConstructors()[constructorIndex]
									.getParameterTypes().length; i++) {
								paramTypes += aClass.getConstructors()[constructorIndex].getParameterTypes()[i]
										.getSimpleName() + ",";
								canonicalParamTypes += aClass.getConstructors()[constructorIndex].getParameterTypes()[i]
										.getCanonicalName() + ",";
								paramNames += aClass.getConstructors()[constructorIndex].getParameters()[i]
										.getAnnotation(JsonProperty.class).value() + ",";
							}
						} else {
							return "constructor not found";
						}
						System.out.println(paramTypes + " " + paramNames);
						String rawJSON = "{";
						String[] paramTypesSplit = paramTypes.split(",");
						String[] paramNamesSplit = paramNames.split(",");
						String[] paramChanonSplit = canonicalParamTypes.split(",");
						if (routineName != "ParallelRoutine") {
							rawJSON += "\"@type\": " + "\"" + routinePackage + "." + routineName + "\"";
							if (paramTypesSplit.length > 0) {
								rawJSON += ",";
							} else {
								rawJSON += "}";
							}
							for (var i = 0; i < paramTypesSplit.length; i++) {
								switch (paramTypesSplit[i]) {
									case "int": {
										rawJSON += "\"" + paramNamesSplit[i] + "\"" + " : " + "0";
										if (i != paramTypesSplit.length - 1) {
											rawJSON += ",";
										}
										continue;
									}
									case "double": {
										rawJSON += "\"" + paramNamesSplit[i] + "\"" + " : " + "0.0";
										if (i != paramTypesSplit.length - 1) {
											rawJSON += ",";
										}
										continue;
									}
									case "String": {
										rawJSON += "\"" + paramNamesSplit[i] + "\"" + " : " + "\"string\"";
										if (i != paramTypesSplit.length - 1) {
											rawJSON += ",";
										}
										continue;
									}
									case "boolean": {
										rawJSON += "\"" + paramNamesSplit[i] + "\"" + " : " + "false";
										if (i != paramTypesSplit.length - 1) {
											rawJSON += ",";
										}
										continue;
									}
									case "Path": {
										rawJSON += "\"" + paramNamesSplit[i] + "\"" + " : "
												+ "[{\"Waypoint\":[{\"x\":0},{\"y\":0},{\"speed\":0}]}]";
										if (i != paramTypesSplit.length - 1) {
											rawJSON += ",";
										}
										continue;
									}
									case "SparkDriveSignal": {
										rawJSON += "\"" + paramNamesSplit[i] + "\"" + " : "
												+ "[{\"leftOutput\":0.0},{\"rightOutput\":0.0}]";
										if (i != paramTypesSplit.length - 1) {
											rawJSON += ",";
										}
										continue;
									}
								}
								String[] chanonSplit = paramChanonSplit[i].split("\\.");
								String enumName = chanonSplit[chanonSplit.length - 1];
								String subsystemName = chanonSplit[chanonSplit.length - 2];
								Class subClass = Class.forName("com.palyrobotics.frc2019.subsystems." + subsystemName);

								// Field enumVar = subClass.getField(enumName);
								Class[] classes = subClass.getClasses();
								for (var j = 0; j < classes.length; j++) {
									String simpleName = classes[j].getSimpleName();
									if (simpleName.equals(enumName)) {
										String enumString = "";
										for (var x = 0; x < classes[j].getEnumConstants().length; x++) {
											enumString += classes[j].getEnumConstants()[x].toString() + " ";
										}
										rawJSON += "\"" + paramNamesSplit[i] + "\"" + " : " + "\""
												+ enumString.split(" ")[0] + "\"";
										if (i != paramTypesSplit.length - 1) {
											rawJSON += ",";
										}
									}

								}

							}
							rawJSON += "}";
							return rawJSON;
						}
					} else {
						return "{\"@type\":\"ParallelRoutine\",\"routines\":[]}";
					}


				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					return "e";
				}

			}
			case "deserializeAuto": {
				String autoString = parse.getString("auto_string");
				String autoName = parse.getString("auto_name");
				ObjectMapper mapper = new ObjectMapper();
				try {
					JsonNode node = mapper.readTree(autoString);
					DeserializeAutos.getInstance().deserialize(autoName, node);
					return "success";
				} catch (IOException | JSONException e) {
					e.printStackTrace();
					return "failure";
				}

			}
			case "getConfigFromName": {
				String configName = parse.getString("config_name");
				Class<? extends AbstractConfig> configClass = Configs.getClassFromName(configName);
				System.out.println(configClass.toString());
				return "temp";
			}
			case "getAuto": {

				String autoIndex = parse.getString("auto_index");
				int autoIndexInt = Integer.parseInt(autoIndex);

				ObjectMapper mapper = new ObjectMapper().configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

				mapper.enableDefaultTyping();
				mapper.configure(SerializationConfig.Feature.AUTO_DETECT_GETTERS, false);
				mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
				SimpleModule module = new SimpleModule("routineSerialization", Version.unknownVersion());
				module.addSerializer(Path.class, new PathSerializer(Path.class));
				module.addSerializer(SparkDriveSignal.class, new SparkDriveSignalSerializer(SparkDriveSignal.class));
				mapper.registerModule(module);
				Routine autoRoutine = AutoModeSelector.getInstance().getAutoModeByIndex(autoIndexInt).getRoutine();
				try {
					return mapper.writeValueAsString(autoRoutine);
				} catch (IOException e) {
					return "cannot parse this routine";
				}

			}
			case "getRoutine": {
				String routineName = parse.getString("routine_name");
				String routinePackage = parse.getString("routine_package");
				try {
					Class aClass;
					if (routinePackage != "null") {
						aClass = Class.forName(
								"com.palyrobotics.frc2019.behavior.routines." + routinePackage + "." + routineName);
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
						for (var i = 0; i < aClass.getConstructors()[constructorIndex]
								.getParameterTypes().length; i++) {
							paramTypes += aClass.getConstructors()[constructorIndex].getParameterTypes()[i]
									.getSimpleName() + ",";
						}
						return paramTypes;
					} else {
						return "constructor not found";
					}

				} catch (ClassNotFoundException e) {
					return "e";
				}

			}
			case "getAutoCount": {
				return Integer.toString(AutoModeSelector.getInstance().getAutoModeList().size());
			}
			case "getAutoName": {
				String autoIndex = parse.getString("auto_index");
				return (AutoModeSelector.getInstance().getAutoModeByIndex(Integer.parseInt(autoIndex)).getClass()
						.getSimpleName());
			}
			case "getAllRoutines": {
				File routinesFile = new File(
						System.getProperty("user.dir") + "/src/main/java/com/palyrobotics/frc2019/behavior/routines/");
				String allRoutines = "";
				for (var i = 0; i < routinesFile.listFiles().length; i++) {
					if (routinesFile.listFiles()[i].toString().contains(".java")) {
						String[] splitFileName = routinesFile.list()[i].split("\\.");
						try {
							allRoutines += (splitFileName[0]) + " ";
						} catch (ArrayIndexOutOfBoundsException e) {
							return "index out of bounds";
						}
					} else {
						File subFolder = routinesFile.listFiles()[i];
						for (var x = 0; x < subFolder.listFiles().length; x++) {
							if (subFolder.listFiles()[x].toString().contains(".java")) {
								String[] splitFileName = subFolder.list()[x].split("\\.");
								try {
									allRoutines += (splitFileName[0]) + "." + routinesFile.list()[i] + " ";
								} catch (ArrayIndexOutOfBoundsException e) {
									return "index out of bounds";
								}

							}
						}
					}
				}
				System.out.println(routinesFile.list()[1]);
				return allRoutines;
			}
			case "get":
			case "set":
			case "save":
			case "editArray":
			case "reload": {
				String configName = parse.getString("config_name");
				if (commandName.equals("get") && configName.equals("Configs")) {
					return String.join(",", Configs.getActiveConfigNames());
				}
				try {
					Class<? extends AbstractConfig> configClass = Configs.getClassFromName(configName);
					if (configClass == null)
						throw new ClassNotFoundException();
					AbstractConfig configObject = Configs.get(configClass);
					String allFieldNames = parse.getString("config_field");
					try {
						switch (commandName) {
							case "set":
							case "get": {
								String[] fieldNames = allFieldNames == null ? null : allFieldNames.split("\\.");
								Object fieldValue = configObject, fieldParentValue = null;
								Field field = null;
								if (fieldNames != null && fieldNames.length != 0) {
									for (String fieldName : fieldNames) {
										field = getField(field == null ? configClass : field.getType(), fieldName);
										fieldParentValue = fieldValue;
										fieldValue = field.get(fieldValue);
									}
								}
								switch (commandName) {
									case "get": {
										String display;
										try {
											display = sMapper.defaultPrettyPrintingWriter()
													.writeValueAsString(fieldValue);
										} catch (IOException ignored) {
											display = fieldValue.toString();
										}
										return parse.getBoolean("raw") ? display
												: String.format("[%s] %s: %s", configName,
														allFieldNames == null ? "all" : allFieldNames, display);
									}
									case "set": {
										if (field == null)
											return "Can't set entire config file yet!";
										String stringValue = parse.getString("config_value");
										if (stringValue == null)
											return "Must provide a value to set!";
										try {
											Object newFieldValue = sMapper.readValue(stringValue, field.getType());
											Configs.set(configObject, fieldParentValue, field, newFieldValue);
											return String.format("Set field %s on config %s to %s", allFieldNames,
													configName, stringValue);
										} catch (IOException parseException) {
											return String.format("Error parsing %s for field %s on config %s",
													stringValue, allFieldNames, configName);
										}
									}
									default: {
										throw new RuntimeException();
									}
								}
							}
							case "editArray": {
								String[] fieldNames = allFieldNames == null ? null : allFieldNames.split("\\.");
								Object fieldValue = configObject, fieldParentValue = null;
								Field field = null;
								if (fieldNames != null && fieldNames.length != 0) {

									field = getField(field == null ? configClass : field.getType(), fieldNames[0]);
									fieldParentValue = fieldValue;
									fieldValue = field.get(fieldValue);

								} else {
									return "No fields inputted";
								}
								File configFile = Paths.get(System.getProperty("user.dir"), "src/main/deploy/config",
										configName + ".json").toFile();
								BufferedReader bR = new BufferedReader(new FileReader(configFile));
								String line = bR.readLine();
								String jsonString = "";
								while (line != null) {
									jsonString += line;
									line = bR.readLine();
								}
								JSONArray jArray;
								try {
									JSONObject jObject = new JSONObject(jsonString);
									jArray = jObject.getJSONArray(fieldNames[0]);
								} catch (JSONException e) {
									return "jsonexception";
								}

								// return jObject.toString();

								ArrayList newVal = new ArrayList();
								for (var i = 0; i < jArray.length(); i++) {
									try {
										newVal.add(jArray.get(i));
									} catch (JSONException e) {
										return "bruh";
									}

								}
								ArrayList waypoint = new ArrayList();
								waypoint.add(Double.valueOf(parse.getString("config_value")));
								waypoint.add(Double.valueOf(parse.getString("config_value_two")));

								newVal.add(waypoint);
								Configs.set(configObject, fieldParentValue, field, newVal);
								return parse.getString("config_value") + parse.getString("config_value_two");
								// return fieldValue.toString();
							}
							case "save": {
								try {
									Configs.saveOrThrow(configClass);
									return String.format("Saved config for %s", configName);
								} catch (IOException saveException) {
									saveException.printStackTrace();
									return String.format("File system error saving config %s - this should NOT happen!",
											configName);
								}
							}
							case "reload": {
								boolean didReload = Configs.reload(configClass);
								return String.format(didReload ? "Reloaded config %s" : "Did not reload config %s",
										configName);
							}
							default: {
								throw new RuntimeException();
							}
						}
					} catch (NoSuchFieldException noFieldException) {
						return String.format("Error getting field %s, it does not exist!", allFieldNames);
					} catch (IllegalAccessException | IllegalArgumentException illegalAccessException) {
						illegalAccessException.printStackTrace();
						return String.format("Error setting field %s", allFieldNames);
					} catch (JsonParseException e) {
						return "parse exception";
					} catch (JsonMappingException e) {
						return "mapping exception";
					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (ClassNotFoundException configException) {
					return String.format("Unknown config class %s", configName);
				}
			}
			case "run": {
				String routineName = parse.getString("routine_name");
				switch (routineName) {
					case "measure_elevator_speed": {
						try {
							double percentOutput = Double.parseDouble(parse.<String>getList("parameters").get(0));
							RoutineManager.getInstance().addNewRoutine(new ElevatorMeasureSpeedAtOutputRoutine(
									percentOutput, Configs.get(ElevatorConfig.class).feedForward, -10));
							return String.format("Starting measure elevator routine with percent output %f",
									percentOutput);
						} catch (Exception exception) {
							throw new ArgumentParserException("Could not parse parameters", exception, mParser);
						}
					}
					default: {
						throw new ArgumentParserException("Routine not recognized!", mParser);
					}
				}
			}
			case "calibrate": {
				double potentiometer = HardwareAdapter.getInstance().getIntake()
						.calibrateIntakeEncoderWithPotentiometer();
				return String.format("Calibrated intake with potentiometer value %f%n", potentiometer);
			}
			default: {
				throw new RuntimeException();
			}
		}
	}
}
