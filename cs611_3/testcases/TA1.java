class Slice {
    public static void slicingCriteria(TA1 x) {
        ;
    }
}

public class TA1 {
    int f;

    public static void main(String args[]) {
        int sum = 0;
        int i = 0;

        int a = 1, b = 2, c = 3, d = 4;
        TA1 obj1 = new TA1();
        TA1 obj2 = new TA1();

        TA1 x = new TA1();

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

        x.f = 12;
        x.f = obj2.foo(obj2);

        obj1.f = x.f;

        Slice.slicingCriteria(x);
        System.out.println(obj1.f);
    }

    private int foo(TA1 p) {
        p.f = 4;
        return p.f;
    }
}