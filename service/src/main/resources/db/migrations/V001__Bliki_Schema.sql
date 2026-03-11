-- Bliki schema (PostgreSQL) : https://martinfowler.com/bliki/WhatIsaBliki.html
-- Goal: model the major Bliki elements closely: bliki, entry, tag, profile, identity, roles, revision,
set lock_timeout = '5s';

create extension if not exists "pgx_ulid" schema bliki;
create extension if not exists pgcrypto schema bliki;

do
$$
    begin
        if not exists (select 1 from pg_type where typname = 'entry_content_type') then
            create type entry_content_type as enum ('text/plain', 'text/markdown');
        end if;
        if not exists (select 1 from pg_type where typname = 'identity_role') then
            create type identity_role as enum (
                'ROLE_ADMIN',
                'ROLE_AUTHOR',
                'ROLE_EDITOR',
                'ROLE_CONTRIBUTOR'
                );
        end if;
        if not exists (select 1 from pg_type where typname = 'entry_status') then
            create type entry_status as enum ('DRAFT', 'PUBLISHED');
        end if;
        if not exists (select 1 from pg_type where typname = 'entry_visibility') then
            create type entry_visibility as enum ('PUBLIC', 'PRIVATE');
        end if;
        -- events for revision history
        if not exists (select 1 from pg_type where typname = 'entry_event') then
            create type entry_event as enum ('CREATED', 'UPDATED', 'DELETED', 'PUBLISHED');
        end if;
        -- Graph Relationships (Wiki Layer)
        if not exists (select 1 from pg_type where typname = 'entry_relation_type') then
            create type entry_relation_type as enum ('RELATED', 'REPLACES', 'IS_UPDATED_BY', 'IS_UPDATED_FROM');
        end if;
        if not exists (select 1 from pg_type where typname = 'tag_scheme') then
            create type tag_scheme as enum ('ROOT', 'BRANCH', 'LEAF');
            -- ROOT: no tags above it, no parent
            -- BRANCH: tags above it; tags below it
            -- LEAF: tags above it, no tags below it
            --
            -- ROOT: /databases
            -- LEAF: /databases/mariadb
            -- BRANCH: /databases/postgresql
            -- LEAF: /databases/postgresql/install
        end if;
    end;
$$;

create table if not exists "identity"
(
    id            ulid primary key   default gen_monotonic_ulid(),
    email         text      not null,
    password_hash text      not null,
    created_at    timestamp not null default now(),
    updated_at    timestamp not null default now()
);

create table if not exists roles
(
    id         ulid primary key       default gen_monotonic_ulid(),
    "role"     identity_role not null,
    label      text          null,
    created_at timestamp     not null default now(),
    updated_at timestamp     not null default now()
);

create table if not exists profile
(
    id          ulid primary key   default gen_monotonic_ulid(),
    identity_id ulid      not null references identity on delete restrict,
    full_name   text      not null,
    affiliation text      null, -- company or institution
    created_at  timestamp not null default now(),
    updated_at  timestamp not null default now()
);

