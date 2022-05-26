public class TA5 {
    public static void main(String[] args) {
        A a = new A(4);
        a.foo(a, a);
    }
}
class A {
    int x;
    A f1, f2;

    A(int x) {
        this.x = x;
    }

    void foo(A b, A c) {
        if (this.x > 4) {
            this.f1 = b;
        } else {
            this.f1 = c;
        }

        this.f2 = null;
    }
}