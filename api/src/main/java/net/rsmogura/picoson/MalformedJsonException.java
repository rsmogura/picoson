package net.rsmogura.picoson;

public class MalformedJsonException extends RuntimeException {
  public MalformedJsonException() {
  }

  public MalformedJsonException(String message) {
    super(message);
  }

  public MalformedJsonException(String message, Throwable cause) {
    super(message, cause);
  }

  public MalformedJsonException(Throwable cause) {
    super(cause);
  }

  public MalformedJsonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
