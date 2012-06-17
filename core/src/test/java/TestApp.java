/**
 *
 */
public class TestApp {

    public static void main(String[] args) {
        long end = System.currentTimeMillis() + 5*60*1000;

        while (System.currentTimeMillis() < end) {
            szxcvbn.Zxcvbn$.MODULE$.apply("a");
            szxcvbn.Zxcvbn$.MODULE$.apply("aaaaaa");
            szxcvbn.Zxcvbn$.MODULE$.apply("password");
            szxcvbn.Zxcvbn$.MODULE$.apply("coRrecth0rseba++ery9.23.2007staple$");
        }
    }
}
