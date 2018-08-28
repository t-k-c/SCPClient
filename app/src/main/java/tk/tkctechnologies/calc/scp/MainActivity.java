package tk.tkctechnologies.calc.scp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    private IntentIntegrator qrScan;
    boolean located=false;
    boolean gpsOn=true;
    ProgressBar progressBar;
    TextView textView;
    String currentLocation="";
    Location locationObject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        qrScan = new IntentIntegrator(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);*/
         progressBar = (ProgressBar) findViewById(R.id.progressBar2);
         textView = (TextView) findViewById(R.id.textV);
//        qrScan.initiateScan();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Please give location access", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                   double lat =  location.getLatitude();
                   double longitude =  location.getLongitude();
                    currentLocation=lat+","+longitude;
                    if(!located ){
                    qrScan.initiateScan();
                        located=true;
                    }
                    locationObject = location;
                    textView.setText("cord: "+currentLocation);

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {
                    if(!gpsOn){
                        gpsOn=true;
                    }
                }

                @Override
                public void onProviderDisabled(String s) {
                   if(gpsOn){
                    Toast.makeText(MainActivity.this, "Please activate your gps", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                       gpsOn=false;
                   }
                }
            });
        }
    }
   String ip="192.168.1.100";
    //Getting the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        located=true;
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
               String contents = result.getContents();
                boolean status=true;
                if(contents.startsWith("&&::")){
                    contents = contents.substring(4,contents.length());
//                    Toast.makeText(this, contents, Toast.LENGTH_SHORT).show();
//                    Toast.makeText(this, "http://"+ip+"/SCP/rowcall/?class="+contents+"&"+"id="+1234, Toast.LENGTH_LONG).show();
                    final Uri uri = Uri.parse("http://"+ip+"/SCP/rowcall/?class="+contents+"&"+"id="+1234);
                   new Thread(new Runnable(){
                       @Override
                       public void run() {
                           try {
                               final Location location = new Location(LocationManager.GPS_PROVIDER);
                               location.setLatitude(3.85024993);
                               location.setLongitude(11.49930528);
                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       Toast.makeText(MainActivity.this,"Dist="+locationObject.distanceTo(location), Toast.LENGTH_SHORT).show();
                                   }
                               });
                               if(locationObject.distanceTo(location)<=10.0){
                              final String resp =  DataBaseResponse.getPostResponseData(uri.toString(),null,null);
                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
//                                       Toast.makeText(MainActivity.this, "resp = "+resp, Toast.LENGTH_SHORT).show();
                                       progressBar.setVisibility(View.GONE);
                                       textView.setText("You are present at "+currentLocation);
                                   }
                               });
                               }else{
                                   runOnUiThread(new Runnable() {
                                       @Override
                                       public void run() {
                                           Toast.makeText(MainActivity.this, "You are at a far distance", Toast.LENGTH_LONG).show();
                                       }
                                   });
                               }
                           } catch (IOException e) {
                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       Toast.makeText(MainActivity.this, "can't connect", Toast.LENGTH_SHORT).show();
                                   }
                               });
                               e.printStackTrace();
                           }
                       }
                   }).start();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}