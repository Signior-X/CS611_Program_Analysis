import soot.PackManager;
import soot.Transform;

public class PA3 {
    public static void main(String[] args) {
        String classPath = ".";
            String fileName = "A";
        if (args.length > 0) {
            fileName = args[0];
        }

        String[] sootArgs = {
            "-cp", classPath, "-pp",
            "-w",
            "-f", "n",
            "-keep-line-number",
            "-no-bodies-for-excluded",
            // "-p", "jb", "use-original-names",
            "-main-class", fileName, fileName
        };

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.ada", new Slicing()));
        soot.Main.main(sootArgs);
    }

}
