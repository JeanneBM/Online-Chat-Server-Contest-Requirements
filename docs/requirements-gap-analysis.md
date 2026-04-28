# Weryfikacja zgodności z wymaganiami z README

Data przeglądu: 2026-04-28

Legenda:
- ✅ Spełnione
- 🟡 Częściowo spełnione / wymaga doprecyzowania
- ❌ Niespełnione

## 1) Ocena ogólna

Repozytorium spełnia większość wymagań funkcjonalnych backendu (auth, pokoje, wiadomości, załączniki, moderacja, zaproszenia, unread), ale nie dostarcza jeszcze pełnych **dowodów konkursowych** dla wymagań niefunkcjonalnych i ma kilka luk jakościowych/UI.

Szacunkowa zgodność (na podstawie przeglądu kodu i artefaktów):
- Funkcjonalne: ~85-90%
- Niefunkcjonalne (z dowodami): ~40-50%
- UI: ~60-70%

## 2) Wymagania funkcjonalne — status

## 2.1 Konta i autoryzacja

- Rejestracja email + username + hasło: ✅
- Unikalny email i username: ✅
- Username niezmienny po rejestracji: ✅
- Logowanie email/hasło: ✅
- Wylogowanie bieżącej sesji: ✅
- Persistent login (token): ✅
- Zmiana hasła: ✅
- Reset hasła: 🟡 (działa, ale token resetu jest zwracany w odpowiedzi API — rozwiązanie developerskie, nieprodukcyjne)
- Haszowanie haseł: ✅
- Usunięcie konta + usuwanie pokojów ownera: ✅

## 2.2 Presence i sesje

- Statusy ONLINE / AFK / OFFLINE: ✅
- AFK po 1 minucie: ✅
- Lista aktywnych sesji + wylogowanie wybranej: ✅
- Multi-tab: ✅ (jedna logika oparta o `SessionService` i `ActiveSession`; heartbeat dotyka sesji JWT przez `authSessionId`)

## 2.3 Znajomi i PM

- Lista znajomych: ✅
- Zaproszenie po username + opcjonalna wiadomość: ✅
- Akceptacja/odrzucenie zaproszenia: ✅
- Usuwanie znajomego: ✅
- User-to-user ban i unban: ✅
- PM tylko między znajomymi bez banów: ✅
- „From room user list”: 🟡 (backend nie ma dedykowanego endpointu pod ten scenariusz, ale może być zrealizowany po samym username z listy użytkowników pokoju)

## 2.4 Pokoje

- Tworzenie pokoju i unikalność nazwy: ✅
- Public/private, katalog publicznych, search: ✅
- Join public, leave room, owner nie może opuścić: ✅
- Prywatne pokoje tylko przez zaproszenie: ✅
- Usunięcie pokoju czyści wiadomości i pliki: ✅
- Moderacja (ban/unban/remove member/delete message/lista banów + kto zbanował): ✅
- Usunięcie członka jako ban: ✅

## 2.5 Wiadomości i załączniki

- Tekst/multiline/emoji + reply/reference: ✅
- Limit 3 KB: ✅
- Chronologia i trwałość historii: ✅
- Załączniki (obrazy i dowolne pliki), local storage: ✅
- Limity 20 MB (plik) i 3 MB (obraz): ✅

## 2.7 Powiadomienia

- Unread indicators w UI: ✅

## 3) Niefunkcjonalne — status

- 300 jednoczesnych użytkowników: 🟡 (jest skrypt k6, brak zapisanego wyniku uruchomienia)
- Czas dostarczenia wiadomości <= 3 s: 🟡 (opis procedury testowej, brak raportu z pomiaru)
- Presence update < 2 s: 🟡 (konfiguracja wskazuje, ale brak twardego benchmarku)
- Historia >= 10 000 wiadomości: 🟡 (opis scenariusza, brak artefaktu potwierdzającego wykonanie)

## 4) UI — status

- Podstawowy layout (top/menu, panele, input, message area): ✅
- Kompletność UX pod scenariusze konkursowe: 🟡 (interfejs wygląda jak MVP; część przepływów administracyjnych/sesyjnych nie jest domknięta UX-owo)

## 5) Lista zmian, które pozostały

## P1 — krytyczne dla „pełnej zgodności konkursowej”

1. ~~**Ujednolicić presence/multi-tab do jednego źródła prawdy**~~ ✅ (zaimplementowane: jedna logika presence oparta o `ActiveSession` + heartbeat websocket przypięty do `sessionId`).
2. ~~**Dodać do repo artefakty wyników**~~ ✅ (dodany katalog `docs/perf/` i skrypt automatyzujący zbieranie raportów).
3. **Wykonać i opublikować twarde raporty niefunkcjonalne** na uruchomionym środowisku: wynik k6 (300 VU), pomiar opóźnień dostarczania wiadomości, pomiar opóźnień presence, dowód 10k historii.

## P2 — jakość i kontrakty API

4. **Utwardzić flow resetu hasła**: nie zwracać tokenu w publicznym API; wysyłka out-of-band (mail/symulacja maila) + TTL + ewentualny rate limit.
5. **Ujednolicić endpointy „delete account”** (obecnie są dwa różne endpointy) i opisać jeden canonical flow.
6. **Dodać testy integracyjne** dla krytycznych reguł: delete-account cleanup, room moderation, PM friends+ban, limity 3KB/3MB/20MB.

## P3 — UX/UI

7. Rozbudować UI o pełny workflow zarządzania sesjami, banami i zaproszeniami room.
8. Dodać czytelniejsze stany błędów i scenariusze edge-case (np. PM po banie, read-only historia przy banie).
9. Dodać krótką checklistę „jak zademonstrować zgodność” (demo script) na potrzeby oceny konkursowej.
