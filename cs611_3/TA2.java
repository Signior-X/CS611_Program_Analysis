class Slice {
    public static void slicingCriteria(int x) {
        ;
    }
}

public class TA2 {
    int f;

    public static void main(String args[]) {
        int sum = 0;
        int i = 0;

        int a = 1, b = 2, c = 3, d = 4;
        TA2 obj1 = new TA2();
        TA2 obj2 = new TA2();

        TA2 x = new TA2();

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

        Slice.slicingCriteria(x.f);
        System.out.println(obj1.f);
    }

    private int foo(TA2 p) {
        p.f = 4;
        return p.f;
    }
}