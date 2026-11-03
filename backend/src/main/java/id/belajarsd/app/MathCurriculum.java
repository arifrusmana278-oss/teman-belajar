package id.belajarsd.app;

import static id.belajarsd.app.MathQuestionBank.gcd;
import static id.belajarsd.app.MathQuestionBank.lcm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Question banks for every math chapter that {@link MathQuestionBank} does not write by hand.
 * A chapter is planned as a mix of topics; each topic offers several question forms per level, and the
 * builder takes from them in turn, up to 240 questions, so a chapter stays varied. Seeds derive from the chapter
 * name, which keeps a bank identical across restarts.
 */
final class MathCurriculum {
  private static final int EASY = 1, MEDIUM = 2, HARD = 3, ESSAY = 4;
  private static final int[] MINIMUM = {0, 15, 20, 15, 10}, MAXIMUM = {0, 60, 80, 60, 40};
  private static final Pattern CHAPTER_TITLE = Pattern.compile("^Bab \\d+ — (.+)$");
  private static final String[] NAMES = {"Alya", "Bima", "Citra", "Doni", "Eka", "Farah", "Gita", "Hadi", "Intan", "Joko"};

  private record Candidate(String prompt, List<String> options, String answer, String longMethod, String quickMethod) {}
  private record Form(int level, List<Candidate> candidates) {}

  private final boolean en;
  private final Random random;
  private final List<Form> forms = new ArrayList<>();

  private MathCurriculum(boolean en, long seed) { this.en = en; this.random = new Random(seed); }

  static List<MathQuestionBank.MathItem> forChapter(String subject, String grade, String title) {
    Matcher chapter = CHAPTER_TITLE.matcher(title);
    if (!chapter.matches() || !grade.startsWith("Kelas ")) return List.of();
    MathCurriculum curriculum = new MathCurriculum(subject.equals("Mathematics in English"), (subject + grade + title).hashCode());
    return curriculum.plan(subject + "|" + grade.substring(6) + "|" + chapter.group(1)) ? curriculum.build() : List.of();
  }

  private boolean plan(String key) {
    switch (key) {
      case "Matematika|1|Bilangan dan nilai tempat", "Hitung Cepat|1|Membilang dan mengenal angka", "Mathematics in English|1|Numbers and place value" -> placeValue(100);
      case "Matematika|1|Penjumlahan dan pengurangan" -> smallAddSub(true, true);
      case "Matematika|1|Bentuk dan posisi" -> shapes(true);
      case "Matematika|1|Pola dan pengukuran" -> { pattern(1); firstMeasures(); }
      case "Matematika|2|Bilangan cacah", "Hitung Cepat|2|Nilai tempat", "Mathematics in English|2|Numbers" -> placeValue(1000);
      case "Matematika|2|Perkalian dan pembagian", "Hitung Cepat|2|Perkalian awal", "Mathematics in English|2|Multiplication" -> mulDiv(1);
      case "Matematika|2|Pengukuran dan bangun datar", "Mathematics in English|2|Measurement" -> { conversions(true); perimeter(true); }
      case "Matematika|3|Bilangan cacah" -> placeValue(10000);
      case "Matematika|3|Kalimat matematika" -> sentences();
      case "Matematika|3|Perkalian dan pembagian", "Hitung Cepat|3|Fakta perkalian", "Hitung Cepat|3|Pembagian sebagai pengelompokan", "Mathematics in English|3|Multiplication and division" -> mulDiv(2);
      case "Matematika|3|Panjang, berat, dan bangun datar", "Mathematics in English|3|Shapes and measurement" -> { conversions(false); perimeter(false); shapes(false); }
      case "Matematika|4|Bilangan cacah sampai 10.000", "Mathematics in English|4|Whole numbers" -> { placeValue(10000); addSub(1000, 9999); }
      case "Matematika|4|Pecahan", "Hitung Cepat|4|Pecahan sederhana", "Mathematics in English|3|Fractions", "Mathematics in English|4|Fractions" -> fractions(1);
      case "Matematika|4|Pola gambar dan pola bilangan", "Hitung Cepat|3|Pola hitung", "Mathematics in English|4|Patterns" -> pattern(2);
      case "Matematika|4|Pengukuran luas dan volume", "Mathematics in English|4|Measurement and geometry" -> { area(false); volume(1); }
      case "Matematika|4|Bangun datar" -> { perimeter(false); area(false); shapes(false); }
      case "Matematika|4|Piktogram dan diagram batang" -> data(1);
      case "Matematika|5|Bilangan cacah sampai 100.000", "Mathematics in English|5|Large numbers" -> { placeValue(100000); addSub(10000, 99999); }
      case "Matematika|5|Bilangan pecahan", "Hitung Cepat|5|Operasi pecahan" -> fractions(2);
      case "Matematika|5|Keliling bangun datar" -> perimeter(false);
      case "Matematika|5|Luas daerah bangun datar" -> area(true);
      case "Matematika|5|Sudut" -> angles();
      case "Matematika|5|Membandingkan ciri-ciri bangun datar" -> { shapes(false); perimeter(false); }
      case "Matematika|5|Data" -> data(2);
      case "Matematika|5|Bilangan cacah sampai 1.000.000" -> { placeValue(1000000); addSub(100000, 999999); }
      case "Matematika|6|Pecahan dan desimal", "Mathematics in English|5|Fractions and decimals", "Mathematics in English|6|Fractions and decimals" -> { decimals(); fractions(2); }
      case "Matematika|6|Rasio", "Hitung Cepat|6|Rasio dan skala", "Mathematics in English|6|Ratios" -> ratio();
      case "Matematika|6|Kubus dan balok", "Mathematics in English|6|Volume" -> volume(2);
      case "Matematika|6|Peluang", "Mathematics in English|6|Probability" -> probability();
      case "Hitung Cepat|1|Strategi penjumlahan", "Mathematics in English|1|Addition" -> smallAddSub(true, false);
      case "Hitung Cepat|1|Strategi pengurangan", "Mathematics in English|1|Subtraction" -> smallAddSub(false, true);
      case "Hitung Cepat|1|Pola bilangan sederhana" -> pattern(1);
      case "Matematika|2|Penjumlahan dan pengurangan", "Hitung Cepat|2|Penjumlahan cepat", "Hitung Cepat|2|Pengurangan cepat", "Mathematics in English|2|Addition and subtraction" -> addSub(10, 99);
      case "Hitung Cepat|3|Operasi campuran", "Hitung Cepat|6|Operasi campuran" -> mixedOperations();
      case "Hitung Cepat|4|Operasi bilangan besar" -> addSub(1000, 9999);
      case "Hitung Cepat|4|Strategi pemecahan masalah", "Mathematics in English|3|Numbers and operations" -> { mixedOperations(); mulDiv(3); }
      case "Hitung Cepat|5|Desimal dan persen", "Hitung Cepat|6|Persentase" -> decimals();
      case "Hitung Cepat|5|KPK dan FPB", "Mathematics in English|5|Factors and multiples" -> lcmAndGcd();
      case "Hitung Cepat|5|Estimasi dan hitung mental", "Hitung Cepat|6|Estimasi dan strategi" -> estimation();
      case "Mathematics in English|1|Shapes and patterns" -> { shapes(true); pattern(1); }
      case "Mathematics in English|5|Geometry and data" -> { area(true); data(2); }
      default -> { return false; }
    }
    return true;
  }

  // ---------- Numbers and place value ----------

  private void placeValue(int max) {
    int digits = String.valueOf(max).length() - 1, low = pow10(digits - 1);
    Form value = form(EASY), expanded = form(MEDIUM), more = form(MEDIUM), arrange = form(HARD), largest = form(HARD), gap = form(ESSAY), bigAndSmall = form(ESSAY);
    for (int i = 0; i < 400; i++) {
      int n = between(low, max - 1), position = between(0, digits - 1), digit = n / pow10(position) % 10;
      if (digit == 0 || !allDifferent(n)) continue;
      int answer = digit * pow10(position);
      numeric(value, T("Nilai angka " + digit + " pada bilangan " + fmt(n) + " adalah ...", "What is the value of the digit " + digit + " in the number " + fmt(n) + "?"), "", answer,
          lines(T("Angka " + digit + " berada di tempat " + place(position) + ".", "The digit " + digit + " is in the " + place(position) + " place."), digit + " × " + fmt(pow10(position)) + " = " + fmt(answer) + "."),
          T("Ganti semua angka di kanan " + digit + " dengan nol.", "Replace every digit to the right of " + digit + " with zero."),
          digit, answer * 10, answer / 10, n);
      StringJoiner parts = new StringJoiner(" + ");
      for (int p = digits - 1; p >= 0; p--) if (n / pow10(p) % 10 != 0) parts.add(fmt(n / pow10(p) % 10 * pow10(p)));
      if (parts.length() > String.valueOf(n).length()) numeric(expanded, parts + " = ...", "", n,
          lines(T("Tulis tiap nilai sesuai tempatnya.", "Write each value in its place."), T("Gabungkan angkanya menjadi ", "Put the digits together to get ") + fmt(n) + "."), null,
          reverseDigits(n), n + pow10(digits - 1) <= max ? n + pow10(digits - 1) : n - pow10(digits - 1), n + 10, n - 10);
      int step = pow10(between(0, digits - 1));
      if (n + step < max) numeric(more, T("Bilangan yang " + fmt(step) + " lebih besar dari " + fmt(n) + " adalah ...", "What number is " + fmt(step) + " more than " + fmt(n) + "?"), "", n + step,
          fmt(n) + " + " + fmt(step) + " = " + fmt(n + step) + ".", null, n - step, n + 2 * step, n + step + (step == 1 ? 10 : 1), n);
      int second = between(0, digits - 1), other = n / pow10(second) % 10;
      if (second != position && other != 0) {
        int a = answer, b = other * pow10(second), difference = Math.abs(a - b);
        essay(gap, T("Pada bilangan " + fmt(n) + ", berapa selisih nilai angka " + digit + " dan nilai angka " + other + "?", "In the number " + fmt(n) + ", what is the difference between the value of the digit " + digit + " and the value of the digit " + other + "?"),
            fmt(difference), lines(T("Nilai angka " + digit + " adalah " + fmt(a) + ".", "The digit " + digit + " has the value " + fmt(a) + "."), T("Nilai angka " + other + " adalah " + fmt(b) + ".", "The digit " + other + " has the value " + fmt(b) + "."), fmt(Math.max(a, b)) + " − " + fmt(Math.min(a, b)) + " = " + fmt(difference) + "."), null);
      }
      int[] set = {between(low, max - 1), between(low, max - 1), between(low, max - 1), n};
      int top = Math.max(Math.max(set[0], set[1]), Math.max(set[2], set[3]));
      if (new HashSet<>(List.of(set[0], set[1], set[2], set[3])).size() == 4) {
        List<String> wrong = new ArrayList<>();
        for (int s : set) if (s != top) wrong.add(fmt(s));
        text(largest, T("Bilangan terbesar di antara " + fmt(set[0]) + "; " + fmt(set[1]) + "; " + fmt(set[2]) + "; dan " + fmt(set[3]) + " adalah ...", "Which is the largest among " + fmt(set[0]) + "; " + fmt(set[1]) + "; " + fmt(set[2]) + "; and " + fmt(set[3]) + "?"),
            fmt(top), wrong, T("Bandingkan dari angka paling kiri. Bilangan dengan angka terdepan lebih besar adalah yang terbesar.", "Compare from the leftmost digit. The number with the bigger leading digit is the largest."), null);
      }
    }
    int count = Math.min(digits, 4);
    for (int i = 0; i < 400; i++) {
      List<Integer> ds = new ArrayList<>();
      while (ds.size() < count) { int d = between(1, 9); if (!ds.contains(d)) ds.add(d); }
      StringJoiner shown = new StringJoiner(", ");
      ds.forEach(d -> shown.add(d.toString()));
      List<Integer> sorted = new ArrayList<>(ds);
      Collections.sort(sorted);
      int small = 0, big = 0;
      for (int k = 0; k < count; k++) { small = small * 10 + sorted.get(k); big = big * 10 + sorted.get(count - 1 - k); }
      essay(bigAndSmall, T("Dari angka " + shown + ", berapa jumlah bilangan terbesar dan bilangan terkecil yang dapat disusun?", "From the digits " + shown + ", what is the sum of the largest and the smallest number you can make?"), fmt(big + small), lines(T("Bilangan terbesar: ", "Largest number: ") + fmt(big) + ".", T("Bilangan terkecil: ", "Smallest number: ") + fmt(small) + ".", fmt(big) + " + " + fmt(small) + " = " + fmt(big + small) + "."), null);
      numeric(arrange, T("Bilangan terbesar yang dapat disusun dari angka " + shown + " adalah ...", "What is the largest number that can be made from the digits " + shown + "?"), "", big,
          T("Urutkan angkanya dari yang terbesar sampai terkecil.", "Order the digits from largest to smallest."), null, small, reverseDigits(big) == small ? big - 9 : reverseDigits(big), big - 1, small + 9);
      numeric(arrange, T("Bilangan terkecil yang dapat disusun dari angka " + shown + " adalah ...", "What is the smallest number that can be made from the digits " + shown + "?"), "", small,
          T("Urutkan angkanya dari yang terkecil sampai terbesar.", "Order the digits from smallest to largest."), null, big, small + 9, small + 1, big - 9);
    }
  }

  // ---------- Addition and subtraction ----------

