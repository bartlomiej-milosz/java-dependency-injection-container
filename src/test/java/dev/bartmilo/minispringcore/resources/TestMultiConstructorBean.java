package dev.bartmilo.minispringcore.resources;

public class TestMultiConstructorBean {
  private final String value;
  private final TestRepository repository;

  public TestMultiConstructorBean() {
    this.value = "default";
    this.repository = null;
  }

  public TestMultiConstructorBean(TestRepository repository) {
    this.value = "repo-only";
    this.repository = repository;
  }

  public String getValue() {
    return value;
  }

  public TestRepository getRepository() {
    return repository;
  }
}
