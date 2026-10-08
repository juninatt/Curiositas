package se.pbt.curiositas.person;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Compares names the way a reader would, so that "Édouard Teach" and "edouard  teach" count as the
 * same name when looking for duplicates.
 */
final class PersonNames {

    private static final Pattern ACCENTS = Pattern.compile("\\p{M}+");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /** Not instantiable; all methods are static. */
    private PersonNames() {
    }

    /**
     * Returns the name in lower case, without accents and with single spaces. Used only for
     * comparison and never stored, since it can always be derived from the name.
     *
     * @param name the name as written
     * @return the normalised name
     */
    static String normalize(String name) {
        String withoutAccents = ACCENTS.matcher(Normalizer.normalize(name, Normalizer.Form.NFD)).replaceAll("");
        return WHITESPACE.matcher(withoutAccents.toLowerCase(Locale.ROOT).strip()).replaceAll(" ");
    }
}
