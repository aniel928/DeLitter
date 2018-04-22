package com.example.anne.delitter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.TooltipCompat;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageButton addNew;
    private RadioButton recycle;
    private RadioButton trash;
    private int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final DatabaseReference[] trashRef = new DatabaseReference[1];
        final DatabaseReference[] recycleRef = new DatabaseReference[1];
        final ValueEventListener[] trashListener = new ValueEventListener[1];
        final ValueEventListener[] recycleListener = new ValueEventListener[1];

        addNew = findViewById(R.id.addNew);
        recycle = findViewById(R.id.recycle);
        trash = findViewById(R.id.trash);

        TooltipCompat.setTooltipText(addNew, "Add new bin to current location");

        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MapsActivity.this, "Add new", Toast.LENGTH_SHORT).show();

                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();

                showRadioButtonDialog(latitude, longitude);
            }
        });

        recycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(trashListener[0] != null) {
                    trashRef[0].removeEventListener(trashListener[0]);
                }
               // Toast.makeText(MapsActivity.this, "View recycling", Toast.LENGTH_SHORT).show();
                mMap.clear();
                //get stuff from database
                recycleRef[0] = ref.child("recycle");
                recycleListener[0] = recycleRef[0].addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            Bin bin = postSnapshot.getValue(Bin.class);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(bin.latitude, bin.longitude));
                            markerOptions.title(bin.text);
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            mMap.addMarker(markerOptions);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MapsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT);
                    }
                });
            }
        });

        trash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recycleListener[0] != null) {
                    recycleRef[0].removeEventListener(recycleListener[0]);
                }
                //Toast.makeText(MapsActivity.this, "View trash", Toast.LENGTH_SHORT).show();
                mMap.clear();
                //get stuff from database
                trashRef[0] = ref.child("trash");
                trashListener[0] = trashRef[0].addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            Bin bin = postSnapshot.getValue(Bin.class);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(bin.latitude, bin.longitude));
                            markerOptions.title(bin.text);
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            mMap.addMarker(markerOptions);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MapsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT);
                    }
                });

            }
        });



    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                showRadioButtonDialog(latLng.latitude, latLng.longitude);
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            mMap.setMyLocationEnabled(true);

        } else {
            mMap.setMyLocationEnabled(true);
        }
        mMap.getUiSettings().setZoomControlsEnabled(true);
        
        zoomIn();

        trash.callOnClick();
    }

    private void zoomIn() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null)
        {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    //.bearing(0)                // Sets the orientation of the camera to east
                    //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }


    private void showRadioButtonDialog(final double latitude, final double longitude) {

        final String[] pick = new String[2];
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.radiobutton_dialog);

        RadioGroup rg = dialog.findViewById(R.id.radio_group);
        Button finish = dialog.findViewById(R.id.finish);
        final EditText tv = dialog.findViewById(R.id.plain_text_input);

        RadioButton rb=new RadioButton(this); // dynamically creating RadioButton and adding to RadioGroup.
        rb.setText("New Recycle Bin");
        rg.addView(rb);
        RadioButton rb2=new RadioButton(this); // dynamically creating RadioButton and adding to RadioGroup.
        rb2.setText("New Trash Can");
        rg.addView(rb2);



        dialog.show();

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++) {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId) {
                        pick[0] = btn.getText().toString();
                    }
                }
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pick[1] = tv.getText().toString();

                //  Toast.makeText(MapsActivity.this, latitude + " " + longitude, Toast.LENGTH_SHORT).show();
                //save pick[0] to DB
                //Toast.makeText(MapsActivity.this, "Save it", Toast.LENGTH_SHORT).show();
                if(pick[0].equals("New Trash Can")) {
                    DatabaseReference trashRef = ref.child("trash");
                    trashRef.push().setValue(new Bin(latitude, longitude, tv.getText().toString()));
                }else{
                    DatabaseReference recycleRef = ref.child("recycle");
                    recycleRef.push().setValue(new Bin(latitude, longitude, tv.getText().toString()));
                }
                dialog.dismiss();
            }
        });


    }
}
