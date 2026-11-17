'use strict';
const $ = (s, r = document) => r.querySelector(s);
const esc = (s) => String(s == null ? '' : s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const safeImg = (u) => (/^(https:\/\/|\.\.\/assets\/[\w\/.-]+$)/i.test(u || '') ? u : '');

const I18N = {
  en: { launchpad: 'Launchpad', mods: 'Modules', servers: 'Servers', cosmetics: 'Cosmetics', settings: 'Settings', launch: 'LAUNCH GAME', launching: 'LAUNCHING...',
    welcome: 'Minecraft 1.8.9 PvP client', news: 'News', noAcc: 'Not signed in', signIn: 'Sign in', switchAcc: 'Switch account', advert: 'Ad space',
    play: 'Play', noServers: 'No servers yet. Add them in launcher/content.json', modsHint: 'Enable or disable modules. Changes apply the next time you launch.',
    store: 'Open store', storeHint: 'Cosmetics are bought on our website and equipped in game (Right Shift > Cosmetics).', notConf: 'The store is not configured yet.', offline: 'Store unreachable.',
    language: 'Language', ram: 'Memory (MB)', java: 'Java 8 path', detect: 'Detect', minimize: 'Minimize when game starts', folder: 'Open game folder', logout: 'Sign out', console: 'Console',
    microsoft: 'Microsoft login', offlineL: 'Offline name', offlineBtn: 'Continue offline', cancel: 'Close', name_short: 'Name must be at least 3 characters.', ms_failed: 'Microsoft login failed.',
    need_login: 'Sign in first.', session_expired: 'Your Microsoft session expired. Sign in again (Account > Sign in).', launch_failed: 'Launch failed.', launch_error: 'Error', crashed: 'The game closed unexpectedly. Check the console.', closed: 'Game closed.', ingame: 'In game',
    updating: 'Updating client...', loader: 'Preparing loader...', starting: 'Starting game...', download: 'Downloading game files...', wings: 'Wings', cape: 'Capes', pet: 'Pets', bandana: 'Bandanas', all: 'All', price: 'Price', online: 'Online', javaHint: 'Optional: the launcher downloads Java by itself' },
  it: { launchpad: 'Launchpad', mods: 'Moduli', servers: 'Server', cosmetics: 'Cosmetici', settings: 'Impostazioni', launch: 'AVVIA GIOCO', launching: 'AVVIO...',
    welcome: 'Client PvP per Minecraft 1.8.9', news: 'Novità', noAcc: 'Non connesso', signIn: 'Accedi', switchAcc: 'Cambia account', advert: 'Spazio pubblicitario',
    play: 'Gioca', noServers: 'Nessun server. Aggiungili in launcher/content.json', modsHint: 'Attiva o disattiva i moduli. Le modifiche valgono al prossimo avvio.',
    store: 'Apri lo store', storeHint: 'I cosmetici si comprano sul nostro sito e si equipaggiano in gioco (Shift destro > Cosmetici).', notConf: 'Lo store non è ancora configurato.', offline: 'Store non raggiungibile.',
    language: 'Lingua', ram: 'Memoria (MB)', java: 'Percorso Java 8', detect: 'Rileva', minimize: 'Riduci a icona all\'avvio', folder: 'Apri cartella di gioco', logout: 'Esci', console: 'Console',
    microsoft: 'Accesso Microsoft', offlineL: 'Nome offline', offlineBtn: 'Continua offline', cancel: 'Chiudi', name_short: 'Il nome deve avere almeno 3 caratteri.', ms_failed: 'Accesso Microsoft non riuscito.',
    need_login: 'Accedi prima.', session_expired: 'La sessione Microsoft è scaduta. Accedi di nuovo (Account > Accedi).', launch_failed: 'Avvio non riuscito.', launch_error: 'Errore', crashed: 'Il gioco si è chiuso all\'improvviso. Controlla la console.', closed: 'Gioco chiuso.', ingame: 'In gioco',
    updating: 'Aggiorno il client...', loader: 'Preparo il loader...', starting: 'Avvio il gioco...', download: 'Scarico i file di gioco...', wings: 'Ali', cape: 'Mantelli', pet: 'Animaletti', bandana: 'Bandane', all: 'Tutti', price: 'Prezzo', online: 'Online', javaHint: 'Facoltativo: il launcher scarica Java da solo' },
  es: { launchpad: 'Launchpad', mods: 'Módulos', servers: 'Servidores', cosmetics: 'Cosméticos', settings: 'Ajustes', launch: 'INICIAR JUEGO', launching: 'INICIANDO...',
    welcome: 'Cliente PvP de Minecraft 1.8.9', news: 'Noticias', noAcc: 'Sin sesión', signIn: 'Iniciar sesión', switchAcc: 'Cambiar cuenta', advert: 'Espacio publicitario',
    play: 'Jugar', noServers: 'Aún no hay servidores. Añádelos en launcher/content.json', modsHint: 'Activa o desactiva módulos. Los cambios se aplican al iniciar.',
    store: 'Abrir tienda', storeHint: 'Los cosméticos se compran en nuestra web y se equipan en el juego (Shift der. > Cosméticos).', notConf: 'La tienda aún no está configurada.', offline: 'Tienda no disponible.',
    language: 'Idioma', ram: 'Memoria (MB)', java: 'Ruta de Java 8', detect: 'Detectar', minimize: 'Minimizar al iniciar', folder: 'Abrir carpeta del juego', logout: 'Cerrar sesión', console: 'Consola',
    microsoft: 'Cuenta Microsoft', offlineL: 'Nombre sin conexión', offlineBtn: 'Continuar sin conexión', cancel: 'Cerrar', name_short: 'El nombre necesita 3 caracteres como mínimo.', ms_failed: 'Error al iniciar sesión con Microsoft.',
    need_login: 'Inicia sesión primero.', session_expired: 'La sesión de Microsoft caducó. Inicia sesión de nuevo.', launch_failed: 'Error al iniciar.', launch_error: 'Error', crashed: 'El juego se cerró inesperadamente. Revisa la consola.', closed: 'Juego cerrado.', ingame: 'En juego',
    updating: 'Actualizando cliente...', loader: 'Preparando loader...', starting: 'Iniciando juego...', download: 'Descargando archivos...', wings: 'Alas', cape: 'Capas', pet: 'Mascotas', bandana: 'Bandanas', all: 'Todo', price: 'Precio', online: 'En línea', javaHint: 'Opcional: el launcher descarga Java solo' },
  de: { launchpad: 'Launchpad', mods: 'Module', servers: 'Server', cosmetics: 'Kosmetik', settings: 'Einstellungen', launch: 'SPIEL STARTEN', launching: 'STARTE...',
    welcome: 'PvP-Client für Minecraft 1.8.9', news: 'Neuigkeiten', noAcc: 'Nicht angemeldet', signIn: 'Anmelden', switchAcc: 'Konto wechseln', advert: 'Werbefläche',
    play: 'Spielen', noServers: 'Noch keine Server. Trage sie in launcher/content.json ein', modsHint: 'Module ein-/ausschalten. Gilt ab dem nächsten Start.',
    store: 'Shop öffnen', storeHint: 'Kosmetik wird auf unserer Website gekauft und im Spiel ausgerüstet (Rechts-Shift > Kosmetik).', notConf: 'Der Shop ist noch nicht eingerichtet.', offline: 'Shop nicht erreichbar.',
    language: 'Sprache', ram: 'Speicher (MB)', java: 'Java-8-Pfad', detect: 'Suchen', minimize: 'Beim Start minimieren', folder: 'Spielordner öffnen', logout: 'Abmelden', console: 'Konsole',
    microsoft: 'Microsoft-Login', offlineL: 'Offline-Name', offlineBtn: 'Offline fortfahren', cancel: 'Schließen', name_short: 'Name braucht mindestens 3 Zeichen.', ms_failed: 'Microsoft-Login fehlgeschlagen.',
    need_login: 'Bitte zuerst anmelden.', launch_failed: 'Start fehlgeschlagen.', launch_error: 'Fehler', crashed: 'Das Spiel wurde unerwartet beendet. Siehe Konsole.', closed: 'Spiel beendet.', ingame: 'Im Spiel',
    updating: 'Client wird aktualisiert...', loader: 'Loader wird vorbereitet...', starting: 'Spiel startet...', download: 'Lade Spieldateien...', wings: 'Flügel', cape: 'Umhänge', pet: 'Haustiere', bandana: 'Bandanas', all: 'Alle', price: 'Preis', online: 'Online', javaHint: 'Optional: der Launcher lädt Java selbst herunter' }
};
Object.assign(I18N.en, { back: 'Welcome back', versions: 'Versions', latest: 'Latest', releases: 'Releases', snapshots: 'Snapshots', old: 'Old', search: 'Search version...', modsFor: 'Modules for Minecraft', avail: 'available on this version',
  needs: 'Only on', vanilla: 'Vanilla', ya: 'YA CLIENT', soon: 'Soon', vanillaNote: 'This version starts without YA CLIENT mods. The mods below work on', java_missing: 'Java {n} is required for this version. Install it (Temurin {n}) and try again.', latestRelease: 'Latest release', change: 'Change version', noRes: 'No versions found.', offlineList: 'Offline: showing a short list of versions.' });
Object.assign(I18N.it, { back: 'Bentornato', versions: 'Versioni', latest: 'Ultima', releases: 'Release', snapshots: 'Snapshot', old: 'Vecchie', search: 'Cerca versione...', modsFor: 'Mod per Minecraft', avail: 'disponibili in questa versione',
  needs: 'Solo su', vanilla: 'Vanilla', ya: 'YA CLIENT', soon: 'Presto', vanillaNote: 'Questa versione parte senza le mod di YA CLIENT. Le mod qui sotto funzionano su', java_missing: 'Per questa versione serve Java {n}. Installalo (Temurin {n}) e riprova.', latestRelease: 'Ultima release', change: 'Cambia versione', noRes: 'Nessuna versione trovata.', offlineList: 'Offline: mostro un elenco ridotto di versioni.' });
Object.assign(I18N.es, { back: 'Bienvenido de nuevo', versions: 'Versiones', latest: 'Última', releases: 'Releases', snapshots: 'Snapshots', old: 'Antiguas', search: 'Buscar versión...', modsFor: 'Mods para Minecraft', avail: 'disponibles en esta versión',
  needs: 'Solo en', vanilla: 'Vanilla', ya: 'YA CLIENT', soon: 'Pronto', vanillaNote: 'Esta versión inicia sin los mods de YA CLIENT. Los mods de abajo funcionan en', java_missing: 'Esta versión necesita Java {n}. Instálalo (Temurin {n}) e inténtalo de nuevo.', latestRelease: 'Última release', change: 'Cambiar versión', noRes: 'No hay versiones.', offlineList: 'Sin conexión: lista reducida de versiones.' });
Object.assign(I18N.de, { session_expired: 'Die Microsoft-Sitzung ist abgelaufen. Bitte neu anmelden.', back: 'Willkommen zurück', versions: 'Versionen', latest: 'Neueste', releases: 'Releases', snapshots: 'Snapshots', old: 'Alt', search: 'Version suchen...', modsFor: 'Mods für Minecraft', avail: 'in dieser Version verfügbar',
  needs: 'Nur in', vanilla: 'Vanilla', ya: 'YA CLIENT', soon: 'Bald', vanillaNote: 'Diese Version startet ohne YA-CLIENT-Mods. Die Mods unten funktionieren in', java_missing: 'Für diese Version wird Java {n} benötigt. Installiere es (Temurin {n}) und versuche es erneut.', latestRelease: 'Neueste Release', change: 'Version ändern', noRes: 'Keine Versionen gefunden.', offlineList: 'Offline: gekürzte Versionsliste.' });
Object.assign(I18N.en, {"plus_nav": "YA Plus", "chip_on": "YA Plus active", "chip_off": "Get YA Plus", "online_s": "Online", "preview": "Preview", "p_title": "YA Plus", "p_promo_t": "Upgrade to YA Plus", "p_promo_d": "Turns the launcher red and animates your cape in game.", "p_buy": "Get YA Plus", "p_active_t": "YA Plus is active", "p_active_d": "Thanks for supporting YA CLIENT. Your perks are active.", "p_rerun": "Run setup again", "p_how": "Plus is added to your account from our store or Discord.", "perk_theme": "Red launcher theme", "perk_cape": "Animated cape movement", "perk_more": "More perks soon", "perk_setup": "Guided setup", "wp_eyebrow": "YA CLIENT PLUS", "wp_title_a": "Welcome to", "wp_title_b": "Plus", "wp_sub": "Your YA Plus subscription is active on this launcher. Here is what you just unlocked.", "cont": "Continue", "skip": "Skip for now", "back_b": "Back", "finish": "Start playing", "st_title": "Set up your client", "st_sub": "Pick how YA CLIENT should start. You can change everything later in Settings and Modules.", "lbl_preset": "MOD PRESET", "pre_pvp": "PvP", "pre_pvp_d": "Combat mods and utilities on, extra info HUD off", "pre_all": "Everything", "pre_all_d": "Every available mod enabled", "pre_min": "Minimal", "pre_min_d": "Only performance and basic HUD", "lbl_perf": "PERFORMANCE", "ram_l": "Game memory", "ram_d": "Recommended for PvP: 3 GB", "done_t": "You are all set", "done_s": "Your launcher is red and your Plus perks are active in game. Join a server and see your cape move."});
Object.assign(I18N.it, {"plus_nav": "YA Plus", "chip_on": "YA Plus attivo", "chip_off": "Ottieni YA Plus", "online_s": "Online", "preview": "Anteprima", "p_title": "YA Plus", "p_promo_t": "Passa a YA Plus", "p_promo_d": "Rende il launcher rosso e anima il tuo mantello in gioco.", "p_buy": "Ottieni YA Plus", "p_active_t": "YA Plus è attivo", "p_active_d": "Grazie per supportare YA CLIENT. I tuoi vantaggi sono attivi.", "p_rerun": "Rifai la configurazione", "p_how": "Plus viene aggiunto al tuo account dal nostro store o da Discord.", "perk_theme": "Launcher a tema rosso", "perk_cape": "Mantello animato", "perk_more": "Altri vantaggi in arrivo", "perk_setup": "Configurazione guidata", "wp_eyebrow": "YA CLIENT PLUS", "wp_title_a": "Benvenuto in", "wp_title_b": "Plus", "wp_sub": "Il tuo abbonamento YA Plus è attivo su questo launcher. Ecco cosa hai appena sbloccato.", "cont": "Continua", "skip": "Salta per ora", "back_b": "Indietro", "finish": "Inizia a giocare", "st_title": "Configura il tuo client", "st_sub": "Scegli come deve partire YA CLIENT. Potrai cambiare tutto dopo in Impostazioni e Moduli.", "lbl_preset": "PRESET MOD", "pre_pvp": "PvP", "pre_pvp_d": "Mod di combattimento e utilità attive, HUD info spento", "pre_all": "Tutto", "pre_all_d": "Tutte le mod disponibili attive", "pre_min": "Minimo", "pre_min_d": "Solo prestazioni e HUD base", "lbl_perf": "PRESTAZIONI", "ram_l": "Memoria di gioco", "ram_d": "Consigliato per PvP: 3 GB", "done_t": "Tutto pronto", "done_s": "Il launcher è rosso e i tuoi vantaggi Plus sono attivi in gioco. Entra in un server e guarda il mantello muoversi."});
Object.assign(I18N.es, {"plus_nav": "YA Plus", "chip_on": "YA Plus activo", "chip_off": "Consigue YA Plus", "online_s": "En línea", "preview": "Vista previa", "p_title": "YA Plus", "p_promo_t": "Pásate a YA Plus", "p_promo_d": "Pone el launcher en rojo y anima tu capa en el juego.", "p_buy": "Consigue YA Plus", "p_active_t": "YA Plus está activo", "p_active_d": "Gracias por apoyar YA CLIENT. Tus ventajas están activas.", "p_rerun": "Repetir configuración", "p_how": "Plus se añade a tu cuenta desde nuestra tienda o Discord.", "perk_theme": "Launcher en rojo", "perk_cape": "Capa animada", "perk_more": "Más ventajas pronto", "perk_setup": "Configuración guiada", "wp_eyebrow": "YA CLIENT PLUS", "wp_title_a": "Bienvenido a", "wp_title_b": "Plus", "wp_sub": "Tu suscripción YA Plus está activa en este launcher. Esto es lo que acabas de desbloquear.", "cont": "Continuar", "skip": "Omitir por ahora", "back_b": "Atrás", "finish": "Empezar a jugar", "st_title": "Configura tu cliente", "st_sub": "Elige cómo debe iniciar YA CLIENT. Podrás cambiarlo luego en Ajustes y Módulos.", "lbl_preset": "PRESET DE MODS", "pre_pvp": "PvP", "pre_pvp_d": "Mods de combate y utilidades activos, HUD de info apagado", "pre_all": "Todo", "pre_all_d": "Todos los mods disponibles activos", "pre_min": "Mínimo", "pre_min_d": "Solo rendimiento y HUD básico", "lbl_perf": "RENDIMIENTO", "ram_l": "Memoria del juego", "ram_d": "Recomendado para PvP: 3 GB", "done_t": "Todo listo", "done_s": "Tu launcher es rojo y tus ventajas Plus están activas en el juego."});
Object.assign(I18N.de, {"plus_nav": "YA Plus", "chip_on": "YA Plus aktiv", "chip_off": "YA Plus holen", "online_s": "Online", "preview": "Vorschau", "p_title": "YA Plus", "p_promo_t": "Auf YA Plus upgraden", "p_promo_d": "Färbt den Launcher rot und animiert deinen Umhang im Spiel.", "p_buy": "YA Plus holen", "p_active_t": "YA Plus ist aktiv", "p_active_d": "Danke für die Unterstützung von YA CLIENT. Deine Vorteile sind aktiv.", "p_rerun": "Einrichtung wiederholen", "p_how": "Plus wird deinem Konto über unseren Shop oder Discord hinzugefügt.", "perk_theme": "Roter Launcher", "perk_cape": "Animierter Umhang", "perk_more": "Bald mehr Vorteile", "perk_setup": "Geführte Einrichtung", "wp_eyebrow": "YA CLIENT PLUS", "wp_title_a": "Willkommen bei", "wp_title_b": "Plus", "wp_sub": "Dein YA-Plus-Abo ist in diesem Launcher aktiv. Das hast du gerade freigeschaltet.", "cont": "Weiter", "skip": "Vorerst überspringen", "back_b": "Zurück", "finish": "Los geht's", "st_title": "Richte deinen Client ein", "st_sub": "Wähle, wie YA CLIENT starten soll. Du kannst alles später ändern.", "lbl_preset": "MOD-PRESET", "pre_pvp": "PvP", "pre_pvp_d": "Kampf-Mods und Hilfsmittel an, Info-HUD aus", "pre_all": "Alles", "pre_all_d": "Alle verfügbaren Mods aktiv", "pre_min": "Minimal", "pre_min_d": "Nur Leistung und Basis-HUD", "lbl_perf": "LEISTUNG", "ram_l": "Spielspeicher", "ram_d": "Empfohlen für PvP: 3 GB", "done_t": "Alles bereit", "done_s": "Dein Launcher ist rot und deine Plus-Vorteile sind im Spiel aktiv."});
Object.assign(I18N.en, {"wp_eyebrow2": "PLUS UNLOCKED", "wp_h": "You're in.", "f1t": "Red launcher", "f1d": "The whole launcher switches to the YA red look.", "f2t": "Animated cape", "f2d": "Your cape moves with cloth motion in game.", "f3t": "More on the way", "f3d": "New Plus perks unlock automatically."});
Object.assign(I18N.it, {"wp_eyebrow2": "PLUS SBLOCCATO", "wp_h": "Sei dentro.", "f1t": "Launcher rosso", "f1d": "Tutto il launcher passa allo stile rosso di YA.", "f2t": "Mantello animato", "f2d": "Il tuo mantello si muove come un vero tessuto in gioco.", "f3t": "Altro in arrivo", "f3d": "I nuovi vantaggi Plus si sbloccano da soli."});
Object.assign(I18N.es, {"wp_eyebrow2": "PLUS DESBLOQUEADO", "wp_h": "Ya estás dentro.", "f1t": "Launcher rojo", "f1d": "Todo el launcher cambia al estilo rojo de YA.", "f2t": "Capa animada", "f2d": "Tu capa se mueve como una tela real en el juego.", "f3t": "Más por venir", "f3d": "Las nuevas ventajas Plus se desbloquean solas."});
Object.assign(I18N.de, {"wp_eyebrow2": "PLUS FREIGESCHALTET", "wp_h": "Du bist dabei.", "f1t": "Roter Launcher", "f1d": "Der ganze Launcher wechselt in den roten YA-Look.", "f2t": "Animierter Umhang", "f2d": "Dein Umhang bewegt sich im Spiel wie echter Stoff.", "f3t": "Mehr folgt", "f3d": "Neue Plus-Vorteile werden automatisch freigeschaltet."});
Object.assign(I18N.en, { java_dl: 'Downloading Java (first launch only)...' });
Object.assign(I18N.it, { java_dl: 'Scarico Java (solo al primo avvio)...' });
Object.assign(I18N.es, { java_dl: 'Descargando Java (solo la primera vez)...' });
Object.assign(I18N.de, { java_dl: 'Lade Java (nur beim ersten Start)...' });
Object.assign(I18N.en, {"friends_nav": "Friends", "fr_title": "Friends", "fr_sub": "Add friends by their Minecraft name and see who is online.", "fr_ph": "Minecraft name", "fr_add": "Add friend", "fr_req": "Requests", "fr_on": "Online", "fr_off": "Offline", "fr_sent": "Sent", "fr_accept": "Accept", "fr_decline": "Decline", "fr_remove": "Remove", "fr_cancel": "Cancel", "fr_wants": "Wants to be your friend", "fr_pending": "Request sent", "fr_game": "Playing now", "fr_launcher": "In the launcher", "fr_offl": "Offline", "fr_empty": "No friends yet. Add someone with their Minecraft name above.", "fr_premium": "Friends need a Microsoft account. Sign in with Microsoft to use them.", "fr_login": "Could not sign in to the friends service. Try signing in again.", "fr_offline": "Friends are not available right now.", "fr_loading": "Loading...", "fr_sending": "Sending...", "fr_r_sent": "Request sent", "fr_r_accepted": "You are now friends", "fr_r_not_found": "Player not found. They must have used YA CLIENT.", "fr_r_self": "You can't add yourself.", "fr_r_already": "Already friends or request pending.", "fr_r_offline": "Could not reach the server.", "fr_n_online": "online"});
Object.assign(I18N.it, {"friends_nav": "Amici", "fr_title": "Amici", "fr_sub": "Aggiungi amici col loro nome Minecraft e guarda chi è online.", "fr_ph": "Nome Minecraft", "fr_add": "Aggiungi amico", "fr_req": "Richieste", "fr_on": "Online", "fr_off": "Offline", "fr_sent": "Inviate", "fr_accept": "Accetta", "fr_decline": "Rifiuta", "fr_remove": "Rimuovi", "fr_cancel": "Annulla", "fr_wants": "Vuole essere tuo amico", "fr_pending": "Richiesta inviata", "fr_game": "In gioco", "fr_launcher": "Nel launcher", "fr_offl": "Offline", "fr_empty": "Ancora nessun amico. Aggiungi qualcuno col suo nome Minecraft qui sopra.", "fr_premium": "Gli amici richiedono un account Microsoft. Accedi con Microsoft per usarli.", "fr_login": "Impossibile accedere al servizio amici. Prova ad accedere di nuovo.", "fr_offline": "Gli amici non sono disponibili al momento.", "fr_loading": "Caricamento...", "fr_sending": "Invio...", "fr_r_sent": "Richiesta inviata", "fr_r_accepted": "Ora siete amici", "fr_r_not_found": "Giocatore non trovato. Deve aver usato YA CLIENT.", "fr_r_self": "Non puoi aggiungere te stesso.", "fr_r_already": "Siete già amici o c'è una richiesta in sospeso.", "fr_r_offline": "Impossibile contattare il server.", "fr_n_online": "online"});
Object.assign(I18N.es, {"friends_nav": "Amigos", "fr_title": "Amigos", "fr_sub": "Añade amigos por su nombre de Minecraft y mira quién está en línea.", "fr_ph": "Nombre de Minecraft", "fr_add": "Añadir amigo", "fr_req": "Solicitudes", "fr_on": "En línea", "fr_off": "Desconectados", "fr_sent": "Enviadas", "fr_accept": "Aceptar", "fr_decline": "Rechazar", "fr_remove": "Eliminar", "fr_cancel": "Cancelar", "fr_wants": "Quiere ser tu amigo", "fr_pending": "Solicitud enviada", "fr_game": "Jugando", "fr_launcher": "En el launcher", "fr_offl": "Desconectado", "fr_empty": "Aún no tienes amigos. Añade a alguien con su nombre de Minecraft arriba.", "fr_premium": "Los amigos necesitan una cuenta Microsoft. Inicia sesión con Microsoft.", "fr_login": "No se pudo iniciar sesión en el servicio de amigos. Inténtalo de nuevo.", "fr_offline": "Los amigos no están disponibles ahora.", "fr_loading": "Cargando...", "fr_sending": "Enviando...", "fr_r_sent": "Solicitud enviada", "fr_r_accepted": "Ya sois amigos", "fr_r_not_found": "Jugador no encontrado. Debe haber usado YA CLIENT.", "fr_r_self": "No puedes añadirte a ti mismo.", "fr_r_already": "Ya sois amigos o hay una solicitud pendiente.", "fr_r_offline": "No se pudo contactar con el servidor.", "fr_n_online": "en línea"});
Object.assign(I18N.de, {"friends_nav": "Freunde", "fr_title": "Freunde", "fr_sub": "Füge Freunde per Minecraft-Namen hinzu und sieh, wer online ist.", "fr_ph": "Minecraft-Name", "fr_add": "Freund hinzufügen", "fr_req": "Anfragen", "fr_on": "Online", "fr_off": "Offline", "fr_sent": "Gesendet", "fr_accept": "Annehmen", "fr_decline": "Ablehnen", "fr_remove": "Entfernen", "fr_cancel": "Abbrechen", "fr_wants": "Möchte dein Freund sein", "fr_pending": "Anfrage gesendet", "fr_game": "Spielt gerade", "fr_launcher": "Im Launcher", "fr_offl": "Offline", "fr_empty": "Noch keine Freunde. Füge oben jemanden per Minecraft-Namen hinzu.", "fr_premium": "Freunde brauchen ein Microsoft-Konto. Melde dich mit Microsoft an.", "fr_login": "Anmeldung beim Freunde-Dienst fehlgeschlagen. Melde dich erneut an.", "fr_offline": "Freunde sind gerade nicht verfügbar.", "fr_loading": "Laden...", "fr_sending": "Sende...", "fr_r_sent": "Anfrage gesendet", "fr_r_accepted": "Ihr seid jetzt Freunde", "fr_r_not_found": "Spieler nicht gefunden. Er muss YA CLIENT benutzt haben.", "fr_r_self": "Du kannst dich nicht selbst hinzufügen.", "fr_r_already": "Bereits befreundet oder Anfrage offen.", "fr_r_offline": "Server nicht erreichbar.", "fr_n_online": "online"});
Object.assign(I18N.en, {"fr_r_not_configured": "The friends server is not set up yet. It will work once the YA CLIENT server is online.", "fr_r_login": "Could not sign in to the friends service. Sign in with Microsoft again.", "fr_r_premium": "Friends need a Microsoft account."});
Object.assign(I18N.it, {"fr_r_not_configured": "Il server degli amici non è ancora attivo. Funzionerà appena il server di YA CLIENT sarà online.", "fr_r_login": "Impossibile accedere al servizio amici. Accedi di nuovo con Microsoft.", "fr_r_premium": "Gli amici richiedono un account Microsoft."});
Object.assign(I18N.es, {"fr_r_not_configured": "El servidor de amigos aún no está activo. Funcionará cuando el servidor de YA CLIENT esté en línea.", "fr_r_login": "No se pudo iniciar sesión en el servicio de amigos. Inicia sesión con Microsoft otra vez.", "fr_r_premium": "Los amigos necesitan una cuenta Microsoft."});
Object.assign(I18N.de, {"fr_r_not_configured": "Der Freunde-Server ist noch nicht aktiv. Er funktioniert, sobald der YA-CLIENT-Server online ist.", "fr_r_login": "Anmeldung beim Freunde-Dienst fehlgeschlagen. Melde dich erneut mit Microsoft an.", "fr_r_premium": "Freunde brauchen ein Microsoft-Konto."});
const LANGS = { en: 'English', it: 'Italiano', es: 'Español', de: 'Deutsch' };
const SLOT = { ya_cape: 'cape', ya_wings: 'wings', ya_bandana: 'bandana', ya_pet: 'pet', mono_cape: 'cape', mono_wings: 'wings', mono_bandana: 'bandana', mono_pet: 'pet', qlux_cape: 'cape', qlux_wings: 'wings', qlux_bandana: 'bandana', qlux_pet: 'pet', sakura_cape: 'cape', sakura_wings: 'wings', sakura_bandana: 'bandana', sakura_pet: 'pet', shinobi_cape: 'cape', shinobi_wings: 'wings', shinobi_bandana: 'bandana', shinobi_pet: 'pet', kitsune_cape: 'cape', kitsune_wings: 'wings', kitsune_bandana: 'bandana', kitsune_pet: 'pet', santa_cape: 'cape', santa_wings: 'wings', santa_bandana: 'bandana', santa_pet: 'pet', frost_cape: 'cape', frost_wings: 'wings', frost_bandana: 'bandana', frost_pet: 'pet' };
const NAMES = { ya_cape: 'YA Cape', ya_wings: 'Ember Wings', ya_bandana: 'YA Bandana', ya_pet: 'Ember (pet)', mono_cape: 'Infinity Cape', mono_wings: 'Shadow Dragon Wings', mono_bandana: 'Monochrome Bandana', mono_pet: 'Yin Yang (pet)' , qlux_cape: 'Qlux Cape', qlux_wings: 'Qlux Angel Wings', qlux_bandana: 'Qlux Band', qlux_pet: 'Lumi (pet)', sakura_cape: 'Sakura Cape', sakura_wings: 'Petal Wings', sakura_bandana: 'Blossom Band', sakura_pet: 'Mochi (pet)', shinobi_cape: 'Moonlit Cloak', shinobi_wings: 'Blade Wings', shinobi_bandana: 'Shinobi Headband', shinobi_pet: 'Kage (pet)', kitsune_cape: 'Kitsune Cape', kitsune_wings: 'Flame Wings', kitsune_bandana: 'Shrine Ribbon', kitsune_pet: 'Inari (pet)', santa_cape: 'Santa Cape', santa_wings: 'Candy Wings', santa_bandana: 'Santa Band', santa_pet: 'Rudy (pet)', frost_cape: 'Snowflake Cape', frost_wings: 'Ice Wings', frost_bandana: 'Frost Band', frost_pet: 'Frosty (pet)' };
const ICON = {
  home: '<path d="M3 11l9-8 9 8"/><path d="M5 10v10h14V10"/>',
  versions: '<path d="M12 3l9 5-9 5-9-5z"/><path d="M3 13l9 5 9-5"/><path d="M3 17.5l9 5 9-5"/>',
  mods: '<rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>',
  servers: '<rect x="3" y="4" width="18" height="7" rx="2"/><rect x="3" y="13" width="18" height="7" rx="2"/><path d="M7 7.5h.01M7 16.5h.01"/>',
  cosmetics: '<path d="M8 3L3.5 5.5 2 10l3.5 1.5L7 10v11h10V10l1.5 1.5L22 10l-1.5-4.5L16 3"/><path d="M8 3c.5 2 2 3.5 4 3.5S15.5 5 16 3"/>',
  console: '<rect x="3" y="4" width="18" height="16" rx="2.5"/><path d="M7 9l3 3-3 3M12 16h5"/>',
  settings: '<circle cx="12" cy="12" r="3"/><path d="M12 2v3M12 19v3M2 12h3M19 12h3M4.9 4.9l2.1 2.1M17 17l2.1 2.1M4.9 19.1L7 17M17 7l2.1-2.1"/>',
  lock: '<rect x="5" y="11" width="14" height="10" rx="2.5"/><path d="M8 11V8a4 4 0 0 1 8 0v3"/>',
  plus: '<path d="M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9z"/>',
  check: '<path d="M5 12.5l4.5 4.5L19 7.5"/>',
  chev: '<path d="M6 9l6 6 6-6"/>',
  friends: '<circle cx="9" cy="8" r="3.6"/><path d="M2.5 20c0-4 3-6 6.5-6s6.5 2 6.5 6"/><path d="M16 4.6a3.6 3.6 0 0 1 1.4 6.4M18 14.2c2.4.6 3.8 2.5 3.8 5.6"/>',
  user: '<circle cx="12" cy="8" r="4"/><path d="M4 21c1-4 4-6 8-6s7 2 8 6"/>'
};
const ic = (n) => `<svg viewBox="0 0 24 24">${ICON[n]}</svg>`;

let S = { lang: 'en', account: null, ram: 2048, javaPath: 'java', minimizeOnLaunch: false };
let C = {}; let MODSTATE = {}; let MODS = []; let VERS = { versions: [], latest: {}, online: true }; let vtab = 'release'; let vq = ''; let page = 'home'; let busy = false; let cosTab = 'all';
let logBuf = []; let status = { text: '', pct: 0, show: false };
const t = (k) => (I18N[S.lang] && I18N[S.lang][k]) || I18N.en[k] || k;

const PAGES = ['home', 'versions', 'servers', 'friends', 'cosmetics', 'plus', 'console', 'settings'];
const PKEY = { home: 'launchpad', versions: 'versions', servers: 'servers', friends: 'friends_nav', cosmetics: 'cosmetics', console: 'console', settings: 'settings', plus: 'plus_nav' };

function renderNav() {
  $('#nav').innerHTML = PAGES.map((p) => `<button class="nav ${p === page ? 'on' : ''} ${p === 'plus' ? 'star' : ''}" data-p="${p}">${ic(p)}<em>${esc(t(PKEY[p]))}</em>${p === 'friends' && FR.data && FR.data.incoming.length ? `<b class="bdg">${FR.data.incoming.length}</b>` : ''}</button>`).join('');
  $('#accIcon').innerHTML = ic('user');
  $('#accLbl').textContent = S.account ? t('switchAcc') : t('signIn');
  $('#accPill').innerHTML = S.account ? `<i>${esc(S.account.name[0].toUpperCase())}</i>${esc(S.account.name)}` : esc(t('signIn'));
  $('#plusChip').textContent = plusOn() ? t('chip_on') : t('chip_off');
  $('#ver').textContent = (isYa(S.version) ? 'YA CLIENT ' : '') + (S.version || '');
  document.body.classList.toggle('plus', plusOn());
}
const plusOn = () => !!(S.plus || S.plusPreview);
function go(p) { page = p; renderNav(); render(); }

function adHtml(a) {
  if (a && safeImg(a.image)) return `<div class="card ad ${a.url ? 'link' : ''}" data-url="${esc(a.url || '')}"><img src="${esc(safeImg(a.image))}" alt=""></div>`;
  return `<div class="card ad">${esc(t('advert'))}</div>`;
}
function newsHtml(list) {
  if (!list || !list.length) return '';
  return `<h2>${esc(t('news'))}</h2><div class="news">` + list.map((n) => {
    const im = safeImg(n.image);
    return `<div class="card nc ${n.url ? 'link' : ''}" data-url="${esc(n.url || '')}"><div class="im" ${im ? `style="background-image:url('${esc(im)}')"` : ''}></div>
      <div class="tx"><b>${esc(n.title)}</b><p>${esc(n.text)}</p><small>${esc(n.date || '')}</small></div></div>`;
  }).join('') + '</div>';
}

function plusCard() {
  if (plusOn()) return `<div class="card pcard"><span class="tag">PLUS</span><b>${esc(t('p_active_t'))}</b><p>${esc(t('p_active_d'))}</p><button class="btn sm" data-p="plus">${esc(t('plus_nav'))}</button></div>`;
  return `<div class="card pcard"><span class="tag">PLUS</span><b>${esc(t('p_promo_t'))}</b><p>${esc(t('p_promo_d'))}</p><button class="btn sm hi" data-p="plus">${esc(t('p_buy'))}</button></div>`;
}
function viewHome() {
  const ads = C.ads || [];
  const nm = S.account ? S.account.name : '';
  const tag = isYa(S.version) ? 'YA CLIENT' : t('vanilla');
  const av = nm ? `<div class="mc3d" id="mc3d"></div>` : `<img class="logo" src="../assets/logo.png" alt="">`;
  return `<div class="lp"><div>
    <div class="card hero3"><div class="who"><b>${esc(nm || t('noAcc'))}</b>${av}</div>
      <div class="playwrap"><button class="launch2" id="play" ${busy ? 'disabled' : ''}><b>${esc(busy ? t('launching') : t('launch'))}</b><span>${esc(tag)} ${esc(S.version)}</span></button>
</div>
      <div class="bar" id="bar" style="display:${status.show ? 'block' : 'none'}"><i id="barI" style="width:${status.pct}%"></i></div><div class="stat" id="stat">${esc(status.text)}</div></div>
    ${newsHtml(C.news)}</div>
    <div class="side">${plusCard()}${adHtml(ads[0])}${adHtml(ads[1])}</div></div>`;
}
function viewPlus() {
  const perks = ['perk_theme', 'perk_cape', 'perk_more', 'perk_setup'].map((k) => `<span class="perk">${ic('check')}${esc(t(k))}</span>`).join('');
  const on = plusOn();
  return `<h1>${esc(t('p_title'))}</h1><p class="sub">${esc(on ? t('p_active_d') : t('p_promo_d'))}</p>
    <div class="card hero3" style="height:300px;justify-content:center;padding:0"><div style="position:relative;z-index:2;text-align:center;padding:0 24px">
      <span class="tag">${on ? 'ACTIVE' : 'PLUS'}</span><h1 style="font-size:40px;letter-spacing:-1px;margin:6px 0">${esc(on ? t('p_active_t') : t('p_promo_t'))}</h1>
      <div class="perks" style="justify-content:center">${perks}</div>
      ${on ? `<button class="btn hi" id="rerun">${esc(t('p_rerun'))}</button>` : `<button class="btn hi" id="buyPlus">${esc(t('p_buy'))}</button><p class="note" style="margin:12px 0 0">${esc(t('p_how'))}</p>`}
    </div></div>`;
}
function cmpV(a, b) {
  const pa = String(a).split('.').map((n) => parseInt(n, 10) || 0), pb = String(b).split('.').map((n) => parseInt(n, 10) || 0);
  for (let i = 0; i < 3; i++) { const d = (pa[i] || 0) - (pb[i] || 0); if (d) return d; }
  return 0;
}
function specOk(spec, v) {
  spec = String(spec);
  if (spec === '*') return true;
  if (spec.indexOf('>=') === 0) return /^\d+\.\d+/.test(v) && cmpV(v, spec.slice(2)) >= 0;
  if (/\.x$/.test(spec)) return v === spec.slice(0, -2) || v.indexOf(spec.slice(0, -1)) === 0;
  return v === spec;
}
const modOk = (m, v) => !m.soon && (m.versions || []).some((sp) => specOk(sp, v));
const isYa = (v) => v === (S.ya || '1.8.9');

function versionRows() {
  const q = vq.trim().toLowerCase();
  let list = VERS.versions.filter((v) => (vtab === 'release' ? v.type === 'release' : vtab === 'snapshot' ? v.type === 'snapshot' : (v.type === 'old_beta' || v.type === 'old_alpha')));
  if (q) list = list.filter((v) => v.id.toLowerCase().indexOf(q) >= 0);
  const lat = VERS.latest && VERS.latest.release;
  const rows = [];
  const row = (v) => `<button class="vrow ${v.id === S.version ? 'on' : ''}" data-ver="${esc(v.id)}"><b>${esc(v.id)}</b>
    ${v.id === lat ? `<span class="badge">${esc(t('latest'))}</span>` : ''}${isYa(v.id) ? `<span class="badge ya">${esc(t('ya'))}</span>` : ''}
    <small>${esc((v.time || '').slice(0, 10))}</small></button>`;
  list.slice(0, 250).forEach((v) => rows.push(row(v)));
  return rows.join('') || `<div class="note" style="padding:14px">${esc(t('noRes'))}</div>`;
}
function modsGrid() {
  const v = S.version;
  const ok = MODS.filter((m) => modOk(m, v)).length;
  const items = MODS.slice().sort((a, b) => (modOk(b, v) ? 1 : 0) - (modOk(a, v) ? 1 : 0));
  return { ok, html: items.map((m) => {
    const on = modOk(m, v);
    const need = m.soon ? t('soon') : t('needs') + ' ' + (m.versions || []).join(', ');
    return `<div class="card mod ${on ? '' : 'off'}"><div><span>${esc(m.name)}</span><small>${esc(on ? m.desc : need)}</small></div>
      ${on ? `<button class="sw ${MODSTATE[m.id] === false ? '' : 'on'}" data-mod="${esc(m.id)}" aria-label="${esc(m.name)}"></button>` : '<span class="lockic">' + ic('lock') + '</span>'}</div>`;
  }).join('') };
}
function viewVersions() {
  const g = modsGrid();
  const tabs = [['release', 'releases'], ['snapshot', 'snapshots'], ['old', 'old']];
  return `<div class="vp"><div class="card vl"><input type="text" id="vq" placeholder="${esc(t('search'))}" value="${esc(vq)}">
    <div class="tabs" id="vtabs" style="margin:10px 0">${tabs.map((x) => `<button class="tab ${vtab === x[0] ? 'on' : ''}" data-vt="${x[0]}">${esc(t(x[1]))}</button>`).join('')}</div>
    ${VERS.online ? '' : `<div class="note">${esc(t('offlineList'))}</div>`}
    <div class="vlist" id="vlist">${versionRows()}</div></div>
    <div><h1>${esc(t('modsFor'))} ${esc(S.version)}</h1>
    <p class="sub" id="vsub">${esc(g.ok + ' / ' + MODS.length + ' ' + t('avail'))}${isYa(S.version) ? '' : ' · ' + esc(t('vanillaNote')) + ' ' + esc(S.ya || '1.8.9')}</p>
    <div class="mods" id="mgrid">${g.html}</div></div></div>`;
}
function viewServers() {
  const list = C.servers || [];
  let h = `<h1>${esc(t('servers'))}</h1><p class="sub"></p><div class="srv">`;
  if (!list.length) h += `<div class="card sv ph">${esc(t('noServers'))}</div>`;
  list.forEach((s, i) => {
    const im = safeImg(s.image);
    h += `<div class="card sv"><div class="im" ${im ? `style="background-image:url('${esc(im)}')"` : ''}>${im ? '' : esc((s.name || '?')[0].toUpperCase())}</div>
      <div class="tx"><b>${esc(s.name)}</b><small>${esc(s.description || (s.host + (s.port && s.port != 25565 ? ':' + s.port : '')))}</small>
      <button class="btn sm" data-srv="${i}" ${busy ? 'disabled' : ''}>${esc(t('play'))}</button></div></div>`;
  });
  return h + '</div>';
}
let FR = { data: null, err: '', msg: '', ok: true };
function frRows(list, kind) {
  return list.map((f) => {
    const on = kind === 'friend' && f.online;
    const sub = kind === 'req' ? t('fr_wants') : kind === 'sent' ? t('fr_pending') : on ? (f.where === 'game' ? t('fr_game') : t('fr_launcher')) : t('fr_offl');
    const acts = kind === 'req'
      ? `<button class="btn sm hi" data-fa="accept" data-fu="${esc(f.uuid)}">${esc(t('fr_accept'))}</button><button class="btn sm" data-fa="decline" data-fu="${esc(f.uuid)}">${esc(t('fr_decline'))}</button>`
      : kind === 'sent' ? `<button class="btn sm" data-fa="decline" data-fu="${esc(f.uuid)}">${esc(t('fr_cancel'))}</button>`
      : `<button class="btn sm ghostb" data-fa="remove" data-fu="${esc(f.uuid)}">${esc(t('fr_remove'))}</button>`;
    return `<div class="card fr ${kind === 'friend' && !f.online ? 'off' : ''}"><div class="fav"><span>${esc(f.name[0].toUpperCase())}</span><img alt="" src="https://mc-heads.net/avatar/${encodeURIComponent(f.name)}/64">${on ? '<i class="dot"></i>' : ''}</div>
      <div class="ftx"><b>${esc(f.name)}</b><small class="${on ? 'live' : ''}">${esc(sub)}</small></div><div class="fact">${acts}</div></div>`;
  }).join('');
}
function frList() {
  if (FR.err) return `<div class="card sv ph">${esc(t(FR.err === 'not_configured' ? 'fr_r_not_configured' : 'fr_' + FR.err))}</div>`;
  if (!FR.data) return `<div class="card sv ph">${esc(t('fr_loading'))}</div>`;
  const d = FR.data; let h = '';
  const sec = (key, n, body) => body ? `<div class="frh">${esc(t(key))} <em>${n}</em></div>${body}` : '';
  h += sec('fr_req', d.incoming.length, frRows(d.incoming, 'req'));
  const on = d.friends.filter((f) => f.online), off = d.friends.filter((f) => !f.online);
  h += sec('fr_on', on.length, frRows(on, 'friend'));
  h += sec('fr_off', off.length, frRows(off, 'friend'));
  h += sec('fr_sent', d.outgoing.length, frRows(d.outgoing, 'sent'));
  return h || `<div class="card sv ph">${esc(t('fr_empty'))}</div>`;
}
function frSub() {
  const n = FR.data ? FR.data.friends.filter((f) => f.online).length : 0;
  return esc(t('fr_sub')) + (FR.data ? ` <span class="frn"><i class="dot s"></i>${n} ${esc(t('fr_n_online'))}</span>` : '');
}
function viewFriends() {
  return `<h1>${esc(t('fr_title'))}</h1><p class="sub" id="frSub">${frSub()}</p>
    <div class="card fradd"><div class="frin">${ic('friends')}<input id="frName" placeholder="${esc(t('fr_ph'))}" maxlength="16" autocomplete="off" spellcheck="false"></div><button class="btn hi" id="frAdd">${esc(t('fr_add'))}</button></div>
    <div class="frmsg ${FR.ok ? 'ok' : 'bad'}" id="frMsg">${esc(FR.msg)}</div><div class="frl" id="frList">${frList()}</div>`;
}
async function loadFriends() {
  if (!S.account) { FR.data = null; FR.err = 'premium'; return; }
  const r = await window.ya.friends();
  if (r.error) { FR.err = ['not_configured', 'login', 'premium'].includes(r.error) ? r.error : 'offline'; if (!FR.data) FR.data = null; }
  else { FR.err = ''; FR.data = r; }
  renderNav();
  if (page === 'friends') { const l = $('#frList'); if (l) l.innerHTML = frList(); const s = $('#frSub'); if (s) s.innerHTML = frSub(); }
}
setInterval(() => { if (S.account && S.account.type === 'microsoft') loadFriends(); }, 15000);
async function friendAct(a, v) {
  const r = await window.ya.friendAct(a, v);
  if (a === 'add') {
    FR.ok = !r.error; FR.msg = r.error ? (I18N.en['fr_r_' + r.error] ? t('fr_r_' + r.error) : t('fr_r_offline')) : r.status === 'accepted' ? t('fr_r_accepted') : t('fr_r_sent');
    const m = $('#frMsg'); if (m) { m.className = 'frmsg ' + (FR.ok ? 'ok' : 'bad'); m.textContent = FR.msg; }
    if (FR.ok) { const i = $('#frName'); if (i) i.value = ''; }
  }
  await loadFriends();
}
function viewCosmetics() {
  return `<h1>${esc(t('cosmetics'))}</h1><p class="sub">${esc(t('storeHint'))}</p>
    <div class="playrow" style="margin-bottom:18px"><button class="btn" id="openStore">${esc(t('store'))}</button></div>
    <div class="tabs" id="ctabs"></div><div id="cmsg" class="note"></div><div class="cos" id="cgrid"></div>`;
}
async function fillCosmetics() {
  const r = await window.ya.catalog();
  const msg = $('#cmsg'); if (!msg) return;
  if (r.error) { msg.textContent = r.error === 'offline' ? t('offline') : t('notConf'); }
  const prices = (r.data) || {};
  const slots = ['all', 'cape', 'wings', 'bandana', 'pet'];
  $('#ctabs').innerHTML = slots.map((s) => `<button class="tab ${s === cosTab ? 'on' : ''}" data-ct="${s}">${esc(t(s))}</button>`).join('');
  $('#cgrid').innerHTML = Object.keys(SLOT).filter((id) => cosTab === 'all' || SLOT[id] === cosTab).map((id) =>
    `<div class="card ci"><div class="pv">${esc(t(SLOT[id]))}</div><b>${esc(NAMES[id])}</b><small>${esc(prices[id] ? t('price') + ': ' + prices[id] : '')}</small></div>`).join('');
  S._store = r.store || '';
}
function viewSettings() {
  return `<h1>${esc(t('settings'))}</h1><p class="sub"></p><div class="set">
   <div class="card row"><label>${esc(t('language'))}</label><select id="sLang">${Object.keys(LANGS).map((k) => `<option value="${k}" ${k === S.lang ? 'selected' : ''}>${LANGS[k]}</option>`).join('')}</select></div>
   <div class="card row"><label>${esc(t('ram'))}<small id="ramv">${S.ram} MB</small></label><input type="range" id="sRam" min="1024" max="8192" step="256" value="${S.ram}"></div>
   <div class="card row"><label>${esc(t('java'))}<small>${esc(t('javaHint'))}</small></label><div style="display:flex;gap:8px"><input type="text" id="sJava" value="${esc(S.javaPath || 'java')}"><button class="btn sm" id="sDet">${esc(t('detect'))}</button></div></div>
   <div class="card row"><label>${esc(t('minimize'))}</label><button class="sw ${S.minimizeOnLaunch ? 'on' : ''}" id="sMin"></button></div>
   <div class="card row"><label>${esc(t('folder'))}</label><button class="btn sm" id="sFolder">&#8594;</button></div>
   ${S.account ? `<div class="card row"><label>${esc(S.account.name)}</label><button class="btn sm" id="sOut">${esc(t('logout'))}</button></div>` : ''}
   </div>`;
}
function viewConsole() {
  return `<h1>${esc(t('console'))}</h1><p class="sub"></p><div class="con conp" id="con">${esc(logBuf.join('\n'))}</div>`;
}
function render() {
  const v = $('#view');
  v.innerHTML = { home: viewHome, plus: viewPlus, versions: viewVersions, servers: viewServers, friends: viewFriends, cosmetics: viewCosmetics, console: viewConsole, settings: viewSettings }[page]();
  if (page === 'cosmetics') fillCosmetics();
  if (page === 'friends') loadFriends();
  if (page === 'home' && S.account) mountPlayer($('#mc3d'), S.account.name, 5.8);
}


// ---------- 3D player (CSS 3D, no WebGL): builds the Minecraft model from the account's skin ----------
const SKIN_URLS = (n) => ['https://mc-heads.net/skin/' + encodeURIComponent(n), 'https://minotar.net/skin/' + encodeURIComponent(n)];
const skinCache = {};
function loadSkin(name) {
  if (skinCache[name]) return skinCache[name];
  const tryAt = (i, resolve) => {
    const urls = SKIN_URLS(name);
    if (i >= urls.length) return resolve(null);
    const im = new Image();
    im.onload = () => resolve({ url: urls[i], h: im.naturalHeight >= 64 ? 64 : 32 });
    im.onerror = () => tryAt(i + 1, resolve);
    im.src = urls[i];
  };
  skinCache[name] = new Promise((res) => tryAt(0, res));
  return skinCache[name];
}
const SHADE = ['', 's-b', 's-s', 's-s', 's-t', 's-d'];
function mkBox(K, sk, w, h, d, u, v, pad) {
  const el = document.createElement('div'); el.className = 'part';
  const W = w + pad * 2, H = h + pad * 2, D = d + pad * 2;
  const faces = [
    [u + d, v + d, w, h, 'translateZ(' + (D * K / 2) + 'px)', W, H],
    [u + 2 * d + w, v + d, w, h, 'rotateY(180deg) translateZ(' + (D * K / 2) + 'px)', W, H],
    [u, v + d, d, h, 'rotateY(-90deg) translateZ(' + (W * K / 2) + 'px)', D, H],
    [u + d + w, v + d, d, h, 'rotateY(90deg) translateZ(' + (W * K / 2) + 'px)', D, H],
    [u + d, v, w, d, 'rotateX(90deg) translateZ(' + (H * K / 2) + 'px)', W, D],
    [u + d + w, v, w, d, 'rotateX(-90deg) translateZ(' + (H * K / 2) + 'px)', W, D]
  ];
  faces.forEach((f) => {
    const sx = f[0], sy = f[1], fw = f[2], fh = f[3], pw = f[5], ph = f[6];
    const e = document.createElement('div'); e.className = 'f ' + SHADE[faces.indexOf(f)];
    e.style.cssText = 'width:' + pw * K + 'px;height:' + ph * K + 'px;margin:' + (-ph * K / 2) + 'px 0 0 ' + (-pw * K / 2) + 'px;transform:' + f[4] +
      ';background-image:url("' + sk.url + '");background-size:' + (64 * K * pw / fw) + 'px ' + (sk.h * K * ph / fh) + 'px;background-position:' + (-sx * K * pw / fw) + 'px ' + (-sy * K * ph / fh) + 'px';
    el.appendChild(e);
  });
  return el;
}
async function mountPlayer(host, name, K) {
  if (!host) return;
  K = K || 5.2;
  const sk = await loadSkin(name);
  if (!sk || !host.isConnected) { if (host.isConnected) host.innerHTML = '<img class="logo" src="../assets/logo.png" alt="">'; return; }
  const modern = sk.h >= 64;
  host.innerHTML = '<div class="stage"></div>';
  const stage = host.firstChild;
  const put = (parent, x, y, kids) => { const g = document.createElement('div'); g.className = 'limb'; g.style.transform = 'translate3d(' + x * K + 'px,' + y * K + 'px,0)'; kids.forEach((c) => g.appendChild(c)); parent.appendChild(g); return g; };
  const part = (w, h, d, u, v, ov, ou, dy) => {   // a body part plus its overlay layer, shifted so the pivot is at the top
    const g = document.createElement('div'); g.className = 'part'; g.style.transform = 'translateY(' + (dy * K) + 'px)';
    g.appendChild(mkBox(K, sk, w, h, d, u, v, 0));
    if (ov) g.appendChild(mkBox(K, sk, w, h, d, ou[0], ou[1], 0.45));
    return g;
  };
  put(stage, 0, -10 + 0, [(() => { const g = part(8, 8, 8, 0, 0, true, [32, 0], 0); return g; })()]);                    // head
  put(stage, 0, 0, [part(8, 12, 4, 16, 16, modern, [16, 32], 0)]);                                                         // body
  const arm = (x, u, v, ou) => put(stage, x, -6, [part(4, 12, 4, u, v, modern, ou, 6)]);
  const leg = (x, u, v, ou) => put(stage, x, 6, [part(4, 12, 4, u, v, modern, ou, 6)]);
  const rArm = arm(-6, 40, 16, [40, 32]);
  const lArm = arm(6, modern ? 32 : 40, modern ? 48 : 16, [48, 48]);
  const rLeg = leg(-2, 0, 16, [0, 32]);
  const lLeg = leg(2, modern ? 16 : 0, modern ? 48 : 16, [0, 48]);
  let yaw = 25, drag = false, lx = 0, t0 = performance.now();
  host.onpointerdown = (e) => { drag = true; lx = e.clientX; host.setPointerCapture(e.pointerId); host.classList.add('grab'); };
  host.onpointermove = (e) => { if (drag) { yaw += (e.clientX - lx) * 0.8; lx = e.clientX; } };
  host.onpointerup = host.onpointercancel = () => { drag = false; host.classList.remove('grab'); };
  (function frame(now) {
    if (!host.isConnected) return;
    const t = (now - t0) / 1000;
    if (!drag) yaw += 0.35;
    stage.style.transform = 'rotateX(-12deg) rotateY(' + yaw + 'deg)';
    const sw = Math.sin(t * 1.6) * 7;
    rArm.style.transform = 'translate3d(' + (-6 * K) + 'px,' + (-6 * K) + 'px,0) rotateX(' + sw + 'deg)';
    lArm.style.transform = 'translate3d(' + (6 * K) + 'px,' + (-6 * K) + 'px,0) rotateX(' + (-sw) + 'deg)';
    rLeg.style.transform = 'translate3d(' + (-2 * K) + 'px,' + (6 * K) + 'px,0) rotateX(' + (-sw * 0.6) + 'deg)';
    lLeg.style.transform = 'translate3d(' + (2 * K) + 'px,' + (6 * K) + 'px,0) rotateX(' + (sw * 0.6) + 'deg)';
    requestAnimationFrame(frame);
  })(performance.now());
}

// ---------- YA Plus: detection, welcome + setup ----------
async function refreshPlus() {
  try {
    const r = await window.ya.plus();
    const was = plusOn();
    S.plus = !!(r && r.plus);
    if (was !== plusOn()) { renderNav(); render(); }
    if (plusOn() && !S.plusSetupDone) showOnboarding();
  } catch (_) { }
}
let obStep = 0, obPreset = 'pvp', obRam = 3072;
const PRESETS = {
  pvp: (m) => m.category === 'pvp' || m.category === 'utility',
  all: () => true,
  min: (m) => ['fps_boost', 'bg_fps', 'fps', 'cps', 'keystrokes', 'coords'].indexOf(m.id) >= 0
};
async function applyPreset() {
  const pick = PRESETS[obPreset] || PRESETS.pvp;
  for (const m of MODS) {
    if (m.soon || !(m.versions || []).some((sp) => specOk(sp, S.ya || '1.8.9'))) continue;
    const on = !!pick(m);
    MODSTATE[m.id] = on; await window.ya.modsSet(m.id, on);
  }
}
function showOnboarding(startAt) {
  obStep = startAt || 0; obPreset = 'pvp'; obRam = Math.max(S.ram || 2048, 3072);
  $('#ob').hidden = false; drawOb();
}
function drawOb() {
  const o = $('#ob');
  const dots = `<div class="dots">${[0, 1, 2].map((i) => `<i class="${i === obStep ? 'on' : ''}"></i>`).join('')}</div>`;
  const perks = ['perk_theme', 'perk_cape', 'perk_more', 'perk_setup'].map((k) => `<span class="perk">${ic('check')}${esc(t(k))}</span>`).join('');
  if (obStep === 0) {
    const row = (ico, k) => `<div class="frow"><span class="fic">${ic(ico)}</span><div><b>${esc(t(k + 't'))}</b><small>${esc(t(k + 'd'))}</small></div></div>`;
    o.innerHTML = `<div class="split"><div class="vis"><div class="mc3d big" id="obPlayer"></div><span class="plusbadge">PLUS</span></div>
      <div class="txt"><div class="eyebrow">${esc(t('wp_eyebrow2'))}</div><h1>${esc(t('wp_h'))}</h1><p>${esc(t('wp_sub'))}</p>
        <div class="frows">${row('plus', 'f1')}${row('cosmetics', 'f2')}${row('check', 'f3')}</div>
        <div class="acts"><button class="go" data-ob="next">${esc(t('cont'))}</button><button class="ghost" data-ob="skip">${esc(t('skip'))}</button></div>${dots}</div></div>`;
    if (S.account) mountPlayer($('#obPlayer'), S.account.name, 6.2);
  } else if (obStep === 1) {
    const op = (k) => `<button class="opt ${obPreset === k ? 'on' : ''}" data-preset="${k}"><b>${esc(t('pre_' + k))}</b><small>${esc(t('pre_' + k + '_d'))}</small></button>`;
    o.innerHTML = `<div class="in"><div class="eyebrow">&#9733; ${esc(t('wp_eyebrow'))}</div><h1 style="font-size:40px">${esc(t('st_title'))}</h1><p>${esc(t('st_sub'))}</p>
      <div class="lbl">${esc(t('lbl_preset'))}</div><div class="opts">${op('pvp')}${op('all')}${op('min')}</div>
      <div class="lbl">${esc(t('lbl_perf'))}</div>
      <div class="line"><div><b>${esc(t('ram_l'))}</b><small id="obRamV">${obRam} MB · ${esc(t('ram_d'))}</small></div><input type="range" id="obRam" min="1024" max="8192" step="256" value="${obRam}"></div>
      <div class="line"><div><b>${esc(t('minimize'))}</b></div><button class="sw ${S.minimizeOnLaunch ? 'on' : ''}" data-ob="min"></button></div>
      <button class="go" data-ob="next">${esc(t('cont'))}</button><button class="ghost" data-ob="back">${esc(t('back_b'))}</button>${dots}</div>`;
  } else {
    o.innerHTML = `<div class="in"><div class="eyebrow">&#9733; ${esc(t('wp_eyebrow'))}</div><h1 style="font-size:46px">${esc(t('done_t'))}</h1><p>${esc(t('done_s'))}</p>
      <button class="go" data-ob="finish">${esc(t('finish'))}</button>${dots}</div>`;
  }
}
async function finishOb() {
  S.ram = obRam; S.plusSetupDone = true;
  await window.ya.saveSettings({ ram: S.ram, plusSetupDone: true, minimizeOnLaunch: S.minimizeOnLaunch });
  await applyPreset();
  $('#ob').hidden = true; go('home');
}

function showLogin() {
  const m = $('#modal'); m.hidden = false;
  m.innerHTML = `<div class="dlg"><div class="mark">YA</div><button class="btn" id="lMs">${esc(t('microsoft'))}</button>
    <label style="color:var(--mute);font-size:12px">${esc(t('offlineL'))}</label><input type="text" id="lName" maxlength="16" style="width:100%">
    <button class="btn" id="lOff">${esc(t('offlineBtn'))}</button><div class="err" id="lErr"></div><button class="btn" id="lClose">${esc(t('cancel'))}</button></div>`;
  const done = (r) => {
    if (r && r.error) { $('#lErr').textContent = t(r.error); return; }
    S.account = r; m.hidden = true; renderNav(); render(); refreshPlus();
  };
  $('#lClose').onclick = () => (m.hidden = true);
  $('#lMs').onclick = async () => { $('#lErr').textContent = '...'; done(await window.ya.loginMicrosoft()); };
  $('#lOff').onclick = async () => done(await window.ya.loginOffline($('#lName').value));
}

async function launch(server) {
  if (busy) return;
  if (!S.account) { showLogin(); return; }
  busy = true; status = { text: '', pct: 2, show: true }; go('home');
  const r = await window.ya.play(server ? { server } : {});
  if (r && r.error) {
    busy = false; status = { text: r.error === 'java_missing' ? t('java_missing').replace(/\{n\}/g, r.detail) : t(r.error) + (r.detail ? ': ' + r.detail : ''), pct: 0, show: false };
    if (r.error === 'need_login') showLogin();
    if (page === 'home') render();
  }
}

let brandClicks = 0, brandT = 0;
document.addEventListener('click', (e) => {
  if (!e.target.closest('#brand')) return;
  const now = Date.now(); brandClicks = now - brandT < 900 ? brandClicks + 1 : 1; brandT = now;
  if (brandClicks >= 5) {   // hidden: preview the Plus look without a subscription (look only, perks stay server-side)
    brandClicks = 0; S.plusPreview = !S.plusPreview; window.ya.saveSettings({ plusPreview: S.plusPreview });
    renderNav(); render(); if (S.plusPreview && !S.plusSetupDone) showOnboarding();
  }
});
document.addEventListener('click', async (e) => {
  const el = e.target.closest('button, .nc, .ad');
  if (!el) return;
  if (el.dataset.w) return window.ya.win(el.dataset.w);
  if (el.dataset.ob) {
    const a = el.dataset.ob;
    if (a === 'next') { obStep++; return drawOb(); }
    if (a === 'back') { obStep--; return drawOb(); }
    if (a === 'skip') { S.plusSetupDone = true; window.ya.saveSettings({ plusSetupDone: true }); $('#ob').hidden = true; return; }
    if (a === 'min') { S.minimizeOnLaunch = !S.minimizeOnLaunch; el.classList.toggle('on', S.minimizeOnLaunch); return; }
    if (a === 'finish') return finishOb();
  }
  if (el.dataset.preset) { obPreset = el.dataset.preset; return drawOb(); }
  if (el.id === 'plusChip') return go('plus');
  if (el.id === 'frAdd') { const v = ($('#frName').value || '').trim(); if (v.length < 3) return; FR.ok = true; FR.msg = t('fr_sending'); $('#frMsg').textContent = FR.msg; $('#frMsg').className = 'frmsg ok'; return friendAct('add', v); }
  if (el.dataset.fa) return friendAct(el.dataset.fa, el.dataset.fu);
  if (el.id === 'accPill') return showLogin();
  if (el.id === 'rerun') return showOnboarding(0);
  if (el.id === 'buyPlus') { const u = C.storeUrl; if (u) window.ya.openExternal(u); return; }
  if (el.dataset.p) return go(el.dataset.p);
  if (el.id === 'btnAcc' || el.id === 'accBtn') return showLogin();
  if (el.id === 'play') return launch();
  if (el.dataset.srv != null) { const s = (C.servers || [])[+el.dataset.srv]; if (s) launch({ host: s.host, port: s.port || 25565 }); return; }
  if (el.dataset.url) return window.ya.openExternal(el.dataset.url);
  if (el.dataset.ver) {
    S.version = el.dataset.ver; window.ya.saveSettings({ version: S.version });
    const g = modsGrid(); $('#vlist').innerHTML = versionRows(); $('#mgrid').innerHTML = g.html;
    $('#vsub').textContent = g.ok + ' / ' + MODS.length + ' ' + t('avail') + (isYa(S.version) ? '' : ' · ' + t('vanillaNote') + ' ' + (S.ya || '1.8.9'));
    $('#view h1').textContent = t('modsFor') + ' ' + S.version; return;
  }
  if (el.dataset.vt) { vtab = el.dataset.vt; document.querySelectorAll('#vtabs .tab').forEach((b) => b.classList.toggle('on', b.dataset.vt === vtab)); $('#vlist').innerHTML = versionRows(); return; }
  if (el.dataset.mod) {
    const id = el.dataset.mod; const on = MODSTATE[id] === false; MODSTATE[id] = on;
    el.classList.toggle('on', on); return window.ya.modsSet(id, on);
  }
  if (el.dataset.ct) { cosTab = el.dataset.ct; return fillCosmetics(); }
  if (el.id === 'openStore') { const u = S._store || C.storeUrl; if (u) window.ya.openExternal(u); return; }
  if (el.id === 'sMin') { S.minimizeOnLaunch = !S.minimizeOnLaunch; el.classList.toggle('on', S.minimizeOnLaunch); return window.ya.saveSettings({ minimizeOnLaunch: S.minimizeOnLaunch }); }
  if (el.id === 'sFolder') return window.ya.openFolder();
  if (el.id === 'sDet') { const p = await window.ya.detectJava(); if (p) { $('#sJava').value = p; S.javaPath = p; window.ya.saveSettings({ javaPath: p }); } return; }
  if (el.id === 'sOut') { await window.ya.logout(); S.account = null; S.plus = false; window.ya.saveSettings({ plus: false }); renderNav(); render(); }
});
document.addEventListener('change', async (e) => {
  if (e.target.id === 'sLang') { S.lang = e.target.value; await window.ya.setLang(S.lang); renderNav(); render(); }
  if (e.target.id === 'sJava') { S.javaPath = e.target.value || 'java'; window.ya.saveSettings({ javaPath: S.javaPath }); }
  if (e.target.id === 'sRam') window.ya.saveSettings({ ram: S.ram });
});
document.addEventListener('keydown', (e) => { if (e.key === 'Enter' && e.target.id === 'frName') $('#frAdd').click(); });
document.addEventListener('input', (e) => {
  if (e.target.id === 'vq') { vq = e.target.value; $('#vlist').innerHTML = versionRows(); }
  if (e.target.id === 'obRam') { obRam = +e.target.value; $('#obRamV').textContent = obRam + ' MB · ' + t('ram_d'); }
  if (e.target.id === 'sRam') { S.ram = +e.target.value; $('#ramv').textContent = S.ram + ' MB'; }
});

window.ya.on('status', (d) => {
  const txt = t(d.k) + (d.k === 'crashed' ? '' : '');
  status = { text: d.done ? t(d.k) : txt, pct: d.pct || 0, show: !d.done };
  if (d.done) busy = false;
  if (page === 'home') {
    const bar = $('#bar'); if (bar) { bar.style.display = status.show ? 'block' : 'none'; $('#barI').style.width = status.pct + '%'; $('#stat').textContent = status.text; }
    const p = $('#play'); if (p) { p.disabled = busy; p.textContent = busy ? t('launching') : t('launch'); }
  }
});
window.ya.on('log', (m) => {
  logBuf.push(String(m)); if (logBuf.length > 400) logBuf.shift();
  const c = $('#con'); if (c) { c.textContent = logBuf.join('\n'); c.scrollTop = c.scrollHeight; }
});

(async function init() {
  S = { ...S, ...(await window.ya.getState()) };
  if (!I18N[S.lang]) S.lang = 'en';
  try { C = await window.ya.content(); } catch (_) { C = {}; }
  try { MODSTATE = await window.ya.modsGet(); } catch (_) { MODSTATE = {}; }
  try { MODS = (await window.ya.mods()).mods || []; } catch (_) { MODS = []; }
  try { VERS = await window.ya.versions(); } catch (_) { }
  renderNav(); render();
  refreshPlus();
  window.addEventListener('focus', () => { if (Date.now() - (S._pt || 0) > 60000) { S._pt = Date.now(); refreshPlus(); } });
})();
