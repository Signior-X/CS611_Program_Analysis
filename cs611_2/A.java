public class A {
    A f1;
    int f2;

    public static void main(String[] args) {
        A a = new A();
        A b = new A();
        a.foo(b);
    }

    void foo(A p1) {
        A b;
        p1.f2 = 10;
        if (p1.f2 == 5) {
            p1.f2 = 10;
        } else {
            bar(p1);
        }
    }
    void bar(A p2) {
        p2.f1 = new A();
        p2.f1.f2= 20;
    }
}