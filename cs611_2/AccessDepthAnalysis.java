import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import soot.Body;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.jimple.IdentityStmt;
import soot.jimple.LengthExpr;
import soot.jimple.NewExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JimpleLocalBox;

// Will there be recursive loops?

public class AccessDepthAnalysis extends SceneTransformer {
    static CallGraph cg;

    // computed methods -> answer map of that method
    static TreeMap<String, TreeMap<String, Integer>> finalAns;
    static TreeSet<String> methodProcessing;
    static TreeMap<String, ArrayList<Integer>> methodParametersDepths;
    static TreeMap<String, Integer> methodReturnDepth;

    @Override
    protected void internalTransform(String arg0, Map<String, String> arg1) {
        // For storing the final answer
        finalAns = new TreeMap<String, TreeMap<String, Integer>>();
        methodProcessing = new TreeSet<String>();
        methodParametersDepths = new TreeMap<String, ArrayList<Integer>>();
        methodReturnDepth = new TreeMap<String, Integer>();

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

        // STEP 2: Change the below code to process methods in bottom-up order over the
        // call graph
        // Collections.reverse(arrSootMethod);
        // for(SootMethod method : arrSootMethod) {
        // processMethod(method);
        // }

        // processMethod(someMethod); // You can create methods like this to perform the
        // intraprocedural analysis

        // Printing the final answer after the analysis
        for (String key : finalAns.keySet()) {
            System.out.println(key);

            TreeMap<String, Integer> mp = finalAns.get(key);
            for (String key2 : mp.keySet()) {
                System.out.println(key2 + " " + mp.get(key2));
            }
        }
    }

