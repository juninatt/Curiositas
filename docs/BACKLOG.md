# Backlog

The plan is built feature by feature. Each step adds its own Flyway migration.

## Milestone 1 (MVP)

- [x] Project skeleton: Maven, Spring Boot, PostgreSQL via Docker Compose, Flyway, CI, release-please
- [x] `HistoricalDate` value type with display text and age range, with thorough unit tests
- [x] OpenAPI contract with generated interfaces and models
- [x] Person: create, read, replace and delete, with validation, Problem Details, authentication for
  writes, ETag with If-Match, and duplicate detection
- [x] Swagger UI and a read-only preview page
- [x] JSON files in `data/` as the source of truth, one file per person: export, import, a
  round-trip test, writing the files with every change, and refusing to start when the database
  and the files differ
- [ ] REST Assured, Testcontainers and contract validation of every response in CI
- [ ] Load Mockito as a Java agent in the test build; it currently attaches itself at runtime,
  which future JDK versions will block
- [ ] Listing with filters and sorting
- [ ] One statistics endpoint

### Before version 1.0

Version 1.0 promises that the API will not change in ways that break existing clients. Adding
optional fields, endpoints and query parameters is safe afterwards; making a field required,
renaming, removing or changing the meaning of one is not. Before 1.0:

- [x] JSON backup in `data/`, so no data depends on a single database
- [ ] REST Assured and contract validation, so the API is proven to match its contract
- [ ] The person fields have the shape they are meant to keep

### Person fields in the first version

- `id`, `wikidataId` (optional, unique)
- `name`, `alsoKnownAs` (typed: nickname, pen name, regnal name, birth name, spelling variant)
- `birth`, `death`, `floruit` (all `HistoricalDate`: year, optional month and day, uncertainty in
  years)
- `birthPlace`, `deathPlace` (free text until places exist)
- `birthCountry` (ISO 3166-1 alpha-2 code, shown as the full country name), `gender`
- `version`, `createdAt`, `updatedAt`

### Person fields added later

All optional, so they can be added without breaking existing clients.

- [ ] `sources` (URL, title, date accessed)
- [ ] `summary`, `whyInteresting`
- [ ] `historicity` (historical, probable, legendary), `HISTORICAL` when not given
- [ ] `causeOfDeath` (fixed list plus free text detail)
- [ ] Tags

## Milestone 2

- [ ] Polities (historical states and realms, optionally mapped to a modern country)
- [ ] Nationalities
- [ ] Residence periods (where a person lived, and when)
- [ ] Titles (ranks and offices) and occupations, each with an optional period
- [ ] Native languages (ISO 639-3, includes historical languages)
- [ ] Religion as periods, since it can change during a life
- [ ] Social origin and peak social status

## Milestone 3

- [ ] Events (title, start, end, type, sources)
- [ ] Participation: person and event, with type (participated, caused, influenced, affected by)
- [ ] Relations between people (teacher, rival, spouse, killed by, ...)

## Later

- [ ] Places with coordinates, in a hierarchy (Bristol, England, United Kingdom, Europe), so an
  uncertain birthplace can be given at the level the sources support
- [ ] Which polity ruled a place over time, so a birthplace can be shown both as its modern country
  and as the realm it belonged to then (Königsberg: Prussia in 1724, Russia today)
- [ ] Separate frontend (for example React) replacing the preview page, with CORS configured for it
- [ ] Public deployment with HTTPS, building its database from `data/` on every deployment
- [ ] Fill in facts from Wikidata by a person's Wikidata id, for review before they are stored
- [ ] Bulk import of CSV with per-row error reporting
- [ ] Black-box API test suite (Postman/Newman or Bruno)
- [ ] Allure reports, mutation testing (PIT), load testing (Gatling)

## TODO

- Family and private life, for example children who died young. Probably modelled as relations
  to lightweight person records rather than as fields on the person.
