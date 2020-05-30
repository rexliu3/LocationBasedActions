package android.example.locationbasedactions;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import javax.xml.datatype.Duration;

public class MainFragment extends Fragment {
    private ImageView mainImage;
    private boolean tracking;

    private GeofencingClient geofencingClient;

    private String address;
    private int radius;
    private int duration;
    private List geofenceList = new ArrayList<Geofence>();
    private PendingIntent geofencePendingIntent;

    EditText addressInput;
    EditText radiusInput;
    EditText durationInput;

    String TRACKING_KEY = "TRACKING";
    String ADDRESS_KEY = "ADDRESS";
    String RADIUS_KEY = "RADIUS";
    String DURATION_KEY = "DURATION";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert savedInstanceState != null;
        geofencingClient = LocationServices.getGeofencingClient(getContext());
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mainpage, container, false);

        mainImage = (ImageView) v.findViewById(R.id.mainImageView);
        addressInput = (EditText) v.findViewById(R.id.addressInput);
        radiusInput = (EditText) v.findViewById(R.id.radiusInput);
        durationInput = (EditText) v.findViewById(R.id.durationInput);

        MainActivity activity = (MainActivity) getActivity();
        tracking = activity.getTracking();
        address = activity.getAddress();
        radius = activity.getRadius();
        duration = activity.getDuration();

        if (tracking) {
            mainImage.setImageResource(R.color.colorPrimary);
            addressInput.setEnabled(false);
            radiusInput.setEnabled(false);
            durationInput.setEnabled(false);
            addressInput.setText(address);
            radiusInput.setText(Integer.toString(radius));
            durationInput.setText(Integer.toString(duration));
        }

        mainImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tracking) {
                    if (!addressInput.getText().toString().equals("")) {
                        address = addressInput.getText().toString();
                        if (!radiusInput.getText().toString().equals("") && Integer.parseInt(radiusInput.getText().toString()) > 0) {
                            radius = Integer.parseInt(radiusInput.getText().toString());
                            if (!durationInput.getText().toString().equals("") && Integer.parseInt(durationInput.getText().toString()) > 0) {
                                duration = Integer.parseInt(durationInput.getText().toString());
                                tracking = true;
                                mainImage.setImageResource(R.color.colorPrimary);
                                addressInput.setEnabled(false);
                                radiusInput.setEnabled(false);
                                durationInput.setEnabled(false);
                                setUpGeofence(address, radius, duration);
                            } else {
                                displayToast("Please Enter a Duration");
                            }
                        } else {
                            displayToast("Please Enter a Radius");
                        }
                    } else {
                        displayToast("Please Enter an Address");
                    }
                } else {
                    tracking = false;
                    stopGeofences();
                    mainImage.setImageResource(R.drawable.common_google_signin_btn_icon_dark);
                    addressInput.setEnabled(true);
                    radiusInput.setEnabled(true);
                    durationInput.setEnabled(true);
                }
            }
        });
        return v;
    }

    private void displayToast(String inline) {
        Toast.makeText(getContext(), inline, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(TRACKING_KEY, tracking);
        savedInstanceState.putString(ADDRESS_KEY, address);
        savedInstanceState.putInt(RADIUS_KEY, radius);
        savedInstanceState.putInt(DURATION_KEY, duration);
    }

    private void setUpGeofence(String address, int radius, int duration) {
        // Set the request ID of the geofence. This is a string to identify this
        // geofence.
        double latitude = 0;
        double longitude = 0;

        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List addressList = geocoder.getFromLocationName(address, 1);
            Address address1 = (Address) addressList.get(0);
            latitude = address1.getLatitude();
            longitude = address1.getLongitude();
        } catch (Exception e) {
           displayToast("Input Valid Address");
        }

        long dur = duration * 60 * 60;

        geofenceList.add(new Geofence.Builder().setRequestId("Main").setCircularRegion(
                            latitude,
                            longitude,
                            radius
                    )
                    .setExpirationDuration(dur)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setLoiteringDelay(3)
                    .build());

        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent()).addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Geofences Added
            }
        }).addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to add geofences
            }
        });
    }

    private void stopGeofences() {
        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        // ...
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        // ...
                    }
                });
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(getContext(), GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }
}