create table if not exists generator
(
    id         ulid primary key   default gen_monotonic_ulid(),
    name       text      not null,
    version    text      not null,
    uri        text      null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create table if not exists bliki
(
    id           ulid primary key   default gen_monotonic_ulid(),
    title        text      not null,
    subtitle     text      null,
    rights       text      not null,
    base_uri     text      not null,
    icon_uri     text      null,
    logo_uri     text      null,
    lang         text      not null,
    author_id    ulid      not null references profile on delete restrict,
    generator_id ulid      not null references generator on delete restrict,
    updated_at   timestamp not null default now()
);

create table if not exists entry
(
    id           ulid primary key            default gen_monotonic_ulid(),
    bliki_id     ulid               not null references bliki on delete restrict,
    title        text               not null,
    slug         varchar(128)       not null unique,
    content      text               not null,
    summary      text               null,
    lang         text               not null,
    content_type entry_content_type not null,
    author_id    ulid               not null references profile on delete restrict,
    visibility   entry_visibility   not null default 'PRIVATE',
    status       entry_status       not null default 'DRAFT',
    published_at timestamp          null,
    created_at   timestamp          not null default now(),
    updated_at   timestamp          not null default now()
);

create table if not exists tag
(
    id         ulid primary key    default gen_monotonic_ulid(),
    parent_id  ulid        null     references tag on delete set null,
    term       text        not null,
    slug       varchar(32) not null unique,
    label      text        null,
    scheme     tag_scheme  null,
    created_at timestamp   not null default now(),
    updated_at timestamp   not null default now()
);

create table if not exists revision
(
    id         ulid primary key     default gen_monotonic_ulid(),
    entry_id   ulid        not null references entry on delete restrict,
    author_id  ulid        not null references profile on delete restrict,
    diff       text        not null,
    summary    text        null,
    event      entry_event not null default 'CREATED',
    created_at timestamp   not null default now()
);

create table if not exists entry_relation
(
    from_entry_id ulid                not null references entry on delete restrict,
    to_entry_id   ulid                not null references entry on delete restrict,
    relation      entry_relation_type not null default 'RELATED',
    primary key (from_entry_id, to_entry_id)
);

create table if not exists entry_tag
(
    entry_id ulid not null references entry on delete restrict,
    tag_id   ulid not null references tag on delete restrict,
    primary key (entry_id, tag_id)
);

create table if not exists entry_contributor
(
    entry_id   ulid not null references entry on delete restrict,
    profile_id ulid not null references profile on delete restrict,
    primary key (entry_id, profile_id)
);

create table if not exists identity_roles
(
    identity_id ulid not null references identity on delete restrict,
    role_id     ulid not null references roles on delete restrict,
    primary key (identity_id, role_id)
);

-- constraints
alter table entry
    add constraint chk_entry_slug_not_empty
        check (length(slug) > 0 and length(slug) <= 128);

alter table tag
    add constraint chk_tag_slug_not_empty
        check (length(slug) > 0 and length(slug) <= 32);

alter table tag
    add constraint chk_tag_no_self_reference
        check (parent_id != id);

alter table entry_relation
    add constraint chk_entry_relation_no_self_reference
        check (from_entry_id != to_entry_id);

alter table "identity"
    add constraint chk_identity_email_not_empty
        check (length(email) > 0);

alter table "identity"
    add constraint chk_identity_password_hash_format
        check (password_hash like '$argon2%');

-- PostgreSQL Row-Level Security (RLS)
-- Enable Row-Level Security
alter table "identity" enable row level security;

create function get_current_identity_id()
    returns ulid
    language sql stable as
$$
    select current_setting('app.current_identity_id', true)::ulid
$$;

-- revoke all on function set_config(text, text, boolean) from bliki_app;
create function set_identity_id(ulid)
    returns void
    language sql security definer as
$$
    select set_config('app.current_identity_id', $1::text, true)
$$;

-- policies
create policy identity_user_read_policy on "identity"
    for select to bliki_app
        using (id = get_current_identity_id());

create policy identity_user_save_policy on "identity"
    for update to bliki_app
        using (id = get_current_identity_id())
        with check (id = get_current_identity_id());

create policy identity_user_delete_policy on "identity"
    for delete to bliki_app
        using (id = get_current_identity_id());

-- admin can see all rows and add any rows
create policy identity_admin_policy on "identity"
    for all to bliki_admin
        using (true)
        with check (true);

-- Full-Text Search
alter table entry
    add column search_vector tsvector
        generated always as (to_tsvector('english', title || ' ' || content)) stored;
create index if not exists idx_entry_search_vector on entry using gin (search_vector);
-- Usage:
-- select * from entry
--   where search_vector @@ plainto_tsquery('english', 'jvm memory')
-- order by published_at desc
-- limit 100;

-- indices
create index if not exists idx_bliki_updated on bliki (updated_at);

create index if not exists idx_entry_created_updated on entry (created_at, updated_at);
create index if not exists idx_entry_published_desc
    on entry (published_at desc) where published_at is not null;
create index if not exists idx_entry_slug on entry (slug);

create index if not exists idx_revision_created on revision (created_at);
create index if not exists idx_revision_entry_author_id on revision (entry_id, author_id);

create index if not exists idx_tag_term on tag (term);
create index if not exists idx_tag_term_slug on tag (slug);
create index if not exists idx_tag_created_updated on tag (created_at, updated_at);

create index if not exists idx_profile_created_updated on profile (created_at, updated_at);
create index if not exists idx_profile_full_name on profile (full_name);

create index if not exists idx_identity_email on identity (email);
create index if not exists idx_identity_created_updated on identity (created_at, updated_at);

create index if not exists idx_roles_created_updated on roles (created_at, updated_at);

-- views
create view identity_public as
select id, email, created_at, updated_at
from identity;

grant select on identity_public to bliki_app;
revoke select (password_hash) on identity from bliki_app;
