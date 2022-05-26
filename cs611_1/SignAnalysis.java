import java.util.*;

import soot.DoubleType;
import soot.IntType;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.jimple.AddExpr;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.DivExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.MulExpr;
import soot.jimple.NumericConstant;
import soot.jimple.SubExpr;
import soot.jimple.UnopExpr;
import soot.jimple.internal.AbstractNegExpr;

class SignAnalysis extends ForwardFlowAnalysis<Unit, HashMap<Local, Integer>> {
    private HashMap<Local, Integer> emptyMap;

    /*
     * 0 -> BOT
     * 1 -> 0
     * 2 -> -
     * 3 -> +
     * 4 -> TOP
     */

    public SignAnalysis(DirectedGraph g) {
        super(g);
        emptyMap = new HashMap<Local, Integer>();
        doAnalysis();
    }

    /**
     * Used to initialize the in and out sets for each node
     * In our case we build up the sets as we go, so we initialize
     * with the empty set.
     */
    @Override
    protected HashMap<Local, Integer> newInitialFlow() {
        return (HashMap<Local, Integer>) emptyMap.clone();
    }

    /**
     * Returns FlowSet representing the initial set of the entry node
     * In our case the entry node is the last node and it
     * should contain the empty set.
     */
    @Override
    protected HashMap<Local, Integer> entryInitialFlow() {
        return (HashMap<Local, Integer>) emptyMap.clone();
    }

    /**
     * Standard copy routine
     */
    @Override
    protected void copy(HashMap<Local, Integer> src, HashMap<Local, Integer> dst) {
        dst.putAll(src);
    }

    /**
     * Perform a join operation over successor nodes (backward analysis)
     * As live variables is a may analysis we join by union
     */
    @Override
    protected void merge(HashMap<Local, Integer> in1, HashMap<Local, Integer> in2, HashMap<Local, Integer> out) {
        // in1.union(in2, out);

        // System.out.println("MERGE");
        // System.out.println("IN1: " + in1.toString());
        // System.out.println("IN2: " + in2.toString());
        // System.out.println("OUT: " + out.toString());

        // First add everything in the first operand
        out.putAll(in1);
        // Then add everything in the second operand, bottoming out the common keys with
        // different values
        for (Local x : in2.keySet()) {
            if (in1.containsKey(x)) {
                // Check the values in both operands
                int sign1 = in1.get(x);
                int sign2 = in2.get(x);
                out.put(x, meetSignTable(sign1, sign2));
            } else {
                // Only in second operand, so add as-is
                out.put(x, in2.get(x));
            }
        }
    }

    /**
     * Set the out (entry) based on the in (exit)
     */
    @Override
    protected void flowThrough(HashMap<Local, Integer> in, Unit node, HashMap<Local, Integer> out) {

        // System.out.println("PRIYAM IN\t" + in.toString());
        // System.out.println("PRIYAM STMT\t" + node.toString());
        out.putAll(in);

        if (node instanceof AssignStmt) {
            Value lhs = ((AssignStmt) node).getLeftOp();
            Value rhs = ((AssignStmt) node).getRightOp();

            // System.out.println("GET MY SIGN" + rhs.toString());
            if (lhs instanceof Local) {
                if (lhs.getType() instanceof IntType || lhs.getType() instanceof DoubleType) {
                    // We will only evaluate the sign for numeric expressions
                    // System.out.println("Now we will evaluate the sign of numeric expression: " +
                    // rhs.toString());

                    if (rhs instanceof CastExpr) {
                        rhs = ((CastExpr) rhs).getOp();
                    }

                    // System.out.println("ASSIGNMENT STMT");
                    out.put((Local) lhs, getMySign(rhs, out));
                } else {
                    // System.out.println("STring most probably, give BOTTOM");
                    out.put((Local) lhs, 0);
                }
            }
        } else if (node instanceof IdentityStmt) {
            // Do For the function parameters case
            Value lhs = ((IdentityStmt) node).getLeftOp();
            Value rhs = ((IdentityStmt) node).getRightOp();

            // System.out.println("GET MY SIGN" + rhs.toString());

            if (lhs instanceof Local) {
                if (lhs.getType() instanceof IntType || lhs.getType() instanceof DoubleType) {
                    // We will only evaluate the sign for numeric expressions
                    // System.out.println("Now we will evaluate the sign of numeric expression: "
                    // + rhs.toString());

                    if (rhs.getType() instanceof IntType || rhs.getType() instanceof DoubleType) {
                        out.put((Local) lhs, 4);
                    } else {
                        out.put((Local) lhs, 0);
                    }
                } else {
                    // System.out.println("this: Test like statements, give BOTTOM");
                    out.put((Local) lhs, 0);
                }
            } else {
                // Do nothing in that case, join will be done automatically
            }
        }

        // System.out.println("PRIYAM OUT\t" + out.toString());
    }

