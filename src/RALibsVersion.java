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
