create table if not exists course_offering_groups
(
    course_offering_id uuid not null
        constraint cog_offering_fk references course_offerings,
    group_id            uuid not null
        constraint cog_group_fk references groups,
    constraint cog_pk primary key (course_offering_id, group_id)
);
