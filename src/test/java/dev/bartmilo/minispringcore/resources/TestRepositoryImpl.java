package dev.bartmilo.minispringcore.resources;

public class TestRepositoryImpl implements TestRepositoryInterface {
  @Override
  public String getData() {
    return "data from impl";
  }
}
