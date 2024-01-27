package ch.epfl.javions;

/**
 * Checks a precondition
 *
 * @author Ünlüer Asli (329696)
 * @author Berlin Nicolas (355535)
 */

public final class Preconditions {

    private Preconditions() {
    }

    /**
     * Checks a precondition and throws an exception if the argument is false.
     *
     * @param shouldBeTrue condition
     * @throws IllegalArgumentException if the condition is false
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}
