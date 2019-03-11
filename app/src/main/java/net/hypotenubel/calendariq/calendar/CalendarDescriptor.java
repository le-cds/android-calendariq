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
    /** Start of the next appointment which is not all-day as UTC timestamp in seconds. */
    private final long upcomingAppointment;


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
                              final int colour, final long upcomingAppointment) {
        this.id = id;
        this.calName = calName;
        this.accName = accName;
        this.colour = colour;
        this.upcomingAppointment = upcomingAppointment;
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

    /**
     * Returns the upcoming appointment. Call {@link #hasUpcomingAppointment()} to check whether
     * there is an upcoming appointment. The upcoming appointment is stored as a UTC timestamp in
     * seconds.
     *
     * @return timestamp of the upcoming appointment or {@link #NO_UPCOMING_APPOINTMENT} if there
     * is no upcoming appointment.
     */
    public long getUpcomingAppointment() {
        return upcomingAppointment;
    }

    /**
     * Checks whether the calendar has an upcoming appointment.
     *
     * @return {@code true} if the calendar has an upcoming appointment.
     */
    public boolean hasUpcomingAppointment() {
        return upcomingAppointment != NO_UPCOMING_APPOINTMENT;
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
