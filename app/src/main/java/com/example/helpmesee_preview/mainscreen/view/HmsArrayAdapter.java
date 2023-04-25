package com.example.helpmesee_preview.mainscreen.view;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.helpmesee_preview.R;

import java.util.List;

/**
 * Created by Marius Olariu <mariuslucian.olariu@gmail.com>
 */
public class HmsArrayAdapter extends ArrayAdapter<String> {
  private final Context context;
  private final List<String> features;
  private final int layoutResId;

  public HmsArrayAdapter(@NonNull Context context,
      int resource, @NonNull List<String> appFeatures) {
    super(context, resource, appFeatures);
    this.context = context;
    this.layoutResId = resource;
    this.features = appFeatures;
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    ItemHolder itemHolder;

    if (convertView == null) {
      convertView = LayoutInflater.from(context).inflate(layoutResId, null);

      itemHolder = new ItemHolder();

      itemHolder.textView = (TextView) convertView.findViewById(R.id.tvId);

      convertView.setTag(itemHolder);
    }else{
      itemHolder = (ItemHolder) convertView.getTag();
    }

    itemHolder.textView.setText(features.get(position));

    return convertView;
  }

  private static class ItemHolder{
    TextView textView;
  }
}
