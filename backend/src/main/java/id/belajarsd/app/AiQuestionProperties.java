package id.belajarsd.app;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiQuestionProperties {
  private boolean enabled = false;
  private String baseUrl = "https://api.openai.com/v1/chat/completions";
  private String apiKey = "";
  private String model = "";
  private int timeoutSeconds = 90;
  private int questionsPerLesson = 12;
  private int retentionDays = 30;
  private int batchSize = 1000;
  private String scheduleCron = "0 59 23 * * SUN";
  private String scheduleZone = "Asia/Jakarta";

  public boolean isEnabled() { return enabled; }
  public void setEnabled(boolean enabled) { this.enabled = enabled; }
  public String getBaseUrl() { return baseUrl; }
  public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
  public String getApiKey() { return apiKey; }
  public void setApiKey(String apiKey) { this.apiKey = apiKey; }
  public String getModel() { return model; }
  public void setModel(String model) { this.model = model; }
  public int getTimeoutSeconds() { return timeoutSeconds; }
  public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
  public int getQuestionsPerLesson() { return questionsPerLesson; }
  public void setQuestionsPerLesson(int questionsPerLesson) { this.questionsPerLesson = questionsPerLesson; }
  public int getRetentionDays() { return retentionDays; }
  public void setRetentionDays(int retentionDays) { this.retentionDays = retentionDays; }
  public int getBatchSize() { return batchSize; }
  public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
  public String getScheduleCron() { return scheduleCron; }
  public void setScheduleCron(String scheduleCron) { this.scheduleCron = scheduleCron; }
  public String getScheduleZone() { return scheduleZone; }
  public void setScheduleZone(String scheduleZone) { this.scheduleZone = scheduleZone; }
}
