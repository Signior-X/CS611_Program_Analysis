import soot.PackManager;
import soot.Transform;

public class PA2 {
    public static void main(String[] args) {
        String classPath = ".";

        String[] sootArgs = {
            "-cp", classPath, "-pp",
            "-w",
            "-f", "n",
            "-keep-line-number",
            "-no-bodies-for-excluded",
            "-p", "jb", "use-original-names",
            "-main-class", "A", "A"
        };

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.ada", new AccessDepthAnalysis()));
        soot.Main.main(sootArgs);
    }

}
