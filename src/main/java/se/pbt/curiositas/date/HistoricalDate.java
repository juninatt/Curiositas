package se.pbt.curiositas.date;

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
public record HistoricalDate(int year, Integer month, Integer day, int uncertaintyYears) {

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
