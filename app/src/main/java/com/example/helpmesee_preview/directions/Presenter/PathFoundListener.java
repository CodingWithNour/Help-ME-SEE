package com.example.helpmesee_preview.directions.Presenter;



import com.example.helpmesee_preview.directions.Model.PathDto;

import java.util.List;

public interface PathFoundListener {

  /**
   * Callback method
   * @param pathDto - dto object
   */
  void onPathsFound(List<PathDto> pathDto);
}
