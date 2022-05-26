class Slice {
    public static void slicingCriteria(TA4 x) {
        ;
    }
}

public class TA4 {
    int f;

    TA4() {
        this.f = 5;
    }

    TA4(int f) {
        this.f = f;
    }

    public static void main(String args[]) {
        TA4 obj1 = new TA4();
        TA4 obj2 = new TA4(10);

        TA4 x = new TA4();

        int c = 5;
        if (args.length > 0) {
            c = Integer.parseInt(args[0]);
        }

        if (c > 5) {
            x.foo(new TA4(obj2.bar(obj2.baz(obj1))));
        } else {
            x.foo(x);
        }

        Slice.slicingCriteria(x);
    }

    private int foo(TA4 p) {
        p.f = 4;
        return p.f;
    }

    private int bar(int a) {
        return a + a;
    }

    private int baz(TA4 b) {
        return b.f;
    }
}