create table if not exists exams
(
    id                  uuid
        constraint exams_pk primary key,
    course_offering_id  uuid          not null
        constraint exams_course_offering_fk references course_offerings,
    coefficient         numeric(4, 3) not null
);
