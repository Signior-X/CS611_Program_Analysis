public class TA3 {
    public static void main(String[] args) {
        A a = new A();
        a.foo(a);
    }
}
class A {
    int x;
    A f1;

    void foo(A b) {
        b.foo2(b.f1);
    }

    void foo2(A p1) {
        this.x = 5;
        this.f1 = p1;
    }
}