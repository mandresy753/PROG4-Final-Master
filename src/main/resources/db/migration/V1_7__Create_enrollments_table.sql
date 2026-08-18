create table if not exists enrollments
(
    id                uuid
        constraint enrollments_pk primary key,
    student_id        uuid not null
        constraint enrollments_student_fk references users,
    group_id          uuid not null
        constraint enrollments_group_fk references groups,
    academic_year_id  uuid not null
        constraint enrollments_academic_year_fk references academic_years,
    level             varchar not null,
    start_date        date not null,
    end_date          date
);
