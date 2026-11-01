package id.belajarsd.app;

import java.util.ArrayList;
import java.util.List;

/** Original, repeatable practice banks for the grade-level starter topics. */
final class CurriculumQuestionBank {
  private record Fact(String prompt, String answer, String wrong1, String wrong2, String wrong3, String explanation) {}
  private record Word(String local, String english) {}
  private static final String[] NAMES = {"Alya", "Bima", "Citra", "Doni", "Eka", "Farah", "Gita", "Hadi", "Intan", "Joko"};

  private CurriculumQuestionBank() {}

  static List<QuestionBank.Item> forLesson(Lesson lesson) {
    int grade = gradeNumber(lesson.getGrade());
    return switch (lesson.getSubject().getName()) {
      case "Matematika", "Hitung Cepat" -> arithmetic(grade, false);
      case "Mathematics in English" -> arithmetic(grade, true);
      case "Bahasa Inggris" -> english(grade);
      case "Bahasa Indonesia" -> indonesian(grade);
      case "IPAS" -> science(grade);
      case "Bahasa Arab" -> arabic(grade);
      case "PAI" -> religion(grade);
      case "Pendidikan Pancasila" -> pancasila(lesson.getTitle());
      case "PJOK" -> pjok(lesson.getTitle());
      case "Seni" -> arts(lesson.getTitle());
      default -> List.of();
    };
  }

