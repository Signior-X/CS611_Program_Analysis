class Slice {
    public static void slicingCriteria(int x) {
        ;
    }
}

public class TA7 {
    int f;

    public static void main(String args[]) {
        int sum = 0;
        int i = 0;

        int a = 1, b = 2, c = 3, d = 4;

        // Alias one example
        TA7 x = new TA7();

        while (i < 11) {
            sum += i;
            i++;

            a = a + i;
            b = b + i;
            c = c + i;
            d = d + sum;
        }

        if (x.f > 10) {
            x.f = b;
        } else {
            b = x.f;
        }

        Slice.slicingCriteria(x.f);
    }

    private int foo(TA7 p) {
        p.f = 4;
        return p.f;
    }
}