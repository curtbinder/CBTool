package info.curtbinder.arduino.tool;

import processing.app.Editor;
import processing.app.BaseNoGui;
import processing.app.SketchFile;
import processing.app.tools.Tool;
import processing.app.helpers.PreferencesMapException;

import java.io.File;
import java.io.IOException;

public class CBTool implements Tool {
    private static final String VERSION = "2.2.0";
    private static final String NAME = "CBTool";

    Editor editor;
    RAFeatures featuresFile;
    RALabels labelsFile;

    public void init(Editor editor) {
        this.editor = editor;
        featuresFile = new RAFeatures();
        labelsFile = new RALabels();
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
        // TODO display libraries version
        if (!hasPrerequisites()) {
            // Failed to find proper files, do not proceed
            return;
        }
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
        featuresFile.init(BaseNoGui.getSketchbookFolder().toString(), getFileName());
        labelsFile.init(BaseNoGui.getSketchbookFolder().toString(), getFileName());
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
