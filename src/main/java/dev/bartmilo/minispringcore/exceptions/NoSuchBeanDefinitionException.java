package dev.bartmilo.minispringcore.exceptions;

public class NoSuchBeanDefinitionException extends BeansException {
  public NoSuchBeanDefinitionException(String message) {
    super(message);
  }
}
