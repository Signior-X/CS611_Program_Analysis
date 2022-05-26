class Slice {
    public static void slicingCriteria(A x) {
        ;
    }
}

public class A {
    int f;

    public static void main(String args[]) {
        int sum = 0;
        int i = 0;

        int a = 1, b = 2, c = 3, d = 4;
        A obj1 = new A();
        A obj2 = new A();

        A x = new A();

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
        x.f = obj2.foo(obj1);

        Slice.slicingCriteria(x);
    }

    private int foo(A p) {
        p.f = 4;
        return p.f;
    }
}