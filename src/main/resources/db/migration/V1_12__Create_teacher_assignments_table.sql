create table if not exists teacher_assignments
(
    id                  uuid
        constraint teacher_assignments_pk primary key,
    course_offering_id  uuid not null
        constraint teacher_assignments_course_offering_fk references course_offerings,
    teacher_id          uuid not null
        constraint teacher_assignments_teacher_fk references users,
    constraint teacher_assignments_offering_teacher_uk unique (course_offering_id, teacher_id)
);
