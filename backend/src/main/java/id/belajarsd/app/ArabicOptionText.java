package id.belajarsd.app;

import java.util.Map;

/** Converts the existing Arabic vocabulary bank to Arabic script at the API boundary. */
final class ArabicOptionText {
  private static final Map<String, String> ARABIC = Map.ofEntries(
      Map.entry("salam", "السَّلَامُ"), Map.entry("assalaamu alaykum", "السَّلَامُ عَلَيْكُمْ"),
      Map.entry("assalamu'alaikum", "السَّلَامُ عَلَيْكُمْ"), Map.entry("wa alaykumussalaam", "وَعَلَيْكُمُ السَّلَامُ"),
      Map.entry("wa'alaikumussalam", "وَعَلَيْكُمُ السَّلَامُ"), Map.entry("terima kasih", "شُكْرًا"),
      Map.entry("syukran", "شُكْرًا"), Map.entry("shukran", "شُكْرًا"), Map.entry("sama-sama", "عَفْوًا"),
      Map.entry("afwan", "عَفْوًا"), Map.entry("selamat datang", "مَرْحَبًا"), Map.entry("marhaban", "مَرْحَبًا"),
      Map.entry("sampai jumpa", "مَعَ السَّلَامَةِ"), Map.entry("ma'a as-salaamah", "مَعَ السَّلَامَةِ"),
      Map.entry("ma'a as-salamah", "مَعَ السَّلَامَةِ"), Map.entry("ummun", "أُمٌّ"), Map.entry("ibu", "أُمٌّ"),
      Map.entry("abun", "أَبٌ"), Map.entry("ayah", "أَبٌ"), Map.entry("kitaabun", "كِتَابٌ"), Map.entry("buku", "كِتَابٌ"),
      Map.entry("baytun", "بَيْتٌ"), Map.entry("rumah", "بَيْتٌ"), Map.entry("waahidun", "وَاحِدٌ"), Map.entry("satu", "وَاحِدٌ"),
      Map.entry("itsnaani", "اِثْنَانِ"), Map.entry("dua", "اِثْنَانِ"), Map.entry("ustaadzun", "أُسْتَاذٌ"), Map.entry("guru", "أُسْتَاذٌ"),
      Map.entry("shadiiqun", "صَدِيقٌ"), Map.entry("teman", "صَدِيقٌ"), Map.entry("madrasatun", "مَدْرَسَةٌ"), Map.entry("sekolah", "مَدْرَسَةٌ"),
      Map.entry("fashlun", "فَصْلٌ"), Map.entry("kelas", "فَصْلٌ"), Map.entry("qalamun", "قَلَمٌ"), Map.entry("pena", "قَلَمٌ"),
      Map.entry("baabun", "بَابٌ"), Map.entry("pintu", "بَابٌ"), Map.entry("ahmaru", "أَحْمَرُ"), Map.entry("merah", "أَحْمَرُ"),
      Map.entry("abyadhu", "أَبْيَضُ"), Map.entry("putih", "أَبْيَضُ"), Map.entry("tsalaatsatun", "ثَلَاثَةٌ"), Map.entry("tiga", "ثَلَاثَةٌ"),
      Map.entry("arba'atun", "أَرْبَعَةٌ"), Map.entry("empat", "أَرْبَعَةٌ"), Map.entry("maa'un", "مَاءٌ"), Map.entry("air", "مَاءٌ"),
      Map.entry("akala", "أَكَلَ"), Map.entry("makan", "أَكَلَ"), Map.entry("jawaban salam", "وَعَلَيْكُمُ السَّلَامُ"),
      Map.entry("keluarga", "أُسْرَةٌ"), Map.entry("usratun", "أُسْرَةٌ"), Map.entry("saudara laki-laki", "أَخٌ"), Map.entry("akhun", "أَخٌ"),
      Map.entry("saudara perempuan", "أُخْتٌ"), Map.entry("ukhtun", "أُخْتٌ"), Map.entry("masjid", "مَسْجِدٌ"), Map.entry("masjidun", "مَسْجِدٌ"),
      Map.entry("membaca", "قَرَأَ"), Map.entry("qara'a", "قَرَأَ"), Map.entry("menulis", "كَتَبَ"), Map.entry("kataba", "كَتَبَ"),
      Map.entry("pergi", "ذَهَبَ"), Map.entry("dzahaba", "ذَهَبَ"), Map.entry("lima", "خَمْسَةٌ"), Map.entry("khamsatun", "خَمْسَةٌ"),
      Map.entry("enam", "سِتَّةٌ"), Map.entry("sittatun", "سِتَّةٌ"), Map.entry("perpustakaan", "مَكْتَبَةٌ"), Map.entry("maktabatun", "مَكْتَبَةٌ"),
      Map.entry("pasar", "سُوقٌ"), Map.entry("suqun", "سُوقٌ"), Map.entry("taman", "حَدِيقَةٌ"), Map.entry("hadiiqah", "حَدِيقَةٌ"),
      Map.entry("pagi", "صَبَاحٌ"), Map.entry("shabaahun", "صَبَاحٌ"), Map.entry("malam", "لَيْلٌ"), Map.entry("laylun", "لَيْلٌ"),
      Map.entry("membuka", "فَتَحَ"), Map.entry("fataha", "فَتَحَ"), Map.entry("duduk", "جَلَسَ"), Map.entry("jalasa", "جَلَسَ"),
      Map.entry("mendengar", "سَمِعَ"), Map.entry("sami'a", "سَمِعَ"), Map.entry("tujuh", "سَبْعَةٌ"), Map.entry("sab'atun", "سَبْعَةٌ"),
      Map.entry("delapan", "ثَمَانِيَةٌ"), Map.entry("tsamaaniyatun", "ثَمَانِيَةٌ"), Map.entry("pelajaran", "دَرْسٌ"), Map.entry("darsun", "دَرْسٌ"),
      Map.entry("pertanyaan", "سُؤَالٌ"), Map.entry("su'aalun", "سُؤَالٌ"), Map.entry("jawaban", "جَوَابٌ"), Map.entry("jawaabun", "جَوَابٌ"),
      Map.entry("bersih", "نَظِيفٌ"), Map.entry("nadhiifun", "نَظِيفٌ"), Map.entry("besar", "كَبِيرٌ"), Map.entry("kabiirun", "كَبِيرٌ"),
      Map.entry("kecil", "صَغِيرٌ"), Map.entry("shaghiirun", "صَغِيرٌ"), Map.entry("sembilan", "تِسْعَةٌ"), Map.entry("tis'atun", "تِسْعَةٌ"),
      Map.entry("sepuluh", "عَشَرَةٌ"), Map.entry("'asyaratun", "عَشَرَةٌ"), Map.entry("sakit", "مَرِيضٌ"), Map.entry("mariidhun", "مَرِيضٌ"),
      Map.entry("sehat", "صَحِيحٌ"), Map.entry("shahiihun", "صَحِيحٌ"));

  private ArabicOptionText() {}

  static boolean isArabicLesson(Lesson lesson) {
    return "Bahasa Arab".equals(lesson.getSubject().getName());
  }

  static String render(Lesson lesson, String option) {
    if (!isArabicLesson(lesson)) return option;
    return ARABIC.getOrDefault(option.trim().toLowerCase(java.util.Locale.ROOT), option);
  }
}
