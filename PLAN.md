# Piano di miglioramento — MinimalTask

Obiettivo: trasformare l'app in un task manager **affidabile e utile ogni giorno**, senza
cambiare stack (Kotlin, Compose, Room, Hilt, DataStore) e senza funzioni "cloud".
Lavorare sul branch `claude/exciting-maxwell-s4dn81` (PR #1 già aperta), un commit per step,
nell'ordine indicato: ogni step è autonomo e lascia l'app funzionante.

## Vincoli generali
- Nessuna `fallbackToDestructiveMigration()` a regime: ogni modifica alle entity Room
  richiede `version + 1` e una `Migration` esplicita (gli utenti non devono perdere i task).
- Tutte le stringhe visibili vanno in `res/values/strings.xml` (oggi sono hardcoded).
- Compilare (`./gradlew assembleDebug lintDebug testDebugUnitTest`) prima di ogni push.
- Non toccare il flusso Billing/Premium se non per il punto 6.

---

## Step 0 — Rendere il progetto compilabile e verificabile (prerequisito)
Il repo non ha il Gradle wrapper né CI: la PR #1 non è mai stata compilata.
1. Aggiungere `gradlew`, `gradlew.bat`, `gradle/wrapper/*` (Gradle 8.9+ compatibile con AGP 8.7.3).
2. Workflow GitHub Actions `.github/workflows/android.yml`: JDK 17, `assembleDebug`,
   `lintDebug`, `testDebugUnitTest`, cache Gradle.
3. Correggere gli eventuali errori di compilazione introdotti dalla PR #1.
4. Rimuovere `app/schemas/` dal `.gitignore` e versionare gli schemi Room (servono per le migrazioni).

**Fatto quando:** CI verde sulla PR.

## Step 1 — Promemoria che funzionano davvero
Oggi il promemoria è solo "tra 1 ora" e si perde al riavvio del telefono.
1. `Task.reminderTime` impostabile a **data + ora scelte** (DatePicker + TimePicker M3) nel `TaskFormScreen`;
   mantenere la scorciatoia "tra 1 ora".
2. `BootReceiver` su `BOOT_COMPLETED` (+ permesso `RECEIVE_BOOT_COMPLETED`) che rilegge i task
   con `reminderTime` futuro e li rischedula tramite `ReminderScheduler`.
3. Chiedere `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` in modo corretto per API 31–34
   (link alle impostazioni se negato), con il fallback inesatto già presente in `AlarmScheduling.kt`.
4. Notifica del promemoria: tap apre direttamente il task (`edit_task/{id}` via deep link),
   azione "Completa" dalla notifica.

**Fatto quando:** un promemoria impostato per domani alle 9 arriva anche dopo un riavvio.

## Step 2 — Home che aiuta a decidere cosa fare
1. Ordinamento di default: scaduti → oggi → prossimi giorni → senza data; a parità, priorità.
   Mettere l'`ORDER BY` nella query Room (`dueDate IS NULL` per ultimo).
2. Sezioni con intestazione nella `LazyColumn` ("Scaduti", "Oggi", "Prossimi", "Senza scadenza"),
   nascoste se vuote.
3. Swipe sulla card: a destra completa, a sinistra elimina (con Snackbar "Annulla" già esistente
   per il completamento; conferma per l'eliminazione).
4. Un badge con il numero di task scaduti sull'icona Home della bottom bar.

**Fatto quando:** aprendo l'app si vede subito cosa è urgente senza usare i filtri.

## Step 3 — Focus collegato ai task
1. Nella `FocusScreen` scegliere un task attivo su cui concentrarsi (dropdown/bottom sheet);
   salvare `taskId` in `FocusSession` (migrazione Room v2).
2. Usare `breakMinutes` (già salvato in DataStore ma mai letto): al termine di un Pomodoro
   parte automaticamente la pausa, poi il ciclo successivo.
3. Notifica persistente durante la sessione con tempo residuo e azione "Pausa"
   (foreground service leggero oppure notifica aggiornata dal ViewModel).
4. Sul task: mostrare i minuti di focus accumulati rispetto alla stima (`estimatedMinutes`).

**Fatto quando:** chiudendo l'app il timer prosegue e, a fine sessione, i minuti compaiono sul task.

## Step 4 — Statistiche comprensibili (Premium)
1. Etichette sotto le barre (giorno della settimana / giorno del mese) e valore sopra la barra.
2. Tre numeri in alto: completati oggi, serie di giorni consecutivi (streak), minuti focus della settimana.
3. Ripartizione per categoria (lista con barre orizzontali, niente librerie esterne).

## Step 5 — Export utile
Oggi mostra solo il path del file.
1. `FileProvider` + `Intent.ACTION_SEND`: dopo l'export si apre il foglio di condivisione.
2. PDF: descrizione, scadenza e stato per ogni task; a capo automatico con `StaticLayout`
   invece del troncamento.
3. Aggiungere export **CSV** (apribile con Excel/Sheets).

## Step 6 — Qualità e manutenibilità
1. Test unitari (JUnit + kotlinx-coroutines-test + Turbine):
   `TaskViewModel.applySmartFilter`, `FocusViewModel` (start/pausa/fine, nessuna sessione doppia),
   `Priority.fromValue`.
2. Test Room in `androidTest` per `TaskDao.searchTasks` e per la migrazione 1→2.
3. Version catalog `gradle/libs.versions.toml` al posto delle versioni inline.
4. Tema: sostituire `LightScheme.withSecondary()` con `ColorScheme` completi per i tre temi colorati
   (oggi `primary` e `secondary` coincidono e i contrasti sono casuali).
5. Localizzazione: `values/strings.xml` in italiano + `values-en/`.

---

## Fuori scopo (non fare)
Sincronizzazione cloud, account utente, widget home screen, sottotask, ricorrenze,
librerie di charting, migrazione a Navigation type-safe. Possono venire dopo.

## Ordine consigliato se il tempo è poco
0 → 1 → 2 → 6.1 (test) → 5 → 3 → 4 → resto del 6.
