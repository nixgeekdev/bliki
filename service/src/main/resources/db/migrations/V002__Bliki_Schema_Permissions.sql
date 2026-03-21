set lock_timeout = '5s';

-------------------------------------------------------------------------------
-- SET TABLE OWNER -> bliki_admin
do
$$
    declare
        r record;
    begin
        for r in
            select n.nspname, c.relname
            from pg_class c
            join pg_namespace n on n.oid = c.relnamespace
            where n.nspname = 'bliki'
            and c.relkind in ('r', 'p', 'v') -- tables + partitioned tables + views
        loop
            execute format('alter table %I.%I owner to %I', r.nspname, r.relname, 'bliki_admin');
        end loop;
    end
$$;

-------------------------------------------------------------------------------
-- Grant read-only access to the schema itself
grant usage on schema bliki to bliki_app;
grant usage on schema bliki to bliki_admin;

-------------------------------------------------------------------------------
-- Grant ALL access to ALL existing tables in the schema TO bliki_admin
grant all on all tables in schema bliki to bliki_admin;

-------------------------------------------------------------------------------
-- Grant READ access to ALL existing tables + views in the schema TO bliki_app
grant select on all tables in schema bliki to bliki_app;
grant select on all views in schema bliki to bliki_app;

-------------------------------------------------------------------------------
-- Revoke access to the `identity` table from bliki_app
revoke all privileges on table "identity" from bliki_app;

-------------------------------------------------------------------------------
-- Make future tables readable as well
alter default privileges in schema bliki grant all on tables to bliki_admin;
alter default privileges in schema bliki grant select on tables to bliki_app;
