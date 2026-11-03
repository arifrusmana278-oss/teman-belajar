package id.belajarsd.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;

/**
 * Chapter-specific math practice with a worked long method and, where one is genuinely shorter, a quick method.
 * Fixed random seeds keep each bank identical across restarts, so startup sync does not rewrite it.
 */
final class MathQuestionBank {
  static final int EASY = 1, MEDIUM = 2, HARD = 3;

  /** An essay has its accepted final answer as the only option. */
  record MathItem(String prompt, List<String> options, int correctOptionIndex, String explanation,
      boolean essay, int level, String longMethod, String quickMethod, String form) {}

  private static final int[] TIMES_25_FACTORS = {12, 16, 24, 28, 32, 36, 44, 48, 52, 56, 64, 68, 72, 76, 84, 88, 92, 96};

  private MathQuestionBank() {}

  static List<MathItem> forLesson(Lesson lesson) {
    return forChapter(lesson.getSubject().getName(), lesson.getGrade(), lesson.getTitle());
  }

  static List<MathItem> forChapter(String subject, String grade, String title) {
    return switch (subject + "|" + grade + "|" + title) {
      case "Hitung Cepat|Kelas 4|Bab 2 — Fakta perkalian dan pembagian" -> quickMultiplication();
      case "Matematika|Kelas 5|Bab 2 — KPK dan FPB" -> lcmAndGcd();
      default -> MathCurriculum.forChapter(subject, grade, title);
    };
  }

  // ---------- Hitung Cepat kelas 4: fakta perkalian dan pembagian ----------

