# Backlog

The plan is built feature by feature. Each step adds its own Flyway migration.

## Milestone 1 (MVP)

- [x] Project skeleton: Maven, Spring Boot, PostgreSQL via Docker Compose, Flyway, CI, release-please
- [ ] `HistoricalDate` value type with thorough unit tests
- [ ] Person and Tag: CRUD, validation, Problem Details, authentication for write operations
- [ ] Listing with filters, sorting and pagination
- [ ] One statistics endpoint
- [ ] REST Assured, Testcontainers and contract validation in CI
- [ ] Data export and import as JSON files in `data/`, one file per person, committed to git as
  backup and source of truth (export format = import format, verified by a round-trip test)

### Person fields in the MVP

- `id`, `wikidataId` (optional, unique)
- `name`, `alsoKnownAs` (typed: nickname, pen name, regnal name, birth name, spelling variant)
- `birth`, `death`, `floruit` (all `HistoricalDate`)
- `birthPlace`, `deathPlace` (free text until places exist)
- `birthCountry` (ISO 3166-1 alpha-2), `gender`
- `summary`, `whyInteresting`
- `causeOfDeath` (fixed list plus free text detail)
- `historicity` (historical, probable, legendary)
- `tags`, `sources` (at least one)
- `version`, `createdAt`, `updatedAt`

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

- [ ] Places with coordinates
- [ ] Bulk import of CSV with per-row error reporting
- [ ] Black-box API test suite (Postman/Newman or Bruno)
- [ ] Allure reports, mutation testing (PIT), load testing (Gatling)

## TODO

- Family and private life, for example children who died young. Probably modelled as relations
  to lightweight person records rather than as fields on the person.
