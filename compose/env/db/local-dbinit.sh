#!/usr/bin/env bash
set -e

# bliki
psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" <<- EOSQL
    create database bliki;
    create role bliki_app with login encrypted password 'FbZ)D_MG5Xfkk%Sl' noinherit;
    create role bliki_admin with login encrypted password 'Z[u;&Im(^w^RGluX' noinherit;
    grant connect on database bliki to bliki_app;
    grant all privileges on database bliki to bliki_admin;
    grant all privileges on database bliki to $POSTGRES_USER;

    \connect "bliki";

    create schema if not exists bliki;
    alter database bliki set search_path to bliki;
    grant select, insert, update on all tables in schema bliki to bliki_app;
    grant all privileges on schema bliki to bliki_admin;
    grant all privileges on schema bliki to $POSTGRES_USER;
    create extension if not exists "pgx_ulid" schema bliki;
EOSQL
