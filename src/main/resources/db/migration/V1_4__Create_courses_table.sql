create table if not exists courses
(
    id           uuid
        constraint courses_pk primary key,
    ref          varchar not null
        constraint courses_ref_uk unique,
    title        varchar not null,
    credit_count integer not null,
    track        varchar not null,
    semester     varchar not null
);
