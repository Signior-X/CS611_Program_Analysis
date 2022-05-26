import soot.*;

public class PA1 {
    public static void main(String[] args) {
        String classPath = "."; // change to appropriate path to the testcase

        // Set up arguments for Soot
        String[] sootArgs = {
                "-cp", classPath, "-pp", // sets the class path for Soot
                // "-p", "jb", "use-original-names:true", // keep java variable names (compile
                // testcase with "javac
                // // -g:vars")
                "-f", "J", // dump jimple code
                "-main-class", "Test", // specify the main class
                "Test" // list the classes to analyze
        };

        // Create transformer for analysis
        AnalysisTransformer analysisTransformer = new AnalysisTransformer();

        // Add transformer to appropriate pack in PackManager; PackManager will run all
        // packs when soot.Main.main is called
        PackManager.v().getPack("jtp").add(new Transform("jtp.dfa", analysisTransformer));

        // Call Soot's main method with arguments
        soot.Main.main(sootArgs);
    }
}
