package dev.bartmilo.minispringcore.resources;

public class MultiConstructorBean {
  private final String value;
  private final TestRepository repository;

  public MultiConstructorBean() {
    this.value = "default";
    this.repository = null;
  }

  public MultiConstructorBean(TestRepository repository) {
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
