package dev.bartmilo.minispringcore.resources;

public class TestDeepService {
  private final TestDeepRepository repository;

  public TestDeepService(TestDeepRepository repository) {
    this.repository = repository;
  }

  public String getData() {
    return repository.getData();
  }
}
