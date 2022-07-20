# CS611_Program_Analysis
Program analysis to make the programs more quick and efficient by analysing and optimising the programs.

## Static Program Analysis
Most of these assignment focus on static program analysis using `soot` and `Java`. These assignemnts run various program analysis on the Java programs.

### Assignment 1 - Sign Analysis
Static Sign Analysis, identifying wheather the variables will be negative, zero, or positive. The precision decrease as we move up the lattice. We go statement by statement and read each unit. Then try to find which will be the sign of the variable.

Results = The lattice is in such a way, BOTTOM is the most precise with nothing can be said and TOP implysing either -, 0 or +.

### Assignment 2 - InterProcedural Access Depth Analysis
We try to find the depth upto which an object is used or called. This is an inter-procedural static analysis that works by first creating the CFG, control flow graph with considering different function call. It finds the access depth of each parameter of the function.
First we create the control flow graph, then we perform a dfs on that graph, to find the maximum depth for that specific variable.

### Assignment 3 - Staic Backward Slicing
We slice the program depending on the slicing criteria. It reduces the lines of codes and make the program specific for a function. It helps in finding which lines in the code are actually useful and thus helps in specializing the code.
We first find the slicing criteria, then we loop backwards in the control flow graph, considering the cycles and all taking care to take all those variables that make an effect in the slicing criteria.
