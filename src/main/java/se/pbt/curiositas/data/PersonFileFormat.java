package se.pbt.curiositas.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import se.pbt.curiositas.api.model.AlternativeNameDto;
import se.pbt.curiositas.api.model.HistoricalDateInputDto;
import se.pbt.curiositas.api.model.PersonInputDto;
import se.pbt.curiositas.person.PersonNames;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.regex.Pattern;

/**
 * The format of the data files: one person per file, in the same format a client sends when
 * creating a person, so a file is valid exactly when the API would accept it.
 *
 * <p>The output is fully deterministic, with fixed field order, two-space indentation and LF line
 * endings on every platform, so the same data always gives the same bytes and git diffs show only
 * real changes.
 */
final class PersonFileFormat {

    private static final Pattern NOT_SLUG = Pattern.compile("[^a-z0-9]+");

    private static final DefaultIndenter INDENTER = new DefaultIndenter("  ", "\n");

    private static final JsonMapper JSON = JsonMapper.builder()
            .addMixIn(PersonInputDto.class, PersonOrder.class)
            .addMixIn(HistoricalDateInputDto.class, DateOrder.class)
            .addMixIn(AlternativeNameDto.class, NameOrder.class)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .defaultPrettyPrinter(new DefaultPrettyPrinter(Separators.createDefaultInstance()
                    .withObjectNameValueSpacing(Separators.Spacing.AFTER))
                    .withObjectIndenter(INDENTER)
                    .withArrayIndenter(INDENTER))
            .build();

    /** Not instantiable; all methods are static. */
    private PersonFileFormat() {
    }

    /**
     * Writes a person as the content of its file, ending with a newline.
     *
     * @param person the person in request format
     * @return the file content
     */
    static String toJson(PersonInputDto person) {
        return JSON.writeValueAsString(person) + "\n";
    }

    /**
     * Returns the file name a person gets before any clash with another person is resolved. The
     * name alone is not enough, so the Wikidata id or, failing that, the birth year is added.
     *
     * @param person the person in request format
     * @return the file name without extension, for example "edward-teach-q213518"
     */
    static String baseName(PersonInputDto person) {
        String name = slug(person.getName());
        if (person.getWikidataId() != null) {
            return name + "-" + person.getWikidataId().toLowerCase();
        }
        if (person.getBirth() != null) {
            int year = person.getBirth().getYear();
            return name + "-" + (year > 0 ? String.valueOf(year) : (1 - year) + "bc");
        }
        return name;
    }

    /**
     * Turns a name into lower-case words joined by hyphens, without accents. Names without any
     * Latin letters or digits, such as names in Chinese script, fall back to "person".
     */
    private static String slug(String name) {
        String slug = NOT_SLUG.matcher(PersonNames.normalize(name)).replaceAll("-").replaceAll("^-|-$", "");
        return slug.isEmpty() ? "person" : slug;
    }

    /** Field order of a person in the files: identity first, then life, then the rest. */
    @JsonPropertyOrder({"name", "wikidataId", "alsoKnownAs", "birth", "birthPlace", "birthCountry",
            "death", "deathPlace", "floruit", "gender"})
    private abstract static class PersonOrder {
    }

    /** Field order of a date in the files, from the largest unit to the smallest. */
    @JsonPropertyOrder({"year", "month", "day", "uncertaintyYears"})
    private abstract static class DateOrder {
    }

    /** Field order of an alternative name in the files. */
    @JsonPropertyOrder({"name", "type"})
    private abstract static class NameOrder {
    }
}
