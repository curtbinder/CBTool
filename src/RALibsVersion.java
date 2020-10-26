package info.curtbinder.arduino.tool;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class RALibsVersion {
    private static String LIBRARY_FILE = "/libraries/ReefAngel/ReefAngel.h";
    private static String VERSION_LABEL = "ReefAngel_Version ";
    private String libraryFile;
    private String libraryVersion;

    public RALibsVersion() {
        libraryFile = LIBRARY_FILE;
        libraryVersion = "";
    }

    public void init(String sketchbookPath) {
        libraryFile = sketchbookPath + LIBRARY_FILE;
    }

    // Returns TRUE if version found, FALSE if not found
    public boolean display() {
        boolean fRet = false;
        try {
            FileReader fr = new FileReader(libraryFile);
            BufferedReader br = new BufferedReader(fr);
            String line = null;
            String version = "";
            while ((line = br.readLine()) != null) {
                int index = line.indexOf(VERSION_LABEL);
                if (index > 0) {
                    // jump to end of version label to get the version
                    version = line.substring(index + VERSION_LABEL.length());
                    // remove quotes from version string
                    version = version.replace("\"", "");
                    break;
                }
            }
            br.close();
            System.out.print("Reef Angel Libraries Version: ");
            if (version.isEmpty()) {
                System.out.println("NONE");
            } else {
                System.out.println(version);
                libraryVersion = version;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Reef Angel Libraries not found.");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return fRet;
    }

    public String getLibraryVersion() {
        return libraryVersion;
    }
}
