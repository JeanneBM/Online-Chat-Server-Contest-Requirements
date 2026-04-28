# Weryfikacja zgodności z wymaganiami z README

Data przeglądu: 2026-04-28

Legenda:
- ✅ Spełnione
- 🟡 Częściowo spełnione
- ❌ Niespełnione / brak dowodu implementacji

## 1) Podsumowanie ogólne

Repozytorium **w dużej części spełnia wymagania backendowe** (auth, pokoje, zaproszenia, wiadomości, limity załączników, podstawowa moderacja), ale ma istotne luki względem pełnej specyfikacji konkursowej:

1. Brak twardych artefaktów potwierdzających wymagania niefunkcjonalne (wyniki benchmarków zamiast samych scenariuszy).  
2. Obsługa presence/multi-tab jest zaimplementowana, ale w dwóch równoległych mechanizmach (`PresenceService` i `SessionService`), co ryzykuje niespójności statusów.  
3. Frontend (`index.html`) wygląda jak MVP i nie pokrywa części wymaganej nawigacji/administracji UX (np. pełen workflow sesji, zarządzanie banami/prywatnymi zaproszeniami w wygodnym UI).

## 2) Ocena wymagań funkcjonalnych

## 2.1 Konta i autoryzacja

- Rejestracja email+username+password: ✅
- Unikalny email i username: ✅
- Username niezmienny: ✅ (brak endpointu zmiany username)
- Logowanie email+hasło: ✅
- Wylogowanie tylko bieżącej sesji: ✅
- Trwałość logowania po zamknięciu przeglądarki: ✅ (token w localStorage)
- Zmiana hasła: ✅
- Reset hasła: 🟡 (token zwracany bezpośrednio w odpowiedzi API; technicznie działa, ale uproszczone)
- Hasło hashowane: ✅ (PasswordEncoder)
- Usunięcie konta + czyszczenie pokojów właściciela i membership: ✅

## 2.2 Presence i sesje

- Statusy online/AFK/offline: ✅
- AFK po 1 minucie bez aktywności: ✅
- Multi-tab (online gdy aktywna min. 1 zakładka): 🟡 (logika jest, ale zdublowana i podatna na race conditions)
- Lista aktywnych sesji + wylogowanie wybranej: ✅

## 2.3 Znajomi

- Lista znajomych: ✅
- Zaproszenie po username: ✅
- Zaproszenie z listy userów pokoju: 🟡 (backend umożliwia po username; brak dedykowanego endpointu „from room user list”)
- Akceptacja znajomości: ✅
- Usuwanie znajomego: ✅
- User-to-user ban: ✅
- Efekt bana (blokada kontaktu/PM, relacja znajomych zrywana): ✅
- PM tylko gdy friends i brak bana: ✅
- Możliwość cofnięcia user-to-user bana: ✅

## 2.4 Pokoje

- Tworzenie pokoju: ✅
- Właściwości pokoju (name/description/visibility/owner/admins/members/bans): ✅
- Unikalna nazwa pokoju: ✅
- Katalog publicznych + search + licznik członków: ✅
- Prywatne pokoje tylko przez zaproszenie: ✅
- Join/leave public: ✅
- Owner nie może opuścić własnego pokoju: ✅
- Usunięcie pokoju czyści wiadomości/pliki: ✅
- Role admin/owner i operacje moderacyjne: ✅
- Usunięcie członka traktowane jak ban: ✅
- Zaproszenia do prywatnych: ✅

## 2.5 Wiadomości

- Tekst/plain/multiline/emoji: ✅ (po stronie backend ograniczenie długości, brak blokad formatu)
- Załączniki w wiadomości: ✅
- Odpowiedź/referencja do innej wiadomości: ✅
- Limit 3 KB: ✅
- Trwałość i chronologia: ✅

## 2.6 Załączniki

- Obrazy + dowolne pliki: ✅
- Lokalny storage: ✅
- Limit plik 20 MB: ✅
- Limit obraz 3 MB: ✅

## 2.7 Powiadomienia

- Wskaźnik nieprzeczytanych: ✅ (backend unread-count + badge w UI)

## 3) Wymagania niefunkcjonalne

- 300 jednoczesnych użytkowników: 🟡 (jest skrypt k6 i opis, brak wyniku/raportu z uruchomienia)
- Dostarczenie wiadomości <= 3 s: 🟡 (opis testu ręcznego, brak twardych metryk)
- Aktualizacja presence < 2 s: 🟡 (ustawienia i opis, brak benchmarku)
- Historia >= 10 000 wiadomości: 🟡 (opis scenariusza, brak dowodu wykonania)

## 4) Wymagania UI

- Podstawowy layout top menu / panele / message area / input: 🟡 (jest prosty layout, ale UX jest mocno techniczny/MVP)

## 5) Lista zmian, które pozostały

Priorytet P1 (krytyczne wobec zgodności konkursowej):
1. Ujednolicić logikę presence/multi-tab do jednego źródła prawdy (usunąć duplikację między `PresenceService` i `SessionService`).
2. Dodać i publikować twarde raporty niefunkcjonalne (czas dostarczenia wiadomości, update presence, 300 users).

Priorytet P2 (dowody i jakość):
3. Dodać rzeczywiste artefakty niefunkcjonalne (wyniki k6, metryki p95, timestampy opóźnień, potwierdzenie 10k historii).
4. Dopisać testy integracyjne dla: usunięcia konta, moderacji room, zasad PM friends+ban, limitów 3KB/3MB/20MB.
5. Dodać walidacje i bardziej jednoznaczne kontrakty API (DTO zamiast `@RequestBody String username` w części endpointów).

Priorytet P3 (UX/UI):
6. Rozbudować UI o pełne workflow: sesje aktywne i ich zamykanie, zarządzanie zaproszeniami/badaniami banów/adminów, wygodne PM i room moderation.
7. Uzupełnić UI o czytelniejsze stany błędów i scenariusze edge-case (np. próba pisania po banie, read-only historia PM po banie).
