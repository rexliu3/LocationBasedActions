package android.example.locationbasedactions;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {
    String TRACKING_KEY = "TRACKING";
    String ADDRESS_KEY = "ADDRESS";
    String RADIUS_KEY = "RADIUS";
    String DURATION_KEY = "DURATION";
    private boolean tracking = false;
    private String address;
    private int radius;
    private int duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(R.string.titleBar);

        Fragment nextFrag = new MainFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.replace(R.id.container, nextFrag, nextFrag.getTag()).commit();
        fragmentTransaction.replace(R.id.container, nextFrag).commit();
    }

    @Override
    public void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        if (savedInstanceState != null && savedInstanceState.containsKey(TRACKING_KEY)) {
            tracking = savedInstanceState.getBoolean(TRACKING_KEY, false);
        } else {
            tracking = false;
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(ADDRESS_KEY)) {
            address = savedInstanceState.getString(ADDRESS_KEY, "");
        } else {
            address = "";
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(RADIUS_KEY)) {
            radius = savedInstanceState.getInt(RADIUS_KEY, 0);
        } else {
            radius = 0;
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(DURATION_KEY)) {
            duration = savedInstanceState.getInt(DURATION_KEY, 0);
        } else {
            duration = 0;
        }
    }

    public boolean getTracking() {
        return tracking;
    }

    public String getAddress() {
        return address;
    }

    public int getRadius() {
        return radius;
    }

    public int getDuration() {
        return duration;
    }

}
