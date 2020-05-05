package info.curtbinder.arduino.tool;

import processing.app.Editor;
import processing.app.BaseNoGui;
import processing.app.SketchFile;
import processing.app.tools.Tool;
import processing.app.helpers.PreferencesMapException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class CBTool implements Tool {
	private static final String VERSION = "2.0.0";
	private static final String NAME = "CBTool";
	private static final String FILE_HEADER = "" +
			"/*\n" +
			" * Copyright 2020 Reef Angel\n" +
			" *\n" +
			" * Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
			" * you may not use this file except in compliance with the License.\n" +
			" * You may obtain a copy of the License at\n" +
			" *\n" +
			" * http://www.apache.org/licenses/LICENSE-2.0\n" +
			" *\n" +
			" * Unless required by applicable law or agreed to in writing, software\n" +
			" * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
			" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
			" * See the License for the specific language governing permissions and\n" +
			" * limitations under the License.\n" +
			" */\n" +
			"\n";
	private static final String LABEL_HEADER = "" +
			"#ifndef __RA_CUSTOMLABELS_H__\n" +
			"#define __RA_CUSTOMLABELS_H__\n";
	private static final String FEATURES_HEADER = "" +
			"#ifndef __REEFANGEL_FEATURES_H__\n" +
			"#define __REEFANGEL_FEATURES_H__\n";
	private static final String FEATURES_FOOTER = "" +
			"\n" +
			"#endif  // __REEFANGEL_FEATURES_H__\n";
	private static final String LABEL_FILE_FOOTER = "" +
			"// Buttons\n" +
			"const char CUSTOMLABELOKBUTTON[] PROGMEM = \"Ok\";\n" +
			"const char CUSTOMLABELCANCELBUTTON[] PROGMEM = \"Cancel\";\n" +
			"\n" +
			"// Arrays\n" +
			"static PROGMEM const char * const LABEL_PWME[] = {LABEL_PWME0,LABEL_PWME1,LABEL_PWME2,LABEL_PWME3,LABEL_PWME4,LABEL_PWME5};\n" +
			"static PROGMEM const char * const LABEL_WL[] = {LABEL_WL0,LABEL_WL1,LABEL_WL2,LABEL_WL3,LABEL_WL4};\n" +
			"static PROGMEM const char * const LABEL_IO[] = {LABEL_IO0,LABEL_IO1,LABEL_IO2,LABEL_IO3,LABEL_IO4,LABEL_IO5};\n" +
			"static PROGMEM const char * const LABEL_C[] = {LABEL_C0,LABEL_C1,LABEL_C2,LABEL_C3,LABEL_C4,LABEL_C5,LABEL_C6,LABEL_C7};\n" +
			"static PROGMEM const char * const LABEL_AI[] = {LABEL_AI_WHITE, LABEL_AI_BLUE, LABEL_AI_ROYAL_BLUE};\n" +
			"static PROGMEM const char * const LABEL_RF[] = {LABEL_RF_WHITE, LABEL_RF_ROYAL_BLUE, LABEL_RF_RED, LABEL_RF_BLUE, LABEL_RF_GREEN, LABEL_RF_INTENSITY};\n" +
			"static PROGMEM const char * const LABEL_RELAY[] = {RELAY_BOX_LABEL, EXP_RELAY_1_LABEL, EXP_RELAY_2_LABEL, EXP_RELAY_3_LABEL, EXP_RELAY_4_LABEL, EXP_RELAY_5_LABEL, EXP_RELAY_6_LABEL, EXP_RELAY_7_LABEL, EXP_RELAY_8_LABEL, PWM_EXPANSION_LABEL, RF_EXPANSION_LABEL, RF_EXPANSION_LABEL1, AI_LABEL, IO_EXPANSION_LABEL, DCPUMP_LABEL, CVAR_LABEL, STATUS_LABEL, ALERT_LABEL};\n" +
			"static PROGMEM const char * const LABEL_PORT[] = {LABEL_PORT1,LABEL_PORT2,LABEL_PORT3,LABEL_PORT4,LABEL_PORT5,LABEL_PORT6,LABEL_PORT7,LABEL_PORT8,LABEL_PORT11,LABEL_PORT12,LABEL_PORT13,LABEL_PORT14,LABEL_PORT15,LABEL_PORT16,LABEL_PORT17,LABEL_PORT18,LABEL_PORT21,LABEL_PORT22,LABEL_PORT23,LABEL_PORT24,LABEL_PORT25,LABEL_PORT26,LABEL_PORT27,LABEL_PORT28,LABEL_PORT31,LABEL_PORT32,LABEL_PORT33,LABEL_PORT34,LABEL_PORT35,LABEL_PORT36,LABEL_PORT37,LABEL_PORT38,LABEL_PORT41,LABEL_PORT42,LABEL_PORT43,LABEL_PORT44,LABEL_PORT45,LABEL_PORT46,LABEL_PORT47,LABEL_PORT48,LABEL_PORT51,LABEL_PORT52,LABEL_PORT53,LABEL_PORT54,LABEL_PORT55,LABEL_PORT56,LABEL_PORT57,LABEL_PORT58,LABEL_PORT61,LABEL_PORT62,LABEL_PORT63,LABEL_PORT64,LABEL_PORT65,LABEL_PORT66,LABEL_PORT67,LABEL_PORT68,LABEL_PORT71,LABEL_PORT72,LABEL_PORT73,LABEL_PORT74,LABEL_PORT75,LABEL_PORT76,LABEL_PORT77,LABEL_PORT78,LABEL_PORT81,LABEL_PORT82,LABEL_PORT83,LABEL_PORT84,LABEL_PORT85,LABEL_PORT86,LABEL_PORT87,LABEL_PORT88};\n" +
			"static PROGMEM const char * const LABEL_ALERT[] = {ALERT_ATO_TIMEOUT_LABEL,ALERT_OVERHEAT_LABEL,ALERT_BUSLOCK_LABEL,ALERT_LEAK_LABEL};\n" +
			"static PROGMEM const char * const LABEL_CUSTOM_EXP[] = {LABEL_CUSTOM_EXP0,LABEL_CUSTOM_EXP1,LABEL_CUSTOM_EXP2,LABEL_CUSTOM_EXP3,LABEL_CUSTOM_EXP4,LABEL_CUSTOM_EXP5,LABEL_CUSTOM_EXP6,LABEL_CUSTOM_EXP7};\n" +
			"\n" +
			"#endif  // __RA_CUSTOMLABELS_H__\n";
	private static final String LABEL_ITEM_TEMPLATE = "const char %s[] PROGMEM = \"%s\";\n";
	private static final String FEATURE_ITEM_TEMPLATE = "#define %s\n";
	private static final String LABEL_LINE_START = "// RA_LABEL ";
	private LinkedHashMap<String, String> mapLabels;

	private final int ID_DEFINE = 0;
	private final int ID_KEYWORD = 1;
	private final int ID_DESCRIPTION = 2;
	private ArrayList<String[]> listFeatures;
	private ArrayList<String> listDetectedFeatures;

	Editor editor;
	
	public void init(Editor editor) {
		this.editor = editor;
		mapLabels = buildDefaultLabels();
		listFeatures = buildDefaultFeatures();
		listDetectedFeatures = new ArrayList<>();
	}
	
	public String getMenuTitle() {
		return "CB Reef Angel Compile";
	}
	
	private String getVersionString() {
		return "" + NAME + " v" + VERSION;
	}

	public void run() {
		updateStatus("Processing code for Features and Labels...");
		System.out.println(getVersionString());

		try {
			System.out.println("Generating Features file from " + getFileName());
			String program = getProgram();
			processFeatures(program);
			String featuresFile = generateFeaturesFile();
			saveFile(featuresFile, getOutputFeaturesFile());
			System.out.println("\nFinished detecting Features.\nGenerating Custom Labels file from " + getFileName());
			processLabels(program);
			String labelsFile = generateLabelsFile();
			saveFile(labelsFile, getOutputLabelsFile());
			System.out.println("\nFinished detecting Labels.");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		updateStatus("Finished.");
		System.out.println("Starting compile.");

		// compile
		Runnable presentHandler = new LocalBuildHandler(editor, false);
		Runnable runHandler = new LocalBuildHandler(editor);
		editor.handleRun(false, presentHandler, runHandler);
	}

	class LocalBuildHandler implements Runnable {

		private final boolean verbose;
		private final boolean saveHex;
		private final Editor editor;

		public LocalBuildHandler(Editor e) {
			this(e,false);
		}

		public LocalBuildHandler(Editor e, boolean verbose) {
			this(e, verbose, false);
		}

		public LocalBuildHandler(Editor e, boolean verbose, boolean saveHex) {
			this.editor = e;
			this.verbose = verbose;
			this.saveHex = saveHex;
		}

		@Override
		public void run() {
			try {
				editor.removeAllLineHighlights();
				editor.getSketchController().build(verbose, saveHex);
				editor.statusNotice("Done compiling.");
			} catch (PreferencesMapException e) {
				editor.statusError("Error while compiling: missing configuration parameter");
			} catch (Exception e) {
				// status.unprogress();
				editor.statusError(e);
			}

			// status.unprogress();
			// cannot deactivate toolbar due to private access
			//toolbar.deactivateRun();
//			editor.statusError("Done compiling.");
			editor.avoidMultipleOperations = false;
		}
	}

	private void updateStatus(String msg) {
		editor.statusNotice(msg);
	}

	private String getProgram() {
		return editor.getCurrentTab().getSketchFile().getProgram();
	}

	private String getFileName() {
		return editor.getCurrentTab().getSketchFile().getFileName();
	}

	private String getOutputFeaturesFile() {
		return BaseNoGui.getSketchbookFolder() + "/libraries/ReefAngel_Features/ReefAngel_Features.h";
	}

	private String getOutputLabelsFile() {
		return BaseNoGui.getSketchbookFolder() + "/libraries/RA_CustomLabels/RA_CustomLabels.h";
	}

	private void processLabels(String code) {
		// Look through the lines of code for the labels
		// Then update the label values
		String[] lines = code.split("\n");
		for (String s : lines) {
			if (s.startsWith(LABEL_LINE_START)) {
				// Make sure we only look at the label lines
				String[] parts = s.substring(LABEL_LINE_START.length()).split("=");
				// look up the label
				if (parts.length > 1) {
					// parts[0] - KEY (label)
					// parts[1] - value (string to be used)
					updateLabelValue(parts[0], parts[1]);
				}
			}
		}
	}

	private void updateLabelValue(String label, String value) {
		if (mapLabels.containsKey(label)) {
			System.out.println("Found Label: " + label + " - " + value);
			mapLabels.replace(label, value);
		} else {
			// Unknown label
			System.out.println("Unknown Label Found: " + label + " - " + value);
		}
	}

	private LinkedHashMap<String, String> buildDefaultLabels() {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("RELAY_BOX_LABEL", "Relay Box");
		map.put("EXP_RELAY_1_LABEL", "Exp. Relay Box 1");
		map.put("EXP_RELAY_2_LABEL", "Exp. Relay Box 2");
		map.put("EXP_RELAY_3_LABEL", "Exp. Relay Box 3");
		map.put("EXP_RELAY_4_LABEL", "Exp. Relay Box 4");
		map.put("EXP_RELAY_5_LABEL", "Exp. Relay Box 5");
		map.put("EXP_RELAY_6_LABEL", "Exp. Relay Box 6");
		map.put("EXP_RELAY_7_LABEL", "Exp. Relay Box 7");
		map.put("EXP_RELAY_8_LABEL", "Exp. Relay Box 8");
		map.put("PWM_EXPANSION_LABEL", "PWM Expansion");
		map.put("SIXTEENCH_PWM_EXPANSION_LABEL", "16 Ch PWM Expansion");
		map.put("RF_EXPANSION_LABEL", "RF Expansion");
		map.put("RF_EXPANSION_LABEL1", "RF Expansion");
		map.put("AI_LABEL", "Aqua Illumination");
		map.put("IO_EXPANSION_LABEL", "IO Expansion");
		map.put("DCPUMP_LABEL", "DC Pump");
		map.put("DIMMING_LABEL", "Dimming");
		map.put("INPUT_LABEL", "Input");
		map.put("CVAR_LABEL", "Custom Variables");
		map.put("STATUS_LABEL", "Status");
		map.put("ALERT_LABEL", "Alert");
		map.put("ALERT_LIGHTS_ON_LABEL", "Lights On");
		map.put("ALERT_ATO_TIMEOUT_LABEL", "ATO Timeout");
		map.put("ALERT_OVERHEAT_LABEL", "Overheat");
		map.put("ALERT_BUSLOCK_LABEL", "Bus Lock");
		map.put("ALERT_LEAK_LABEL", "Water Leak");
		map.put("LABEL_TEMP1", "Temp 1");
		map.put("LABEL_TEMP1_SHORT", "T1");
		map.put("LABEL_TEMP2", "Temp 2");
		map.put("LABEL_TEMP2_SHORT", "T2");
		map.put("LABEL_TEMP3", "Temp 3");
		map.put("LABEL_TEMP3_SHORT", "T3");
		map.put("LABEL_TEMP4", "Temp 4");
		map.put("LABEL_TEMP4_SHORT", "T4");
		map.put("LABEL_TEMP5", "Temp 5");
		map.put("LABEL_TEMP5_SHORT", "T5");
		map.put("LABEL_TEMP6", "Temp 6");
		map.put("LABEL_TEMP6_SHORT", "T6");
		map.put("LABEL_PH", "pH");
		map.put("LABEL_ATOLOW", "Low ATO");
		map.put("LABEL_ATOHIGH", "High ATO");
		map.put("LABEL_ALARM", "Alarm");
		map.put("LABEL_LEAK", "Leak");
		map.put("LABEL_DAYLIGHT", "Daylight");
		map.put("LABEL_DAYLIGHT2", "Daylight 2");
		map.put("LABEL_ACTINIC", "Actinic");
		map.put("LABEL_ACTINIC2", "Actinic 2");
		map.put("LABEL_SALINITY", "Salinity");
		map.put("LABEL_SALINITY_SHORT", "Sal");
		map.put("LABEL_ORP", "ORP");
		map.put("LABEL_PHEXP", "pH Exp");
		map.put("LABEL_PHEXP_SHORT", "pHE");
		map.put("LABEL_HUMIDITY", "Humidity");
		map.put("LABEL_HUMIDITY_SHORT", "Hum");
		map.put("LABEL_PAR", "PAR");
		map.put("LABEL_CUSTOM_EXP0", "Exp 0");
		map.put("LABEL_CUSTOM_EXP1", "Exp 1");
		map.put("LABEL_CUSTOM_EXP2", "Exp 2");
		map.put("LABEL_CUSTOM_EXP3", "Exp 3");
		map.put("LABEL_CUSTOM_EXP4", "Exp 4");
		map.put("LABEL_CUSTOM_EXP5", "Exp 5");
		map.put("LABEL_CUSTOM_EXP6", "Exp 6");
		map.put("LABEL_CUSTOM_EXP7", "Exp 7");
		map.put("LABEL_WL_CHANNEL", "WL Ch");
		map.put("LABEL_WL0", "Water");
		map.put("LABEL_WL1", "WL 1");
		map.put("LABEL_WL2", "WL 2");
		map.put("LABEL_WL3", "WL 3");
		map.put("LABEL_WL4", "WL 4");
		map.put("LABEL_PWME_CHANNEL", "Dimming Ch");
		map.put("LABEL_PWME0", "Channel 0");
		map.put("LABEL_PWME1", "Channel 1");
		map.put("LABEL_PWME2", "Channel 2");
		map.put("LABEL_PWME3", "Channel 3");
		map.put("LABEL_PWME4", "Channel 4");
		map.put("LABEL_PWME5", "Channel 5");
		map.put("LABEL_IO_CHANNEL", "I/O Ch");
		map.put("LABEL_IO0", "I/O Channel 0");
		map.put("LABEL_IO1", "I/O Channel 1");
		map.put("LABEL_IO2", "I/O Channel 2");
		map.put("LABEL_IO3", "I/O Channel 3");
		map.put("LABEL_IO4", "I/O Channel 4");
		map.put("LABEL_IO5", "I/O Channel 5");
		map.put("LABEL_C0", "Custom Var 0");
		map.put("LABEL_C1", "Custom Var 1");
		map.put("LABEL_C2", "Custom Var 2");
		map.put("LABEL_C3", "Custom Var 3");
		map.put("LABEL_C4", "Custom Var 4");
		map.put("LABEL_C5", "Custom Var 5");
		map.put("LABEL_C6", "Custom Var 6");
		map.put("LABEL_C7", "Custom Var 7");
		map.put("LABEL_PORT1", "Port 1");
		map.put("LABEL_PORT2", "Port 2");
		map.put("LABEL_PORT3", "Port 3");
		map.put("LABEL_PORT4", "Port 4");
		map.put("LABEL_PORT5", "Port 5");
		map.put("LABEL_PORT6", "Port 6");
		map.put("LABEL_PORT7", "Port 7");
		map.put("LABEL_PORT8", "Port 8");
		map.put("LABEL_PORT11", "Port 11");
		map.put("LABEL_PORT12", "Port 12");
		map.put("LABEL_PORT13", "Port 13");
		map.put("LABEL_PORT14", "Port 14");
		map.put("LABEL_PORT15", "Port 15");
		map.put("LABEL_PORT16", "Port 16");
		map.put("LABEL_PORT17", "Port 17");
		map.put("LABEL_PORT18", "Port 18");
		map.put("LABEL_PORT21", "Port 21");
		map.put("LABEL_PORT22", "Port 22");
		map.put("LABEL_PORT23", "Port 23");
		map.put("LABEL_PORT24", "Port 24");
		map.put("LABEL_PORT25", "Port 25");
		map.put("LABEL_PORT26", "Port 26");
		map.put("LABEL_PORT27", "Port 27");
		map.put("LABEL_PORT28", "Port 28");
		map.put("LABEL_PORT31", "Port 31");
		map.put("LABEL_PORT32", "Port 32");
		map.put("LABEL_PORT33", "Port 33");
		map.put("LABEL_PORT34", "Port 34");
		map.put("LABEL_PORT35", "Port 35");
		map.put("LABEL_PORT36", "Port 36");
		map.put("LABEL_PORT37", "Port 37");
		map.put("LABEL_PORT38", "Port 38");
		map.put("LABEL_PORT41", "Port 41");
		map.put("LABEL_PORT42", "Port 42");
		map.put("LABEL_PORT43", "Port 43");
		map.put("LABEL_PORT44", "Port 44");
		map.put("LABEL_PORT45", "Port 45");
		map.put("LABEL_PORT46", "Port 46");
		map.put("LABEL_PORT47", "Port 47");
		map.put("LABEL_PORT48", "Port 48");
		map.put("LABEL_PORT51", "Port 51");
		map.put("LABEL_PORT52", "Port 52");
		map.put("LABEL_PORT53", "Port 53");
		map.put("LABEL_PORT54", "Port 54");
		map.put("LABEL_PORT55", "Port 55");
		map.put("LABEL_PORT56", "Port 56");
		map.put("LABEL_PORT57", "Port 57");
		map.put("LABEL_PORT58", "Port 58");
		map.put("LABEL_PORT61", "Port 61");
		map.put("LABEL_PORT62", "Port 62");
		map.put("LABEL_PORT63", "Port 63");
		map.put("LABEL_PORT64", "Port 64");
		map.put("LABEL_PORT65", "Port 65");
		map.put("LABEL_PORT66", "Port 66");
		map.put("LABEL_PORT67", "Port 67");
		map.put("LABEL_PORT68", "Port 68");
		map.put("LABEL_PORT71", "Port 71");
		map.put("LABEL_PORT72", "Port 72");
		map.put("LABEL_PORT73", "Port 73");
		map.put("LABEL_PORT74", "Port 74");
		map.put("LABEL_PORT75", "Port 75");
		map.put("LABEL_PORT76", "Port 76");
		map.put("LABEL_PORT77", "Port 77");
		map.put("LABEL_PORT78", "Port 78");
		map.put("LABEL_PORT81", "Port 81");
		map.put("LABEL_PORT82", "Port 82");
		map.put("LABEL_PORT83", "Port 83");
		map.put("LABEL_PORT84", "Port 84");
		map.put("LABEL_PORT85", "Port 85");
		map.put("LABEL_PORT86", "Port 86");
		map.put("LABEL_PORT87", "Port 87");
		map.put("LABEL_PORT88", "Port 88");
		map.put("LABEL_AI_WHITE", "White");
		map.put("LABEL_AI_BLUE", "Blue");
		map.put("LABEL_AI_ROYAL_BLUE", "R. Blue");
		map.put("LABEL_RF_WHITE", "White");
		map.put("LABEL_RF_ROYAL_BLUE", "R. Blue");
		map.put("LABEL_RF_RED", "Red");
		map.put("LABEL_RF_GREEN", "Green");
		map.put("LABEL_RF_BLUE", "Blue");
		map.put("LABEL_RF_INTENSITY", "Intensity");
		return map;
	}

	private String generateLabelsFile() {
		StringBuilder sb = new StringBuilder();
		sb.append(getAutoGeneratedString(getFileName()));
		sb.append("\n");
		sb.append(FILE_HEADER);
		sb.append(LABEL_HEADER);
		sb.append("\n");

		// loop through the labels
		for (Map.Entry<String, String> entry : mapLabels.entrySet()) {
			sb.append(generateLabelItem(entry.getKey(), entry.getValue()));
		}

		sb.append("\n");
		sb.append(LABEL_FILE_FOOTER);

		return sb.toString();
	}

	private String getAutoGeneratedString(String filename) {
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		return "// AutoGenerated file by Arduino (Reef Angel)\n" +
				"// Generated on " + df.format(LocalDateTime.now()) + " for INO file: " + filename + "\n";
	}

	private String generateLabelItem(String variableName, String value){
		return String.format(LABEL_ITEM_TEMPLATE, variableName, value);
	}

	private void saveFile(String fileContents, String fileName) throws IOException {
		BaseNoGui.saveFile(fileContents, new File(fileName));
	}

	private ArrayList<String[]> buildDefaultFeatures() {
		ArrayList<String[]> list = new ArrayList<>();

		try {
			FileReader fr = new FileReader(getDefaultFeaturesFilename());
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
				list.add(items);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
	}

	private String getDefaultFeaturesFilename() {
		return BaseNoGui.getSketchbookPath() + "/update/feature.txt";
	}

	private void processFeatures(String code) {
		System.out.println("The following features were automatically added:");
		addFeature(new String[]{"WDT", "", "Watchdog Timer"});
		addFeature(new String[]{"VersionMenu", "", "Version Menu"});
		System.out.println("\nThe following features were detected:");

		// loop through all the features and search for them in the code
		for (String[] feature : listFeatures) {
			if (code.contains(feature[ID_KEYWORD])) {
				addFeature(feature);
			}
		}

		// handle the specific cases
		checkForRelayExpansions(code);
		checkForCustomMenu(code);
	}

	private void checkForRelayExpansions(String code) {
		// Search for REGEX pattern "Box#_" to determine relay modules
		String box;
		int numrelays = 0;
		for (int i = 1; i <= 8; ++i) {
			box = String.format("Box%d_", i);
			if (code.contains(box)) {
				numrelays = i;
			}
		}
		if (numrelays > 0){
			addFeature(new String[]{"InstalledRelayExpansionModules " + numrelays,
					"",
					"Number of Relay Expansion Modules: " + numrelays});
		}
	}

	private void checkForCustomMenu(String code) {
		String menu;
		int menuentries = 0;
		for (int i = 1; i <= 9; ++i) {
			menu = String.format("MenuEntry%d", i);
			if (code.contains(menu)) {
				menuentries = i;
			}
		}
		if (menuentries > 0) {
			// Custom menu created
			addFeature(new String[]{"CUSTOM_MENU_ENTRIES " + menuentries,
					"",
					"Number of Menu Options: " + menuentries});
		} else {
			// Check for default menus
			if (code.contains("ReefAngel.AddStandardMenu")) {
				// Standard Menu
				System.out.println("Standard Menu");
				addFeature(new String[]{"WavemakerSetup", "", "Wavemaker Menu"});
				addFeature(new String[]{"ATOSetup", "", "ATO Menu"});
				addFeature(new String[]{"OverheatSetup", "", "Overheat Menu"});
				addFeature(new String[]{"StandardLightSetup", "", "Standard Light Menu"});
			} else {
				// Simple Menu
				addFeature(new String[]{"SIMPLE_MENU", "", "Simple Menu"});
			}
		}
	}

	private void addFeature(String[] feature) {
		// Check to see if the feature already exists
		if (!listDetectedFeatures.contains(feature[ID_DEFINE])) {
			System.out.println(feature[ID_DESCRIPTION]);
			listDetectedFeatures.add(feature[ID_DEFINE]);
		}
	}

	private String generateFeaturesFile() {
		StringBuilder sb = new StringBuilder();
		sb.append(getAutoGeneratedString(getFileName()));
		sb.append("\n");
		sb.append(FILE_HEADER);
		sb.append(FEATURES_HEADER);
		sb.append("\n");

		for (String s : listDetectedFeatures) {
			sb.append(generateFeatureItem(s));
		}

		sb.append("\n");
		sb.append(FEATURES_FOOTER);

		return sb.toString();
	}

	private String generateFeatureItem(String feature) {
		return String.format(FEATURE_ITEM_TEMPLATE, feature);
	}
}