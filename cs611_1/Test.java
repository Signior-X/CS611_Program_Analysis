public class Test {
    public static void main(String[] args) {
        int x;
        int y = 10;
        int i = 20;
        while (i < 10) {
            x = i + 1;
            y = x + y + (2 * y);
            i = x * 2;
        }
        System.out.println(y);
    }
}