  private static List<MathItem> quickMultiplication() {
    Bank bank = new Bank(402L);
    List<Integer> timesFive = new ArrayList<>(), divideFive = new ArrayList<>(), timesNine = new ArrayList<>(), timesEleven = new ArrayList<>();
    for (int n = 12; n <= 99; n++) {
      if (n % 10 == 0) continue;
      if (n > 12) timesFive.add(n);
      if (n > 20) divideFive.add(n);
      timesNine.add(n);
      if (n % 11 != 0) timesEleven.add(n);
    }
    List<Integer> timesTwentyFive = new ArrayList<>(), boxes = new ArrayList<>(), shares = new ArrayList<>();
    for (int factor : TIMES_25_FACTORS) { timesTwentyFive.add(factor); boxes.add(factor); shares.add(factor); }

    bank.take(timesFive, 30, n -> bank.add(EASY, n + " × 5 = ...", "", n * 5,
        values(n * 10, n * 5 + 10, n * 5 - 10, n * 5 + 5, (n / 10 * 5) * 10 + n % 10 * 5 % 10),
        carryTimesLong(n, 5), lines("× 5 sama dengan × 10 lalu dibagi 2.", n + " × 10 = " + fmt(n * 10) + ".", fmt(n * 10) + " ÷ 2 = " + fmt(n * 5) + ".")));
    bank.take(divideFive, 30, n -> {
      int dividend = n * 5, firstTwo = dividend / 10, first = firstTwo / 5, rest = firstTwo % 5, next = rest * 10 + dividend % 10;
      bank.add(EASY, fmt(dividend) + " ÷ 5 = ...", "", n,
          values(dividend / 10, dividend * 2, n + 10, n - 10, n + 5, n + 1),
          lines("Bagi bersusun dari kiri.", firstTwo + " ÷ 5 = " + first + " sisa " + rest + ".", "Turunkan angka berikutnya: " + next + " ÷ 5 = " + (next / 5) + ".", "Gabungkan " + first + " dan " + (next / 5) + ": " + n + "."),
          lines("÷ 5 sama dengan × 2 lalu dibagi 10.", fmt(dividend) + " × 2 = " + fmt(dividend * 2) + ".", fmt(dividend * 2) + " ÷ 10 = " + n + "."));
    });
    bank.take(timesNine, 30, n -> bank.add(MEDIUM, n + " × 9 = ...", "", n * 9,
        values(n * 10, n * 8, n * 9 + 10, n * 9 - 10, n * 9 + 9),
        carryTimesLong(n, 9), lines("× 9 sama dengan × 10 dikurangi bilangan itu sendiri.", n + " × 10 = " + fmt(n * 10) + ".", fmt(n * 10) + " − " + n + " = " + fmt(n * 9) + ".")));
    bank.take(timesEleven, 25, n -> {
      int tens = n / 10, ones = n % 10, digitSum = tens + ones, answer = n * 11;
      String quick = digitSum < 10
          ? lines("× 11 dua angka: jumlahkan kedua angkanya, taruh di tengah.", tens + " + " + ones + " = " + digitSum + ".", "Sisipkan " + digitSum + " di antara " + tens + " dan " + ones + ": " + fmt(answer) + ".")
          : lines("× 11 dua angka: jumlahkan kedua angkanya, taruh di tengah.", tens + " + " + ones + " = " + digitSum + ", tulis " + (digitSum % 10) + " di tengah dan simpan 1.", "Angka depan: " + tens + " + 1 = " + (tens + 1) + ".");
      bank.add(MEDIUM, n + " × 11 = ...", "", answer,
          values(n * 10, answer - 100, answer + 100, answer + 10, answer - 10),
          lines("Pecah 11 menjadi 10 + 1.", n + " × 1 = " + n + ".", n + " × 10 = " + fmt(n * 10) + ".", n + " + " + fmt(n * 10) + " = " + fmt(answer) + "."),
          quick);
    });
    bank.take(timesTwentyFive, 18, n -> bank.add(MEDIUM, n + " × 25 = ...", "", n * 25,
        times25Distractors(n), times25Long(n), times25Quick(n)));

    String[][] boxStories = {{"Bu Rani", "kotak", "pensil"}, {"Pak Dani", "dus", "buku"}, {"Bu Sari", "plastik", "permen"}, {"Pak Hadi", "keranjang", "jeruk"}};
    int[] boxStory = {0};
    bank.take(boxes, 18, n -> {
      String[] story = boxStories[boxStory[0]++ % boxStories.length];
      String unit = story[2];
      bank.add(HARD, story[0] + " membeli " + n + " " + story[1] + " " + unit + ". Setiap " + story[1] + " berisi 25 " + unit + ". Berapa banyak " + unit + " seluruhnya?",
          unit, n * 25, times25Distractors(n), times25Long(n), times25Quick(n));
    });
    String[][] shareStories = {{"Panitia", "buku", "kelas"}, {"Sekolah", "pensil", "kelompok"}, {"Koperasi", "kue", "regu"}, {"Perpustakaan", "kartu", "tim"}};
    int[] shareStory = {0};
    bank.take(shares, 18, n -> {
      String[] story = shareStories[shareStory[0]++ % shareStories.length];
      int total = n * 25, tensPart = 25 * (n / 10 * 10), rest = total - tensPart;
      bank.add(HARD, story[0] + " menyiapkan " + fmt(total) + " " + story[1] + ". Semuanya dibagi sama rata untuk 25 " + story[2] + ". Berapa " + story[1] + " yang diterima setiap " + story[2] + "?",
          story[1], n, values(total / 5, total * 4, n + 10, n - 10, n + 1, n - 1),
          lines("Cari kelipatan 25 yang dekat.", "25 × " + (n / 10 * 10) + " = " + fmt(tensPart) + ".", "Sisa " + fmt(total) + " − " + fmt(tensPart) + " = " + fmt(rest) + ".", fmt(rest) + " ÷ 25 = " + (n % 10) + ".", (n / 10 * 10) + " + " + (n % 10) + " = " + n + "."),
          lines("÷ 25 sama dengan × 4 lalu dibagi 100.", fmt(total) + " × 4 = " + fmt(total * 4) + ".", fmt(total * 4) + " ÷ 100 = " + n + "."));
    });
    List<Integer> leftovers = new ArrayList<>();
    for (int factor : TIMES_25_FACTORS) leftovers.add(factor);
    bank.take(leftovers, 18, n -> {
      int total = n * 25, given = 25 * (1 + n % 7) + 10, answer = total - given;
      bank.addEssay("Bu Rani membeli " + n + " kotak pensil. Setiap kotak berisi 25 pensil. Sebanyak " + given + " pensil dibagikan kepada siswa. Berapa pensil yang tersisa? Tuliskan langkah-langkahmu.", "pensil", answer,
          times25Long(n) + "\nKurangi yang dibagikan: " + fmt(total) + " − " + given + " = " + fmt(answer) + ".",
          times25Quick(n) + "\nKurangi yang dibagikan: " + fmt(total) + " − " + given + " = " + fmt(answer) + ".");
    });
    bank.take(timesEleven, 20, n -> {
      int rows = n * 11, extra = 5 + n % 30, answer = rows + extra, tens = n / 10, ones = n % 10;
      bank.addEssay("Aula memiliki " + n + " baris kursi. Setiap baris berisi 11 kursi. Lalu ditambah " + extra + " kursi lagi. Berapa kursi di aula seluruhnya? Tuliskan langkah-langkahmu.", "kursi", answer,
          lines("Pecah 11 menjadi 10 + 1.", n + " × 10 = " + fmt(n * 10) + ", ditambah " + n + " = " + fmt(rows) + ".", "Tambah kursi baru: " + fmt(rows) + " + " + extra + " = " + fmt(answer) + "."),
          tens + ones < 10 ? lines("× 11 dua angka: sisipkan " + tens + " + " + ones + " = " + (tens + ones) + " di tengah, hasilnya " + fmt(rows) + ".", "Tambah kursi baru: " + fmt(rows) + " + " + extra + " = " + fmt(answer) + ".") : null);
    });
    return bank.items();
  }

