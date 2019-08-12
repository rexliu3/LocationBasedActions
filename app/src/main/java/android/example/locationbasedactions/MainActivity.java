package android.example.locationbasedactions;

import android.example.locationbasedactions.Fragments.ActionListFragment;
import android.example.locationbasedactions.Fragments.NewActionFragment;
import android.example.locationbasedactions.Fragments.SettingsFragment;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int id = item.getItemId();
            Fragment nextFrag;

            if (id == R.id.navigation_actionList) {
                nextFrag = new ActionListFragment();
                getSupportActionBar().setTitle(R.string.title_currentActionsList);
            } else if (id == R.id.navigation_newAction) {
                nextFrag = new NewActionFragment();
                getSupportActionBar().setTitle(R.string.title_newAction);
            } else if (id == R.id.navigation_settings) {
                nextFrag = new SettingsFragment();
                getSupportActionBar().setTitle(R.string.title_settings);
            } else {
                nextFrag = new ActionListFragment();
                getSupportActionBar().setTitle(R.string.title_currentActionsList);
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, nextFrag, nextFrag.getTag())
                    .commit();

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