  private static List<QuestionBank.Item> pancasila(String title) {
    String topic = title.toLowerCase(java.util.Locale.ROOT);
    Fact[] facts;
    if (topic.contains("musyawarah") || topic.contains("mufakat")) facts = new Fact[] {
      new Fact("Saat menentukan tujuan kelas bersama, sikap yang tepat adalah ...", "Mendengarkan pendapat semua teman", "Memaksakan pilihan sendiri", "Mengabaikan teman yang berbeda pendapat", "Memutuskan tanpa berdiskusi", "Musyawarah memberi kesempatan kepada semua orang untuk menyampaikan pendapat."),
      new Fact("Keputusan bersama sebaiknya dilaksanakan dengan ...", "Tanggung jawab", "Terpaksa sambil mengganggu", "Menyalahkan teman", "Mengubahnya diam-diam", "Hasil kesepakatan perlu dihormati dan dilaksanakan bersama."),
      new Fact("Jika pendapat kita tidak dipilih dalam musyawarah, kita sebaiknya ...", "Menghormati keputusan bersama", "Marah dan meninggalkan kelompok", "Mengejek pendapat yang dipilih", "Menghalangi keputusan", "Menghargai keputusan bersama menjaga kerukunan."),
      new Fact("Contoh sikap adil saat berdiskusi adalah ...", "Memberi kesempatan setiap anggota berbicara", "Hanya mendengar ketua", "Memotong semua pembicaraan", "Menolak semua usul", "Setiap anggota berhak menyampaikan pendapat dengan tertib."),
      new Fact("Musyawarah dilakukan untuk mencapai ...", "Kesepakatan bersama", "Kemenangan satu orang", "Pertengkaran", "Hukuman bagi teman", "Musyawarah bertujuan mencari keputusan yang dapat diterima bersama."),
      new Fact("Saat teman berbicara dalam diskusi, kita sebaiknya ...", "Menyimak dengan sopan", "Berteriak lebih keras", "Bermain sendiri", "Menertawakannya", "Menyimak menunjukkan sikap menghargai."),
      new Fact("Jika belum tercapai kesepakatan, kelompok dapat ...", "Berdiskusi kembali dengan tenang", "Langsung bertengkar", "Membubarkan kelompok sambil marah", "Memaksa teman setuju", "Diskusi dapat dilanjutkan untuk mencari pilihan yang adil."),
      new Fact("Ketua musyawarah yang baik akan ...", "Membantu diskusi berjalan tertib", "Menentukan semua jawaban sendiri", "Melarang anggota berbicara", "Memihak teman dekat", "Pemimpin diskusi menjaga agar semua anggota dapat berpartisipasi."),
      new Fact("Menghargai perbedaan pendapat berarti ...", "Tidak merendahkan pendapat orang lain", "Selalu menganggap diri paling benar", "Menolak berbicara dengan teman", "Memaksa semua orang sama", "Perbedaan pendapat disampaikan dan ditanggapi dengan santun."),
      new Fact("Contoh keputusan bersama di kelas adalah ...", "Menyepakati jadwal piket", "Memilih sendiri tanpa bertanya", "Mengubah aturan diam-diam", "Menyuruh satu teman mengerjakan semuanya", "Jadwal piket dapat dibuat melalui kesepakatan kelas.")
    };
    else if (topic.contains("hak") || topic.contains("kewajiban") || topic.contains("tanggung jawab")) facts = new Fact[] {
      new Fact("Contoh kewajiban siswa di kelas adalah ...", "Menjaga kebersihan kelas", "Merusak meja", "Mengganggu teman belajar", "Mengambil barang teman", "Siswa ikut bertanggung jawab menjaga lingkungan belajar."),
      new Fact("Hak seorang siswa di sekolah adalah ...", "Mendapat kesempatan belajar", "Bebas merusak fasilitas", "Selalu didahulukan", "Tidak menaati aturan", "Setiap siswa berhak memperoleh pendidikan dan perlakuan yang baik."),
      new Fact("Setelah memakai buku perpustakaan, kita berkewajiban ...", "Mengembalikannya tepat waktu", "Mencoret semua halaman", "Menyimpannya selamanya", "Membuangnya", "Mengembalikan buku menjaga hak pembaca berikutnya."),
      new Fact("Menjalankan kewajiban dengan baik menunjukkan sikap ...", "Bertanggung jawab", "Acuh tak acuh", "Suka menyalahkan", "Tidak peduli", "Tanggung jawab berarti melaksanakan tugas dan menerima akibatnya."),
      new Fact("Hak dan kewajiban sebaiknya dilaksanakan secara ...", "Seimbang", "Terpisah tanpa aturan", "Hanya oleh orang lain", "Semaunya", "Hak perlu dihormati sambil menjalankan kewajiban."),
      new Fact("Contoh tanggung jawab di rumah adalah ...", "Merapikan mainan setelah digunakan", "Meninggalkan sampah", "Merusak barang", "Menyuruh orang lain selalu", "Merapikan barang adalah bentuk tanggung jawab."),
      new Fact("Saat meminjam alat tulis teman, kita sebaiknya ...", "Menjaga dan mengembalikannya", "Menyembunyikannya", "Mengaku sebagai pemilik", "Merusaknya", "Barang pinjaman harus dijaga dan dikembalikan."),
      new Fact("Mematuhi aturan keselamatan di sekolah membantu ...", "Melindungi diri dan orang lain", "Membuat kegiatan berbahaya", "Mengganggu kelas", "Menghilangkan hak teman", "Aturan dibuat antara lain untuk menjaga keamanan bersama."),
      new Fact("Jika tidak sengaja merusak barang kelas, tindakan bertanggung jawab adalah ...", "Mengakui dan melapor kepada guru", "Menyalahkan teman", "Menyembunyikan kerusakan", "Pergi tanpa memberi tahu", "Mengakui kejadian membantu memperbaiki masalah dengan jujur."),
      new Fact("Ketika menggunakan fasilitas umum, kita harus ...", "Menjaganya agar dapat dipakai bersama", "Mencoret dan merusaknya", "Menghalangi orang lain", "Membawanya pulang", "Fasilitas bersama perlu dijaga untuk kepentingan semua orang.")
    };
    else if (topic.contains("keberagaman") || topic.contains("keragaman") || topic.contains("persatuan") || topic.contains("budaya")) facts = new Fact[] {
      new Fact("Sikap yang tepat kepada teman yang berbeda budaya adalah ...", "Menghormati kebiasaannya", "Mengejek bahasanya", "Menjauhinya", "Memaksanya mengikuti kebiasaan kita", "Keberagaman perlu dihargai agar semua merasa aman dan diterima."),
      new Fact("Gotong royong berarti ...", "Bekerja bersama untuk kepentingan bersama", "Bekerja sendiri sambil menyalahkan orang", "Menghindari tugas", "Memaksa satu orang bekerja", "Gotong royong dilakukan dengan saling membantu."),
      new Fact("Bahasa yang berbeda di daerah Indonesia menunjukkan ...", "Keberagaman budaya", "Alasan untuk bertengkar", "Bahwa satu daerah lebih baik", "Bahwa teman tidak boleh bermain", "Indonesia memiliki beragam bahasa dan budaya."),
      new Fact("Saat bekerja kelompok dengan teman yang berbeda, kita sebaiknya ...", "Bekerja sama dan saling menghargai", "Memilih hanya teman yang sama", "Menolak berbagi tugas", "Mengejek cara bicaranya", "Kerja sama mempererat persatuan dalam keberagaman."),
      new Fact("Contoh menjaga persatuan di sekolah adalah ...", "Bermain rukun dengan semua teman", "Membuat kelompok untuk mengejek", "Bertengkar karena perbedaan", "Melarang teman ikut bermain", "Kerukunan membantu menjaga persatuan."),
      new Fact("Jika melihat teman diejek karena asal daerahnya, kita sebaiknya ...", "Membela dengan sopan dan meminta bantuan guru", "Ikut mengejek", "Merekam lalu menyebarkan", "Membiarkannya selalu", "Menghentikan ejekan dan mencari bantuan menunjukkan kepedulian."),
      new Fact("Kerja bakti membersihkan halaman merupakan contoh ...", "Gotong royong", "Persaingan", "Musyawarah yang gagal", "Hukuman", "Kerja bakti dilakukan bersama demi kepentingan bersama."),
      new Fact("Menghargai budaya daerah lain dapat dilakukan dengan ...", "Mempelajarinya dengan sikap terbuka", "Menyebutnya aneh", "Melarang orang merayakannya", "Mengubahnya tanpa izin", "Sikap terbuka membantu kita menghormati budaya yang beragam."),
      new Fact("Semboyan Bhinneka Tunggal Ika mengajarkan bahwa kita ...", "Berbeda-beda tetapi tetap satu", "Harus memiliki kebiasaan yang sama", "Tidak boleh berteman dengan perbedaan", "Hanya satu budaya yang penting", "Bhinneka Tunggal Ika menegaskan persatuan dalam keberagaman."),
      new Fact("Saat membagi tugas kelompok, sebaiknya ...", "Membagi tugas dengan adil", "Memberikan semua tugas kepada satu teman", "Tidak memberi tugas kepada teman tertentu", "Memilih berdasarkan ejekan", "Pembagian tugas yang adil membuat semua anggota dapat berperan.")
    };
    else facts = new Fact[] {
      new Fact("Lambang negara Indonesia adalah ...", "Garuda Pancasila", "Harimau Sumatra", "Komodo", "Burung merak", "Garuda Pancasila adalah lambang negara Indonesia."),
      new Fact("Dasar negara Indonesia adalah ...", "Pancasila", "Pancaindra", "Pancawarna", "Pancalomba", "Pancasila menjadi dasar negara Indonesia."),
      new Fact("Simbol sila pertama Pancasila adalah ...", "Bintang", "Rantai", "Pohon beringin", "Kepala banteng", "Bintang melambangkan sila Ketuhanan Yang Maha Esa."),
      new Fact("Simbol sila kedua Pancasila adalah ...", "Rantai", "Bintang", "Padi dan kapas", "Kepala banteng", "Rantai melambangkan kemanusiaan yang adil dan beradab."),
      new Fact("Simbol sila ketiga Pancasila adalah ...", "Pohon beringin", "Rantai", "Bintang", "Padi dan kapas", "Pohon beringin melambangkan persatuan Indonesia."),
      new Fact("Contoh sikap sesuai sila keempat adalah ...", "Bermusyawarah menentukan keputusan kelas", "Memaksakan kehendak", "Mengejek teman", "Tidak mau mendengar", "Sila keempat mengajarkan musyawarah dan perwakilan."),
      new Fact("Contoh sikap adil kepada teman adalah ...", "Berbagi giliran menggunakan alat bermain", "Mengambil semua alat", "Memilih teman tertentu saja", "Melanggar antrean", "Bersikap adil berarti memberi hak secara patut."),
      new Fact("Saat teman beribadah, kita sebaiknya ...", "Menghormati dan tidak mengganggu", "Menertawakan", "Membuat keributan", "Melarangnya", "Menghormati ibadah orang lain menunjukkan sikap toleran."),
      new Fact("Contoh perilaku sesuai nilai persatuan adalah ...", "Menolong teman tanpa membeda-bedakan", "Mengejek teman berbeda daerah", "Menolak bekerja sama", "Membuat pertengkaran", "Persatuan tumbuh saat kita saling menolong dan menghargai."),
      new Fact("Pancasila terdiri atas ... sila", "Lima", "Tiga", "Empat", "Enam", "Kata panca berarti lima; Pancasila memiliki lima sila.")
    };
    return factBank(facts);
  }

