-- Historical people. Dates are stored as year, optional month and day, and an uncertainty in
-- years, so a date is never more precise than its source. All three dates are either fully
-- absent (unknown) or have at least a year and an uncertainty.
create table person
(
    id                        uuid primary key,
    version                   bigint       not null,
    created_at                timestamptz  not null,
    updated_at                timestamptz  not null,

    wikidata_id               varchar(20)  unique,
    name                      varchar(200) not null,

    birth_year                integer,
    birth_month               integer,
    birth_day                 integer,
    birth_uncertainty_years   integer,

    death_year                integer,
    death_month               integer,
    death_day                 integer,
    death_uncertainty_years   integer,

    floruit_year              integer,
    floruit_month             integer,
    floruit_day               integer,
    floruit_uncertainty_years integer,

    birth_place               varchar(200),
    death_place               varchar(200),
    birth_country             varchar(2),
    gender                    varchar(20)  not null,

    constraint person_wikidata_id_format check (wikidata_id ~ '^Q[1-9][0-9]*$'),
    constraint person_birth_country_format check (birth_country ~ '^[A-Z]{2}$'),
    constraint person_gender_valid check (gender in ('FEMALE', 'MALE', 'OTHER', 'UNKNOWN')),

    constraint person_birth_valid check (
        (birth_year is null and birth_month is null and birth_day is null and birth_uncertainty_years is null)
        or (birth_year is not null and birth_uncertainty_years >= 0
            and (birth_month is null or birth_month between 1 and 12)
            and (birth_day is null or (birth_month is not null and birth_day between 1 and 31)))),
    constraint person_death_valid check (
        (death_year is null and death_month is null and death_day is null and death_uncertainty_years is null)
        or (death_year is not null and death_uncertainty_years >= 0
            and (death_month is null or death_month between 1 and 12)
            and (death_day is null or (death_month is not null and death_day between 1 and 31)))),
    constraint person_floruit_valid check (
        (floruit_year is null and floruit_month is null and floruit_day is null and floruit_uncertainty_years is null)
        or (floruit_year is not null and floruit_uncertainty_years >= 0
            and (floruit_month is null or floruit_month between 1 and 12)
            and (floruit_day is null or (floruit_month is not null and floruit_day between 1 and 31))))
);

-- Other names of a person, such as nicknames and regnal names, in the order they were given.
create table person_alternative_name
(
    person_id uuid         not null references person (id) on delete cascade,
    position  integer      not null,
    name      varchar(200) not null,
    type      varchar(30)  not null,

    primary key (person_id, position),
    constraint person_alternative_name_type_valid
        check (type in ('NICKNAME', 'PEN_NAME', 'REGNAL_NAME', 'BIRTH_NAME', 'SPELLING_VARIANT'))
);
