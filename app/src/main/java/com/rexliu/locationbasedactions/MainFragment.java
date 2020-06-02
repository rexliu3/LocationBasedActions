package com.rexliu.locationbasedactions;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainFragment extends Fragment {
    private ImageView mainImage;

    private boolean tracking;
    private String address;
    private int radius;
    private int duration;

    private GeofencingClient geofencingClient;
    private List geofenceList = new ArrayList<Geofence>();
    private PendingIntent geofencePendingIntent;

    private EditText addressInput;
    private EditText radiusInput;
    private EditText durationInput;

    private String TRACKING_KEY = "TRACKING";
    private String ADDRESS_KEY = "ADDRESS";
    private String RADIUS_KEY = "RADIUS";
    private String DURATION_KEY = "DURATION";
    private int disableDrawable = R.drawable.ic_location_disabled_black_24dp;
    private int enabledDrawable = R.drawable.ic_location_searching_black_24dp;


    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert savedInstanceState != null;
        geofencingClient = LocationServices.getGeofencingClient(getContext());
        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mainpage, container, false);

        mainImage = v.findViewById(R.id.mainImageView);
        addressInput = v.findViewById(R.id.addressInput);
        radiusInput = v.findViewById(R.id.radiusInput);
        durationInput = v.findViewById(R.id.durationInput);

        mainImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tracking) {
                    if (!addressInput.getText().toString().equals("")) {
                        address = addressInput.getText().toString();
                        if (!radiusInput.getText().toString().equals("") && Integer.parseInt(radiusInput.getText().toString()) >= 100) {
                            radius = Integer.parseInt(radiusInput.getText().toString());
                            if (!durationInput.getText().toString().equals("") && Integer.parseInt(durationInput.getText().toString()) > 0) {
                                duration = Integer.parseInt(durationInput.getText().toString());
                                tracking = true;
                                mainImage.setImageResource(enabledDrawable);
                                addressInput.setEnabled(false);
                                radiusInput.setEnabled(false);
                                durationInput.setEnabled(false);
                                setUpGeofence(address, radius, duration);
                            } else {
                                displayToast("Please Enter a Duration.");
                            }
                        } else {
                            displayToast("Please Enter a Radius above 100 meters.");
                        }
                    } else {
                        displayToast("Please Enter an Address.");
                    }
                } else {
                    tracking = false;
                    stopGeofences();
                    mainImage.setImageResource(disableDrawable);
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
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(TRACKING_KEY, tracking);
        editor.putString(ADDRESS_KEY, address);
        editor.putInt(RADIUS_KEY, radius);
        editor.putInt(DURATION_KEY, duration);
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sharedPreferences.contains(TRACKING_KEY)) {
            tracking = sharedPreferences.getBoolean(TRACKING_KEY, false);
        } else {
            tracking = false;
        }

        if (sharedPreferences.contains(ADDRESS_KEY)) {
            address = sharedPreferences.getString(ADDRESS_KEY, "");
        } else {
            address = "";
        }

        if (sharedPreferences.contains(RADIUS_KEY)) {
            radius = sharedPreferences.getInt(RADIUS_KEY, 0);
        } else {
            radius = 0;
        }

        if (sharedPreferences.contains(DURATION_KEY)) {
            duration = sharedPreferences.getInt(DURATION_KEY, 0);
        } else {
            duration = 0;
        }

        if (tracking) {
            mainImage.setImageResource(enabledDrawable);
            addressInput.setEnabled(false);
            radiusInput.setEnabled(false);
            durationInput.setEnabled(false);
            addressInput.setText(address);
            radiusInput.setText(Integer.toString(radius));
            durationInput.setText(Integer.toString(duration));
        }
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
            displayToast("Please Input a Valid Address.");
        }

        long dur = duration * 60 * 60;

        geofenceList.add(new Geofence.Builder()
                .setRequestId("Main")
                .setCircularRegion(
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
                displayToast("Geofences Successfully Added. Currently Tracking.");
            }
        }).addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                displayToast("Failed to Add Geofences. Please Try Again.");
            }
        });
    }

    private void stopGeofences() {
        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        displayToast("Geofences Successfully Removed. Tracking has Stopped.");
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        displayToast("Failed to Remove Geofences. Please Try Again.");
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
