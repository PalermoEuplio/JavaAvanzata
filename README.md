PROGETTO GUESS THE WORD
Corso di Java Programmazione Avanzata - A.A. 2025/2026
Dipartimento DIEM - Università degli Studi di Salerno

INTRODUZIONE
"Guess The Word" è un'applicazione multithreading Client-Server sviluppata in Java con interfaccia grafica JavaFX. 
Il gioco permette a due utenti (tramite il software Client) di sfidarsi contemporaneamente per individuare e decifrare una o più parole nascoste all'interno di un testo cifrato con il Cifrario di Cesare.
Il software Server ha il ruolo di "Game Master": gestisce un'interfaccia di amministrazione che consente di analizzare documenti testuali (memorizzando statistiche ed eliminando le stopword), configurare dinamicamente i parametri di difficoltà della partita, interagire con il database SQLite interno e coordinare, infine, la competizione in tempo reale tra i due player sfruttando una comunicazione asincrona tramite Socket.


ISTRUZIONI PER IL TESTING
Per testare il comportamento del sistema, è possibile utilizzare i seguenti account preconfigurati all'interno del database:

Account Amministratore (Lato Server)
- Username: Giacomo
- Password: Poretti
- Descrizione: Account per accedere alla dashboard amministrativa

Account Player 1 (Lato Client)
- Username: Luigi
- Password: luigirossi00
- Descrizione: Account utente per la sfida

Account Player 2 (Lato Client)
- Username: Rosalinda
- Password: Rosa2026
- Descrizione: Account utente per la sfida
