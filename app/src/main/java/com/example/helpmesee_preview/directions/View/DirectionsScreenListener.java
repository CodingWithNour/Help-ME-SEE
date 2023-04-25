package com.example.helpmesee_preview.directions.View;


import com.example.helpmesee_preview.util.HmsActivity;

public abstract class DirectionsScreenListener extends HmsActivity {

  public abstract void findPath(String destination);

  /**
   * Called when one of the suggested destinations is clicked
   */
  public abstract void hideSoftKeyboard();
}
