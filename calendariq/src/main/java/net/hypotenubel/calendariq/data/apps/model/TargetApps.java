package net.hypotenubel.calendariq.data.apps.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TargetApps {

    private final List<String> targetAppIds;

    public TargetApps(List<String> appIds) {
        if (appIds == null) {
            this.targetAppIds = Collections.emptyList();
        } else {
            List<String> modifiableTargetAppIds = new ArrayList<>(appIds);
            this.targetAppIds = Collections.unmodifiableList(modifiableTargetAppIds);
        }
    }

    /**
     * Returns an unmodifiable list of target app IDs to send data to.
     */
    public List<String> getTargetAppIds() {
        return targetAppIds;
    }
}
