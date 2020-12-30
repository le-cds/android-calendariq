package net.hypotenubel.calendariq.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import net.hypotenubel.calendariq.R;
import net.hypotenubel.calendariq.util.Utilities;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = Utilities.logTag(MainActivity.class);

    /** Used for having the Navigation framework manage the action bar. */
    private AppBarConfiguration appBarConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the view and our action bar
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.mainActivity_appBar));

        // Make the Navigation framework manage the action bar
        NavController navController = getNavHostFragment().getNavController();
        appBarConfig = new AppBarConfiguration.Builder(R.id.calendarListFragment).build();
        Toolbar toolbar = findViewById(R.id.mainActivity_appBar);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = getNavHostFragment();
        NavController navController = navHostFragment.getNavController();
        return NavigationUI.navigateUp(navController, appBarConfig)
                || super.onSupportNavigateUp();
    }

    private NavHostFragment getNavHostFragment() {
        return (NavHostFragment) getSupportFragmentManager().findFragmentById(
                R.id.mainActivity_fragmentContainer);
    }


}
