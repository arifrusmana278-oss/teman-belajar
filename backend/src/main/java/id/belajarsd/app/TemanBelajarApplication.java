package id.belajarsd.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableScheduling
public class TemanBelajarApplication {
  public static void main(String[] args) {
    SpringApplication.run(TemanBelajarApplication.class, args);
  }

  @Bean
  WebMvcConfigurer corsConfigurer(@Value("${app.cors.origins}") String origins) {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(origins.split(","))
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS").allowedHeaders("Content-Type", "Authorization").exposedHeaders("X-Questions-Exhausted");
      }
    };
  }

  @Bean
  CommandLineRunner bootstrapAdmin(AuthService auth, @Value("${app.admin.email}") String email,
      @Value("${app.admin.password:}") String password) {
    return args -> {
      String generatedPassword = auth.bootstrapAdmin(email, password);
      if (generatedPassword != null) LoggerFactory.getLogger(TemanBelajarApplication.class)
          .warn("Akun admin dibuat. Email: {} | Kata sandi awal: {}. Simpan kredensial ini.", email, generatedPassword);
    };
  }

  @Bean
  CommandLineRunner seedData(SubjectRepository subjects, LessonRepository lessons, MathQuestionSync mathQuestions, SeedQuestionSync seedQuestions) {
    return args -> {
      Object[][] data = {
            {"Matematika", "🔢", "Berlatih angka dan logika", "#dff3ff", 1},
            {"Bahasa Inggris", "🔤", "Words, stories, and speaking", "#ffebd9", 2},
            {"Hitung Cepat", "⚡", "Asah kecepatan dan ketelitian", "#fff3bf", 3},
            {"Bahasa Indonesia", "📚", "Membaca dan bercerita", "#f8ddf2", 4},
            {"IPAS", "🌱", "Jelajahi alam dan masyarakat", "#dff7e8", 5},
            {"Bahasa Arab", "🌙", "Kenali huruf dan kosakata Arab", "#e9e2ff", 6},
            {"PAI", "🕌", "Belajar agama dan akhlak baik", "#d9f4ef", 7},
            {"Mathematics in English", "🧩", "Learn math in English", "#e3e8ff", 8},
            {"Pendidikan Pancasila", "🇮🇩", "Belajar nilai Pancasila dan kehidupan berbangsa", "#ffe3dd", 9},
            {"PJOK", "🏃", "Bergerak aktif, sehat, dan aman", "#dcf5ff", 10},
            {"Seni", "🎨", "Bereksplorasi melalui seni rupa, musik, tari, dan teater", "#f4e2ff", 11},
            {"Fiqih", "🤲", "Tata cara ibadah dan muamalah sehari-hari", "#e2f6ea", 12},
            {"Akidah Akhlak", "💚", "Iman kepada Allah dan akhlak terpuji", "#fdf0d5", 13},
            {"Sejarah Kebudayaan Islam", "🕋", "Kisah Nabi, sahabat, dan dakwah Islam", "#efe6d8", 14},
            {"Sejarah Indonesia", "📜", "Jejak perjalanan bangsa Indonesia", "#fbe4d6", 15}
      };
      for (Object[] item : data) {
        if (subjects.findByName((String) item[0]).isEmpty()) {
          subjects.save(new Subject((String) item[0], (String) item[1], (String) item[2],
              (String) item[3], (Integer) item[4]));
        }
      }
      createLesson(subjects, lessons, "Matematika", "Penjumlahan sampai 20", "Kelas 1",
            "Penjumlahan menggabungkan dua kelompok benda. Jumlahnya dapat dihitung dengan membilang maju.");
      createLesson(subjects, lessons, "Bahasa Inggris", "Everyday Greetings", "Kelas 2",
            "Learn simple greetings, saying thank you, and saying goodbye in everyday situations.");
      createLesson(subjects, lessons, "Hitung Cepat", "Perkalian sebagai penjumlahan berulang", "Kelas 3",
            "Perkalian menunjukkan kelompok dengan jumlah benda yang sama dan dapat dihitung sebagai penjumlahan berulang.");
      createLesson(subjects, lessons, "Bahasa Indonesia", "Memahami kalimat", "Kelas 2",
            "Kalimat pernyataan diawali huruf kapital dan diakhiri tanda titik.");
      createLesson(subjects, lessons, "IPAS", "Bagian tumbuhan", "Kelas 3",
            "Akar menyerap air dan mineral, batang menopang tumbuhan, daun menangkap cahaya, bunga berperan dalam perkembangbiakan, dan biji dapat tumbuh menjadi tanaman baru.");
      createLesson(subjects, lessons, "Bahasa Arab", "Salam sederhana", "Kelas 3",
            "Pelajari ungkapan salam, jawaban salam, ucapan terima kasih, dan ungkapan perpisahan sederhana.");
      createLesson(subjects, lessons, "PAI", "Membiasakan perilaku jujur", "Kelas 2",
            "Jujur berarti berkata dan bertindak sesuai kenyataan, termasuk saat melakukan kesalahan atau menemukan sesuatu.");
      createLesson(subjects, lessons, "Mathematics in English", "Addition facts", "Grade 1",
            "Addition combines groups. Count on to find the total.");
      CurriculumCatalog.seed(subjects, lessons);
      // Keep every chapter's starter questions equal to its bank; unchanged banks are left as they are, so restarts are safe.
      for (Lesson lesson : lessons.findAllWithSubject()) {
        if (mathQuestions.sync(lesson)) continue;
        var bank = QuestionBank.forLesson(lesson.getTitle());
        if (bank.isEmpty()) bank = CurriculumQuestionBank.forLesson(lesson);
        seedQuestions.sync(lesson, bank);
      }
    };
  }

  private void createLesson(SubjectRepository subjects, LessonRepository lessons,
      String subjectName, String title, String grade, String content) {
    Subject subject = subjects.findByName(subjectName).orElseThrow();
    if (!lessons.existsBySubjectIdAndTitleAndGrade(subject.getId(), title, grade)) {
      lessons.save(new Lesson(subject, title, grade, content, 10));
    }
  }
}
