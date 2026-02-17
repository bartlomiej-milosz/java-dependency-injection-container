package dev.bartmilo.minispringcore.resources;

public class TestDeepRepository {
  private final TestDatabase database;

  public TestDeepRepository(TestDatabase database) {
    this.database = database;
  }

  public String getData() {
    return database.getData();
  }
}
