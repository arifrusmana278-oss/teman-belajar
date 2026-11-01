package id.belajarsd.app;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/** Original practice questions aligned to official curriculum outcomes; see QUESTION_SOURCES.md. */
final class QuestionBank {
  record Item(String prompt, List<String> options, int correctOptionIndex, String explanation) {}

  private QuestionBank() {}

  static List<Item> forLesson(String title) {
    return switch (title) {
      case "Penjumlahan sampai 20" -> addition(false);
      case "Everyday Greetings" -> greetings();
      case "Perkalian sebagai penjumlahan berulang" -> multiplication();
      case "Memahami kalimat" -> sentences();
      case "Bagian tumbuhan" -> plants();
      case "Salam sederhana" -> arabicGreetings();
      case "Membiasakan perilaku jujur" -> honesty();
      case "Addition facts" -> addition(true);
      default -> List.of();
    };
  }

  private static List<Item> addition(boolean english) {
    List<Item> result = new ArrayList<>(50);
    for (int i = 0; i < 50; i++) {
      int first = 1 + i / 5;
      int second = 1 + i % 5 + (i / 25) * 5;
      int sum = first + second;
      String prompt = english
          ? first + " + " + second + " = ?"
          : first + " + " + second + " = ...";
      String explanation = english
          ? "Count on " + second + " steps from " + first + ". The total is " + sum + "."
          : "Hitung maju " + second + " langkah dari " + first + ". Hasilnya " + sum + ".";
      result.add(item(prompt, Integer.toString(sum), Integer.toString(sum + 1),
          Integer.toString(sum - 1), Integer.toString(sum + 2), explanation));
    }
    return result;
  }

  private static List<Item> multiplication() {
    List<Item> result = new ArrayList<>(50);
    for (int i = 0; i < 50; i++) {
      int groups = 2 + i / 10;
      int each = 1 + i % 10;
      int total = groups * each;
      result.add(item(groups + " × " + each + " = ...", Integer.toString(total),
          Integer.toString(total + groups), Integer.toString(total - 1), Integer.toString(total + 1),
          groups + " kelompok yang masing-masing berisi " + each + " benda berarti "
              + "penjumlahan berulang sebanyak " + groups + " kali, yaitu " + total + "."));
    }
    return result;
  }

  private static List<Item> greetings() {
    String[] names = {"Alya", "Bima", "Citra", "Doni", "Eka", "Farah", "Gita", "Hadi", "Intan", "Joko"};
    String[][] cases = {
        {"It is morning. What should %s say when meeting a friend?", "Good morning", "Good night", "Goodbye", "Sorry", "Use ‘Good morning’ to greet someone in the morning."},
        {"A friend helps %s carry some books. What should %s say?", "Thank you", "Good night", "Goodbye", "Excuse me", "Say ‘Thank you’ to show gratitude."},
        {"%s meets a teacher in the afternoon. Which greeting fits?", "Good afternoon", "Good morning", "Good night", "Goodbye", "Use ‘Good afternoon’ during the afternoon."},
        {"%s is going to bed. What can %s say to the family?", "Good night", "Good morning", "Thank you", "Good afternoon", "‘Good night’ is used when someone is going to bed or parting at night."},
        {"School has ended and %s is leaving. What can %s say to a friend?", "Goodbye", "Good morning", "Thank you", "Good night", "‘Goodbye’ is a polite way to part from someone."}
    };
    List<Item> result = new ArrayList<>(50);
    for (String[] c : cases) {
      for (String name : names) {
        String prompt = c[0].formatted(name, name);
        result.add(item(prompt, c[1], c[2], c[3], c[4], c[5]));
      }
    }
    return result;
  }

  private static List<Item> sentences() {
    String[] subjects = {"Budi", "Siti", "Ayah", "Ibu", "Dina", "Rani", "Kakak", "Adik", "Pak Guru", "Mereka"};
    String[] actions = {"pergi ke sekolah", "membaca buku", "menyiram tanaman", "bermain bola", "makan bersama"};
    List<Item> result = new ArrayList<>(50);
    for (String subject : subjects) {
      for (String action : actions) {
        String plain = subject + " " + action;
        String correct = plain + ".";
        String lowercase = plain.substring(0, 1).toLowerCase(Locale.ROOT) + plain.substring(1) + ".";
        result.add(item("Pilih penulisan yang benar untuk kalimat: " + plain + ".", correct,
            lowercase, plain, plain + "?",
            "Kalimat pernyataan diawali huruf kapital dan diakhiri tanda titik."));
      }
    }
    return result;
  }

