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

import processing.app.Editor;
import processing.app.BaseNoGui;
import processing.app.SketchFile;
import processing.app.tools.Tool;
import processing.app.helpers.PreferencesMapException;

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class CBTool implements Tool {
    private static final String VERSION = "2.4.0";
    private static final String NAME = "CBTool";
    private static final String DOWNLOAD_URL = "https://curtbinder.info/reefangel/";
    private static final String LATEST_VERSION_FILENAME = "cbtool-version.txt";
    private static final String TOOLS_FOLDER = "/tools/";
    private static final String ZIP_FILENAME_FORMAT = "CBTool-v%s.zip";

    Editor editor;
    RAFeatures featuresFile;
    RALabels labelsFile;
    RALibsVersion librariesVersion;
    RACustomSettings customSettings;

    public void init(Editor editor) {
        this.editor = editor;
        featuresFile = new RAFeatures();
        labelsFile = new RALabels();
        librariesVersion = new RALibsVersion();
        customSettings = new RACustomSettings();
    }

    public String getMenuTitle() {
        return "CB Reef Angel Process Sketch";
    }

    private String getVersionString() {
        return "" + NAME + " v" + VERSION;
    }

    public void run() {
        System.out.println(getVersionString());
        init();
        librariesVersion.display();
        if (!hasPrerequisites()) {
            // Failed to find proper files, do not proceed
            return;
        }
        displayCodeVersionString();
        loadDefaults();
        updateStatus("Processing code for Features and Labels...");

        try {
            System.out.println("Generating Features file from " + getFileName());
            String program = getProgram();
            featuresFile.process(program);
            saveFile(featuresFile.generateFile(), featuresFile.getFileName());
            System.out.println("\nFinished detecting Features.\nGenerating Custom Labels file from " + getFileName());
            labelsFile.process(program);
            saveFile(labelsFile.generateFile(), labelsFile.getFileName());
            System.out.println("\nFinished detecting Labels.\nGenerating Cloud Authentication (if any) from " + getFileName());
            // TODO process custom settings and display output
            customSettings.process(program);
            saveFile(customSettings.generateFile(), customSettings.getFileName());
            System.out.println("\nFinished detecting Cloud Authentication.");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        updateStatus("Finished.");
        System.out.println("Ready to compile & upload.");

        // compile
        /*
        Runnable presentHandler = new LocalBuildHandler(editor, false);
        Runnable runHandler = new LocalBuildHandler(editor);
        editor.handleRun(false, presentHandler, runHandler);
        */
    }

    private void init() {
        featuresFile.init(BaseNoGui.getSketchbookPath(), getFileName());
        labelsFile.init(BaseNoGui.getSketchbookPath(), getFileName());
        librariesVersion.init(BaseNoGui.getSketchbookPath());
        customSettings.init(BaseNoGui.getSketchbookPath(), getFileName());
    }

    private void loadDefaults() {
        featuresFile.loadDefaults();
        labelsFile.loadDefaults();
        customSettings.loadDefaults();
    }

    private boolean hasPrerequisites() {
        if (! labelsFile.hasPrerequisites()) {
            return false;
        }
        if (! featuresFile.hasPrerequisites()) {
            return false;
        }
        if (! customSettings.hasPrerequisites()) {
            return false;
        }
        return true;
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

    private void saveFile(String fileContents, String fileName) throws IOException {
        BaseNoGui.saveFile(fileContents, new File(fileName));
    }

    private static String CODE_VERSION_START = "ReefAngel.SetCodeVersion(\"";
    private static String CODE_VERSION_END = "\");";

    private void displayCodeVersionString() {
        String program = getProgram();
        String version = "";
        /*
        Get Index of the function call to set the code version.
        From that index, we need to locate the closing part of the function call.
        The code version will be between the quotes.
        */
        int start_index = program.indexOf(CODE_VERSION_START);
        if (start_index > 0) {
            int end_index = program.indexOf(CODE_VERSION_END, start_index);
            version = program.substring(start_index + CODE_VERSION_START.length(), end_index);
        }
        System.out.print("User Code Version: ");
        if (version.isEmpty()) {
            System.out.println("NONE");
        } else {
            System.out.println(version);
        }
    }

    /*
    TODO
        - download latest version file (store in /tools/ folder)
        - read version from file
        - compare against current version
        - if versions match, do nothing
        - if versions do not match, display message box to download latest version
            - download latest version (store in /tools/ folder)
            - extract it in place (extract in /tools/ folder)
            - prompt to restart arduino to take effect
            - delete zip file
        - delete latest version file (stored in /tools/ folder)
    */
    /*
    private void checkForLatestVersion() {
        if (!downloadFile(LATEST_VERSION_FILENAME)) {
            System.out.println("Failed to check for latest plugin version.");
            return;
        }
        // have the file, lets read it
        String latest_version = "";
        try {
            FileReader fr = new FileReader(getToolsFolder() + LATEST_VERSION_FILENAME);
            BufferedReader br = new BufferedReader(fr);
            latest_version = br.readLine();
            br.close();
        } catch (IOException e) {
            // Failed to read file
            return;
        }
        // We should have latest file version now

    }
    */

    private String getToolsFolder() {
        return BaseNoGui.getSketchbookPath() + TOOLS_FOLDER;
    }

    private boolean downloadFile(String filename) {
        // Download the given file and save in /tools/ folder
        String file = DOWNLOAD_URL + filename;
        String outfile = getToolsFolder() + filename;
        System.out.println("\nDownloading '" + file + "'\nSaving to: '" + outfile + "'");
        boolean fRet = false;
        try {
            URL url = new URL(file);
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(outfile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();
            fRet = true;
        } catch (IOException e) {
            // Failed to download the file from IOException
        }
        return fRet;
    }

    /*
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
    */
}
