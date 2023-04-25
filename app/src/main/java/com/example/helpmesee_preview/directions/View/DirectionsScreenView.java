package com.example.helpmesee_preview.directions.View;


import com.example.helpmesee_preview.app_logic.MvpView;

public interface DirectionsScreenView extends MvpView {

  void setScreenListener(DirectionsScreenListener listener);

  void setDestination(String destination);

  void setDistance(String distance);

  void setDuration(String duration);

}
