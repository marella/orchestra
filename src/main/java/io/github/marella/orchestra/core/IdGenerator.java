package io.github.marella.orchestra.core;

/**
 * @see
 *     https://github.com/twitter-archive/snowflake/blob/snowflake-2010/src/main/scala/com/twitter/service/snowflake/IdWorker.scala
 */
public class IdGenerator {

  private static final long startTime = 1_616_000_000_000L;

  private static final int timeBits = 41;
  private static final int nodeBits = 10;
  private static final int sequenceBits = 12;

  private static final int sequenceShift = 0;
  private static final int nodeShift = sequenceBits + sequenceShift;
  private static final int timeShift = nodeBits + nodeShift;

  private static final long maxTime = (1L << timeBits) - 1;
  private static final int maxNode = (1 << nodeBits) - 1;
  private static final int maxSequence = (1 << sequenceBits) - 1;
  private static final long maxCurrentTime = startTime + maxTime;

  private final int node;

  private int sequence = 0;
  private long lastTime = 0;

  public IdGenerator(int node) {
    if (node < 0 || node > maxNode) {
      throw new IllegalArgumentException(
          String.format("Node ID = %d should be >= 0 and <= %d", node, maxNode));
    }
    this.node = node;
  }

  public synchronized long next() {
    long currentTime = System.currentTimeMillis();
    if (currentTime > maxCurrentTime) {
      throw new IllegalStateException(
          String.format("Current time = %d should be <= %d", currentTime, maxCurrentTime));
    } else if (currentTime < lastTime) {
      throw new IllegalStateException(
          String.format("Current time = %d should be >= %d", currentTime, lastTime));
    } else if (currentTime == lastTime) {
      if (sequence == maxSequence) {
        throw new IllegalStateException(
            String.format("Sequence = %d should be <= %d", sequence, maxSequence));
      }
      sequence++;
    } else { // currentTime > lastTime
      sequence = 0;
    }
    lastTime = currentTime;
    return ((currentTime - startTime) << timeShift)
        | (node << nodeShift)
        | (sequence << sequenceShift);
  }
}