  private static List<QuestionBank.Item> pjok(String title) {
    String topic = title.toLowerCase(java.util.Locale.ROOT);
    Fact[] facts;
    if (topic.contains("sehat") || topic.contains("gizi") || topic.contains("kesehatan") || topic.contains("kebersihan")) facts = new Fact[] {
      new Fact("Agar tubuh memperoleh zat gizi beragam, kita sebaiknya ...", "Makan makanan beragam dengan gizi seimbang", "Hanya makan makanan manis", "Tidak pernah makan sayur", "Mengganti air dengan minuman bersoda", "Makanan beragam membantu memenuhi kebutuhan gizi tubuh."),
      new Fact("Kebiasaan yang membantu mencegah kuman sebelum makan adalah ...", "Mencuci tangan dengan sabun", "Menyeka tangan pada baju kotor", "Tidak mencuci tangan", "Berbagi alat makan yang kotor", "Mencuci tangan dengan sabun membantu menghilangkan kotoran dan kuman."),
      new Fact("Saat merasa haus setelah beraktivitas, minuman yang baik adalah ...", "Air putih", "Minuman yang tidak diketahui isinya", "Air sabun", "Minyak", "Air membantu mengganti cairan tubuh."),
      new Fact("Sebelum berolahraga, kita sebaiknya melakukan ...", "Pemanasan", "Langsung berlari secepat mungkin", "Makan sebanyak-banyaknya", "Tidur di lapangan", "Pemanasan menyiapkan tubuh untuk bergerak."),
      new Fact("Setelah berolahraga, tubuh perlu ...", "Pendinginan dan istirahat secukupnya", "Terus dipaksa bergerak tanpa jeda", "Tidak minum sama sekali", "Langsung tidur di jalan", "Pendinginan dan istirahat membantu tubuh pulih."),
      new Fact("Agar gigi tetap sehat, kita perlu ...", "Menyikat gigi secara teratur", "Menggigit benda keras", "Tidak pernah membersihkan gigi", "Berbagi sikat gigi", "Menyikat gigi teratur menjaga kebersihan gigi dan mulut."),
      new Fact("Jika tubuh terasa sakit saat berolahraga, kita sebaiknya ...", "Berhenti dan memberi tahu guru", "Memaksakan diri terus bermain", "Menyembunyikan rasa sakit", "Mendorong teman", "Berhenti dan meminta bantuan menjaga keselamatan."),
      new Fact("Tidur yang cukup membantu tubuh ...", "Beristirahat dan memulihkan tenaga", "Menjadi selalu lelah", "Tidak membutuhkan makanan", "Berhenti tumbuh", "Istirahat yang cukup mendukung kesehatan dan kebugaran."),
      new Fact("Sampah saat beraktivitas sebaiknya ...", "Dibuang ke tempat sampah", "Ditinggalkan di lapangan", "Dimasukkan ke saluran air", "Dilempar kepada teman", "Menjaga kebersihan lingkungan merupakan bagian dari hidup sehat."),
      new Fact("Saat batuk atau bersin, kita sebaiknya ...", "Menutup mulut dan hidung dengan benar", "Menghadap dekat ke teman", "Meminjamkan sapu tangan kotor", "Meludah sembarangan", "Menutup batuk dan bersin membantu menjaga kesehatan orang lain.")
    };
    else if (topic.contains("permainan") || topic.contains("olahraga") || topic.contains("bola") || topic.contains("atletik")) facts = new Fact[] {
      new Fact("Dalam permainan beregu, keberhasilan tim terbantu oleh ...", "Kerja sama dan komunikasi", "Bermain sendiri tanpa aturan", "Menyalahkan teman", "Mengganggu lawan", "Kerja sama membantu anggota tim mencapai tujuan bersama."),
      new Fact("Sebelum permainan dimulai, pemain sebaiknya ...", "Memahami aturan permainan", "Mengubah aturan diam-diam", "Mendorong pemain lain", "Mengambil alat tanpa izin", "Memahami aturan membuat permainan aman dan adil."),
      new Fact("Sikap yang baik kepada lawan setelah pertandingan adalah ...", "Menghormati dan memberi selamat", "Mengejek yang kalah", "Merusak alat permainan", "Menolak berjabat tangan dengan marah", "Sikap sportif menghargai semua pemain."),
      new Fact("Jika bola keluar dari lapangan, pemain sebaiknya ...", "Mengikuti aturan untuk memulai kembali permainan", "Bertengkar menentukan pemilik bola", "Menyembunyikan bola", "Meninggalkan tim", "Permainan dilanjutkan sesuai kesepakatan dan aturan."),
      new Fact("Saat berlari di lintasan, kita harus ...", "Memperhatikan arah dan ruang di sekitar", "Mendorong pelari lain", "Menutup mata", "Berlari melawan arah tanpa melihat", "Memperhatikan lintasan membantu mencegah tabrakan."),
      new Fact("Alat olahraga setelah dipakai sebaiknya ...", "Dikembalikan ke tempatnya", "Ditinggalkan di tengah lapangan", "Dilempar ke teman", "Dibawa pulang tanpa izin", "Mengembalikan alat menjaga kerapian dan keselamatan."),
      new Fact("Ketika teman terjatuh saat bermain, tindakan tepat adalah ...", "Berhenti membantu dan memanggil guru bila perlu", "Menertawakannya", "Merebut tempatnya", "Meninggalkannya tanpa melihat", "Pertolongan dan bantuan orang dewasa dapat mencegah cedera bertambah."),
      new Fact("Gerak berpindah tempat seperti berjalan dan berlari disebut gerak ...", "Lokomotor", "Diam", "Bernapas", "Melempar", "Gerak lokomotor memindahkan tubuh dari satu tempat ke tempat lain."),
      new Fact("Menangkap bola dengan aman dilakukan dengan ...", "Memperhatikan arah bola dan menggunakan kedua tangan", "Membelakangi bola", "Menutup mata", "Mendorong teman", "Fokus pada bola membantu menangkap dengan aman."),
      new Fact("Bermain secara sportif berarti ...", "Jujur dan menaati aturan", "Curang agar selalu menang", "Mengubah skor", "Mengganggu pemain lain", "Sportivitas mencakup kejujuran dan ketaatan pada aturan.")
    };
    else if (topic.contains("kebugaran") || topic.contains("latihan") || topic.contains("daya tahan")) facts = new Fact[] {
      new Fact("Kebugaran jasmani membantu kita ...", "Melakukan kegiatan sehari-hari dengan bugar", "Tidak perlu tidur", "Tidak perlu makan", "Menghindari semua gerakan", "Kebugaran mendukung kegiatan sehari-hari."),
      new Fact("Latihan perlu dilakukan dengan ...", "Teratur dan sesuai kemampuan", "Berlebihan sampai cedera", "Tanpa pemanasan", "Memaksakan teman", "Latihan bertahap dan teratur lebih aman."),
      new Fact("Contoh aktivitas yang melatih daya tahan adalah ...", "Berjalan atau berlari dengan tempo sesuai kemampuan", "Duduk sepanjang hari", "Menonton televisi", "Mengangkat benda sangat berat tanpa bantuan", "Aktivitas aerobik dapat melatih daya tahan bila dilakukan dengan aman."),
      new Fact("Untuk menjaga keseimbangan saat bergerak, kita dapat ...", "Mengatur posisi tubuh dan memperhatikan pijakan", "Berlari dengan mata tertutup", "Mendorong teman", "Berdiri di tempat licin", "Posisi tubuh dan pijakan membantu menjaga keseimbangan."),
      new Fact("Jika napas menjadi sangat sesak saat latihan, kita harus ...", "Berhenti, beristirahat, dan memberi tahu guru", "Memaksa diri lebih cepat", "Mengejek teman", "Menahan napas", "Tubuh perlu beristirahat dan guru perlu diberi tahu jika ada keluhan."),
      new Fact("Agar latihan aman, tempat berolahraga sebaiknya ...", "Rata dan bebas dari benda berbahaya", "Penuh benda tajam", "Licin dan gelap", "Berada di jalan ramai", "Lingkungan yang aman mengurangi risiko cedera."),
      new Fact("Kegiatan peregangan membantu tubuh ...", "Bergerak lebih siap dan lentur", "Berhenti membutuhkan air", "Tidak perlu pemanasan", "Menjadi kebal cedera", "Peregangan yang sesuai membantu kesiapan gerak."),
      new Fact("Kebugaran meningkat lebih baik jika latihan dilakukan ...", "Bertahap dari ringan sesuai kemampuan", "Sekali saja dengan beban berlebihan", "Tanpa jeda setiap saat", "Saat sedang cedera", "Peningkatan latihan dilakukan bertahap dan memperhatikan kondisi tubuh."),
      new Fact("Saat berolahraga di luar ruangan yang panas, kita perlu ...", "Minum air dan beristirahat bila perlu", "Mengabaikan rasa haus", "Memakai pakaian sangat tebal", "Berlatih tanpa henti", "Minum dan istirahat membantu menjaga kondisi tubuh."),
      new Fact("Mengukur kemajuan latihan sebaiknya dilakukan dengan ...", "Mencatat perkembangan secara jujur", "Mengubah hasil agar terlihat hebat", "Membandingkan untuk mengejek", "Tidak memperhatikan kondisi tubuh", "Catatan jujur membantu melihat perkembangan dan menentukan latihan selanjutnya.")
    };
    else facts = new Fact[] {
      new Fact("Gerak lokomotor adalah gerak yang ...", "Memindahkan tubuh dari satu tempat ke tempat lain", "Tidak melibatkan tubuh", "Hanya dilakukan sambil tidur", "Selalu menggunakan alat musik", "Berjalan dan berlari adalah contoh gerak lokomotor."),
      new Fact("Gerak nonlokomotor dilakukan ...", "Tanpa berpindah tempat", "Dengan selalu berlari jauh", "Hanya saat berenang", "Dengan melempar benda ke orang", "Membungkuk dan memutar badan termasuk gerak nonlokomotor."),
      new Fact("Gerak manipulatif biasanya melibatkan ...", "Penggunaan atau pengendalian benda", "Membaca buku saja", "Tidur", "Bernyanyi tanpa bergerak", "Melempar dan menangkap bola merupakan gerak manipulatif."),
      new Fact("Contoh gerak lokomotor adalah ...", "Melompat ke depan", "Membungkukkan badan di tempat", "Memutar pergelangan tangan", "Berdiri diam", "Melompat memindahkan tubuh ke tempat lain."),
      new Fact("Sebelum melakukan gerakan baru, kita sebaiknya ...", "Mendengarkan petunjuk guru", "Mencoba dengan cara berbahaya", "Mendorong teman lebih dulu", "Mengabaikan ruang sekitar", "Petunjuk guru membantu melakukan gerakan dengan benar dan aman."),
      new Fact("Saat bergerak bersama teman, kita perlu ...", "Menjaga jarak agar tidak bertabrakan", "Menutup mata", "Saling mendorong", "Berlari tanpa arah", "Menjaga jarak membuat aktivitas kelompok lebih aman."),
      new Fact("Gerakan mengayun lengan di tempat termasuk gerak ...", "Nonlokomotor", "Lokomotor", "Manipulatif", "Istirahat", "Mengayun lengan tidak memindahkan tubuh."),
      new Fact("Gerakan melempar bola termasuk gerak ...", "Manipulatif", "Diam", "Membaca", "Tidur", "Melempar melibatkan pengendalian benda."),
      new Fact("Saat belajar keterampilan gerak, kita sebaiknya ...", "Berlatih bertahap dan memperhatikan keselamatan", "Terburu-buru tanpa arahan", "Memaksakan tubuh saat sakit", "Mengganggu teman", "Latihan bertahap membantu keterampilan berkembang dengan aman."),
      new Fact("Jika belum berhasil melakukan gerakan, kita dapat ...", "Mencoba lagi dengan arahan dan latihan", "Mengejek teman", "Menyerah tanpa bertanya", "Melakukan gerakan berbahaya", "Latihan dan umpan balik membantu memperbaiki gerakan.")
    };
    return factBank(facts);
  }

