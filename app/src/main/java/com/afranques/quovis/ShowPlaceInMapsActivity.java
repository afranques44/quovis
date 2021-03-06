package com.afranques.quovis;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wunderlist.slidinglayer.SlidingLayer;

import java.io.File;

public class ShowPlaceInMapsActivity extends AppCompatActivity implements GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback {

    private static final int REQUEST_LOCATION_PERMISSION_CODE = 2;
    private GoogleMap mMap;
    DatabaseHelper myDb;
    private int place_id;
    private String title_place;
    private String description_place;
    private String category_place;
    private String pic_location_place;
    private LatLng placeLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_place_in_maps);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.show_place_maps_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true); //show the return icon on action bar
        }

        //to get the parameters sent from the previous intent
        Intent prevIntent = getIntent();
        place_id = prevIntent.getIntExtra("place_id", -1);
        //Toast.makeText(this, Integer.toString(place_id), Toast.LENGTH_SHORT).show();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_show_place);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topcorner_menu_show_place_in_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_show_maps:
                myDb.deletePlace(place_id);
                //after deleting the place jump to the main activity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_locate_show_maps:
                if (mMap != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLocation, 16));
                } else {
                    Toast.makeText(this, "Error: couldn't center marker", Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.action_edit_show_maps:
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
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
        enableMyLocation();

        myDb = new DatabaseHelper(this);
        Cursor res = myDb.getPlace(place_id);
        if (res.getCount() != 0) {
            res.moveToNext();

            // Add a marker in place location and move the camera
            placeLocation = new LatLng(res.getDouble(4), res.getDouble(5));
            //Toast.makeText(this, res.getString(0)+" "+res.getString(4)+" "+res.getString(5), Toast.LENGTH_LONG).show();
            //String snippetString = "Category: "+res.getString(3)+"\nDescription: "+res.getString(2);

            pic_location_place = res.getString(6);
            //Toast.makeText(this, pic_location, Toast.LENGTH_SHORT).show();

            Cursor catRes = myDb.getCatName(res.getInt(3));
            catRes.moveToNext();
            title_place = res.getString(1);
            description_place = res.getString(2);
            String description_place_hint;
            if (description_place.length() > 45) {
                description_place_hint = description_place.substring(0,44)+"...";
            } else {
                description_place_hint = description_place;
            }
            category_place = catRes.getString(0);
            if (mMap != null) {
                mMap.addMarker(new MarkerOptions()
                        .position(placeLocation)
                        .title(res.getString(1))
                        //.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .snippet(description_place_hint)).showInfoWindow();
                mMap.setOnInfoWindowClickListener(this);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLocation, 16));
            } else {
                Toast.makeText(this, "Map is null and couldn't place the marker", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If we don't have access yet, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION_CODE);
        } else if (mMap != null) {
            // If we already have access, prepare maps
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        TextView placeTitle = (TextView) findViewById(R.id.show_place_title);
        placeTitle.setText(title_place);

        TextView placeDescription = (TextView) findViewById(R.id.show_place_description);
        placeDescription.setText(Html.fromHtml("<b>Note: </b>"+description_place));

        TextView placeCategory = (TextView) findViewById(R.id.show_place_category);
        placeCategory.setText(Html.fromHtml("<b>Category: </b>"+category_place));

        BitmapFactory.Options options = new BitmapFactory.Options();
        ImageView myImage = (ImageView) findViewById(R.id.show_thePicture);
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(pic_location_place, options);
        myImage.setImageBitmap(bitmap);

        //Toast.makeText(this, "Info window clicked",Toast.LENGTH_SHORT).show();
        SlidingLayer slidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
        slidingLayer.openPreview(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION_CODE: {
                if (grantResults.length > 0) {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // permission was granted, yay! Do the
                        // contacts-related task you need to do.
                        enableMyLocation();
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Toast.makeText(this, "We need location to get your place", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        Toast.makeText(this, "The app is not gonna work without location permitions, so go to settings and allow it", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                    }
                    //return;
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
