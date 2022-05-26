class Slice {
    public static void slicingCriteria(TA9 x) {
        ;
    }
}

public class TA9 {
    TA9 f;

    public static void main(String args[]) {
        TA9 obj1 = new TA9();
        TA9 obj2 = new TA9();

        TA9 x = new TA9();
        obj1.f = x;
        x.f = obj2;
        obj2.f = obj1;

        Slice.slicingCriteria(x);
    }

    private void foo(TA9 p) {
        p.f = p;
    }
}