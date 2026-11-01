package id.belajarsd.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Chapter-level, grade-filtered practice units. See QUESTION_SOURCES.md for scope and references. */
final class CurriculumCatalog {
  record Seed(String subject, int grade, String title, String content) {}

  private static final List<Seed> SEEDS = buildSeeds();

  private CurriculumCatalog() {}

  static void seed(SubjectRepository subjects, LessonRepository lessons) {
    Set<String> activeTitles = new HashSet<>();
    for (Seed seed : SEEDS) {
      Subject subject = subjects.findByName(seed.subject()).orElse(null);
      if (subject == null) continue;
      String grade = "Kelas " + seed.grade();
      activeTitles.add(key(seed.subject(), grade, seed.title()));
      Lesson lesson = lessons.findBySubjectIdAndTitleAndGrade(subject.getId(), seed.title(), grade).orElse(null);
      if (lesson == null) lessons.save(new Lesson(subject, seed.title(), grade, seed.content(), 25));
      else {
        lesson.updateMaterial(seed.content(), 25);
        lessons.save(lesson);
      }
    }
    // Earlier preview material remains available, clearly separated from the current chapter map.
    for (Lesson lesson : lessons.findAllWithSubject()) {
      if (!lesson.getTitle().matches("(?i)^Bab\\s+\\d+\\s*[—–-].*")) continue;
      String subject = lesson.getSubject().getName();
      if (activeTitles.contains(key(subject, lesson.getGrade(), lesson.getTitle()))) continue;
      String supplementalTitle = lesson.getTitle().replaceFirst("(?i)^Bab\\s+\\d+\\s*[—–-]\\s*", "");
      lesson.renameMaterial("Latihan tambahan — " + supplementalTitle, lesson.getContent(), lesson.getEstimatedMinutes());
      lessons.save(lesson);
    }
  }

