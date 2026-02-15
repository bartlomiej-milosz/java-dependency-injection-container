package dev.bartmilo.minispringcore.exceptions;

public class BeanCreationException extends BeansException {
  public BeanCreationException(String message) {
    super(message);
  }

  public BeanCreationException(String message, Throwable cause) {
    super(message, cause);
  }
}
