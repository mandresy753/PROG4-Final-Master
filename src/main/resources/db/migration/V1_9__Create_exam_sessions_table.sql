create table if not exists exam_sessions
(
    id         uuid
        constraint exam_sessions_pk primary key,
    exam_id    uuid      not null
        constraint exam_sessions_exam_fk references exams,
    exam_date  timestamp not null,
    teacher_id uuid
        constraint exam_sessions_teacher_fk references users
);
