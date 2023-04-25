package com.example.helpmesee_preview.location.view;


import com.example.helpmesee_preview.app_logic.MvpView;
import com.example.helpmesee_preview.location.presenter.LocationScreenListener;

public interface LocationScreenView extends MvpView {

  void displayUserCurrentLocation(String userCurrentLocation);

  void setListener(LocationScreenListener listener);
}