  private void addSub(int lo, int hi) {
    int unit = Math.max(10, pow10(String.valueOf(hi).length() - 2));
    Form easyAdd = form(EASY), easySub = form(EASY), sign = form(EASY), mediumAdd = form(MEDIUM), mediumSub = form(MEDIUM), same = form(MEDIUM), story = form(HARD), missing = form(HARD), loans = form(ESSAY), journeys = form(ESSAY), shelves = form(ESSAY);
    for (int i = 0; i < 700; i++) {
      int k = between(1, 3), b = between(1, Math.max(1, hi / unit - 1)) * unit - k;
      if (b < lo / 2 || b <= 0) continue;
      int a = between(lo, Math.max(lo, hi - b));
      if (a + b <= hi) {
        Form target = a + b <= (lo + hi) / 2 ? easyAdd : mediumAdd;
        numeric(target, fmt(a) + " + " + fmt(b) + " = ...", "", a + b, columnAdd(a, b), addQuick(a, b, k), a + b + k, a + b + 2 * k, a + b - 10, a + b + 10, a + b - unit);
        int sum = a + b;
        signQuestion(sign, fmt(a) + " + " + fmt(b), sum, sum + (i % 3 - 1) * unit);
        if (b > unit && a > unit) sameValueQuestion(same, fmt(a) + " + " + fmt(b), sum, fmt(a + unit) + " + " + fmt(b - unit), List.of(fmt(a + unit) + " + " + fmt(b), fmt(a) + " + " + fmt(b - unit), fmt(a + unit) + " + " + fmt(b + unit)), T("Jika satu bilangan ditambah " + fmt(unit) + " dan yang lain dikurangi " + fmt(unit) + ", hasilnya tetap.", "If one number gains " + fmt(unit) + " and the other loses " + fmt(unit) + ", the total stays the same."));
        numeric(target == easyAdd ? easySub : mediumSub, fmt(sum) + " − " + fmt(b) + " = ...", "", a, columnSub(sum, b), subQuick(sum, b, k), a - k, a - 2 * k, a + 10, a - 10, a + unit);
        numeric(missing, "... + " + fmt(b) + " = " + fmt(sum) + T(". Bilangan yang tepat adalah ...", ". What is the missing number?"), "", a,
            lines(T("Bilangan yang hilang adalah " + fmt(sum) + " − " + fmt(b) + ".", "The missing number is " + fmt(sum) + " − " + fmt(b) + "."), columnSub(sum, b)),
            lines(T("Bilangan yang hilang adalah " + fmt(sum) + " − " + fmt(b) + ".", "The missing number is " + fmt(sum) + " − " + fmt(b) + "."), subQuick(sum, b, k)), sum + b, a + 10, a - 10, a - k);
        int c = between(Math.max(1, b / 2), Math.max(1, b - 1)), now = sum - c;
        String name = name(i), object = T("kelereng", "marbles"), storyText;
        switch (i % 4) {
          case 0 -> storyText = T(name + " memiliki " + fmt(a) + " " + object + ". Ibu memberi " + fmt(b) + " " + object + " lagi. Lalu " + name + " memberikan " + fmt(c) + " " + object + " kepada adik. Berapa " + object + " " + name + " sekarang?", name + " has " + fmt(a) + " " + object + ". Mother gives " + fmt(b) + " more " + object + ". Then " + name + " gives " + fmt(c) + " " + object + " to a sibling. How many " + object + " does " + name + " have now?");
          case 1 -> { object = T("penumpang", "passengers"); storyText = T("Sebuah bus berisi " + fmt(a) + " penumpang. Di halte, " + fmt(b) + " penumpang naik, lalu " + fmt(c) + " penumpang turun. Berapa penumpang di bus sekarang?", "A bus has " + fmt(a) + " passengers. At a stop, " + fmt(b) + " passengers get on, then " + fmt(c) + " get off. How many passengers are on the bus now?"); }
          case 2 -> { object = T("jeruk", "oranges"); storyText = T("Kebun memanen " + fmt(a) + " jeruk pada pagi hari dan " + fmt(b) + " jeruk pada sore hari. Sebanyak " + fmt(c) + " jeruk dijual. Berapa jeruk yang belum dijual?", "A farm picks " + fmt(a) + " oranges in the morning and " + fmt(b) + " in the afternoon, then sells " + fmt(c) + ". How many oranges are not sold?"); }
          default -> { object = T("buku", "books"); storyText = T("Toko memiliki " + fmt(a) + " buku. Hari ini datang " + fmt(b) + " buku baru dan terjual " + fmt(c) + " buku. Berapa buku di toko sekarang?", "A shop has " + fmt(a) + " books. Today " + fmt(b) + " new books arrive and " + fmt(c) + " are sold. How many books does the shop have now?"); }
        }
        essay(journeys, storyText, fmt(now) + " " + object, lines(fmt(a) + " + " + fmt(b) + " = " + fmt(sum) + ".", fmt(sum) + " − " + fmt(c) + " = " + fmt(now) + "."),
            lines(fmt(b) + " − " + fmt(c) + " = " + fmt(b - c) + ".", fmt(a) + " + " + fmt(b - c) + " = " + fmt(now) + "."));
        if (2 * a + c <= hi) essay(shelves, T("Rak A berisi " + fmt(a) + " buku. Rak B berisi " + fmt(c) + " buku lebih banyak daripada rak A. Berapa buku di kedua rak seluruhnya?", "Shelf A holds " + fmt(a) + " books. Shelf B holds " + fmt(c) + " more books than shelf A. How many books are on both shelves?"), fmt(2 * a + c) + " " + T("buku", "books"),
            lines(fmt(a) + " + " + fmt(c) + " = " + fmt(a + c) + ".", fmt(a) + " + " + fmt(a + c) + " = " + fmt(2 * a + c) + "."), lines(fmt(a) + " + " + fmt(a) + " = " + fmt(2 * a) + ".", fmt(2 * a) + " + " + fmt(c) + " = " + fmt(2 * a + c) + "."));
        numeric(story, storyText, object, now,
            lines(fmt(a) + " + " + fmt(b) + " = " + fmt(sum) + ".", fmt(sum) + " − " + fmt(c) + " = " + fmt(now) + "."),
            lines(T("Hitung dulu selisih yang diterima dan diberikan.", "First find the difference between what was received and given."), fmt(b) + " − " + fmt(c) + " = " + fmt(b - c) + ".", fmt(a) + " + " + fmt(b - c) + " = " + fmt(now) + "."),
            a + b + c, sum, now + 10, now - 10);
        int returned = between(Math.max(1, b / 2), Math.max(1, b - 1)), start = Math.max(sum, b + 1), left = start - b, finalCount = left + returned;
        essay(loans, T("Perpustakaan memiliki " + fmt(start) + " buku. Sebanyak " + fmt(b) + " buku dipinjam, lalu " + fmt(returned) + " buku dikembalikan. Berapa buku di perpustakaan sekarang?", "A library has " + fmt(start) + " books. " + fmt(b) + " books are borrowed, then " + fmt(returned) + " books are returned. How many books are in the library now?"),
            fmt(finalCount) + " " + T("buku", "books"), lines(fmt(start) + " − " + fmt(b) + " = " + fmt(left) + ".", fmt(left) + " + " + fmt(returned) + " = " + fmt(finalCount) + "."),
            lines(T("Hitung buku yang belum kembali.", "Find the books not yet returned."), fmt(b) + " − " + fmt(returned) + " = " + fmt(b - returned) + ".", fmt(start) + " − " + fmt(b - returned) + " = " + fmt(finalCount) + "."));
      }
    }
  }

  private String addQuick(int a, int b, int k) {
    int rounded = b + k;
    return lines(T("Bulatkan: ", "Round: ") + fmt(b) + " = " + fmt(rounded) + " − " + k + ".", fmt(a) + " + " + fmt(rounded) + " = " + fmt(a + rounded) + ".", fmt(a + rounded) + " − " + k + " = " + fmt(a + b) + ".");
  }

  private String subQuick(int sum, int b, int k) {
    int rounded = b + k;
    return lines(T("Bulatkan: ", "Round: ") + fmt(b) + " = " + fmt(rounded) + " − " + k + ".", fmt(sum) + " − " + fmt(rounded) + " = " + fmt(sum - rounded) + ".",
        T("Terlalu banyak dikurangi " + k + ", jadi ", "That took away " + k + " too many, so ") + fmt(sum - rounded) + " + " + k + " = " + fmt(sum - b) + ".");
  }

  private String columnAdd(int a, int b) {
    List<String> steps = new ArrayList<>(List.of(T("Susun bersusun, mulai dari satuan.", "Line up the digits and start from the ones.")));
    int carry = 0;
    for (int p = 0; a / pow10(p) > 0 || b / pow10(p) > 0 || carry > 0; p++) {
      int x = a / pow10(p) % 10, y = b / pow10(p) % 10, total = x + y + carry;
      String sum = carry > 0 ? x + " + " + y + " + " + carry : x + " + " + y;
      steps.add(capital(place(p)) + ": " + sum + " = " + total + (total >= 10 ? T(", tulis " + total % 10 + ", simpan 1.", ", write " + total % 10 + ", carry 1.") : "."));
      carry = total / 10;
    }
    return String.join("\n", steps);
  }

  private String columnSub(int a, int b) {
    List<String> steps = new ArrayList<>(List.of(T("Susun bersusun, mulai dari satuan.", "Line up the digits and start from the ones.")));
    int borrow = 0;
    for (int p = 0; a / pow10(p) > 0; p++) {
      int x = a / pow10(p) % 10 - borrow, y = b / pow10(p) % 10;
      String note = borrow > 0 ? T(" (sudah dipinjam 1)", " (1 was borrowed)") : "";
      if (x < y) { steps.add(capital(place(p)) + note + T(": pinjam 1, ", ": borrow 1, ") + (x + 10) + " − " + y + " = " + (x + 10 - y) + "."); borrow = 1; }
      else { steps.add(capital(place(p)) + note + ": " + x + " − " + y + " = " + (x - y) + "."); borrow = 0; }
    }
    return String.join("\n", steps);
  }

  private void smallAddSub(boolean add, boolean subtract) {
    Form easy = form(EASY), sign = form(EASY), medium = form(MEDIUM), story = form(HARD), missing = form(HARD), steps = form(ESSAY), prizes = form(ESSAY);
    for (int a = 1; a <= 18; a++) for (int b = 1; b <= 9; b++) {
      int sum = a + b;
      if (sum > 20) continue;
      if (add) signQuestion(sign, a + " + " + b, sum, sum + (a + b) % 3 - 1);
      if (subtract) signQuestion(sign, sum + " − " + b, a, a + (a + b) % 3 - 1);
      if (add) {
        String counting = lines(T("Mulai dari " + a + ", hitung maju " + b + " langkah.", "Start at " + a + " and count on " + b + " steps."), a + " + " + b + " = " + sum + ".");
        String ten = a < 10 && sum > 10 ? lines(T("Lengkapi " + a + " menjadi 10.", "Make " + a + " up to 10."), a + " + " + (10 - a) + " = 10.", "10 + " + (sum - 10) + " = " + sum + ".") : null;
        numeric(sum <= 10 ? easy : medium, a + " + " + b + " = ...", "", sum, counting, ten, sum - 1, sum + 1, sum - 2, sum + 2, Math.abs(a - b), a, b);
        numeric(missing, "... + " + b + " = " + sum + T(". Bilangan yang tepat adalah ...", ". What is the missing number?"), "", a, lines(T("Bilangan yang hilang adalah " + sum + " − " + b + ".", "The missing number is " + sum + " − " + b + "."), sum + " − " + b + " = " + a + "."), null, sum + b, a + 1, a + 2, b == a ? a + 3 : b);
        numeric(story, T(name(a) + " memiliki " + a + " stiker. Ibu memberi " + b + " stiker lagi. Berapa stiker " + name(a) + " sekarang?", name(a) + " has " + a + " stickers. Mother adds " + b + " more stickers. How many stickers are there now?"), T("stiker", "stickers"), sum, a + " + " + b + " = " + sum + ".", ten, Math.abs(a - b), sum + 1, sum - 1, sum + 2, sum - 2, a, b);
      }
      if (subtract) {
        String counting = lines(T("Mulai dari " + sum + ", hitung mundur " + b + " langkah.", "Start at " + sum + " and count back " + b + " steps."), sum + " − " + b + " = " + a + ".");
        String ten = sum > 10 && a < 10 ? lines(T("Turunkan dulu ke 10.", "First go down to 10."), sum + " − " + (sum - 10) + " = 10.", "10 − " + (b - (sum - 10)) + " = " + a + ".") : null;
        numeric(sum <= 10 ? easy : medium, sum + " − " + b + " = ...", "", a, counting, ten, a - 1, a + 1, a - 2, a + 2, sum + b, b, sum);
        numeric(missing, sum + " − ... = " + a + T(". Bilangan yang tepat adalah ...", ". What is the missing number?"), "", b, lines(T("Bilangan yang hilang adalah " + sum + " − " + a + ".", "The missing number is " + sum + " − " + a + "."), sum + " − " + a + " = " + b + "."), null, sum + a, b + 1, b + 2, b - 1);
        numeric(story, T(name(a) + " memiliki " + sum + " permen. Sebanyak " + b + " permen dimakan. Berapa permen yang tersisa?", name(a) + " has " + sum + " sweets and eats " + b + " of them. How many sweets are left?"), T("permen", "sweets"), a, sum + " − " + b + " = " + a + ".", ten, sum + b, a + 1, a - 1, a + 2, a - 2, b, sum);
      }
      for (int c = 1; c <= 5 && sum + c <= 20; c++) essay(prizes, T(name(a) + " mendapat " + a + " bintang pada hari pertama, " + b + " bintang pada hari kedua, dan " + c + " bintang pada hari ketiga. Berapa bintang seluruhnya?", name(a) + " earns " + a + " stars on day one, " + b + " stars on day two and " + c + " stars on day three. How many stars are there in total?"), (sum + c) + " " + T("bintang", "stars"),
          lines(a + " + " + b + " = " + sum + ".", sum + " + " + c + " = " + (sum + c) + "."), null);
      for (int c = 1; c < sum; c++) {
        int result = sum - c;
        if (result < 1 || c > 9) continue;
        essay(steps, T(name(c) + " mengumpulkan " + a + " daun, lalu " + b + " daun lagi. Sebanyak " + c + " daun terbang. Berapa daun yang masih ada?", name(c) + " collects " + a + " leaves, then " + b + " more leaves. " + c + " leaves blow away. How many leaves are still there?"), result + " " + T("daun", "leaves"),
            lines(a + " + " + b + " = " + sum + ".", sum + " − " + c + " = " + result + "."), null);
      }
    }
  }

  // ---------- Multiplication and division ----------

