package net.hypotenubel.calendariq.data.calendar.model;

import java.util.Objects;

/**
 * Represents a calendar. A calendar can be active or not. This influences whether appointments from
 * this calendar are taken into account when attempting to find upcoming appointments.
 */
public final class CalendarDescriptor implements Comparable<CalendarDescriptor> {

    /** The calendar's ID. */
    private final int id;
    /** The calendar's name. */
    private final String name;
    /** The account the calendar belongs to. */
    private final AccountDescriptor account;
    /** The calendar's color. */
    private final int colour;

    /** Whether the calendar is currently active or not. */
    private boolean active = false;


    /**
     * Creates a new calendar with the given properties.
     *
     * @param id
     *         the calendar's identifier.
     * @param name
     *         the calendar's name.
     * @param account
     *         the account the calendar belongs to.
     * @param colour
     *         the calendar's colour.
     */
    public CalendarDescriptor(final int id, final String name, final AccountDescriptor account,
                              final int colour) {
        this.id = id;
        this.name = name;
        this.account = account;
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
    public String getName() {
        return name;
    }

    /**
     * Returns the account the calendar belongs to.
     *
     * @return the account.
     */
    public AccountDescriptor getAccount() {
        return account;
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
     * Checks whether this calendar is active.
     *
     * @return {@code true} or {@code false} as the calendar is or isn't active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Determines whether this calendar is active or not.
     *
     * @param active {@code true} if the calendar should be active.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return account + "/" + getName() + "/" + getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()){
            return false;
        } else {
            CalendarDescriptor that = (CalendarDescriptor) o;
            return id == that.id;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(CalendarDescriptor other) {
        // We sort by account name, then calendar name, then ID (as a fallback)
        int accountComparison = account.compareTo(other.account);

        if (accountComparison == 0) {
            if (name.equals(other.name)) {
                return Integer.compare(id, other.id);
            } else {
                return name.compareTo(other.name);
            }
        } else {
            return accountComparison;
        }
    }

}