    protected static void processMethod(SootMethod method) {
        // Ignore Java library methods
        if (method.isJavaLibraryMethod()) {
            return;
        }

        String methodName = method.getDeclaringClass().getName() + ":" + method.getName();
        methodProcessing.add(methodName);
        // System.out.println("PRIYAM DOING:\t " + methodName);

        ArrayList<String> methodParametersOrderWise = new ArrayList<String>();

        Body body = method.getActiveBody();
        // Get the control-flow graph (CFG)
        UnitGraph cfg = new BriefUnitGraph(body);
        PatchingChain<Unit> units = body.getUnits();

        // Storing the call graphs as the final result
        TreeMap<String, Integer> ans = new TreeMap<String, Integer>();

        // Stores the element name and to which node in points to graph it points to
        TreeMap<String, ArrayList<String>> pointingTo = new TreeMap<String, ArrayList<String>>();

        // My datastructure for making the points to graph
        TreeMap<String, GraphNode> myPTG = new TreeMap<String, GraphNode>();

        // store the variables that are atleast used once
        TreeSet<String> usedParams = new TreeSet<String>();

        // If the function is returning something
        ArrayList<String> returnVars = new ArrayList<String>();

        for (Unit u : units) {
            // You will have to consider assignment statements involving pointers and
            // dereferences
            // System.out.println(u.toString());

            if (!(u instanceof IdentityStmt)) {
                for (ValueBox vlBox : u.getUseAndDefBoxes()) {
                    usedParams.add(vlBox.getValue().toString());
                }
            }

            if (((soot.jimple.Stmt) u).containsInvokeExpr()) {
                // System.out.println("INVOKE STMT: \t" + u.toString());
                // This is how you get the methods called at a call site based on the call-graph

                if (((soot.jimple.Stmt) u).getInvokeExpr() instanceof VirtualInvokeExpr
                        || ((soot.jimple.Stmt) u).getInvokeExpr() instanceof StaticInvokeExpr) {
                    // System.out.println("Virtual invoke it is");

                    // System.out.println("SEE: \t " + ((soot.jimple.Stmt)
                    // u).getInvokeExpr().getArgs().toString());
                    // System.out.println(((soot.jimple.Stmt) u).getInvokeExpr());

                    Iterator<Edge> it = cg.edgesOutOf(u);
                    while (it.hasNext()) {
                        SootMethod target = (SootMethod) it.next().getTgt();
                        // Complete the interprocedural mapping here

                        if (target.getDeclaringClass().toString().equals("java.io.PrintStream")) {
                            continue;
                        }

                        // System.out.println("NEED to PROCESS: \t " + u.toString());
                        // System.out.println();

                        String callingFunctionName = target.getDeclaringClass() + ":" + target.getName();
                        if (!(finalAns.containsKey(callingFunctionName))) {
                            processMethod(target);
                        }

                        List<Value> functionArgs = ((soot.jimple.Stmt) u).getInvokeExpr().getArgs();

                        // TODO: Add for "this" also, the access depth - DONE
                        // TODO find the base object also here - DONE not tested

                        List<ValueBox> functionArgsBoxes = ((soot.jimple.Stmt) u).getInvokeExpr().getUseBoxes();
                        String functionCallingBase = "";

                        if (((soot.jimple.Stmt) u).getInvokeExpr() instanceof StaticInvokeExpr) {
                            // What will be the function calling base for this??

                            // Also add the value for this?

                            for (int i = 0; i < functionArgs.size(); i++) {
                                for (String nodeName : pointingTo.get(functionArgs.get(i).toString())) {

                                    if (!(nodeName.equals("LEAF"))) {
                                        myPTG.get(nodeName)
                                                .setDepth(methodParametersDepths.get(callingFunctionName).get(i));
                                    }
                                    // System.out.println("NodeName\t: " + nodeName);
                                    // System.out.println("PRIYAM 2\t: " + myPTG.toString());
                                    // myPTG.get(nodeName).depth = 10;
                                }
                            }

                        } else {
                            for (ValueBox vlBox : functionArgsBoxes) {
                                if (vlBox instanceof JimpleLocalBox) {
                                    functionCallingBase = vlBox.getValue().toString();
                                }
                            }

                            for (int i = 0; i < functionArgs.size() + 1; i++) {
                                String varName = functionCallingBase;
                                if (i > 0) {
                                    varName = functionArgs.get(i - 1).toString();
                                }

                                // System.out.println("PRIYAM STMT:\t" + u.toString());
                                // System.out.println("PRIYAM varName: " + varName);

                                ArrayList<String> nodeNameArray = pointingTo.get(varName);

                                if (nodeNameArray != null) {
                                    // If not a variable
                                    for (String nodeName : nodeNameArray) {

                                        // Since it is calling by this, it should have atleast 2
                                        if (varName == "this") {
                                            myPTG.get(nodeName).setDepth(2);
                                        }

                                        if (!(nodeName.equals("LEAF"))) {
                                            myPTG.get(nodeName)
                                                    .setDepth(methodParametersDepths.get(callingFunctionName).get(i));
                                        }
                                        // System.out.println("NodeName\t: " + nodeName);
                                        // System.out.println("PRIYAM 2\t: " + myPTG.toString());
                                        // myPTG.get(nodeName).depth = 10;
                                    }
                                }
                            }
                        }

                        String lhsVariable = "";
                        List<ValueBox> lhsFunctionCall = ((soot.jimple.Stmt) u).getDefBoxes();
                        if (lhsFunctionCall.size() > 0) {
                            // we have a return statement for the function
                            lhsVariable = lhsFunctionCall.get(0).getValue().toString();

                            // Now create a new GraphNode with this virtualInvoke value
                            SootMethod callingMethod = ((soot.jimple.Stmt) u).getInvokeExpr().getMethod();
                            String objName = callingMethod.getDeclaringClass() + "_fun_obj_" + callingMethod.getName();
                            GraphNode newGraphNode = new GraphNode(callingMethod.getReturnType(), objName);
                            myPTG.put(objName, newGraphNode);
                            addToPointingTo(pointingTo, lhsVariable, objName);

                            int callingFunctionDepth = methodReturnDepth.get(callingFunctionName);
                            newGraphNode.setDepth(callingFunctionDepth);
                        }

                        // First one is this
                        // System.out.println("PRIYAM SEE methodsDepth \t" +
                        // methodParametersDepths.toString());

                    }
                } else {
                    // SKipping special invoke
                }

            } else

            if (u instanceof IdentityStmt) {
                // Function parameters
                Value lhs = ((IdentityStmt) u).getLeftOp();
                // System.out.println("Identity STMT: \t " + lhs + " " + lhs.getType());
                ans.put(lhs.toString(), 0);
                methodParametersOrderWise.add(lhs.toString());

                String nodeName = method.getName() + "_$_" + lhs.toString();
                GraphNode n1 = new GraphNode(lhs.getType(), nodeName);
                myPTG.put(nodeName, n1);
                addToPointingTo(pointingTo, lhs.toString(), nodeName);
            } else

            if (u instanceof JAssignStmt) {
                // Write code here to find which kind of statement it is, its LHS and RHS, and
                // update required information accordingly

                // System.out.println(u.toString());

                // Create new nodes for new statements
                Value lhs = ((JAssignStmt) u).getLeftOp();
                Value rhs = ((JAssignStmt) u).getRightOp();

                if (rhs instanceof NewExpr) {
                    // New statements, create a new node here
                    // System.out.println("NEW EXPR: \t " + u.toString());
                    String nodeName = lhs.toString();
                    GraphNode n1 = new GraphNode(rhs.getType(), nodeName);
                    myPTG.put(nodeName, n1);
                    // pointing to nothing, just creating an object

                    addToPointingTo(pointingTo, nodeName, nodeName);

                    // usedParams.add(lhs.toString());
                } else

                if (lhs instanceof Local && rhs instanceof Local) {
                    // System.out.println("BOTH Local pointing: \t " + u.toString());
                    String nodeName = rhs.toString();

                    if (pointingTo.containsKey(nodeName)) {
                        // This should instead point to all the objects pointed to by this nodeName

                        ArrayList<String> pointingToArray = pointingTo.get(nodeName);
                        for (String nodeName2 : pointingToArray) {
                            addToPointingTo(pointingTo, lhs.toString(), nodeName2);
                        }

                    } else {
                        addToPointingTo(pointingTo, lhs.toString(), nodeName);
                    }

                    // usedParams.add(lhs.toString());
                    // usedParams.add(rhs.toString());
                } else

                if (rhs instanceof JInstanceFieldRef) {
                    String rhsBase = ((JInstanceFieldRef) rhs).getBase().toString();
                    String rhsField = ((JInstanceFieldRef) rhs).getField().getName();

                    // usedParams.add(rhsBase);

                    if (lhs instanceof Local) {
                        // System.out.println("RHS instance of JInstanceFieldRef: \t " + u.toString());

                        ArrayList<String> objectPointingToArray = pointingTo.get(rhsBase);

                        for (String objectPointingTo : objectPointingToArray) {
                            if (objectPointingTo != null) {
                                GraphNode node = myPTG.get(objectPointingTo);

                                ArrayList<String> rightObjArray = node.getKey(rhsField);

                                // UNTEST CODE START
                                if (rightObjArray == null) {
                                    // Create a new node in myPTG
                                    String newNodeName = rhsBase + "_._" + rhsField;
                                    myPTG.put(newNodeName, new GraphNode(rhs.getType(), u.toString()));
                                    node.addKey(rhsField, newNodeName);
                                }

                                rightObjArray = node.getKey(rhsField);
                                // UNTESTED CODE END

                                if (rightObjArray != null) {
                                    for (String rightObj : rightObjArray) {
                                        addToPointingTo(pointingTo, lhs.toString(), rightObj);
                                    }
                                }
                            }
                        }

                        // usedParams.add(lhs.toString());
                    }

                } else

                if (lhs instanceof JInstanceFieldRef) {
                    String lhsBase = ((JInstanceFieldRef) lhs).getBase().toString();
                    String lhsField = ((JInstanceFieldRef) lhs).getField().getName();
                    ArrayList<String> objectPointingToArray = pointingTo.get(lhsBase);
                    // System.out.println(lhsBase + " SEE " + lhsField + " THERE " +
                    // objectPointingToArray.toString());

                    // usedParams.add(lhs.toString());

                    for (String objectPointingTo : objectPointingToArray) {
                        if (objectPointingTo != null) {
                            if (rhs instanceof Local) {
                                ArrayList<String> rightObjNodeArray = pointingTo.get(rhs.toString());

                                // System.out.println("PRIYAM STM:\t" + u.toString());
                                // System.out.println("PRIYAM SEE:\t" + rhs.toString() + " \t " +
                                // rightObjNodeArray);
                                // System.out.println("PRIYAM OYE:\t" + pointingTo.toString());
                                // System.out.println("PRIYAM WHA:\t" + myPTG.toString());
                                // System.out.println("PRIYAM OBJ:\t" + objectPointingTo);

                                for (String rightObjNode : rightObjNodeArray) {
                                    // System.out.println("PRIYAM SEE:\t" + rightObjNode);

                                    GraphNode node = myPTG.get(objectPointingTo);
                                    node.addKey(lhsField, rightObjNode);
                                }

                                // usedParams.add(rhs.toString());
                            } else {
                                GraphNode node = myPTG.get(objectPointingTo);
                                node.addKey(lhsField, "LEAF");
                            }
                        }
                    }
                } else {
                    if (((JAssignStmt) u).getRightOp() instanceof LengthExpr) {
                        // System.out.println("YEA:\t" + u.toString());

                        // Taken from above code piece
                        String rhsBase = ((JAssignStmt) u).getRightOp().getUseBoxes().get(0).getValue().toString();
                        String rhsField = "length";

                        // System.out.println("PRIYAM rhsBase:\t" + rhsBase);

                        // usedParams.add(rhsBase);

                        if (lhs instanceof Local) {
                            // System.out.println("RHS instance of JInstanceFieldRef: \t " + u.toString());

                            ArrayList<String> objectPointingToArray = pointingTo.get(rhsBase);

                            for (String objectPointingTo : objectPointingToArray) {
                                if (objectPointingTo != null) {
                                    GraphNode node = myPTG.get(objectPointingTo);

                                    ArrayList<String> rightObjArray = node.getKey(rhsField);

                                    if (rightObjArray == null) {
                                        // Create a new node in myPTG
                                        String newNodeName = rhsBase + "_._" + rhsField;
                                        myPTG.put(newNodeName, new GraphNode(rhs.getType(), u.toString()));
                                        node.addKey(rhsField, newNodeName);
                                    }

                                    rightObjArray = node.getKey(rhsField);
                                    if (rightObjArray != null) {
                                        for (String rightObj : rightObjArray) {
                                            // System.out.println("PRIYAM WHAT:\t" + rightObj);
                                            addToPointingTo(pointingTo, lhs.toString(), rightObj);
                                        }
                                    }
                                }
                            }

                            // usedParams.add(lhs.toString());
                        }

                        // System.out.println("PRIYAM MYPTG:\t" + myPTG.toString());
                        // System.out.println("PRIYAM POINTINGTO:\t" + pointingTo.toString());

                    }
                }

            } else {
                // System.out.println("STMT type not found: \t" + u.toString());
                // TODO: what about return statements?

                if (u instanceof ReturnStmt) {
                    String returnVar = u.getUseBoxes().get(0).getValue().toString();
                    returnVars.add(returnVar);
                }
            }
        }

        // // Let's see which parameter is pointing to which
        // System.out.println("VARIABLES POINTING");
        // for (String key : pointingTo.keySet()) {
        // System.out.println(key + "\t\t -> " + pointingTo.get(key).toString());

        // // System.out.println("Object pointing to is: " +
        // // myPTG.get(pointingTo.get(key)).toString());
        // }

        // System.out.println("POINTS TO GRAPH");
        // for (String key : myPTG.keySet()) {
        // System.out.println(key + " = " + myPTG.get(key));
        // }

        // Now we need to do a dfs from the node to get the answer, the maximum
        // depth that can be reached
        // for a particular node

        HashSet<String> visitedNodes = new HashSet<String>();

        int returnAns = 0;
        if (returnVars.size() > 0) {
            // System.out.println(methodName + " : " + returnVars.toString());

            for (String returnVar : returnVars) {
                // returnVar can point to multiple objects
                ArrayList<String> pointingToNodeArray = pointingTo.get(returnVar);
                for (String pointingToNode : pointingToNodeArray) {
                    GraphNode node = myPTG.get(pointingToNode);
                    returnAns = Math.max(returnAns, dfs(node, myPTG, visitedNodes));
                }
            }
        }

        // System.out.println(methodName + " returnAns: " + returnAns);
        methodReturnDepth.put(methodName, returnAns);

        for (String key : ans.keySet()) {
            // Get the initial depth
            int depthAns = Math.max(0, ans.get(key));

            // Do a dfs for the given key
            ArrayList<String> pointingToNodeArray = pointingTo.get(key);

            for (String pointingToNode : pointingToNodeArray) {
                GraphNode node = myPTG.get(pointingToNode);
                depthAns = Math.max(depthAns, dfs(node, myPTG, visitedNodes));
            }

            // System.out.println("Key: " + key + " depth: " + depthAns);
            ans.put(key, depthAns);
        }

        for (String key : ans.keySet()) {
            if (!(usedParams.contains(key))) {
                ans.put(key, 0); // make depths to zero for variables that are not used
            }
        }

        // ANSWERS Calculated for this method

        // Add the answers for the method parameters above
        ArrayList<Integer> depthsOrderWise = new ArrayList<Integer>();
        for (String paramName : methodParametersOrderWise) {
            depthsOrderWise.add(ans.get(paramName));
        }

        finalAns.put(methodName, ans);

        // Add the depths in arraylist of the methodName
        methodParametersDepths.put(methodName, depthsOrderWise);
    }