  private void mulDiv(int stage) {
    int[] tables = stage == 1 ? new int[] {2, 3, 4, 5, 10} : new int[] {2, 3, 4, 5, 6, 7, 8, 9};
    Form times = form(EASY), sign = form(EASY), divide = form(MEDIUM), same = form(MEDIUM), larger = form(MEDIUM), groups = form(HARD), rows = form(HARD), share = form(HARD), left = form(ESSAY), empty = form(ESSAY), remainder = form(ESSAY);
    for (int t : tables) for (int m = 2; m <= (stage == 1 ? 10 : 12); m++) {
      int product = t * m;
      signQuestion(sign, m + " × " + t, product, product + (m % 3 - 1) * t);
      if (m != t && m + t != product) sameValueQuestion(same, m + " × " + t, product, t + " × " + m, List.of((m + 1) + " × " + t, m + " × " + (t + 1), (m + t) + " × 1"), T("Urutan perkalian boleh ditukar, hasilnya tetap.", "Swapping the order of a multiplication keeps the answer."));
      numeric(rows, T("Aula memiliki " + m + " baris kursi. Setiap baris berisi " + t + " kursi. Berapa kursi seluruhnya?", "A hall has " + m + " rows of chairs. Each row has " + t + " chairs. How many chairs are there in total?"), T("kursi", "chairs"), product, timesLong(m, t), timesQuick(m, t), m + t, product + t, product - t, product + m);
      numeric(rows, T("Ibu membeli " + m + " keranjang apel. Setiap keranjang berisi " + t + " apel. Berapa apel seluruhnya?", "Mother buys " + m + " baskets of apples. Each basket has " + t + " apples. How many apples are there in total?"), T("apel", "apples"), product, timesLong(m, t), timesQuick(m, t), m + t, product + t, product - t, product + m);
      numeric(times, m + " × " + t + " = ...", "", product, timesLong(m, t), timesQuick(m, t), product + t, product - t, m + t, product + m);
      numeric(divide, product + " ÷ " + t + " = ...", "", m, divideLong(product, t, m), lines(T("Ingat perkalian: ", "Use the times table: ") + t + " × " + m + " = " + product + "."), m + 1, m - 1, product - t, t == m ? m + 2 : t);
      String box = T("kotak", "boxes"), pencil = T("pensil", "pencils");
      numeric(groups, T(name(m) + " membeli " + m + " kotak pensil. Setiap kotak berisi " + t + " pensil. Berapa banyak pensil seluruhnya?", name(m) + " buys " + m + " " + box + " of pencils. Each box holds " + t + " pencils. How many pencils are there in total?"), pencil, product, timesLong(m, t), timesQuick(m, t), m + t, product + t, product - t, product + m);
      numeric(share, T("Guru menyiapkan " + product + " buku. Semuanya dibagi sama rata untuk " + t + " kelompok. Berapa buku yang diterima setiap kelompok?", "A teacher prepares " + product + " books and shares them equally among " + t + " groups. How many books does each group get?"), T("buku", "books"), m, divideLong(product, t, m), T("Ingat perkalian: ", "Use the times table: ") + t + " × " + m + " = " + product + ".", m + 1, m - 1, product - t, t == m ? m + 2 : t);
      for (int e = 1; e < t; e++) essay(remainder, T((product + e) + " stiker dibagi kepada " + t + " anak sama banyak dan tersisa " + e + " stiker. Berapa stiker yang diterima setiap anak?", (product + e) + " stickers are shared equally among " + t + " children with " + e + " left over. How many stickers does each child get?"), m + " " + T("stiker", "stickers"),
          lines((product + e) + " − " + e + " = " + product + ".", product + " ÷ " + t + " = " + m + "."), null);
      for (int seat = 1; seat < product; seat += Math.max(1, product / 3)) essay(empty, T("Aula memiliki " + m + " baris kursi. Setiap baris berisi " + t + " kursi, dan " + seat + " kursi masih kosong. Berapa kursi yang terisi?", "A hall has " + m + " rows of chairs. Each row has " + t + " chairs, and " + seat + " chairs are empty. How many chairs are taken?"), (product - seat) + " " + T("kursi", "chairs"),
          lines(m + " × " + t + " = " + product + ".", product + " − " + seat + " = " + (product - seat) + "."), null);
      for (int given = 1; given < product; given += Math.max(1, product / 4)) {
        essay(left, T(name(given) + " membeli " + m + " kotak pensil. Setiap kotak berisi " + t + " pensil. Sebanyak " + given + " pensil dibagikan. Berapa pensil yang tersisa?", name(given) + " buys " + m + " boxes of pencils. Each box holds " + t + " pencils. " + given + " pencils are given away. How many pencils are left?"), (product - given) + " " + pencil,
            lines(m + " × " + t + " = " + product + ".", product + " − " + given + " = " + (product - given) + "."), null);
      }
    }
    if (stage >= 2) for (int i = 0; i < 300; i++) {
      int n = between(12, stage == 2 ? 49 : 99), d = between(2, 9), product = n * d;
      if (n % 10 == 0) continue;
      numeric(larger, n + " × " + d + " = ...", "", product, splitTimes(n, d), d == 5 || d == 9 || d == 4 || d == 2 ? timesQuick(n, d) : null, product + d, product - d, (n / 10 * d) * 10 + n % 10, product + 10, product - 10);
      if (stage == 3) numeric(larger, fmt(product) + " ÷ " + d + " = ...", "", n, splitDivide(product, n, d), null, n + 1, n - 1, n + 10, n - 10);
    }
  }

  private String timesLong(int m, int t) {
    if (m <= 5) {
      StringJoiner sum = new StringJoiner(" + ");
      for (int i = 0; i < m; i++) sum.add(Integer.toString(t));
      return lines(T("Perkalian adalah penjumlahan berulang.", "Multiplication is repeated addition."), sum + " = " + m * t + ".");
    }
    int rest = m - 5;
    return lines(T("Pecah " + m + " menjadi 5 + " + rest + ".", "Split " + m + " into 5 + " + rest + "."), "5 × " + t + " = " + 5 * t + ".", rest + " × " + t + " = " + rest * t + ".", 5 * t + " + " + rest * t + " = " + m * t + ".");
  }

  private String timesQuick(int m, int t) {
    int product = m * t;
    return switch (t) {
      case 2 -> T("× 2 berarti menggandakan: ", "× 2 means double: ") + m + " + " + m + " = " + product + ".";
      case 4 -> lines(T("× 4 berarti menggandakan dua kali.", "× 4 means double twice."), m + " + " + m + " = " + 2 * m + ".", 2 * m + " + " + 2 * m + " = " + product + ".");
      case 5 -> lines(T("× 5 sama dengan × 10 lalu dibagi 2.", "× 5 is the same as × 10 then divide by 2."), m + " × 10 = " + 10 * m + ".", 10 * m + " ÷ 2 = " + product + ".");
      case 9 -> lines(T("× 9 sama dengan × 10 dikurangi bilangan itu.", "× 9 is the same as × 10 minus the number."), m + " × 10 = " + 10 * m + ".", 10 * m + " − " + m + " = " + product + ".");
      case 10 -> T("× 10 berarti menambah satu nol di belakang.", "× 10 means writing one zero at the end.");
      default -> null;
    };
  }

  private String divideLong(int product, int t, int m) {
    StringJoiner steps = new StringJoiner(", ");
    for (int value = product; value >= 0; value -= t) steps.add(Integer.toString(value));
    return lines(T("Kurangi " + t + " berulang sampai habis: ", "Keep taking away " + t + " until nothing is left: ") + steps + ".", T("Pengurangan dilakukan " + m + " kali.", "That is " + m + " subtractions."));
  }

  private String splitTimes(int n, int d) {
    int tens = n / 10 * 10, ones = n % 10;
    return lines(T("Pecah " + n + " menjadi " + tens + " + " + ones + ".", "Split " + n + " into " + tens + " + " + ones + "."), tens + " × " + d + " = " + tens * d + ".", ones + " × " + d + " = " + ones * d + ".", fmt(tens * d) + " + " + ones * d + " = " + fmt(n * d) + ".");
  }

  private String splitDivide(int product, int n, int d) {
    int tens = n / 10 * 10, ones = n % 10;
    return lines(T("Pecah " + fmt(product) + " menjadi " + fmt(tens * d) + " + " + ones * d + ".", "Split " + fmt(product) + " into " + fmt(tens * d) + " + " + ones * d + "."), fmt(tens * d) + " ÷ " + d + " = " + tens + ".", ones * d + " ÷ " + d + " = " + ones + ".", tens + " + " + ones + " = " + n + ".");
  }

  // ---------- Mixed operations and number sentences ----------

  private void mixedOperations() {
    Form easy = form(EASY), medium = form(MEDIUM), brackets = form(MEDIUM), hard = form(HARD), shop = form(HARD), change = form(ESSAY), teams = form(ESSAY);
    String order = T("Kali dan bagi dikerjakan lebih dulu daripada tambah dan kurang.", "Do × and ÷ before + and −.");
    for (int a = 2; a <= 9; a++) for (int b = 2; b <= 9; b++) for (int c = 2; c <= 12; c += 2) {
      int ab = a * b;
      numeric(easy, a + " × " + b + " + " + c + " = ...", "", ab + c, lines(order, a + " × " + b + " = " + ab + ".", ab + " + " + c + " = " + (ab + c) + "."), null, a * (b + c), ab, a + b + c, ab + c + 1);
      int big = c * 10 + a;
      numeric(medium, big + " + " + a + " × " + b + " = ...", "", big + ab, lines(order, a + " × " + b + " = " + ab + ".", big + " + " + ab + " = " + (big + ab) + "."), null, (big + a) * b, big + a + b, big + ab + 1, big + ab - 10);
      numeric(brackets, "(" + a + " + " + c + ") × " + b + " = ...", "", (a + c) * b, lines(T("Kerjakan yang di dalam kurung lebih dulu.", "Do the brackets first."), a + " + " + c + " = " + (a + c) + ".", (a + c) + " × " + b + " = " + (a + c) * b + "."),
          lines(T("Bagikan perkaliannya.", "Share out the multiplication."), a + " × " + b + " = " + ab + ".", c + " × " + b + " = " + c * b + ".", ab + " + " + c * b + " = " + (a + c) * b + "."), a + c * b, ab + c, a + b + c, (a + c) * b + b);
      int d = between(2, 9), dividend = c * d, value = ab + big - dividend / d;
      if (value > 0) numeric(hard, a + " × " + b + " + " + big + " − " + dividend + " ÷ " + d + " = ...", "", value, lines(order, a + " × " + b + " = " + ab + ".", dividend + " ÷ " + d + " = " + c + ".", ab + " + " + big + " = " + (ab + big) + ".", (ab + big) + " − " + c + " = " + value + "."), null, ab + big + c, value + 10, value - 10, value + 1);
      essay(teams, T("Setiap regu terdiri atas " + a + " anak laki-laki dan " + c + " anak perempuan. Ada " + b + " regu. Berapa anak seluruhnya?", "Each team has " + a + " boys and " + c + " girls. There are " + b + " teams. How many children are there in total?"), (a + c) * b + " " + T("anak", "children"),
          lines(a + " + " + c + " = " + (a + c) + ".", (a + c) + " × " + b + " = " + (a + c) * b + "."), null);
      int price = a * 500, price2 = b * 1000, total = c * price + a * price2;
      numeric(shop, T(name(c) + " membeli " + c + " pensil dengan harga " + fmt(price) + " rupiah per buah dan " + a + " buku dengan harga " + fmt(price2) + " rupiah per buah. Berapa rupiah yang dibayar?", name(c) + " buys " + c + " pencils that cost " + fmt(price) + " rupiah each and " + a + " books that cost " + fmt(price2) + " rupiah each. How many rupiah is that in total?"), "rupiah", total,
          lines(c + " × " + fmt(price) + " = " + fmt(c * price) + ".", a + " × " + fmt(price2) + " = " + fmt(a * price2) + ".", fmt(c * price) + " + " + fmt(a * price2) + " = " + fmt(total) + "."), null, price + price2, c * price, a * price2, total + 1000);
      int brought = (total / 10000 + 1) * 10000, rest = brought - total;
      essay(change, T(name(b) + " membawa uang " + fmt(brought) + " rupiah, lalu membeli " + c + " pensil dengan harga " + fmt(price) + " rupiah per buah dan " + a + " buku dengan harga " + fmt(price2) + " rupiah per buah. Berapa rupiah sisa uangnya?", name(b) + " brings " + fmt(brought) + " rupiah and buys " + c + " pencils that cost " + fmt(price) + " rupiah each and " + a + " books that cost " + fmt(price2) + " rupiah each. How many rupiah remain?"), fmt(rest) + " rupiah",
          lines(c + " × " + fmt(price) + " = " + fmt(c * price) + ".", a + " × " + fmt(price2) + " = " + fmt(a * price2) + ".", fmt(c * price) + " + " + fmt(a * price2) + " = " + fmt(total) + ".", fmt(brought) + " − " + fmt(total) + " = " + fmt(rest) + "."), null);
    }
  }

  private void sentences() {
    Form add = form(EASY), times = form(MEDIUM), minus = form(MEDIUM), story = form(HARD), write = form(HARD), both = form(ESSAY), found = form(ESSAY);
    String tail = T(". Bilangan yang tepat adalah ...", ". What is the missing number?");
    for (int a = 11; a <= 60; a += 3) for (int b = 7; b <= 39; b += 4) {
      int sum = a + b;
      numeric(add, "... + " + b + " = " + sum + tail, "", a, lines(T("Lawan dari tambah adalah kurang.", "The opposite of adding is subtracting."), sum + " − " + b + " = " + a + "."), null, sum + b, a + 10, a - 1, a + 1);
      numeric(minus, sum + " − ... = " + a + tail, "", b, lines(T("Yang dikurangkan adalah selisihnya.", "What was taken away is the difference."), sum + " − " + a + " = " + b + "."), null, sum + a, b + 10, b + 1, b - 1);
      numeric(story, T(name(a) + " memiliki " + a + " kartu dan mendapat beberapa kartu lagi sehingga menjadi " + sum + " kartu. Berapa kartu yang didapat?", name(a) + " has " + a + " cards and gets some more, making " + sum + " cards. How many cards did " + name(a) + " get?"), T("kartu", "cards"), b, sum + " − " + a + " = " + b + ".", null, sum + a, b + 10, b + 1, b - 1);
      essay(found, T(name(a) + " memiliki " + a + " kartu dan mendapat beberapa kartu lagi sehingga menjadi " + sum + " kartu. Berapa kartu yang didapat?", name(a) + " has " + a + " cards and gets some more, making " + sum + " cards. How many cards did " + name(a) + " get?"), b + " " + T("kartu", "cards"), lines(T("Tulis kalimatnya: ", "Write the sentence: ") + a + " + ... = " + sum + ".", sum + " − " + a + " = " + b + "."), null);
      List<String> wrong = List.of(a + " − " + b + " = " + Math.abs(a - b), a + " × " + b + " = " + a * b, sum + " + " + a + " = " + (sum + a));
      text(write, T(name(b) + " memiliki " + a + " kelereng dan diberi " + b + " kelereng. Kalimat matematika yang tepat adalah ...", name(b) + " has " + a + " marbles and is given " + b + " more. Which number sentence fits?"), a + " + " + b + " = " + sum, wrong, T("Diberi berarti bertambah, jadi kalimatnya memakai tanda tambah.", "Being given more means adding, so the sentence uses a plus sign."), null);
    }
    for (int t = 2; t <= 9; t++) for (int m = 2; m <= 12; m++) {
      numeric(times, "... × " + t + " = " + t * m + tail, "", m, lines(T("Lawan dari kali adalah bagi.", "The opposite of multiplying is dividing."), t * m + " ÷ " + t + " = " + m + "."), null, t * m - t, m + 1, m - 1, t * m + t);
      for (int e = 1; e <= 9; e += 2) {
        int total = t * m + e;
        essay(both, T(total + " stiker dibagi kepada " + t + " anak sama banyak dan tersisa " + e + " stiker. Berapa stiker yang diterima setiap anak?", total + " stickers are shared equally among " + t + " children with " + e + " left over. How many stickers does each child get?"), m + " " + T("stiker", "stickers"),
            lines(T("Kurangi sisanya dulu: ", "First take away the leftovers: ") + total + " − " + e + " = " + t * m + ".", t * m + " ÷ " + t + " = " + m + "."), null);
      }
    }
  }

  // ---------- Patterns ----------

