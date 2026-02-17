package dev.bartmilo.minispringcore.resources;

import dev.bartmilo.minispringcore.annotations.Autowired;

public class MultipleAutowiredBean {
  @Autowired
  public MultipleAutowiredBean() {}

  @Autowired
  public MultipleAutowiredBean(TestRepository repo) {}
}
