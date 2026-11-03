package id.belajarsd.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Scores a written final answer by its value, so "36", "36 detik", "1.200", "1,5" and "2/4" are all understood. */
final class EssayAnswer {
  private static final Pattern ANSWER = Pattern.compile("^\\s*(?:(\\d{1,6})/(\\d{1,6})|([1-9]\\d{0,2}(?:[.,]\\d{3})+)|(\\d{1,9})(?:[.,](\\d{1,3}))?)(?:\\s*[\\p{L}°%²³][\\p{L}°%²³\\s-]*)?\\s*$");

  private EssayAnswer() {}

  static boolean matches(String expected, String written) {
    Double expectedValue = value(expected), writtenValue = value(written);
    return expectedValue != null && writtenValue != null && Math.abs(expectedValue - writtenValue) < 1e-9;
  }

  /** @return the number written, or null when the text is not a single number with an optional unit. */
  static Double value(String text) {
    if (text == null) return null;
    Matcher answer = ANSWER.matcher(text);
    if (!answer.matches()) return null;
    if (answer.group(1) != null) {
      double bottom = Double.parseDouble(answer.group(2));
      return bottom == 0 ? null : Double.parseDouble(answer.group(1)) / bottom;
    }
    if (answer.group(3) != null) return Double.valueOf(answer.group(3).replaceAll("[.,]", ""));
    return Double.valueOf(answer.group(4) + (answer.group(5) == null ? "" : "." + answer.group(5)));
  }
}
