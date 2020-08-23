package com.example.user.wintervacation;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.*;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MapsActivity extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback {
    private static String REGEX_CHINESE = "[\u4e00-\u9fa5]";
    Pattern pat = Pattern.compile(REGEX_CHINESE);

    private static final String API_KEY = "AIzaSyBMum64_lpZuX7_M0ua4Mwc8aqz3CyArLI";
    private GoogleMap mMap;
    private Button navi = null;
    private EditText end = null;
    private Button enter = null;
    private EditText number=null;
    private FusedLocationProviderClient mFusedLocationClient;
    private double Longitude;
    private double Latitude;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private JSONObject oJson;
    private int i = 0;
    private int cnt = 0;

    private Set<BluetoothDevice> mPairedDevices;
    private BluetoothAdapter mBTAdapter;
    private Handler mHandler;
    // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread;
    // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null;
    // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    /*@Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("aaqq", "hahaha");
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("aaqq", "ahahah");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }*/
    private String _recieveData = "";
    String phonenumber = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {






        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        3);
            }
        }
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        mPairedDevices = mBTAdapter.getBondedDevices();

        new Thread() {
            public void run() {
                boolean fail = false;
                //取得裝置MAC找到連接的藍芽裝置
                BluetoothDevice device = mBTAdapter.getRemoteDevice("00:18:E4:35:26:52");

                try {
                    mBTSocket = createBluetoothSocket(device);
                    //建立藍芽socket
                } catch (IOException e) {
                    fail = true;
                    Toast.makeText(getBaseContext(), "Socket creation failed",
                            Toast.LENGTH_SHORT).show();
                }
                // Establish the Bluetooth socket connection.
                try {
                    mBTSocket.connect(); //建立藍芽連線
                } catch (IOException e) {
                }
                if (fail == false) {
                    //開啟執行緒用於傳輸及接收資料
                    mConnectedThread = new ConnectedThread(mBTSocket);
                    mConnectedThread.start();
                }
            }
        }.start();


        mLocationCallback = new LocationCallback() {
            boolean a=false;
            String result = null;
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    if (mConnectedThread != null) {
                        if (oJson != null) {
                            try {
                                JSONArray routeArray = oJson.getJSONArray("routes");
                                JSONObject route = routeArray.getJSONObject(0);
                                JSONArray legsArray = route.getJSONArray("legs");
                                JSONObject legs = legsArray.getJSONObject(0);
                                JSONArray stepsArray = legs.getJSONArray("steps");
                                JSONObject step = stepsArray.getJSONObject(i);
                                JSONObject startlocation = step.getJSONObject("start_location");
                                String lat = startlocation.getString("lat");
                                String lng = startlocation.getString("lng");
                                double distance = 0.0;
                                MapDAOImpl dao = new MapDAOImpl();
                                Log.d("aaqq", String.valueOf(a));
                                if(a)
                                {
                                    distance = dao.countDistance(lng, lat, String.valueOf(location.getLongitude()), String.valueOf(location.getLatitude()));
                                    result = step.getString("html_instructions");
                                    result = result.replace("\u003c", "");
                                    result = result.replace("\u003e", "");
                                    result = result.replace("/", "");
                                    result = result.replace("b", "");
                                    Toast.makeText(MapsActivity.this, String.valueOf((int)distance), Toast.LENGTH_SHORT).show();
                                    result = result.concat(String.valueOf((int)distance).concat("m"));
                                    Matcher mat = pat.matcher(result);
                                    mConnectedThread.write(mat.replaceAll(""));
                                }

                                if (((location.getLatitude() - Double.parseDouble(lat)) < 0.0002 && (Double.parseDouble(lat) - location.getLatitude()) < 0.0002) && ((location.getLongitude() - Double.parseDouble(lng)) < 0.0002 && (Double.parseDouble(lng) - location.getLongitude()) < 0.0002)) {
                                    cnt++;
                                    if (cnt == 3) {
                                        result = step.getString("html_instructions");
                                        result = result.replace("\u003c", "");
                                        result = result.replace("\u003e", "");
                                        result = result.replace("/", "");
                                        result = result.replace("b", "");
                                        Matcher mat = pat.matcher(result);
                                        mConnectedThread.write(mat.replaceAll(""));
                                        Toast.makeText(MapsActivity.this,mat.replaceAll(""), Toast.LENGTH_SHORT).show();
                                        i++;
                                        cnt = 0;
                                        a=true;
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(2500);
        mLocationRequest.setFastestInterval(2500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_page);
        navi = findViewById(R.id.navigate);
        end = findViewById(R.id.end);
        enter = findViewById(R.id.enter);
        number = findViewById(R.id.number);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Longitude = location.getLongitude();
                            Latitude = location.getLatitude();
                        }
                    }
                });
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phonenumber = "tel:";
                phonenumber=phonenumber.concat(number.getText().toString());

            }
        });

        navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder sb = new StringBuilder();//parsing the edit text into string for source location
                EditText endtxt = (EditText) end;
                Log.d("aaqq", "A");
                String strend = endtxt.getText().toString();
                sb.append("https://maps.googleapis.com/maps/api/directions/json?origin=");
                sb.append(String.valueOf(Latitude));
                Log.d("aaqq", "B");
                sb.append(",");
                sb.append(String.valueOf(Longitude));
                sb.append("&destination=");
                sb.append(strend);
                sb.append("&key=AIzaSyBMum64_lpZuX7_M0ua4Mwc8aqz3CyArLI");
                Log.d("aaqq", sb.toString());
                onNavi(sb.toString());

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mBTSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    public void onNavi(String url) {
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
        HttpConnectTask task = new HttpConnectTask();
        Log.d("aaqq", "C");
        task.execute(url);
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
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Latitude, Longitude), 15.0f));
        // Permission is not granted
        // No explanation needed; request the permission
        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
        // app-defined int constant. The callback method gets the
        // result of the reques

    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    private class HttpConnectTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder sb = new StringBuilder();
            Log.d("aaqq", "D");
            try {
                HttpURLConnection htc = (HttpURLConnection) new URL(strings[0]).openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(htc.getInputStream()));
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    line = br.readLine();
                }
                publishProgress(new Integer(0));

            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();

        }

        @Override
        protected void onPostExecute(String result) {
            drawPath(result);
        }
    }


    public void drawPath(String result) { //function to draw the routes between the src and the destination
        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            oJson = json;
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject route = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = route.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(12)
                    .color(Color.RED)//Google maps blue color
                    .geodesic(true)
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            }
            while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            }
            while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if (bytes != 0) {

                        SystemClock.sleep(100);
                        //pause and wait for rest of data
                        bytes = mmInStream.available();
                        // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes);

                        final String strReceived = new String(buffer, 0, bytes);
                        // record how many bytes we actually read
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (strReceived.equals("2"))
                                {
                                    Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                                    phoneIntent.setData(Uri.parse(phonenumber));
                                    startActivity(phoneIntent);
                                }
                                if (strReceived.equals("1"))
                                {
                                    Toast.makeText(MapsActivity.this,"酒精超標了!!!", Toast.LENGTH_SHORT).show();
                                    System.exit(0);
                                }



                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }


        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws
            IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    public static class MapDAOImpl {
        private static final double EARTH_RADIUS = 6378137;

        public double countDistance(String lng1, String lat1, String lng2, String lat2) {
            double radLng1 = rad(Double.parseDouble(lng1));
            double radLat1 = rad(Double.parseDouble(lat1));

            double radLng2 = rad(Double.parseDouble(lng2));
            double radLat2 = rad(Double.parseDouble(lat2));

            if (radLat1 < 0)
                radLat1 = Math.PI / 2 + Math.abs(radLat1);// south
            if (radLat1 > 0)
                radLat1 = Math.PI / 2 - Math.abs(radLat1);// north
            if (radLng1 < 0)
                radLng1 = Math.PI * 2 - Math.abs(radLng1);// west
            if (radLat2 < 0)
                radLat2 = Math.PI / 2 + Math.abs(radLat2);// south
            if (radLat2 > 0)
                radLat2 = Math.PI / 2 - Math.abs(radLat2);// north
            if (radLng2 < 0)
                radLng2 = Math.PI * 2 - Math.abs(radLng2);// west
            double x1 = EARTH_RADIUS * Math.cos(radLng1) * Math.sin(radLat1);
            double y1 = EARTH_RADIUS * Math.sin(radLng1) * Math.sin(radLat1);
            double z1 = EARTH_RADIUS * Math.cos(radLat1);

            double x2 = EARTH_RADIUS * Math.cos(radLng2) * Math.sin(radLat2);
            double y2 = EARTH_RADIUS * Math.sin(radLng2) * Math.sin(radLat2);
            double z2 = EARTH_RADIUS * Math.cos(radLat2);

            double d = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)
                    + (z1 - z2) * (z1 - z2));
// 餘弦定理求夾角
            double theta = Math.acos((EARTH_RADIUS * EARTH_RADIUS + EARTH_RADIUS
                    * EARTH_RADIUS - d * d)
                    / (2 * EARTH_RADIUS * EARTH_RADIUS));
            double dist = theta * EARTH_RADIUS;
            return dist;
        }

        private static double rad(double d) {
            return d * Math.PI / 180.0;
        }


    }
}

