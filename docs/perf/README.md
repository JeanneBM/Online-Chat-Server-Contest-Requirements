# Artefakty wydajnościowe (P1)

Ten katalog przechowuje **twarde raporty** potwierdzające wymagania niefunkcjonalne z `README.txt`:
- 300 jednoczesnych użytkowników,
- czas odpowiedzi / dostarczenia wiadomości <= 3s,
- aktualizacja presence < 2s,
- historia >= 10k wiadomości.

## Jak generować raport

1. Uruchom środowisko:

```bash
docker compose up -d --build
```

2. Uruchom skrypt zbierający metryki:

```bash
./scripts/perf/run-k6-and-report.sh
```

3. Raport Markdown i JSON pojawią się w `docs/perf/results/`.

## Minimalny zestaw artefaktów do publikacji

- `k6-summary.json` (surowe metryki),
- `non-functional-report-YYYY-MM-DD.md` (czytelny raport),
- opcjonalnie logi i screeny z pomiaru realtime (delivery/presence).
