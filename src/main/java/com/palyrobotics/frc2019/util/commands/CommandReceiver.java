package com.palyrobotics.frc2019.util.commands;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.*;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.palyrobotics.frc2019.behavior.RoutineManager;
import com.palyrobotics.frc2019.behavior.routines.elevator.ElevatorMeasureSpeedAtOutputRoutine;
import com.palyrobotics.frc2019.config.subsystem.ElevatorConfig;
import com.palyrobotics.frc2019.robot.HardwareAdapter;
import com.palyrobotics.frc2019.util.config.AbstractConfig;
import com.palyrobotics.frc2019.util.config.Configs;
import com.palyrobotics.frc2019.util.service.RobotService;

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
						System.out.println(simpleName + enumName);
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
			case "getConfigFromName": {
				String configName = parse.getString("config_name");
				Class<? extends AbstractConfig> configClass = Configs.getClassFromName(configName);
				System.out.println(configClass.toString());
				return "temp";
			}
			case "getRoutine": {
				String routineName = parse.getString("routine_name");
				String routinePackage = parse.getString("routine_package");
				System.out.println(routinePackage);
				File routineFile;
				String test = "";
				if (routinePackage.equals("null") == false) {
					test = "/src/main/java/com/palyrobotics/frc2019/behavior/routines/" + routinePackage + "/"
							+ routineName + ".java";

				} else {
					test = "/src/main/java/com/palyrobotics/frc2019/behavior/routines/" + routineName + ".java";
				}

				routineFile = new File(System.getProperty("user.dir") + test);
				try {
					BufferedReader bR = new BufferedReader(new FileReader(routineFile));
					String line = bR.readLine();
					while (line != null) {
						if (line.contains("public " + routineName)) {
							return line;
						}
						line = bR.readLine();
					}
					return "No Parameters";
					// return line;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return "error";
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
