# Provide the directory path of the testcases

javac -cp soot-cs611.jar:. PA2.java

for i in {1..2} ; do 
    TESTCASE_MARKS=0
    cp TA$i.java tests/
    cd tests
    javac -g:vars TA$i.java
    cd ..       
    echo "[EVALSCRIPT] Testcase TA$i:"
    # Check whether testcase is passed
    timeout 2 java -cp soot-cs611.jar:. PA2 TA$i > out$i
    if [ $? -ne 0 ]; then
        echo "[EVALSCRIPT] Error while running the testcase TA$i"
        continue
    fi
    sed -i '1d' out$i
    sed -i '$d' out$i
    sed -i '$d' out$i
    # Compare outputs
    sed -i 's/[ \t]*$//' out$i
    diff out$i answers/TA$i
    if [ $? -ne 0 ]; then
        echo "[EVALSCRIPT] Testcase output does not match"
    else
        echo "[EVALSCRIPT] Testcase passed"
        TESTCASE_MARKS=$(echo "$TESTCASE_MARKS + 1.0" | bc)
    fi

    echo "[EVALSCRIPT] Marks for Testcase $TESTCASE: $TESTCASE_MARKS"
    OBTAINED_MARKS=$(echo "$OBTAINED_MARKS + $TESTCASE_MARKS" | bc)
    # rm tests/*
done