  private static String carryTimesLong(int n, int factor) {
    int ones = n % 10 * factor, carry = ones / 10, tensProduct = n / 10 * factor;
    return lines("Kalikan bersusun, mulai dari satuan.",
        "Satuan: " + (n % 10) + " × " + factor + " = " + ones + (carry == 0 ? "." : ", tulis " + (ones % 10) + ", simpan " + carry + "."),
        "Puluhan: " + (n / 10) + " × " + factor + " = " + tensProduct + (carry == 0 ? "." : ", ditambah " + carry + " = " + (tensProduct + carry) + "."));
  }

  private static int[] times25Distractors(int n) {
    int answer = n * 25, shifted = 25 * (n % 10) + 25 * (n / 10);
    return values(shifted, 25 * (n / 10 * 10), n + 25, answer + 100, answer - 100, answer + 25, answer - 25);
  }

  private static String times25Long(int n) {
    int ones = 25 * (n % 10), tens = 25 * (n / 10 * 10);
    return lines("Pecah " + n + " menjadi " + (n / 10 * 10) + " + " + (n % 10) + ".",
        "25 × " + (n % 10) + " = " + fmt(ones) + ".", "25 × " + (n / 10 * 10) + " = " + fmt(tens) + ".",
        fmt(ones) + " + " + fmt(tens) + " = " + fmt(n * 25) + ".");
  }

  private static String times25Quick(int n) {
    return lines("25 × 4 = 100, dan " + n + " = 4 × " + (n / 4) + ".", "100 × " + (n / 4) + " = " + fmt(n * 25) + ".");
  }

  // ---------- Matematika kelas 5: KPK dan FPB ----------

