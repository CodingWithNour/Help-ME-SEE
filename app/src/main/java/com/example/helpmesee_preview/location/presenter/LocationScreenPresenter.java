package com.example.helpmesee_preview.location.presenter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;

import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.helpmesee_preview.R;
import com.example.helpmesee_preview.app_logic.AppFeaturesEnum;
import com.example.helpmesee_preview.app_logic.Constants;
import com.example.helpmesee_preview.location.model.Contact;
import com.example.helpmesee_preview.location.model.LocationModelManager;
import com.example.helpmesee_preview.location.view.LocationScreenView;
import com.example.helpmesee_preview.util.ModelsFactory;
import com.example.helpmesee_preview.util.ViewsFactory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationScreenPresenter extends LocationScreenListener {

  private LocationScreenView rootView;
  private LocationModelManager locationModelManager;
  private final static int REQUEST_CODE = 100;

  TextView locationTV;
  String add;
  //logic
  private static final String LOCATION_SCREEN_TAG = "LocationScreen";
  private FusedLocationProviderClient fusedLocationProviderClient;
  private static final int PICK_CONTACT_REQUEST = 90;
  private static final int PERMISSIONS_REQUEST_CODE = 91;
  private double currentLocationlong,currentLocationlat;
  private boolean isThisActivityInForeground;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    askForNeededPermissions();
    //null is passed beacause the layout is the parent of all views, i.e. container=none

    rootView = (LocationScreenView) ViewsFactory.createView(this, AppFeaturesEnum.LOCATION);
    rootView.setListener(this);
    setContentView(rootView.getAndroidLayoutView());
    locationModelManager = (LocationModelManager) ModelsFactory.createModel(this, AppFeaturesEnum.LOCATION);
    locationTV=findViewById(R.id.locationTV);
  }

  private void askForNeededPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        && checkSelfPermission(Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(
          new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS},
          PERMISSIONS_REQUEST_CODE);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    isThisActivityInForeground = true;
    getLastLocation();
  }

  @Override
  protected void onPause() {
    super.onPause();
    isThisActivityInForeground = false;
  }

  @Override
  public void execute(String detectedText) {
    if (detectedText.equalsIgnoreCase("SEND")) {
      textToSpeech.speak("Sending messages to friends ", TextToSpeech.QUEUE_ADD, null);
      locationModelManager.getContacts();
      Toast.makeText(this, "Sendingggggggg", Toast.LENGTH_SHORT).show();
      sendMessages();
    }else{
      textToSpeech.speak("Cannot process command: " + detectedText, TextToSpeech.QUEUE_ADD, null);
    }
  }

  @Override
  public void sendMessages() {
    StringBuilder messageBody = new StringBuilder("Hello,\n");
    messageBody.append("I'm here: ");
    messageBody.append("(").append(currentLocationlat).append(",")
        .append(currentLocationlong).append(").\n");
    messageBody.append(" Can you come and pick me up please?");
    List<Contact> contacts = locationModelManager.getContacts();
    if (contacts.size() == 0) {
      textToSpeech.speak("Add friends contact info first!", TextToSpeech.QUEUE_ADD, null);
    } else {
      Log.i(LOCATION_SCREEN_TAG, "Sending messages to following contacts: ");
    }

    String message = messageBody.toString();
    for (Contact c : contacts) {
      Log.i(LOCATION_SCREEN_TAG, c.toString());
       sendMessage(c.phoneNumber, message);
    }

  }


/////////////////////////////////////////////////////////
  private void getLastLocation(){

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){


      fusedLocationProviderClient.getLastLocation()
              .addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                  if (location != null){
                    Geocoder geocoder = new Geocoder(LocationScreenPresenter.this, Locale.getDefault());
                    List<Address> addresses=null;
                    currentLocationlat=location.getLatitude();
                    currentLocationlong=location.getLongitude();
                    try {
                      addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                      add=addresses.get(0).getAddressLine(0);
                      if ((!textToSpeech.isSpeaking()) && (isThisActivityInForeground)) {
                        textToSpeech.speak(add, TextToSpeech.QUEUE_ADD, null);
                      }
                      locationTV.setText("Your Locations is  "+addresses.get(0).getAddressLine(0));
                      // txt_view.setText("City: "+addresses.get(0).getLocality());
                      //country.setText("Country: "+addresses.get(0).getCountryName());*/
                    } catch (IOException e) {
                      e.printStackTrace();
                    }


                  }

                }
              });


    }else {

      askPermission();


    }


  }

  private void askPermission() {

    ActivityCompat.requestPermissions(LocationScreenPresenter.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);


  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {

    if (requestCode == REQUEST_CODE){

      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){


        getLastLocation();

      }else {


        Toast.makeText(LocationScreenPresenter.this,"Please provide the required permission",Toast.LENGTH_SHORT).show();

      }



    }

    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }


