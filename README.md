# Teman Belajar SD — MVP

Platform belajar web untuk siswa SD kelas 1–6 Indonesia. MVP ini menyediakan dashboard siswa, 8 jalur mata pelajaran, materi contoh, kuis pilihan ganda, pembahasan jawaban, dan riwayat progres.

## Teknologi
- Frontend: Vue 3, JavaScript, Vite
- Backend: Java 21, Spring Boot 3, Spring Data JPA, REST API
- Database: PostgreSQL 16
- Deployment lokal: Docker Compose

## Menjalankan dengan Docker Compose
Prasyarat: Docker Engine dan Docker Compose plugin.

```bash
cp .env.example .env
# Edit .env dan ubah DB_PASSWORD menjadi password lokal yang kuat
docker compose up --build -d
docker compose ps
docker compose logs -f backend
```

Buka:
- Web: http://localhost:5173
- Admin: username `admin` (atau email `admin@temanbelajar.local`), password `admin123`
- API health: http://localhost:8080/api/health
- Daftar mata pelajaran: http://localhost:8080/api/subjects
- Actuator health: http://localhost:8080/actuator/health

Hentikan:

```bash
docker compose down
```

### Uji pilot melalui ngrok

Vite mengizinkan domain ngrok (`*.ngrok-free.dev`, `*.ngrok.app`, `*.ngrok-free.app`, dan `*.ngrok.io`). Untuk hostname khusus, buat `frontend/.env.local` dan isi hostname saja (tanpa `https://`):

```dotenv
NGROK_HOST=pilot-anda.example.com
```

Untuk beberapa hostname, pisahkan dengan koma. Jalankan backend dan frontend secara manual, lalu mulai tunnel ke frontend dengan `ngrok http 5173`. Permintaan `/api` otomatis diteruskan oleh Vite ke backend lokal di `http://localhost:8080`, sehingga penguji dapat menggunakan hostname ngrok yang sama. Jika backend berjalan di alamat lain, tambahkan `VITE_API_PROXY_TARGET=http://alamat-backend:8080` ke `frontend/.env.local`. Setelah mengubah `.env.local`, mulai ulang frontend.

## Donasi QRIS