  private static List<MathItem> lcmAndGcd() {
    Bank bank = new Bank(502L);
    List<int[]> small = validPairs(4, 15, 120), medium = validPairs(16, 60, 240), story = validPairs(6, 30, 180);
    bank.take(small, 30, p -> bank.add(EASY, "KPK dari " + p[0] + " dan " + p[1] + " adalah ...", "", lcm(p[0], p[1]), lcmDistractors(p[0], p[1]), lcmListing(p[0], p[1]), lcmQuick(p[0], p[1])));
    bank.take(small, 30, p -> bank.add(EASY, "FPB dari " + p[0] + " dan " + p[1] + " adalah ...", "", gcd(p[0], p[1]), gcdDistractors(p[0], p[1]), gcdListing(p[0], p[1]), gcdQuick(p[0], p[1])));
    bank.take(medium, 40, p -> bank.add(MEDIUM, "KPK dari " + p[0] + " dan " + p[1] + " adalah ...", "", lcm(p[0], p[1]), lcmDistractors(p[0], p[1]), lcmFactors(p[0], p[1]), lcmQuick(p[0], p[1])));
    bank.take(medium, 40, p -> bank.add(MEDIUM, "FPB dari " + p[0] + " dan " + p[1] + " adalah ...", "", gcd(p[0], p[1]), gcdDistractors(p[0], p[1]), gcdFactors(p[0], p[1]), gcdQuick(p[0], p[1])));

    String[][] lcmStories = {
        {"Lampu A berkedip setiap %d detik dan lampu B setiap %d detik. Keduanya baru saja berkedip bersamaan. Setelah berapa detik keduanya berkedip bersamaan lagi?", "detik"},
        {"Bus A berangkat setiap %d menit dan bus B setiap %d menit. Keduanya baru saja berangkat bersamaan. Setelah berapa menit keduanya berangkat bersamaan lagi?", "menit"},
        {"Alya berenang setiap %d hari dan Bima setiap %d hari. Hari ini mereka berenang bersama. Setelah berapa hari mereka berenang bersama lagi?", "hari"}};
    int[] lcmStory = {0};
    bank.take(story, 30, p -> {
      String[] template = lcmStories[lcmStory[0]++ % lcmStories.length];
      String unit = template[1];
      bank.add(HARD, template[0].formatted(p[0], p[1]), unit, lcm(p[0], p[1]), lcmDistractors(p[0], p[1]),
          prepend("Bersamaan lagi berarti mencari KPK.", List.of(lcmFactors(p[0], p[1]).split("\n"))),
          lcmQuick(p[0], p[1]));
    });
    String[][] gcdStories = {{"Bu Sari", "apel", "jeruk"}, {"Pak Dani", "pensil", "buku"}, {"Bu Rani", "roti", "susu"}, {"Pak Hadi", "kelereng", "stiker"}};
    int[] gcdStory = {0};
    bank.take(story, 30, p -> {
      String[] template = gcdStories[gcdStory[0]++ % gcdStories.length];
      bank.add(HARD, template[0] + " memiliki " + p[0] + " " + template[1] + " dan " + p[1] + " " + template[2] + ". Semuanya dibagi ke dalam bingkisan dengan isi yang sama pada tiap bingkisan. Paling banyak berapa bingkisan yang dapat dibuat?",
          "bingkisan", gcd(p[0], p[1]), gcdDistractors(p[0], p[1]),
          prepend("Paling banyak dan sama rata berarti mencari FPB.", List.of(gcdFactors(p[0], p[1]).split("\n"))),
          gcdQuick(p[0], p[1]));
    });
    bank.take(story, 20, p -> {
      int a = p[0], b = p[1], l = lcm(a, b), times = l / a;
      String quick = lcmQuick(a, b);
      bank.addEssay("Bel A berbunyi setiap " + a + " menit dan bel B setiap " + b + " menit. Keduanya baru saja berbunyi bersamaan. Berapa kali bel A berbunyi sampai keduanya berbunyi bersamaan lagi? Tuliskan langkah-langkahmu.", "kali", times,
          lcmFactors(a, b) + "\nBel A berbunyi " + l + " ÷ " + a + " = " + times + " kali.",
          quick == null ? null : quick + "\nBel A berbunyi " + l + " ÷ " + a + " = " + times + " kali.");
    });
    bank.take(story, 20, p -> {
      int a = p[0], b = p[1], g = gcd(a, b), each = a / g;
      String quick = gcdQuick(a, b);
      bank.addEssay("Bu Sari memiliki " + a + " apel dan " + b + " jeruk. Semuanya dibagi ke dalam bingkisan sebanyak mungkin dengan isi yang sama pada tiap bingkisan. Berapa apel dalam setiap bingkisan? Tuliskan langkah-langkahmu.", "apel", each,
          gcdFactors(a, b) + "\nBanyak bingkisan " + g + ", jadi apel tiap bingkisan " + a + " ÷ " + g + " = " + each + ".",
          quick == null ? null : quick + "\nBanyak bingkisan " + g + ", jadi apel tiap bingkisan " + a + " ÷ " + g + " = " + each + ".");
    });
    return bank.items();
  }

