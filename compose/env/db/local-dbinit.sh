#!/usr/bin/env sh
set -eu

: "${POSTGRES_USER:?POSTGRES_USER must be set}"
: "${POSTGRES_PASS:?POSTGRES_PASS must be set}"
: "${DB_USER_APP:?DB_USER_APP must be set}"
: "${DB_PASS_APP:?DB_PASS_APP must be set}"
: "${DB_NAME_ADMIN:?DB_NAME_ADMIN must be set}"
: "${DB_SCHEMA_ADMIN:?DB_SCHEMA_APP must be set}"
: "${DB_USER_ADMIN:?DB_USER_ADMIN must be set}"
: "${DB_PASS_ADMIN:?DB_PASS_ADMIN must be set}"

# Optional but useful if your image provides these tools.
# Avoid echoing secrets to logs.

psql --username "${POSTGRES_USER:-postgres}" --dbname "${POSTGRES_DB:-postgres}" <<SQL
do \$\$
begin
    if not exists (select from pg_roles where rolname = '${DB_USER_APP}') then
        create role "${DB_USER_APP}" login password '${DB_PASS_APP}';
    else
        alter role "${DB_USER_APP}" with password '${DB_PASS_APP}';
    end if;
    if not exists (select from pg_roles where rolname = '${DB_USER_ADMIN}') then
        create role "${DB_USER_ADMIN}" login password '${DB_PASS_ADMIN}';
    else
        alter role "${DB_USER_ADMIN}" with password '${DB_PASS_ADMIN}';
    end if;
end
\$\$;

do \$\$
begin
    if not exists (select from pg_database where datname = '${DB_NAME_ADMIN}') then
        create database "${DB_NAME_ADMIN}" owner "${DB_USER_ADMIN}";
    end if;
end
\$\$;

do \$\$
begin
    grant connect on database "${DB_NAME_ADMIN}" to "${DB_USER_APP}";
    grant all privileges on database "${DB_NAME_ADMIN}" to "${POSTGRES_USER}";
end
\$\$;

\connect "${DB_NAME_ADMIN}";

do \$\$
begin
    create schema if not exists "${DB_SCHEMA_ADMIN}";
    alter database "${DB_NAME_ADMIN}" set search_path to "${DB_SCHEMA_ADMIN}";

    grant all privileges on schema "${DB_SCHEMA_ADMIN}" to "${DB_USER_ADMIN}";
    grant all privileges on schema "${DB_SCHEMA_ADMIN}" to "${POSTGRES_USER}";
    grant select on all tables in schema "${DB_SCHEMA_ADMIN}" to "${DB_USER_APP}";
    grant select on all views in schema "${DB_SCHEMA_ADMIN}" to "${DB_USER_APP}";

    alter default privileges in schema "${DB_SCHEMA_ADMIN}" grant all on tables to "${DB_USER_ADMIN}";
    alter default privileges in schema "${DB_SCHEMA_ADMIN}" grant select on tables to "${DB_USER_APP}";
    alter default privileges in schema "${DB_SCHEMA_ADMIN}" grant select on views to "${DB_USER_APP}";

    create extension if not exists "pgx_ulid" schema "${DB_SCHEMA_ADMIN}";
end
\$\$;
SQL
