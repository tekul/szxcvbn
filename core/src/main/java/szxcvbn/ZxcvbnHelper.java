package szxcvbn;

/**
 * Simple helper to provide Java-friendly function call
 */
public final class ZxcvbnHelper {

    public static Zxcvbn zxcvbn(String password) {
        return Zxcvbn$.MODULE$.apply(password);
    }

}
