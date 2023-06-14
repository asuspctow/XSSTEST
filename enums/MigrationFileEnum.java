package com.go2group.synapse.enums;

public enum MigrationFileEnum {
  XML("xml"),  IMAGE("image");
  
  private String name;
  
  private MigrationFileEnum(String name) { this.name = name; }
  
  public String getName()
  {
    return name;
  }
  
  public static MigrationFileEnum getByName(String name) {
    for (MigrationFileEnum val : ) {
      if (val.getName().equalsIgnoreCase(name)) {
        return val;
      }
    }
    return null;
  }
}
