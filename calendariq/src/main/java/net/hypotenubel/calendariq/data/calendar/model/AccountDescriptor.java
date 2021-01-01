package net.hypotenubel.calendariq.data.calendar.model;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a calendar account, identified solely by its name. Accounts can be sorted by name.
 */
public final class AccountDescriptor implements Comparable<AccountDescriptor> {

    /** The account's name. */
    private final String name;


    /**
     * Creates a new account with the given name.
     *
     * @param name non-{@code null} account name.
     */
    public AccountDescriptor(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        this.name = name;
    }


    /**
     * Returns the account's name.
     */
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountDescriptor that = (AccountDescriptor) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(AccountDescriptor other) {
        return name.compareTo(other.name);
    }

}
