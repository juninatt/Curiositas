# Curiositas

A public database of curious history, from antiquity to modern times. It starts with people and
grows to cover events, places and more. The data is structured so it can be shown in charts and
tables, and the project doubles as a showcase of API design and API testing.

## Tech stack

- Java 21, Spring Boot 4.1, Spring Data JPA
- PostgreSQL 18 with Flyway migrations
- Contract-first REST API (OpenAPI), errors as RFC 9457 Problem Details
- Tests: JUnit 5, AssertJ, Testcontainers
- Planned: REST Assured, contract validation against the OpenAPI spec

## Getting started

Requirements: JDK 21 and Docker (Docker Desktop on Windows/macOS). Docker must be running both
when starting the application and when running the tests.

```bash
./mvnw spring-boot:run
```

The application starts the PostgreSQL container in `compose.yaml` automatically.

Run all tests (Docker must be running, the tests use a real PostgreSQL container):

```bash
./mvnw verify
```

On Windows in PowerShell, use `.\mvnw.cmd` instead of `./mvnw`, for example
`.\mvnw.cmd spring-boot:run`.

## Design principles

- The schema grows step by step through Flyway migrations.
- Derived data such as age is never stored; it is calculated from stored dates.
- Every record has sources.
- Fixed fields for anything that is counted or grouped on; tags for everything else.

### Uncertain historical dates

Exact dates are often unknown for historical people. A date is stored as the earliest and latest
possible point in time, together with its precision (day, month, year, ...) and a qualifier
(exact, circa, before, after, between). Sorting and filtering use these bounds, and ages are
calculated as a range.

Years use astronomical numbering, where year 0 is 1 BC and year -43 is 44 BC.

**Known limitation:** dates are stored as the source gives them. Dates before 1582 are usually in
the Julian calendar and are not converted, which may shift them by up to about ten days.

## Development workflow

- Work is done directly on `main`, one feature at a time, in small commits that each build.
- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/) with a scope,
  for example `feat(person): add person listing`.
- Versions and the changelog are managed by
  [release-please](https://github.com/googleapis/release-please). It keeps a release pull request
  open; merging it creates a tag and a GitHub release.

See [docs/BACKLOG.md](docs/BACKLOG.md) for the roadmap.