  private static List<Seed> buildSeeds() {
    List<Seed> result = new ArrayList<>();
    add(result, "Matematika", new String[][] {
      {"Bilangan dan nilai tempat", "Penjumlahan dan pengurangan", "Bentuk dan posisi", "Pola dan pengukuran"},
      {"Bilangan cacah", "Penjumlahan dan pengurangan", "Perkalian dan pembagian", "Pengukuran dan bangun datar"},
      {"Bilangan cacah", "Kalimat matematika", "Perkalian dan pembagian", "Panjang, berat, dan bangun datar"},
      {"Bilangan cacah sampai 10.000", "Pecahan", "Pola gambar dan pola bilangan", "Pengukuran luas dan volume", "Bangun datar", "Piktogram dan diagram batang"},
      {"Bilangan cacah sampai 100.000", "KPK dan FPB", "Bilangan pecahan", "Keliling bangun datar", "Luas daerah bangun datar", "Sudut", "Membandingkan ciri-ciri bangun datar", "Data", "Bilangan cacah sampai 1.000.000"},
      {"Pecahan dan desimal", "Rasio", "Kubus dan balok", "Peluang"}
    });
    add(result, "Bahasa Indonesia", new String[][] {
      {"Bunyi Apa?", "Ayo, Bermain!", "Awas Kuman!", "Aku Bisa!", "Teman Baru", "Aku Ingin", "Temanku Berbeda", "Di Sekitar Rumah"},
      {"Mengenal Perasaan", "Menjaga Kesehatan", "Berhati-hati di Mana Saja", "Keluargaku Unik", "Berteman dalam Keragaman", "Bijak Memakai Uang", "Sayang Lingkungan", "Hobi yang Jadi Prestasi"},
      {"Ayo, Main!", "Kawan Seiring", "Pengobar Semangat", "Senyum di Sekitarku", "Bola-Bola Cokelat", "Tersesat", "Aku dan si Merah", "Sahabat dari Seberang"},
      {"Sudah Besar", "Di Bawah Atap", "Lihat Sekitar", "Meliuk dan Menerjang", "Bertukar atau Membayar", "Satu Titik", "Asal-Usul", "Sehatlah Ragaku"},
      {"Aku yang Unik", "Buku Jendela Dunia", "Ekspresi Diri melalui Hobi", "Belajar Berwirausaha", "Menjadi Warga Dunia", "Cinta Indonesia", "Sayangi Bumi", "Bergerak Bersama"},
      {"Bangga menjadi anak Indonesia", "Musisi Indonesia di panggung dunia", "Taman Nasional dan situs warisan dunia", "Jeda untuk iklim"}
    });
    add(result, "IPAS", new String[][] {
      {"Aku dan Lingkungan Sekitarku", "Bagian Tubuh dan Pancaindra", "Makhluk Hidup di Sekitarku", "Cuaca dan Perubahannya"},
      {"Hewan dan Tumbuhan di Sekitarku", "Tubuhku Sehat", "Benda dan Sifatnya", "Lingkungan Rumah dan Sekolah"},
      {"Mari Kenali Hewan di Sekitar Kita", "Ayo Mengenal Siklus pada Makhluk Hidup", "Hidup Bersama Alam", "Berkenalan dengan Energi", "Aku dan Lingkungan Sekitarku", "Aku Bagian dari Masyarakat", "Cerita dari Kampung Halaman", "Bentang Alam Indonesia"},
      {"Mengubah Bentuk Energi", "Gaya di Sekitar Kita", "Di Sini Tempat Tinggalku", "Iklim dan Perubahannya", "Bagaimana Mendapatkan Semua Keperluan Kita?", "Membangun Masyarakat yang Beradab", "Keragaman Budaya dan Kearifan Lokal", "Menjadi Pahlawan Lingkungan"},
      {"Di Mana Indonesia Berada?", "Majulah Daerahku!", "Harmoni dalam Ekosistem", "Air Sumber Kehidupan", "Daerahku yang Bersejarah", "Perubahan pada Diriku", "Bermain dengan Cahaya", "Ramai karena Bunyi"},
      {"Makanan Bergizi untuk Kesehatan Pencernaanmu", "Tarik dan Embuskan", "Jejak Sang Pahlawan", "Kearifan Lokal, Warisan Leluhur untuk Persatuan Bangsa", "Energi untuk Masa Depan", "Mengembara di Jagat Raya"}
    });
    add(result, "Bahasa Inggris", new String[][] {
      {"Greetings and classroom language", "My friends and family", "Numbers, colors, and things", "My daily activities"},
      {"My school", "My family and friends", "Food and drinks", "Daily routines"},
      {"My Self", "My Family", "My Family Time", "My School", "My Classroom", "My Teachers"},
      {"My Morning Routine", "Meal Time", "My Toys", "My School Activities", "My PE Class", "My School Days"},
      {"What are you doing?", "How many pets do you have?", "Directions and places", "Stories and descriptions"},
      {"About me", "I love cooking", "How was your holiday?", "Stories and information"}
    });
    add(result, "Hitung Cepat", new String[][] {
      {"Membilang dan mengenal angka", "Strategi penjumlahan", "Strategi pengurangan", "Pola bilangan sederhana"},
      {"Nilai tempat", "Penjumlahan cepat", "Pengurangan cepat", "Perkalian awal"},
      {"Fakta perkalian", "Pembagian sebagai pengelompokan", "Operasi campuran", "Pola hitung"},
      {"Operasi bilangan besar", "Fakta perkalian dan pembagian", "Pecahan sederhana", "Strategi pemecahan masalah"},
      {"Operasi pecahan", "Desimal dan persen", "KPK dan FPB", "Estimasi dan hitung mental"},
      {"Operasi campuran", "Rasio dan skala", "Persentase", "Estimasi dan strategi"}
    });
    add(result, "Bahasa Arab", new String[][] {
      {"Salam dan perkenalan", "Angka dan warna", "Benda di kelas", "Keluarga"},
      {"Salam dan identitas", "Angka dan benda sekolah", "Keluarga", "Warna dan sifat"},
      {"Salam dan ungkapan", "Angka dan waktu", "Sekolah dan keluarga", "Kegiatan sehari-hari"},
      {"Keluarga", "Sekolah", "Rumah dan lingkungan", "Kegiatan sehari-hari"},
      {"Anggota Tubuh", "Profesi", "Kebun Binatang", "Di Ruang Tamu dan Ruang Belajar", "Di Laboratorium dan Perpustakaan", "Di Kantin"},
      {"Kegiatan dan waktu", "Kesehatan", "Lingkungan", "Membaca percakapan sederhana"}
    });
    add(result, "PAI", new String[][] {
      {"Mengenal Allah dan ciptaan-Nya", "Al-Qur'an dan huruf hijaiyah", "Aku anak saleh", "Bersuci dan salat"},
      {"Mari mengenal Allah", "Ayo mengenal Al-Qur'an", "Perilaku terpuji", "Praktik ibadah"},
      {"Al-Qur'an dan hadis", "Asmaulhusna dan keimanan", "Akhlak terpuji", "Ibadah dan kisah teladan"},
      {"Membaca dan memahami Al-Qur'an", "Beriman kepada Allah dan rasul", "Akhlak kepada sesama", "Ibadah dan kisah nabi"},
      {"Menyayangi Anak Yatim", "Lebih Dekat dengan Nama-Nama Allah", "Aku Anak Saleh", "Hidup Lapang dengan Berbagi", "Meneladani Perjuangan Rasulullah", "Hidup Damai dalam Kebersamaan", "Ketika Kehidupan Telah Berhenti", "Senangnya Berteman", "Ibadah Haji dan Kurban", "Keteladanan Khulafaurasyidin"},
      {"Mengaji Al-Qur'an", "Iman kepada hari akhir", "Indahnya toleransi", "Keteladanan dan tanggung jawab"}
    });
    add(result, "Mathematics in English", new String[][] {
      {"Numbers and place value", "Addition", "Subtraction", "Shapes and patterns"},
      {"Numbers", "Addition and subtraction", "Multiplication", "Measurement"},
      {"Numbers and operations", "Multiplication and division", "Fractions", "Shapes and measurement"},
      {"Whole numbers", "Fractions", "Patterns", "Measurement and geometry"},
      {"Large numbers", "Factors and multiples", "Fractions and decimals", "Geometry and data"},
      {"Fractions and decimals", "Ratios", "Volume", "Probability"}
    });
    add(result, "Pendidikan Pancasila", new String[][] {
      {"Aku dan teman-temanku", "Aturan di rumah dan sekolah", "Mengenal simbol Pancasila", "Bekerja sama dalam keberagaman"},
      {"Pancasila dalam kehidupan sehari-hari", "Aturan dan tanggung jawab", "Aku bagian dari keluarga dan sekolah", "Mengenal lingkungan tempat tinggal"},
      {"Makna sila-sila Pancasila", "Hak dan kewajiban", "Keberagaman di lingkungan sekitar", "Musyawarah dan gotong royong"},
      {"Pancasila sebagai dasar negara", "Membangun kebersamaan", "Hak dan kewajiban warga", "Bangga menjadi warga Indonesia"},
      {"Nilai Pancasila dalam kehidupan", "Keragaman budaya Indonesia", "Menjaga persatuan", "Mengenal norma dan aturan"},
      {"Pancasila dan kehidupan berbangsa", "Menghargai keberagaman", "Hak, kewajiban, dan tanggung jawab", "Bermusyawarah untuk mufakat"}
    });
    add(result, "PJOK", new String[][] {
      {"Gerak dasar dan keseimbangan", "Permainan gerak sederhana", "Kebugaran dan keselamatan", "Kebiasaan hidup bersih dan sehat"},
      {"Gerak berpindah dan manipulatif", "Permainan sederhana", "Menjaga kebugaran tubuh", "Keselamatan diri dan kebersihan"},
      {"Kombinasi gerak dasar", "Permainan bola dan kerja sama", "Kebugaran jasmani", "Makanan sehat dan keselamatan"},
      {"Gerak lokomotor dan nonlokomotor", "Permainan dan strategi sederhana", "Latihan kebugaran", "Kesehatan diri dan lingkungan"},
      {"Variasi gerak dalam permainan", "Atletik dan aktivitas ritmik", "Kebugaran dan daya tahan", "Gizi, kesehatan, dan keselamatan"},
      {"Keterampilan permainan dan olahraga", "Aktivitas senam dan ritmik", "Merancang latihan kebugaran", "Menjaga kesehatan dan keselamatan"}
    });
    add(result, "Seni", new String[][] {
      {"Mengenal garis, bentuk, dan warna", "Bermain pola bunyi dan irama", "Gerak dan ekspresi tubuh", "Membuat karya dari bahan sekitar"},
      {"Menggambar pengalaman sehari-hari", "Mengenal tempo dan bunyi", "Gerak tari sederhana", "Bercerita melalui gambar dan gerak"},
      {"Unsur rupa dan karya dua dimensi", "Irama dan alat musik sederhana", "Gerak tari daerah", "Bermain peran dan ekspresi"},
      {"Berkarya dengan bentuk dan tekstur", "Menyanyikan lagu dengan pola irama", "Mengamati gerak tari tradisi", "Membuat pertunjukan sederhana"},
      {"Mengembangkan karya seni rupa", "Bermain musik secara berkelompok", "Gerak dan pola lantai tari", "Teater: tokoh dan cerita"},
      {"Apresiasi dan pameran karya rupa", "Mencipta musik sederhana", "Kreasi tari kelompok", "Pementasan dan refleksi karya"}
    });
    return List.copyOf(result);
  }

