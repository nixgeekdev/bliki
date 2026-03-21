# Locally Composing

This directory contains Docker Compose configuration for running the application locally with all required services.

## Containers

The `docker-compose.yaml` defines the following containers:

### Database (PostgreSQL)

A PostgreSQL database container with the `pgx_ulid` extension pre-installed. This container:

- Uses a custom PostgreSQL image that includes the ULID extension
- Runs initialization scripts on startup to configure databases, schemas, users, and permissions
- Exposes port `${DB_PORT}` (typically 5432) for database connections
- Persists data using a named volume

### Cache (Valkey)

A Valkey cache container for high-performance key-value storage. This container:

- Uses the official Valkey Alpine image (version 9.0.3)
- Configured with append-only persistence (AOF) enabled
- Uses 4 I/O threads for improved performance with read operations
- Exposes port `${VALKEY_PORT}` for cache connections
- Password-protected with `requirepass` authentication
- Disables dangerous commands (CONFIG, FLUSHALL, FLUSHDB) for security
- Persists data using a named volume
- Includes health checks using `valkey-cli ping`

## Database Image with ULID Extension

The PostgreSQL database is configured to support ULID (Universally Unique Lexicographically Sortable Identifier) through
the `pgx_ulid` extension.

### How the Extension is Built In

1. **Custom Image**: The database uses a custom PostgreSQL image (or base PostgreSQL image with extension support) that
   includes the compiled `pgx_ulid` extension.

2. **Initialization Script**: The `env/db/local-dbinit.sh` script runs automatically when the container starts and:
    - Creates application and admin database roles with specified credentials
    - Creates the admin database with appropriate ownership
    - Configures the admin schema with proper search paths
    - **Installs the ULID extension**: `CREATE EXTENSION IF NOT EXISTS "pgx_ulid" SCHEMA "${DB_SCHEMA_ADMIN}"`
    - Sets up granular permissions for app and admin users

3. **Environment Variables**: The initialization script uses the following environment variables:
    - `POSTGRES_USER`, `POSTGRES_PASS`: Superuser credentials
    - `DB_USER_APP`, `DB_PASS_APP`: Application user (read-only access)
    - `DB_USER_ADMIN`, `DB_PASS_ADMIN`: Admin user (full privileges)
    - `DB_NAME_ADMIN`: Database name
    - `DB_SCHEMA_ADMIN`: Schema name where the ULID extension is installed

## Environment Configuration

From this `compose` directory, follow these steps to configure the environment:

1. Copy `.env.example` to `.env` and configure the required environment variables:
   ```bash
   cp .env.example .env
   ```
2. Copy `env/db/db.env.example` to `env/db/db.env` and configure the database environment variables:
   ```bash
   cp env/db/db.env.example env/db/db.env
   ```
3. Copy `env/vk/vk.env.example` to `env/vk/vk.env` and configure the Valkey environment variables:
   ```bash
   cp env/vk/vk.env.example env/vk/vk.env
   ```

Environment files are git-ignored for security (see `.gitignore`).

## Usage

1. Start all services:
   ```bash
   docker-compose up -d
   ```
2. Stop all services:
   ```bash
   docker-compose down
   ```
3. View logs:
   ```bash
   docker-compose logs -f
   ```
4. Rebuild the database image from scratch and launch the container:
   ```bash
   docker compose down
   docker system prune --all -f
   docker volume prune --all -f
   docker compose up -d postgres
   ```

