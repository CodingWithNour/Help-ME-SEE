package com.example.helpmesee_preview.app_logic;

public interface SpecificScreenVoiceCommand {

  /*Each screen has a specific  command that it can process
    i.e.: DirectionsScreen - set destination via voice input
          TextRecognition/Scene Description -  capture a photo that is going to be analyzed via "take photo" command
   */
  void execute(String detectedText);
}
