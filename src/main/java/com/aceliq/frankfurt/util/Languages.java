package com.aceliq.frankfurt.util;

public enum Languages {
  EN(1);
  
  int code;
  
  Languages(int code) {
    this.code = code;
  }
  
  public int getCode() {
    return this.code;
  }
}
