package net.hypotenubel.calendariq.sync.synchroniser;

import android.content.Context;

import net.hypotenubel.calendariq.data.msg.model.ConnectMessage;
import net.hypotenubel.calendariq.data.stats.model.BroadcastStatistics;
import net.hypotenubel.calendariq.sync.connectiq.IBroadcasterEventListener;

import java.util.Random;

import javax.inject.Inject;

/**
 * Mocks a broadcast with a random result.
 */
public class RandomBroadcastStrategy implements IBroadcastStrategy {

    @Inject
    public RandomBroadcastStrategy() {
    }

    @Override
    public void broadcast(ConnectMessage msg, Context appContext, IBroadcasterEventListener listener) {
        Random rand = new Random();
        if (rand.nextBoolean()) {
            listener.broadcastFinished(BroadcastStatistics.success(
                    1 + rand.nextInt(9)));
        } else {
            int totalApps = 1 + rand.nextInt(9);
            listener.broadcastFinished(BroadcastStatistics.failure(
                    totalApps,
                    1 + rand.nextInt(totalApps),
                    "Oh noes: something went terribly wrong at random!"));
        }
    }

}
