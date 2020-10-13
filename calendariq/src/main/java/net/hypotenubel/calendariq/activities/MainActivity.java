package net.hypotenubel.calendariq.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.util.Utilities;

public class MainActivity extends AppCompatActivity {

    /** Log tag used to log log messages in a logging fashion. */
    private static final String LOG_TAG = Utilities.logTag(MainActivity.class);


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the view and our action bar
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.mainActivity_appBar));
    }

}
