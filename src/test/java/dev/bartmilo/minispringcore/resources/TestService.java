package dev.bartmilo.minispringcore.resources;

public class TestService {
  private TestRepository testRepository;
  
  public TestService(TestRepository testRepository) {
    this.testRepository = testRepository;
  }
}
