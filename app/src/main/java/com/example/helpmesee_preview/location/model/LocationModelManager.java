package com.example.helpmesee_preview.location.model;


import com.example.helpmesee_preview.app_logic.MvpModel;

import java.util.List;

public interface LocationModelManager  extends MvpModel {
  void addContact(String contactName, String phoneNumber);

  List<Contact> getContacts();
}