    public static int dfs(GraphNode node, TreeMap<String, GraphNode> myPTG, HashSet<String> visitedNodes) {
        // Loop in the map of the node
        // TODO: Improve dfs with a visited array

        if (visitedNodes.contains(node.nodeName)) {
            return 0;
        }

        // Backtracking adding visited array in dfs
        visitedNodes.add(node.nodeName);

        int depthAns = node.depth;

        for (ArrayList<String> fieldValues : node.node.values()) {
            for (String nextNodeName : fieldValues) {

                if (nextNodeName.equals("LEAF")) {
                    depthAns = Math.max(depthAns, 1 + 1);
                } else {
                    depthAns = Math.max(depthAns, 1 + dfs(myPTG.get(nextNodeName), myPTG, visitedNodes));
                }
            }
        }

        // Backtracking
        visitedNodes.remove(node.nodeName);

        node.depth = depthAns;
        return depthAns;
    }

    public static void addToPointingTo(TreeMap<String, ArrayList<String>> pointingTo, String key, String value) {
        if (!(pointingTo.containsKey(key))) {
            pointingTo.put(key, new ArrayList<>());
        }

        pointingTo.get(key).add(value);
    }

}

class GraphNode {
    // TODO: Do we need a leaf node as well or we can use this one?

    public Type type;
    public TreeMap<String, ArrayList<String>> node;
    public int depth;
    public String nodeName;

    GraphNode(Type type, String name) {
        nodeName = name;
        depth = 1;
        this.type = type;
        node = new TreeMap<String, ArrayList<String>>();
    }

    public void setDepth(int depth) {
        this.depth = Math.max(this.depth, depth);
    }

    public void addKey(String key, String value) {
        if (!(node.containsKey(key))) {
            node.put(key, new ArrayList<>());
        }

        node.get(key).add(value);
    }

    public ArrayList<String> getKey(String key) {
        return node.get(key);
    }

    public String toString() {
        return type + ": \t" + node.toString();
    }
}
