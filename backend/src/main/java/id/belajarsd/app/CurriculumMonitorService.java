package id.belajarsd.app;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CurriculumMonitorService {
  public record Source(String key, String subject, int grade, String title, String url) {}
  private static final List<Source> SOURCES = List.of(
      new Source("matematika-1", "Matematika", 1, "Buku Matematika Kelas I (Kemendikdasmen)", "https://repositori.kemendikdasmen.go.id/35868/1/Matematika-BS-KLS-I.pdf"),
      new Source("bahasa-indonesia-1", "Bahasa Indonesia", 1, "Buku Bahasa Indonesia Kelas I (Kemendikdasmen)", "https://repositori.kemendikdasmen.go.id/35871/1/Bahasa_Indonesia_BS_KLS_I_Rev.pdf"),
      new Source("matematika-4", "Matematika", 4, "Buku Matematika Kelas IV (Kemendikdasmen)", "https://repositori.kemendikdasmen.go.id/35863/1/Matematika-BS-KLS-IV.pdf"),
      new Source("ipas-4", "IPAS", 4, "Buku IPAS Kelas IV (Kemendikdasmen)", "https://repositori.kemendikdasmen.go.id/35919/1/IPAS_BS_KLS_IV_Rev.pdf"),
      new Source("bahasa-inggris-3", "Bahasa Inggris", 3, "Panduan Bahasa Inggris Kelas III (Kemendikdasmen)", "https://repositori.kemendikdasmen.go.id/35815/1/Inggris_FN_BG_KLS_III_Rev.pdf"),
      new Source("bahasa-inggris-4", "Bahasa Inggris", 4, "Buku Bahasa Inggris Kelas IV (Kemendikdasmen)", "https://repositori.kemendikdasmen.go.id/35869/1/Inggris_FN_BS_KLS_IV.pdf"),
      new Source("ipas-5", "IPAS", 5, "Buku IPAS Kelas V (Kemendikdasmen)", "https://static-sc.cloudapp.web.id/content/pdf/bukuteks/kurikulum21/IPAS_BS_KLS_V_Rev.pdf"),
      new Source("ipas-6", "IPAS", 6, "Buku IPAS Kelas VI (Kemendikdasmen)", "https://static-sc.cloudapp.web.id/content/pdf/bukuteks/kurikulum21/IPAS_BS_KLS_VI_Rev.pdf"),
      new Source("matematika-5", "Matematika", 5, "Buku Matematika Kelas V (Kemendikdasmen)", "https://static-sc.cloudapp.web.id/content/pdf/bukuteks/kurikulum21/Matematika-BS-KLS-V.pdf")
  );

  private final LessonRepository lessons;
  private final CurriculumSourceSnapshotRepository snapshots;
  private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).followRedirects(HttpClient.Redirect.NORMAL).build();

  public CurriculumMonitorService(LessonRepository lessons, CurriculumSourceSnapshotRepository snapshots) {
    this.lessons = lessons;
    this.snapshots = snapshots;
  }

  public List<Map<String, Object>> catalog() {
    List<Lesson> all = lessons.findAllWithSubject();
    return all.stream().collect(java.util.stream.Collectors.groupingBy(
        l -> l.getSubject().getName() + "|" + l.getGrade(), LinkedHashMap::new, java.util.stream.Collectors.toList()))
        .entrySet().stream().<Map<String, Object>>map(entry -> {
          String[] key = entry.getKey().split("\\|", 2);
          var row = new LinkedHashMap<String, Object>();
          row.put("subject", key[0]); row.put("grade", key[1]);
          row.put("chapters", entry.getValue().stream().filter(l -> l.getTitle().matches("(?i)^Bab\\s+\\d+.*"))
              .sorted(java.util.Comparator.comparingInt(l -> chapterNumber(l.getTitle())))
              .map(Lesson::getTitle).toList());
          row.put("supplementalCount", entry.getValue().stream().filter(l -> l.getTitle().startsWith("Latihan tambahan")).count());
          row.put("sources", SOURCES.stream().filter(s -> s.subject().equals(key[0]) && ("Kelas " + s.grade()).equals(key[1]))
              .map(s -> sourceDto(s, snapshots.findById(s.key()).orElse(null), null)).toList());
          return row;
        }).toList();
  }

  public List<Map<String, Object>> checkAll() {
    return SOURCES.parallelStream().map(source -> {
      try {
        HttpRequest request = HttpRequest.newBuilder(URI.create(source.url())).timeout(Duration.ofSeconds(22))
            .header("User-Agent", "TemanBelajarSD-CurriculumMonitor/1.0").GET().build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        try (InputStream stream = response.body()) {
          if (response.statusCode() < 200 || response.statusCode() >= 300) throw new IllegalStateException("Situs sumber merespons HTTP " + response.statusCode());
          MessageDigest digest = MessageDigest.getInstance("SHA-256");
          byte[] buffer = new byte[16384]; long bytes = 0; int read;
          while ((read = stream.read(buffer)) != -1) {
            bytes += read;
            if (bytes > 80L * 1024 * 1024) throw new IllegalStateException("Berkas sumber melebihi batas pemeriksaan 80 MB");
            digest.update(buffer, 0, read);
          }
          String hash = HexFormat.of().formatHex(digest.digest());
          Instant now = Instant.now();
          CurriculumSourceSnapshot snapshot = snapshots.findById(source.key()).orElse(null);
          String result;
          if (snapshot == null) {
            snapshot = new CurriculumSourceSnapshot(source.key(), hash, now);
            snapshots.save(snapshot);
            result = "BASELINE_SAVED";
          } else {
            boolean changed = !snapshot.getSha256().equals(hash);
            snapshot.checked(hash, now);
            snapshots.save(snapshot);
            result = snapshot.isPendingReview() ? "CHANGED" : changed ? "CHANGED" : "UNCHANGED";
          }
          return sourceDto(source, snapshot, result, bytes);
        }
      } catch (Exception e) {
        return sourceDto(source, snapshots.findById(source.key()).orElse(null), "UNAVAILABLE", 0L, e.getMessage());
      }
    }).toList();
  }

  public Map<String, Object> acknowledge(String key) {
    Source source = SOURCES.stream().filter(s -> s.key().equals(key)).findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sumber kurikulum tidak ditemukan"));
    CurriculumSourceSnapshot snapshot = snapshots.findById(source.key())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Periksa sumber ini terlebih dahulu"));
    snapshot.acknowledge();
    snapshots.save(snapshot);
    return sourceDto(source, snapshot, "ACKNOWLEDGED");
  }

  private Map<String, Object> sourceDto(Source s, CurriculumSourceSnapshot snapshot, String status) {
    return sourceDto(s, snapshot, status, 0L, null);
  }
  private Map<String, Object> sourceDto(Source s, CurriculumSourceSnapshot snapshot, String status, long bytes) {
    return sourceDto(s, snapshot, status, bytes, null);
  }
  private Map<String, Object> sourceDto(Source s, CurriculumSourceSnapshot snapshot, String status, long bytes, String error) {
    var dto = new LinkedHashMap<String, Object>();
    dto.put("key", s.key()); dto.put("title", s.title()); dto.put("url", s.url()); dto.put("subject", s.subject()); dto.put("grade", s.grade());
    dto.put("status", status != null ? status : snapshot != null && snapshot.isPendingReview() ? "CHANGED" : snapshot == null ? "NOT_CHECKED" : "UNCHANGED");
    dto.put("checkedAt", snapshot == null ? null : snapshot.getCheckedAt()); dto.put("changedAt", snapshot == null ? null : snapshot.getChangedAt());
    dto.put("pendingReview", snapshot != null && snapshot.isPendingReview()); dto.put("bytes", bytes);
    if (error != null) dto.put("error", error);
    return dto;
  }
  private int chapterNumber(String title) {
    var matcher = java.util.regex.Pattern.compile("(?i)^Bab\\s+(\\d+)").matcher(title);
    return matcher.find() ? Integer.parseInt(matcher.group(1)) : Integer.MAX_VALUE;
  }
}
