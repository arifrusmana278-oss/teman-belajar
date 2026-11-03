package id.belajarsd.app;

import java.util.ArrayList;
import java.util.List;

/**
 * Chapter banks. Every non-math chapter has its own written bank in resources/question-bank, except the IPAS
 * grade 1–2 introduction units, which are below.
 * Every question is a distinct fact, so a chapter's bank size is the number of new questions it can offer a day.
 */
final class CurriculumQuestionBank {

  private CurriculumQuestionBank() {}

  static List<QuestionBank.Item> forLesson(Lesson lesson) {
    int grade = gradeNumber(lesson.getGrade());
    String subject = lesson.getSubject().getName();
    List<QuestionBank.Item> written = FactBank.forChapter(subject, grade, lesson.getTitle());
    if (!written.isEmpty()) return written;
    return switch (subject) {
      case "Matematika", "Hitung Cepat" -> arithmetic(grade, false);
      case "Mathematics in English" -> arithmetic(grade, true);
      case "IPAS" -> grade <= 2 ? factBank(earlyScience(lesson.getTitle())) : List.of();
      default -> List.of();
    };
  }

  /** One question per fact: rewording a fact does not make a new question. */
  private static List<QuestionBank.Item> factBank(String[][] facts) {
    List<QuestionBank.Item> bank = new ArrayList<>(facts.length);
    for (String[] fact : facts) bank.add(question(fact[0], fact[1], fact[2], fact[3], fact[4], fact[5]));
    return bank;
  }

  private static List<QuestionBank.Item> arithmetic(int grade, boolean english) {
    List<QuestionBank.Item> bank = new ArrayList<>(50);
    for (int i = 0; i < 50; i++) {
      int a, b, answer;
      String prompt;
      if (grade <= 2) {
        a = (grade == 1 ? 1 : 5) + (i * 3 % (grade == 1 ? 10 : 35)); b = 1 + (i * 7 % (grade == 1 ? 10 : 30));
        if ((i & 1) == 0) { answer = a + b; prompt = a + " + " + b + " = ?"; }
        else { if (b > a) { int t = a; a = b; b = t; } answer = a - b; prompt = a + " − " + b + " = ?"; }
      } else if (grade <= 4) {
        a = 2 + i % 9; b = 2 + (i * 3 % 9);
        if ((i & 1) == 0) { answer = a * b; prompt = a + " × " + b + " = ?"; }
        else { answer = a; prompt = (a * b) + " ÷ " + b + " = ?"; }
      } else if (grade == 5) {
        int denominator = 2 + i % 5, numerator = 1 + i % (denominator - 1), whole = denominator * (2 + i % 12);
        answer = whole / denominator * numerator;
        prompt = "Berapakah " + numerator + "/" + denominator + " dari " + whole + "?";
      } else {
        int percent = 10 + (i % 9) * 10, whole = 100 + (i / 9) * 100;
        answer = whole * percent / 100;
        prompt = "Berapakah " + percent + "% dari " + whole + "?";
      }
      String explanation = english ? "Calculate the operation carefully. The answer is " + answer + "." : "Hitung operasi tersebut dengan teliti. Hasilnya " + answer + ".";
      String p = english ? prompt.replace("Berapakah ", "What is ").replace(" dari ", " of ").replace("?", "?") : prompt;
      bank.add(question(p, Integer.toString(answer), Integer.toString(answer + 1), Integer.toString(Math.max(0, answer - 1)), Integer.toString(answer + 2), explanation));
    }
    return bank;
  }

