package net.hypotenubel.calendariq.calendar;

/**
 * Represents a calendar, together with its upcoming appointment.
 */
public final class CalendarDescriptor implements Comparable<CalendarDescriptor> {

    /** Indicates that the calendar has no upcoming appointment. */
    public static final long NO_UPCOMING_APPOINTMENT = -1;

    /** The calendar's ID. */
    private final int id;
    /** The calendar's name. */
    private final String calName;
    /** Name of the account the calendar belongs to. */
    private final String accName;
    /** The calendar's color. */
    private final int colour;


    /**
     * Creates a new calendar with the given properties.
     *
     * @param id
     *         the calendar's identifier.
     * @param calName
     *         the calendar's name.
     * @param accName
     *         name of the account the calendar belongs to.
     * @param colour
     *         the calendar's colour.
     */
    public CalendarDescriptor(final int id, final String calName, final String accName,
                              final int colour) {
        this.id = id;
        this.calName = calName;
        this.accName = accName;
        this.colour = colour;
    }


    /**
     * Returns the calendar's ID.
     *
     * @return the ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the calendar's name.
     *
     * @return the name.
     */
    public String getCalName() {
        return calName;
    }

    /**
     * Returns the name of the account the calendar belongs to.
     *
     * @return the name.
     */
    public String getAccName() {
        return accName;
    }

    /**
     * Returns the calendar's colour.
     *
     * @return the colour.
     */
    public int getColour() {
        return colour;
    }

    @Override
    public String toString() {
        return getAccName() + "/" + getCalName() + "/" + getId();
    }

    @Override
    public int compareTo(CalendarDescriptor other) {
        // We sort by account name, then calendar name, then ID (as a fallback)
        if (accName.equals(other.accName)) {
            if (calName.equals(other.calName)) {
                return Integer.compare(id, other.id);
            } else {
                return calName.compareTo(other.calName);
            }
        } else {
            return accName.compareTo(other.accName);
        }
    }
}
