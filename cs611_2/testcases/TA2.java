public class TA2 {
    public static void main(String[] args) {
        A a = new A();
        a.foo();
    }
}
class A {
    int x;
    A f1;

    void foo() {
        A b = new A();
        b.foo2(b.f1);
    }

    void foo2(A p1) {
        this.x = 5;
        this.f1 = p1;
    }
}