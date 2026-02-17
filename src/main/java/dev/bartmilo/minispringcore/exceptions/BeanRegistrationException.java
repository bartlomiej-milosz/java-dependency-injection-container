package dev.bartmilo.minispringcore.exceptions;

public class BeanRegistrationException extends BeansException {
  public BeanRegistrationException(String message) {
    super(message);
  }

  public BeanRegistrationException(String message, Throwable cause) {
    super(message, cause);
  }
}
