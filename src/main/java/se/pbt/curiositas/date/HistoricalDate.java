package se.pbt.curiositas.date;

import jakarta.persistence.Embeddable;

/**
 * A date in history that is only as precise as its source. Month and day are optional, so a
 * person known to be born "in 1718" is never given an invented month or day, and an uncertainty
 * in years expresses approximate dates such as "1680 ± 5".
 *
 * <p>Years are astronomical: year 0 is 1 BC and year -43 is 44 BC. Dates are stored as the source
 * gives them, without converting between the Julian and Gregorian calendars.
 *
 * @param year             the astronomical year
 * @param month            the month (1–12), or {@code null} if unknown
 * @param day              the day of the month, or {@code null} if unknown; requires a month
 * @param uncertaintyYears how many years the date may be off in either direction; 0 when certain
 */
@Embeddable
public record HistoricalDate(int year, Integer month, Integer day, int uncertaintyYears) {

    /** English month abbreviations, fixed here so the display text never depends on the server locale. */
    private static final String[] MONTH_NAMES = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    /**
     * Rejects dates that cannot exist, so that invalid values never reach the database or the
     * calculations built on top of them.
     *
     * @throws IllegalArgumentException if the month, day or uncertainty is out of range, or if a
     *                                  day is given without a month
     */
    public HistoricalDate {
        if (uncertaintyYears < 0) {
            throw new IllegalArgumentException(
                    "Uncertainty must be zero or more years, but was " + uncertaintyYears);
        }
        if (month != null && (month < 1 || month > 12)) {
            throw new IllegalArgumentException("Month must be between 1 and 12, but was " + month);
        }
        if (day != null) {
            if (month == null) {
                throw new IllegalArgumentException("A day requires a month");
            }
            int daysInMonth = daysInMonth(year, month);
            if (day < 1 || day > daysInMonth) {
                throw new IllegalArgumentException("Day must be between 1 and " + daysInMonth
                        + " for month " + month + " of year " + year + ", but was " + day);
            }
        }
    }

    /**
     * Returns the date as a human-readable text, for example "22 Nov 1718", "1680 ± 5" or
     * "44 BC". It is derived on every call and never stored, so it always matches the data.
     *
     * @return the date with only the parts that are known, followed by the uncertainty if any
     */
    public String displayText() {
        StringBuilder text = new StringBuilder();
        if (day != null) {
            text.append(day).append(' ');
        }
        if (month != null) {
            text.append(MONTH_NAMES[month - 1]).append(' ');
        }
        text.append(year > 0 ? String.valueOf(year) : (1 - year) + " BC");
        if (uncertaintyYears > 0) {
            text.append(" ± ").append(uncertaintyYears);
        }
        return text.toString();
    }

    /**
     * Returns the number of days a month can have. February gets 29 days in every year divisible
     * by four, which accepts leap days that are valid in either the Julian or the Gregorian
     * calendar, since sources before 1582 usually use the Julian one.
     */
    private static int daysInMonth(int year, int month) {
        return switch (month) {
            case 2 -> Math.floorMod(year, 4) == 0 ? 29 : 28;
            case 4, 6, 9, 11 -> 30;
            default -> 31;
        };
    }
}
