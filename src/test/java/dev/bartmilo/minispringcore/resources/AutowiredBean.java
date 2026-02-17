package dev.bartmilo.minispringcore.resources;

import dev.bartmilo.minispringcore.annotations.Autowired;

public class AutowiredBean {
  public AutowiredBean(TestRepository repo, TestService service) {}

  @Autowired
  public AutowiredBean(TestRepository repo) {}
}
