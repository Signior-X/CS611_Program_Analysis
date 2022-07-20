# CS611_Program_Analysis
Program analysis to make the programs more quick and efficient by analysing and optimising the programs.

## Static Program Analysis
Most of these assignment focus on static program analysis using `soot` and `Java`. These assignemnts run various program analysis on the Java programs.

### Assignment 1 - Sign Analysis
Static Sign Analysis, identifying wheather the variables will be negative, zero, or positive. The precision decrease as we move up the lattice. We go statement by statement and read each unit. Then try to find which will be the sign of the variable.

Results = The lattice is in such a way, BOTTOM is the most precise with nothing can be said and TOP implysing either -, 0 or +.

