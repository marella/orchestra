package io.github.marella.orchestra.dns;

import org.springframework.stereotype.Component;

@Component
public class SerialGenerator {

  private static final long MAX_SERIAL = (1L << 32) - 1; // max unsigned 32-bit int

  private long serial = 0;

  public synchronized long next() {
    // Use epoch time to generate increasing serial numbers across servers
    long current = System.currentTimeMillis() / 1000;
    // Ensure serial is strictly increasing for an instance
    serial = Math.max(current, serial + 1);
    return serial > MAX_SERIAL ? serial % MAX_SERIAL : serial;
  }
}