  private void pattern(int stage) {
    int[] steps = stage == 1 ? new int[] {1, 2, 5, 10} : new int[] {3, 4, 6, 7, 8, 9, 12, 15, 25};
    Form next = form(EASY), missing = form(MEDIUM), shapesForm = form(MEDIUM), term = form(HARD), growth = form(ESSAY), savings = form(ESSAY);
    for (int d : steps) for (int a = 1; a <= (stage == 1 ? 30 : 60); a++) {
      numeric(next, T("Lanjutkan pola: ", "Continue the pattern: ") + a + ", " + (a + d) + ", " + (a + 2 * d) + ", " + (a + 3 * d) + T(", ... Bilangan berikutnya adalah ...", ", ... What comes next?"), "", a + 4 * d,
          lines(T("Setiap suku bertambah " + d + ".", "Each term goes up by " + d + "."), (a + 3 * d) + " + " + d + " = " + (a + 4 * d) + "."), null, a + 4 * d + 1, a + 3 * d, a + 5 * d, a + 4 * d - 1);
      numeric(missing, T("Bilangan yang hilang pada pola: ", "Find the missing number in the pattern: ") + a + ", _, " + (a + 2 * d) + ", " + (a + 3 * d), "", a + d,
          lines(T("Selisih dua suku yang berdekatan: ", "The difference between neighbours: ") + (a + 3 * d) + " − " + (a + 2 * d) + " = " + d + ".", a + " + " + d + " = " + (a + d) + "."), null, a + 2 * d + d, a + 1 + d, a + d - 1, a + 2 * d);
      for (int n = 6; n <= 12; n++) {
        int value = a + (n - 1) * d;
        StringJoiner list = new StringJoiner(", ");
        for (int k = 0; k < n; k++) list.add(Integer.toString(a + k * d));
        numeric(term, T("Pola: ", "Pattern: ") + a + ", " + (a + d) + ", " + (a + 2 * d) + T(", ... Suku ke-" + n + " adalah ...", ", ... What is term number " + n + "?"), "", value,
            lines(T("Tulis sukunya satu per satu: ", "Write the terms one by one: ") + list + "."),
            lines(T("Dari suku pertama ada " + (n - 1) + " lompatan.", "From the first term there are " + (n - 1) + " jumps."), (n - 1) + " × " + d + " = " + (n - 1) * d + ".", a + " + " + (n - 1) * d + " = " + value + "."), value + d, value - d, a + n * d + 1, n * d);
        essay(savings, T("Pada minggu pertama, " + name(n) + " membaca " + a + " halaman. Setiap minggu berikutnya bertambah " + d + " halaman. Berapa halaman pada minggu ke-" + n + "?", "In week one, " + name(n) + " reads " + a + " pages. Each next week adds " + d + " pages. How many pages are read in week " + n + "?"), value + " " + T("halaman", "pages"),
            lines(T("Tulis sukunya satu per satu: ", "Write the terms one by one: ") + list + "."), lines((n - 1) + " × " + d + " = " + (n - 1) * d + ".", a + " + " + (n - 1) * d + " = " + value + "."));
        essay(growth, T("Baris pertama sebuah aula berisi " + a + " kursi. Setiap baris berikutnya bertambah " + d + " kursi. Berapa kursi pada baris ke-" + n + "?", "The first row of a hall has " + a + " chairs. Each next row has " + d + " more chairs. How many chairs are in row " + n + "?"), value + " " + T("kursi", "chairs"),
            lines(T("Tulis sukunya satu per satu: ", "Write the terms one by one: ") + list + "."),
            lines((n - 1) + " × " + d + " = " + (n - 1) * d + ".", a + " + " + (n - 1) * d + " = " + value + "."));
      }
    }
    String[][] cycles = {{T("lingkaran", "circle"), T("segitiga", "triangle")}, {T("persegi", "square"), T("lingkaran", "circle"), T("segitiga", "triangle")}, {T("bintang", "star"), T("persegi", "square")}, {T("segitiga", "triangle"), T("bintang", "star"), T("lingkaran", "circle"), T("persegi", "square")}};
    for (String[] cycle : cycles) for (int n = 5; n <= 40; n++) {
      String answer = cycle[(n - 1) % cycle.length];
      List<String> wrong = new ArrayList<>();
      for (String s : List.of(T("lingkaran", "circle"), T("segitiga", "triangle"), T("persegi", "square"), T("bintang", "star"))) if (!s.equals(answer)) wrong.add(s);
      text(shapesForm, T("Pola bentuk berulang: ", "Repeating shape pattern: ") + String.join(", ", cycle) + T(". Bentuk ke-" + n + " adalah ...", ". What is shape number " + n + "?"), answer, wrong,
          T("Bagi " + n + " dengan " + cycle.length + " untuk mencari posisinya dalam satu putaran pola.", "Divide " + n + " by " + cycle.length + " to find its place in one round of the pattern."), null);
    }
  }

  // ---------- Measurement ----------

  private void firstMeasures() {
    Form gap = form(EASY), joined = form(MEDIUM), clock = form(MEDIUM), total = form(HARD), cut = form(ESSAY), shorter = form(ESSAY);
    for (int a = 8; a <= 40; a++) for (int b = 3; b < a; b += 2) {
      essay(shorter, T("Pensil A panjangnya " + a + " cm. Pensil B " + b + " cm lebih pendek daripada pensil A. Berapa panjang kedua pensil jika dijumlahkan?", "Pencil A is " + a + " cm long. Pencil B is " + b + " cm shorter than pencil A. What is their total length?"), (2 * a - b) + " cm", lines(a + " − " + b + " = " + (a - b) + ".", a + " + " + (a - b) + " = " + (2 * a - b) + "."), null);
      numeric(gap, T("Pita merah panjangnya " + a + " cm dan pita biru " + b + " cm. Berapa selisih panjangnya?", "A red ribbon is " + a + " cm long and a blue ribbon is " + b + " cm long. What is the difference in length?"), "cm", a - b, a + " − " + b + " = " + (a - b) + ".", null, a + b, a - b + 1, a - b - 1, a);
      numeric(joined, T("Dua pita panjangnya " + a + " cm dan " + b + " cm disambung. Berapa panjang seluruhnya?", "Two ribbons of " + a + " cm and " + b + " cm are joined. What is the total length?"), "cm", a + b, a + " + " + b + " = " + (a + b) + ".", null, a - b, a + b + 1, a + b - 1, a);
      for (int c = 2; c <= 8; c += 3) {
        numeric(total, T("Tiga potong kayu panjangnya " + a + " cm, " + b + " cm, dan " + c + " cm. Berapa panjang ketiganya?", "Three sticks are " + a + " cm, " + b + " cm and " + c + " cm long. What is their total length?"), "cm", a + b + c, lines(a + " + " + b + " = " + (a + b) + ".", (a + b) + " + " + c + " = " + (a + b + c) + "."), null, a + b - c, a + b, a + b + c + 1, a + c);
        if (a + b > c) essay(cut, T("Dua tali panjangnya " + a + " cm dan " + b + " cm disambung, lalu dipotong " + c + " cm. Berapa cm panjangnya sekarang?", "Two ropes of " + a + " cm and " + b + " cm are joined, then " + c + " cm is cut off. How long is the rope now?"), (a + b - c) + " cm", lines(a + " + " + b + " = " + (a + b) + ".", (a + b) + " − " + c + " = " + (a + b - c) + "."), null);
      }
    }
    for (int h = 1; h <= 8; h++) for (int later = 1; later <= 4; later++) {
      numeric(clock, T("Sekarang pukul " + h + ". Sekitar " + later + " jam lagi pukul berapa?", "It is " + h + " o'clock. What time is it " + later + " hours later?"), "", h + later, h + " + " + later + " = " + (h + later) + ".", null, h + later + 1, Math.abs(h - later) + 1, h + later + 2, h + later - 1 == h ? h + later + 3 : h + later - 1);
    }
  }

  /** Early grades stay with metres and hours and small numbers. */
  private void conversions(boolean early) {
    String[][] units = early ? new String[][] {{"m", "cm", "100"}, {T("jam", "hours"), T("menit", "minutes"), "60"}}
        : new String[][] {{"m", "cm", "100"}, {"km", "m", "1000"}, {"kg", "g", "1000"}, {T("jam", "hours"), T("menit", "minutes"), "60"}};
    Form forward = form(EASY), back = form(MEDIUM), mixed = form(HARD), rope = form(ESSAY), trip = form(ESSAY);
    for (String[] unit : units) {
      int factor = Integer.parseInt(unit[2]);
      String trick = factor == 60 ? null : T("Tambahkan " + (factor == 100 ? "dua" : "tiga") + " nol di belakang.", "Write " + (factor == 100 ? "two" : "three") + " zeros at the end.");
      for (int a = 2; a <= (early ? 12 : 40); a++) {
        numeric(forward, a + " " + unit[0] + " = ... " + unit[1], unit[1], a * factor, lines("1 " + unit[0] + " = " + fmt(factor) + " " + unit[1] + ".", a + " × " + fmt(factor) + " = " + fmt(a * factor) + "."), trick, a * factor * 10, a * factor / 10, a + factor, a * factor + factor);
        numeric(back, fmt(a * factor) + " " + unit[1] + " = ... " + unit[0], unit[0], a, lines("1 " + unit[0] + " = " + fmt(factor) + " " + unit[1] + ".", fmt(a * factor) + " ÷ " + fmt(factor) + " = " + a + "."), null, a * 10, a + 10, a * factor, a + 1);
        for (int b = 5; b < factor; b += factor / 7) {
          int total = a * factor + b;
          numeric(mixed, a + " " + unit[0] + " " + b + " " + unit[1] + " = ... " + unit[1], unit[1], total, lines(a + " × " + fmt(factor) + " = " + fmt(a * factor) + ".", fmt(a * factor) + " + " + b + " = " + fmt(total) + "."), null, a * 10 + b, a + b, total + factor, total + 10);
        }
      }
    }
    for (int a = 2; a <= 12; a++) for (int c = 5; c <= 55; c += 5) {
      int minutes = a * 60 + c;
      essay(trip, T("Perjalanan berlangsung " + a + " jam, lalu dilanjutkan " + c + " menit. Berapa menit seluruh perjalanan?", "A trip takes " + a + " hours and then another " + c + " minutes. How many minutes is the whole trip?"), minutes + " " + T("menit", "minutes"), lines(a + " × 60 = " + a * 60 + ".", a * 60 + " + " + c + " = " + minutes + "."), null);
    }
    for (int a = 3; a <= (early ? 9 : 30); a++) for (int c = 35; c <= 95; c += 10) {
      int left = a * 100 - c;
      essay(rope, T("Tali sepanjang " + a + " m dipotong " + c + " cm. Berapa cm sisa tali itu?", "A rope " + a + " m long has " + c + " cm cut off. How many cm of rope remain?"), fmt(left) + " cm",
          lines(a + " × 100 = " + fmt(a * 100) + ".", fmt(a * 100) + " − " + c + " = " + fmt(left) + "."), null);
    }
  }

  private void perimeter(boolean early) {
    Form square = form(EASY), triangle = form(EASY), rectangle = form(MEDIUM), side = form(HARD), lap = form(HARD), fence = form(ESSAY), laps = form(ESSAY);
    for (int s = 3; s <= 40; s++) numeric(square, T("Keliling persegi dengan sisi " + s + " cm adalah ...", "What is the perimeter of a square with side " + s + " cm?"), "cm", 4 * s,
        s + " + " + s + " + " + s + " + " + s + " = " + 4 * s + ".", T("Keempat sisinya sama: ", "All four sides are equal: ") + "4 × " + s + " = " + 4 * s + ".", s * s, 2 * s, 4 * s + 4, 3 * s);
    for (int a = 3; a <= 20; a++) for (int b = a; b <= 20; b += 2) for (int c = b; c < a + b && c <= 25; c += 3) {
      int sum = a + b + c;
      numeric(triangle, T("Keliling segitiga dengan sisi " + a + " cm, " + b + " cm, dan " + c + " cm adalah ...", "What is the perimeter of a triangle with sides " + a + " cm, " + b + " cm and " + c + " cm?"), "cm", sum, lines(a + " + " + b + " = " + (a + b) + ".", (a + b) + " + " + c + " = " + sum + "."), null, a * b, a + b, sum + 1, sum + c);
    }
    for (int p = 5; p <= 40; p++) for (int l = 2; l < p; l += 2) {
      int around = 2 * (p + l);
      numeric(rectangle, T("Keliling persegi panjang dengan panjang " + p + " cm dan lebar " + l + " cm adalah ...", "What is the perimeter of a rectangle " + p + " cm long and " + l + " cm wide?"), "cm", around,
          lines(p + " + " + l + " = " + (p + l) + ".", (p + l) + " + " + (p + l) + " = " + around + "."), lines(T("Hitung setengah keliling lalu kali 2.", "Find half the perimeter, then × 2."), p + " + " + l + " = " + (p + l) + ".", (p + l) + " × 2 = " + around + "."), p * l, p + l, around + 2, 2 * p + l);
      numeric(lap, T(name(p) + " berlari satu kali mengelilingi taman berbentuk persegi panjang dengan panjang " + p + " m dan lebar " + l + " m. Berapa meter jarak larinya?", name(p) + " runs once around a rectangular park " + p + " m long and " + l + " m wide. How many metres is that?"), "m", around, lines(p + " + " + l + " = " + (p + l) + ".", (p + l) + " × 2 = " + around + "."), null, p * l, p + l, around + 2, 2 * p + l);
      for (int k = 2; k <= (early ? 3 : 5); k++) essay(laps, T(name(p) + " berlari " + k + " putaran mengelilingi taman berbentuk persegi panjang dengan panjang " + p + " m dan lebar " + l + " m. Berapa meter jarak larinya?", name(p) + " runs " + k + " laps around a rectangular park " + p + " m long and " + l + " m wide. How many metres is that?"), fmt(k * around) + " m", lines(p + " + " + l + " = " + (p + l) + ".", (p + l) + " × 2 = " + around + ".", around + " × " + k + " = " + fmt(k * around) + "."), null);
      numeric(side, T("Keliling persegi panjang " + around + " cm dan panjangnya " + p + " cm. Berapa lebarnya?", "A rectangle has a perimeter of " + around + " cm and a length of " + p + " cm. What is its width?"), "cm", l,
          lines(around + " ÷ 2 = " + (p + l) + ".", (p + l) + " − " + p + " = " + l + "."), null, around - p, around - 2 * p, p + l, l + 2);
      if (!early) for (int price = 2000; price <= 6000; price += 2000) {
        int cost = around * price;
        essay(fence, T("Kebun berbentuk persegi panjang berukuran " + p + " m dan " + l + " m dipagari. Harga pagar " + fmt(price) + " rupiah per meter. Berapa rupiah biaya pagarnya?", "A rectangular garden " + p + " m by " + l + " m is fenced. Fencing costs " + fmt(price) + " rupiah per metre. How many rupiah does the fence cost?"), fmt(cost) + " rupiah",
            lines(p + " + " + l + " = " + (p + l) + ".", (p + l) + " × 2 = " + around + ".", around + " × " + fmt(price) + " = " + fmt(cost) + "."), null);
      }
    }
  }

