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

import java.io.File;
import java.io.IOException;

public class CBTool implements Tool {
    private static final String VERSION = "2.3.0";
    private static final String NAME = "CBTool";

    Editor editor;
    RAFeatures featuresFile;
    RALabels labelsFile;
    RALibsVersion librariesVersion;

    public void init(Editor editor) {
        this.editor = editor;
        featuresFile = new RAFeatures();
        labelsFile = new RALabels();
        librariesVersion = new RALibsVersion();
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
            saveFile(labelsFile.generateFile(), labelsFile.getFileName());
            labelsFile.process(program);

            System.out.println("\nFinished detecting Labels.");
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
    }

    private void loadDefaults() {
        featuresFile.loadDefaults();
        labelsFile.loadDefaults();
    }

    private boolean hasPrerequisites() {
        if (! labelsFile.hasPrerequisites()) {
            return false;
        }
        if (! featuresFile.hasPrerequisites()) {
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