  private static List<QuestionBank.Item> arts(String title) {
    String topic = title.toLowerCase(java.util.Locale.ROOT);
    Fact[] facts;
    if (topic.contains("musik") || topic.contains("irama") || topic.contains("bunyi") || topic.contains("tempo")) facts = new Fact[] {
      new Fact("Pola bunyi panjang dan pendek dalam musik disebut ...", "Irama", "Warna", "Tekstur", "Bayangan", "Irama tersusun dari bunyi dan ketukan dalam waktu."),
      new Fact("Cepat atau lambatnya lagu disebut ...", "Tempo", "Garis", "Bentuk", "Warna", "Tempo menunjukkan kecepatan lagu."),
      new Fact("Alat musik yang dimainkan dengan cara dipukul contohnya ...", "Gendang", "Seruling", "Biola", "Kecapi petik", "Gendang menghasilkan bunyi saat membrannya dipukul."),
      new Fact("Suara dalam musik dapat berasal dari ...", "Alat musik dan suara manusia", "Benda yang tidak pernah bergetar", "Warna kertas saja", "Bayangan", "Musik dapat dibuat dengan alat musik maupun suara manusia."),
      new Fact("Saat bernyanyi bersama, kita sebaiknya ...", "Menjaga tempo dan mendengarkan kelompok", "Bernyanyi sekeras mungkin tanpa mendengar", "Mengubah lagu teman", "Berhenti setiap saat", "Mendengarkan kelompok membantu menjaga kekompakan."),
      new Fact("Bunyi dapat dihasilkan oleh benda yang ...", "Bergetar", "Tidak bergerak dan tidak bergetar", "Berubah warna", "Memiliki bayangan", "Getaran menghasilkan bunyi."),
      new Fact("Pola ketukan yang teratur membantu lagu terasa ...", "Terjaga iramanya", "Tidak memiliki bunyi", "Berubah menjadi gambar", "Selalu lebih lambat", "Ketukan teratur menjadi dasar irama."),
      new Fact("Salah satu contoh alat musik tiup adalah ...", "Seruling", "Gong", "Kendang", "Marakas", "Seruling dimainkan dengan meniup dan mengatur aliran udara."),
      new Fact("Ketika memainkan alat musik bersama, kita perlu ...", "Menunggu giliran dan mengikuti aba-aba", "Memainkan alat teman tanpa izin", "Membuat bunyi sesuka hati", "Mengganggu pemain lain", "Aba-aba membantu kelompok bermain bersama dengan tertib."),
      new Fact("Lagu dengan tempo cepat biasanya dinyanyikan ...", "Dengan ketukan yang lebih cepat", "Tanpa ketukan", "Selalu dengan suara berbisik", "Dengan berhenti pada setiap kata", "Tempo cepat memiliki ketukan yang bergerak lebih cepat.")
    };
    else if (topic.contains("tari") || topic.contains("gerak") || topic.contains("lantai")) facts = new Fact[] {
      new Fact("Dalam tari, gerak tubuh digunakan untuk ...", "Menyampaikan ekspresi atau gagasan", "Menghitung angka saja", "Mengukur panjang meja", "Membuat alat olahraga", "Gerak tari dapat menyampaikan perasaan dan cerita."),
      new Fact("Pola lantai menunjukkan ...", "Susunan posisi penari di ruang", "Warna kostum saja", "Kecepatan membaca", "Jumlah alat musik", "Pola lantai menggambarkan posisi dan perpindahan penari."),
      new Fact("Sebelum menari, kita perlu memastikan ruang ...", "Cukup aman dan tidak licin", "Penuh barang berserakan", "Gelap tanpa pengawasan", "Dipakai berlari tanpa aturan", "Ruang aman membantu penari bergerak tanpa bertabrakan."),
      new Fact("Gerakan tari yang dilakukan bersama sebaiknya ...", "Selaras dengan kelompok", "Selalu berbeda tanpa aba-aba", "Mendorong penari lain", "Dilakukan sambil menutup mata", "Keselarasan membuat tari kelompok tampak kompak."),
      new Fact("Ekspresi wajah dalam tari membantu ...", "Menunjukkan suasana atau perasaan", "Mengubah irama lagu", "Menghitung penonton", "Mengukur panggung", "Ekspresi wajah memperjelas suasana yang disampaikan."),
      new Fact("Gerak tari daerah dapat terinspirasi dari ...", "Budaya dan lingkungan setempat", "Aturan matematika saja", "Warna buku tulis", "Benda yang tidak dikenal", "Banyak tari daerah mencerminkan budaya dan kehidupan masyarakat."),
      new Fact("Saat melihat pertunjukan tari, sikap penonton yang baik adalah ...", "Menyimak dan menghargai penampil", "Berteriak mengganggu", "Berlari di depan panggung", "Mengejek gerakan", "Penonton menghargai penampil dengan tertib."),
      new Fact("Gerak tari dapat dilakukan dengan variasi ...", "Arah, level, dan tenaga", "Rasa makanan", "Warna suara saja", "Ukuran huruf", "Arah, level, dan tenaga dapat membuat gerak lebih beragam."),
      new Fact("Latihan tari kelompok perlu dilakukan dengan ...", "Kerja sama dan latihan bersama", "Saling menyalahkan", "Mengubah gerakan tanpa memberi tahu", "Mengabaikan aba-aba", "Latihan bersama membantu penari bergerak selaras."),
      new Fact("Setelah tampil, refleksi membantu kita ...", "Mengetahui hal yang sudah baik dan perlu diperbaiki", "Mengejek kelompok lain", "Menghapus semua latihan", "Menghindari belajar", "Refleksi membantu merencanakan perbaikan berikutnya.")
    };
    else if (topic.contains("teater") || topic.contains("peran") || topic.contains("cerita") || topic.contains("pementasan")) facts = new Fact[] {
      new Fact("Dalam bermain peran, tokoh adalah ...", "Karakter yang dimainkan", "Tempat menyimpan alat", "Jenis warna", "Pola bunyi", "Tokoh merupakan karakter dalam sebuah cerita."),
      new Fact("Dialog dalam teater berarti ...", "Percakapan antartokoh", "Gambar latar", "Gerak tanpa suara", "Daftar penonton", "Dialog adalah percakapan yang disampaikan tokoh."),
      new Fact("Agar cerita mudah dipahami penonton, pemain sebaiknya ...", "Berbicara jelas dan berekspresi", "Membelakangi penonton terus-menerus", "Berbisik tanpa alasan", "Mengubah cerita tanpa latihan", "Suara dan ekspresi membantu penonton memahami cerita."),
      new Fact("Sebelum pementasan, kelompok sebaiknya ...", "Berlatih dan membagi tugas", "Bertengkar memilih peran", "Tidak membaca cerita", "Meninggalkan panggung berantakan", "Persiapan dan pembagian tugas mendukung pementasan."),
      new Fact("Properti dalam pertunjukan adalah ...", "Benda pendukung adegan", "Penonton yang duduk", "Tepuk tangan", "Judul pelajaran", "Properti membantu menggambarkan suasana atau kegiatan dalam adegan."),
      new Fact("Saat teman memainkan adegan, kita sebaiknya ...", "Menyimak dan memberi ruang tampil", "Memotong dialognya", "Menertawakannya", "Mengambil propertinya", "Menghargai giliran membuat pertunjukan berjalan baik."),
      new Fact("Ekspresi tokoh sedih biasanya ditunjukkan dengan ...", "Wajah dan suara yang sesuai suasana sedih", "Tertawa keras tanpa alasan", "Berbicara tanpa ekspresi", "Melompat tanpa kaitan", "Ekspresi yang sesuai membantu menyampaikan perasaan tokoh."),
      new Fact("Cerita pertunjukan memiliki urutan ...", "Awal, tengah, dan akhir", "Warna, ukuran, dan berat", "Nada, angka, dan garis", "Pagi, siang, dan nama", "Urutan cerita membantu penonton mengikuti peristiwa."),
      new Fact("Penonton yang baik saat pertunjukan berlangsung akan ...", "Menjaga ketertiban", "Berjalan di panggung", "Mengganggu pemain", "Melempar benda", "Ketertiban membantu pemain dan penonton menikmati pertunjukan."),
      new Fact("Setelah pementasan, kelompok dapat berdiskusi untuk ...", "Mengevaluasi dan memperbaiki penampilan", "Mengejek teman", "Menyalahkan satu orang", "Menghindari latihan selanjutnya", "Evaluasi membantu kelompok belajar dari pengalaman.")
    };
    else facts = new Fact[] {
      new Fact("Garis, bentuk, warna, dan tekstur merupakan ...", "Unsur seni rupa", "Aturan permainan bola", "Jenis makanan", "Gerak lokomotor", "Garis, bentuk, warna, dan tekstur dapat digunakan dalam karya seni rupa."),
      new Fact("Warna merah, kuning, dan biru sering disebut warna ...", "Primer", "Gelap saja", "Transparan", "Tidak berwarna", "Merah, kuning, dan biru dikenal sebagai warna primer dalam pembelajaran seni dasar."),
      new Fact("Karya dua dimensi memiliki ...", "Panjang dan lebar", "Panjang, lebar, dan volume nyata", "Hanya berat", "Suara dan tempo", "Gambar dan lukisan umumnya memiliki panjang dan lebar."),
      new Fact("Bahan yang dapat digunakan untuk membuat kolase adalah ...", "Potongan kertas atau daun", "Air minum saja", "Asap", "Cahaya matahari saja", "Kolase dibuat dengan menempelkan beragam bahan pada bidang."),
      new Fact("Sebelum membuat karya, kita sebaiknya ...", "Menyiapkan alat dan bahan dengan aman", "Menggunakan alat tajam sambil berlari", "Membuang bahan sembarangan", "Mengganggu karya teman", "Persiapan yang aman membantu proses berkarya."),
      new Fact("Tekstur pada karya seni dapat terasa ...", "Halus atau kasar", "Cepat atau lambat", "Tinggi atau rendah bunyinya", "Pahit atau manis", "Tekstur berkaitan dengan kesan permukaan benda."),
      new Fact("Saat menggunakan gunting, kita harus ...", "Menggunakannya dengan hati-hati sesuai arahan", "Mengarahkannya ke teman", "Berlari sambil membawa gunting terbuka", "Melemparkannya", "Alat harus digunakan sesuai aturan keselamatan."),
      new Fact("Menghargai karya teman dapat dilakukan dengan ...", "Memberi tanggapan yang sopan", "Merusak karyanya", "Mengejek hasilnya", "Menyembunyikan alatnya", "Tanggapan sopan mendukung suasana belajar yang baik."),
      new Fact("Karya seni dapat dibuat untuk ...", "Menyampaikan gagasan atau perasaan", "Menggantikan semua aturan", "Mengukur suhu", "Menentukan nilai olahraga", "Karya seni dapat menyampaikan ide dan perasaan pembuatnya."),
      new Fact("Sisa bahan berkarya sebaiknya ...", "Dirapikan dan dibuang atau disimpan pada tempatnya", "Dibiarkan berserakan", "Dilempar ke teman", "Disumbat ke saluran air", "Merapikan bahan menjaga kebersihan ruang berkarya.")
    };
    return factBank(facts);
  }

