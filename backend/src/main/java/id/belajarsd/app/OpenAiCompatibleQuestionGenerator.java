package id.belajarsd.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class OpenAiCompatibleQuestionGenerator implements AiQuestionGenerator {
  private final AiQuestionProperties properties;
  private final ObjectMapper mapper;
  private final HttpClient client;

  public OpenAiCompatibleQuestionGenerator(AiQuestionProperties properties, ObjectMapper mapper) {
    this.properties = properties;
    this.mapper = mapper;
    this.client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(Math.max(1, properties.getTimeoutSeconds())))
        .build();
  }

  @Override
  public List<GeneratedQuestion> generate(Lesson lesson, int count) {
    try {
      String material = lesson.getContent() == null ? "" : lesson.getContent();
      if (material.length() > 5000) material = material.substring(0, 5000);
      var prompt = mapper.createObjectNode();
      prompt.put("model", properties.getModel());
      prompt.put("temperature", 0.9);
      prompt.putObject("response_format").put("type", "json_object");
      var messages = prompt.putArray("messages");
      messages.addObject().put("role", "system").put("content", "Anda menyusun soal latihan orisinal untuk siswa sekolah dasar di Indonesia. Ikuti topik dan materi yang diberikan. Gunakan bahasa Indonesia yang sederhana, satu jawaban benar, tiga pengecoh yang masuk akal, dan penjelasan singkat yang mendidik. Jangan membuat soal ambigu atau meminta data pribadi. Kembalikan JSON saja dengan format {\"questions\":[{\"prompt\":\"...\",\"options\":[\"...\",\"...\",\"...\",\"...\"],\"correctOptionIndex\":0,\"explanation\":\"...\"}]}.");
      messages.addObject().put("role", "user").put("content", "Buat tepat " + count + " soal yang berbeda untuk:\nMata pelajaran: "
          + lesson.getSubject().getName() + "\nKelas: " + lesson.getGrade() + "\nBab: " + lesson.getTitle()
          + "\nMateri acuan:\n" + material + "\nSetiap prompt harus berdiri sendiri. Variasikan bentuk pertanyaan dan jangan menyalin kalimat materi secara mentah.");

      HttpRequest request = HttpRequest.newBuilder(URI.create(properties.getBaseUrl()))
          .timeout(Duration.ofSeconds(Math.max(1, properties.getTimeoutSeconds())))
          .header("Authorization", "Bearer " + properties.getApiKey())
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofByteArray(mapper.writeValueAsBytes(prompt)))
          .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new IllegalStateException("AI provider mengembalikan HTTP " + response.statusCode());
      }
      JsonNode root = mapper.readTree(response.body());
      String content = root.path("choices").path(0).path("message").path("content").asText(null);
      if (content == null || content.isBlank()) throw new IllegalStateException("Respons AI tidak berisi konten soal.");
      JsonNode rows = mapper.readTree(content).path("questions");
      if (!rows.isArray() || rows.size() != count) throw new IllegalStateException("Jumlah soal dari AI tidak sesuai.");

      List<GeneratedQuestion> result = new ArrayList<>();
      Set<String> uniquePrompts = new HashSet<>();
      for (JsonNode row : rows) {
        String question = row.path("prompt").asText("").trim();
        String explanation = row.path("explanation").asText("").trim();
        JsonNode optionNodes = row.path("options");
        int answer = row.path("correctOptionIndex").asInt(-1);
        if (question.length() < 8 || question.length() > 1000 || explanation.isBlank() || explanation.length() > 2000
            || !optionNodes.isArray() || optionNodes.size() != 4 || answer < 0 || answer > 3) {
          throw new IllegalStateException("Ada soal AI dengan format atau panjang yang tidak valid.");
        }
        List<String> options = new ArrayList<>();
        Set<String> normalizedOptions = new HashSet<>();
        for (JsonNode option : optionNodes) {
          String value = option.asText("").trim();
          if (value.isBlank() || value.length() > 500 || value.contains("|||")
              || !normalizedOptions.add(value.toLowerCase(Locale.ROOT))) {
            throw new IllegalStateException("Pilihan jawaban kosong atau duplikat.");
          }
          options.add(value);
        }
        String key = question.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]", "");
        if (!uniquePrompts.add(key)) throw new IllegalStateException("AI menghasilkan prompt yang duplikat.");
        result.add(new GeneratedQuestion(question, List.copyOf(options), answer, explanation));
      }
      return List.copyOf(result);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Pembuatan soal AI terinterupsi.", exception);
    } catch (Exception exception) {
      if (exception instanceof IllegalStateException state) throw state;
      throw new IllegalStateException("Gagal meminta atau membaca soal dari AI.", exception);
    }
  }
}
