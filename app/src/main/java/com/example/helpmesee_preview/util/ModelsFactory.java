package com.example.helpmesee_preview.util;

import android.content.Context;

import com.example.helpmesee_preview.app_logic.AppFeaturesEnum;
import com.example.helpmesee_preview.app_logic.MvpModel;
import com.example.helpmesee_preview.directions.Model.DirectionsModelManagerImpl;
import com.example.helpmesee_preview.location.model.LocationModelManagerImpl;

/**
 * Created by Marius Olariu <mariuslucian.olariu@gmail.com>
 */
public class ModelsFactory {

  public static MvpModel createModel(Context context, AppFeaturesEnum featureId) {
    MvpModel mvpModel = null;
    switch (featureId) {
      case DIRECTIONS:
        mvpModel = new DirectionsModelManagerImpl();

        break;

      case LOCATION:
        mvpModel = new LocationModelManagerImpl(context);

        break;
      default:
    }

    return mvpModel;
  }

}