Admin dapat membuka menu **QRIS Mandiri** dan mengunggah gambar PNG, JPG, atau WebP maksimal 5 MB. Nomor rekening Mandiri saja tidak dapat dijadikan QRIS; rekening perlu didaftarkan sebagai rekening merchant melalui [Livin' Usaha](https://qris.bankmandiri.co.id/), kemudian Mandiri menerbitkan kode QRIS setelah verifikasi. QR baru berstatus menunggu dan belum terlihat oleh pengguna sampai pemilik website menyetujui tautan sekali pakai yang dikirim ke `DONATION_OWNER_EMAIL` (default `arifrusmana278@gmail.com`). QR aktif disimpan di PostgreSQL.

Email memerlukan konfigurasi SMTP di `.env`. Untuk Gmail, gunakan `MAIL_HOST=smtp.gmail.com`, `MAIL_PORT=587`, `MAIL_SMTP_STARTTLS=true`, alamat Gmail pada `MAIL_USERNAME` dan `MAIL_FROM`, serta App Password Gmail pada `MAIL_PASSWORD` (bukan kata sandi login biasa). Setelah mengubah `.env`, mulai ulang backend. Atur `BACKEND_PUBLIC_URL` ke alamat publik backend jika aplikasi dibuka dari internet, agar tautan persetujuan dapat dijangkau.

### Pembuatan soal dengan AI

Fitur soal AI nonaktif secara default. Untuk mengaktifkannya, isi `AI_BASE_URL`, `AI_MODEL`, dan `AI_API_KEY` di `.env`, lalu set `AI_QUESTIONS_ENABLED=true` dan mulai ulang backend. Key hanya dibaca backend dan jangan dimasukkan ke frontend atau source control. Endpoint harus kompatibel dengan format Chat Completions OpenAI. Backend memeriksa format dan duplikasi sebelum menyimpan tepat 12 soal per bab (dapat diubah melalui `AI_QUESTIONS_PER_LESSON`).

Backend menjalankan pembaruan setiap **hari Minggu pukul 23:59 WIB** (`Asia/Jakarta`), dengan batas maksimum `AI_QUESTIONS_BATCH_SIZE` bab per putaran. Jika kredensial belum lengkap, putaran dilewati tanpa menghubungi provider. Jika provider gagal atau hasilnya tidak valid, putaran dihentikan dan dicoba kembali pada jadwal Minggu berikutnya; bank yang ada tetap dipakai. Batch AI berlaku 30 hari, lalu dibersihkan setiap hari pukul 02:15 waktu server. Soal awal dipertahankan hingga penggantinya tersedia; bila tidak ada soal aktif karena layanan AI gagal, bank awal tetap menjadi cadangan. Siswa berbagi bank soal per bab, dan setiap sesi mengacak 10 soal. Admin dapat melihat konfigurasi nonrahasia dan status ringkas melalui `GET /api/admin/ai-questions`.

Jika SMTP belum diatur atau gagal mengirim, unggahan tetap tersimpan sebagai menunggu persetujuan dan dashboard admin akan menampilkan bahwa email belum terkirim. Atur SMTP, lalu unggah QR kembali untuk memicu permintaan persetujuan baru.

Data PostgreSQL disimpan di volume `postgres_data`. Untuk menghapus seluruh data lokal termasuk progres:

```bash
docker compose down -v
```

Perintah terakhir menghapus data database secara permanen.

## Menjalankan tanpa Docker
Prasyarat: Java 21, Maven 3.9+, Node.js 22+, PostgreSQL 16.

1. Buat database `belajarsd` dan user `belajarsd` di PostgreSQL.
2. Jalankan backend:

```bash
cd backend
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/belajarsd'
export SPRING_DATASOURCE_USERNAME='belajarsd'
export SPRING_DATASOURCE_PASSWORD='password_database_anda'
mvn spring-boot:run
```

3. Di terminal lain jalankan frontend:

```bash
cd frontend
npm install
npm run dev
```

## API utama

| Method | Endpoint | Keterangan |
|---|---|---|
| GET | `/api/health` | Health check aplikasi |
| GET | `/api/subjects` | Daftar 8 mata pelajaran |
| POST | `/api/auth/register` | Daftar akun siswa dan kelas |
| POST | `/api/auth/login` | Login siswa atau admin |
| GET | `/api/subjects/{id}/lessons` | Materi untuk mata pelajaran |
| GET | `/api/lessons/{id}` | Detail materi |
| GET | `/api/lessons/{id}/questions` | Soal tanpa kunci jawaban |
| POST | `/api/lessons/{id}/check` | Periksa jawaban dan kembalikan pembahasan |
| POST | `/api/progress` | Simpan hasil latihan |
| GET | `/api/progress?studentName=Adik` | Ringkasan dan riwayat progres |
| GET | `/api/admin/students` | Monitoring nilai semua siswa (admin) |

Contoh memeriksa jawaban:

```bash
curl -X POST http://localhost:8080/api/lessons/1/check \
  -H 'Content-Type: application/json' \
  -d '{"questionId":1,"selectedOptionIndex":1}'
```

Contoh menyimpan progres:

```bash
curl -X POST http://localhost:8080/api/progress \
  -H 'Content-Type: application/json' \
  -d '{"studentName":"Adik","lessonId":1,"score":1,"total":1}'
```

## Struktur folder

```text
teman-belajar-sd/
├── docker-compose.yml
├── .env.example
├── README.md
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/id/belajarsd/app/
│       │   ├── TemanBelajarApplication.java
│       │   ├── ApiController.java
│       │   ├── Subject.java / SubjectRepository.java
│       │   ├── Lesson.java / LessonRepository.java
│       │   ├── Question.java / QuestionRepository.java
│       │   └── Progress.java / ProgressRepository.java
│       └── resources/application.properties
└── frontend/
    ├── Dockerfile
    ├── package.json
    ├── vite.config.js
    └── src/App.vue, main.js, style.css
```

## Catatan keamanan dan batasan MVP
- Konten yang disertakan adalah **data contoh** untuk memvalidasi alur aplikasi, belum merupakan materi lengkap atau tervalidasi untuk seluruh kurikulum kelas 1–6.
- Akun admin lokal dibuat saat backend pertama kali berjalan. Atur `ADMIN_EMAIL` dan `ADMIN_PASSWORD` melalui `.env` jika ingin memakai kredensial berbeda. Menu monitoring hanya dapat dibuka akun dengan peran admin.
- Sesi login disimpan di memori backend, sehingga semua pengguna perlu login kembali setelah backend dimulai ulang. Konfigurasi ini ditujukan untuk pengembangan lokal, bukan deployment publik.
- Password database default hanya untuk pengembangan lokal; ubah melalui `.env` sebelum dipakai di lingkungan bersama.
- `ddl-auto=update` digunakan untuk prototipe. Sebelum produksi, gunakan migrasi database seperti Flyway dan `ddl-auto=validate`.
- Materi PAI dan semua materi pendidikan perlu ditelaah guru/ahli yang sesuai. Kesesuaian kurikulum dan aksesibilitas perlu diuji bersama guru, orang tua, dan anak.
- Font Google diimpor dari CDN; tampilan inti tetap dapat digunakan jika font eksternal tidak tersedia.

## Rencana lanjutan
1. Manajemen akun orang tua/guru dan pemulihan kata sandi.
2. Pemetaan kurikulum resmi per fase, kelas, semester, dan kompetensi.
3. CMS untuk materi, soal, audio, dan gambar.
4. Evaluasi adaptif, timer hitung cepat, lencana, dan misi belajar.
5. Tes otomatis, CI/CD, backup, observability, dan deployment produksi.
