public class TA1 {
    public static void main(String[] args) {
        A a = new A();
        a.foo();
    }
}
class A {
    int x;

    void foo() {
        A b = new A();
        b.foo2();
    }

    void foo2() {
        this.x = 5;        
    }
}