package com.example.helpmesee_preview.scene_description;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.helpmesee_preview.util.HmsActivity;

/**
 * Created by Marius Olariu <mariuslucian.olariu@gmail.com>
 */
public class SceneDescPresenter extends HmsActivity {
  private SceneDescView rootView;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    rootView = new SceneDescViewImpl(this, null);
    setContentView(rootView.getAndroidLayoutView());
  }

  @Override
  public void execute(String detectedText) {

  }
}
