class Slice {
    public static void slicingCriteria(int x) {
        ;
    }
}

public class TA3 {
    int f;

    public static void main(String args[]) {
        int sum = 0;
        int i = 0;

        int a = 1, b = 2, c = 3, d = 4;
        TA3 obj1 = new TA3();
        TA3 obj2 = new TA3();

        TA3 x = new TA3();

        while (i < 11) {
            sum += i;
            i++;

            a = a + i;
            b = b + i;
            c = c + i;
            d = d + sum;
        }

        System.out.print(sum);
        System.out.print(i);

        c = d;
        x.f = a + b + d;
        Slice.slicingCriteria(x.f);
    }

    private int foo(TA3 p) {
        p.f = 4;
        return p.f;
    }
}