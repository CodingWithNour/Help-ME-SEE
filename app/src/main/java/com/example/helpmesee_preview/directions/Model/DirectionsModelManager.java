package com.example.helpmesee_preview.directions.Model;

import android.location.Location;

import com.example.helpmesee_preview.app_logic.MvpModel;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface DirectionsModelManager extends MvpModel {

  void fetchInstruction(Location newLocation);

  void setModelListener(DirectionsModelListener listener);

  void initialize(List<LatLng> currentPathCoordinates, Float radius, Float deltaT,
      Float currentPhoneBearing);

}
