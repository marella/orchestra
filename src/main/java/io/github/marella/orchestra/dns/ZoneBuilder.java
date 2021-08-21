package io.github.marella.orchestra.dns;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ZoneBuilder {

  private static final int ttl = 5;
  private static final int refresh = 60;
  private static final int retry = 10;
  private static final int expire = 600;
  private static final int minimum = 5;

  private final SerialGenerator serials;

  public ZoneBuilder(SerialGenerator serials) {
    this.serials = serials;
  }

  public String build(List<List<String>> records) {
    long serial = serials.next();
    var data = new StringBuilder();
    data.append(getTtlLine()).append("\n");
    data.append(getSoaRecordLine(serial)).append("\n");
    data.append(getNsRecordLine()).append("\n");
    for (var record : records) {
      data.append(getARecordLine(record.get(0), record.get(1))).append("\n");
    }
    return data.toString();
  }

  private String getTtlLine() {
    return String.format("$TTL %d", ttl);
  }

  private String getSoaRecordLine(long serial) {
    return String.format(
        "@ IN SOA @ root (%d %d %d %d %d)", serial, refresh, retry, expire, minimum);
  }

  private String getNsRecordLine() {
    return "@ IN NS @";
  }

  private String getARecordLine(String domain, String ip) {
    return String.format("%s IN A %s", domain, ip);
  }
}