  private static String key(String subject, String grade, String title) {
    return subject + "|" + grade + "|" + title;
  }

  private static void add(List<Seed> result, String subject, String[][] chapters) {
    add(result, subject, chapters, 1);
  }

  private static void add(List<Seed> result, String subject, String[][] chapters, int firstGrade) {
    for (int gradeIndex = 0; gradeIndex < chapters.length; gradeIndex++) {
      int grade = firstGrade + gradeIndex;
      for (int chapterIndex = 0; chapterIndex < chapters[gradeIndex].length; chapterIndex++) {
        String chapter = chapters[gradeIndex][chapterIndex];
        String title = "Bab " + (chapterIndex + 1) + " — " + chapter;
        String content = material(subject, grade, chapter);
        result.add(new Seed(subject, grade, title, content));
      }
    }
  }

  private static String[] earlyScienceMaterial(String chapter) {
    return switch (chapter) {
      case "Aku dan Lingkungan Sekitarku" -> new String[] {
        "Lingkungan adalah semua yang ada di sekitar kita. Di rumah ada keluarga dan benda-benda yang kita pakai setiap hari. Di sekolah ada guru, teman, dan ruang kelas. Di sekitar kita juga ada tumbuhan, hewan, dan tetangga. Kita ikut menjaga lingkungan agar tetap bersih dan nyaman.",
        "Contoh: setelah makan jajan, bungkusnya dibuang ke tempat sampah. Halaman dan kelas pun tetap bersih."};
      case "Bagian Tubuh dan Pancaindra" -> new String[] {
        "Tubuh kita memiliki kepala, tangan, kaki, dan bagian lainnya. Kita juga memiliki lima pancaindra: mata untuk melihat, telinga untuk mendengar, hidung untuk mencium bau, lidah untuk mengecap rasa, dan kulit untuk meraba. Semua bagian tubuh perlu dijaga kebersihannya.",
        "Contoh: saat makan jeruk, mata melihat warnanya, hidung mencium baunya, lidah mengecap rasanya, dan kulit tangan merasakan permukaannya."};
      case "Makhluk Hidup di Sekitarku" -> new String[] {
        "Manusia, hewan, dan tumbuhan adalah makhluk hidup. Makhluk hidup bernapas, memerlukan makanan dan air, tumbuh, dan berkembang biak. Batu, meja, dan sepatu adalah benda tak hidup karena tidak makan dan tidak tumbuh.",
        "Contoh: anak kucing minum susu dan lama-kelamaan menjadi besar, sedangkan batu di halaman tidak makan dan tidak tumbuh."};
      case "Cuaca dan Perubahannya" -> new String[] {
        "Cuaca adalah keadaan udara yang kita rasakan pada hari itu. Cuaca dapat cerah, berawan, mendung, berangin, atau hujan, dan dapat berubah dalam satu hari. Kita menyesuaikan pakaian dan kegiatan dengan cuaca. Indonesia memiliki musim hujan dan musim kemarau.",
        "Contoh: pagi hari langit cerah, lalu siang hari awan menjadi gelap dan hujan turun. Karena itu, kita menyiapkan payung atau jas hujan."};
      case "Hewan dan Tumbuhan di Sekitarku" -> new String[] {
        "Di sekitar kita ada banyak hewan dan tumbuhan. Hewan berbeda-beda bentuk tubuh, makanan, dan tempat hidupnya. Tumbuhan memiliki bagian seperti akar, batang, daun, bunga, dan buah. Hewan dan tumbuhan perlu dirawat dengan baik.",
        "Contoh: sapi memakan rumput dan ikan hidup di air. Tanaman di pot perlu disiram dan mendapat cahaya matahari agar tumbuh segar."};
      case "Tubuhku Sehat" -> new String[] {
        "Tubuh sehat membuat kita dapat belajar dan bermain dengan senang. Kita menjaga kesehatan dengan makan makanan bergizi, minum air putih, mandi, menyikat gigi, mencuci tangan, berolahraga, dan tidur cukup. Jika merasa sakit, beri tahu orang dewasa.",
        "Contoh: sebelum makan siang, cuci tangan dengan sabun, lalu makan nasi, lauk, sayur, dan buah."};
      case "Benda dan Sifatnya" -> new String[] {
        "Benda dapat berbentuk padat, cair, atau gas. Batu memiliki bentuk tetap, air mengikuti bentuk wadahnya, dan udara mengisi balon. Benda juga memiliki sifat, misalnya keras, lembut, ringan, berat, atau lentur. Sifat benda dapat kita kenali dengan mengamatinya.",
        "Contoh: tuang air ke dalam gelas lalu ke dalam mangkuk. Bentuk air berubah mengikuti wadahnya, sedangkan kelereng tetap bulat."};
      default -> new String[] {
        "Rumah dan sekolah adalah lingkungan yang dekat dengan kita. Di sekolah ada kelas, perpustakaan, kantin, UKS, dan lapangan. Di rumah dan sekolah ada aturan agar semua orang merasa aman dan nyaman. Kita ikut menjaganya dengan piket, gotong royong, dan menghemat air.",
        "Contoh: saat mendapat giliran piket, kita menyapu kelas bersama teman. Kelas menjadi bersih dan nyaman untuk belajar."};
    };
  }

