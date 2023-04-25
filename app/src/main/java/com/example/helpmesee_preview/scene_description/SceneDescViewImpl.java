package com.example.helpmesee_preview.scene_description;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.helpmesee_preview.R;


/**
 * Created by Marius Olariu <mariuslucian.olariu@gmail.com>
 */
public class SceneDescViewImpl implements SceneDescView {
  private final View rootView;
  private final Context context;

  public SceneDescViewImpl(Context context, ViewGroup container) {
    this.context = context;
    rootView = LayoutInflater.from(context).inflate(R.layout.scene_desc_layout, container);
  }

  @Override
  public View getAndroidLayoutView() {
    return rootView;
  }

  @Override
  public Bundle getViewState() {
    return null;
  }

  @Override
  public void onSaveViewState(Bundle outState) {

  }

  @Override
  public void onRestoreInstanceState(Bundle inState) {

  }

}
