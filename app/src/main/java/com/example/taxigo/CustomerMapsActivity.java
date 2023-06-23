package com.example.taxigo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.taxigo.databinding.ActivityCustomerMapsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomerMapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private ActivityCustomerMapsBinding binding;

    GoogleApiClient googleApiClient;  // access google api
    Location lastLocation;
    LatLng pickup_location;
    double radius = 1.0;


    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    GeoQuery geoQuery;

    // initialise a  reference to the customer dataset
    DatabaseReference CustomerRequestRef;

    // initialise a  reference to the driver dataset
    DatabaseReference DriverAvailableRef,DriverRef,DriverLocationRef;

    Marker pickupmarker;

    ValueEventListener DriverLocationRefListener;

    Boolean driver_found = false;
    Boolean requestType = false;  // flag to tell if request for booking or cancelling
    String driver_foundID;
    Button book_taxi;
    String customerID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomerMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        book_taxi = findViewById(R.id.booktaxi);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // create a reference from "Customer Request" node which stores all customers that want to book a ride
        CustomerRequestRef = FirebaseDatabase.getInstance().getReference().child("Customer Request");

        // get reference from "Drivers Available" node which stores all online drivers
        DriverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");

        // get reference from "Drivers Working" node which stores all booked drivers
        DriverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");

        book_taxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // register customer on database using CustomerRequestRef

                if (requestType){
                    // user requested for a taxi
                    requestType = false;
                    geoQuery.removeAllListeners();
                    DriverLocationRef.removeEventListener(DriverLocationRefListener);

                    if (driver_found != null){
                        DriverRef = FirebaseDatabase.getInstance().getReference().child("Users")
                                .child("Drivers").child(driver_foundID).child("CustomerRideID");
                        driver_foundID = null;

                        DriverRef.removeValue();
                    }

                    driver_found = false;
                    radius = 1;

                    GeoFire geoFire = new GeoFire(CustomerRequestRef);
                    geoFire.removeLocation(customerID);

                    if (pickupmarker != null){
                        pickupmarker.remove();
                    }

                    book_taxi.setText("TaxiGo");

                }

                else {
                    // user has not requested for cancellation

                    requestType = true;


                    GeoFire geoFire = new GeoFire(CustomerRequestRef);

                    // save the location(latitude,longitude) of the customer in the "Customer Request" Node
                    geoFire.setLocation(customerID,new GeoLocation(lastLocation.getLatitude(),lastLocation.getLongitude()));

                    // show pickup location on map
                    pickup_location = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(pickup_location).title("You"));

                    book_taxi.setText("Getting your driver");
                    GetClosestTaxi();


                }



            }
        });




    }

    private void GetClosestTaxi() {
        // get the ID of driver from "Driver Availabilty" node

        GeoFire geoFire = new GeoFire(DriverAvailableRef);


        // search radius = 1
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickup_location.latitude,pickup_location.longitude),radius);

        geoQuery.removeAllListeners();



        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // called when a driver nearby customer location

                if (!driver_found && requestType){
                    driver_found = true;
                    driver_foundID = key; // driver key returned by database

                    DriverRef = FirebaseDatabase.getInstance().getReference().child("Users")
                            .child("Drivers").child(driver_foundID);

                    HashMap driverMap = new HashMap();

                    // put id of the customer who wants a ride
                    driverMap.put("CustomerRideID",customerID);

                    DriverRef.updateChildren(driverMap);
                    
                    //show customer driver's location
                    GetDriverLocation();
                    book_taxi.setText("Locating driver coordinates");


                }



            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                // called when driver not available
                // increment search radius

                if (!driver_found){
                    radius ++;
                    GetClosestTaxi();

                }



            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    private void GetDriverLocation() {
        // get location of the driver

        DriverLocationRefListener = DriverLocationRef.child(driver_foundID).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists() && requestType){
                            List<Object> drivelocationMap = (List<Object>) snapshot.getValue();
                            double Locationlat = 0;
                            double Locationlong = 0;
                            book_taxi.setText("Driver Found");

                            if(drivelocationMap.get(0) != null){
                                // typecast latitude to double if value not null
                                Locationlat = Double.parseDouble(drivelocationMap.get(0).toString());
                            }

                            if(drivelocationMap.get(1) != null){
                                // typecast latitude to double if value not null
                                Locationlong = Double.parseDouble(drivelocationMap.get(1).toString());
                            }

                            LatLng drivelatlng = new LatLng(Locationlat,Locationlong);

                            if (pickupmarker != null){
                                pickupmarker.remove();
                            }

                            // get customer location
                            Location location1 = new Location("");
                            location1.setLatitude(pickup_location.latitude);
                            location1.setLongitude(pickup_location.longitude);

                            // get driver location
                            Location location2 = new Location("");
                            location2.setLatitude(drivelatlng.latitude);
                            location2.setLongitude(drivelatlng.longitude);

                            // distance between customer and driver
                            float dist = location1.distanceTo(location2);
                            book_taxi.setText("Driver Found");

                            if (dist<90){
                                book_taxi.setText("Your Taxi has Arrived");
                            }



                            pickupmarker = mMap.addMarker(new MarkerOptions().position(drivelatlng).title("Your Taxi has arrived").icon(BitmapDescriptorFactory.fromResource(R.drawable.drriveronmap)));

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Change the map type based on the user's selection.
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
                //call logout code

                mAuth.signOut();
                LogoutCustomer();



            case R.id.settings:
                // call settings option
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private void CustomerDisconnect() {
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // part of GoogleApiClient.ConnectionCallbacks
        // connection established

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);  // 1s
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);



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
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // location updated
        // part of com.google.android.gms.location.LocationListener

        lastLocation = location;
        LatLng latling = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latling));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));





    }



    @Override
    protected void onStop() {
        super.onStop();

    }

    private void LogoutCustomer() {
        // transfer back to Second Activity
        Intent gobackIntent = new Intent(CustomerMapsActivity.this,SecondActivity.class);

        gobackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(gobackIntent);
        finish();


    }
}