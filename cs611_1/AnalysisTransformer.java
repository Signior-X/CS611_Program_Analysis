import java.util.*;
import soot.*;
import soot.toolkits.graph.*;

// Need to add a concept of undefined TOP for undefined variables

public class AnalysisTransformer extends BodyTransformer {
    @Override
    protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
        // Construct CFG for the current method's body
        UnitGraph graph = new CompleteUnitGraph(body);

        HashMap<Local, Integer> initlocal = new HashMap<Local, Integer>();
        for (Local local : body.getLocals()) {
            if (local.getType() instanceof IntType || local.getType() instanceof DoubleType) {
                initlocal.put(local, 4);
            } else {
                initlocal.put(local, 0);
            }
        }

        // Perform live variable analysis over the CFG
        SignAnalysis analysis = new SignAnalysis(graph);

        System.out.println(body.getMethod().getDeclaringClass() + "-" + body.getMethod().getName() + ":");

        // Print live variables at the in and out of each node
        Iterator<Unit> unitIt = graph.iterator();
        while (unitIt.hasNext()) {
            Unit u = unitIt.next();

            System.out.print("<" + u.toString() + ">: {");
            String tempout = "";

            Map<Local, Integer> set = analysis.getFlowAfter(u);

            Map<String, Integer> tmp = new TreeMap<String, Integer>();
            for (Map.Entry<Local, Integer> entry : initlocal.entrySet()) {
                if (set.containsKey(entry.getKey())) {
                    tmp.put(entry.getKey().toString(), set.get(entry.getKey()));
                } else {
                    tmp.put(entry.getKey().toString(), entry.getValue());
                }
            }

            for (Map.Entry<String, Integer> entry : tmp.entrySet()) {
                tempout += (entry.getKey() + ": " + signMapping(entry.getValue()) + ", ");
            }

            if (tempout.length() > 0) {
                tempout = tempout.substring(0, tempout.length() - 2);
            }

            System.out.print(tempout + "}");
            System.out.println();
        }
    }

    public String signMapping(int val) {
        /*
         * 0 -> BOT
         * 1 -> 0
         * 2 -> -
         * 3 -> +
         * 4 -> TOP
         */

        switch (val) {
            case 0:
                return "BOT";
            case 1:
                return "ZERO";
            case 2:
                return "MINUS";
            case 3:
                return "PLUS";
            case 4:
                return "TOP";
            default:
                return "ERROR";
        }
    }
}
