#!/bin/bash

# rebuild postgres image from scratch and launch container
docker compose down
docker system prune --all -f
docker volume prune --all -f
docker compose up -d postgres
