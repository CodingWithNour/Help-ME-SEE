package com.example.helpmesee_preview.mainscreen.view;


import com.example.helpmesee_preview.app_logic.MvpView;

import java.util.List;

/**
 * Created by Marius Olariu <mariuslucian.olariu@gmail.com>
 */

public interface MainMenuScreenView extends MvpView {

  void displayAppFeatures(List<String> features);

  void setScreenListener(MainMenuScreenListener listener);

  void askTurnGpsOn();

  void askTurnInternetOn();
}
