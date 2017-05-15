package com.example.naran.location;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.naran.location.Modules.DirectionsJSONParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.naran.location.R.id.map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback
        , GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener
        , GoogleMap.OnMarkerDragListener, GoogleMap.InfoWindowAdapter,
        LocationListener {

    private GoogleMap mMap;

    ArrayList<LatLng> markerPoints;
    TextView tvDistanceDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        //for getting user origin and destination

        tvDistanceDuration = (TextView) findViewById(R.id.tv_distance_time);


        markerPoints = new ArrayList<LatLng>();

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

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            onLocationChanged(location);
        }

        locationManager.requestLocationUpdates(bestProvider, 2000, 0, this);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        } else {

            mMap.setMyLocationEnabled(true);
        }

        //build a polyline

        LatLng myLocation = new LatLng(27.670925, 85.308168);
        LatLng locationOne = new LatLng(27.672948, 85.309001);
        LatLng locationTwo = new LatLng(27.671588, 85.311905);
        LatLng locationThree = new LatLng(27.670722, 85.311710);
        LatLng locationFour = new LatLng(27.669647, 85.310258);
        mMap.addPolyline(new PolylineOptions().add(

                myLocation,
                locationOne,
                locationTwo,
                locationThree,
                locationFour,
                myLocation
                )
                        .width(10).color(Color.BLACK)
        );
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setInfoWindowAdapter(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.maptypeHYBRID:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.maptypeNORMAL:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.maptypeTERRAIN:

                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;

            case R.id.maptypeSATELITE:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.menu_addmarkers:
                addMarker();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void addMarker() {

        if (mMap != null) {

            //create custom LinearLayout programmatically
            LinearLayout layout = new LinearLayout(MapsActivity.this);
            layout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.setOrientation(LinearLayout.VERTICAL);

            final EditText titleField = new EditText(MapsActivity.this);
            titleField.setHint("Title");

            final EditText latField = new EditText(MapsActivity.this);
            latField.setHint("Latitude");
            latField.setInputType(InputType.TYPE_CLASS_NUMBER
                    | InputType.TYPE_NUMBER_FLAG_DECIMAL
                    | InputType.TYPE_NUMBER_FLAG_SIGNED);

            final EditText lonField = new EditText(MapsActivity.this);
            lonField.setHint("Longitude");
            lonField.setInputType(InputType.TYPE_CLASS_NUMBER
                    | InputType.TYPE_NUMBER_FLAG_DECIMAL
                    | InputType.TYPE_NUMBER_FLAG_SIGNED);

            layout.addView(titleField);
            layout.addView(latField);
            layout.addView(lonField);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add Marker");
            builder.setView(layout);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean parsable = true;
                    Double lat = null, lon = null;

                    String strLat = latField.getText().toString();
                    String strLon = lonField.getText().toString();
                    String strTitle = titleField.getText().toString();

                    try {
                        lat = Double.parseDouble(strLat);
                    } catch (NumberFormatException ex) {
                        parsable = false;
                        Toast.makeText(MapsActivity.this,
                                "Latitude does not contain a parsable double",
                                Toast.LENGTH_LONG).show();
                    }

                    try {
                        lon = Double.parseDouble(strLon);
                    } catch (NumberFormatException ex) {
                        parsable = false;
                        Toast.makeText(MapsActivity.this,
                                "Longitude does not contain a parsable double",
                                Toast.LENGTH_LONG).show();
                    }

                    if (parsable) {
                        LatLng targetLatLng = new LatLng(lat, lon);
                        MarkerOptions markerOptions =
                                new MarkerOptions().position(targetLatLng).title(strTitle);
                        mMap.addMarker(markerOptions);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(targetLatLng));
                    }
                }
            });
            builder.setNegativeButton("Cancel", null);

            builder.show();
        } else {
            Toast.makeText(MapsActivity.this, "Map not ready", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {

        if(markerPoints.size()>1){
            markerPoints.clear();
            mMap.clear();
        }

        // Adding new item to the ArrayList
        markerPoints.add(latLng);

        // Creating MarkerOptions
        MarkerOptions options = new MarkerOptions();

        // Setting the position of the marker
        options.position(latLng);

        /**
         * For the start location, the color of marker is GREEN and
         * for the end location, the color of marker is RED.
         */
        if(markerPoints.size()==1){
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }else if(markerPoints.size()==2){
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

        // Add new marker to the Google Map Android API V2
        mMap.addMarker(options);

        // Checks, whether start and end locations are captured
        if(markerPoints.size() >= 2){
            LatLng origin = markerPoints.get(0);
            LatLng dest = markerPoints.get(1);

            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(origin, dest);

            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }


    @Override
    public void onMapLongClick(LatLng latLng) {

        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(latLng.toString());
        markerOptions.draggable(true);
        mMap.addMarker(markerOptions);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

        marker.setTitle(marker.getPosition().toString());
        marker.showInfoWindow();
        marker.setAlpha(0.5f);

    }

    @Override
    public void onMarkerDrag(Marker marker) {

        marker.setTitle(marker.getPosition().toString());
        marker.showInfoWindow();
        marker.setAlpha(0.5f);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

        marker.setTitle(marker.getPosition().toString());
        marker.showInfoWindow();
        marker.setAlpha(1.0f);

    }

    @Override
    public View getInfoWindow(Marker marker) {
        return prepareInfoView(marker);
    }

    private View prepareInfoView(Marker marker) {

        LinearLayout infoView = new LinearLayout(MapsActivity.this);
        LinearLayout.LayoutParams infoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoView.setOrientation(LinearLayout.HORIZONTAL);
        infoView.setLayoutParams(infoViewParams);

        ImageView infoImageView = new ImageView(MapsActivity.this);

        //Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher);

        Drawable drawable = getResources().getDrawable(android.R.drawable.ic_dialog_map);
        infoImageView.setImageDrawable(drawable);
        infoView.addView(infoImageView);

        LinearLayout subInfoView = new LinearLayout(MapsActivity.this);
        LinearLayout.LayoutParams subInfoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subInfoView.setOrientation(LinearLayout.VERTICAL);

        subInfoView.setLayoutParams(subInfoViewParams);

        TextView subInfoLat = new TextView(MapsActivity.this);
        subInfoLat.setText("Latitude: " + marker.getPosition().latitude);
        TextView subInfoLnt = new TextView(MapsActivity.this);
        subInfoLnt.setText("Longitude: " + marker.getPosition().longitude);
        subInfoView.addView(subInfoLat);
        subInfoView.addView(subInfoLnt);
        infoView.addView(subInfoView);
        return infoView;

    }

    @Override
    public View getInfoContents(Marker marker) {
        return prepareInfoView(marker);
    }

    GoogleMap.OnInfoWindowClickListener minfoClick = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {

            Toast.makeText(MapsActivity.this,
                    "onInfoWindowClick():\n" +
                            marker.getPosition().latitude + "\n" +
                            marker.getPosition().longitude,
                    Toast.LENGTH_LONG).show();

        }
    };

    @Override
    public void onLocationChanged(Location location) {

//        double latitude = location.getLatitude();
//        double longitude = location.getLongitude();
//        LatLng latLng = new LatLng(latitude, longitude);
//        mMap.addMarker(new MarkerOptions().position(latLng));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {


    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {


    }


    private class DownloadTask extends AsyncTask<String, Void,String>{
        @Override
        protected String doInBackground(String... params) {

            // For storing data from web service
            String data = "";
            try{
                // Fetching the data from web service
                data = downloadUrl(params[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private String downloadUrl(String param) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(param);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception downloading", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;

    }


    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... params) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(params[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";

            if(result.size()<1){
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    if(j==0){    // Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);
            }

            tvDistanceDuration.setText("Distance:"+distance + ", Duration:"+duration);

            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }
}
