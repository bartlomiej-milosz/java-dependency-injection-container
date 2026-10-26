package dev.bartmilo.dicontainer.exceptions;

public class BeanRegistrationException extends BeansException {
  public BeanRegistrationException(String message) {
    super(message);
  }

  public BeanRegistrationException(String message, Throwable cause) {
    super(message, cause);
  }
}