    public int getMySign(Value rhs, HashMap<Local, Integer> out) {
        // get sign of the expression

        if (rhs instanceof Local) {
            // variable
            Local local = (Local) rhs;
            if (out.containsKey(local)) {
                return out.get(local);
            } else {
                // If variable not defined till now, return bottom
                return 0; // bottom
            }

        } else if (rhs instanceof NumericConstant) {
            NumericConstant num = (NumericConstant) rhs;

            if (rhs instanceof DoubleConstant) {
                // System.out.println("Double CONSTANT: " + rhs.toString());

                // check if greater than zero or less than zero
                NumericConstant zero = DoubleConstant.v(0);
                NumericConstant one = DoubleConstant.v(1);
                if (num.lessThan(zero).equals(one)) {
                    return 2; // negative
                } else if (num.greaterThan(zero).equals(one)) {
                    return 3; // plus
                } else {
                    return 1; // zero
                }
            } else {
                // System.out.println("NUMERIC CONSTANT: " + rhs.toString());

                // check if greater than zero or less than zero
                NumericConstant zero = IntConstant.v(0);
                NumericConstant one = IntConstant.v(1);
                if (num.lessThan(zero).equals(one)) {
                    return 2; // negative
                } else if (num.greaterThan(zero).equals(one)) {
                    return 3; // plus
                } else {
                    return 1; // zero
                }
            }
        } else if (rhs instanceof BinopExpr) {
            return getSignOfBinaryExp(rhs, out);

        } else if (rhs instanceof UnopExpr) {
            // unary operation like +x, -x

            if (rhs instanceof AbstractNegExpr) {
                // Handle unary minus
                Value op = ((AbstractNegExpr) rhs).getOp();
                int sign = getMySign(op, out);

                if (sign == 3) {
                    return 2;
                } else if (sign == 2) {
                    return 3;
                } else if (sign == 4) {
                    return 4;
                } else if (sign == 1) {
                    return 1;
                } else {
                    return 0;
                }

            } else {
                // We do not handle other types of binary expressions
                return 0;
            }
        }

        return 0; // Bottom
    }

    public int getSignOfBinaryExp(Value rhs, HashMap<Local, Integer> out) {
        // Jimple so all the expressions in binary
        BinopExpr exp = (BinopExpr) rhs;

        // Get both the operands
        Value opr1 = (Value) exp.getOp1();
        Value opr2 = (Value) exp.getOp2();

        // Now get the signs of both the operands
        int sgn1 = getMySign(opr1, out);
        int sgn2 = getMySign(opr2, out);

        // Now let's meetup and have the combined sign from the tables
        if (rhs instanceof AddExpr) {
            return addSignTable(sgn1, sgn2);
        } else if (rhs instanceof SubExpr) {
            return SubSignTable(sgn1, sgn2);
        } else if (rhs instanceof MulExpr) {
            return MulSignTable(sgn1, sgn2);
        } else if (rhs instanceof DivExpr) {
            return DivSignTable(sgn1, sgn2);
        }

        return 0;
    }

    public int addSignTable(int a, int b) {
        // Edge case so as to not have any error
        if (a >= 5 || b >= 5 || a < 0 || b < 0)
            return 0;

        int mapping[][] = {
                { 0, 0, 0, 0, 0 },
                { 0, 1, 2, 3, 4 },
                { 0, 2, 2, 4, 4 },
                { 0, 3, 4, 3, 4 },
                { 0, 4, 4, 4, 4 }
        };

        return mapping[a][b];
    }

    public int SubSignTable(int a, int b) {
        // a - b

        /*
         * 0 -> BOT
         * 1 -> 0
         * 2 -> -
         * 3 -> +
         * 4 -> TOP
         */

        // Edge case so as to not have any error
        if (a >= 5 || b >= 5 || a < 0 || b < 0)
            return 0;

        int mapping[][] = {
                { 0, 0, 0, 0, 0 },
                { 0, 1, 3, 2, 4 },
                { 0, 2, 2, 2, 4 },
                { 0, 3, 4, 4, 4 },
                { 0, 4, 4, 4, 4 }
        };

        return mapping[a][b];
    }

    public int MulSignTable(int a, int b) {
        if (a >= 5 || b >= 5 || a < 0 || b < 0)
            return 0;

        int mapping[][] = {
                { 0, 0, 0, 0, 0 },
                { 0, 1, 1, 1, 1 },
                { 0, 1, 3, 2, 4 },
                { 0, 1, 2, 3, 4 },
                { 0, 1, 4, 4, 4 }
        };

        return mapping[a][b];
    }

    public int DivSignTable(int a, int b) {
        // a / b

        if (a >= 5 || b >= 5 || a < 0 || b < 0)
            return 0;

        int mapping[][] = {
                { 0, 0, 0, 0, 0 },
                { 0, 1, 1, 1, 1 },
                { 0, 1, 3, 2, 4 },
                { 0, 1, 2, 3, 4 },
                { 0, 1, 4, 4, 4 }
        };

        return mapping[a][b];
    }

    public int meetSignTable(int a, int b) {
        if (a >= 5 || b >= 5 || a < 0 || b < 0)
            return 0;

        int mapping[][] = {
                { 0, 0, 0, 0, 0 },
                { 0, 1, 4, 4, 4 },
                { 0, 4, 2, 4, 4 },
                { 0, 4, 4, 3, 4 },
                { 0, 4, 4, 4, 4 }
        };

        return mapping[a][b];
    }
}