  private static List<QuestionBank.Item> factBank(Fact[] facts) {
    List<QuestionBank.Item> bank = new ArrayList<>(50);
    for (int form = 0; form < 5; form++) for (int i = 0; i < facts.length; i++) {
      Fact fact = facts[i];
      String prompt = switch (form) {
        case 0 -> fact.prompt();
        case 1 -> "Pilih jawaban yang tepat. " + fact.prompt();
        case 2 -> "Manakah pernyataan yang benar? " + fact.prompt();
        case 3 -> "Untuk menjawab, ingat materi yang dipelajari. " + fact.prompt();
        default -> "Pada materi ini, " + fact.prompt();
      };
      Fact first = facts[(i + 2) % facts.length];
      Fact second = facts[(i + 5) % facts.length];
      Fact third = facts[(i + 7) % facts.length];
      bank.add(question(prompt, fact.answer(), first.answer(), second.answer(), third.answer(), fact.explanation()));
    }
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

  private static List<QuestionBank.Item> english(int grade) {
    String[][] vocab = switch (grade) {
      case 1 -> pairs("buku|book", "pensil|pencil", "tas|bag", "meja|table", "kursi|chair", "merah|red", "biru|blue", "satu|one", "dua|two", "guru|teacher");
      case 2 -> pairs("pagi|morning", "keluarga|family", "makan|eat", "minum|drink", "bermain|play", "sekolah|school", "rumah|house", "teman|friend", "air|water", "terima kasih|thank you");
      case 3 -> pairs("membaca|read", "menulis|write", "berlari|run", "berjalan|walk", "kucing|cat", "burung|bird", "pasar|market", "taman|park", "senang|happy", "besar|big");
      case 4 -> pairs("perpustakaan|library", "jendela|window", "sarapan|breakfast", "membantu|help", "bersih|clean", "cepat|fast", "di samping|beside", "di antara|between", "dokter|doctor", "petani|farmer");
      case 5 -> pairs("kemarin|yesterday", "biasanya|usually", "membeli|buy", "membawa|bring", "memilih|choose", "perjalanan|journey", "cuaca|weather", "lingkungan|environment", "hati-hati|careful", "sehat|healthy");
      default -> pairs("menjelaskan|explain", "menemukan|discover", "pengalaman|experience", "tujuan|purpose", "jawaban|answer", "pertanyaan|question", "perubahan|change", "berbeda|different", "penting|important", "mungkin|perhaps");
    };
    List<QuestionBank.Item> bank = new ArrayList<>(50);
    for (int form = 0; form < 5; form++) for (int i = 0; i < vocab.length; i++) {
      Word w = new Word(vocab[i][0], vocab[i][1]); String[] distractors = englishDistractors(vocab, i);
      String prompt = switch (form) {
        case 0 -> "What is the English word for ‘" + w.local + "’?";
        case 1 -> "Which English word means ‘" + w.local + "’?";
        case 2 -> "Complete the vocabulary: ‘" + w.local + "’ means ...";
        case 3 -> "Choose the correct translation of ‘" + w.local + "’.";
        default -> "In this lesson, how do you say ‘" + w.local + "’ in English?";
      };
      bank.add(question(prompt, w.english, distractors[0], distractors[1], distractors[2], "‘" + w.local + "’ in English is ‘" + w.english + "’."));
    }
    return bank;
  }

  private static List<QuestionBank.Item> indonesian(int grade) {
    if (grade <= 2) {
      String[] subjects = {"Alya", "Bima", "Ibu", "Ayah", "Siti", "Doni", "Kakak", "Adik", "Rani", "Pak Guru"};
      String[] actions = {"membaca buku", "menanam bunga", "pergi ke sekolah", "menyiram tanaman", "bermain bola"};
      List<QuestionBank.Item> bank = new ArrayList<>(50);
      for (String subject : subjects) for (String action : actions) {
        String correct = subject + " " + action + ".";
        bank.add(question("Pilih penulisan kalimat yang benar.", correct, subject.toLowerCase() + " " + action + ".", subject + " " + action, subject + " " + action + "?", "Kalimat pernyataan diawali huruf kapital dan diakhiri tanda titik."));
      }
      return bank;
    }
    String[][] words = pairs("apotik|apotek", "ijin|izin", "resiko|risiko", "aktifitas|aktivitas", "nasehat|nasihat", "praktek|praktik", "kwalitas|kualitas", "antri|antre", "sekedar|sekadar", "obyek|objek");
    List<QuestionBank.Item> bank = new ArrayList<>(50);
    for (int form = 0; form < 5; form++) for (int i = 0; i < words.length; i++) {
      String wrong = words[i][0], correct = words[i][1];
      String prompt = switch (form) {
        case 0 -> "Manakah penulisan kata baku yang tepat? ‘" + wrong + "’ atau ‘" + correct + "’?";
        case 1 -> "Pilih bentuk baku dari kata ‘" + wrong + "’.";
        case 2 -> "Kata yang tepat untuk melengkapi tulisan resmi: ‘" + wrong + "’ menjadi ...";
        case 3 -> "Dalam ejaan baku, kata ‘" + wrong + "’ ditulis ...";
        default -> "Manakah kata yang sesuai dengan bentuk baku bahasa Indonesia?";
      };
      bank.add(question(prompt, correct, wrong, words[(i + 3) % 10][1], words[(i + 6) % 10][1], "Bentuk baku yang benar adalah ‘" + correct + "’."));
    }
    return bank;
  }

  private static List<QuestionBank.Item> science(int grade) {
    String[][] raw = switch (grade) {
      case 3 -> new String[][] {
        {"Bagian tumbuhan yang menyerap air dari tanah adalah ...","akar","daun","bunga","buah","Akar menyerap air dan mineral dari tanah."},
        {"Hewan yang memakan tumbuhan disebut ...","herbivor","karnivor","omnivor","pengurai","Herbivor memperoleh makanan dari tumbuhan."},
        {"Sumber cahaya dan panas terbesar bagi bumi adalah ...","Matahari","Bulan","bintang laut","api unggun","Matahari memberi cahaya dan panas bagi bumi."},
        {"Benda yang dapat ditarik magnet contohnya ...","paku besi","kertas","kayu","karet","Magnet menarik benda tertentu yang mengandung besi."},
        {"Air yang dipanaskan lama-kelamaan berubah menjadi ...","uap air","tanah","batu","minyak","Pemanasan dapat mengubah air cair menjadi uap."},
        {"Tempat hidup suatu makhluk hidup disebut ...","habitat","populasi","cuaca","energi","Habitat adalah tempat makhluk hidup tinggal."},
        {"Agar tubuh sehat, kita perlu makan makanan ...","bergizi seimbang","manis saja","asin saja","tanpa sayur","Makanan beragam dan bergizi membantu tubuh tumbuh sehat."},
        {"Contoh tindakan menghemat air adalah ...","menutup keran setelah digunakan","membiarkan keran terbuka","bermain air lama","membuang air bersih","Menutup keran mencegah air terbuang sia-sia."},
        {"Benda cair mengikuti bentuk ...","wadahnya","tutupnya","warnanya","bayangannya","Benda cair berubah bentuk mengikuti wadah."},
        {"Hewan yang berkembang biak dengan bertelur contohnya ...","ayam","kucing","sapi","kambing","Ayam berkembang biak dengan bertelur."}};
      case 4 -> new String[][] {
        {"Hubungan makan dan dimakan dalam ekosistem disebut ...","rantai makanan","daur air","gaya","siklus batuan","Rantai makanan menunjukkan perpindahan energi melalui proses makan dan dimakan."},
        {"Pada rantai makanan, tumbuhan berperan sebagai ...","produsen","konsumen puncak","pengurai","pemangsa","Tumbuhan membuat makanannya sendiri sehingga disebut produsen."},
        {"Gaya dapat menyebabkan benda ...","berubah gerak atau bentuk","berubah menjadi air","hilang massanya","selalu membesar","Gaya dapat mengubah gerak, arah, atau bentuk benda."},
        {"Perubahan energi pada lampu adalah listrik menjadi ...","cahaya","bunyi saja","gerak saja","kimia","Lampu mengubah energi listrik menjadi cahaya dan sebagian panas."},
        {"Salah satu cara menjaga keseimbangan lingkungan adalah ...","menanam kembali pohon","membuang sampah ke sungai","membakar hutan","menangkap semua hewan","Menanam pohon membantu menjaga lingkungan dan habitat."},
        {"Bayangan terbentuk ketika cahaya ...","terhalang benda","menjadi bunyi","menjadi air","berubah rasa","Bayangan muncul saat cahaya terhalang benda."},
        {"Bunyi dihasilkan oleh benda yang ...","bergetar","membeku","menguap","mencair","Getaran benda menghasilkan bunyi."},
        {"Perubahan wujud dari cair menjadi padat disebut ...","membeku","menguap","mencair","menyublim","Membeku adalah perubahan zat cair menjadi padat."},
        {"Sumber daya alam yang dapat diperbarui contohnya ...","tumbuhan","batu bara","minyak bumi","gas alam","Tumbuhan dapat ditanam dan tumbuh kembali."},
        {"Bagian telinga yang membantu menangkap bunyi dari luar adalah ...","daun telinga","lidah","kulit","hidung","Daun telinga membantu mengumpulkan gelombang bunyi."}};
      case 5 -> new String[][] {
        {"Organ yang memompa darah ke seluruh tubuh adalah ...","jantung","paru-paru","lambung","ginjal","Jantung memompa darah ke seluruh tubuh."},
        {"Pertukaran oksigen dan karbon dioksida terutama terjadi di ...","paru-paru","lambung","usus","tulang","Paru-paru menjadi tempat pertukaran gas saat bernapas."},
        {"Energi dari matahari dapat diubah menjadi listrik menggunakan ...","panel surya","kompas","termometer","katrol","Panel surya mengubah energi cahaya matahari menjadi listrik."},
        {"Makhluk hidup yang menguraikan sisa organisme disebut ...","pengurai","produsen","konsumen","predator","Pengurai membantu mengembalikan unsur hara ke lingkungan."},
        {"Bumi berputar pada porosnya. Peristiwa ini disebut ...","rotasi","revolusi","evaporasi","kondensasi","Rotasi adalah perputaran bumi pada porosnya."},
        {"Air hujan yang meresap ke dalam tanah membantu mengisi ...","air tanah","awan","angin","cahaya","Air yang meresap menjadi bagian dari cadangan air tanah."},
        {"Bahan yang dapat menghantarkan panas dengan baik umumnya berupa ...","logam","kain wol","kayu","plastik","Logam merupakan penghantar panas yang baik."},
        {"Perubahan energi pada kipas angin adalah listrik menjadi ...","gerak","bunyi saja","cahaya","makanan","Motor kipas mengubah energi listrik menjadi gerak."},
        {"Tempat pelestarian hewan di habitat aslinya disebut ...","suaka alam","pasar","pabrik","bendungan","Kawasan suaka alam melindungi makhluk hidup dan habitatnya."},
        {"Planet tempat manusia tinggal adalah ...","Bumi","Mars","Venus","Jupiter","Bumi adalah planet yang menjadi tempat hidup manusia."}};
      default -> new String[][] {
        {"Organ yang mengalirkan darah ke seluruh tubuh adalah ...","jantung","lambung","paru-paru","kulit","Jantung memompa darah melalui pembuluh darah."},
        {"Gas yang dibutuhkan tubuh saat bernapas adalah ...","oksigen","karbon monoksida","helium","metana","Tubuh menggunakan oksigen untuk proses pernapasan."},
        {"Perputaran bumi mengelilingi matahari disebut ...","revolusi","rotasi","getaran","evaporasi","Revolusi bumi mengelilingi matahari berlangsung sekitar satu tahun."},
        {"Sumber energi terbarukan yang memanfaatkan angin adalah ...","turbin angin","bensin","batu bara","minyak tanah","Turbin angin mengubah energi gerak angin menjadi listrik."},
        {"Peristiwa air permukaan berubah menjadi uap karena panas disebut ...","penguapan","pengembunan","pembekuan","pengendapan","Penguapan mengubah air cair menjadi uap air."},
        {"Interaksi beberapa rantai makanan membentuk ...","jaring-jaring makanan","siklus siang","gaya gesek","tata surya","Jaring-jaring makanan menghubungkan banyak rantai makanan."},
        {"Benda yang dapat menghantarkan listrik dengan baik contohnya ...","tembaga","karet","kayu kering","kertas","Tembaga adalah penghantar listrik yang baik."},
        {"Gaya gesek muncul saat dua permukaan ...","bersentuhan","berjauhan","bercahaya","menguap","Gaya gesek timbul antara permukaan yang bersentuhan."},
        {"Usaha mengurangi sampah dengan memakai kembali barang disebut ...","guna ulang","pembakaran","penimbunan","pembuangan","Guna ulang memperpanjang pemakaian barang dan mengurangi sampah."},
        {"Bulan tampak bercahaya karena ...","memantulkan cahaya Matahari","menghasilkan api","menghasilkan listrik","berisi gas bercahaya","Bulan tampak terang karena memantulkan cahaya Matahari."}};
    };
    List<QuestionBank.Item> bank = new ArrayList<>(50);
    for (int form = 0; form < 5; form++) for (String[] fact : raw) {
      String prompt = form == 0 ? fact[0] : switch (form) {
        case 1 -> "Pernyataan yang tepat: " + fact[0].replace("...", "?");
        case 2 -> "Untuk menjawab, ingat konsep berikut. " + fact[0];
        case 3 -> "Manakah jawaban yang benar? " + fact[0];
        default -> "Pilih jawaban yang paling sesuai. " + fact[0];
      };
      bank.add(question(prompt, fact[1], fact[2], fact[3], fact[4], fact[5]));
    }
    return bank;
  }

  private static List<QuestionBank.Item> arabic(int grade) {
    String[][] vocab = switch (grade) {
      case 1 -> pairs("salam|assalaamu alaykum", "terima kasih|syukran", "ibu|ummun", "ayah|abun", "buku|kitaabun", "rumah|baytun", "satu|waahidun", "dua|itsnaani", "guru|ustaadzun", "teman|shadiiqun");
      case 2 -> pairs("sekolah|madrasatun", "kelas|fashlun", "pena|qalamun", "pintu|baabun", "merah|ahmaru", "putih|abyadhu", "tiga|tsalaatsatun", "empat|arba'atun", "air|maa'un", "makan|akala");
      case 3 -> pairs("salam|assalaamu alaykum", "jawaban salam|wa alaykumussalaam", "terima kasih|syukran", "sama-sama|afwan", "selamat datang|marhaban", "sampai jumpa|ma'a as-salaamah", "buku|kitaabun", "sekolah|madrasatun", "guru|ustaadzun", "teman|shadiiqun");
      case 4 -> pairs("keluarga|usratun", "saudara laki-laki|akhun", "saudara perempuan|ukhtun", "rumah|baytun", "masjid|masjidun", "membaca|qara'a", "menulis|kataba", "pergi|dzahaba", "lima|khamsatun", "enam|sittatun");
      case 5 -> pairs("perpustakaan|maktabatun", "pasar|suqun", "taman|hadiiqah", "pagi|shabaahun", "malam|laylun", "membuka|fataha", "duduk|jalasa", "mendengar|sami'a", "tujuh|sab'atun", "delapan|tsamaaniyatun");
      default -> pairs("pelajaran|darsun", "pertanyaan|su'aalun", "jawaban|jawaabun", "bersih|nadhiifun", "besar|kabiirun", "kecil|shaghiirun", "sembilan|tis'atun", "sepuluh|'asyaratun", "sakit|mariidhun", "sehat|shahiihun");
    };
    return wordBank(vocab, "Bahasa Arab");
  }

  private static List<QuestionBank.Item> wordBank(String[][] words, String language) {
    List<QuestionBank.Item> bank = new ArrayList<>(50);
    for (int form = 0; form < 5; form++) for (int i = 0; i < words.length; i++) {
      String[] d = {words[(i + 2) % 10][1], words[(i + 5) % 10][1], words[(i + 7) % 10][1]};
      String prompt = switch (form) {
        case 0 -> "Apa bahasa Arab dari ‘" + words[i][0] + "’?";
        case 1 -> "Pilih kosakata Arab yang berarti ‘" + words[i][0] + "’.";
        case 2 -> "Arti kata ‘" + words[i][1] + "’ adalah ...";
        case 3 -> "Pasangan kosakata yang benar: ‘" + words[i][0] + "’ berarti ...";
        default -> "Manakah terjemahan yang tepat untuk ‘" + words[i][0] + "’?";
      };
      boolean reverse = form == 2;
      bank.add(question(prompt, reverse ? words[i][0] : words[i][1], reverse ? words[(i+2)%10][0] : d[0], reverse ? words[(i+5)%10][0] : d[1], reverse ? words[(i+7)%10][0] : d[2], "Kosakata " + language + " yang tepat: ‘" + words[i][0] + "’ = ‘" + words[i][1] + "’."));
    }
    return bank;
  }

  private static List<QuestionBank.Item> religion(int grade) {
    String[][] cases = {
      {"%s menemukan uang di kelas. Apa yang sebaiknya dilakukan?", "Menyerahkannya kepada guru", "Menyimpannya diam-diam", "Membuangnya", "Mengaku sebagai pemilik", "Menyerahkan barang temuan kepada guru adalah sikap jujur dan amanah."},
      {"%s melakukan kesalahan saat bermain. Sikap terpuji adalah ...", "Mengakui kesalahan dan meminta maaf", "Menyalahkan teman", "Menyembunyikan kejadian", "Marah kepada semua orang", "Mengakui kesalahan dan meminta maaf menunjukkan tanggung jawab."},
      {"Sebelum makan, %s sebaiknya ...", "Berdoa dan mencuci tangan", "Bermain dengan makanan", "Membuang makanan", "Berlari sambil makan", "Berdoa dan menjaga kebersihan merupakan kebiasaan baik."},
      {"%s melihat teman kesulitan membawa buku. Perilaku baik adalah ...", "Menawarkan bantuan", "Menertawakan teman", "Mengambil bukunya", "Berpura-pura tidak melihat", "Tolong-menolong adalah perilaku terpuji."},
      {"%s mendapat nilai baik. Sikap yang tepat adalah ...", "Bersyukur dan tetap rajin belajar", "Mengejek teman", "Berhenti belajar", "Menyombongkan diri", "Bersyukur dan rendah hati menjaga akhlak baik."}
    };
    List<QuestionBank.Item> bank = new ArrayList<>(50);
    for (String[] c : cases) for (String name : NAMES) bank.add(question(c[0].formatted(name), c[1], c[2], c[3], c[4], c[5]));
    return bank;
  }

  private static String[][] pairs(String... values) {
    String[][] pairs = new String[values.length][2];
    for (int i = 0; i < values.length; i++) pairs[i] = values[i].split("\\|", 2);
    return pairs;
  }

  private static String[] englishDistractors(String[][] words, int index) {
    return new String[] {words[(index + 2) % words.length][1], words[(index + 5) % words.length][1], words[(index + 7) % words.length][1]};
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
