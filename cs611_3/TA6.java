class Slice {
    public static void slicingCriteria(int x) {
        ;
    }
}

public class TA6 {
    int f;

    public static void main(String args[]) {
        int x = 0, y = 10;
        for(int i = 0; i < y; i++) {
            x += i;
        }
        
        Slice.slicingCriteria(x);
    }

    private int foo(TA6 p) {
        p.f = 4;
        return p.f;
    }
}