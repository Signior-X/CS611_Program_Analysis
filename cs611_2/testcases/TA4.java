public class TA4 {
    public static void main(String[] args) {
        A a = new A();
        a.foo(a, a);
    }
}
class A {
    int x;
    A f1;

    void foo(A b, A c) {
        b.foo2(b.f1);
        this.f1 = c;
    }

    void foo2(A p1) {
        this.x = 5;
        this.f1 = p1;

        this.foo3(p1);
    }

    void foo3(A p1) {
        p1.f1 = new A();
        p1.f1.x = 10;
    }
}