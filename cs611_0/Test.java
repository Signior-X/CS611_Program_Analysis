public class Test {
    public static void main(String[] args) {
        int x, y = 0, i = 0, j = 5;
        while (i < 10) {
            x = i + 1;
            y = x + y;
            i = x * 2;
        }
        System.out.println(y);
        System.out.println(y);
    }
}
