package szxcvbn;

import java.util.List;

/**
 * Simple helper to provide Java-friendly function call
 */
public final class ZxcvbnHelper {

    public static Zxcvbn zxcvbn(String password) {
        return Zxcvbn$.MODULE$.apply(password);
    }

    public static Zxcvbn zxcvbn(String password, List<String> userData) {
        return Zxcvbn$.MODULE$.apply(password, scala.collection.JavaConversions.asScalaBuffer(userData));
    }
}
