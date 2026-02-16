package dev.bartmilo.minispringcore.resources;

public class TestFailingConstructorBean {
  public TestFailingConstructorBean() {
    throw new RuntimeException("Constructor failed!");
  }
}