  /** Pairs with a common factor above 1 where neither divides the other, so KPK and FPB are worth working out. */
  private static List<int[]> validPairs(int min, int max, int maxLcm) {
    List<int[]> pairs = new ArrayList<>();
    for (int a = min; a <= max; a++) for (int b = a + 1; b <= max; b++) {
      if (gcd(a, b) > 1 && b % a != 0 && lcm(a, b) <= maxLcm) pairs.add(new int[] {a, b});
    }
    return pairs;
  }

  private static int[] lcmDistractors(int a, int b) {
    int l = lcm(a, b);
    return values(gcd(a, b), a * b, a + b, l + b, l * 2, l + a);
  }

  private static int[] gcdDistractors(int a, int b) {
    int g = gcd(a, b);
    return values(lcm(a, b), a, b - a, g * 2, a + b, g + 1, g * 3);
  }

  private static String lcmListing(int a, int b) {
    int l = lcm(a, b);
    return lines("Kelipatan " + a + ": " + multiples(a, l) + ".", "Kelipatan " + b + ": " + multiples(b, l) + ".", "Kelipatan sama yang terkecil adalah " + l + ".");
  }

  private static String gcdListing(int a, int b) {
    return lines("Faktor " + a + ": " + factors(a) + ".", "Faktor " + b + ": " + factors(b) + ".", "Faktor sama yang terbesar adalah " + gcd(a, b) + ".");
  }

  private static String lcmFactors(int a, int b) {
    List<Integer> shared = new ArrayList<>(primeFactors(a)), other = new ArrayList<>(primeFactors(b));
    for (int prime : primeFactors(a)) other.remove(Integer.valueOf(prime));
    shared.addAll(other);
    Collections.sort(shared);
    return lines(a + " = " + product(primeFactors(a)) + ".", b + " = " + product(primeFactors(b)) + ".",
        "Ambil semua faktor, yang sama dihitung sekali: " + product(shared) + " = " + lcm(a, b) + ".");
  }

  private static String gcdFactors(int a, int b) {
    List<Integer> shared = new ArrayList<>(), other = new ArrayList<>(primeFactors(b));
    for (int prime : primeFactors(a)) if (other.remove(Integer.valueOf(prime))) shared.add(prime);
    return lines(a + " = " + product(primeFactors(a)) + ".", b + " = " + product(primeFactors(b)) + ".",
        "Ambil faktor yang dimiliki keduanya: " + (shared.size() == 1 ? "" : product(shared) + " = ") + gcd(a, b) + ".");
  }

  private static String lcmQuick(int a, int b) {
    int l = lcm(a, b);
    if (l / b > 5) return null;
    return lines("Cukup tulis kelipatan bilangan terbesar: " + multiples(b, l) + ".",
        "Yang pertama habis dibagi " + a + " adalah " + l + ".");
  }

  private static String gcdQuick(int a, int b) {
    int difference = b - a;
    if (a % difference != 0) return null;
    return lines("FPB selalu menjadi faktor dari selisih kedua bilangan.", "Selisih: " + b + " − " + a + " = " + difference + ".",
        difference + " membagi habis " + a + " dan " + b + ".");
  }

