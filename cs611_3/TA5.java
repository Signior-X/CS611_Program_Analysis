class Slice {
    public static void slicingCriteria(TA5 x) {
        ;
    }
}

public class TA5 {
    int f;
    TA5 fo;

    public static void main(String args[]) {
        int a = 1, b = 2;
        TA5 obj1 = new TA5();
        TA5 obj2 = new TA5();

        TA5 x = new TA5();
        obj2 = x;


        x.fo = x;

        if (a > 4) {
            if (b < 3) {
                x = new TA5();
                x.fo = obj1;
            } else {
                x = obj2;
                obj2.f = 10;
            }
        } else {
            x.f = 12;
            x.f = obj2.foo(obj2);

            obj1.fo = x;
        }

        Slice.slicingCriteria(x);
    }

    private int foo(TA5 p) {
        p.f = 4;
        return p.f;
    }
}