  private void area(boolean withTriangle) {
    Form square = form(EASY), rectangle = form(MEDIUM), triangle = form(withTriangle ? MEDIUM : EASY), width = form(HARD), yard = form(HARD), tiles = form(ESSAY), grass = form(ESSAY);
    for (int s = 3; s <= 30; s++) numeric(square, T("Luas persegi dengan sisi " + s + " cm adalah ...", "What is the area of a square with side " + s + " cm?"), "cm²", s * s, s + " × " + s + " = " + s * s + ".", null, 4 * s, 2 * s, s * s + s, s * s - s);
    for (int p = 4; p <= 30; p++) for (int l = 2; l < p; l++) {
      numeric(rectangle, T("Luas persegi panjang dengan panjang " + p + " cm dan lebar " + l + " cm adalah ...", "What is the area of a rectangle " + p + " cm long and " + l + " cm wide?"), "cm²", p * l, splitTimesOrSimple(p, l), null, 2 * (p + l), p + l, p * l + l, p * l - l);
      for (int price = 5000; price <= 15000; price += 5000) essay(grass, T("Halaman berukuran " + p + " m dan " + l + " m ditanami rumput seharga " + fmt(price) + " rupiah per m². Berapa rupiah biayanya?", "A yard " + p + " m by " + l + " m is planted with grass costing " + fmt(price) + " rupiah per m². How many rupiah does it cost?"), fmt(p * l * price) + " rupiah", lines(p + " × " + l + " = " + p * l + ".", p * l + " × " + fmt(price) + " = " + fmt(p * l * price) + "."), null);
      numeric(yard, T("Halaman sekolah berbentuk persegi panjang dengan panjang " + p + " m dan lebar " + l + " m akan ditanami rumput. Berapa m² luas yang ditanami?", "A rectangular school yard " + p + " m long and " + l + " m wide will be planted with grass. How many m² will be planted?"), "m²", p * l, splitTimesOrSimple(p, l), null, 2 * (p + l), p + l, p * l + l, p * l - l);
      numeric(width, T("Luas persegi panjang " + p * l + " cm² dan panjangnya " + p + " cm. Berapa lebarnya?", "A rectangle has an area of " + p * l + " cm² and a length of " + p + " cm. What is its width?"), "cm", l, p * l + " ÷ " + p + " = " + l + ".", null, p * l - p, l + 1, l + 2, p);
      if (l % 2 == 0) numeric(triangle, T("Luas segitiga dengan alas " + p + " cm dan tinggi " + l + " cm adalah ...", "What is the area of a triangle with base " + p + " cm and height " + l + " cm?"), "cm²", p * l / 2,
          lines(p + " × " + l + " = " + p * l + ".", p * l + " ÷ 2 = " + p * l / 2 + "."), lines(T("Tinggi genap: bagi 2 dulu.", "The height is even, so halve it first."), l + " ÷ 2 = " + l / 2 + ".", p + " × " + l / 2 + " = " + p * l / 2 + "."), p * l, p + l, p * l / 2 + p, p * l / 2 + l);
    }
    for (int s = 2; s <= 5; s++) for (int a = 2; a <= 12; a++) for (int b = 2; b <= 12; b++) {
      int p = a * s, l = b * s, count = a * b;
      essay(tiles, T("Lantai berukuran " + p + " dm dan " + l + " dm ditutup ubin persegi bersisi " + s + " dm. Berapa ubin yang diperlukan?", "A floor " + p + " dm by " + l + " dm is covered with square tiles of side " + s + " dm. How many tiles are needed?"), count + " " + T("ubin", "tiles"),
          lines(p + " × " + l + " = " + p * l + ".", s + " × " + s + " = " + s * s + ".", p * l + " ÷ " + s * s + " = " + count + "."), lines(p + " ÷ " + s + " = " + a + ".", l + " ÷ " + s + " = " + b + ".", a + " × " + b + " = " + count + "."));
    }
  }

  private String splitTimesOrSimple(int a, int b) { return a % 10 == 0 || a < 12 ? a + " × " + b + " = " + a * b + "." : splitTimes(a, b); }

  private void volume(int stage) {
    Form cube = form(EASY), cuboid = form(MEDIUM), height = form(HARD), surface = form(stage == 2 ? HARD : MEDIUM), tank = form(ESSAY), stack = form(ESSAY);
    for (int s = 2; s <= 20; s++) {
      for (int k = 2; k <= 12; k++) essay(stack, T("Ada " + k + " kubus kecil yang masing-masing berusuk " + s + " cm. Berapa cm³ volume seluruhnya?", "There are " + k + " small cubes, each with edge " + s + " cm. What is their total volume in cm³?"), fmt(k * s * s * s) + " cm³", lines(s + " × " + s + " = " + s * s + ".", s * s + " × " + s + " = " + fmt(s * s * s) + ".", fmt(s * s * s) + " × " + k + " = " + fmt(k * s * s * s) + "."), null);
      numeric(cube, T("Volume kubus dengan rusuk " + s + " cm adalah ...", "What is the volume of a cube with edge " + s + " cm?"), "cm³", s * s * s, lines(s + " × " + s + " = " + s * s + ".", s * s + " × " + s + " = " + fmt(s * s * s) + "."), null, s * s, 3 * s, 6 * s * s, 12 * s);
      numeric(surface, T("Luas permukaan kubus dengan rusuk " + s + " cm adalah ...", "What is the surface area of a cube with edge " + s + " cm?"), "cm²", 6 * s * s, lines(s + " × " + s + " = " + s * s + ".", T("Kubus punya 6 sisi: ", "A cube has 6 faces: ") + "6 × " + s * s + " = " + fmt(6 * s * s) + "."), null, s * s, s * s * s, 12 * s, 4 * s * s);
    }
    for (int p = 3; p <= 20; p++) for (int l = 2; l <= 10; l++) for (int t = 2; t <= 10; t++) {
      int base = p * l, v = base * t;
      numeric(cuboid, T("Volume balok dengan panjang " + p + " cm, lebar " + l + " cm, dan tinggi " + t + " cm adalah ...", "What is the volume of a cuboid " + p + " cm long, " + l + " cm wide and " + t + " cm high?"), "cm³", v, lines(p + " × " + l + " = " + base + ".", base + " × " + t + " = " + fmt(v) + "."), null, p + l + t, base, 2 * base, v + base);
      numeric(height, T("Volume balok " + fmt(v) + " cm³, panjangnya " + p + " cm, dan lebarnya " + l + " cm. Berapa tingginya?", "A cuboid has a volume of " + fmt(v) + " cm³, a length of " + p + " cm and a width of " + l + " cm. What is its height?"), "cm", t, lines(p + " × " + l + " = " + base + ".", fmt(v) + " ÷ " + base + " = " + t + "."), null, t + 1, t + 2, base, Math.max(1, t - 1) == t ? t + 3 : t - 1);
      if (t % 2 == 0) {
        int filled = v / 2, more = v - filled;
        essay(tank, T("Bak berbentuk balok berukuran " + p + " dm, " + l + " dm, dan " + t + " dm sudah berisi " + fmt(filled) + " liter air. Berapa liter air lagi agar penuh?", "A tank " + p + " dm by " + l + " dm by " + t + " dm already holds " + fmt(filled) + " litres of water. How many more litres fill it?"), fmt(more) + " " + T("liter", "litres"),
            lines(p + " × " + l + " = " + base + ".", base + " × " + t + " = " + fmt(v) + T(" dm³, sama dengan liter.", " dm³, the same as litres."), fmt(v) + " − " + fmt(filled) + " = " + fmt(more) + "."), null);
      }
    }
  }

  // ---------- Shapes and angles ----------

  /** Grade 1 has not met multiplication yet, so it uses small counts and repeated addition. */
  private String timesOrAdd(boolean early, int k, int sides) {
    if (!early) return k + " × " + sides + " = " + k * sides + ".";
    StringJoiner sum = new StringJoiner(" + ");
    for (int i = 0; i < k; i++) sum.add(Integer.toString(sides));
    return sum + " = " + k * sides + ".";
  }

  private void shapes(boolean early) {
    String[] names = {T("segitiga", "triangles"), T("persegi", "squares"), T("segilima", "pentagons"), T("segienam", "hexagons")};
    int[] sides = {3, 4, 5, 6};
    Form many = form(EASY), pair = form(MEDIUM), line = form(HARD), sticks = form(ESSAY), between = form(ESSAY);
    for (int i = 0; i < names.length; i++) {
      for (int k = 2; k <= (early ? 5 : 12); k++) {
        numeric(many, T("Ada " + k + " " + names[i] + ". Berapa jumlah seluruh sisinya?", "There are " + k + " " + names[i] + ". How many sides are there altogether?"), "", k * sides[i], lines(T("Setiap " + names[i] + " punya " + sides[i] + " sisi.", "Each of the " + names[i] + " has " + sides[i] + " sides."), timesOrAdd(early, k, sides[i])), null, k + sides[i], k * sides[i] + sides[i], k * sides[i] - sides[i], k * sides[i] + 1);
        for (int j = i + 1; j < names.length; j++) for (int m = 1; m <= (early ? 3 : 8); m++) {
          int total = k * sides[i] + m * sides[j];
          numeric(pair, T("Ada " + k + " " + names[i] + " dan " + m + " " + names[j] + ". Berapa jumlah seluruh sisinya?", "There are " + k + " " + names[i] + " and " + m + " " + names[j] + ". How many sides are there altogether?"), "", total, lines(timesOrAdd(early, k, sides[i]), timesOrAdd(early, m, sides[j]), k * sides[i] + " + " + m * sides[j] + " = " + total + "."), null, k + m, total + 1, total - sides[i], total + sides[j]);
          int bag = total + between(3, 20), rest = bag - total;
          essay(sticks, T(name(k) + " memiliki " + bag + " stik dan membuat " + k + " " + names[i] + " serta " + m + " " + names[j] + ". Berapa stik yang tersisa?", name(k) + " has " + bag + " sticks and makes " + k + " " + names[i] + " and " + m + " " + names[j] + ". How many sticks are left?"), rest + " " + T("stik", "sticks"),
              lines(timesOrAdd(early, k, sides[i]), timesOrAdd(early, m, sides[j]), k * sides[i] + " + " + m * sides[j] + " = " + total + ".", bag + " − " + total + " = " + rest + "."), null);
        }
      }
    }
    for (int n = 6; n <= (early ? 15 : 30); n++) for (int k = 2; k < n; k++) {
      for (int j = k + 2; j <= n; j += 2) essay(between, T(name(k) + " berada di urutan ke-" + k + " dan " + name(j) + " di urutan ke-" + j + " dalam satu antrean. Berapa anak di antara mereka?", name(k) + " is number " + k + " and " + name(j) + " is number " + j + " in a queue. How many children are between them?"), (j - k - 1) + " " + T("anak", "children"), lines(j + " − " + k + " = " + (j - k) + ".", (j - k) + " − 1 = " + (j - k - 1) + "."), null);
      numeric(line, T("Ada " + n + " anak dalam satu barisan. " + name(k) + " berada di urutan ke-" + k + " dari depan. " + name(k) + " berada di urutan ke berapa dari belakang?", "There are " + n + " children in a line. " + name(k) + " is number " + k + " from the front. What number is " + name(k) + " from the back?"), "", n - k + 1,
          lines(T("Banyak anak di belakangnya: ", "Children behind: ") + n + " − " + k + " = " + (n - k) + ".", (n - k) + " + 1 = " + (n - k + 1) + "."), null, n - k, n - k + 2, n + k, n - k + 3);
    }
  }

  private void angles() {
    Form right = form(EASY), kind = form(EASY), straight = form(MEDIUM), triangle = form(MEDIUM), quad = form(HARD), clock = form(HARD), isosceles = form(ESSAY), rightTriangle = form(ESSAY);
    for (int a = 5; a <= 85; a += 5) essay(rightTriangle, T("Segitiga siku-siku memiliki satu sudut lancip " + a + "°. Berapa besar sudut lancip lainnya?", "A right-angled triangle has an acute angle of " + a + "°. How big is the other acute angle?"), (90 - a) + "°", lines("180 − 90 = 90.", "90 − " + a + " = " + (90 - a) + "."), null);
    for (int a = 5; a <= 85; a += 5) numeric(right, T("Dua sudut bersama membentuk sudut siku-siku. Satu sudut besarnya " + a + "°. Berapa besar sudut lainnya?", "Two angles together make a right angle. One angle is " + a + "°. What is the other angle?"), "°", 90 - a, "90 − " + a + " = " + (90 - a) + ".", null, 180 - a, 90 + a, 100 - a, 80 - a);
    for (int a = 5; a <= 175; a += 5) {
      numeric(straight, T("Dua sudut bersama membentuk sudut lurus. Satu sudut besarnya " + a + "°. Berapa besar sudut lainnya?", "Two angles together make a straight angle. One angle is " + a + "°. What is the other angle?"), "°", 180 - a, "180 − " + a + " = " + (180 - a) + ".", null, 360 - a, 90 + a, 190 - a, a == 90 ? 80 : 90);
      String answer = a < 90 ? T("sudut lancip", "acute") : a == 90 ? T("sudut siku-siku", "right") : T("sudut tumpul", "obtuse");
      List<String> wrong = new ArrayList<>();
      for (String s : List.of(T("sudut lancip", "acute"), T("sudut siku-siku", "right"), T("sudut tumpul", "obtuse"), T("sudut lurus", "straight"))) if (!s.equals(answer)) wrong.add(s);
      text(kind, T("Sudut yang besarnya " + a + "° termasuk ...", "An angle of " + a + "° is called ..."), answer, wrong, T("Lancip kurang dari 90°, siku-siku tepat 90°, dan tumpul antara 90° dan 180°.", "Acute is under 90°, right is exactly 90°, and obtuse is between 90° and 180°."), null);
    }
    for (int a = 20; a <= 120; a += 5) for (int b = 20; a + b < 170; b += 5) {
      int third = 180 - a - b;
      numeric(triangle, T("Dua sudut segitiga besarnya " + a + "° dan " + b + "°. Berapa besar sudut ketiga?", "Two angles of a triangle are " + a + "° and " + b + "°. What is the third angle?"), "°", third, lines(a + " + " + b + " = " + (a + b) + ".", "180 − " + (a + b) + " = " + third + "."), null, 360 - a - b, 90 - Math.min(a, 80), third + 10, third + 5);
      for (int c = 50; c <= 120; c += 10) if (360 - a - b - c > 0) {
        int fourth = 360 - a - b - c;
        numeric(quad, T("Tiga sudut segi empat besarnya " + a + "°, " + b + "°, dan " + c + "°. Berapa besar sudut keempat?", "Three angles of a quadrilateral are " + a + "°, " + b + "° and " + c + "°. What is the fourth angle?"), "°", fourth, lines(a + " + " + b + " = " + (a + b) + ".", (a + b) + " + " + c + " = " + (a + b + c) + ".", "360 − " + (a + b + c) + " = " + fourth + "."), null, fourth + 10, fourth + 5, fourth + 180, Math.abs(180 - a - b) + 1);
      }
    }
    for (int h = 1; h <= 11; h++) {
      int angle = Math.min(h, 12 - h) * 30;
      numeric(clock, T("Pada pukul " + h + ".00, berapa besar sudut terkecil antara jarum jam dan jarum menit?", "At " + h + ":00, what is the smaller angle between the hour hand and the minute hand?"), "°", angle, lines(T("Setiap angka pada jam berjarak 30°.", "Each number on the clock is 30° apart."), Math.min(h, 12 - h) + " × 30 = " + angle + "."), null, angle + 30, 360 - angle, angle + 60, h * 10 == angle ? angle + 90 : h * 10);
    }
    for (int a = 20; a <= 140; a += 2) {
      int base = (180 - a) / 2;
      essay(isosceles, T("Sudut puncak segitiga sama kaki besarnya " + a + "°. Berapa besar satu sudut alasnya?", "The top angle of an isosceles triangle is " + a + "°. How big is one base angle?"), base + "°", lines("180 − " + a + " = " + (180 - a) + ".", (180 - a) + " ÷ 2 = " + base + "."), null);
    }
  }