  private static String multiples(int n, int upTo) {
    StringJoiner joiner = new StringJoiner(", ");
    for (int value = n; value <= upTo; value += n) joiner.add(Integer.toString(value));
    return joiner.toString();
  }

  private static String factors(int n) {
    StringJoiner joiner = new StringJoiner(", ");
    for (int value = 1; value <= n; value++) if (n % value == 0) joiner.add(Integer.toString(value));
    return joiner.toString();
  }

  private static List<Integer> primeFactors(int n) {
    List<Integer> primes = new ArrayList<>();
    for (int p = 2; n > 1; p++) while (n % p == 0) { primes.add(p); n /= p; }
    return primes;
  }

  private static String product(List<Integer> values) {
    StringJoiner joiner = new StringJoiner(" × ");
    values.forEach(value -> joiner.add(value.toString()));
    return joiner.toString();
  }

  static int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }
  static int lcm(int a, int b) { return a / gcd(a, b) * b; }

  // ---------- Shared helpers ----------

  private static int[] values(int... values) { return values; }

  private static String lines(String... lines) { return String.join("\n", lines); }

  private static String prepend(String first, List<String> rest) { return first + "\n" + String.join("\n", rest); }

  /** Indonesian thousands separator, e.g. 2.400. */
  static String fmt(int value) {
    return value < 1000 ? Integer.toString(value) : (value / 1000) + "." + String.format("%03d", value % 1000);
  }

  /** Collects a chapter's items, rejecting repeats and drawing wrong answers from common mistakes. */
  private static final class Bank {
    private final Random random;
    private final List<MathItem> items = new ArrayList<>();
    private final Set<String> prompts = new HashSet<>();
    private int formNumber;

    Bank(long seed) { random = new Random(seed); }

    <T> void take(List<T> candidates, int count, Consumer<T> add) {
      List<T> shuffled = new ArrayList<>(candidates);
      Collections.shuffle(shuffled, random);
      formNumber++;
      shuffled.stream().limit(count).forEach(add);
    }

    void add(int level, String prompt, String unit, int answer, int[] distractors, String longMethod, String quickMethod) {
      if (!prompts.add(prompt)) throw new IllegalStateException("Soal duplikat: " + prompt);
      String correct = option(answer, unit);
      LinkedHashSet<String> pool = new LinkedHashSet<>();
      for (int value : distractors) if (value > 0 && value != answer) pool.add(option(value, unit));
      List<String> wrong = new ArrayList<>(pool);
      if (wrong.size() < 3) throw new IllegalStateException("Pengecoh kurang: " + prompt);
      Collections.shuffle(wrong, random);
      List<String> options = List.of(correct, wrong.get(0), wrong.get(1), wrong.get(2));
      store(prompt, options, correct, false, level, longMethod, quickMethod);
    }

    void addEssay(String prompt, String unit, int answer, String longMethod, String quickMethod) {
      if (!prompts.add(prompt)) throw new IllegalStateException("Soal duplikat: " + prompt);
      String correct = option(answer, unit);
      store(prompt, List.of(correct), correct, true, HARD, longMethod, quickMethod);
    }

    private void store(String prompt, List<String> options, String correct, boolean essay, int level, String longMethod, String quickMethod) {
      String conclusion = "Jadi, jawabannya " + correct + ".";
      String fullLong = longMethod + "\n" + conclusion;
      String fullQuick = quickMethod == null ? null : quickMethod + "\n" + conclusion;
      items.add(new MathItem(prompt, options, 0, fullLong, essay, level, fullLong, fullQuick, "f" + formNumber));
    }

    List<MathItem> items() { return List.copyOf(items); }

    private static String option(int value, String unit) { return unit.isEmpty() ? fmt(value) : fmt(value) + " " + unit; }
  }
}
