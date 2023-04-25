package com.example.helpmesee_preview.directions.Model;


public enum Instruction {
  SLIGHTLY_LEFT("go slightly  left"), SLIGHTLY_RIGHT("go slightly right"),
  LEFT("go left"), RIGHT("go right"), STRAIGHT("continue straight"), END(
      "You arrived at destination!"),
  T_RIGHT_150("Turn right abruplty soon"), T_RIGHT_90("Turn right soon"),T_RIGHT_30("Turn slightly to right soon"),
  T_LEFT_150("Turn left abruplty soon"), T_LEFT_90("Turn left soon"),T_LEFT_30("Turn slightly to left soon");


  private final String instruction;

  Instruction(String instruction) {
    this.instruction = instruction;
  }

  @Override
  public String toString() {
    return instruction;
  }

}
