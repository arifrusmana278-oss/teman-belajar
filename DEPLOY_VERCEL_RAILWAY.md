# Deploy Teman Belajar ke Vercel dan Railway

## 1. Deploy database dan backend ke Railway

1. Buat project baru di Railway, lalu tambahkan service **PostgreSQL**.
2. Tambahkan service dari repository GitHub yang sama. Pada pengaturan service, set **Root Directory** ke `/backend`. Railway akan memakai `backend/Dockerfile` dan `backend/railway.toml`.
3. Buat domain publik untuk service backend melalui **Settings → Networking → Generate Domain**.
4. Tambahkan variable berikut di backend service (**Variables**). Gunakan reference variable Railway dari PostgreSQL service agar kredensial tidak disalin manual:

   | Variable | Value |
   |---|---|
   | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://${{Postgres.RAILWAY_PRIVATE_DOMAIN}}:5432/${{Postgres.PGDATABASE}}` |
   | `SPRING_DATASOURCE_USERNAME` | `${{Postgres.PGUSER}}` |
   | `SPRING_DATASOURCE_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
   | `APP_CORS_ORIGINS` | Domain frontend Vercel, contoh `https://teman-belajar.vercel.app` |
   | `ADMIN_EMAIL` | Email admin yang akan digunakan |
   | `ADMIN_PASSWORD` | Password admin kuat dan unik |
   | `BACKEND_PUBLIC_URL` | URL publik backend, contoh `https://api-teman-belajar.up.railway.app` |
   | `DONATION_OWNER_EMAIL` | Email pemilik untuk persetujuan QRIS |

   Ganti `Postgres` dalam reference variable dengan nama service PostgreSQL yang terlihat pada project Railway. Isi `APP_CORS_ORIGINS` tanpa slash di akhir. Untuk beberapa domain, pisahkan dengan koma.

5. Deploy backend dan pastikan `https://<domain-backend>/actuator/health` menunjukkan status `UP`. API aplikasi tersedia di `https://<domain-backend>/api`.

SMTP untuk persetujuan email QRIS dan variable `AI_*` bersifat opsional. Konfigurasikan hanya jika fitur tersebut digunakan. Untuk mengaktifkan persetujuan QRIS melalui email, isi `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, dan `MAIL_FROM`.

## 2. Deploy frontend ke Vercel

1. Import repository yang sama sebagai project Vercel.
2. Set **Root Directory** ke `frontend` dan aktifkan opsi untuk memasukkan file di luar root directory jika Vercel menanyakannya. Semua aset build frontend berada di dalam folder tersebut.
3. Framework akan terdeteksi sebagai Vite. Konfigurasi build juga tersedia di `frontend/vercel.json`.
4. Tambahkan environment variable untuk Production, Preview, dan Development sesuai kebutuhan:

   | Variable | Value |
   |---|---|
   | `VITE_API_URL` | `https://<domain-backend>/api` |

5. Deploy. Setelah mendapat domain Vercel, tambahkan domain itu ke `APP_CORS_ORIGINS` di Railway backend, lalu redeploy backend. Jika menggunakan domain Vercel untuk preview, masukkan domain preview tersebut secara eksplisit juga.

Setiap kali domain frontend/backend berubah, perbarui variable yang merujuk ke domain tersebut lalu redeploy service terkait.

## Catatan operasional

- Database Railway bersifat persisten dan terpisah dari container backend. Jangan menghapus PostgreSQL service jika data akun/progres harus dipertahankan.
- Backend saat ini memakai Hibernate `ddl-auto=update` untuk membuat dan memperbarui tabel saat startup.
- Sesi login disimpan di memori backend; pengguna perlu login kembali setelah backend restart atau redeploy.
- File `frontend/public/QRIS_AR.jpeg` ikut disajikan sebagai aset statis Vercel.
