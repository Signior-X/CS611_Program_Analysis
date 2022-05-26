import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import soot.Body;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JimpleLocalBox;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

// Will there be recursive loops?

public class Slicing extends SceneTransformer {
    static CallGraph cg;
    static UnitGraph cfg;

    static TreeSet<String> alreadyProcessed;
    static HashSet<Unit> finalAns;

    @Override
    protected void internalTransform(String arg0, Map<String, String> arg1) {
        // For storing the final answer

        alreadyProcessed = new TreeSet<String>();
        finalAns = new HashSet<Unit>();

        // STEP 1: Get the call graph
        cg = Scene.v().getCallGraph();

        Iterator<MethodOrMethodContext> callerEdges = cg.sourceMethods();

        // ArrayList<SootMethod> arrSootMethod = new ArrayList<SootMethod>();
        while (callerEdges.hasNext()) {
            SootMethod methodCaller = (SootMethod) callerEdges.next();
            // arrSootMethod.add(methodCaller);
            if (methodCaller.isMain()) {
                // System.out.println(methodCaller);
                processMethod(methodCaller);
            }
        }

    }

    protected static void processMethod(SootMethod method) {
        // Ignore Java library methods

        Body body = method.getActiveBody();
        // Get the control-flow graph (CFG)
        cfg = new BriefUnitGraph(body);
        PatchingChain<Unit> units = body.getUnits();

        Unit slicingCriteria = null;
        List<Value> slicingArgs = new ArrayList<Value>();

        for (Unit u : units) {
            // System.out.println(u.toString());

            if (((soot.jimple.Stmt) u).containsInvokeExpr()) {
                // System.out.println("PRIYAM:\t" + ((soot.jimple.Stmt)
                // u).getInvokeExpr().getMethod().getDeclaringClass().toString().equals("Slice"));
                SootMethod invokeMethod = ((soot.jimple.Stmt) u).getInvokeExpr().getMethod();
                if (invokeMethod.getDeclaringClass().toString().equals("Slice")) {
                    slicingCriteria = u;

                    slicingArgs = ((soot.jimple.Stmt) u).getInvokeExpr().getArgs();
                    // System.out.println("Slicing Args: " + slicingArgs.toString());
                }
            }
        }

        if (slicingCriteria == null) {
            // System.out.println("No slicing criteria provided");
            return;
        }

        // Now start the slicing part
        finalAns.add(slicingCriteria);
        for (Value v : slicingArgs) {
            analyseSlice(slicingCriteria, v);
        }

        // Now after analysing the set, we will make the final output as asked for .dot
        // file
        String finalOutputString = "digraph G {\n";
        for (Unit u : units) {
            if (finalAns.contains(u)) {
                finalOutputString += "\"" + u.toString() + "\" [color=\"red\"];\n";
            }
        }

        // Now add the lines here for the cfg
        // TODO
        // May be we will start from the head and keep adding the lines of codes
        HashSet<Unit> visited = new HashSet<Unit>();
        LinkedList<Unit> queue = new LinkedList<Unit>();

        List<Unit> startUnit = cfg.getHeads();
        // Mark the current node as visited and enqueue it
        for (Unit u : startUnit) {
            visited.add(u);
            queue.add(u);
        }

        while (queue.size() != 0) {
            // Dequeue a vertex from queue and print it
            Unit s = queue.poll();

            // Get all adjacent vertices of the dequeued vertex s
            // If a adjacent has not been visited, then mark it
            // visited and enqueue it
            List<Unit> succs = cfg.getSuccsOf(s);

            for (Unit u : succs) {

                if (u instanceof GotoStmt) {
                    finalOutputString += "\"" + s.toString() + "\" -> \"" + ((GotoStmt) u).getTarget().toString()
                            + "\"\n";
                } else if (s instanceof GotoStmt) {
                } else {
                    finalOutputString += "\"" + s.toString() + "\" -> \"" + u.toString() + "\"\n";
                }

                if (!visited.contains(u)) {
                    visited.add(u);
                    queue.add(u);
                }
            }
        }

        finalOutputString += "}";

        // Here is the final dot file
        // System.out.println(finalOutputString);
        try {
            FileWriter myWriter = new FileWriter("input.dot");
            myWriter.write(finalOutputString);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static boolean isPartOfExpr(Unit u, Value var) {
        boolean toConsider = false;
        List<ValueBox> usedVars = u.getUseAndDefBoxes();
        for (ValueBox vbx : usedVars) {
            if (vbx.getValue() instanceof Local) {
                if (vbx.getValue() == var) {
                    toConsider = true;
                }
            }
        }

        return toConsider;
    }

    private static void handleInvokes23(Unit u, List<Value> nextVars) {
        List<ValueBox> functionArgsBoxes = ((soot.jimple.Stmt) u).getInvokeExpr().getUseBoxes();

        if (((soot.jimple.Stmt) u).getInvokeExpr() instanceof StaticInvokeExpr) {
            // No need to worry, it is this.
        } else {
            for (ValueBox vlBox : functionArgsBoxes) {
                if (vlBox instanceof JimpleLocalBox) {
                    nextVars.add(vlBox.getValue());
                }
            }
        }

        // TODo, add the base also here, not the other parameters however
        // Also add the statements related to this
        List<ValueBox> usedVars = u.getUseBoxes();

        // System.out.println(usedVars.toString());
        for (ValueBox vbx : usedVars) {
            if (vbx.getValue() instanceof Local) {
                // System.out.println("Is it: " + vbx);
                nextVars.add(vbx.getValue());
            }
        }

        System.out.println("ADDED THIS: " + u.toString() + " : " + nextVars.toString());
    }

    private static void addUseBoxes(Unit u, List<Value> nextVars) {
        List<ValueBox> usedVars = u.getUseBoxes();
        // System.out.println(usedVars.toString());
        for (ValueBox vbx : usedVars) {
            if (vbx.getValue() instanceof Local) {
                // System.out.println("Is it: " + vbx);
                nextVars.add(vbx.getValue());
            }
        }

        System.out.println("ADDED USE BOX: " + u.toString() + " : " + nextVars.toString());
    }

    private static void analyseSlice(Unit u, Value var) {
        String key = u.toString() + "__PRIYAM__" + var.toString();

        if (alreadyProcessed.contains(key))
            return;
        alreadyProcessed.add(key);

        // System.out.println(u.toString() + " checking for " + var);
        // Here check for it

        // Next recursion after processing this node properly
        List<Value> nextVars = new ArrayList<Value>();

        if (((soot.jimple.Stmt) u).containsInvokeExpr()) {
            System.out.println("CONTAINS INVOKE EXPR: " + key);

            if (isPartOfExpr(u, var)) {
                if (u instanceof JAssignStmt) {
                    System.out.println("ALSO CONTAINS INVOKE EXPR: " + key);
                    // Also an assignment with invoke expr
                    Value lhs = ((JAssignStmt) u).getLeftOp();

                    if (lhs instanceof Local) {
                        if (lhs == var) {
                            // x = y.foo(p1, p2)
                            finalAns.add(u);
                            addUseBoxes(u, nextVars);
                            handleInvokes23(u, nextVars);
                        } else {
                            // y = x.foo(p1, p2)
                            finalAns.add(u);
                            handleInvokes23(u, nextVars);
                            nextVars.add(var);
                        }
                    } else {
                        // Field Field reference variable cases
                        finalAns.add(u);

                        if (lhs instanceof JInstanceFieldRef) {
                            Value lhsBase = ((JInstanceFieldRef) lhs).getBase();
                            if (lhsBase == var) {
                                // x.f1 = y.foo(p1, p2), Take x and p1 both
                                addUseBoxes(u, nextVars);
                                handleInvokes23(u, nextVars);
                            } else {
                                // y.f1 = x.foo(p1, p2), not add y
                                handleInvokes23(u, nextVars);
                            }
                        } else {
                            // Not a field expression with invoke TODO??
                        }

                        nextVars.add(var);
                    }
                } else {
                    // Not an assignment, but includes it y.foo(x), x slicingCriteria
                    finalAns.add(u);
                    handleInvokes23(u, nextVars);
                    nextVars.add(var);
                }
            } else {
                // skip this statement
                nextVars.add(var);
            }

        } else if (u instanceof JAssignStmt) {
            // Assignment statement not involving invokeExpr
            Value lhs = ((JAssignStmt) u).getLeftOp();
            Value rhs = ((JAssignStmt) u).getRightOp();

            if (lhs instanceof Local) {
                if (lhs == var) {
                    // y = b
                    finalAns.add(u);
                    addUseBoxes(u, nextVars);
                } else {
                    // b = y, a = b
                    nextVars.add(var);
                }
            } else {
                // lhs is not an instance of Local, field reference
                if (isPartOfExpr(u, var)) {
                    // Add condition for field sensitve one

                    if (lhs instanceof JInstanceFieldRef) {
                        // System.out.println("YES" + key);
                        Value lhsBase = ((JInstanceFieldRef) lhs).getBase();
                        if (lhsBase == var) {
                            // x.f1 = p1, Take x and p1 both
                            finalAns.add(u);
                            addUseBoxes(u, nextVars);
                        } else {
                            // a.f1 = x, not inlcude x
                        }
                    } else {
                        // Not a field expression TODO
                    }
                }

                // Adding var as var = something is not here
                nextVars.add(var);
            }

        } else if (u instanceof IfStmt) {
            // System.out.println("if statement: " + key);
            if (isPartOfExpr(u, var)) {
                finalAns.add(u);

                List<ValueBox> usedVars = u.getUseBoxes();
                for (ValueBox vbx : usedVars) {
                    if (vbx.getValue() instanceof Local) {
                        nextVars.add(vbx.getValue());
                    }
                }
            }

            nextVars.add(var);
        } else {
            System.out.println("STMTS Left to Process: " + key);
            if (isPartOfExpr(u, var)) {
                finalAns.add(u);
            }
            nextVars.add(var);
        }

        List<Unit> predsUnits = cfg.getPredsOf(u);
        for (Value v : nextVars) {
            for (Unit pu : predsUnits) {
                analyseSlice(pu, v);
            }
        }
    }
}
