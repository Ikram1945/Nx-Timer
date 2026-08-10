# ⏱️ Timer & Alarm

Aplikasi Android serbaguna yang menggabungkan **stopwatch/hitung mundur** dan **alarm berulang** dalam satu antarmuka sederhana. Dibangun dengan Java 17 dan Material Design 3.

## ✨ Fitur

### ⏱️ Timer / Stopwatch
- **Stopwatch** — hitung waktu naik dengan presisi sentidetik (00:00:00.00)
- **Hitung mundur (Countdown)** — atur target waktu, hitung mundur, dan dapatkan notifikasi saat selesai
- **Lap** — catat waktu lap/split selama stopwatch berjalan
- **Salin waktu** — salin waktu saat ini ke clipboard
- **Bagikan** — bagikan waktu via intent Android
- **Mode fokus** — tombol toggle untuk mode fokus
- **Atur target** — tambah/kurangi target countdown per 10 detik
- **Progress bar** — visualisasi progres countdown

### ⏰ Alarm
- **Set alarm berulang** — pilih hari (Senin–Minggu) dengan chip
- **Label kustom** — beri nama setiap alarm
- **Notifikasi & layar penuh** — alarm berbunyi dengan suara, getar, dan tampilan layar penuh
- **Reschedule otomatis** — alarm dijadwalkan ulang setiap minggu
- **Boot receiver** — semua alarm di-reschedule otomatis setelah restart perangkat
- **Penyimpanan persisten** — data alarm disimpan di SharedPreferences (JSON)

## 🛠️ Teknologi

| Komponen | Keterangan |
|---|---|
| Bahasa | Java 17 |
| UI | Material Design 3 (`com.google.android.material:material:1.12.0`) |
| Data Binding | `androidx.databinding:viewbinding:8.7.3` |
| Fragment | `TimerFragment` + `AlarmFragment` dengan `BottomNavigationView` |
| Alarm | `AlarmManager` + `PendingIntent` + `BroadcastReceiver` |
| Notifikasi | `NotificationCompat` + `NotificationChannel` |
| Getar | `Vibrator` / `VibratorManager` |
| Suara | `RingtoneManager` + `MediaPlayer` |
| Penyimpanan | `SharedPreferences` (JSON) |
| Animasi | Custom `ClickAnimator` (bounce + ripple) |

## 📁 Struktur Proyek

```
app/src/main/java/com/nx/timer/
├── MainActivity.java          # Activity utama, navigasi bottom nav
├── TimerFragment.java         # Fragment stopwatch & countdown
├── TimerManager.java          # Logika stopwatch/countdown (Handler-based)
├── TimerUiController.java     # Kontrol UI timer (tombol, progress, status)
├── AlarmFragment.java         # Fragment alarm
├── AlarmItem.java             # Model data alarm
├── AlarmStorage.java          # Simpan/muat alarm (SharedPreferences JSON)
├── AlarmReceiver.java         # BroadcastReceiver untuk trigger alarm
├── AlarmAlertActivity.java    # Activity layar penuh saat alarm berbunyi
├── BootReceiver.java          # Reschedule alarm setelah reboot
├── AppActionsController.java  # Handler FAB, share, focus mode
├── ClickAnimator.java         # Animasi ripple & bounce untuk semua view
├── BounceTouchListener.java   # (helper ClickAnimator)
└── BounceLayoutListener.java  # (helper ClickAnimator)
```

## 🚀 Cara Menjalankan

1. Buka project di **CodeAssist**
2. Pilih modul `app`
3. Jalankan task **Run app** atau compile & install langsung ke perangkat Android

## 📱 Persyaratan

- Android 5.0 (API 21) atau lebih tinggi
- Izin:
  - `VIBRATE` — getar saat alarm
  - `POST_NOTIFICATIONS` — notifikasi alarm (Android 13+)
  - `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` — alarm tepat waktu
  - `USE_FULL_SCREEN_INTENT` — tampilan alarm layar penuh
  - `WAKE_LOCK` — bangunkan layar saat alarm
  - `RECEIVE_BOOT_COMPLETED` — reschedule alarm setelah reboot

## 🎨 Tema

Menggunakan Material Design 3 dengan tema kustom (`@style/Theme.App`), mendukung light/dark mode sesuai setelan sistem.

---

Dibuat dengan ❤️ menggunakan [CodeAssist](https://codeassist.dev).