  // ---------- Fractions, decimals and percent ----------

  private void fractions(int stage) {
    int[] denominators = stage == 1 ? new int[] {2, 3, 4, 5, 6, 8, 10} : new int[] {2, 3, 4, 5, 6, 8, 9, 10, 12};
    Form of = form(EASY), equal = form(MEDIUM), plus = form(MEDIUM), compare = form(HARD), story = form(HARD), ribbon = form(HARD), rest = form(ESSAY), cloth = form(ESSAY);
    for (int d : denominators) for (int n = 1; n < d; n++) {
      if (gcd(n, d) != 1) continue;
      for (int k = 2; k <= 12; k++) {
        int w = d * k, part = n * k;
        numeric(of, n + "/" + d + T(" dari " + w + " adalah ...", " of " + w + " is ..."), "", part, lines(w + " ÷ " + d + " = " + k + ".", k + " × " + n + " = " + part + "."), n == d - 1 && d > 2 ? lines(T("Lebih cepat dengan bagian sisanya.", "It is faster to use the part left over."), w + " ÷ " + d + " = " + k + ".", w + " − " + k + " = " + part + ".") : null, k, w - n, part + k, w);
        numeric(equal, n + "/" + d + " = .../" + w, "", part, lines(w + " ÷ " + d + " = " + k + ".", n + " × " + k + " = " + part + "."), null, n + k, part + 1, w - d, part + k);
        numeric(story, T("Di kantin ada " + w + " kue. Sebanyak " + n + "/" + d + " bagian terjual. Berapa kue yang belum terjual?", "A canteen has " + w + " cakes. " + n + "/" + d + " of them are sold. How many cakes are not sold?"), T("kue", "cakes"), w - part, lines(w + " ÷ " + d + " = " + k + ".", k + " × " + n + " = " + part + ".", w + " − " + part + " = " + (w - part) + "."),
            lines(T("Bagian yang belum terjual adalah " + (d - n) + "/" + d + ".", "The unsold part is " + (d - n) + "/" + d + "."), w + " ÷ " + d + " = " + k + ".", k + " × " + (d - n) + " = " + (w - part) + "."), part, k, w, w - part + k);
        numeric(ribbon, T("Pita sepanjang " + w + " cm. Sebanyak " + n + "/" + d + " bagian dipotong. Berapa cm sisa pita itu?", "A ribbon is " + w + " cm long. " + n + "/" + d + " of it is cut off. How many cm of ribbon remain?"), "cm", w - part, lines(w + " ÷ " + d + " = " + k + ".", k + " × " + n + " = " + part + ".", w + " − " + part + " = " + (w - part) + "."),
            lines(T("Sisanya " + (d - n) + "/" + d + " bagian.", "What remains is " + (d - n) + "/" + d + "."), w + " ÷ " + d + " = " + k + ".", k + " × " + (d - n) + " = " + (w - part) + "."), part, k, w, w - part + k);
        essay(cloth, T("Pita sepanjang " + w + " cm. Sebanyak " + n + "/" + d + " bagian dipotong. Berapa cm sisa pita itu?", "A ribbon is " + w + " cm long. " + n + "/" + d + " of it is cut off. How many cm of ribbon remain?"), (w - part) + " cm", lines(w + " ÷ " + d + " = " + k + ".", k + " × " + n + " = " + part + ".", w + " − " + part + " = " + (w - part) + "."),
            lines(T("Sisanya " + (d - n) + "/" + d + " bagian.", "What remains is " + (d - n) + "/" + d + "."), w + " ÷ " + d + " = " + k + ".", k + " × " + (d - n) + " = " + (w - part) + "."));
        essay(rest, T("Di kelas ada " + w + " siswa. Sebanyak " + n + "/" + d + " bagian adalah siswa perempuan. Berapa siswa laki-laki di kelas itu?", "A class has " + w + " pupils. " + n + "/" + d + " of them are girls. How many boys are in the class?"), (w - part) + " " + T("siswa", "pupils"),
            lines(w + " ÷ " + d + " = " + k + ".", k + " × " + n + " = " + part + ".", w + " − " + part + " = " + (w - part) + "."),
            lines(T("Bagian laki-laki adalah " + (d - n) + "/" + d + ".", "The boys' part is " + (d - n) + "/" + d + "."), w + " ÷ " + d + " = " + k + ".", k + " × " + (d - n) + " = " + (w - part) + "."));
      }
      for (int m = 1; m + n < d; m++) if (gcd(n + m, d) == 1) textFractions(plus, n + "/" + d + " + " + m + "/" + d + " = ...", (n + m) + "/" + d, List.of((n + m) + "/" + (2 * d), (n * m) + "/" + d, (n + m + 1) + "/" + d),
          T("Penyebutnya sama, jadi cukup jumlahkan pembilangnya: ", "The denominators match, so just add the numerators: ") + n + " + " + m + " = " + (n + m) + ".");
    }
    if (stage == 2) for (int b : denominators) for (int d : denominators) for (int a = 1; a < b; a++) for (int c = 1; c < d; c++) {
      if (b == d || gcd(a, b) != 1 || gcd(c, d) != 1) continue;
      int l = lcm(b, d), top = a * l / b + c * l / d;
      if (top >= l || gcd(top, l) != 1) continue;
      textFractions(plus, a + "/" + b + " + " + c + "/" + d + " = ...", top + "/" + l, List.of((a + c) + "/" + (b + d), (a + c) + "/" + l, (top + 1) + "/" + l),
          lines(T("Samakan penyebut menjadi " + l + ".", "Make both denominators " + l + "."), a + "/" + b + " = " + a * l / b + "/" + l + T(" dan ", " and ") + c + "/" + d + " = " + c * l / d + "/" + l + ".", a * l / b + " + " + c * l / d + " = " + top + "."));
    }
    int[] common = stage == 1 ? new int[] {5, 8, 10, 12} : new int[] {12, 15, 20, 24};
    for (int d : common) for (int a = 1; a < d; a++) for (int b = a + 1; b < d; b++) for (int c = b + 1; c < d; c++) for (int e = c + 1; e < d; e += 2) {
      text(compare, T("Pecahan terbesar di antara " + a + "/" + d + ", " + e + "/" + d + ", " + b + "/" + d + ", dan " + c + "/" + d + " adalah ...", "Which fraction is the largest among " + a + "/" + d + ", " + e + "/" + d + ", " + b + "/" + d + " and " + c + "/" + d + "?"), e + "/" + d, List.of(a + "/" + d, b + "/" + d, c + "/" + d),
          T("Penyebutnya sama, jadi bandingkan pembilangnya. Pembilang terbesar adalah " + e + ".", "The denominators match, so compare the numerators. The largest numerator is " + e + "."), null);
    }
  }

  private void textFractions(Form form, String prompt, String answer, List<String> wrong, String method) { text(form, prompt, answer, wrong, method, null); }

  private void decimals() {
    Form convert = form(EASY), percentOf = form(MEDIUM), add = form(MEDIUM), toPercent = form(HARD), discount = form(HARD), absent = form(ESSAY), sale = form(ESSAY);
    int[][] clean = {{1, 2}, {1, 4}, {3, 4}, {1, 5}, {2, 5}, {3, 5}, {4, 5}, {1, 10}, {3, 10}, {7, 10}, {9, 10}, {1, 20}, {3, 20}, {7, 20}, {9, 20}, {11, 20}, {13, 20}, {17, 20}, {1, 25}, {2, 25}, {3, 25}};
    for (int[] f : clean) {
      int hundredths = f[0] * 100 / f[1];
      text(convert, f[0] + "/" + f[1] + T(" dalam bentuk desimal adalah ...", " as a decimal is ..."), decimal(hundredths), List.of(decimal(hundredths + 10), decimal(f[0] * 10), decimal(Math.max(1, hundredths - 5)), decimal(hundredths + 5)),
          lines(T("Ubah penyebutnya menjadi 100: ", "Make the denominator 100: ") + f[0] + "/" + f[1] + " = " + hundredths + "/100.", T("Dua angka di belakang koma: ", "Two decimal places: ") + decimal(hundredths) + "."), null);
      numeric(toPercent, f[0] + "/" + f[1] + T(" dalam bentuk persen adalah ...", " as a percent is ..."), "%", hundredths, lines(T("Ubah penyebutnya menjadi 100: ", "Make the denominator 100: ") + f[0] + "/" + f[1] + " = " + hundredths + "/100.", T("Per seratus berarti persen.", "Per hundred means percent.")), null, f[0] * f[1], hundredths + 10, hundredths + 5, f[1]);
    }
    for (int p = 5; p <= 95; p += 5) for (int w = 20; w <= 1000; w += 20) {
      if (p * w % 100 != 0) continue;
      int value = p * w / 100;
      String tenth = p % 10 == 0 ? lines(T("10% dari " + w + " adalah " + w / 10 + ".", "10% of " + w + " is " + w / 10 + "."), w / 10 + " × " + p / 10 + " = " + value + ".") : null;
      numeric(percentOf, p + "%" + T(" dari " + w + " adalah ...", " of " + w + " is ..."), "", value, lines(w + " ÷ 100 = " + (w % 100 == 0 ? Integer.toString(w / 100) : decimal(w)) + ".", T("Dikali " + p + " menjadi ", "Times " + p + " is ") + value + "."), tenth, p, w - value, value + 10, value + w / 10);
      if (w >= 100) {
        int price = w * 1000, off = value * 1000, pay = price - off;
        String[] item = w % 60 == 0 ? new String[] {T("sepatu", "A pair of shoes")} : w % 40 == 0 ? new String[] {T("jaket", "A jacket")} : new String[] {T("tas", "A bag")};
        essay(sale, T("Harga " + item[0] + " " + fmt(price) + " rupiah dan mendapat diskon " + p + "%. Berapa rupiah yang harus dibayar?", item[0] + " costs " + fmt(price) + " rupiah with a " + p + "% discount. How many rupiah must be paid?"), fmt(pay) + " rupiah",
            lines(T("Diskon: " + p + "% dari ", "Discount: " + p + "% of ") + fmt(price) + " = " + fmt(off) + ".", fmt(price) + " − " + fmt(off) + " = " + fmt(pay) + "."), null);
        numeric(discount, T("Harga " + item[0] + " " + fmt(price) + " rupiah dan mendapat diskon " + p + "%. Berapa rupiah yang harus dibayar?", item[0] + " costs " + fmt(price) + " rupiah with a " + p + "% discount. How many rupiah must be paid?"), "rupiah", pay,
            lines(T("Diskon: " + p + "% dari ", "Discount: " + p + "% of ") + fmt(price) + " = " + fmt(off) + ".", fmt(price) + " − " + fmt(off) + " = " + fmt(pay) + "."), lines(T("Yang dibayar adalah " + (100 - p) + "% dari harga.", "You pay " + (100 - p) + "% of the price."), fmt(price) + " ÷ 100 = " + fmt(w * 10) + ".", fmt(w * 10) + " × " + (100 - p) + " = " + fmt(pay) + "."), off, price + off, pay + 1000, pay - 1000);
      }
      essay(absent, T("Sekolah memiliki " + w + " siswa. Sebanyak " + p + "% siswa ikut lomba. Berapa siswa yang tidak ikut lomba?", "A school has " + w + " pupils. " + p + "% of them join a contest. How many pupils do not join?"), (w - value) + " " + T("siswa", "pupils"),
          lines(p + "%" + T(" dari ", " of ") + w + " = " + value + ".", w + " − " + value + " = " + (w - value) + "."), lines(T("Yang tidak ikut adalah " + (100 - p) + "%.", "Those not joining are " + (100 - p) + "%."), (100 - p) + "%" + T(" dari ", " of ") + w + " = " + (w - value) + "."));
    }
    for (int a = 5; a <= 95; a += 5) for (int b = 5; a + b < 100; b += 5) text(add, decimal(a) + " + " + decimal(b) + " = ...", decimal(a + b), List.of(decimal(a + b + 10), decimal(a + b + 5), decimal(Math.abs(a - b) + 1)),
        lines(T("Sejajarkan koma, lalu jumlahkan perseratusannya.", "Line up the decimal points, then add the hundredths."), a + " + " + b + " = " + (a + b) + T(" perseratus.", " hundredths.")), null);
  }

  private void ratio() {
    Form simplify = form(EASY), scaleUp = form(MEDIUM), share = form(HARD), map = form(MEDIUM), total = form(ESSAY), parts = form(ESSAY);
    for (int a = 2; a <= 9; a++) for (int b = a + 1; b <= 12; b++) {
      if (gcd(a, b) != 1) continue;
      for (int k = 2; k <= 12; k++) {
        List<String> wrong = List.of((a * k) / 1 + " : " + b, a + " : " + (b * k), (a + 1) + " : " + b);
        text(simplify, T("Bentuk paling sederhana dari rasio " + a * k + " : " + b * k + " adalah ...", "What is the simplest form of the ratio " + a * k + " : " + b * k + "?"), a + " : " + b, wrong, lines(T("Bagi keduanya dengan faktor yang sama, yaitu " + k + ".", "Divide both by their common factor, " + k + "."), a * k + " ÷ " + k + " = " + a + ".", b * k + " ÷ " + k + " = " + b + "."), null);
        numeric(scaleUp, T("Rasio pensil merah dan biru adalah " + a + " : " + b + ". Jika ada " + a * k + " pensil merah, berapa pensil biru?", "The ratio of red to blue pencils is " + a + " : " + b + ". If there are " + a * k + " red pencils, how many blue pencils are there?"), T("pensil", "pencils"), b * k, lines(a * k + " ÷ " + a + " = " + k + ".", k + " × " + b + " = " + b * k + "."), null, a * k + b, b + k, a * k, b * k + k);
        int whole = (a + b) * k;
        numeric(share, T("Kelereng dibagi kepada Alya dan Bima dengan rasio " + a + " : " + b + ". Jumlah kelereng " + whole + ". Berapa kelereng Bima?", "Marbles are shared between Alya and Bima in the ratio " + a + " : " + b + ". There are " + whole + " marbles. How many marbles does Bima get?"), T("kelereng", "marbles"), b * k, lines(a + " + " + b + " = " + (a + b) + ".", whole + " ÷ " + (a + b) + " = " + k + ".", k + " × " + b + " = " + b * k + "."), null, a * k, whole - a, whole / 2 == b * k ? whole / 2 + 1 : whole / 2, b * k + k);
        essay(parts, T("Kelereng dibagi kepada Alya dan Bima dengan rasio " + a + " : " + b + ". Jumlah kelereng " + whole + ". Berapa kelereng Bima?", "Marbles are shared between Alya and Bima in the ratio " + a + " : " + b + ". There are " + whole + " marbles. How many marbles does Bima get?"), b * k + " " + T("kelereng", "marbles"), lines(a + " + " + b + " = " + (a + b) + ".", whole + " ÷ " + (a + b) + " = " + k + ".", k + " × " + b + " = " + b * k + "."), null);
        int gap = (b - a) * k;
        essay(total, T("Rasio uang Alya dan Bima adalah " + a + " : " + b + ". Uang Bima " + fmt(gap * 1000) + " rupiah lebih banyak daripada uang Alya. Berapa rupiah uang mereka seluruhnya?", "The ratio of Alya's money to Bima's is " + a + " : " + b + ". Bima has " + fmt(gap * 1000) + " rupiah more than Alya. How many rupiah do they have together?"), fmt((a + b) * k * 1000) + " rupiah",
            lines(b + " − " + a + " = " + (b - a) + ".", fmt(gap * 1000) + " ÷ " + (b - a) + " = " + fmt(k * 1000) + ".", a + " + " + b + " = " + (a + b) + ".", (a + b) + " × " + fmt(k * 1000) + " = " + fmt((a + b) * k * 1000) + "."), null);
      }
    }
    for (int km = 1; km <= 5; km++) for (int cm = 2; cm <= 30; cm++) {
      int scale = km * 100000;
      numeric(map, T("Skala peta 1 : " + fmt(scale) + ". Jarak pada peta " + cm + " cm. Berapa km jarak sebenarnya?", "A map has a scale of 1 : " + fmt(scale) + ". The distance on the map is " + cm + " cm. How many km is the real distance?"), "km", cm * km,
          lines(T("1 cm pada peta mewakili " + fmt(scale) + " cm, yaitu " + km + " km.", "1 cm on the map stands for " + fmt(scale) + " cm, which is " + km + " km."), cm + " × " + km + " = " + cm * km + "."), null, cm + km, cm * km * 10, cm * km + km, cm * 10);
    }
  }

