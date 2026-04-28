# Online Chat Server – rozwiązanie konkursowe

To repozytorium zawiera implementację serwera czatu webowego przygotowaną na podstawie wymagań z warsztatu.

> Pełna, oryginalna treść zadania znajduje się w pliku `README.txt`.

## Zakres projektu

Aplikacja realizuje klasyczny scenariusz komunikatora internetowego, obejmujący m.in.:

- rejestrację i logowanie użytkowników,
- pokoje publiczne i prywatne,
- wiadomości 1:1,
- listę znajomych i zaproszenia,
- obecność użytkowników (online/AFK/offline),
- historię wiadomości,
- udostępnianie plików,
- podstawowe funkcje moderacyjne.

## Wymagania techniczne

- Docker + Docker Compose
- Java 17+

## Szybki start

1. Sklonuj repozytorium.
2. Uruchom aplikację w katalogu głównym:

```bash
docker compose up --build
```

3. Otwórz przeglądarkę i przejdź do adresu aplikacji zdefiniowanego w `docker-compose.yml`.

## Struktura repozytorium

- `src/main/java` – kod aplikacji (Spring Boot),
- `src/main/resources` – konfiguracja i statyczny frontend,
- `src/test/java` – testy,
- `scripts/` – skrypty pomocnicze (np. smoke/perf),
- `docs/` – dokumentacja uzupełniająca i analiza wymagań.

## Testy i walidacja

Przykładowe komendy lokalne:

```bash
./mvnw test
./mvnw verify
```

## Dokumentacja wymagań

- `README.txt` – pełna treść zadania konkursowego,
- `docs/requirements-gap-analysis.md` – mapa pokrycia wymagań,
- `docs/non-functional-evidence.md` – dowody spełnienia wymagań niefunkcjonalnych,
- `docs/perf/README.md` – informacje o testach wydajnościowych.