  private static String[][] earlyScience(String title) {
    String topic = title.toLowerCase(java.util.Locale.ROOT);
    if (topic.contains("aku dan lingkungan")) return new String[][] {
      {"Tempat kita tinggal bersama keluarga disebut ...","rumah","sungai","hutan","pasar","Rumah adalah tempat kita tinggal bersama keluarga."},
      {"Tempat kita belajar bersama guru dan teman adalah ...","sekolah","kebun","pantai","stasiun","Di sekolah kita belajar bersama guru dan teman."},
      {"Orang yang mengajar kita di sekolah disebut ...","guru","dokter","petani","nelayan","Guru mengajar dan membimbing siswa di sekolah."},
      {"Tempat memasak makanan di rumah disebut ...","dapur","kamar tidur","garasi","teras","Makanan biasanya dimasak di dapur."},
      {"Benda yang ada di dalam kelas contohnya ...","papan tulis","perahu","traktor","kompor","Papan tulis dipakai guru saat mengajar di kelas."},
      {"Sampah sebaiknya dibuang ke ...","tempat sampah","sungai","selokan","halaman tetangga","Membuang sampah di tempatnya membuat lingkungan bersih."},
      {"Agar kelas tetap bersih, kita dapat ...","menyapu lantai","mencoret meja","membuang sampah di lantai","menginjak kursi","Menyapu lantai membuat kelas bersih dan nyaman."},
      {"Saat bertemu tetangga, kita sebaiknya ...","menyapa dengan sopan","mengejek","berteriak","membuang muka","Menyapa dengan sopan membuat hubungan dengan tetangga menjadi baik."},
      {"Tumbuhan yang dapat ditemukan di halaman rumah contohnya ...","pohon mangga","kucing","sepeda","batu","Pohon mangga adalah tumbuhan."},
      {"Menyiram tanaman di halaman termasuk kegiatan ...","merawat lingkungan","merusak lingkungan","mengotori lingkungan","mengganggu tetangga","Tanaman yang disiram dapat tumbuh dan membuat lingkungan asri."}};
    if (topic.contains("pancaindra")) return new String[][] {
      {"Kita melihat benda dengan ...","mata","telinga","hidung","lidah","Mata adalah indra penglihat."},
      {"Kita mendengar suara dengan ...","telinga","mata","kulit","lidah","Telinga adalah indra pendengar."},
      {"Kita mencium bau bunga dengan ...","hidung","telinga","mata","kaki","Hidung adalah indra pencium."},
      {"Rasa manis pada gula dapat kita kenali dengan ...","lidah","mata","telinga","rambut","Lidah adalah indra pengecap."},
      {"Kita dapat merasakan kain yang halus dengan ...","kulit","telinga","hidung","gigi","Kulit adalah indra peraba."},
      {"Jumlah pancaindra manusia adalah ...","lima","dua","tiga","sepuluh","Pancaindra terdiri atas mata, telinga, hidung, lidah, dan kulit."},
      {"Bagian tubuh yang kita pakai untuk berjalan adalah ...","kaki","telinga","hidung","rambut","Kita berjalan menggunakan kaki."},
      {"Bagian tubuh yang kita pakai untuk memegang pensil adalah ...","tangan","kaki","telinga","lutut","Tangan dipakai untuk memegang benda."},
      {"Agar mata tetap sehat, sebaiknya kita membaca di tempat yang ...","terang","gelap","berasap","berdebu","Tempat yang terang membuat mata tidak cepat lelah."},
      {"Agar telinga tetap sehat, kita sebaiknya ...","menjauhi suara yang terlalu keras","memasukkan benda kecil ke telinga","berteriak di dekat telinga teman","membiarkan telinga kotor","Suara yang terlalu keras dapat merusak pendengaran."}};
    if (topic.contains("makhluk hidup")) return new String[][] {
      {"Yang termasuk makhluk hidup adalah ...","kucing","batu","meja","sepatu","Kucing bernapas, makan, tumbuh, dan bergerak."},
      {"Yang termasuk benda tak hidup adalah ...","batu","ayam","pohon","ikan","Batu tidak makan, tidak bernapas, dan tidak tumbuh."},
      {"Agar dapat hidup, hewan dan manusia membutuhkan ...","makanan dan minuman","mainan dan buku","kursi dan meja","tas dan sepatu","Makanan dan minuman dibutuhkan untuk hidup dan tumbuh."},
      {"Agar tumbuh dengan baik, tanaman membutuhkan ...","air dan cahaya matahari","mainan dan buku","kursi dan meja","tas dan sepatu","Tanaman membutuhkan air dan cahaya matahari untuk tumbuh."},
      {"Ikan hidup di ...","air","pohon","lemari","awan","Ikan hidup dan berenang di air."},
      {"Burung berpindah tempat dengan cara ...","terbang","berenang di dasar laut","menggali tanah","melata","Burung menggunakan sayapnya untuk terbang."},
      {"Anak ayam lama-kelamaan menjadi ayam dewasa. Ini menunjukkan makhluk hidup dapat ...","tumbuh","membeku","mencair","berkarat","Makhluk hidup tumbuh menjadi lebih besar."},
      {"Makhluk hidup bernapas untuk mendapatkan ...","udara","pasir","batu","kertas","Makhluk hidup membutuhkan udara untuk bernapas."},
      {"Hewan yang berkaki empat contohnya ...","kambing","ayam","ikan","ular","Kambing berjalan dengan empat kaki."},
      {"Yang termasuk tumbuhan adalah ...","pohon pisang","kelinci","kupu-kupu","sepeda","Pohon pisang adalah tumbuhan."}};
    if (topic.contains("cuaca")) return new String[][] {
      {"Saat hujan turun, benda yang kita pakai agar tidak basah adalah ...","payung","kacamata","topi ulang tahun","sandal","Payung melindungi tubuh dari air hujan."},
      {"Jika Matahari bersinar dan langit biru, cuacanya ...","cerah","hujan lebat","berkabut tebal","bersalju","Langit biru dan sinar Matahari menandakan cuaca cerah."},
      {"Langit yang tertutup awan gelap disebut ...","mendung","cerah","pelangi","terik","Awan gelap membuat langit mendung dan sering menjadi tanda akan hujan."},
      {"Pakaian basah paling cepat kering jika dijemur saat cuaca ...","cerah","hujan","mendung gelap","badai","Panas Matahari membantu pakaian cepat kering."},
      {"Setelah hujan, kadang di langit tampak ...","pelangi","salju","gunung","sungai","Pelangi dapat muncul ketika cahaya Matahari mengenai titik air."},
      {"Saat cuaca panas, kita sebaiknya ...","minum air yang cukup","memakai jaket tebal","berhenti minum","bermain di bawah terik sepanjang hari","Minum air yang cukup membantu tubuh tidak kekurangan cairan."},
      {"Layang-layang mudah diterbangkan ketika ada ...","angin","hujan","petir","kabut","Tiupan angin membantu layang-layang naik."},
      {"Saat hujan disertai petir, kita sebaiknya ...","berlindung di dalam bangunan","berdiri di bawah pohon","bermain di lapangan","berenang di sungai","Berlindung di dalam bangunan lebih aman saat ada petir."},
      {"Pagi cerah lalu siang hujan. Ini menunjukkan cuaca dapat ...","berubah","berhenti selamanya","dimakan","dibeli","Cuaca dapat berubah dalam waktu singkat."},
      {"Dua musim di Indonesia adalah musim hujan dan musim ...","kemarau","salju","gugur","semi","Indonesia mengalami musim hujan dan musim kemarau."}};
    if (topic.contains("hewan dan tumbuhan")) return new String[][] {
      {"Hewan yang sering dipelihara di rumah contohnya ...","kucing","harimau","buaya","hiu","Kucing adalah hewan peliharaan yang jinak."},
      {"Makanan utama sapi adalah ...","rumput","ikan","daging","batu","Sapi adalah hewan pemakan tumbuhan."},
      {"Ayam berkembang biak dengan cara ...","bertelur","melahirkan","membelah diri","bertunas","Ayam betina bertelur, lalu telur dapat menetas."},
      {"Hewan yang susunya sering kita minum adalah ...","sapi","ayam","ikan","kupu-kupu","Sapi menghasilkan susu."},
      {"Bagian tumbuhan yang biasanya berada di dalam tanah adalah ...","akar","bunga","buah","daun","Akar berada di dalam tanah dan menyerap air."},
      {"Warna daun yang masih segar biasanya ...","hijau","ungu","hitam","biru","Sebagian besar daun segar berwarna hijau."},
      {"Sayuran yang berasal dari tumbuhan contohnya ...","bayam","telur","susu","ikan","Bayam adalah sayuran dari tumbuhan."},
      {"Agar tanaman di pot tidak layu, kita perlu ...","menyiramnya","menutupnya dari cahaya","mencabut akarnya","memotong semua daunnya","Tanaman membutuhkan air agar tetap segar."},
      {"Ikan hias di rumah dapat dipelihara di ...","akuarium","kandang ayam","lemari","kotak pasir","Ikan membutuhkan air sehingga dipelihara di akuarium atau kolam."},
      {"Cara menyayangi hewan peliharaan adalah ...","memberi makan dan menjaga kandangnya tetap bersih","memukulnya","membiarkannya kelaparan","menarik ekornya","Hewan peliharaan perlu dirawat dengan baik."}};
    if (topic.contains("tubuhku sehat")) return new String[][] {
      {"Agar gigi tetap sehat, kita sebaiknya menyikat gigi ...","pagi dan sebelum tidur","sebulan sekali","hanya saat sakit gigi","tidak perlu","Menyikat gigi pagi dan sebelum tidur membantu menjaga gigi."},
      {"Sebelum makan, kita sebaiknya ...","mencuci tangan dengan sabun","bermain tanah","memegang sampah","menggaruk kaki","Mencuci tangan dengan sabun membantu menghilangkan kuman."},
      {"Makanan yang menyehatkan contohnya ...","sayur dan buah","permen setiap saat","minuman manis setiap saat","keripik setiap saat","Sayur dan buah mengandung zat yang baik bagi tubuh."},
      {"Minuman terbaik saat kita haus adalah ...","air putih","minuman bersoda","kopi","sirup yang sangat manis","Air putih membantu memenuhi kebutuhan cairan tubuh."},
      {"Agar tubuh tidak lelah keesokan harinya, pada malam hari kita perlu ...","tidur cukup","bermain gawai sampai larut","begadang","makan permen","Tidur cukup membantu tubuh beristirahat."},
      {"Berlari dan bersepeda termasuk kegiatan ...","olahraga","tidur","memasak","membaca","Olahraga membuat tubuh bugar."},
      {"Agar badan bersih, kita perlu ...","mandi setiap hari","memakai baju kotor","jarang mandi","bermain di selokan","Mandi membersihkan badan dari kotoran dan keringat."},
      {"Kuku yang panjang sebaiknya ...","dipotong","digigit","dibiarkan kotor","dicat dengan spidol","Kuku pendek lebih mudah dijaga kebersihannya."},
      {"Jika merasa sakit, kita sebaiknya ...","memberi tahu orang dewasa","menyembunyikannya","minum obat sendiri tanpa izin","tetap bermain sampai lelah","Orang dewasa dapat membantu kita mendapatkan perawatan yang tepat."},
      {"Saat batuk, kita sebaiknya ...","menutup mulut dengan lengan atau tisu","batuk di depan wajah teman","meludah sembarangan","berteriak","Menutup mulut membantu mengurangi penyebaran kuman."}};
    if (topic.contains("benda dan sifatnya")) return new String[][] {
      {"Benda yang terasa keras contohnya ...","batu","kapas","busa","bantal","Batu terasa keras saat dipegang."},
      {"Benda yang terasa lembut contohnya ...","kapas","batu","paku","kelereng","Kapas terasa lembut."},
      {"Air termasuk benda ...","cair","padat","gas","keras","Air dapat mengalir sehingga termasuk benda cair."},
      {"Es batu termasuk benda ...","padat","cair","gas","lunak","Es batu memiliki bentuk tetap sehingga termasuk benda padat."},
      {"Udara di dalam balon termasuk benda ...","gas","padat","cair","keras","Udara termasuk benda gas."},
      {"Air yang dituang ke dalam gelas akan mengikuti bentuk ...","gelasnya","mejanya","tangan kita","atap rumah","Benda cair mengikuti bentuk wadahnya."},
      {"Es batu yang dibiarkan di tempat panas akan ...","mencair","membesar","menjadi batu","menjadi kayu","Panas membuat es batu mencair menjadi air."},
      {"Benda yang mudah pecah jika jatuh contohnya ...","gelas kaca","bola karet","bantal","kain","Kaca mudah pecah jika jatuh."},
      {"Karet gelang dapat ditarik lalu kembali seperti semula karena bersifat ...","lentur","rapuh","cair","panas","Karet bersifat lentur."},
      {"Benda yang lebih ringan daripada batu bata adalah ...","bulu ayam","kursi besi","lemari kayu","karung beras","Bulu ayam jauh lebih ringan daripada batu bata."}};
    return new String[][] {
      {"Tempat meminjam dan membaca buku di sekolah disebut ...","perpustakaan","kantin","lapangan","gudang","Perpustakaan menyediakan buku untuk dibaca dan dipinjam."},
      {"Tempat membeli makanan di sekolah disebut ...","kantin","perpustakaan","kelas","UKS","Kantin menjual makanan dan minuman."},
      {"Jika sakit atau terluka di sekolah, kita dapat pergi ke ...","UKS","kantin","lapangan","gudang","UKS adalah tempat pertolongan pertama di sekolah."},
      {"Tugas membersihkan kelas secara bergiliran disebut ...","piket","upacara","ulangan","istirahat","Piket adalah tugas menjaga kebersihan kelas secara bergiliran."},
      {"Bekerja bersama membersihkan lingkungan rumah disebut ...","gotong royong","bertengkar","bermalas-malasan","berebut","Gotong royong membuat pekerjaan terasa lebih ringan."},
      {"Orang yang tinggal di dekat rumah kita disebut ...","tetangga","kepala sekolah","masinis","pilot","Tetangga adalah orang yang tinggal di sekitar rumah kita."},
      {"Contoh aturan sekolah yang baik adalah ...","datang tepat waktu","mencoret dinding","membuang sampah di kelas","mengganggu teman","Datang tepat waktu membantu kegiatan belajar berjalan tertib."},
      {"Dinding sekolah harus dijaga dengan ...","tidak mencoretnya","menggambarinya dengan spidol","memukulnya","menempelkan permen karet","Fasilitas sekolah digunakan bersama sehingga perlu dijaga."},
      {"Setelah memakai air di rumah, kita sebaiknya ...","menutup keran","membiarkan air mengalir","merusak keran","bermain air sepanjang hari","Menutup keran membantu menghemat air."},
      {"Saluran air di sekitar rumah harus bebas dari sampah agar ...","air mengalir lancar","air tidak bisa lewat","lingkungan menjadi bau","banyak nyamuk bersarang","Saluran yang bersih membantu mengurangi genangan."}};
  }

  private static QuestionBank.Item question(String prompt, String answer, String wrong1, String wrong2, String wrong3, String explanation) {
    List<String> options = new ArrayList<>(4);
    for (String option : List.of(answer, wrong1, wrong2, wrong3)) if (!options.contains(option)) options.add(option);
    for (int fallback = 1; options.size() < 4; fallback++) options.add("Pilihan lain " + fallback);
    return new QuestionBank.Item(prompt, List.copyOf(options), 0, explanation);
  }

  private static int gradeNumber(String value) {
    var matcher = java.util.regex.Pattern.compile("\\d+").matcher(value);
    return matcher.find() ? Integer.parseInt(matcher.group()) : 1;
  }
}
