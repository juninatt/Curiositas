package se.pbt.curiositas.date;

/**
 * The shortest and longest age a person can have reached, in completed years. Age is a range
 * because birth and death are often only partly known; it is always calculated and never stored.
 *
 * @param min the lowest possible age, never below 0
 * @param max the highest possible age
 */
public record AgeRange(int min, int max) {

    /**
     * Rejects ranges that cannot describe a real age.
     *
     * @throws IllegalArgumentException if {@code min} is negative or greater than {@code max}
     */
    public AgeRange {
        if (min < 0 || min > max) {
            throw new IllegalArgumentException(
                    "Age range must satisfy 0 <= min <= max, but was " + min + "–" + max);
        }
    }

    /**
     * Calculates the possible ages at death. The youngest age assumes the latest possible birth
     * and the earliest possible death, the oldest age the opposite, so the range covers every
     * reading of the dates. A minimum below zero, possible when uncertain dates overlap, becomes 0.
     *
     * @param birth when the person was born
     * @param death when the person died
     * @return the range of possible ages in completed years
     * @throws IllegalArgumentException if death can only have happened before birth, which is
     *                                  impossible in every reading of the dates
     */
    public static AgeRange between(HistoricalDate birth, HistoricalDate death) {
        int max = completedYears(Point.earliest(birth), Point.latest(death));
        if (max < 0) {
            throw new IllegalArgumentException(
                    "Death (" + death.displayText() + ") cannot be before birth (" + birth.displayText() + ")");
        }
        int min = completedYears(Point.latest(birth), Point.earliest(death));
        return new AgeRange(Math.max(0, min), max);
    }

    /** Counts the birthdays that have passed between two points in time. */
    private static int completedYears(Point from, Point to) {
        boolean beforeBirthday = to.month < from.month || (to.month == from.month && to.day < from.day);
        return to.year - from.year - (beforeBirthday ? 1 : 0);
    }

    /**
     * One end of the time span a historical date covers. Unknown months and days are filled with
     * the first or last value they can have; day 31 stands for "end of month", which is enough
     * since points are only compared, never shown.
     */
    private record Point(int year, int month, int day) {

        /** Returns the earliest moment the date can refer to. */
        static Point earliest(HistoricalDate date) {
            return new Point(
                    date.year() - date.uncertaintyYears(),
                    date.month() != null ? date.month() : 1,
                    date.day() != null ? date.day() : 1);
        }

        /** Returns the latest moment the date can refer to. */
        static Point latest(HistoricalDate date) {
            return new Point(
                    date.year() + date.uncertaintyYears(),
                    date.month() != null ? date.month() : 12,
                    date.day() != null ? date.day() : 31);
        }
    }
}
