package dev.bartmilo.dicontainer.exceptions;

public class NoSuchBeanDefinitionException extends BeansException {
  public NoSuchBeanDefinitionException(String message) {
    super(message);
  }
}
