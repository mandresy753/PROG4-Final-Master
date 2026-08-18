create table if not exists exam_session_groups
(
    exam_session_id uuid not null
        constraint esg_session_fk references exam_sessions,
    group_id        uuid not null
        constraint esg_group_fk references groups,
    constraint esg_pk primary key (exam_session_id, group_id)
);