  private static List<Item> plants() {
    String[] plantNames = {"mangga", "jagung", "mawar", "padi", "tomat", "kacang", "kelapa", "cabai", "bunga matahari", "bayam"};
    String[][] parts = {
        {"Bagian tumbuhan %s yang menyerap air dan mineral dari tanah adalah ...", "akar", "batang", "daun", "bunga", "Akar menyerap air dan mineral dari tanah."},
        {"Bagian tumbuhan %s yang menopang tubuhnya dan menyalurkan air adalah ...", "batang", "akar", "bunga", "biji", "Batang menopang tumbuhan dan membantu menyalurkan air."},
        {"Bagian tumbuhan %s yang menangkap cahaya untuk membantu membuat makanan adalah ...", "daun", "akar", "buah", "biji", "Daun menangkap cahaya dan menjadi tempat utama tumbuhan membuat makanan."},
        {"Bagian tumbuhan %s yang berperan dalam perkembangbiakan dan dapat berkembang menjadi buah adalah ...", "bunga", "akar", "batang", "daun", "Bunga berperan dalam perkembangbiakan tumbuhan dan dapat berkembang menjadi buah."},
        {"Bagian %s yang dapat tumbuh menjadi tanaman baru adalah ...", "biji", "daun", "batang", "bunga", "Biji dapat tumbuh menjadi tanaman baru jika mendapat kondisi yang sesuai."}
    };
    List<Item> result = new ArrayList<>(50);
    for (String[] part : parts) {
      for (String plant : plantNames) {
        result.add(item(part[0].formatted(plant), part[1], part[2], part[3], part[4], part[5]));
      }
    }
    return result;
  }

  private static List<Item> arabicGreetings() {
    String[] names = {"Alya", "Bima", "Citra", "Doni", "Eka", "Farah", "Gita", "Hadi", "Intan", "Joko"};
    String[][] cases = {
        {"%s bertemu gurunya dan ingin mengucapkan salam. Ungkapan yang tepat adalah ...", "Assalamu'alaikum", "Shukran", "Ma'a as-salamah", "Afwan", "Assalamu'alaikum adalah ungkapan salam."},
        {"Teman menyapa %s dengan ‘Assalamu'alaikum’. Jawaban yang tepat adalah ...", "Wa'alaikumussalam", "Shukran", "Ma'a as-salamah", "Marhaban", "Jawaban salam ‘Assalamu'alaikum’ adalah ‘Wa'alaikumussalam’."},
        {"%s menerima bantuan dari teman. Ungkapan bahasa Arab untuk ‘terima kasih’ adalah ...", "Shukran", "Afwan", "Marhaban", "Ma'a as-salamah", "‘Shukran’ berarti terima kasih."},
        {"Seseorang berkata ‘Shukran’ kepada %s. Jawaban sopan yang berarti ‘sama-sama’ adalah ...", "Afwan", "Shukran", "Marhaban", "Assalamu'alaikum", "‘Afwan’ dapat digunakan untuk menjawab ucapan terima kasih."},
        {"%s berpamitan kepada temannya. Ungkapan bahasa Arab yang sesuai adalah ...", "Ma'a as-salamah", "Shukran", "Afwan", "Wa'alaikumussalam", "‘Ma'a as-salamah’ digunakan untuk berpamitan."}
    };
    List<Item> result = new ArrayList<>(50);
    for (String[] c : cases) {
      for (String name : names) result.add(item(c[0].formatted(name), c[1], c[2], c[3], c[4], c[5]));
    }
    return result;
  }

  private static List<Item> honesty() {
    String[] names = {"Alya", "Bima", "Citra", "Doni", "Eka", "Farah", "Gita", "Hadi", "Intan", "Joko"};
    String[][] cases = {
        {"%s tidak sengaja memecahkan gelas di rumah. Apa yang sebaiknya dilakukan?", "Mengakui kejadian dan meminta bantuan orang dewasa", "Menyembunyikan pecahan", "Menyalahkan teman", "Pura-pura tidak tahu", "Mengakui kejadian sesuai kenyataan adalah sikap jujur. Minta bantuan orang dewasa agar pecahan ditangani dengan aman."},
        {"%s menemukan uang di halaman sekolah. Sikap jujur yang tepat adalah ...", "Menyerahkan uang kepada guru", "Menyimpannya diam-diam", "Membaginya dengan teman", "Membuangnya", "Uang yang ditemukan sebaiknya diserahkan kepada guru agar dapat dikembalikan kepada pemiliknya."},
        {"Kasir memberi kembalian lebih kepada %s. Apa tindakan yang jujur?", "Mengembalikan kelebihan uang kepada kasir", "Menyimpan uang itu", "Berpura-pura tidak menyadari", "Memberikannya kepada teman", "Sampaikan bahwa kembalian berlebih dan kembalikan kelebihannya."},
        {"%s lupa mengerjakan pekerjaan rumah. Apa yang sebaiknya dikatakan kepada guru?", "Mengatakan yang sebenarnya dan meminta arahan", "Mengaku bukunya tertinggal padahal tidak", "Menyalin pekerjaan teman", "Menyalahkan anggota keluarga", "Berkata jujur tentang pekerjaan yang belum selesai dan meminta arahan."},
        {"%s melihat pensil milik teman tertinggal di kelas. Sikap yang jujur adalah ...", "Mengembalikannya kepada pemilik atau guru", "Membawanya pulang", "Menyembunyikannya", "Mengatakan pensil itu miliknya", "Barang yang bukan milik kita harus dikembalikan kepada pemiliknya atau diserahkan kepada guru."}
    };
    List<Item> result = new ArrayList<>(50);
    for (String[] c : cases) {
      for (String name : names) result.add(item(c[0].formatted(name), c[1], c[2], c[3], c[4], c[5]));
    }
    return result;
  }

  private static Item item(String prompt, String correct, String wrong1, String wrong2,
      String wrong3, String explanation) {
    LinkedHashSet<String> options = new LinkedHashSet<>();
    options.add(correct);
    options.add(wrong1);
    options.add(wrong2);
    options.add(wrong3);
    return new Item(prompt, List.copyOf(options), 0, explanation);
  }
}