/////////////////////////////////////////////////////////////////////////////////////
  /*
  private void detectUserCurrentAddress() {
    try {
      Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
       locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
        @Override
        public void onComplete(@NonNull Task<Location> task) {
          if (task.isSuccessful()) {
            currentLocation = task.getResult();

            Address currentAddress = getAdressFromLocation(currentLocation,
                LocationScreenPresenter.this);

            String prettyPrintedAddress = prettyPrintAddress(currentAddress);
            Toast.makeText(LocationScreenPresenter.this, prettyPrintedAddress, Toast.LENGTH_SHORT).show();

            if ((!textToSpeech.isSpeaking()) && (isThisActivityInForeground)) {
              textToSpeech.speak(prettyPrintedAddress, TextToSpeech.QUEUE_ADD, null);
            }

            Log.i(LOCATION_SCREEN_TAG, "Pretty: " + prettyPrintedAddress);
            Toast.makeText(LocationScreenPresenter.this, "B", Toast.LENGTH_SHORT).show();

               rootView.displayUserCurrentLocation(prettyPrintedAddress);
            Toast.makeText(LocationScreenPresenter.this, "C", Toast.LENGTH_SHORT).show();

          } else {
            Toast.makeText(LocationScreenPresenter.this, "Not Found", Toast.LENGTH_SHORT).show();
            Log.e(LOCATION_SCREEN_TAG, "Exception: %s", task.getException());
          }
        }
      });
    } catch (SecurityException e) {
      Log.e(LOCATION_SCREEN_TAG, e.getMessage());
    }
  }
*/
  /**
   * Returns a string where each element from address is separted by comma
   *//*
  public static String prettyPrintAddress(Address currentAddress) {
    StringBuilder buffer = new StringBuilder();

    int lastAddressLineIndex = currentAddress.getMaxAddressLineIndex();
    for (int i = 0; i <= lastAddressLineIndex; i++) {
      buffer.append(currentAddress.getAddressLine(i)).append(",");
    }

    return buffer.toString();
  }
*/
  /**
   *//*
  private Address getAdressFromLocation(Location location, Context context) {
    Geocoder geocoder = new Geocoder(context, Locale.getDefault());

    try {
      List<Address> addresses = geocoder
              .getFromLocation(location.getLatitude(), location.getLongitude(), 1);

      if (addresses.size() > 0) {
        //Log.i(Constants.HMS_INFO, "Detected current address: " + address);
        Toast.makeText(context, ""+addresses, Toast.LENGTH_SHORT).show();
        return addresses.get(0);
      }

    } catch (IOException e) {
      e.printStackTrace();

      Log.d(Constants.HMS_INFO, "Couldn't reverse geocode from location: " + location);
    }

    return null;
  }
*/
  @Override
  public void addContact() {
    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
    startActivityForResult(intent, PICK_CONTACT_REQUEST);
  }

 // @SuppressLint("Range")
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case PICK_CONTACT_REQUEST:
        if (resultCode == Activity.RESULT_OK) {
          Uri contactData = data.getData();
          Cursor cursorContact = getContentResolver().query(contactData, null, null, null, null);
          if (cursorContact.moveToFirst()) {
            String contactName = cursorContact.getString(cursorContact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String contactId = cursorContact.getString(cursorContact.getColumnIndex(ContactsContract.Contacts._ID));
            String hasPhone = cursorContact
                .getString(cursorContact.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            String contactPhoneNumber = null;

            if ("1".equals(hasPhone) || Boolean.parseBoolean(hasPhone)) {
              Cursor cursorPhoneNumbers = this.getContentResolver()
                  .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                      ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null,
                      null);
              while (cursorPhoneNumbers.moveToNext()) {
                contactPhoneNumber = cursorPhoneNumbers.getString(
                    cursorPhoneNumbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
              }
              cursorPhoneNumbers.close();
            }

            if (contactPhoneNumber != null) {
              Log.i(LOCATION_SCREEN_TAG,
                  "Picked contact:" + contactName + "-" + contactPhoneNumber);
              locationModelManager.addContact(contactName, contactPhoneNumber);
            }
          }

          cursorContact.close();
        }
    }
  }
/*
  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions,
      int[] grantResults) {

    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (PERMISSIONS_REQUEST_CODE == requestCode) {
      if (grantResults[0] != PackageManager.PERMISSION_GRANTED) { //ask again
        askForNeededPermissions();
        textToSpeech.speak("The app cannot work without the required permissions!",
                TextToSpeech.QUEUE_ADD, null);
      }
    }
  }
*/
  public void sendMessage(String phoneNo, String msg) {
    try {
      SmsManager smsManager = SmsManager.getDefault();
      smsManager.sendTextMessage(phoneNo, null, msg, null, null);
    } catch (Exception ex) {
      Log.i(LOCATION_SCREEN_TAG,
          "Could not send help msg to : " + phoneNo + ".\n Reason: " + ex.getMessage());
      ex.printStackTrace();
    }
  }


}
