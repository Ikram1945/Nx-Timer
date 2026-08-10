package com.nx.timer

/**
 * Built-in work/break session template (mis. Pomodoro: 25 menit kerja + 5 menit istirahat).
 *
 * @property workMillis   Durasi sesi kerja dalam milidetik.
 * @property breakMillis  Durasi sesi istirahat dalam milidetik.
 * @property label        Nama template yang ditampilkan pada UI.
 */
data class TimerTemplate(
    val workMillis: Long,
    val breakMillis: Long,
    val label: String
) {
    companion object {
        /** Template Pomodoro klasik: 25 menit kerja + 5 menit istirahat.
         *  Di-ekspos sebagai field statis langsung di TimerTemplate (bukan di
         *  Companion) supaya Java interop bisa menulis
         *  TimerTemplate.POMODORO_25_5 tanpa prefix "Companion". */
        @JvmStatic
        val POMODORO_25_5: TimerTemplate = TimerTemplate(
            workMillis = 25 * 60_000L,
            breakMillis = 5 * 60_000L,
            label = "Pomodoro 25:5"
        )
    }
}
