ARG PG_MAJOR=18
ARG PG_MINOR=3
ARG PG_RELEASE="${PG_MAJOR}.${PG_MINOR}"
ARG DEBIAN_TAG=trixie
ARG DOCKER_TAG="${PG_RELEASE}-${DEBIAN_TAG}"

#########################################################################################
# Build stage for pgx_ulid extension # ARG PGX_ULID_VERSION=0.2.3
FROM postgres:${DOCKER_TAG} AS builder

ENV DEBIAN_FRONTEND=noninteractive
ENV HOME=/root
ENV PATH=/root/.cargo/bin:$PATH

ENV BUILD_DEPS curl ca-certificates build-essential pkg-config libssl-dev git cmake \
    libpq-dev postgresql-server-dev-${PG_MAJOR} libreadline6-dev zlib1g-dev

RUN apt-get update && \
    apt-get install -y --no-install-recommends $BUILD_DEPS && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /tmp

RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y --no-modify-path --profile minimal --default-toolchain 1.90.0
RUN rustup --version && rustc --version && cargo --version

RUN cargo install --locked --version 0.17.0 cargo-pgrx
RUN cargo pgrx init --pg${PG_MAJOR} $(which pg_config)

RUN git clone https://github.com/pksunkara/pgx_ulid.git

WORKDIR /tmp/pgx_ulid

RUN cargo pgrx package --pg-config $(which pg_config)

#########################################################################################
# Final stage - PostgreSQL 18 with ulid extension
FROM postgres:${DOCKER_TAG} AS runtime

COPY --from=builder /tmp/pgx_ulid/target/release/pgx_ulid-pg${PG_MAJOR}/usr/share/postgresql/${PG_MAJOR}/extension/* /usr/share/postgresql/${PG_MAJOR}/extension/
COPY --from=builder /tmp/pgx_ulid/target/release/pgx_ulid-pg${PG_MAJOR}/usr/lib/postgresql/${PG_MAJOR}/lib/* /usr/lib/postgresql/${PG_MAJOR}/lib/

RUN echo "shared_preload_libraries = 'pgx_ulid'" >> /usr/share/postgresql/postgresql.conf.sample
