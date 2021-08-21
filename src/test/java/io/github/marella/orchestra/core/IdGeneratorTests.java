package io.github.marella.orchestra.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IdGeneratorTests {

  @Test
  void testNext() {
    var ids = new IdGenerator(0);
    Set<Long> seen = new HashSet<>();
    for (int i = 0; i < 5; i++) {
      long id = ids.next();
      assertFalse(seen.contains(id));
      seen.add(id);
    }
  }
}
