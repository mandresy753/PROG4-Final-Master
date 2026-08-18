create table if not exists academic_years
(
    id         uuid
        constraint academic_years_pk primary key,
    label      varchar not null
        constraint academic_years_label_uk unique,
    start_date date    not null,
    end_date   date    not null
);
