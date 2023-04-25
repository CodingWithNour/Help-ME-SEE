package com.example.helpmesee_preview.util;

import android.content.Context;

import com.example.helpmesee_preview.app_logic.AppFeaturesEnum;
import com.example.helpmesee_preview.app_logic.MvpView;
import com.example.helpmesee_preview.directions.View.DirectionsScreenViewImpl;
import com.example.helpmesee_preview.location.view.LocationScreenViewImpl;
import com.example.helpmesee_preview.mainscreen.view.MainMenuScreenViewImpl;

/**
 * Created by Marius Olariu <mariuslucian.olariu@gmail.com>
 */
public class ViewsFactory {

  public static MvpView createView(Context context, AppFeaturesEnum featureId) {
    MvpView screenView = null;

    switch (featureId) {
      case DIRECTIONS:
       //screenView = new DirectionsScreenViewImpl(context, null);
        break;

      case LOCATION:
        screenView = new LocationScreenViewImpl(context, null);
        break;

        //add the other features when they are implemented
      default:
        //screenView = new MainMenuScreenViewImpl(context, null);
    }

    return screenView;
  }

}
