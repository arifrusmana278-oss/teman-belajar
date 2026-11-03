package id.belajarsd.app;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MathQuestionBankTest {
  private static final Set<String> MATH_SUBJECTS = Set.of("Matematika", "Hitung Cepat", "Mathematics in English");
  private static final String NUMBER = "\\d{1,3}(?:[.,]\\d{3})+|\\d+[.,]\\d{1,2}(?!\\d)|\\d+";
  private static final Pattern NUMBERS = Pattern.compile(NUMBER);
  private static final Pattern EQUATION = Pattern.compile("(?<![\\d/.,])(?<![+−×÷] )(?<![)] )(" + NUMBER + ") ([+−×÷]) (" + NUMBER + ") = (" + NUMBER + ")(?![\\d/]|\\s[+−×÷]| sisa)");
  private static final Map<String, Integer> SIDES = Map.of("segitiga", 3, "persegi", 4, "segilima", 5, "segienam", 6, "triangles", 3, "squares", 4, "pentagons", 5, "hexagons", 6);
  private static final String[][] PILOTS = {
      {"Matematika", "Kelas 2", "Bab 2 — Penjumlahan dan pengurangan"},
      {"Hitung Cepat", "Kelas 4", "Bab 2 — Fakta perkalian dan pembagian"},
      {"Matematika", "Kelas 5", "Bab 2 — KPK dan FPB"}};

  private record Chapter(String subject, String grade, String title, List<MathQuestionBank.MathItem> items) {
    String name() { return subject + " " + grade + " " + title; }
    boolean english() { return subject.equals("Mathematics in English"); }
  }

  private static final List<Chapter> CHAPTERS = CurriculumCatalog.seeds().stream()
      .filter(seed -> MATH_SUBJECTS.contains(seed.subject()))
      .map(seed -> new Chapter(seed.subject(), "Kelas " + seed.grade(), seed.title(), MathQuestionBank.forChapter(seed.subject(), "Kelas " + seed.grade(), seed.title())))
      .toList();

  private static List<MathQuestionBank.MathItem> bank(String[] chapter) { return MathQuestionBank.forChapter(chapter[0], chapter[1], chapter[2]); }

  private static double parse(String number) {
    if (number.matches("[1-9]\\d{0,2}(?:[.,]\\d{3})+")) return Double.parseDouble(number.replaceAll("[.,]", ""));
    return Double.parseDouble(number.replace(',', '.'));
  }

  private static List<Double> numbers(String text) {
    Matcher matcher = NUMBERS.matcher(text);
    List<Double> found = new ArrayList<>();
    while (matcher.find()) found.add(parse(matcher.group()));
    return found;
  }

  private static boolean has(String prompt, String phrases) { return Pattern.compile(phrases).matcher(prompt).find(); }
  private static long gcd(long a, long b) { return b == 0 ? a : gcd(b, a % b); }
  private static double lcm(double a, double b) { double v = b; while (v % a != 0) v += b; return v; }
  private static double bruteGcd(double a, double b) { double v = Math.min(a, b); while (a % v != 0 || b % v != 0) v--; return v; }
  private static double nearest(double n, double unit) { return Math.floor((n + unit / 2) / unit) * unit; }

  /** Small arithmetic evaluator so bare sums are checked without using the generator's own arithmetic. */
  private static final class Calc {
    private final String s; private int i;
    Calc(String s) { this.s = s.replace(" ", ""); }
    double sum() { double v = term(); while (i < s.length() && (s.charAt(i) == '+' || s.charAt(i) == '−')) { char op = s.charAt(i++); double t = term(); v = op == '+' ? v + t : v - t; } return v; }
    double term() { double v = atom(); while (i < s.length() && (s.charAt(i) == '×' || s.charAt(i) == '÷')) { char op = s.charAt(i++); double t = atom(); v = op == '×' ? v * t : v / t; } return v; }
    double atom() { if (s.charAt(i) == '(') { i++; double v = sum(); i++; return v; } Matcher m = Pattern.compile(NUMBER + "|\\d+/\\d+").matcher(s); assertTrue(m.find(i) && m.start() == i, s); String text = m.group(); i = m.end(); return parse(text); }
  }

  /** Works the answer out from the prompt alone: a Double for numbers and fractions, a String for worded answers. */
  private static Object solve(String p) {
    List<Double> n = numbers(p);
    Matcher m = Pattern.compile("^(?:Tanda yang tepat untuk|Which sign makes this true:) (.+) \\.\\.\\. (\\S+?)(?: adalah|\\?)$").matcher(p);
    if (m.find()) { double left = new Calc(m.group(1)).sum(), right = parse(m.group(2)); return left > right ? ">" : left < right ? "<" : "="; }
    if (has(p, "^Setiap regu|^Each team")) return (n.get(0) + n.get(1)) * n.get(2);
    if (has(p, "bintang pada hari|stars on day")) return n.get(0) + n.get(1) + n.get(2);
    if (has(p, "masih kosong|are empty")) return n.get(0) * n.get(1) - n.get(2);
    if (has(p, "Pada minggu pertama|In week one")) return n.get(0) + (n.get(2) - 1) * n.get(1);
    if (has(p, "lebih pendek|shorter than")) return 2 * n.get(0) - n.get(1);
    if (has(p, "^Perjalanan|^A trip")) return n.get(0) * 60 + n.get(1);
    if (has(p, "putaran|laps")) return n.get(0) * 2 * (n.get(1) + n.get(2));
    if (has(p, "per m²")) return n.get(0) * n.get(1) * n.get(2);
    if (has(p, "kubus kecil|small cubes")) return n.get(0) * n.get(1) * n.get(1) * n.get(1);
    if (has(p, "antrean|in a queue")) return n.get(1) - n.get(0) - 1;
    if (has(p, "Segitiga siku-siku|right-angled")) return 90 - n.get(0);
    if (has(p, "koin dilempar|coin is tossed")) return n.get(0) / 2;
    if (has(p, "jumlah bilangan terbesar|sum of the largest")) return digits(n, true) + digits(n, false);
    if (has(p, "^Data |^Favourite ")) {
      double a = n.get(0), b = n.get(1), c = n.get(2), d = n.get(3), top = Math.max(Math.max(a, b), Math.max(c, d)), low = Math.min(Math.min(a, b), Math.min(c, d));
      if (has(p, "seluruh|in total")) return a + b + c + d;
      if (has(p, "selisih|difference")) return top - low;
      if (has(p, "rata-rata|mean")) return (a + b + c + d) / 4;
      if (has(p, "digabung|plus")) return Math.abs(a + b - d);
      m = Pattern.compile("(\\p{L}+) " + (int) top + "[,.]").matcher(p); assertTrue(m.find(), p); return m.group(1);
    }
    if (has(p, "piktogram|pictogram")) return n.size() > 3 ? (n.get(2) + n.get(3)) * n.get(1) : n.get(2) * n.get(1);
    if (has(p, "dilempar sekali|rolled once")) return (6 - n.get(0)) / 6;
    if (has(p, "dilempar \\d+ kali|rolled \\d+ times")) return n.get(0) / 6;
    if (has(p, "hanya berisi|holds only")) return has(p, "kelereng biru dari|a blue marble") ? (p.contains("Picking") ? "impossible" : "mustahil") : (p.contains("Picking") ? "certain" : "pasti");
    if (has(p, "sudah diambil|has been taken")) return (n.get(0) - 1) / (n.get(0) + n.get(1) - 1);
    if (has(p, "^Kantong|^A bag holds")) return n.get(0) / (n.get(0) + n.get(1));
    if (has(p, "Roda putar|spinner")) return n.get(1) / n.get(0);
    if (has(p, "paling sederhana|simplest")) { long g = gcd(n.get(1).longValue(), n.get(2 < n.size() ? 2 : 1).longValue()); g = gcd(n.get(0).longValue(), n.get(1).longValue()); return n.get(0).longValue() / g + " : " + n.get(1).longValue() / g; }
    if (has(p, "pensil merah dan biru|red to blue")) return n.get(2) / n.get(0) * n.get(1);
    if (has(p, "Kelereng dibagi|Marbles are shared")) return n.get(2) / (n.get(0) + n.get(1)) * n.get(1);
    if (has(p, "Skala|scale")) return n.get(2) * n.get(1) / 100000;
    if (has(p, "uang Alya|Alya's money")) return n.get(2) / (n.get(1) - n.get(0)) * (n.get(0) + n.get(1));
    if (has(p, "diskon|discount")) return n.get(0) - n.get(0) * n.get(1) / 100;
    if (has(p, "ikut lomba|join a contest")) return n.get(0) - n.get(0) * n.get(1) / 100;
    if (has(p, "bentuk persen|as a percent")) return n.get(0) / n.get(1) * 100;
    if (has(p, "bentuk desimal|as a decimal")) return n.get(0) / n.get(1);
    if (has(p, "^\\d+% (dari|of) ")) return n.get(0) * n.get(1) / 100;
    if (has(p, "^\\d+/\\d+ (dari|of) ")) return n.get(2) / n.get(1) * n.get(0);
    if (has(p, "^\\d+/\\d+ = \\.\\.\\./")) return n.get(2) / n.get(1) * n.get(0);
    if (has(p, "^\\d+/\\d+ \\+ \\d+/\\d+")) return n.get(0) / n.get(1) + n.get(2) / n.get(3);
    if (has(p, "Pecahan terbesar|Which fraction")) {
      m = Pattern.compile("(\\d+)/(\\d+)").matcher(p); String best = ""; double top = -1;
      while (m.find()) { double v = Double.parseDouble(m.group(1)) / Double.parseDouble(m.group(2)); if (v > top) { top = v; best = m.group(); } }
      return best;
    }
    if (has(p, "bagian terjual|of them are sold|bagian dipotong|of it is cut") || has(p, "siswa perempuan|are girls")) return n.get(0) - n.get(0) * n.get(1) / n.get(2);
    m = Pattern.compile("^(\\S+) (m|km|kg|jam|hours) (\\S+) (cm|m|g|menit|minutes) = \\.\\.\\. ").matcher(p);
    if (m.find()) return parse(m.group(1)) * factor(m.group(2)) + parse(m.group(3));
    m = Pattern.compile("^(\\S+) (\\p{L}+) = \\.\\.\\. (\\p{L}+)$").matcher(p);
    if (m.find()) return factor(m.group(2) + ">" + m.group(3)) == 0 ? parse(m.group(1)) / factor(m.group(3) + ">" + m.group(2)) : parse(m.group(1)) * factor(m.group(2) + ">" + m.group(3));
    if (has(p, "^Dua tali|^Two ropes")) return n.get(0) + n.get(1) - n.get(2);
    if (has(p, "^Tali sepanjang|^A rope")) return n.get(0) * 100 - n.get(1);
    if (has(p, "selisih panjang|difference in length")) return n.get(0) - n.get(1);
    if (has(p, "disambung|are joined")) return n.get(0) + n.get(1);
    if (has(p, "^Tiga potong|^Three sticks")) return n.get(0) + n.get(1) + n.get(2);
    if (has(p, "^Sekarang pukul|o'clock\\.")) return n.get(0) + n.get(1);
    if (has(p, "cm dan panjangnya|has a perimeter of")) return n.get(0) / 2 - n.get(1);
    if (has(p, "mengelilingi taman|runs once around")) return 2 * (n.get(0) + n.get(1));
    if (has(p, "Halaman sekolah|school yard")) return n.get(0) * n.get(1);
    if (has(p, "dipagari|is fenced")) return 2 * (n.get(0) + n.get(1)) * n.get(2);
    if (has(p, "Keliling persegi panjang|perimeter of a rectangle")) return 2 * (n.get(0) + n.get(1));
    if (has(p, "Keliling persegi|perimeter of a square")) return 4 * n.get(0);
    if (has(p, "Keliling segitiga|perimeter of a triangle")) return n.get(0) + n.get(1) + n.get(2);
    if (has(p, "cm² dan panjangnya|has an area of")) return n.get(0) / n.get(1);
    if (has(p, "Luas persegi panjang|area of a rectangle")) return n.get(0) * n.get(1);
    if (has(p, "Luas permukaan|surface area")) return 6 * n.get(0) * n.get(0);
    if (has(p, "Luas persegi|area of a square")) return n.get(0) * n.get(0);
    if (has(p, "Luas segitiga|area of a triangle")) return n.get(0) * n.get(1) / 2;
    if (has(p, "ubin|tiles")) return n.get(0) * n.get(1) / (n.get(2) * n.get(2));
    if (has(p, "Volume kubus|volume of a cube")) return n.get(0) * n.get(0) * n.get(0);
    if (has(p, "cm³, panjangnya|has a volume of")) return n.get(0) / (n.get(1) * n.get(2));
    if (has(p, "Volume balok|volume of a cuboid")) return n.get(0) * n.get(1) * n.get(2);
    if (has(p, "^Bak|^A tank")) return n.get(0) * n.get(1) * n.get(2) - n.get(3);
    if (has(p, "\\bstik\\b|\\bsticks\\b") || has(p, "^(Ada|There are) \\d+ (segitiga|persegi|segilima|segienam|triangles|squares|pentagons|hexagons)")) {
      m = Pattern.compile("(\\d+) (segitiga|persegi|segilima|segienam|triangles|squares|pentagons|hexagons)").matcher(p);
      double total = 0;
      while (m.find()) total += Double.parseDouble(m.group(1)) * SIDES.get(m.group(2));
      return has(p, "\\bstik\\b|\\bsticks\\b") ? n.get(0) - total : total;
    }
    if (has(p, "dalam satu barisan|children in a line")) return n.get(0) - n.get(1) + 1;
    if (has(p, "termasuk|is called")) { double a = n.get(0); return p.startsWith("An angle") ? (a < 90 ? "acute" : a == 90 ? "right" : "obtuse") : (a < 90 ? "sudut lancip" : a == 90 ? "sudut siku-siku" : "sudut tumpul"); }
    if (has(p, "membentuk sudut siku-siku|make a right angle")) return 90 - n.get(0);
    if (has(p, "membentuk sudut lurus|make a straight angle")) return 180 - n.get(0);
    if (has(p, "sudut segitiga|angles of a triangle")) return 180 - n.get(0) - n.get(1);
    if (has(p, "segi empat|quadrilateral")) return 360 - n.get(0) - n.get(1) - n.get(2);
    if (has(p, "jarum menit|minute hand")) return Math.min(n.get(0), 12 - n.get(0)) * 30;
    if (has(p, "sama kaki|isosceles")) return (180 - n.get(0)) / 2;
    if (has(p, "^Lanjutkan pola|^Continue")) return n.get(3) + (n.get(3) - n.get(2));
    if (has(p, "hilang pada pola|missing number in the pattern")) return n.get(0) + (n.get(2) - n.get(1));
    if (has(p, "Suku ke-|term number")) return n.get(0) + (n.get(3) - 1) * (n.get(1) - n.get(0));
    if (has(p, "^Baris pertama|^The first row")) return n.get(0) + (n.get(2) - 1) * n.get(1);
    if (has(p, "^Pola bentuk|^Repeating")) { String[] cycle = p.substring(p.indexOf(": ") + 2, p.indexOf(". ")).split(", "); return cycle[(n.get(0).intValue() - 1) % cycle.length]; }
    if (has(p, "^Bulatkan \\S+ ke|^Round \\S+ to")) return nearest(n.get(0), n.get(1));
    if (has(p, "ratus ribuan|hundred thousand")) return n.get(0) - nearest(n.get(1), 100000) - nearest(n.get(2), 100000);
    if (has(p, "Perkirakan selisih|Estimate the difference")) return Math.abs(nearest(n.get(0), 100) - nearest(n.get(1), 100));
    if (has(p, "Perkirakan \\d+ \\+|Estimate \\d+ \\+")) return nearest(n.get(0), 100) + nearest(n.get(1), 100);
    if (has(p, "Perkirakan \\d+ ×|Estimate \\d+ ×")) return nearest(n.get(0), 10) * nearest(n.get(1), 10);
    if (has(p, "membawa uang|brings .* rupiah and buys")) return n.get(0) - n.get(1) * n.get(2) - n.get(3) * n.get(4);
    if (has(p, "dengan harga|that cost")) return n.get(0) * n.get(1) + n.get(2) * n.get(3);
    if (has(p, "^KPK|^The LCM")) return lcm(n.get(0), n.get(1));
    if (has(p, "^FPB|^The GCF")) return bruteGcd(n.get(0), n.get(1));
    if (has(p, "Berapa kali")) return lcm(n.get(0), n.get(1)) / n.get(0);
    if (has(p, "dalam setiap bingkisan|in each gift")) return n.get(0) / bruteGcd(n.get(0), n.get(1));
    if (has(p, "Paling banyak|largest number of gift")) return bruteGcd(n.get(0), n.get(1));
    if (has(p, "bersamaan lagi|bersama lagi|together again")) return lcm(n.get(0), n.get(1));
    if (has(p, "lebih banyak daripada|more books than")) return 2 * n.get(0) + n.get(1);
    if (has(p, "dipinjam|borrowed")) return n.get(0) - n.get(1) + n.get(2);
    if (has(p, "tersisa e|stickers are shared") || has(p, "stiker dibagi")) return (n.get(0) - n.get(2)) / n.get(1);
    if (has(p, "dibagikan|given away")) return n.get(0) * n.get(1) - n.get(2);
    if (has(p, "ditambah")) return n.get(0) * n.get(1) + n.get(2);
    if (has(p, "sama rata|equally")) return n.get(0) / n.get(1);
    if (has(p, "Setiap|Each (box|row|basket)")) return n.get(0) * n.get(1);
    if (has(p, "memberikan|gives [\\d,]+ |penumpang naik|passengers get on|jeruk dijual|then sells|Toko memiliki|A shop has")) return n.get(0) + n.get(1) - n.get(2);
    if (has(p, "stiker lagi|more stickers")) return n.get(0) + n.get(1);
    if (has(p, "permen|sweets")) return n.get(0) - n.get(1);
    if (has(p, "daun|leaves")) return n.get(0) + n.get(1) - n.get(2);
    if (has(p, "kartu|cards")) return n.get(1) - n.get(0);
    if (has(p, "Kalimat matematika|number sentence")) return n.get(0).intValue() + " + " + n.get(1).intValue() + " = " + (n.get(0).intValue() + n.get(1).intValue());
    if (has(p, "Nilai angka|value of the digit") && !has(p, "selisih|difference")) {
      String digits = String.valueOf(n.get(1).longValue()), digit = String.valueOf(n.get(0).intValue());
      return n.get(0) * Math.pow(10, digits.length() - 1 - digits.indexOf(digit));
    }
    if (has(p, "selisih nilai|difference between the value")) {
      String digits = String.valueOf(n.get(0).longValue());
      double a = n.get(1) * Math.pow(10, digits.length() - 1 - digits.indexOf(String.valueOf(n.get(1).intValue())));
      double b = n.get(2) * Math.pow(10, digits.length() - 1 - digits.indexOf(String.valueOf(n.get(2).intValue())));
      return Math.abs(a - b);
    }
    if (has(p, "lebih besar dari|more than")) return n.get(0) + n.get(1);
    if (has(p, "terbesar di antara|largest among")) return n.stream().mapToDouble(Double::doubleValue).max().orElseThrow();
    if (has(p, "terbesar yang dapat|largest number that")) return digits(n, true);
    if (has(p, "terkecil yang dapat|smallest number that")) return digits(n, false);
    if (p.startsWith("... + ")) return n.get(1) - n.get(0);
    if (p.startsWith("... × ")) return n.get(1) / n.get(0);
    m = Pattern.compile("^(\\S+) − \\.\\.\\. = (\\S+?)\\.").matcher(p);
    if (m.find()) return parse(m.group(1)) - parse(m.group(2));
    if (p.endsWith(" = ...")) return new Calc(p.substring(0, p.length() - 6)).sum();
    throw new AssertionError("Bentuk soal belum dikenal: " + p);
  }

  private static double digits(List<Double> ds, boolean descending) {
    List<Integer> sorted = ds.stream().map(Double::intValue).sorted().collect(Collectors.toCollection(ArrayList::new));
    if (descending) java.util.Collections.reverse(sorted);
    double value = 0;
    for (int d : sorted) value = value * 10 + d;
    return value;
  }

  private static double factor(String unit) { return switch (unit) { case "m", "m>cm" -> 100; case "jam", "hours", "jam>menit", "hours>minutes" -> 60; case "km", "kg", "km>m", "kg>g" -> 1000; default -> 0; }; }

  @Test
  void everyMathChapterInTheCatalogHasABank() {
    assertEquals(79, CHAPTERS.size());
    for (Chapter chapter : CHAPTERS) assertFalse(chapter.items().isEmpty(), chapter.name());
  }

  @Test
  void everyChapterHasEnoughUniqueQuestionsToKeepSessionsDifferent() {
    for (Chapter chapter : CHAPTERS) {
      assertEquals(chapter.items().size(), chapter.items().stream().map(MathQuestionBank.MathItem::prompt).distinct().count(), chapter.name());
      assertTrue(chapter.items().stream().filter(MathQuestionBank.MathItem::essay).count() >= 10, chapter.name());
      Map<Integer, Long> levels = chapter.items().stream().filter(item -> !item.essay()).collect(Collectors.groupingBy(MathQuestionBank.MathItem::level, Collectors.counting()));
      assertTrue(levels.get(1) >= 15 && levels.get(2) >= 20 && levels.get(3) >= 15, chapter.name() + " " + levels);
    }
  }

  @Test
  void everyChapterMixesSeveralQuestionForms() {
    for (Chapter chapter : CHAPTERS) {
      long essayForms = chapter.items().stream().filter(MathQuestionBank.MathItem::essay).map(MathQuestionBank.MathItem::form).distinct().count();
      assertTrue(essayForms >= 2, chapter.name() + " esai " + essayForms);
      long choiceForms = chapter.items().stream().filter(item -> !item.essay()).map(MathQuestionBank.MathItem::form).distinct().count();
      assertTrue(choiceForms >= 3, chapter.name() + " " + choiceForms);
    }
  }

  @Test
  void storedAnswerMatchesAnIndependentCalculation() {
    List<String> wrong = new ArrayList<>();
    for (Chapter chapter : CHAPTERS) for (var item : chapter.items()) {
      String answer = item.options().get(item.correctOptionIndex());
      try {
        Matcher sameValue = Pattern.compile("^(?:Manakah yang hasilnya sama dengan|Which has the same value as) (.+)\\?$").matcher(item.prompt());
        if (sameValue.find()) {
          double target = new Calc(sameValue.group(1)).sum();
          List<String> matching = item.options().stream().filter(option -> Math.abs(new Calc(option).sum() - target) < 1e-9).toList();
          if (!matching.equals(List.of(answer))) wrong.add(item.prompt() + " " + item.options());
          continue;
        }
        Object solved = solve(item.prompt());
        boolean same = solved instanceof String worded ? worded.equals(answer) : Math.abs((Double) solved - EssayAnswer.value(answer)) < 1e-9;
        if (!same) wrong.add(item.prompt() + " → " + answer + ", dihitung " + solved);
      } catch (RuntimeException | AssertionError problem) {
        wrong.add(item.prompt() + " → " + problem);
      }
    }
    assertTrue(wrong.isEmpty(), wrong.size() + " soal: " + String.join("\n", wrong.subList(0, Math.min(25, wrong.size()))));
  }

  @Test
  void everyCalculationWrittenInTheMethodsIsCorrect() {
    int checked = 0;
    for (Chapter chapter : CHAPTERS) for (var item : chapter.items()) for (String method : new String[] {item.longMethod(), item.quickMethod()}) {
      if (method == null) continue;
      Matcher m = EQUATION.matcher(method);
      while (m.find()) {
        double a = parse(m.group(1)), b = parse(m.group(3)), c = parse(m.group(4));
        double expected = switch (m.group(2)) { case "+" -> a + b; case "−" -> a - b; case "×" -> a * b; default -> a / b; };
        assertEquals(expected, c, 1e-9, chapter.name() + ": " + item.prompt() + " | " + m.group());
        checked++;
      }
    }
    assertTrue(checked > 5000, "Terlalu sedikit hitungan yang diperiksa: " + checked);
  }

  @Test
  void choiceQuestionsHaveFourDistinctOptionsAndEssaysOnlyTheirAnswer() {
    for (Chapter chapter : CHAPTERS) for (var item : chapter.items()) {
      assertFalse(String.join(" ", item.options()).contains("Pilihan lain"), item.prompt());
      if (item.essay()) {
        assertEquals(1, item.options().size(), item.prompt());
        assertEquals(MathQuestionBank.HARD, item.level(), item.prompt());
        assertNotNull(EssayAnswer.value(item.options().get(0)), item.prompt() + " → " + item.options().get(0));
        assertTrue(item.prompt().endsWith("Tuliskan langkah-langkahmu.") || item.prompt().endsWith("Write your steps."), item.prompt());
      } else {
        assertEquals(4, new HashSet<>(item.options()).size(), chapter.name() + ": " + item.prompt() + " " + item.options());
        for (String option : item.options()) if (EssayAnswer.value(option) != null) assertTrue(EssayAnswer.value(option) >= 0, item.prompt());
      }
    }
  }

  @Test
  void bothMethodsEndOnTheCorrectAnswer() {
    for (Chapter chapter : CHAPTERS) for (var item : chapter.items()) {
      String correct = item.options().get(item.correctOptionIndex());
      String expected = (chapter.english() ? "So, the answer is " : "Jadi, jawabannya ") + correct + ".";
      assertLastLine(expected, item.longMethod(), item.prompt());
      if (item.quickMethod() != null) assertLastLine(expected, item.quickMethod(), item.prompt());
    }
  }

  private static void assertLastLine(String expected, String method, String prompt) {
    String[] lines = method.split("\n");
    assertTrue(lines.length >= 2, prompt);
    assertEquals(expected, lines[lines.length - 1], prompt);
  }

  @Test
  void chaptersStayInTheirOwnLanguage() {
    Pattern indonesian = Pattern.compile("\\b(Berapa|berapa|Jadi|jawabannya|adalah|dari|dan|yang)\\b"), english = Pattern.compile("\\b(How|What|the|answer|of|and)\\b");
    for (Chapter chapter : CHAPTERS) for (var item : chapter.items()) for (String text : new String[] {item.prompt(), item.longMethod(), item.quickMethod() == null ? "" : item.quickMethod(), String.join(" ", item.options())}) {
      assertFalse((chapter.english() ? indonesian : english).matcher(text).find(), chapter.name() + ": " + text);
    }
  }

  @Test
  void quickMethodsAreOfferedButNeverForced() {
    long withQuick = bank(PILOTS[2]).stream().filter(item -> item.quickMethod() != null).count();
    assertTrue(withQuick > 0 && withQuick < bank(PILOTS[2]).size(), "KPK/FPB hanya punya cara cepat jika memang lebih singkat: " + withQuick);
  }

  @Test
  void correctAnswerDoesNotSitAtAPredictableSortedPosition() {
    for (Chapter chapter : CHAPTERS) {
      int[] positions = new int[4];
      int total = 0;
      for (var item : chapter.items()) {
        if (item.essay() || item.options().stream().anyMatch(option -> EssayAnswer.value(option) == null)) continue;
        double[] sorted = item.options().stream().mapToDouble(EssayAnswer::value).sorted().toArray();
        positions[Arrays.binarySearch(sorted, EssayAnswer.value(item.options().get(item.correctOptionIndex())))]++;
        total++;
      }
      for (int count : positions) assertTrue(count <= Math.max(10, total * 0.6), chapter.name() + " " + Arrays.toString(positions));
    }
  }

  @Test
  void bankIsStableAcrossCallsSoStartupDoesNotRewriteIt() {
    for (Chapter chapter : CHAPTERS) assertEquals(chapter.items(), MathQuestionBank.forChapter(chapter.subject(), chapter.grade(), chapter.title()), chapter.name());
  }

  @Test
  void matematikaAndHitungCepatDoNotShareABank() {
    var matematika = MathQuestionBank.forChapter("Matematika", "Kelas 3", "Bab 3 — Perkalian dan pembagian");
    var hitungCepat = MathQuestionBank.forChapter("Hitung Cepat", "Kelas 3", "Bab 1 — Fakta perkalian");
    assertNotEquals(matematika.stream().map(MathQuestionBank.MathItem::prompt).toList(), hitungCepat.stream().map(MathQuestionBank.MathItem::prompt).toList());
  }

  @Test
  void otherSubjectsAndOldPracticeUnitsKeepTheirCurrentBank() {
    assertTrue(MathQuestionBank.forChapter("IPAS", "Kelas 3", "Bab 1 — Mari Kenali Hewan di Sekitar Kita").isEmpty());
    assertTrue(MathQuestionBank.forChapter("Matematika", "Kelas 1", "Penjumlahan sampai 20").isEmpty());
    assertTrue(MathQuestionBank.forChapter("Matematika", "Kelas 1", "Latihan tambahan — Bilangan").isEmpty());
  }

  private static long nextId = 1;
  private static Question withId(Question question) { org.springframework.test.util.ReflectionTestUtils.setField(question, "id", nextId++); return question; }
  private static Question levelled(int level) { return levelled(level, "f" + level); }
  private static Question levelled(int level, String form) { return withId(new Question(Mockito.mock(Lesson.class), "soal " + nextId, "a|||b|||c|||d", 0, "x", false, level, "x", null, form)); }
  private static Question essay() { return withId(new Question(Mockito.mock(Lesson.class), "esai " + nextId, "36 detik", 0, "x", true, 3, "x", null, "e")); }

  private static List<Question> fullBank() {
    List<Question> bank = new ArrayList<>();
    for (int i = 0; i < 60; i++) bank.add(levelled(1, "f" + i % 3));
    for (int i = 0; i < 80; i++) bank.add(levelled(2, "g" + i % 4));
    for (int i = 0; i < 60; i++) bank.add(levelled(3, "h" + i % 3));
    for (int i = 0; i < 40; i++) bank.add(essay());
    return bank;
  }

  @Test
  void sessionsDoNotRepeatAQuestionUntilTheLevelRunsOut() {
    List<Question> bank = fullBank();
    Map<Long, java.time.Instant> lastSeen = new java.util.HashMap<>();
    Set<Long> seen = new HashSet<>();
    java.time.Instant clock = java.time.Instant.parse("2026-01-01T00:00:00Z");
    for (int session = 0; session < 8; session++) {
      var selected = QuestionSelector.select(bank, 10, new Random(session), lastSeen);
      assertEquals(15, selected.size());
      for (Question question : selected) assertTrue(seen.add(question.getId()), "Soal terulang pada sesi " + session);
      clock = clock.plusSeconds(60);
      for (Question question : selected) lastSeen.put(question.getId(), clock);
    }
  }

  @Test
  void onceEverythingIsSeenTheOldestQuestionsComeBackFirst() {
    List<Question> bank = new ArrayList<>();
    for (int i = 0; i < 12; i++) bank.add(new Question(Mockito.mock(Lesson.class), "soal " + i, "a|||b", 0, "fakta " + i));
    long start = nextId;
    bank.forEach(MathQuestionBankTest::withId);
    Map<Long, java.time.Instant> lastSeen = new java.util.HashMap<>();
    for (int i = 0; i < 12; i++) lastSeen.put(start + i, java.time.Instant.ofEpochSecond(i));
    var selected = QuestionSelector.select(bank, 2, new Random(1), lastSeen);
    assertEquals(Set.of(start, start + 1), selected.stream().map(Question::getId).collect(Collectors.toSet()));
  }

  @Test
  void aLevelSpreadsAcrossFormsBeforeRepeatingOne() {
    var selected = QuestionSelector.select(fullBank(), 10, new Random(3), Map.of());
    assertEquals(3, selected.subList(0, 3).stream().map(Question::getQuestionForm).distinct().count());
    assertEquals(4, selected.subList(3, 7).stream().map(Question::getQuestionForm).distinct().count());
    assertEquals(3, selected.subList(7, 10).stream().map(Question::getQuestionForm).distinct().count());
  }

  @Test
  void writtenAnswerIsJudgedByItsValue() {
    assertTrue(EssayAnswer.matches("36 detik", "36"));
    assertTrue(EssayAnswer.matches("36 detik", " 36 detik "));
    assertTrue(EssayAnswer.matches("1.200 pensil", "1200"));
    assertTrue(EssayAnswer.matches("1.200 pensil", "1.200"));
    assertTrue(EssayAnswer.matches("1,200 pencils", "1200"));
    assertTrue(EssayAnswer.matches("0,75", "0.75"));
    assertTrue(EssayAnswer.matches("1/2", "2/4"));
    assertTrue(EssayAnswer.matches("1/2", "0,5"));
    assertTrue(EssayAnswer.matches("45°", "45"));
    assertFalse(EssayAnswer.matches("36 detik", "37"));
    assertFalse(EssayAnswer.matches("36 detik", "3 6"));
    assertFalse(EssayAnswer.matches("36 detik", "kira-kira 36"));
    assertFalse(EssayAnswer.matches("36 detik", "36 + 1"));
    assertFalse(EssayAnswer.matches("1/2", "1/0"));
    assertFalse(EssayAnswer.matches("36 detik", "99999999999999999999"));
    assertFalse(EssayAnswer.matches("36 detik", ""));
  }

  @Test
  void fullTestIsTenChoiceQuestionsThenFiveEssays() {
    List<Question> bank = new ArrayList<>();
    for (int i = 0; i < 15; i++) bank.add(levelled(1));
    for (int i = 0; i < 20; i++) bank.add(levelled(2));
    for (int i = 0; i < 15; i++) bank.add(levelled(3));
    for (int i = 0; i < 10; i++) bank.add(essay());
    var test = QuestionSelector.select(bank, 10, new Random(1), Map.of());
    assertEquals(15, new HashSet<>(test).size());
    assertEquals(List.of(1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3), test.stream().map(Question::getDifficultyLevel).toList());
    assertEquals(List.of(false, false, false, false, false, false, false, false, false, false, true, true, true, true, true), test.stream().map(Question::isEssay).toList());
    var practice = QuestionSelector.select(bank, 5, new Random(1), Map.of());
    assertEquals(List.of(false, false, false, false, true), practice.stream().map(Question::isEssay).toList());
    assertEquals(List.of(1, 1, 2, 2, 3), practice.stream().map(Question::getDifficultyLevel).toList());
  }

  @Test
  void bankWithoutLevelsStillGetsARandomDraw() {
    List<Question> bank = new ArrayList<>();
    for (int i = 0; i < 50; i++) bank.add(withId(new Question(Mockito.mock(Lesson.class), "soal " + i, "a|||b", 0, "fakta " + i)));
    assertEquals(10, QuestionSelector.select(bank, 10, new Random(1), Map.of()).size());
  }

  private static Lesson lesson(String[] chapter) {
    Subject subject = Mockito.mock(Subject.class);
    Mockito.when(subject.getName()).thenReturn(chapter[0]);
    Lesson lesson = Mockito.mock(Lesson.class);
    Mockito.when(lesson.getSubject()).thenReturn(subject);
    Mockito.when(lesson.getGrade()).thenReturn(chapter[1]);
    Mockito.when(lesson.getTitle()).thenReturn(chapter[2]);
    Mockito.when(lesson.getId()).thenReturn(7L);
    return lesson;
  }

  @Test
  void syncReplacesOldRowsOnceAndThenLeavesThemAlone() {
    QuestionRepository repository = Mockito.mock(QuestionRepository.class);
    Lesson lesson = lesson(PILOTS[0]);
    Question legacy = new Question(lesson, "1 + 1 = ?", "2|||3|||1|||4", 0, "lama");
    Mockito.when(repository.findByLessonIdOrderByIdAsc(7L)).thenReturn(List.of(legacy));
    MathQuestionSync sync = new MathQuestionSync(repository);

    assertTrue(sync.sync(lesson));
    Mockito.verify(repository).deleteAll(List.of(legacy));
    @SuppressWarnings("unchecked")
    org.mockito.ArgumentCaptor<List<Question>> saved = org.mockito.ArgumentCaptor.forClass(List.class);
    Mockito.verify(repository).saveAll(saved.capture());
    assertEquals(bank(PILOTS[0]).size(), saved.getValue().size());

    Mockito.clearInvocations(repository);
    Mockito.when(repository.findByLessonIdOrderByIdAsc(7L)).thenReturn(saved.getValue());
    assertTrue(sync.sync(lesson));
    Mockito.verify(repository, Mockito.never()).deleteAll(Mockito.anyList());
    Mockito.verify(repository, Mockito.never()).saveAll(Mockito.anyList());
  }

  @Test
  void syncIgnoresChaptersWithoutAGeneratedBank() {
    QuestionRepository repository = Mockito.mock(QuestionRepository.class);
    assertFalse(new MathQuestionSync(repository).sync(lesson(new String[] {"IPAS", "Kelas 3", "Bab 1 — Hewan"})));
    Mockito.verifyNoInteractions(repository);
  }
}
