# GI-GuitarTuner

<center>
  <img width="339" height="731" alt="GI-GuitarTuner App Screenshot" src="https://github.com/user-attachments/assets/0680e11f-a336-403e-8c33-b38f9cef2080" />
</center>

<br/>

<center>

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![UI framework](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Storage-Room-4285F4.svg?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-blue.svg?style=for-the-badge)](https://developer.android.com/about/versions/nougat)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-brightgreen.svg?style=for-the-badge)](https://developer.android.com/about/versions/16)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

</center>

---

## 🎵 Descrizione Generale

**GI-GuitarTuner** è una suite musicale completa e ad alte prestazioni per Android, sviluppata nativamente con **Kotlin** e **Jetpack Compose (Material 3)**. Combina un accordatore cromatico di precisione basato sull'algoritmo YIN, un metronomo avanzato con loop di batteria sintetizzati proceduralmente, un visualizzatore di scale interattivo per strumenti a corda, un oscilloscopio audio in tempo reale ed un registratore professionale WAV con gestione locale tramite **Room Database**.

---

## 🔥 Funzionalità Dettagliate

### 🎼 Accordatore YIN ad Alta Precisione
* **Pitch Detection:** Algoritmo YIN avanzato per il rilevamento istantaneo ed accurato della frequenza fondamentale.
* **Interfaccia Visiva:** Quadrante ad arco con deviazione da **-50 a +50 cent** e barra stroboscopica visiva.
* **Feedback Acustico:** Segnale sonoro automatico di conferma quando la nota è perfettamente in tono (*In-Tune*).
* **Supporto Strumenti:** Preset dedicati per **Chitarra, Violino, Viola, Violoncello, Ukulele** e modalità **Cromatica libera**.

### 🎛️ Calibrazione Diapason & Noise Gate
* **Calibrazione A4:** Frequenza del Diapason regolabile da **410 Hz a 466 Hz** con preset rapidi:
  * `415 Hz` (Barocco)
  * `432 Hz` (Verdi / Scientifico)
  * `440 Hz` (Standard ISO)
  * `442 Hz` (Orchestra Europea)
* **Noise Gate Personalizzabile:** Filtro del rumore di fondo regolabile per garantire precisione anche in ambienti rumorosi.

### 📊 Oscilloscopio Audio Live
* Renderizzazione della forma d'onda del segnale in ingresso in tempo reale tramite **Jetpack Compose Canvas** ad alta frequenza di aggiornamento.

### 🥁 Metronomo Digitale & Drum Loops Procedurali
* Generazione audio a **latenza zero** con ritmi di batteria sintetizzati matematicamente via DSP.
* Stili di accompagnamento inclusi: **Rock, Funk, Jazz, Ballad e Blues**.

### 🎸 Tutorial Interattivo delle Scale
* **Tastiera Chitarra:** Visualizzazione dettagliata dei tasti (0–12) con evidenziazione di tonica e intervalli.
* **Manico Violino:** Rappresentazione della 1ª posizione con indicazione visiva dei nastri e della diteggiatura.
* **Riproduzione Acustica:** Ascolto delle scale con sintesi audio nativa.

### 🎙️ Registratore Audio WAV & Database Room
* Registrazione audio in formato **WAV non compresso**.
* **Database Room:** Archiviazione, personalizzazione dei nomi ed eliminazione delle registrazioni e dei preset personalizzati.
* **Condivisione Sicura:** Esportazione ed invio immediato tramite Android `FileProvider`.

---

## 🏗️ Tech Stack & Architettura

L'applicazione adotta le linee guida ufficiali **Modern Android Development (MAD)** con architettura reattiva **MVVM**:

* **UI Layer:** Jetpack Compose (Material 3) + Custom Canvas Rendering.
* **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture.
* **Concurrency:** Kotlin Coroutines & `StateFlow` / `SharedFlow` per flussi dati in tempo reale.
* **Local Data:** Room Database con supporto KSP (Kotlin Symbol Processing).
* **Audio DSP:** AudioRecord API con elaborazione nativa del segnale ed algoritmi YIN.
* **File Sharing:** AndroidX `FileProvider` per una condivisione sicura dei file audio WAV.

---

## 📁 Struttura del Progetto

```text
GI-GuitarTuner/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/gi/guitartuner/
│   │   │   │   ├── audio/          # Signal Processing, Algoritmo YIN, AudioRecorder
│   │   │   │   ├── data/           # Room Database, Entities, Data Access Objects (DAO)
│   │   │   │   ├── ui/             # Schermate Compose, Componenti UI, Canvas Oscilloscopio
│   │   │   │   │   ├── components/ # Meter ad arco, metronomo, tastiera/manico
│   │   │   │   │   ├── theme/      # Material 3 Design Tokens
│   │   │   │   │   └── viewmodel/  # ViewModel reattivi con StateFlow
│   │   │   │   └── MainActivity.kt # Entry point con top bar GI-GUITARTUNER
│   │   │   ├── res/                # Layout, Stringhe, Icone
│   │   │   └── AndroidManifest.xml # Permessi e Provider
│   │   └── test/                   # Test Unitari Robolectric & JUnit
│   └── build.gradle.kts            # Dipendenze (Compose, Room, KSP)
├── metadata.json                   # Metadata di progetto
├── settings.gradle.kts             # Configurazione rootProject.name = "GI-GuitarTuner"
└── LICENSE                         # Licenza Open Source MIT
```

---

## 🔒 Permessi Android & Privacy Policy

| Permesso | Motivazione |
| :--- | :--- |
| `android.permission.RECORD_AUDIO` | **Indispensabile** per catturare il segnale del microfono, calcolare l'accordatura YIN, mostrare l'oscilloscopio ed effettuare registrazioni WAV. |
| `android.permission.VIBRATE` | Fornisce un feedback aptico all'utente al raggiungimento della perfetta accordatura. |

> 🛡️ **Privacy Policy:** GI-GuitarTuner lavora **100% Offline**. Nessun dato audio, registrazione o informazione sull'uso viene raccolto, memorizzato su server o trasmesso a terzi. Tutto il processing audio e il salvataggio dei file avviene esclusivamente all'interno del dispositivo.

---

## 🛠️ Istruzioni di Compilazione ed Esecuzione

### Requisiti Prerequisiti
* **Android Studio** (versione Ladybug o successiva raccomandata).
* **JDK 17** o superiore.
* Dispositivo reale Android (API 24+) per testare l'input del microfono e il feedback aptico, oppure emulatore con supporto audio attivo.

### Compilazione da Terminale (Gradle Wrapper)
```bash
# Clona il repository
git clone https://github.com/tuousername/GI-GuitarTuner.git
cd GI-GuitarTuner

# Compila l'APK di Debug
./gradlew assembleDebug

# Esegui i test unitari
./gradlew testDebugUnitTest
```

### Esecuzione da Android Studio
1. Apri Android Studio e seleziona **Open Project**.
2. Seleziona la cartella radice `GI-GuitarTuner`.
3. Attendi la sincronizzazione Gradle.
4. Seleziona il target (Dispositivo Fisico o Emulatore) e premi **Run (▶)** (`Shift + F10`).

---

## 📋 Note sulle Ultime Modifiche
* **Ridenominazione completata:** L'applicazione è stata rinominata in **GI-GuitarTuner** aggiornando `metadata.json`, `strings.xml`, `settings.gradle.kts` e la barra di stato di `MainActivity.kt`.
* **Test automatizzati:** I test unitari (`ExampleRobolectricTest.kt`) sono stati aggiornati con successo (`BUILD SUCCESSFUL`).

---

## 📄 Licenza (LICENSE)

```text
MIT License

Copyright (c) 2026 GI-GuitarTuner

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
