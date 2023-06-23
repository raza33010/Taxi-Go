package com.example.taxigo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.taxigo.databinding.ActivityDriverMaps2Binding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.sql.Driver;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DriverMapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private ActivityDriverMaps2Binding binding;
    GoogleApiClient googleApiClient;  // access google api
    Location lastLocation;


    String driverID,customerID = "";


    DatabaseReference AssignedCustomerRef,AssignedCustomerPickupRef;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Boolean logout_status = false; // flag which is tells that the driver is offline

    Marker pickupmarker;

    ValueEventListener AssignedCustomerPickupRefListner;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverMaps2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        driverID = mAuth.getCurrentUser().getUid();

        GetAssignedCustomerRequest();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.satellite_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.terrain_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            case R.id.logout:
                logout_status = true;
                DriverDisconnect();
                mAuth.signOut();
                LogoutDriver();

            case R.id.settings:
                // call settings option
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission
                .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }


    private void GetAssignedCustomerRequest() {
        // tell the driver the customer location

        //get customerID
        AssignedCustomerRef =FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(driverID).child("CustomerRideID");

        AssignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){   // taxi requested

                    // retrieve customerID
                    customerID = snapshot.getValue().toString();

                    //get customer location
                    GetAssignedCustomerPickUpLocation();
                }

                else{
                    customerID = "";
                    if (pickupmarker != null){
                        pickupmarker.remove();
                    }

                    if (AssignedCustomerPickupRefListner != null){
                        AssignedCustomerPickupRef.removeEventListener(AssignedCustomerPickupRefListner);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


    }

    private void GetAssignedCustomerPickUpLocation() {
        AssignedCustomerPickupRef = FirebaseDatabase.getInstance().getReference().child("Customer Request")
                .child(customerID).child("l"); // l = location  0 = latitude     1 = longitude


        AssignedCustomerPickupRefListner =  AssignedCustomerPickupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    List<Object> customerlocationmap = (List<Object>) snapshot.getValue();
                    double Locationlat = 0;
                    double Locationlong = 0;

                    if(customerlocationmap.get(0) != null){
                        // typecast latitude to double if value not null
                        Locationlat = Double.parseDouble(customerlocationmap.get(0).toString());
                    }

                    if(customerlocationmap.get(1) != null){
                        // typecast latitude to double if value not null
                        Locationlong = Double.parseDouble(customerlocationmap.get(1).toString());
                    }

                    LatLng Driverlatlng = new LatLng(Locationlat,Locationlong);
                    mMap.addMarker(new MarkerOptions().position(Driverlatlng).title("Pickup Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.pickup)));



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // part of GoogleApiClient.ConnectionCallbacks
        // connection established

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);  // 1s
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // part of GoogleApiClient.ConnectionCallbacks
        // connection suspended

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // part of GoogleApiClient.OnConnectionFailedListener
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently




    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // location updated
        // part of com.google.android.gms.location.LocationListener

        if(getApplicationContext() != null){
            lastLocation = location;
            LatLng latling = new LatLng(location.getLatitude(),location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latling));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

            // get the driver's user id
            String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // initialise a  reference to the database
            DatabaseReference DriverAvailabilityRef;

            // create a reference from "Drivers Available" node which stores all online drivers NOT assigned a job
            DriverAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
            GeoFire geoFireavailable = new GeoFire(DriverAvailabilityRef);

            // create a reference from "Drivers Working" node which stores all online drivers assigned a job
            DatabaseReference DriverWorkingRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");
            GeoFire geoFireworking = new GeoFire(DriverWorkingRef);


            switch(customerID){
                case "":
                    // no customer request a taxi
                    // driver available
                    // driver not working

                    geoFireworking.removeLocation(driverID);

                    // save the location(latitude,longitude) of the driver in the "Drivers Available" Node
                    geoFireavailable.setLocation(driverID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;

                default:
                    // a customer requested so driver assigned job
                    //  driver working
                    // driver not available

                    geoFireavailable.removeLocation(driverID);

                    // save the location(latitude,longitude) of the driver in the "Drivers Working" Node
                    geoFireworking.setLocation(driverID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }
        }
    }

    protected synchronized void buildGoogleApiClient(){

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!logout_status){ //logout_status is true so druver needs to be disconnected from the system
            DriverDisconnect();
        }



    }

    private void DriverDisconnect() {

        // get user id
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //show drivers available in near by place
        DatabaseReference DriverAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");

        GeoFire geoFire = new GeoFire(DriverAvailabilityRef);

        // remove driver
        geoFire.removeLocation(userID, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);

    }

    private void LogoutDriver() {
        // transfer back to Second Activity

        Intent backIntent = new Intent(DriverMapsActivity.this,SecondActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(backIntent);
        finish();

    }



}


