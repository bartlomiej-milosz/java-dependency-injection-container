package dev.bartmilo.minispringcore.exceptions;

public class CircularDependencyException extends BeansException {
  public CircularDependencyException(String message) {
    super(message);
  }

  public CircularDependencyException(String message, Throwable cause) {
    super(message, cause);
  }
}
