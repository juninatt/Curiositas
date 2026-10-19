package se.pbt.curiositas.json;

import tools.jackson.core.JacksonException;

/**
 * Names the field a JSON error belongs to, in the same form as validation errors, so the API and
 * the data files report problems the same way.
 */
public final class JsonFieldPath {

    /** Not instantiable; all methods are static. */
    private JsonFieldPath() {
    }

    /**
     * Returns the path to the field where reading failed.
     *
     * @param exception the error from reading JSON
     * @return the path, for example "alsoKnownAs[0].type"; empty if the error is not in a field
     */
    public static String of(JacksonException exception) {
        StringBuilder path = new StringBuilder();
        for (JacksonException.Reference reference : exception.getPath()) {
            if (reference.getPropertyName() != null) {
                if (!path.isEmpty()) {
                    path.append('.');
                }
                path.append(reference.getPropertyName());
            } else {
                path.append('[').append(reference.getIndex()).append(']');
            }
        }
        return path.toString();
    }
}
