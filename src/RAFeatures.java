/*
 * MIT License
 * Copyright (c) 2020 Curt Binder
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package info.curtbinder.arduino.tool;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class RAFeatures extends RABaseFile {
    private static final String FEATURES_HEADER = "" +
            "#ifndef __REEFANGEL_FEATURES_H__\n" +
            "#define __REEFANGEL_FEATURES_H__\n";
    private static final String FEATURES_FOOTER = "" +
            "\n" +
            "#endif  // __REEFANGEL_FEATURES_H__\n";
    private static final String FEATURE_ITEM_TEMPLATE = "#define %s\n";
    private static final String LIBRARY_FEATURES_FOLDER = "/libraries/ReefAngel_Features/";
    private static final String LIBRARY_FEATURES_FILENAME = "ReefAngel_Features.h";
    private static final String UPDATE_FEATURE_FOLDER = "/update/";
    private static final String UPDATE_FEATURE_FILENAME = "feature.txt";
    private static final String MAIN_FEATURE_URL = "https://curtbinder.info/reefangel/feature.txt";

    private final int ID_DEFINE = 0;
    private final int ID_KEYWORD = 1;
    private final int ID_DESCRIPTION = 2;
    private ArrayList<String[]> listFeatures;
    private ArrayList<String> listDetectedFeatures;
    private String baseFolder;
    private String sketchFileName;

    public RAFeatures() {
        baseFolder = "";
        sketchFileName = "";
    }

    public boolean hasPrerequisites() {
    	boolean fRet = false;
        
        // Create folder, if non existant
        File dir = new File(getLibraryFolder());
        if (! dir.exists() ) {
            System.out.println("Controller Features folder doesn't exist, creating it now.\n  --> " + getLibraryFolder());
            dir.mkdir();
        }

        int iDownloaded = 0;
        do {
            // Check for the features.txt file
            dir = new File(getDefaultFolder());
            if (! dir.exists()) {
                System.out.println("Creating missing update folder");
                dir.mkdir();
            } else {
                // Directory exists, now check if the master feature file exists
                File f = new File(getDefaultFilename());
                if (f.exists() && (f.length() > 0L) ) {
                    // File exists and the filesize is greather than 0 bytes
                    fRet = true;
                } // otherwise, file doesn't exist or is zero bytes
            }
            if (!fRet && (iDownloaded < 1)) {
                // We don't have the feature.txt file, so give a warning about it and offer to download it
                // But only attempt to download it once
                System.out.println("ERROR!  Missing main feature.txt file.");
                try {
                    downloadMainFeatureFile();
                    System.out.println("Downloaded feature.txt file. Re-checking.\n");
                } catch (IOException e) {
                    System.out.println("\nERROR!  Failed to download feature.txt file automatically.\n" +
                    "You must download it manually from: " + MAIN_FEATURE_URL +
                    "\nThen move it to this folder: " + getDefaultFolder() +
                    "\nAfter that, re-run the Process Sketch plugin");
                }
            }
            ++iDownloaded;
        } while ((iDownloaded < 2) && !fRet);
        return fRet;
    }

    private void downloadMainFeatureFile() throws IOException {
        // Downloads the main feature file to the default location
        System.out.println("Downloading " + MAIN_FEATURE_URL);
        System.out.println("Saving to " + getDefaultFilename());

        URL url = new URL(MAIN_FEATURE_URL);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(getDefaultFilename());
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

    public String getFileName() {
        return getLibraryFolder() + LIBRARY_FEATURES_FILENAME;
    }

    private String getLibraryFolder() {
        return baseFolder + LIBRARY_FEATURES_FOLDER;
    }

    private String getDefaultFilename() {
        return getDefaultFolder() + UPDATE_FEATURE_FILENAME;
    }

    private String getDefaultFolder() {
        return baseFolder + UPDATE_FEATURE_FOLDER;
    }

    public void init(String sketchFolder, String sketchFileName) {
    	baseFolder = sketchFolder;
    	this.sketchFileName = sketchFileName;
    }
    
    public void loadDefaults() {
   	    listFeatures = build();
    	listDetectedFeatures = new ArrayList<>();
    }

    private ArrayList<String[]> build() {
        ArrayList<String[]> list = new ArrayList<>();

        try {
            FileReader fr = new FileReader(getDefaultFilename());
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

    public void process(String code) {
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

    private void addFeature(String[] feature) {
        // Check to see if the feature already exists
        if (!listDetectedFeatures.contains(feature[ID_DEFINE])) {
            System.out.println(feature[ID_DESCRIPTION]);
            listDetectedFeatures.add(feature[ID_DEFINE]);
        }
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

    public String generateFile() {
        StringBuilder sb = new StringBuilder();
        sb.append(getAutoGeneratedString(sketchFileName));
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