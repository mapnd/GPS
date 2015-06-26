package nd.edu.mapresearch;

/*
 * MainActivity.java 1.0 Jan 27, 2014
 *
 */

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public  GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private double currentLatitude;//The current latitude of the user
    private double currentLongitude;//The current longitude of the user
    private Marker curSelectedMarker;//when a user clicks on a marker, this is it
    private LatLng currentPositionLagLng;//The current position of the user as a LatLng object
    private boolean markerClicked;//Keeps track if a marker is clicked
    private boolean polyLineDrawn;//Keeps track if a route(polyLine) is drawn
    public TextView distanceDuration;//This textview displays the distance and duration once a user chooses to map directions
    public TextView directions;
    private String enteredAddress;//Used for adding the title of the marker placed at a searched location
    private ParseObject userObject;//The object that is used to push a new object to the database
    private String iconOfOnLongClick;//Stores the name of the icon chosen by the user
    private boolean circleOnMap;//Keeps track if a circle around the user is on the map
    private boolean startUp;//Used for starting the map on the current location
    private boolean timerIsRunning;//Keeps track if the timer is running
    public Polyline mPolyline;//The polyline that is the directions in between markers
    private boolean dialog_error;
    private String userName;
    private String userID;

    //for use of timer, run parse query every 10 seconds, delay 1
    private final int DELAY_TIME = 10000;//delay 10 second
    private final int PERIOD_TIME = 10000;//run timer every 10 seconds
    private Timer onResumeTimer;

    //which icons to show
    private boolean animalReports = true;
    private boolean roadObsReports = true;
    private boolean policeReports = true;



    //location update
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    // Maximum post search radius for map in Miles
    private double searchDistance = 1;
    private static final double METER_CONVERSION = 1609.344;

    // Hashmap used to track all markers shwon. The key is the marker ID (from the Parser DB).
    private HashMap<String, Marker> visibleMarkers = new HashMap<String, Marker>();

    //Dialog used to show marker information
    private AlertDialog markerDialog;
    private boolean isDialogOpen = false;
    private String idOfMarkerBeingShown;

    private AutoCompleteTextView autoCompleteTextView;
    PlacesTask placesTask;
    ParserTask parserTask;

    private boolean isGPSMode = false;
    private GPSNavigator navigator;
    private String idOfMarkerGps = "";

    final CharSequence[] eventsPlotted ={"Bear",
            "Buffalo",
            "Deer",
            "Police",
            "Major Accident",
            "Minor Accident",
            "Construction",
            "Traffic",
            "Building",
            "Tire",
            "Rain",
            "Truck Stopped"
    };

    private String[] reportDialog = {
            "Animals",
            "Road Obstacles",
            "Police"
    };

    private int[] imageID = {
            R.mipmap.bear,
            R.mipmap.roadopstical,
            R.mipmap.police
    };

    private String[] animalDialog = {
            "Bear",
            "Buffalo",
            "Deer"
    };

    private int[] animalImageID = {
            R.mipmap.bear,
            R.mipmap.buffalo,
            R.mipmap.deer
    };

    private String[] roadObstacleDialog = {
            "Construction",
            "Major Accident",
            "Minor Accident",
            "Tire",
            "Traffic",
            "Truck Stopped"
    };

    private int[] roadObstacleImageID = {
            R.mipmap.construction,
            R.mipmap.majoraccident,
            R.mipmap.minoraccident,
            R.mipmap.roadopstical,
            R.mipmap.traffic,
            R.mipmap.truckstopped
    };

    private String[] policeDialog = {
            "Police"
    };
    //Radar maybe
    private int[] policeImageID = {
            R.mipmap.police
    };


    //////////////////////////////
    //Section A
    //////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();

        Toast.makeText(MainActivity.this, "USER: " + getIntent().getStringExtra(LoginActivity.USERNAME), Toast.LENGTH_LONG).show();
        userName = getIntent().getStringExtra(LoginActivity.USERNAME);
        userID = getIntent().getStringExtra(LoginActivity.USER_ID);

        Parse.initialize(getBaseContext(), "lqmx2GmYTOn8of5IM0LrrZ8bYT0ehDvzHTSdGLGA", "Uk4Leh4EpoN0i04lg7fU5yUW7O6UL94RhTdVWfED");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.loc_edit_text);
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesTask = new PlacesTask();
                Log.d("MainActivity", "SEQUENCE: " + s);
                placesTask.execute(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });


        //setting bools to false
        markerClicked = false;
        polyLineDrawn = false;
        circleOnMap = false;
        startUp = false;

        timerIsRunning = false;

        //setting to null
        enteredAddress = "";
        iconOfOnLongClick = "";

        userObject = new ParseObject("PlaceObject");

        distanceDuration = (TextView) findViewById(R.id.tv_distance_time);
        directions = (TextView) findViewById(R.id.MainDirectionsTextView);

        Button findButton = (Button)findViewById(R.id.find_btn);//button to execute find location
        Button clearButton = (Button)findViewById(R.id.clear_btn);//clears the map
        Button mapDirButton = (Button)findViewById(R.id.map_directions_btn);//maps directions from current location to selected marker
        Button gatherLocations = (Button)findViewById(R.id.query_btn);//maps directions from current location to selected marker
        //listeners for the respective buttons
        gatherLocations.setOnClickListener(gatherLocsButtonListener);
        findButton.setOnClickListener(findClickListener);
        clearButton.setOnClickListener(clearClickListener);
        mapDirButton.setOnClickListener(mapDirClickListener);

        mMap.setOnMarkerClickListener(onMarkerClickListener);
        setUpLongClick();

        if(mMap != null){
            setUpClick();
        }
    }

    //makes sure that app is loading correctly onResume
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        //remembers the users choice of distance from SettingsActivity.java
        SharedPreferences settings = getSharedPreferences("Choice", 0);
        searchDistance = (double)settings.getFloat("search_distance",1);

        //remembers the users choice of which events to filter from SettingsActivity.java
        SharedPreferences settingsBool = getSharedPreferences("Choice",MODE_PRIVATE);
        animalReports = settingsBool.getBoolean("animalChkBox",false);
        roadObsReports = settingsBool.getBoolean("roadChkBox",false);
        policeReports = settingsBool.getBoolean("policeChkBox",false);
        //
        clearMap();
        DoParseQuery();
        drawCircle();
        onResumeTimer = new Timer();
        onResumeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timerIsRunning = true;
                        DoParseQuery();
                        Log.i("ABCDEFGHI", "Ran Update");
                    }
                });
            }
        }, DELAY_TIME, PERIOD_TIME);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        onResumeTimer.cancel();//stopping the timer
        onResumeTimer = null;//null the timer
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("HH", "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("HH", "GoogleApiClient connection has failed");
    }

    //Clears the map, and resets the booleans
    public void clearMap(){
        mMap.clear();
        visibleMarkers.clear();
        polyLineDrawn = false;
        circleOnMap = false;
        navigator.stopGPS();
        isGPSMode = false;
        idOfMarkerGps = "";
    }


    //////////////////////////////
    //Section B
    //////////////////////////////

    //when a marker is clicked and there is no line drawn, map draws the polyLine
    private final OnClickListener gatherLocsButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            DoParseQuery();
        }
    };

    /* Followed tutorial on http://wptrafficanalyzer.in/blog/android-geocoding-showing-user-input-location-on-google-map-android-api-v2/
     */
    //OnClickListener for find button
    private final OnClickListener findClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // Getting reference to EditText to get the user input location
            //EditText editTextLoc = (EditText) findViewById(R.id.loc_edit_text);
            //hide keyboard once clicked
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);

            // Getting user input location
            //String location = editTextLoc.getText().toString();
            String location = autoCompleteTextView.getText().toString();
            enteredAddress = location;

            if(location!=null && !location.equals("")){
                new GeocodingTask().execute(location);
            }
        }
    };

    //when a marker is clicked and there is no line drawn, map draws the polyLine
    private final OnClickListener mapDirClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            if (isGPSMode) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                TextView text = new TextView(MainActivity.this);

                text.setText("Do you want to stop GPS navigation and trace rout to other marker?");
                builder.setView(text);

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isGPSMode = false;
                        navigator.stopGPS();
                        idOfMarkerGps = "";
                        //call method to map the polygon
                        if (markerClicked && !polyLineDrawn) {//if a marker selected and no directions on map
                            mapDirections(currentPositionLagLng, curSelectedMarker);
                        } else if (markerClicked && polyLineDrawn) {//if a marker is selected and directions on map
                            mPolyline.remove();
                            mapDirections(currentPositionLagLng, curSelectedMarker);
                        }
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                });
            } else {
                if (markerClicked && !polyLineDrawn) {//if a marker selected and no directions on map
                    mapDirections(currentPositionLagLng, curSelectedMarker);
                } else if (markerClicked && polyLineDrawn) {//if a marker is selected and directions on map
                    mPolyline.remove();
                    mapDirections(currentPositionLagLng, curSelectedMarker);
                }

            }

        }
    };

    //Method that maps the directions
    public void mapDirections(LatLng curSpot, Marker desSpot){

        LatLng origin = curSpot;
        LatLng dest = desSpot.getPosition();

        if(origin!= null && dest != null){
            // Getting URL to the Google Directions API
            String url = makeDirectionsURL(origin, dest);
            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
            markerClicked = false;
            polyLineDrawn = true;
        }

    }


    /*
     *Sets up the onClickMap Listener
     */
    public void setUpClick(){
        mMap.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                markerClicked = false;//tells map directions that there is no marker clicked
            }
        });
    }

    //Clears map.  For debugging/testing purposes
    private final OnClickListener clearClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            distanceDuration.setText("  ");//clears distanceDuration Text
            clearMap();
        }
    };

    //Detects when a marker is clicked
    private final OnMarkerClickListener onMarkerClickListener = new OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(final Marker marker) {
            Log.d("MainActivity", "marker clicked!");
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            //final TextView textview = (TextView)new TextView(MainActivity.this);
            //builder.setView(textview);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.marker_info_dialog, null);
            builder.setView(view);
            final TextView textview = (TextView)view.findViewById(R.id.markerInfoDialogTextView);
            final TextView votesText = (TextView)view.findViewById(R.id.markerInfoDialogVotesTextview);
            final TextView minutesText = (TextView)view.findViewById(R.id.markerInfoDialogMinutesExpireTextView);
            final Button upButton = (Button)view.findViewById(R.id.markerInfoDialogButtonUp);
            final Button downButton = (Button)view.findViewById(R.id.markerInfoDialogButtonDown);
            ParseQuery query = ParseQuery.getQuery("PlaceObject");
            query.getInBackground(marker.getTitle(), new GetCallback<ParseObject>() {
                @Override
                public void done(final ParseObject obj, ParseException e) {
                    if (e == null){
                        // If current user has already voted, disable vote buttons
                        List<String> userIds = obj.getList("voterslist");
                        if (userIds.contains(userID)) {
                            upButton.setEnabled(false);
                            downButton.setEnabled(false);
                        }
                        // Increment votes and save
                        upButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //String users = obj.getString("voterslist");
                                obj.increment("votes");
                                obj.add("voterslist", userID);
                                obj.saveInBackground();
                                votesText.setText("Votes: " + String.valueOf(obj.getInt("votes")));
                                upButton.setEnabled(false);
                                downButton.setEnabled(false);
                                //List<String> list = new ArrayList<String>(Arrays.asList(users.split(";")));
                            }
                        });
                        // Decrement vote and save. See if marker should be deleted
                        downButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                obj.increment("votes", -1);
                                int votes = obj.getInt("votes");
                                if (votes < -4) {
                                    //Delete marker!
                                    Marker temp = visibleMarkers.get(obj.getObjectId());
                                    temp.remove();
                                    visibleMarkers.remove(obj.getObjectId());
                                    obj.deleteEventually();
                                    markerDialog.dismiss();
                                    isDialogOpen = false;
                                } else {
                                    obj.add("voterslist", userID);
                                    upButton.setEnabled(false);
                                    downButton.setEnabled(false);
                                    obj.saveInBackground();
                                    votesText.setText("Votes: " + String.valueOf(obj.getInt("votes")));
                                }
                            }
                        });

                        // Setting up the creation time
                        Date creationDate = obj.getCreatedAt();
                        Calendar calendar = GregorianCalendar.getInstance();
                        calendar.setTime(creationDate);

                        String tempMinutes = "";
                        if (calendar.get(Calendar.MINUTE) < 10) {
                            tempMinutes = "0" + String.valueOf(calendar.get(Calendar.MINUTE));
                        } else {
                            tempMinutes = String.valueOf(calendar.get(Calendar.MINUTE));
                        }

                        String amorpm = "";
                        String tempHour = "";
                        if (calendar.get(Calendar.HOUR_OF_DAY) == 0) {
                            tempHour = "12";
                            amorpm = "AM";
                        }
                        else if (calendar.get(Calendar.HOUR_OF_DAY) > 12) {
                            tempHour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY) - 12);
                            amorpm = "PM";
                        }
                        else if (calendar.get(Calendar.HOUR_OF_DAY) < 12) {
                            tempHour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                            amorpm = "AM";
                        }
                        else {
                            tempHour = "12";
                            amorpm = "PM";
                        }

                        String hourCreated = tempHour + ":" + tempMinutes + " " + amorpm;

                        // Checking to see how many minutes remaining
                        long timeOfCreation = creationDate.getTime();
                        long currentTime = System.currentTimeMillis();
                        long timeOfExpiration = timeOfCreation + obj.getInt("minutes")*60000;
                        long difference = timeOfExpiration - currentTime;
                        long minutes = difference / 60000;
                        // If is already expired, display it correctly, if not, display the minutes
                        if (currentTime > timeOfExpiration) {
                            minutesText.setText("Minutes until expires: Expired");
                        } else {
                            minutesText.setText("Minutes until expires: " + String.valueOf(minutes));
                        }

                        String text = "Group: " + obj.getString("group") + "\n" +
                                "Notes: " + obj.getString("notes") + "\n" +
                                "Created by: " + obj.getString("username") + "\n" +
                                "Created at: " + hourCreated;
                        textview.setText(text);
                        votesText.setText("Votes: " + String.valueOf(obj.getInt("votes")));

                        //builder.show();
                        markerDialog.show();
                        idOfMarkerBeingShown = obj.getObjectId();
                        isDialogOpen = true;
                    }
                }
            });
            builder.setTitle("Marker Info");
            builder.setPositiveButton("Select marker", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    curSelectedMarker = marker;
                    currentPositionLagLng = new LatLng(currentLatitude, currentLongitude);
                    markerClicked = true;
                    isDialogOpen = false;
                }
            });

            builder.setNeutralButton("GPS", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    RadioGroup radioGroup = new RadioGroup(MainActivity.this);
                    RadioButton walking = new RadioButton(MainActivity.this);
                    int walkingId = View.generateViewId();
                    walking.setId(walkingId);
                    walking.setText("Walking");
                    RadioButton driving = new RadioButton(MainActivity.this);
                    int drivingId = View.generateViewId();
                    driving.setId(drivingId);
                    driving.setText("Driving");
                    radioGroup.addView(walking, 0);
                    radioGroup.addView(driving, 1);

                    final AlertDialog.Builder gpsBuilder = new AlertDialog.Builder(MainActivity.this);
                    gpsBuilder.setView(radioGroup);

                    gpsBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int id = radioGroup.getCheckedRadioButtonId();
                            if (id == walkingId) {
                                startGPSNavigation(marker, GPSNavigator.WALKING_MODE);
                            } else {
                                startGPSNavigation(marker, GPSNavigator.DRIVING_MODE);
                            }
                        }
                    });



                }
            });


            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                /* Exits the dialog */
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing besides exiting dialog
                    isDialogOpen = false;
                }
            });
            //final AlertDialog disDialog = builder.show();
            markerDialog = builder.create();
            //marker.showInfoWindow();

            return true;
        }
    };

    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        currentPositionLagLng = new LatLng(location.getLatitude(),location.getLongitude());
        //LatLng userPosition = new LatLng(location.getLatitude(), location.getLongitude());
        if (mMap != null && !startUp){
            startUp = true;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPositionLagLng, 15));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        menu.findItem(R.id.action_settings).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            }
        });

        menu.findItem(R.id.action_logout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                final SharedPreferences settings = getSharedPreferences("Choice", MODE_PRIVATE);
                final SharedPreferences.Editor editor = settings.edit();
                // Erasing previous username and hashed password
                editor.putString("PreviousUsername", "");
                editor.putString("PreviousPasswordHash", "");
                editor.putString("PreviousUserID", "");
                editor.commit();

                finish();
                // Go back to login screen
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                return true;
            }
        });

        menu.findItem(R.id.action_clear_db).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("PlaceObject");
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null) {
                            Log.d("LoginAcitivity", "Query done!");
                            if (objects.size() == 0) { //no user on DD
                                return;
                            }
                            for (ParseObject marker : objects) {
                                marker.deleteEventually();
                            }
                        }
                    }
                });
                return true;
            }
        });

        menu.findItem(R.id.gps_off).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                navigator.stopGPS();
                isGPSMode = false;
                idOfMarkerGps = "";
                Toast.makeText(MainActivity.this, "GPS Navigation Stopped!", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        return true;
    }


    //////////////////////////////
    //Section C
    //////////////////////////////
    /*
     * Sets up the onLongClickMap Listener
     * When user performs a LongClick,
     * gives option to add a marker with onLongClickMap Listener
     */
    public void setUpLongClick(){
        mMap.setOnMapLongClickListener(new OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                final LatLng a = latLng;
                float distanceBtCurA = distanceAtoB((float)a.latitude,(float)a.longitude,(float)currentPositionLagLng.latitude,(float)currentPositionLagLng.longitude);

                Toast distanceBtCurAToast = Toast.makeText(getApplicationContext(),"distanceBtCurA: " + distanceBtCurA,Toast.LENGTH_SHORT);
                distanceBtCurAToast.show();
                //see if the distance is more than a mile
                if(distanceBtCurA >= searchDistance){
                    //do nothing
                }else{
                    GridView gridView;
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    CustomGridViewAdapter adapter = new CustomGridViewAdapter(MainActivity.this, reportDialog, imageID);
                    gridView=new GridView(MainActivity.this);
                    gridView.setAdapter(adapter);
                    builder.setView(gridView);
                    builder.setTitle("Reports");
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        /* Exits the dialog */
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing besides exiting dialog
                        }
                    });
                    final AlertDialog disDialog = builder.show();
                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            Toast.makeText(MainActivity.this, "You Clicked at " + reportDialog[+position], Toast.LENGTH_SHORT).show();
                            if(reportDialog[+position].equals("Animals")){
                                startAnimalDialog(a);
                                disDialog.dismiss();
                            }else if(reportDialog[+position].equals("Road Obstacles")){
                                startRoadObstaclesDialog(a);
                                disDialog.dismiss();
                            }else if(reportDialog[+position].equals("Police")){
                                startPoliceDialog(a);
                                disDialog.dismiss();
                            }
                        }
                    });


                }


            }
        });
    }

    /*
    Takes coordinates a and b and calculates the disntance between them
    Slightly modified from:
    http://www.codecodex.com/wiki/Calculate_Distance_Between_Two_Points_on_a_Globe
    */
    public float distanceAtoB(float lat_a, float lng_a, float lat_b, float lng_b )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;


        return new Float(distance).floatValue();


    }

    /*
    Method run when animal dialog is chosen
     */
    private void startAnimalDialog(final LatLng a){
        GridView gridView;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CustomGridViewAdapter adapter = new CustomGridViewAdapter(MainActivity.this, animalDialog, animalImageID);
        gridView=new GridView(this);
        gridView.setAdapter(adapter);
        builder.setView(gridView);
        builder.setTitle("Animals");
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
						/* Exits the dialog */

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing besides exiting dialog
                    }
                });
        final AlertDialog disDialog = builder.show();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MarkerOptions marker = new MarkerOptions().position(a).title("Animals");
                for(int i = 0; i < eventsPlotted.length;i++){
                    if(animalDialog[+position].equals(eventsPlotted[i])){
                        iconOfOnLongClick = eventsPlotted[i].toString();
                        String mDrawName = eventsPlotted[i].toString().toLowerCase();
                        mDrawName = mDrawName.replaceAll("\\s","");
                        int resId = getResources().getIdentifier(mDrawName , "mipmap", getPackageName());
                        marker.icon(BitmapDescriptorFactory.fromResource(resId));
                    }
                }
                ParseGeoPoint point = new ParseGeoPoint(marker.getPosition().latitude,marker.getPosition().longitude);
                userObject.put("location", point);
                userObject.put("group", "Animal");
                userObject.put("icon", iconOfOnLongClick);
                setInfo(marker);
                disDialog.dismiss();
            }
        });

    }

    /*
    Method run when road obstacle dialog is chosen
     */
    private void startRoadObstaclesDialog(final LatLng a){
        GridView gridView;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CustomGridViewAdapter adapter = new CustomGridViewAdapter(MainActivity.this, roadObstacleDialog, roadObstacleImageID);
        gridView=new GridView(this);
        gridView.setAdapter(adapter);
        builder.setView(gridView);
        builder.setTitle("Road Obstacles");
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
						/* Exits the dialog */

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing besides exiting dialog
                    }
                });
        final AlertDialog disDialog = builder.show();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MarkerOptions marker = new MarkerOptions().position(a).title("Road Obstacles");
                for (int i = 0; i < eventsPlotted.length; i++) {
                    if (roadObstacleDialog[+position].equals(eventsPlotted[i])) {
                        iconOfOnLongClick = eventsPlotted[i].toString();
                        String mDrawName = eventsPlotted[i].toString().toLowerCase();
                        mDrawName = mDrawName.replaceAll("\\s", "");
                        int resId = getResources().getIdentifier(mDrawName, "mipmap", getPackageName());
                        marker.icon(BitmapDescriptorFactory.fromResource(resId));
                    }
                }
                ParseGeoPoint point = new ParseGeoPoint(marker.getPosition().latitude, marker.getPosition().longitude);
                userObject.put("location", point);
                userObject.put("group", "Road Obstacle");
                userObject.put("icon", iconOfOnLongClick);
                setInfo(marker);
                disDialog.dismiss();
            }
        });
    }

    /*
    Method run when police dialog is chosen
     */
    private void startPoliceDialog(final LatLng a){
        GridView gridView;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CustomGridViewAdapter adapter = new CustomGridViewAdapter(MainActivity.this, policeDialog, policeImageID);
        gridView=new GridView(this);
        gridView.setAdapter(adapter);
        builder.setView(gridView);
        builder.setTitle("Police");
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
						/* Exits the dialog */

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing besides exiting dialog
                    }
                });
        final AlertDialog disDialog = builder.show();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MarkerOptions marker = new MarkerOptions().position(a).title("Police");
                for (int i = 0; i < eventsPlotted.length; i++) {
                    if (policeDialog[+position].equals(eventsPlotted[i])) {
                        iconOfOnLongClick = eventsPlotted[i].toString();
                        String mDrawName = eventsPlotted[i].toString().toLowerCase();
                        mDrawName = mDrawName.replaceAll("\\s", "");
                        int resId = getResources().getIdentifier(mDrawName, "mipmap", getPackageName());
                        marker.icon(BitmapDescriptorFactory.fromResource(resId));
                    }
                }
                ParseGeoPoint point = new ParseGeoPoint(marker.getPosition().latitude, marker.getPosition().longitude);
                userObject.put("location", point);
                userObject.put("group", "Police");
                userObject.put("icon", iconOfOnLongClick);
                setInfo(marker);
                disDialog.dismiss();
            }
        });
    }

    /*
    Sets info of the marker just added to the map
    */
    public void setInfo(final MarkerOptions marker){
        final AlertDialog.Builder settingInfo = new AlertDialog.Builder(MainActivity.this);
        //settingInfo.setTitle("Any notes to add?");
        settingInfo.setTitle("Extra Information");
        // Set an EditText view to get user input

        View view = getLayoutInflater().inflate(R.layout.info_dialog, null);

        settingInfo.setView(view);

        final EditText editnotes = (EditText)view.findViewById(R.id.infoDialogEditNote);
        final EditText editminutes = (EditText)view.findViewById(R.id.infoDialogEditMinutes);

        settingInfo.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
//                String value = input.getText().toString();
                String notes = editnotes.getText().toString();
                String minutes = editminutes.getText().toString();
                dialog_error = false;
                if (minutes.equals("")){
                    Toast.makeText(MainActivity.this, "Please input a valid number for minutes to expire", Toast.LENGTH_LONG).show();
                    dialog_error = true;
                    return;
                }

                if(notes.equals("")){
                    userObject.put("notes", iconOfOnLongClick);

                }else {
                    userObject.put("notes", marker.getTitle());

                }

                int minutesInt = Integer.valueOf(minutes);

                if (minutesInt == 0) {
                    Toast.makeText(MainActivity.this, "Please input a valid number for minutes to expire", Toast.LENGTH_LONG).show();
                    dialog_error = true;
                    return;
                } else {
                    userObject.put("minutes", minutesInt);
                    userObject.put("username", userName);
                    userObject.put("userid", userID);
                    userObject.put("votes", 1);
                    userObject.add("voterslist", userID); //or put

                }

                userObject.saveInBackground(new SaveCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            // Saved successfully.
                            Log.d("HHHHHHH", "Marker Saved!");
                            marker.title(userObject.getObjectId());
                            Marker tempMarker = mMap.addMarker(marker);
                            visibleMarkers.put(userObject.getObjectId(), tempMarker);
                            userObject = new ParseObject("PlaceObject");
                        } else {
                            // The save failed.
                            Log.d("HHHHHHH", "Error updating user data: " + e);
                        }
                    }
                });



                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_IMPLICIT_ONLY);

            }
        });
        settingInfo.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            /* Exits dialog */
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // close keyboard
                marker.title(iconOfOnLongClick);
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_IMPLICIT_ONLY);
            }


        });
        settingInfo.show();

        settingInfo.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                //If the error flag was set to true then show the dialog again
                if (dialog_error == true) {
                    Log.d("MainActivity", "dialog error eh true!");
                    settingInfo.show();
                } else {
                    return;
                }

            }
        });
    }


    //////////////////////////////
    //Section D
    //////////////////////////////
    /*
    This method gathers the locations from
    */
    private void DoParseQuery(){
        ParseGeoPoint userLocation = new ParseGeoPoint(currentLatitude,currentLongitude);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("PlaceObject");
        query.whereWithinMiles("location", userLocation, searchDistance);
        drawCircle();
        query.setLimit(20);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    objectsWereRetrievedSuccessfully(objects);
                } else {
                    Toast.makeText(getBaseContext(), "objectRetrievalFailed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /* GPS Part*/
        if (isGPSMode) {
            if (navigator.isGPSReady()) {
                //Check if we are close to the next destination
                LatLng nextDest = navigator.getNextDestination();
                float distance = distanceAtoB((float) currentPositionLagLng.latitude, (float) currentPositionLagLng.longitude, (float) nextDest.latitude, (float) nextDest.longitude);
                distance *= 1609.34;
                Log.d("MainActivity", "Distance: " + String.valueOf(distance));
                if (distance < GPSNavigator.MIN_DISTANCE) {
                    //Go to next instruction!
                    if (navigator.isFinalDestination()) {
                        //Destination Reached!
                        isGPSMode = false;
                        idOfMarkerGps = "";
                        Toast.makeText(MainActivity.this, "You reached your destination!", Toast.LENGTH_LONG).show();
                        navigator.stopGPS();
                    } else {
                        //Change to next destination
                        navigator.advanceDestination();
                        directions.setText("Next step: " + Html.fromHtml(navigator.getCurrentDirection()));
                        distanceDuration.setText("Distance: " + navigator.getCurrentDistance() + ", Duration: " + navigator.getCurrentDurations());
                        //ver de por polyline
                        mPolyline.remove();
                        mPolyline = mMap.addPolyline(navigator.getNextPolyline());

                    }
                }
            }
        }
    }
    /*
    Only called if there are objects from the Parse database.
    Will plot the markers in their respective locations, with their respective icons
     */
    private void objectsWereRetrievedSuccessfully(List<ParseObject> objects){
        Log.d("MainActivity", "Size: " + objects.size());
        // If there were markers on screen, remove them
        if (!visibleMarkers.isEmpty()) {
            for (String key : visibleMarkers.keySet()) {
                visibleMarkers.get(key).remove();
            }
            visibleMarkers.clear();
        }
        // If the marker dialog is currently opened, we want to see if the marker will still be shown after this update
        // If yes, keep the dialog open. If no, close the dialog and warn the user
        if (isDialogOpen) {
            boolean markerFound = false;
            // see if marker is on the list. If not, marker should not be showed anymore
            for (ParseObject obj : objects) {
                if (obj.getObjectId().equals(idOfMarkerBeingShown)) {
                    updateMarkerDialog(markerDialog, obj);
                    markerFound = true;
                    break;
                }
            }

            if (!markerFound) {
                markerDialog.dismiss();
                isDialogOpen = false;
                Toast.makeText(getBaseContext(), "Marker was deleted!", Toast.LENGTH_LONG).show();
            }
        }


        for(int i = 0; i < objects.size(); i++){
            //Let us check if ther marker should be deleted first!
            Date creationDate = objects.get(i).getCreatedAt();
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(creationDate);

            int votes = objects.get(i).getInt("votes");
            if (votes < -4) {
                objects.get(i).deleteEventually();
                continue;
            }

            long timeOfCreation = creationDate.getTime();
            long currentTime = System.currentTimeMillis();
            long timeOfExpiration = timeOfCreation + objects.get(i).getInt("minutes")*60000;
            long timeOfExclusion = timeOfExpiration + 10 * 60000; //add 10 minutes after expiration
            if (currentTime >= timeOfExclusion && !objects.get(i).getObjectId().equals(idOfMarkerGps)) {
                //delete if it is past the time and if it is not the marker being traveled to
                objects.get(i).deleteEventually();
                continue;
            }


            MarkerOptions nearObject = new MarkerOptions().position(new LatLng(objects.get(i).getParseGeoPoint("location").getLatitude(),
                    objects.get(i).getParseGeoPoint("location").getLongitude()));
            String icon = objects.get(i).getString("icon");
            for(int j = 0; j < eventsPlotted.length;j++){
                if(icon.equals(eventsPlotted[j])){
                    String mDrawName = eventsPlotted[j].toString().toLowerCase();
                    mDrawName = mDrawName.replaceAll("\\s","");
                    int resId = getResources().getIdentifier(mDrawName , "mipmap", getPackageName());
                    nearObject.icon(BitmapDescriptorFactory.fromResource(resId));
                }
            }
            String title = objects.get(i).getObjectId();//MODIFIED!
            nearObject.title(title);
            if(animalReports){
                for(int k = 0; k < animalDialog.length; k++){
                    if(icon.equals(animalDialog[k])){
                        Marker temp = mMap.addMarker(nearObject);
                        //Put marker on hashmap for later access
                        visibleMarkers.put(objects.get(i).getObjectId(), temp);
                    }
                }
            }
            if(roadObsReports){
                for(int k = 0; k < roadObstacleDialog.length; k++){
                    if(icon.equals(roadObstacleDialog[k])){
                        Marker temp = mMap.addMarker(nearObject);
                        visibleMarkers.put(objects.get(i).getObjectId(), temp);
                    }
                }
            }
            if(policeReports){
                for(int k = 0; k < policeDialog.length; k++){
                    if(icon.equals(policeDialog[k])){
                        Marker temp = mMap.addMarker(nearObject);
                        visibleMarkers.put(objects.get(i).getObjectId(), temp);
                    }
                }
            }
        }
    }

    //Draws the circle around the users position. Either 1,2,5, or 10 miles
    private void drawCircle(){
        int baseColor = Color.DKGRAY;
        if (!circleOnMap){
            Circle circle = mMap.addCircle(
                    new CircleOptions().center(currentPositionLagLng).radius(searchDistance * METER_CONVERSION).strokeColor(Color.RED).fillColor(Color.argb(50, Color.red(baseColor), Color.green(baseColor),
                            Color.blue(baseColor))));
            circleOnMap = true;
        }else{
            //do nothing
        }
    }


    //////////////////////////////
    //Section E
    //////////////////////////////
    /*
     *  Geocoder
     *  Followed tutorial on:
     * http://wptrafficanalyzer.in/blog/android-geocoding-showing-user-input-location-on-google-map-android-api-v2/
     */
    // An AsyncTask class for accessing the GeoCoding Web Service
    //@TargetApi(Build.VERSION_CODES.CUPCAKE)
    private class GeocodingTask extends AsyncTask<String, Void, List<Address>>{

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addressList = null;

            try {
                addressList = geocoder.getFromLocationName(locationName[0], 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addressList;
        }

        @Override
        protected void onPostExecute(List<Address> addressList) {
            LatLng markerLatLng;
            MarkerOptions addressMarkerOptions;
            if(addressList==null || addressList.size()==0){
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }

            for(int i=0;i<addressList.size();i++){

                Address address = (Address) addressList.get(i);

                markerLatLng = new LatLng(address.getLatitude(), address.getLongitude());

                String addressText = enteredAddress.toUpperCase();

                addressMarkerOptions = new MarkerOptions();
                addressMarkerOptions.position(markerLatLng);
                addressMarkerOptions.title(addressText);

                mMap.addMarker(addressMarkerOptions);

                if(i==0)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(markerLatLng));
            }
        }
    }

    /*
    Followed tutorial on
    http://wptrafficanalyzer.in/blog/driving-route-from-my-location-to-destination-in-google-maps-android-api-v2/
     */
    private String makeDirectionsURL(LatLng origin, LatLng dest){

        // Origin of route
        String sOrigin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String sDest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the url to the web service
        String dirURL = "https://maps.googleapis.com/maps/api/directions/"+"json"+"?"+sOrigin+"&"+sDest+"&"+sensor;

        return dirURL;
    }


    /** Followed Tutorial on
     * http://wptrafficanalyzer.in/blog/driving-distance-and-travel-time-duration-between-two-locations-in-google-map-android-api-v2/ */
    private class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... url) {
            String response = "";
            try{
                response = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParsingTask parsingTask = new ParsingTask();
            parsingTask.execute(result);
        }
    }

    /** Followed tutorial on
     * http://wptrafficanalyzer.in/blog/driving-distance-and-travel-time-duration-between-two-locations-in-google-map-android-api-v2/ */
    private String downloadUrl(String stringURL) throws IOException{
        String response = "";
        InputStream content = null;
        HttpURLConnection htURLCon = null;
        try{
            URL url = new URL(stringURL);
            htURLCon = (HttpURLConnection) url.openConnection();
            htURLCon.connect();
            content = htURLCon.getInputStream();
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(content));
            String s = "";
            while( ( s = buffRead.readLine())  != null){
                response += s;
            }
            buffRead.close();
        }catch(Exception e){
            Log.d("Exception downloadURL", e.toString());
        }finally{
            content.close();
            htURLCon.disconnect();
        }
        return response;
    }


    /**
     * Followed tutorial on http://wptrafficanalyzer.in/blog/driving-distance-and-travel-time-duration-between-two-locations-in-google-map-android-api-v2/*/
    private class ParsingTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routeOptions = null;

            try{
                jsonObject = new JSONObject(jsonData[0]);
                MyJSONParser myJSONParser = new MyJSONParser();

                routeOptions = myJSONParser.parse(jsonObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routeOptions;
        }


        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> latLngPoints = null;
            PolylineOptions lineOptions = null;
            String distance = "";
            String duration = "";

            if(result.size()<1){
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }
            for(int i=0;i<result.size();i++){
                latLngPoints = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    if(j==0){
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){
                        duration = (String)point.get("duration");
                        continue;
                    }
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    latLngPoints.add(position);
                }
                lineOptions.addAll(latLngPoints);
                lineOptions.width(2);
                lineOptions.color(Color.rgb(30, 60, 90));
            }
            distanceDuration.setText("Distance: "+distance + ", Duration: "+duration);
            // Drawing polyline in the Google Map
            mPolyline = mMap.addPolyline(lineOptions);
        }
    }

    //////////////////////////////
    //Section F
    //////////////////////////////

    //Method provided by Google
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapview))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     *
     * Method provided by Google
     */
    private void setUpMap() {
        // Enable MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true);

        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Get Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);
        //Toast.makeText(getBaseContext(), "myLoc: " + myLocation.toString(), Toast.LENGTH_SHORT).show();
        currentPositionLagLng = new LatLng(currentLatitude, currentLongitude);

        // set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if(myLocation != null){//not on emulator or GPS is not working
            // Get latitude of the current location
            double latitude = myLocation.getLatitude();
            currentLatitude = latitude;

            // Get longitude of the current location
            double longitude = myLocation.getLongitude();
            currentLongitude = longitude;

            // Create a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);
            currentPositionLagLng = new LatLng(currentLatitude, currentLongitude);

            // Show the current location in Google Map
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            // Zoom in the Google Map
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            drawCircle();
        }

    }
    // Fetches all places from GooglePlaces AutoComplete Web Service
    private class PlacesTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=AIzaSyCh0IztU40zYvay3rZzbITFFfIPVfQN3gM";

            String input="";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

            // place type to be searched
            String types = "types=geocode";

            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = input+"&"+types+"&"+sensor+"&"+key;

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

            try{
                // Fetching the data from we service
                Log.d("MainActivity", "EXECUTANDO DOWNLOAD");
                data = downloadUrl(url);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Creating ParserTask
            parserTask = new ParserTask();

            // Starting Parsing the JSON string returned by Web Service
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJSONParser placeJsonParser = new PlaceJSONParser();


            try{
                jObject = new JSONObject(jsonData[0]);

                // Getting the parsed data as a List construct
                places = placeJsonParser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            String[] from = new String[] { "description"};
            int[] to = new int[] { android.R.id.text1 };

            // Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

            // Setting the adapter
            autoCompleteTextView.setAdapter(adapter);
        }
    }

    private void updateMarkerDialog (AlertDialog dialog, ParseObject obj) {
        TextView minutesText = (TextView)dialog.findViewById(R.id.markerInfoDialogMinutesExpireTextView);

        Date creationDate = obj.getCreatedAt();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(creationDate);
        long timeOfCreation = creationDate.getTime();
        long currentTime = System.currentTimeMillis();
        long timeOfExpiration = timeOfCreation + obj.getInt("minutes")*60000;
        long difference = timeOfExpiration - currentTime;
        long minutes = difference / 60000;

        if (currentTime > timeOfExpiration) {
            minutesText.setText("Minutes until expires: Expired");
        } else {
            minutesText.setText("Minutes until expires: " + String.valueOf(minutes));
        }

    }

    private void startGPSNavigation(Marker marker, String mode) {
        isGPSMode = true;
        idOfMarkerGps = marker.getTitle();
        LatLng start = currentPositionLagLng;
        LatLng end = marker.getPosition();
        navigator = new GPSNavigator(start, end, mode, MainActivity.this);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem itm = menu.findItem(R.id.gps_off);

        if (!isGPSMode) {
            itm.setVisible(false);
        } else {
            itm.setVisible(true);
        }
        return true;
    }
}