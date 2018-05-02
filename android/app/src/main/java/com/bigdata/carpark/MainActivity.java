package com.bigdata.carpark;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.List;

import static com.bigdata.carpark.SelectCarParkActivity.mAndroidMapList;
import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;

/**
 * Created by Rach on 25/4/2018.
 */

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final String KEY_NUM = "car_park_no";
    private static final String KEY_ADDR = "address";
    private static final String KEY_TYPE = "car_park_type";
    private static final String KEY_X_COORD = "x_coord";
    private static final String KEY_Y_COORD = "y_coord";

    public static String carParkName;
    public static String availability="";
    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private TextView carparkNameText;
    private TextView predictedAvailabilityText;

    public static int minteger = 0;

    public static int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_maps);

        mGeoDataClient = Places.getGeoDataClient(this, null);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initializeView();
        checkAvailability();

    }

    public void initializeView() {
        carparkNameText = (TextView) findViewById(R.id.get_selected_car_park);

        predictedAvailabilityText = (TextView) findViewById(R.id.get_selected_car_park_availability);
        /*if (Double.parseDouble((String) predictedAvailabilityText.getText()) > 50) {
            predictedAvailabilityText.setTextColor(Color.parseColor("#32CD32"));
        } else {
            predictedAvailabilityText.setTextColor(Color.parseColor("#FFA500"));
        }*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        carparkNameText.setText(carParkName);
        predictedAvailabilityText.setText(availability + " % available! ");
       /* if (Double.parseDouble((String) predictedAvailabilityText.getText()) > 50) {
            predictedAvailabilityText.setTextColor(Color.parseColor("#32CD32"));
        } else {
            predictedAvailabilityText.setTextColor(Color.parseColor("#FFA500"));
        }*/
        if (mMap != null) {
            mMap.clear();
            Geocoder coder = new Geocoder(this);
            List<Address> address;
            try {
                String locationName = carParkName;
                Geocoder gc = new Geocoder(this);
                List<Address> addressList = coder.getFromLocationName(locationName, 5);
                Address location = addressList.get(0);

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                LatLng dataloc = new LatLng(latitude, longitude);
                //mMap.setMyLocationEnabled(true);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dataloc, 13));

                mMap.addMarker(new MarkerOptions().position(dataloc).title(carParkName));

            } catch (Exception e) {

            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            openSelectCarPark();
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);
                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());
                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());
                return infoWindow;
            }
        });
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
    }

    private void getDeviceLocation() {

        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(
                                    newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }


    private void openSelectCarPark() {
        Intent intent = new Intent(this, SelectCarParkActivity.class);
        startActivity(intent);
    }


    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void increaseMin(View view) {
        minteger = minteger + 15;
        display(minteger);
        String EMPTY = "";
        if (!EMPTY.equals(carparkNameText.getText())) {
            final Button button = (Button) findViewById(R.id.check_results);
            button.setVisibility(View.VISIBLE);
        }

    }

    public void decreaseMin(View view) {
        minteger = minteger - 15;
        display(minteger);
        String EMPTY = "";
        if (!EMPTY.equals(carparkNameText.getText())) {
            final Button button = (Button) findViewById(R.id.check_results);
            button.setVisibility(View.VISIBLE);
        }
    }

    private void display(int number) {
        TextView displayInteger = (TextView) findViewById(
                R.id.integer_number);
        displayInteger.setText("" + number + " m");
    }

    public void checkAvailability() {
        final Button button = (Button) findViewById(R.id.check_results);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.setVisibility(View.INVISIBLE);
                // Code here executes on main thread after user presses button
                SocketThread socketThread = new SocketThread(position);
                socketThread.start();

                try {
                    Thread.currentThread().sleep(1000);
                    predictedAvailabilityText.setText(availability +" % available!");
                   /* if (Double.parseDouble((String) predictedAvailabilityText.getText()) > 50) {
                        predictedAvailabilityText.setTextColor(Color.parseColor("#32CD32"));
                    } else {
                        predictedAvailabilityText.setTextColor(Color.parseColor("#FFA500"));
                    }*/
                } catch (Exception e) {

                }
            }
        });

    }

    class SocketThread extends Thread {
        int position;

        public SocketThread(int position) {
            this.position = position;
        }

        public void run() {
            Socket socket = null;
            OutputStream output = null;
            try {
                //socket = new Socket("172.31.67.160", 15000);
                socket = new Socket("192.168.43.187", 15000);
                output = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            PrintWriter writer = new PrintWriter(output, true);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, minteger);

            String time = calendar.get(Calendar.HOUR) + "" + String.format("%02d", calendar.get(Calendar.MINUTE)) + "" + String.format("%02d", calendar.get(Calendar.SECOND));
            writer.println(mAndroidMapList.get(position).get(KEY_NUM) + "," + time);
            InputStream is = null;
            String predictedAvailability = null;
            MainActivity.carParkName = mAndroidMapList.get(position).get(KEY_ADDR);
            try {
                is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isr);
                predictedAvailability = reader.readLine();
                MainActivity.availability = predictedAvailability;

                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("PREDICTION", predictedAvailability);
            // Toast.makeText(SelectCarParkActivity.this,predictedAvailability,Toast.LENGTH_SHORT).show();

        }
    }
}
