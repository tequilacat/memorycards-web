package org.tequilacat.utils;

public class NotImplementedException extends RuntimeException {
  public NotImplementedException() {
    super();
  }

  public NotImplementedException(String reason) {
    super(reason);
  }
}
