package net.hypotenubel.calendariq.ui.pref;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

/**
 * Summary provider that works with a format string the preference value is passed to.
 */
class FormattingSummaryProvider
        implements Preference.SummaryProvider<Preference> {

    /** The format string we'll be using to generate summaries. */
    private final String formatString;

    /**
     * Creates a new summary provider for the given format string.
     *
     * @param formatString the format string to use.
     */
    public FormattingSummaryProvider(String formatString) {
        this.formatString = formatString;
    }

    @Override
    public CharSequence provideSummary(Preference preference) {
        String prefValue = null;

        // Extract proper preference value
        if (preference instanceof ListPreference) {
            CharSequence prefValueSequence = ((ListPreference) preference).getEntry();
            if (prefValueSequence != null) {
                prefValue = prefValueSequence.toString();
            }

        } else if (preference instanceof EditTextPreference) {
            prefValue = ((EditTextPreference) preference).getText();

        }

        // Return proper summary
        if (prefValue == null) {
            return "";
        } else {
            return String.format(formatString, prefValue);
        }
    }
}
