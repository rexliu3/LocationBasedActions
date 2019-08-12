package android.example.locationbasedactions.Fragments;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.example.locationbasedactions.GeofenceBroadcastReceiver;
import android.example.locationbasedactions.R;
import android.example.locationbasedactions.Utils;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;


public class NewActionFragment extends Fragment {
    private TextInputEditText addressInputText;
    private ImageView homeImageView;
    private Button addActionButton;

    private GeofencingClient geofencingClient;
    private Geofence mainGeofence;
    private PendingIntent geofencePendingIntent;

    private double LatitudeNum = 0.00;
    private double LongitudeNum = 0.00;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_newaction, container, false);

        addActionButton = (Button) v.findViewById(R.id.addActionButton);

        homeImageView = (ImageView) v.findViewById(R.id.homeImageView);
        homeImageView.setVisibility(View.INVISIBLE);

        addActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressInputText = (TextInputEditText) v.findViewById(R.id.inputAddressText);
                String address = addressInputText.getText().toString();

                if (LatitudeNum != 0 && LatitudeNum != 0) {
                    setLatLongValues(address);
                    setUpGeoFence();

                } else {
                    Utils.sendErrorMessageBox(getContext());
                }
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void setLatLongValues(String locationAddress) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address>
                    addressList = geocoder.getFromLocationName(locationAddress, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);

                LatitudeNum = address.getLatitude();
                LongitudeNum = address.getLongitude();

            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();

        }
    }

    private void setUpGeoFence() {

        geofencingClient = LocationServices.getGeofencingClient(getContext());
        String requestID = getString(R.string.GeofenceRequestID);
//        Nums Below for My House
//        double latitudeNum = 49.035858;
//        double longitudeNum = -122.810361;
        float geoFenceRadius = 100;
        long GEOFENCE_EXPIRATION_IN_MILLISECONDS = 1000000000;

        //Make this a GeoFence List to have More
        mainGeofence = new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(requestID)

                .setCircularRegion(
                        LatitudeNum,
                        LongitudeNum,
                        geoFenceRadius
                )
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .build();

        addGeoFences();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofence(mainGeofence);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(getContext(), GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    private void addGeoFences() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};

            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, 1);
            return;
        }


        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener((Executor) this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        // ...
                    }
                })
                .addOnFailureListener((Executor) this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Utils.sendErrorMessageBox(getContext());
                    }
                });
    }

    private void removeGeoFences() {
        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener((Executor) this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        // ...
                    }
                })
                .addOnFailureListener((Executor) this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        // ...
                    }
                });
    }


}
