-- Une note est liée à une exam_session précise (la séance que l'étudiant a
-- réellement passée), pas directement à l'Exam : ça évite toute ambiguïté
-- quand un même Exam a plusieurs séances (groupes/profs différents).
create table if not exists grades
(
    id               uuid
        constraint grades_pk primary key,
    exam_session_id  uuid          not null
        constraint grades_exam_session_fk references exam_sessions,
    student_id       uuid          not null
        constraint grades_student_fk references users,
    value            numeric(4, 2) not null,
    entry_date       timestamp     not null,
    entered_by_id    uuid          not null
        constraint grades_entered_by_fk references users,
    reason           varchar
);