  // ---------- Factors and multiples ----------

  private void lcmAndGcd() {
    Form smallLcm = form(EASY), smallGcd = form(EASY), bigLcm = form(MEDIUM), bigGcd = form(MEDIUM), lights = form(HARD), gifts = form(HARD), each = form(ESSAY), meet = form(ESSAY);
    String lcmName = T("KPK", "The LCM"), gcdName = T("FPB", "The GCF");
    for (int a = 4; a <= 60; a++) for (int b = a + 1; b <= 60; b++) {
      int g = gcd(a, b), l = lcm(a, b);
      if (g == 1 || b % a == 0 || l > 240) continue;
      int[] lcmWrong = {g, a * b, a + b, l + b, 2 * l}, gcdWrong = {l, a, b - a, 2 * g, g + 1};
      String quickLcm = l / b <= 5 ? lines(T("Cukup tulis kelipatan bilangan terbesar: ", "Just list multiples of the larger number: ") + multiples(b, l) + ".", T("Yang pertama habis dibagi " + a + " adalah " + l + ".", "The first one divisible by " + a + " is " + l + ".")) : null;
      String quickGcd = a % (b - a) == 0 ? lines(T("FPB selalu menjadi faktor selisih.", "The GCF always divides the difference."), b + " − " + a + " = " + (b - a) + ".", T((b - a) + " membagi habis " + a + " dan " + b + ".", (b - a) + " divides both " + a + " and " + b + ".")) : null;
      boolean small = b <= 15;
      numeric(small ? smallLcm : bigLcm, T("KPK dari " + a + " dan " + b + " adalah ...", "The LCM of " + a + " and " + b + " is ..."), "", l, lines(T("Kelipatan ", "Multiples of ") + a + ": " + multiples(a, l) + ".", T("Kelipatan ", "Multiples of ") + b + ": " + multiples(b, l) + ".", lcmName + " = " + l + "."), quickLcm, lcmWrong);
      numeric(small ? smallGcd : bigGcd, T("FPB dari " + a + " dan " + b + " adalah ...", "The GCF of " + a + " and " + b + " is ..."), "", g, lines(T("Faktor ", "Factors of ") + a + ": " + factors(a) + ".", T("Faktor ", "Factors of ") + b + ": " + factors(b) + ".", gcdName + " = " + g + "."), quickGcd, gcdWrong);
      if (b > 30) continue;
      essay(meet, T("Lampu A berkedip setiap " + a + " detik dan lampu B setiap " + b + " detik. Keduanya baru saja berkedip bersamaan. Setelah berapa detik keduanya berkedip bersamaan lagi?", "Light A flashes every " + a + " seconds and light B every " + b + " seconds. They have just flashed together. After how many seconds do they flash together again?"), l + " " + T("detik", "seconds"), lines(T("Kelipatan ", "Multiples of ") + a + ": " + multiples(a, l) + ".", T("Kelipatan ", "Multiples of ") + b + ": " + multiples(b, l) + ".", lcmName + " = " + l + "."), quickLcm);
      numeric(lights, T("Lampu A berkedip setiap " + a + " detik dan lampu B setiap " + b + " detik. Keduanya baru saja berkedip bersamaan. Setelah berapa detik keduanya berkedip bersamaan lagi?", "Light A flashes every " + a + " seconds and light B every " + b + " seconds. They have just flashed together. After how many seconds do they flash together again?"), T("detik", "seconds"), l, lines(T("Bersamaan lagi berarti mencari KPK.", "Together again means finding the LCM."), T("Kelipatan ", "Multiples of ") + a + ": " + multiples(a, l) + ".", T("Kelipatan ", "Multiples of ") + b + ": " + multiples(b, l) + "."), quickLcm, lcmWrong);
      numeric(gifts, T("Bu Sari memiliki " + a + " apel dan " + b + " jeruk. Semuanya dibagi ke dalam bingkisan yang isinya sama. Paling banyak berapa bingkisan yang dapat dibuat?", "Mrs Sari has " + a + " apples and " + b + " oranges to share into identical gift bags. What is the largest number of gift bags she can make?"), T("bingkisan", "gift bags"), g, lines(T("Paling banyak dan sama rata berarti mencari FPB.", "The largest equal sharing means finding the GCF."), T("Faktor ", "Factors of ") + a + ": " + factors(a) + ".", T("Faktor ", "Factors of ") + b + ": " + factors(b) + "."), quickGcd, gcdWrong);
      essay(each, T("Bu Sari memiliki " + a + " apel dan " + b + " jeruk untuk dibagi ke dalam bingkisan sebanyak mungkin dengan isi yang sama. Berapa apel dalam setiap bingkisan?", "Mrs Sari shares " + a + " apples and " + b + " oranges into as many identical gift bags as possible. How many apples are in each gift bag?"), a / g + " " + T("apel", "apples"),
          lines(T("Faktor ", "Factors of ") + a + ": " + factors(a) + ".", T("Faktor ", "Factors of ") + b + ": " + factors(b) + ".", gcdName + " = " + g + ".", a + " ÷ " + g + " = " + a / g + "."), quickGcd == null ? null : quickGcd + "\n" + a + " ÷ " + g + " = " + a / g + ".");
    }
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

  // ---------- Data and chance ----------

  private void data(int stage) {
    String[][] topics = {{T("Data buah kesukaan: ", "Favourite fruit data: "), T("apel", "apple"), T("jeruk", "orange"), T("mangga", "mango"), T("pisang", "banana"), T("Buah apa yang paling banyak dipilih?", "Which fruit was chosen most?")},
        {T("Data olahraga kesukaan: ", "Favourite sport data: "), T("renang", "swimming"), T("futsal", "futsal"), T("voli", "volleyball"), T("lari", "running"), T("Olahraga apa yang paling banyak dipilih?", "Which sport was chosen most?")}};
    Form total = form(EASY), picture = form(EASY), gap = form(MEDIUM), most = form(MEDIUM), mean = form(HARD), compare = form(HARD), both = form(ESSAY), sumUp = form(ESSAY);
    for (int i = 0; i < 900; i++) {
      int[] v = {between(4, 40), between(4, 40), between(4, 40), between(4, 40)};
      if (new HashSet<>(List.of(v[0], v[1], v[2], v[3])).size() < 4) continue;
      String[] topic = topics[i % topics.length], fruit = {topic[1], topic[2], topic[3], topic[4]};
      String table = topic[0] + fruit[0] + " " + v[0] + ", " + fruit[1] + " " + v[1] + ", " + fruit[2] + " " + v[2] + ", " + fruit[3] + " " + v[3] + ". ";
      int sum = v[0] + v[1] + v[2] + v[3], top = Math.max(Math.max(v[0], v[1]), Math.max(v[2], v[3])), low = Math.min(Math.min(v[0], v[1]), Math.min(v[2], v[3]));
      numeric(total, table + T("Berapa jumlah seluruh siswa?", "How many pupils are there in total?"), T("siswa", "pupils"), sum, lines(v[0] + " + " + v[1] + " = " + (v[0] + v[1]) + ".", (v[0] + v[1]) + " + " + v[2] + " = " + (v[0] + v[1] + v[2]) + ".", (v[0] + v[1] + v[2]) + " + " + v[3] + " = " + sum + "."), null, sum - v[3], sum + 1, sum - 1, top);
      essay(sumUp, table + T("Berapa jumlah seluruh siswa?", "How many pupils are there in total?"), sum + " " + T("siswa", "pupils"), lines(v[0] + " + " + v[1] + " = " + (v[0] + v[1]) + ".", (v[0] + v[1]) + " + " + v[2] + " = " + (v[0] + v[1] + v[2]) + ".", (v[0] + v[1] + v[2]) + " + " + v[3] + " = " + sum + "."), null);
      numeric(gap, table + T("Berapa selisih data terbanyak dan tersedikit?", "What is the difference between the largest and smallest values?"), T("siswa", "pupils"), top - low, top + " − " + low + " = " + (top - low) + ".", null, top + low, top, low, top - low + 1);
      List<String> names = new ArrayList<>();
      String best = "";
      for (int k = 0; k < 4; k++) { if (v[k] == top) best = fruit[k]; else names.add(fruit[k]); }
      text(most, table + topic[5], best, names, T("Bilangan terbesar pada data adalah " + top + ".", "The largest value in the data is " + top + "."), null);
      if (stage == 2 && sum % 4 == 0) numeric(mean, table + T("Berapa rata-rata data tersebut?", "What is the mean of the data?"), "", sum / 4, lines(v[0] + " + " + v[1] + " = " + (v[0] + v[1]) + ".", (v[0] + v[1]) + " + " + v[2] + " = " + (v[0] + v[1] + v[2]) + ".", (v[0] + v[1] + v[2]) + " + " + v[3] + " = " + sum + ".", sum + " ÷ 4 = " + sum / 4 + "."), null, sum, sum / 2, sum / 4 + 1, sum / 4 + 2);
      numeric(compare, table + T("Berapa banyak " + fruit[0] + " dan " + fruit[1] + " jika digabung, lalu dikurangi " + fruit[3] + "?", "How many is " + fruit[0] + " plus " + fruit[1] + ", minus " + fruit[3] + "?"), "", Math.abs(v[0] + v[1] - v[3]), v[0] + v[1] >= v[3] ? lines(v[0] + " + " + v[1] + " = " + (v[0] + v[1]) + ".", (v[0] + v[1]) + " − " + v[3] + " = " + (v[0] + v[1] - v[3]) + ".") : lines(v[0] + " + " + v[1] + " = " + (v[0] + v[1]) + ".", v[3] + " − " + (v[0] + v[1]) + " = " + (v[3] - v[0] - v[1]) + "."), null, v[0] + v[1], v[0] + v[1] + v[3], Math.abs(v[0] - v[1]), v[3]);
    }
    for (int k : new int[] {2, 3, 4, 5, 10}) for (int a = 2; a <= 12; a++) {
      numeric(picture, T("Pada piktogram, 1 gambar mewakili " + k + " buku. Rak A digambar dengan " + a + " gambar. Berapa buku di rak A?", "In a pictogram, 1 picture stands for " + k + " books. Shelf A has " + a + " pictures. How many books are on shelf A?"), T("buku", "books"), a * k, a + " × " + k + " = " + a * k + ".", null, a + k, a * k + k, a * k - k, a * k + 1);
      for (int b = 2; b <= 12; b++) if (b != a) essay(both, T("Pada piktogram, 1 gambar mewakili " + k + " buku. Rak A digambar dengan " + a + " gambar dan rak B dengan " + b + " gambar. Berapa buku di kedua rak?", "In a pictogram, 1 picture stands for " + k + " books. Shelf A has " + a + " pictures and shelf B has " + b + " pictures. How many books are on both shelves?"), (a + b) * k + " " + T("buku", "books"),
          lines(a + " × " + k + " = " + a * k + ".", b + " × " + k + " = " + b * k + ".", a * k + " + " + b * k + " = " + (a + b) * k + "."), lines(a + " + " + b + " = " + (a + b) + ".", (a + b) + " × " + k + " = " + (a + b) * k + "."));
    }
  }

  private void probability() {
    Form bag = form(EASY), sure = form(EASY), dice = form(MEDIUM), spinner = form(MEDIUM), afterDraw = form(HARD), expected = form(ESSAY), coin = form(ESSAY);
    for (int r = 1; r <= 12; r++) for (int b = 1; b <= 12; b++) {
      int t = r + b;
      textFraction(bag, T("Kantong berisi " + r + " kelereng merah dan " + b + " kelereng biru. Berapa peluang terambil kelereng merah?", "A bag holds " + r + " red and " + b + " blue marbles. What is the probability of picking a red marble?"), r, t, b, lines(T("Banyak hasil yang diinginkan: ", "Wanted outcomes: ") + r + ".", T("Banyak seluruh hasil: ", "All outcomes: ") + r + " + " + b + " = " + t + "."));
      if (r >= 2) textFraction(afterDraw, T("Kantong berisi " + r + " kelereng merah dan " + b + " kelereng biru. Satu kelereng merah sudah diambil dan tidak dikembalikan. Berapa peluang kelereng berikutnya berwarna merah?", "A bag holds " + r + " red and " + b + " blue marbles. One red marble has been taken and not returned. What is the probability that the next marble is red?"), r - 1, t - 1, b, lines(T("Sisa kelereng merah: ", "Red marbles left: ") + r + " − 1 = " + (r - 1) + ".", T("Sisa seluruh kelereng: ", "All marbles left: ") + t + " − 1 = " + (t - 1) + "."));
      List<String> options = List.of(T("pasti", "certain"), T("mustahil", "impossible"), T("mungkin", "possible"), T("sama saja dengan seri", "a tie"));
      text(sure, T("Kantong hanya berisi " + t + " kelereng merah. Mengambil kelereng biru dari kantong itu adalah kejadian ...", "A bag holds only " + t + " red marbles. Picking a blue marble from it is ..."), options.get(1), List.of(options.get(0), options.get(2), options.get(3)), T("Tidak ada kelereng biru di kantong, jadi kejadian itu tidak dapat terjadi.", "There are no blue marbles in the bag, so it cannot happen."), null);
      text(sure, T("Kantong hanya berisi " + t + " kelereng merah. Mengambil kelereng merah dari kantong itu adalah kejadian ...", "A bag holds only " + t + " red marbles. Picking a red marble from it is ..."), options.get(0), List.of(options.get(1), options.get(2), options.get(3)), T("Semua kelereng berwarna merah, jadi kejadian itu selalu terjadi.", "All the marbles are red, so it always happens."), null);
    }
    for (int k = 1; k <= 5; k++) textFraction(dice, T("Sebuah dadu dilempar sekali. Berapa peluang muncul mata dadu lebih dari " + k + "?", "A die is rolled once. What is the probability of rolling more than " + k + "?"), 6 - k, 6, k, lines(T("Mata dadu lebih dari " + k + " ada ", "Numbers above " + k + ": ") + "6 − " + k + " = " + (6 - k) + ".", T("Seluruh mata dadu ada 6.", "A die has 6 numbers.")));
    for (int part = 1; part <= 11; part++) for (int all = part + 1; all <= 12; all++) textFraction(spinner, T("Roda putar dibagi " + all + " bagian sama besar, dan " + part + " bagian berwarna kuning. Berapa peluang jarum berhenti di warna kuning?", "A spinner has " + all + " equal parts, and " + part + (part == 1 ? " part is" : " parts are") + " yellow. What is the probability of landing on yellow?"), part, all, all - part, lines(T("Bagian kuning: ", "Yellow parts: ") + part + ".", T("Seluruh bagian: ", "All parts: ") + all + "."));
    for (int n = 10; n <= 400; n += 10) essay(coin, T("Sebuah koin dilempar " + n + " kali. Berapa kali sisi angka diharapkan muncul?", "A coin is tossed " + n + " times. How many times are heads expected?"), n / 2 + " " + T("kali", "times"), lines(T("Peluang setiap sisi koin adalah 1/2.", "Each side has a probability of 1/2."), n + " ÷ 2 = " + n / 2 + "."), null);
    for (int n = 12; n <= 600; n += 6) for (int face = 1; face <= 6; face += 5) {
      essay(expected, T("Sebuah dadu dilempar " + n + " kali. Berapa kali mata dadu " + face + " diharapkan muncul?", "A die is rolled " + n + " times. How many times is a " + face + " expected?"), n / 6 + " " + T("kali", "times"), lines(T("Peluang setiap mata dadu adalah 1/6.", "Each number has a probability of 1/6."), n + " ÷ 6 = " + n / 6 + "."), null);
    }
  }

  private void textFraction(Form form, String prompt, int top, int bottom, int other, String method) {
    int g = gcd(top, bottom), a = top / g, b = bottom / g;
    LinkedHashSet<String> wrong = new LinkedHashSet<>();
    int complement = bottom - top;
    if (complement > 0) wrong.add(simplified(complement, bottom));
    if (other > 0 && other != bottom) wrong.add(simplified(Math.min(other, bottom), bottom + 1));
    wrong.add(simplified(1, bottom + 1));
    wrong.add(a + 1 < b ? (a + 1) + "/" + b : a + "/" + (b + 1));
    wrong.add(a + "/" + (b + 2));
    wrong.remove(a + "/" + b);
    String simplifiedLine = g > 1 ? "\n" + T("Disederhanakan: ", "Simplified: ") + top + "/" + bottom + " = " + a + "/" + b + "." : "";
    text(form, prompt, a + "/" + b, new ArrayList<>(wrong), method + simplifiedLine, null);
  }

  private static String simplified(int top, int bottom) { int g = gcd(top, bottom); return top / g + "/" + bottom / g; }

  private void estimation() {
    Form round = form(EASY), sum = form(MEDIUM), difference = form(MEDIUM), product = form(HARD), money = form(ESSAY), roughSum = form(ESSAY);
    for (int i = 0; i < 700; i++) {
      int unit = pow10(between(1, 3)), n = between(unit + 1, unit * 100 - 1);
      if (n % unit == 0 || n % unit == unit / 2) continue;
      int rounded = nearest(n, unit);
      numeric(round, T("Bulatkan " + fmt(n) + " ke kelipatan " + fmt(unit) + " terdekat.", "Round " + fmt(n) + " to the nearest " + fmt(unit) + "."), "", rounded, lines(T("Lihat sisanya: ", "Look at the remainder: ") + fmt(n) + " − " + fmt(n / unit * unit) + " = " + fmt(n % unit) + ".", T("Setengah dari " + fmt(unit) + " adalah " + fmt(unit / 2) + ".", "Half of " + fmt(unit) + " is " + fmt(unit / 2) + ".")), null, rounded + unit, Math.max(unit, rounded - unit), n, rounded + 2 * unit);
      int a = between(101, 899), b = between(101, 899);
      if (a % 100 != 50 && b % 100 != 50) {
        int ra = nearest(a, 100), rb = nearest(b, 100);
        essay(roughSum, T("Perkirakan " + a + " + " + b + " dengan membulatkan keduanya ke ratusan terdekat.", "Estimate " + a + " + " + b + " by rounding both to the nearest hundred."), fmt(ra + rb), lines(a + T(" dibulatkan menjadi ", " rounds to ") + ra + ".", b + T(" dibulatkan menjadi ", " rounds to ") + rb + ".", ra + " + " + rb + " = " + fmt(ra + rb) + "."), null);
        numeric(sum, T("Perkirakan " + a + " + " + b + " dengan membulatkan keduanya ke ratusan terdekat.", "Estimate " + a + " + " + b + " by rounding both to the nearest hundred."), "", ra + rb, lines(a + T(" dibulatkan menjadi ", " rounds to ") + ra + ".", b + T(" dibulatkan menjadi ", " rounds to ") + rb + ".", ra + " + " + rb + " = " + fmt(ra + rb) + "."), null, a + b, ra + rb + 100, ra + rb - 100, ra + rb + 200);
        if (ra != rb) numeric(difference, T("Perkirakan selisih " + a + " dan " + b + " dengan membulatkan keduanya ke ratusan terdekat.", "Estimate the difference between " + a + " and " + b + " by rounding both to the nearest hundred."), "", Math.abs(ra - rb), lines(a + T(" dibulatkan menjadi ", " rounds to ") + ra + ".", b + T(" dibulatkan menjadi ", " rounds to ") + rb + ".", Math.max(ra, rb) + " − " + Math.min(ra, rb) + " = " + Math.abs(ra - rb) + "."), null, Math.abs(a - b), Math.abs(ra - rb) + 100, ra + rb, Math.abs(ra - rb) + 200);
        int brought = (ra + rb) * 1000 + 100000, rest = brought - (ra + rb) * 1000;
        essay(money, T(name(i) + " membawa " + fmt(brought) + " rupiah, lalu membeli barang seharga " + fmt(a * 1000) + " rupiah dan " + fmt(b * 1000) + " rupiah. Bulatkan harganya ke ratus ribuan terdekat. Berapa rupiah perkiraan sisa uangnya?", name(i) + " brings " + fmt(brought) + " rupiah and buys items costing " + fmt(a * 1000) + " rupiah and " + fmt(b * 1000) + " rupiah. Round the prices to the nearest hundred thousand. About how many rupiah remain?"), fmt(rest) + " rupiah",
            lines(fmt(a * 1000) + T(" dibulatkan menjadi ", " rounds to ") + fmt(ra * 1000) + ".", fmt(b * 1000) + T(" dibulatkan menjadi ", " rounds to ") + fmt(rb * 1000) + ".", fmt(ra * 1000) + " + " + fmt(rb * 1000) + " = " + fmt((ra + rb) * 1000) + ".", fmt(brought) + " − " + fmt((ra + rb) * 1000) + " = " + fmt(rest) + "."), null);
      }
      int x = between(11, 99), y = between(11, 99);
      if (x % 10 != 5 && y % 10 != 5 && x % 10 != 0 && y % 10 != 0) {
        int rx = nearest(x, 10), ry = nearest(y, 10);
        numeric(product, T("Perkirakan " + x + " × " + y + " dengan membulatkan keduanya ke puluhan terdekat.", "Estimate " + x + " × " + y + " by rounding both to the nearest ten."), "", rx * ry, lines(x + T(" dibulatkan menjadi ", " rounds to ") + rx + ".", y + T(" dibulatkan menjadi ", " rounds to ") + ry + ".", rx + " × " + ry + " = " + fmt(rx * ry) + "."), null, x * y, rx * ry + 100, Math.max(100, rx * ry - 100), rx + ry);
      }
    }
  }

  private static int nearest(int n, int unit) { return (n + unit / 2) / unit * unit; }

  // ---------- Forms shared by the arithmetic topics ----------

  private void signQuestion(Form form, String expression, int value, int other) {
    String sign = value > other ? ">" : value < other ? "<" : "=";
    List<String> wrong = new ArrayList<>(List.of(">", "<", "=", T("tidak dapat ditentukan", "cannot be told")));
    text(form, T("Tanda yang tepat untuk " + expression + " ... " + fmt(other) + " adalah", "Which sign makes this true: " + expression + " ... " + fmt(other) + "?"), sign, wrong,
        lines(expression + " = " + fmt(value) + ".", T("Bandingkan " + fmt(value) + " dengan " + fmt(other) + ".", "Compare " + fmt(value) + " with " + fmt(other) + ".")), null);
  }

  private void sameValueQuestion(Form form, String expression, int value, String same, List<String> different, String why) {
    text(form, T("Manakah yang hasilnya sama dengan " + expression + "?", "Which has the same value as " + expression + "?"), same, different, lines(expression + " = " + fmt(value) + ".", why), null);
  }

  // ---------- Building ----------

  private Form form(int level) {
    Form form = new Form(level, new ArrayList<>());
    forms.add(form);
    return form;
  }

  /**
   * Numeric choice question. Wrong answers come from the common mistakes given, split so the right answer can land
   * anywhere when the options are sorted; small offsets fill any side that has too few mistakes.
   */
  private void numeric(Form form, String prompt, String unit, int answer, String longMethod, String quickMethod, int... mistakes) {
    List<Integer> below = new ArrayList<>(), above = new ArrayList<>();
    for (int mistake : mistakes) {
      if (mistake > 0 && mistake < answer && !below.contains(mistake)) below.add(mistake);
      if (mistake > answer && !above.contains(mistake)) above.add(mistake);
    }
    Collections.shuffle(below, random);
    Collections.shuffle(above, random);
    int wantBelow = between(0, 3), step = 1;
    while (answer != 0 && answer % (step * 10) == 0) step *= 10;
    for (int offset = step; below.size() < wantBelow && answer - offset > 0; offset += step) if (!below.contains(answer - offset)) below.add(answer - offset);
    int takeBelow = Math.min(wantBelow, below.size());
    for (int offset = step; above.size() < 3 - takeBelow; offset += step) if (!above.contains(answer + offset)) above.add(answer + offset);
    List<String> options = new ArrayList<>(List.of(withUnit(answer, unit)));
    below.stream().limit(takeBelow).forEach(value -> options.add(withUnit(value, unit)));
    above.stream().limit(3 - takeBelow).forEach(value -> options.add(withUnit(value, unit)));
    add(form, prompt, options.get(0), List.copyOf(options), longMethod, quickMethod);
  }

  private void text(Form form, String prompt, String answer, List<String> mistakes, String longMethod, String quickMethod) {
    LinkedHashSet<String> pool = new LinkedHashSet<>(mistakes);
    pool.remove(answer);
    if (pool.size() < 3) return;
    List<String> wrong = new ArrayList<>(pool);
    Collections.shuffle(wrong, random);
    add(form, prompt, answer, List.of(answer, wrong.get(0), wrong.get(1), wrong.get(2)), longMethod, quickMethod);
  }

  private void essay(Form form, String prompt, String answer, String longMethod, String quickMethod) {
    add(form, prompt + T(" Tuliskan langkah-langkahmu.", " Write your steps."), answer, List.of(answer), longMethod, quickMethod);
  }

  private void add(Form form, String prompt, String answer, List<String> options, String longMethod, String quickMethod) {
    form.candidates().add(new Candidate(prompt, options, answer, longMethod, quickMethod));
  }

  private List<MathQuestionBank.MathItem> build() {
    List<MathQuestionBank.MathItem> items = new ArrayList<>();
    Set<String> used = new HashSet<>();
    for (int level = EASY; level <= ESSAY; level++) {
      List<List<Candidate>> pools = new ArrayList<>();
      List<String> keys = new ArrayList<>();
      for (int f = 0; f < forms.size(); f++) if (forms.get(f).level() == level && !forms.get(f).candidates().isEmpty()) {
        List<Candidate> shuffled = new ArrayList<>(forms.get(f).candidates());
        Collections.shuffle(shuffled, random);
        pools.add(shuffled);
        keys.add("f" + f);
      }
      int taken = 0;
      int[] next = new int[pools.size()];
      for (boolean progress = true; progress && taken < MAXIMUM[level]; ) {
        progress = false;
        for (int p = 0; p < pools.size() && taken < MAXIMUM[level]; p++) {
          while (next[p] < pools.get(p).size()) {
            Candidate candidate = pools.get(p).get(next[p]++);
            if (!used.add(candidate.prompt())) continue;
            items.add(item(candidate, level, keys.get(p)));
            taken++;
            progress = true;
            break;
          }
        }
      }
      if (taken < MINIMUM[level]) throw new IllegalStateException("Soal kurang pada level " + level + ": " + taken);
    }
    return List.copyOf(items);
  }

  private MathQuestionBank.MathItem item(Candidate candidate, int level, String key) {
    String conclusion = T("Jadi, jawabannya ", "So, the answer is ") + candidate.answer() + ".";
    String longMethod = candidate.longMethod() + "\n" + conclusion;
    String quickMethod = candidate.quickMethod() == null ? null : candidate.quickMethod() + "\n" + conclusion;
    boolean essay = level == ESSAY;
    return new MathQuestionBank.MathItem(candidate.prompt(), candidate.options(), 0, longMethod, essay, essay ? HARD : level, longMethod, quickMethod, key);
  }

  private String T(String indonesian, String english) { return en ? english : indonesian; }
  private int between(int low, int high) { return high <= low ? low : low + random.nextInt(high - low + 1); }
  private String name(int index) { return NAMES[Math.floorMod(index, NAMES.length)]; }
  private static String lines(String... lines) { return String.join("\n", lines); }
  private static String capital(String text) { return text.substring(0, 1).toUpperCase(java.util.Locale.ROOT) + text.substring(1); }
  private String withUnit(int value, String unit) { return unit.isEmpty() ? fmt(value) : unit.equals("°") || unit.equals("%") ? fmt(value) + unit : fmt(value) + " " + unit; }

  private String fmt(int value) {
    String digits = Integer.toString(value);
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < digits.length(); i++) {
      if (i > 0 && (digits.length() - i) % 3 == 0) out.append(en ? ',' : '.');
      out.append(digits.charAt(i));
    }
    return out.toString();
  }

  /** Writes hundredths as a decimal, e.g. 75 → 0,75 (0.75 in English). */
  private String decimal(int hundredths) {
    String fraction = String.format("%02d", hundredths % 100).replaceAll("0$", "");
    return hundredths / 100 + (en ? "." : ",") + (fraction.isEmpty() ? "0" : fraction);
  }

  private String place(int position) {
    return switch (position) {
      case 0 -> T("satuan", "ones");
      case 1 -> T("puluhan", "tens");
      case 2 -> T("ratusan", "hundreds");
      case 3 -> T("ribuan", "thousands");
      case 4 -> T("puluh ribuan", "ten thousands");
      default -> T("ratus ribuan", "hundred thousands");
    };
  }

  private static int pow10(int exponent) { int value = 1; for (int i = 0; i < exponent; i++) value *= 10; return value; }
  private static int reverseDigits(int n) { int out = 0; for (; n > 0; n /= 10) out = out * 10 + n % 10; return out; }
  private static boolean allDifferent(int n) { String s = Integer.toString(n); return s.chars().distinct().count() == s.length(); }
}
