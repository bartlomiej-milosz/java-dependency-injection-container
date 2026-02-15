package dev.bartmilo.minispringcore.resources;

public class FailingConstructorBean {
  public FailingConstructorBean() {
    throw new RuntimeException("Constructor failed!");
  }
}
