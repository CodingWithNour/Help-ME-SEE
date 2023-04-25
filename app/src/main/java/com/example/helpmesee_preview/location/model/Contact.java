package com.example.helpmesee_preview.location.model;


public class Contact {
  public String contactName;
  public String phoneNumber;

  public Contact(String contactName, String phoneNumber) {
    this.contactName = contactName;
    this.phoneNumber = phoneNumber;
  }

  @Override
  public String toString() {
    return contactName + " - " + phoneNumber;
  }
}
