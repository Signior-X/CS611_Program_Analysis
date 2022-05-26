class Slice {
    public static void slicingCriteria(int x) {
        ;
    }
}

public class TA8 {
    int f;

    public static void main(String args[]) {
        int x = 5;

        int a = 0, b = 10;
        for(int i = 0; i < 10; i++) {
            a = a + b;
            b = b + 2;
        }

        int c = foo();
        while(c > 0) {
            c = c - 5;
        }

        if (x > b && (a < b)) {
            System.out.println(x + b);
        }

        System.out.println(x);
        Slice.slicingCriteria(x);
    }

    private static int foo() {
        return 5;
    }
}