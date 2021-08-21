package io.github.marella.orchestra.dns;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SerialGeneratorTests {

  @Test
  void testNext() {
    var serials = new SerialGenerator();
    Set<Long> seen = new HashSet<>();
    for (int i = 0; i < 5; i++) {
      long serial = serials.next();
      assertFalse(seen.contains(serial));
      seen.add(serial);
    }
  }
}
