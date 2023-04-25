package com.example.helpmesee_preview.directions.Presenter;

import static com.google.android.gms.common.util.JsonUtils.escapeString;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.speech.tts.TextToSpeech;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.helpmesee_preview.R;
import com.example.helpmesee_preview.app_logic.AppFeaturesEnum;
import com.example.helpmesee_preview.app_logic.Constants;
import com.example.helpmesee_preview.directions.Model.DirectionsModelListener;
import com.example.helpmesee_preview.directions.Model.DirectionsModelManager;
import com.example.helpmesee_preview.directions.Model.Instruction;
import com.example.helpmesee_preview.directions.Model.PathDto;
import com.example.helpmesee_preview.directions.View.DirectionsScreenListener;
import com.example.helpmesee_preview.directions.View.DirectionsScreenView;
import com.example.helpmesee_preview.util.ModelsFactory;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DirectionsScreenPresenter extends DirectionsScreenListener implements
        OnMapReadyCallback, PathFoundListener, DirectionsModelListener {

  //view
  private DirectionsScreenView rootView;

  DirectionsScreenListener listener;
  //constants
  private static final int DEFAULT_ZOOM = 15;
  private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2000;
  private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
          UPDATE_INTERVAL_IN_MILLISECONDS / 2;

  // Keys for storing activity state.
  private static final String KEY_CAMERA_POSITION = "camera_position";
  private static final String KEY_LOCATION = "location";
  private static final String DIRECTIONS_SCREEN_TAG = "DirectionsScreen";
  private final int REQUEST_CHECK_SETTINGS = 20;


  //interaction with map object
  private LocationRequest locationRequest;
  private GoogleMap googleMap;
  private LocationCallback locationCallback;
  // The entry point to the Fused Location Provider.
  private FusedLocationProviderClient fusedLocationProviderClient;

  //logic
  private Location currentUserLocation;
  private PathProvider pathProvider;
  private boolean requestedLocationUpdates;
  private DirectionsModelManager directionsMM;
  private SensorManager sensorManager;
  private Sensor sensorOrientation;
  private Float currentPhoneBearing;
  private HmsSensorEventListener orientationSensorListener;
  private List<LatLng> currentPathCoordinates;

  private Bundle instanceBundle;
  //predict future location in DELTA_T seconds
  private final static Float DELTA_T = 3f;
  private final static Float PATH_RADIUS = 3.5f;
  private long startTime;
  /**
   * used to avoid useless animation of camera
   */
  private float previousBearing;

  //used for real-time debugging
  private Marker futureLocationMarker;
  private Marker normalLocationMarker;
  private Marker targetLocationMarker;

  private Button speechInputButton,findPathButton;

  private static final String[] INITIAL_PERMS={
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.READ_CONTACTS
  };
  private static final int INITIAL_REQUEST=1337;

  Context context;
  AutoCompleteTextView destActv;
  TextView distanceTV,durationTV;

  private List<PathDto> path ;


  private static final String DIRECTIONS_ROOT_URL = "https://maps.googleapis.com/maps/api/directions/json?";
  private static final String GOOGLE_DIRECTIONS_KEY = "AIzaSyBTXORoX0NFLxBmO2CGQRbEk5cjHUzJOeg";

  private  HashMap<String,String> requestParameters;



  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.directions_screen_layout);
    path = new ArrayList<>();
    speechInputButton=findViewById(R.id.speechButton_directions);
    destActv=findViewById(R.id.destACTV);
    findPathButton=findViewById(R.id.findPathB);
    distanceTV=findViewById(R.id.distanceTV);
    durationTV=findViewById(R.id.durationTV);
    requestParameters = new HashMap<>();
    requestParameters.put(Constants.PATH_ORIGIN,"");
    requestParameters.put(Constants.PATH_DESTINATION,"");
    requestParameters.put(Constants.PATH_MODE,Constants.PATH_WALKING);
    //  rootView = (DirectionsScreenView) ViewsFactory.createView(this, AppFeaturesEnum.DIRECTIONS);
    //  rootView.setScreenListener(this);
    directionsMM = (DirectionsModelManager) ModelsFactory
            .createModel(this, AppFeaturesEnum.DIRECTIONS);
    directionsMM.setModelListener(this);
    if (!canAccessLocation()) {
      requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
    }
    /**
     * A Fragment is a piece of an application's user interface or behavior that can be placed in an Activity <br>
     *   Note: isn't this a breaking of the MVP architecture?
     *   https://developer.android.com/reference/android/app/Fragment
     *   1 dirtiness point
     * Note: I could move the part where I get the fragment into the view but then I will end up with
     *       a dependency on the activity (i.e. 1 dirtiness point)
     *       https://www.techyourchance.com/activities-android/
     */
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map);
    if (mapFragment != null) {
      mapFragment.getMapAsync(this);
      Toast.makeText(this, "hhhh", Toast.LENGTH_SHORT).show();
    }
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

    createLocationCallback();
    createLocationRequest();
    buildLocationRequestSettings();

    initialize();
    Toast.makeText(this, "Create", Toast.LENGTH_SHORT).show();

    speechInputButton.setOnClickListener(v->{
      startRecording();
    });

    findPathButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (isDesinationSet()) {
          String destination = destActv.getText().toString();
          findPath(destination);
        }
      }
    });

    String[] testLocations = this.getResources().getStringArray(R.array.test_locations);
    ArrayAdapter<String> adapter =
            new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, testLocations);
    destActv.setAdapter(adapter);

    destActv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        hideSoftKeyboard();

      }

    });

  }
  private boolean canAccessLocation() {
    return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
  }

  private boolean hasPermission(String perm) {
    return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(perm));
  }



  private void createLocationCallback() {
    //algorithm : compute bearing of direction between the two closest points to user location
    // compare user's direction of moving bearing with the bearing between two stopPoints
    // notify user to go left or right

    locationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult lRs) {
        if (lRs == null) {
          return;
        }
        for (Location newLocation : lRs.getLocations()) {

          double latitude = newLocation.getLatitude();
          double longitude = newLocation.getLongitude();
          Toast.makeText(listener, latitude + "", Toast.LENGTH_SHORT).show();
          float bearing = newLocation.getBearing();
          // Log.i(DIRECTIONS_SCREEN_TAG,
          //     /*time + " (" + lat + ", " + lng + ")" +*/ " Bearing: " + bearing + "\n Accuracy: " + accuracy);
          directionsMM.fetchInstruction(newLocation);
          //Toast.makeText(DirectionsScreenPresenter.this, "Bearing: " + newLocation.getSpeed(), Toast.LENGTH_SHORT).show();
          // Log.i(DIRECTIONS_SCREEN_TAG,  + latitude + "," + longitude);
          Toast.makeText(listener, newLocation+"", Toast.LENGTH_SHORT).show();
          //avoid useless calls to method
          if ((bearing != 0) && (bearing != previousBearing)) {
            adjustCamera(bearing, latitude, longitude);
            previousBearing = bearing;
          }

        }
      }

    };
  }


  private void adjustCamera(float bearing, double latitude, double longitude) {
    if (googleMap == null) {
      return;
    }
    CameraPosition camPos = CameraPosition
            .builder(googleMap.getCameraPosition())
            .bearing(bearing)
            .target(new LatLng(latitude, longitude))
            .build();
    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));

  }

  private void initialize() {
    pathProvider = new PathProvider(this);
    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    sensorOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    orientationSensorListener = new HmsSensorEventListener();
  }

  /**
   * Saves the state of the map when the activity is paused.
   */
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    if (googleMap != null) {
      outState.putParcelable(KEY_CAMERA_POSITION, googleMap.getCameraPosition());
      outState.putParcelable(KEY_LOCATION, currentUserLocation);
    }

    if (currentPathCoordinates != null) { //if user has a path already set
      rootView.onSaveViewState(outState);

      String currentPathString = currentPathCoordinates.toString();
      outState.putString(Constants.CURRENT_PATH_STRING, currentPathString);
    }

    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    this.instanceBundle = savedInstanceState;
  }


  private List<LatLng> toListLatLng(String currentPathString) {
    ArrayList<LatLng> coordinates = new ArrayList<>();
    int size = currentPathString.length();

    currentPathString = currentPathString.substring(1, size - 2);
    //creates an array of  with elements of form: latidude,longitude
    String[] latLngArr = currentPathString.split("\\)?,?\\s?lat/lng: \\(");

    int length = latLngArr.length;
    for (int i = 1; i < length; i++) {
      String[] latLng = latLngArr[i].split(",");
      Double latitude = Double.valueOf(latLng[0]);
      Double longitude = Double.valueOf(latLng[1]);

      Toast.makeText(this, latitude+"", Toast.LENGTH_SHORT).show();
      coordinates.add(new LatLng(latitude, longitude));
    }

    return coordinates;
  }


  @Override
  /**
   * Permision check is done when the app is launched
   */
  public void onMapReady(GoogleMap googleMap) {
    this.googleMap = googleMap;

    googleMap.setMyLocationEnabled(true);


    //my location button is bad for accesibility as described by Scanner
    googleMap.getUiSettings().setMyLocationButtonEnabled(false);

    getDeviceLocation();

    //handle a configuration change like screen resize
    if ((instanceBundle != null) &&
        (instanceBundle.getSerializable(Constants.CURRENT_PATH_STRING) != null)) {
      String currentPathString = instanceBundle.getString(Constants.CURRENT_PATH_STRING);
      if (currentPathString != null) {
        currentPathCoordinates = toListLatLng(currentPathString);
        Toast.makeText(this, currentPathString, Toast.LENGTH_SHORT).show();
        drawPathOnMap(currentPathCoordinates);
      }
      directionsMM.initialize(currentPathCoordinates, PATH_RADIUS, DELTA_T, currentPhoneBearing);

      requestedLocationUpdates = true;

      onRestoreInstanceState(instanceBundle);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (requestedLocationUpdates) {
      startLocationUpdates();
    }
  }

  protected void onPause() {
    super.onPause();
    if (requestedLocationUpdates) {
      stopLocationUpdates();
    }
  }

  @SuppressLint("MissingPermission")
  private void startLocationUpdates() {
    fusedLocationProviderClient.requestLocationUpdates(locationRequest,
        locationCallback,
        Looper.myLooper()/* Looper */);
  }

  private void stopLocationUpdates() {
    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
  }

  /**
   * Location updates are received each second
   */
  private void createLocationRequest() {
    locationRequest = new LocationRequest();
    locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS); // 2000 ms
    locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS); // 1000 ms
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
  }

  private void buildLocationRequestSettings() {
    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest);
    SettingsClient client = LocationServices.getSettingsClient(this);
    Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
    task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
      @Override
      public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
        Log.i(DIRECTIONS_SCREEN_TAG, "Location settings are ok!");
      }
    });

    task.addOnFailureListener(this, new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        if (e instanceof ResolvableApiException) {
          try {
            ResolvableApiException resolvable = (ResolvableApiException) e;
            resolvable.startResolutionForResult(DirectionsScreenPresenter.this,
                REQUEST_CHECK_SETTINGS);
          } catch (IntentSender.SendIntentException sendEx) {
          }
        }
      }
    });
  }


  /**
   * Gets the current location of the device and moves camera on user's location.
   */
  private void getDeviceLocation() {
    try {
      Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
      locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
        @Override
        public void onComplete(@NonNull Task<Location> task) {
          if (task.isSuccessful()) {
            // Set the map's camera position to the current location of the device.
            currentUserLocation = task.getResult();
            final double latitude = currentUserLocation.getLatitude();
            final double longitude = currentUserLocation.getLongitude();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(latitude,
                    longitude), DEFAULT_ZOOM));

            Log.i(DIRECTIONS_SCREEN_TAG,
                "User location:here (" + latitude + ", " + longitude + ")");


          } else {
            Log.e(DIRECTIONS_SCREEN_TAG, "Exception: %s", task.getException());
          }
        }
      });
    } catch (SecurityException e) {
      Log.e(DIRECTIONS_SCREEN_TAG, e.getMessage());
    }
  }
  private URL createRequestUrl() throws UnsupportedEncodingException, MalformedURLException {
    //SB not synchronized (I don't think is necessary)
    StringBuilder requestUrlSB = new StringBuilder(
    );

    requestUrlSB.append(DIRECTIONS_ROOT_URL);

    for (Map.Entry<String, String> entry : requestParameters.entrySet()) {
      String paramName = entry.getKey();
      String value = escapeString(entry.getValue());

      String paramNameEncoded = URLEncoder.encode(paramName, "utf-8");
      String valueEncoded = URLEncoder.encode(value, "utf-8");
      String option = paramNameEncoded + "=" + valueEncoded + "&";

      requestUrlSB.append(option);
    }

    requestUrlSB.append("key=" + GOOGLE_DIRECTIONS_KEY);


    return new URL(requestUrlSB.toString());
  }

  public void setDestinationS(String destination){
    requestParameters.put(Constants.PATH_DESTINATION, destination);
  }


  public void start(){
    URL requestUrl = null;

    try {
      requestUrl = createRequestUrl();

      //A task can be executed only once
      new GetPathTasks().execute(requestUrl);
    } catch (UnsupportedEncodingException e) {
      Log.e(Constants.HMS_INFO, "Could not create Url, Unsupported encoding: UTF-8" );
      e.printStackTrace();
    } catch (MalformedURLException e) {
      Log.e(Constants.HMS_INFO, "Could not create Url, the format of url is incorrect" );
      e.printStackTrace();
    }

  }

  @Override
  public void findPath(final String destination) {
    setUserCurrentBearing();

    //get the path
    //send message to view to draw it
    //update device location
    try {
      Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
      locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
        @Override
        public void onComplete(@NonNull Task<Location> task) {
          if (task.isSuccessful()) {
            // Set the map's camera position to the current location of the device.
            currentUserLocation = task.getResult();
            final double latitude = currentUserLocation.getLatitude();
            final double longitude = currentUserLocation.getLongitude();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(latitude,
                    longitude), DEFAULT_ZOOM));

            Log.i(DIRECTIONS_SCREEN_TAG,
                "User location before computing the path: (" + latitude + ", " + longitude + ")");

            setDestinationS(destination);

            final String originLatLng =
                latitude + "," + longitude;
            setOrigin(originLatLng);

            start();

          } else {
            Log.e(DIRECTIONS_SCREEN_TAG, "Exception: %s", task.getException());
          }
        }
      });
    } catch (SecurityException e) {
      Log.e(DIRECTIONS_SCREEN_TAG, e.getMessage());
    }

  }
  public void setOrigin(String origin){
    requestParameters.put(Constants.PATH_ORIGIN, origin);
  }


  private void setUserCurrentBearing() {
    sensorManager.registerListener(orientationSensorListener, sensorOrientation,
        SensorManager.SENSOR_DELAY_NORMAL);
    //   It passes 0 as initial bearing, too fast
    currentPhoneBearing = orientationSensorListener.getCurrentBearing();
    sensorManager.unregisterListener(orientationSensorListener);

  }



  @Override
  public void onPathsFound(List<PathDto> pathDtos) {
    //if (pathDtos != null && pathDtos.size() > 0) {
      currentPathCoordinates = pathDtos.get(0).coordinatesLatLng;
      PathDto pathDto = pathDtos.get(0);

       String duration = DirectionsHelper.prettyReadDuration(pathDto.timeM);
       setDuration(duration);

      String distance = pathDto.distanceKM + "km";
      setDistance(distance);

      Toast.makeText(this, currentPathCoordinates+"", Toast.LENGTH_SHORT).show();

      drawPathOnMap(currentPathCoordinates);

      directionsMM.initialize(currentPathCoordinates, PATH_RADIUS, DELTA_T, currentPhoneBearing);

      requestedLocationUpdates = true;
      startLocationUpdates();

    //} else {
      Toast.makeText(this, currentPathCoordinates+"", Toast.LENGTH_SHORT).show();


      Log.e(DIRECTIONS_SCREEN_TAG, "Couldn't find a valid path!");
      // td:  add an id for each request to text textToSpeech and check status
      textToSpeech.speak("Couldn't find a valid path! Please try a more detailed description!",
          TextToSpeech.QUEUE_ADD, null);
      Toast.makeText(this, "Couldn't find a valid path! Please try a more detailed description!",
          Toast.LENGTH_SHORT).show();
    //}


  }

  public void setDistance(String distance) {
    distanceTV.setText(distance);
  }


  public void setDuration(String duration) {
    durationTV.setText(duration);
  }

  private void drawPathOnMap(List<LatLng> coordinatesLatLng) {
//here is a npe, google map
    googleMap.clear();

    PolylineOptions polylineOptions = new PolylineOptions()
        .geodesic(true)
        .color(Color.BLUE)
        .width(10);

    // debug markers used for observing in real-time the predictedFutureLocation, normalPoint and target
   LatLng startPoint = coordinatesLatLng.get(0);
    futureLocationMarker = googleMap
        .addMarker(
            new MarkerOptions().position(new LatLng(startPoint.latitude, startPoint.longitude)));

    targetLocationMarker = googleMap
        .addMarker(
            new MarkerOptions().position(new LatLng(startPoint.latitude, startPoint.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    normalLocationMarker = googleMap
        .addMarker(
          new MarkerOptions().position(new LatLng(startPoint.latitude, startPoint.longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

    for (LatLng p : coordinatesLatLng) {
      polylineOptions.add(p);
    }

    //draw markers representing the straight segments of path on map for debugging purposes
    for (LatLng p : coordinatesLatLng) {
      String locSnippet = p.latitude + ", " + p.longitude;
      MarkerOptions markerOptions = new MarkerOptions().position(p).title("m").snippet(locSnippet)
          .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
      Marker marker = googleMap.addMarker(markerOptions);
      marker.showInfoWindow();
    }

    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
      @Override
      public boolean onMarkerClick(Marker marker) {
        LatLng latLng = marker.getPosition();
        Log.i(DIRECTIONS_SCREEN_TAG, "Clicked marker:" + latLng);
        return false;
      }
    });

    googleMap.addPolyline(polylineOptions);
    final LatLng destinationLatLng = coordinatesLatLng.get(coordinatesLatLng.size() - 1);

    //add finish marker - icon: a flag
    googleMap.addMarker(new MarkerOptions()
        .position(destinationLatLng)
        .icon(BitmapDescriptorFactory
            .fromResource(R.drawable.add_icn)));

    textToSpeech.speak("Path found, you can start!", TextToSpeech.QUEUE_ADD, null);
  }

  @Override
  public void hideSoftKeyboard() {
    InputMethodManager inputManager =
        (InputMethodManager) this.
            getSystemService(Context.INPUT_METHOD_SERVICE);
    inputManager.hideSoftInputFromWindow(
        this.getCurrentFocus().getWindowToken(),
        InputMethodManager.HIDE_NOT_ALWAYS);
  }

  @Override
  public void onInstrFetched(Instruction currentInstrForUser) {
    switch (currentInstrForUser) {
      case STRAIGHT:
        long now = System.currentTimeMillis();
        long elapsed = now - startTime;

          /*
            ensures that instructions "continue straight" are not spammed
          */
        long TIMER_THRESHOLD = 10000;
        if (elapsed >= TIMER_THRESHOLD) {
          //        Log.i(DIRECTIONS_SCREEN_TAG, "elapsedTime: " + elapsed);
          textToSpeech.speak(currentInstrForUser.toString(), TextToSpeech.QUEUE_ADD, null);
          startTime = System.currentTimeMillis();
        }

        break;

      case END:
        //user arrived at destination, clear map
        textToSpeech.speak(currentInstrForUser.toString(), TextToSpeech.QUEUE_ADD, null);
        stopLocationUpdates();
        requestedLocationUpdates = false;
        googleMap.clear();

        break;
      default: // other types of instructions
        textToSpeech.speak(currentInstrForUser.toString(), TextToSpeech.QUEUE_ADD, null);
    }

    // Log.i(Constants.HMS_INFO, currentInstrForUser.toString());

    //Update debuggin markers position on the map
//    double latitude = currentInstrForUser.predictedFutureLocation.getLatitude();
//    double longitude = currentInstrForUser.predictedFutureLocation.getLongitude();
//    futureLocationMarker.setPosition(new LatLng(latitude, longitude));
//    normalLocationMarker.setPosition(
//        new LatLng(currentInstrForUser.normalPoint.getLatitude(), currentInstrForUser.normalPoint.getLongitude()));
//    targetLocationMarker
//        .setPosition(new LatLng(currentInstrForUser.target.getLatitude(), currentInstrForUser.target.getLongitude()));
  }


  @Override
  public void execute(String detectedText) {
    textToSpeech.speak("Destination: " + detectedText, TextToSpeech.QUEUE_ADD, null);
    setDestination(detectedText);
  }
  public void setDestination(String destination) {
    destActv.setText(destination);
  }

  private boolean isDesinationSet() {
    boolean valid = true;

    String destination = destActv.getText().toString();

    if (TextUtils.isEmpty(destination)) {
      destActv.setError("Required!");
      valid = false;
    }

    return valid;
  }
  class GetPathTasks extends AsyncTask<URL, Void, Void>{


    protected Void doInBackground(URL... urls) {
      try {
        URL requestUrl = urls[0];
        InputStream inputStream = requestUrl.openConnection().getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder buffer = new StringBuilder();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
          buffer.append(line).append("\n");
        }
        String jsonRep = buffer.toString();

        parseJson(jsonRep);

      } catch (MalformedURLException e) {
        Log.e(Constants.HMS_INFO, "Directions request url isn't valid!");
        e.printStackTrace();
      } catch (IOException e) {
        Log.e(Constants.HMS_INFO, "Could not request directions!");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      //result
      onPathsFound(path);
    }

    /**
     * @param jsonRep
     * @return
     */
    private void parseJson(String jsonRep) {
      if (jsonRep == null) {
        Log.e(Constants.HMS_INFO, "Could not download the json that contains google directions!");
      }

      try {
        JSONObject rootJsonObj = new JSONObject(jsonRep);
        JSONArray routesJsonArray = rootJsonObj.getJSONArray("routes");

        for (int i = 0; i < routesJsonArray.length(); i++) {
          JSONObject currentRouteJsonO = routesJsonArray.getJSONObject(i);

          JSONArray legs = currentRouteJsonO.getJSONArray("legs");

          JSONObject leg = null;
          JSONObject legStartLocation = null;
          JSONObject legEndLocation = null;
          String originAddress = null;
          LatLng originLatLng = null;
          String destinationAddress = null;
          LatLng destinationLatLng = null;
          int distanceM = 0; //in meters
          int timeS = 0; //in minutes

          for (int j = 0; j < legs.length(); j++) {
            leg = legs.getJSONObject(j); // a portion of the route
            legStartLocation = leg.getJSONObject("start_location");
            legEndLocation = leg.getJSONObject("end_location");
            distanceM += leg.getJSONObject("distance").getInt("value");
            timeS += leg.getJSONObject("duration").getInt("value");

            if (j == 0) {
              originAddress = leg.getString("start_address");
              double lat = legStartLocation.getDouble("lat");
              double lng = legStartLocation.getDouble("lng");

              originLatLng = new LatLng(lat, lng);
            }

            //process other info from this leg
          }

          destinationAddress = leg.getString("end_address");
          double lng = legEndLocation.getDouble("lng");
          double lat = legEndLocation.getDouble("lat");

          destinationLatLng = new LatLng(lat, lng);

          JSONObject o_polyline = currentRouteJsonO.getJSONObject("overview_polyline");
          String encodedLatLangs = o_polyline.getString("points");
          List<LatLng> coordinates = PolyUtil.decode(encodedLatLangs);

          float timeM = formatFloat((float) timeS / 60);
          float distanceKM = formatFloat((float) distanceM / 1000);
          PathDto pathDto = new PathDto(originAddress, destinationAddress, originLatLng, destinationLatLng,
                  distanceKM,
                  timeM, coordinates);
          path.add(pathDto);
        }

      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    private float formatFloat(float f) {
      DecimalFormat decimalFormat = new DecimalFormat("#.0");
      f = Float.valueOf(decimalFormat.format(f));

      return f;
    }

  }

}

class HmsSensorEventListener implements SensorEventListener {

  private float currentBearing;

  @Override
  public void onSensorChanged(SensorEvent sensorEvent) {
    float bearingAngle = sensorEvent.values[0];
    currentBearing = bearingAngle;
    Log.i(Constants.HMS_INFO,
        "Bearing detected using software sensor Orientation: " + bearingAngle);
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int i) {

  }

  public float getCurrentBearing() {
    return currentBearing;
  }
}