  private static String material(String subject, int grade, String chapter) {
    String topic = chapter.toLowerCase(java.util.Locale.ROOT);
    String objectives;
    String concept;
    String workedExample;
    String practice;
    switch (subject) {
      case "Matematika", "Hitung Cepat", "Mathematics in English" -> {
        objectives = "Setelah belajar, siswa dapat menjelaskan ide utama bab, memilih operasi atau strategi yang sesuai, dan memeriksa kewajaran jawabannya.";
        if (topic.contains("pecahan")) {
          concept = "Pecahan menyatakan bagian dari satu keseluruhan yang dibagi sama besar. Pembilang menunjukkan banyak bagian yang diambil, sedangkan penyebut menunjukkan jumlah seluruh bagian. Pecahan senilai memiliki nilai yang sama meskipun pembilang dan penyebutnya berbeda. Untuk membandingkan pecahan, samakan satuannya atau gunakan gambar dan garis bilangan.";
          workedExample = "Contoh: 1/2 senilai dengan 2/4 karena keduanya menunjukkan setengah bagian. Jika sebuah kue dibagi empat sama besar dan dua potong dimakan, bagian yang dimakan adalah 2/4 atau 1/2.";
        } else if (topic.contains("rasio") || topic.contains("perbandingan")) {
          concept = "Rasio membandingkan dua banyak atau ukuran. Urutan besaran harus dijaga dan satuan perlu disamakan sebelum membandingkan. Rasio dapat disederhanakan dengan membagi kedua bilangan oleh faktor yang sama.";
          workedExample = "Contoh: 4 pensil merah dan 6 pensil biru memiliki rasio merah terhadap biru 4:6. Setelah disederhanakan dengan membagi keduanya oleh 2, rasionya menjadi 2:3.";
        } else if (topic.contains("kpk") || topic.contains("fpb")) {
          concept = "Kelipatan adalah hasil perkalian suatu bilangan dengan bilangan cacah. KPK adalah kelipatan persekutuan terkecil, sedangkan FPB adalah faktor persekutuan terbesar. Daftar kelipatan atau pohon faktor dapat membantu menemukan keduanya.";
          workedExample = "Contoh: kelipatan 4 adalah 4, 8, 12, 16, ... dan kelipatan 6 adalah 6, 12, 18, ... sehingga KPK(4,6)=12. Faktor persekutuannya 1 dan 2, jadi FPB(4,6)=2.";
        } else if (topic.contains("data") || topic.contains("diagram") || topic.contains("piktogram")) {
          concept = "Data adalah kumpulan informasi. Data dapat dicatat dalam tabel lalu disajikan sebagai piktogram atau diagram batang. Judul, label, skala, dan keterangan harus dibaca agar informasi tidak tertukar.";
          workedExample = "Contoh: jika satu gambar mewakili 2 siswa dan ada 4 gambar pada kategori membaca, maka ada 8 siswa yang memilih membaca.";
        } else if (topic.contains("luas") || topic.contains("keliling") || topic.contains("bangun") || topic.contains("geometri") || topic.contains("sudut") || topic.contains("kubus") || topic.contains("balok") || topic.contains("volume")) {
          concept = "Bangun datar dan bangun ruang dikenali dari sisi, titik sudut, rusuk, dan permukaannya. Keliling mengukur panjang batas, luas mengukur banyak satuan persegi yang menutup daerah, dan volume mengukur ruang yang ditempati. Pilih rumus berdasarkan bentuk dan tuliskan satuannya.";
          workedExample = "Contoh: persegi dengan sisi 5 cm mempunyai keliling 4 × 5 = 20 cm dan luas 5 × 5 = 25 cm². Satuan keliling dan luas berbeda.";
        } else if (topic.contains("pola")) {
          concept = "Pola adalah aturan yang berulang atau berubah secara teratur. Amati beberapa suku awal, tentukan apa yang berubah, lalu gunakan aturan itu untuk melanjutkan pola atau mencari suku yang hilang.";
          workedExample = "Contoh: pada pola 3, 6, 9, 12, setiap suku bertambah 3. Suku berikutnya adalah 15.";
        } else if (topic.contains("peluang")) {
          concept = "Peluang menggambarkan seberapa mungkin suatu kejadian terjadi. Kejadian dapat mustahil, mungkin, atau pasti. Bandingkan banyak hasil yang mendukung kejadian dengan seluruh hasil yang mungkin.";
          workedExample = "Contoh: sebuah koin memiliki dua sisi. Pada satu kali lemparan, peluang muncul gambar dan angka sama besar karena masing-masing satu dari dua kemungkinan.";
        } else if (topic.contains("ukur") || topic.contains("panjang") || topic.contains("berat") || topic.contains("waktu")) {
          concept = "Pengukuran membandingkan suatu benda dengan satuan. Pilih alat dan satuan yang sesuai, letakkan titik awal alat ukur dengan benar, lalu catat hasil beserta satuannya. Perhatikan hubungan antarsatuan saat mengubah ukuran.";
          workedExample = "Contoh: 1 meter sama dengan 100 sentimeter. Panjang meja 120 cm dapat ditulis sebagai 1 m 20 cm.";
        } else if (topic.contains("desimal") || topic.contains("persen")) {
          concept = "Nilai tempat menentukan nilai sebuah angka. Pada bilangan desimal, angka di kanan koma menyatakan bagian dari satu, seperti persepuluhan dan perseratusan. Persen berarti per seratus dan dapat dihubungkan dengan pecahan serta desimal.";
          workedExample = "Contoh: 0,5 sama dengan 5/10, 1/2, dan 50%. Bentuk berbeda itu menyatakan nilai yang sama.";
        } else if (topic.contains("tambah") || topic.contains("kurang") || topic.contains("operasi")) {
          concept = "Penjumlahan menggabungkan banyak benda, sedangkan pengurangan mencari sisa atau selisih. Susun nilai tempat sejajar, hitung dari nilai tempat terkecil, dan lakukan pengelompokan bila diperlukan. Periksa hasil dengan operasi kebalikan.";
          workedExample = "Contoh: 38 + 27 = 65. Pemeriksaan dapat dilakukan dengan 65 − 27 = 38.";
        } else {
          concept = "Bilangan dapat dibaca, ditulis, dibandingkan, dan diurutkan dengan memahami nilai tempat. Setiap digit memiliki nilai sesuai posisinya. Gunakan garis bilangan, benda konkret, atau tabel nilai tempat untuk membantu melihat hubungan antarbilangan.";
          workedExample = "Contoh: pada bilangan 352, angka 3 bernilai 300, angka 5 bernilai 50, dan angka 2 bernilai 2. Jadi 352 = 300 + 50 + 2.";
        }
        practice = "Latihan mandiri: buat satu soal baru tentang “" + chapter + "”, tuliskan langkah penyelesaiannya, lalu cek kembali operasi, satuan, dan kewajaran hasil.";
      }
      case "Bahasa Indonesia" -> {
        objectives = "Setelah belajar, siswa dapat memahami informasi utama, menggunakan kosakata yang sesuai, menyampaikan gagasan dengan santun, dan menghasilkan tanggapan atau tulisan sederhana.";
        if (topic.contains("bunyi") || topic.contains("huruf")) {
          concept = "Menyimak bunyi membantu kita membedakan suara dan mengenali kata. Huruf mewakili bunyi dalam tulisan. Baca suku kata dengan menggabungkan bunyi konsonan dan vokal, lalu tulis kata dengan bentuk huruf yang tepat.";
          workedExample = "Contoh: bunyi /b/ dan /a/ digabung menjadi suku kata ba. Suku kata ba dapat ditemukan pada kata baju.";
        } else if (topic.contains("kesehatan") || topic.contains("kuman")) {
          concept = "Teks petunjuk menjelaskan urutan melakukan sesuatu. Perhatikan kata kerja dan penanda urutan seperti pertama, kemudian, dan terakhir. Informasi dari teks dapat digunakan untuk menjelaskan kebiasaan hidup bersih dan sehat.";
          workedExample = "Contoh urutan mencuci tangan: basahi tangan, gunakan sabun, gosok seluruh bagian, bilas, lalu keringkan.";
        } else if (topic.contains("perasaan") || topic.contains("teman") || topic.contains("keluarga") || topic.contains("berbeda")) {
          concept = "Cerita menyampaikan tokoh, peristiwa, dan perasaan. Saat membaca, cari siapa yang terlibat, apa yang terjadi, di mana dan kapan peristiwa berlangsung, serta alasan tindakan tokoh. Gunakan kata yang santun ketika menceritakan pengalaman dan menghargai perbedaan.";
          workedExample = "Contoh: jika tokoh meminjamkan pensil kepada teman, peristiwa itu menunjukkan sikap peduli. Kesimpulan harus didukung oleh kejadian dalam cerita.";
        } else if (topic.contains("informasi") || topic.contains("gagasan") || topic.contains("iklim") || topic.contains("nasional") || topic.contains("dunia")) {
          concept = "Teks informasi menyajikan fakta dan gagasan. Temukan topik, gagasan pokok, dan rincian pendukung pada setiap paragraf. Bedakan informasi yang tertulis dari pendapat pembaca dan gunakan bukti teks saat menyampaikan simpulan.";
          workedExample = "Contoh: apabila paragraf menjelaskan manfaat pohon dengan beberapa bukti, gagasan pokoknya adalah manfaat pohon; contoh manfaat menjadi rincian pendukung.";
        } else {
          concept = "Pembaca memahami teks dengan memperhatikan judul, kosakata, urutan peristiwa, tokoh, dan hubungan sebab-akibat. Penulis menyusun gagasan secara runtut, memakai ejaan yang dapat dibaca, lalu memeriksa kembali tulisannya.";
          workedExample = "Contoh: ringkasan yang baik menyebutkan gagasan penting secara singkat dengan kata-kata sendiri tanpa menambahkan peristiwa yang tidak ada di teks.";
        }
        practice = "Latihan mandiri: baca satu teks pendek tentang “" + chapter + "”, catat tiga informasi penting, lalu ceritakan kembali dengan kalimatmu sendiri.";
      }
      case "IPAS" -> {
        objectives = "Setelah belajar, siswa dapat mengamati gejala, mengajukan pertanyaan, mengelompokkan bukti, menjelaskan hubungan sebab-akibat, dan mengusulkan tindakan yang bertanggung jawab.";
        if (topic.contains("tumbuhan") || topic.contains("hewan") || topic.contains("makhluk") || topic.contains("ekosistem")) {
          concept = "Makhluk hidup memiliki kebutuhan dan ciri yang membantu mereka bertahan. Tumbuhan, hewan, manusia, dan lingkungan saling berhubungan. Dalam ekosistem, perubahan pada satu bagian dapat memengaruhi bagian lain; amati bukti sebelum menarik kesimpulan.";
          workedExample = "Contoh: tumbuhan memerlukan air, cahaya, udara, dan ruang tumbuh. Jika tanaman kekurangan air, daunnya dapat layu; pengamatan beberapa hari membantu menguji hubungan tersebut.";
        } else if (topic.contains("tubuh") || topic.contains("kesehatan")) {
          concept = "Tubuh tersusun atas bagian dan organ yang bekerja bersama. Kebiasaan makan seimbang, bergerak, beristirahat, menjaga kebersihan, dan meminta bantuan orang dewasa saat sakit mendukung kesehatan.";
          workedExample = "Contoh: saat berlari, napas dan denyut jantung meningkat karena tubuh membutuhkan lebih banyak oksigen. Setelah beristirahat, keduanya berangsur kembali.";
        } else if (topic.contains("energi") || topic.contains("gaya") || topic.contains("bunyi")) {
          concept = "Gaya dapat mengubah gerak atau bentuk benda. Energi dapat berpindah dan berubah bentuk, misalnya listrik menjadi cahaya atau gerak. Bunyi berasal dari getaran dan merambat melalui medium.";
          workedExample = "Contoh: saat bola ditendang, gaya kaki mengubah keadaan bola yang semula diam menjadi bergerak. Kipas mengubah energi listrik menjadi gerak.";
        } else if (topic.contains("wujud") || topic.contains("benda") || topic.contains("materi")) {
          concept = "Benda memiliki sifat yang dapat diamati, seperti bentuk, volume, dan kemampuan mengalir. Pemanasan atau pendinginan dapat menyebabkan perubahan wujud. Catat keadaan sebelum dan sesudah percobaan untuk menemukan pola.";
          workedExample = "Contoh: es yang dibiarkan pada suhu ruang menerima panas dan mencair menjadi air. Air yang dipanaskan terus dapat menguap.";
        } else if (topic.contains("bumi") || topic.contains("cuaca") || topic.contains("lingkungan")) {
          concept = "Lingkungan alam dan kehidupan masyarakat dipengaruhi oleh kondisi tempat dan perubahan waktu. Cuaca berbeda dari iklim; cuaca diamati dalam waktu singkat, sedangkan iklim menggambarkan pola yang lebih lama. Perilaku manusia dapat menjaga atau merusak lingkungan.";
          workedExample = "Contoh: catatan hujan dan suhu selama beberapa hari adalah data cuaca. Menutup saluran air dari sampah membantu mengurangi risiko genangan di sekitar rumah.";
        } else {
          concept = "Sumber daya dan kegiatan manusia saling berhubungan. Gunakan pengamatan, peta sederhana, tabel, atau wawancara untuk mengumpulkan informasi. Bandingkan temuan dengan bukti sebelum menyimpulkan.";
          workedExample = "Contoh: catat jenis pekerjaan warga di lingkungan sekitar, kelompokkan hasilnya, lalu jelaskan kebutuhan yang dipenuhi pekerjaan tersebut.";
        }
        practice = "Latihan mandiri: amati contoh “" + chapter + "” di sekitar rumah atau sekolah, tuliskan apa yang terlihat, bukti yang mendukung, dan satu pertanyaan lanjutan.";
        if (grade <= 2) {
          String[] early = earlyScienceMaterial(chapter);
          objectives = "Setelah belajar, siswa dapat mengamati hal di sekitarnya, menyebutkan contohnya, dan menceritakan apa yang dilihat.";
          concept = early[0];
          workedExample = early[1];
          practice = "Latihan mandiri: bersama orang tua atau guru, amati satu contoh “" + chapter + "” di sekitarmu, lalu gambar atau ceritakan apa yang kamu lihat.";
        }
      }
      case "Bahasa Inggris" -> {
        objectives = "By the end of this lesson, learners can recognize key words, understand a short message, and use a simple spoken or written sentence in context.";
        if (topic.contains("name") || topic.contains("self") || topic.contains("greeting")) {
          concept = "Key words: hello, good morning, name, age, and favorite activity. Use “What is your name?” — “My name is ...”, “How old are you?” — “I am ... years old”, and “What do you like?” — “I like ...”. Begin a conversation with a greeting, listen for the question word, and answer with your own information.";
          workedExample = "Example conversation: “Hello, my name is Rani. I am nine years old. I like cycling after school.” The name, age, and activity can be changed to match the speaker.";
        } else if (topic.contains("family")) {
          concept = "Key words: father, mother, brother, sister, grandfather, and grandmother. Use “This is my ...” to introduce a family member and “Where is your family from?” to ask about origin. Use he/she when talking about one person and my/your to show whose family member is meant.";
          workedExample = "Example: “This is my sister. Her name is Sari. My family is from Bandung.” The first sentence introduces a person, the second gives a name, and the last tells a place of origin.";
        } else if (topic.contains("meal") || topic.contains("food") || topic.contains("eat")) {
          concept = "Key words: rice, bread, egg, vegetables, fruit, breakfast, lunch, and dinner. Use “I eat ... for breakfast/lunch/dinner” to tell what you eat and “Do you like ...?” to ask about a preference. Answer with “Yes, I do” or “No, I don't” when appropriate.";
          workedExample = "Example: “I eat rice and eggs for breakfast. I like bananas.” Name the meal first, then say the food. Use a food word that matches the picture or situation.";
        } else if (topic.contains("school") || topic.contains("classroom") || topic.contains("teacher")) {
          concept = "Key words: classroom objects, school subjects, people at school, and classroom activities. Use “This is a ...” to name an object, “I have ...” for something you possess, and “I study ...” or “I learn ...” for a school activity. Follow classroom instructions such as open, close, listen, read, and write.";
          workedExample = "Example: “This is my classroom. I have a pencil and a book. I study English on Monday.” Match each sentence to the object, place, or activity it describes.";
        } else if (topic.contains("routine") || topic.contains("morning") || topic.contains("daily")) {
          concept = "Key words: wake up, take a bath, have breakfast, go to school, and go to bed. Use the simple present to describe a regular routine: “I wake up at six” and “I go to school in the morning”. Put the actions in the order they happen and use time expressions clearly.";
          workedExample = "Example: “First, I wake up. Then, I take a bath. I have breakfast before I go to school.” Sequence words help the listener follow the routine.";
        } else if (topic.contains("toy") || topic.contains("things") || topic.contains("object")) {
          concept = "Key words: names of toys and familiar objects, colors, and numbers. Use “I have a ...”, “It is ...”, and “How many ...?” to describe an object, its color, or its quantity. Use a/an before one countable object and plural -s for more than one when the word follows that pattern.";
          workedExample = "Example: “I have two kites. They are blue.” The number tells how many kites there are; the color describes them.";
        } else {
          concept = "Learn the meaning and pronunciation of the key words in “" + chapter + "”. Notice the people, objects, actions, or places in the topic. Use a model sentence, replace one word at a time, and pay attention to word order and context.";
          workedExample = "Example: “This is my book.” names an object; “I like reading.” shares a preference. Say each sentence aloud, then change one detail to make it true for you.";
        }
        practice = "Independent practice: write three short sentences about “" + chapter + "”, read them aloud, and check that each sentence matches its meaning.";
      }
      case "Bahasa Arab" -> {
        objectives = "Setelah belajar, siswa dapat mengenali kosakata tema, memahami maknanya, menirukan pelafalan dengan bimbingan, dan menggunakannya dalam ungkapan sederhana.";
        concept = "Pelajari kosakata bahasa Arab yang berkaitan dengan “" + chapter + "” sebagai pasangan bunyi dan makna. Dengarkan contoh pelafalan, ulangi perlahan, lalu kelompokkan kata berdasarkan benda, orang, tempat, atau kegiatan. Gunakan tulisan Arab dari buku pendamping guru agar bentuk huruf dan harakat dipelajari dengan benar.";
        workedExample = "Contoh: cocokkan kartu kosakata dengan gambar yang tepat, ucapkan kata setelah guru, lalu gunakan satu kata dalam ungkapan pendek sesuai contoh pelajaran.";
        practice = "Latihan mandiri: buat kartu lima kosakata tema “" + chapter + "” dengan bantuan guru/orang tua, lalu sebutkan maknanya tanpa melihat sisi jawaban.";
      }
      case "PAI" -> {
        objectives = "Setelah belajar, siswa dapat menjelaskan pesan utama materi, memberi contoh penerapan yang baik, dan menunjukkan sikap santun serta bertanggung jawab.";
        concept = "Materi “" + chapter + "” dipelajari dengan memahami pesan, membiasakan perilaku baik, dan menghubungkannya dengan kehidupan sehari-hari. Dengarkan penjelasan guru, gunakan buku ajar untuk bacaan atau tata cara ibadah yang tepat, serta bertanya kepada guru/orang tua bila ada bacaan yang belum dipahami.";
        workedExample = "Contoh penerapan: pilih tindakan yang jujur, menjaga kebersihan, menghormati orang lain, dan menolong sesuai kemampuan. Jelaskan alasan tindakan tersebut dengan bahasa yang baik.";
        practice = "Latihan mandiri: tuliskan satu pesan yang kamu pelajari dari “" + chapter + "” dan satu tindakan nyata yang akan kamu biasakan hari ini.";
      }
      default -> {
        objectives = "Siswa dapat memahami pokok bahasan dan menerapkannya dalam latihan sederhana.";
        concept = "Pelajari konsep “" + chapter + "” secara bertahap. Hubungkan istilah baru dengan contoh yang dekat dengan pengalaman sehari-hari, lalu periksa kembali pemahaman dengan menjelaskan menggunakan kata-kata sendiri.";
        workedExample = "Contoh: buat peta konsep kecil berisi topik, tiga gagasan penting, dan satu contoh penerapan.";
        practice = "Latihan mandiri: jelaskan satu hal baru yang dipahami tentang “" + chapter + "” kepada teman atau keluarga.";
      }
    }
    return "TUJUAN BELAJAR\n" + objectives + "\n\nMATERI INTI\n" + concept
        + "\n\nCONTOH TERBIMBING\n" + workedExample + "\n\nLATIHAN MANDIRI\n" + practice
        + "\n\nRANGKUMAN\nPahami konsep, gunakan bukti atau langkah yang tepat, dan periksa kembali hasil belajar. Materi ini merupakan uraian belajar orisinal; gunakan buku pegangan dan arahan guru untuk rincian yang khusus mengikuti edisi sekolah.";
  }
}
