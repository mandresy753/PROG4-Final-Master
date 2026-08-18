package com.example.demo.enums;

public enum UserRole {
  STUDENT("STD"),
  TEACHER("TCH"),
  ADMIN("ADM");

  private final String referencePrefix;

  UserRole(String referencePrefix) {
    this.referencePrefix = referencePrefix;
  }

  public String referencePrefix() {
    return referencePrefix;
  }
}
