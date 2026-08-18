create table if not exists course_offerings
(
    id                uuid
        constraint course_offerings_pk primary key,
    course_id         uuid not null
        constraint course_offerings_course_fk references courses,
    academic_year_id  uuid not null
        constraint course_offerings_academic_year_fk references academic_years
);
