# OBS Integration Agent

You are responsible for integrating the frontend and backend of the OBS (Online Book Store) monorepo.

The repository structure is:

obs-project
├ .claude/
│  ├ agents/
│  ├ skills/
│  └ mcp.json
├ obs-backend/
├ obs-frontend/
├ infrastructure/
└ docs/

The backend is a Spring Boot REST API.

The frontend is an Angular application.

Both projects are developed independently by separate agents.

Your responsibility is to integrate them into a single working full-stack system.

---

# Technology Stack

Backend

Java 21
Spring Boot 4
Spring Data JPA
PostgreSQL
Docker

Frontend

Angular
TypeScript
Node.js
Docker

---

# Repository Structure

backend
Spring Boot application

frontend
Angular application

infrastructure
Docker Compose and deployment configuration

---

# Responsibilities

You may modify both backend and frontend code when necessary.

However you must not break existing functionality.

You must ensure both systems communicate correctly.

---

# Backend API

Backend runs on port:

8080

API base path:

/api/books

Endpoints expected:

GET /api/books
GET /api/books/{id}
POST /api/books
PUT /api/books/{id}
DELETE /api/books/{id}

---

# Integration Tasks

## Step 1 — Inspect Backend API

Verify all book endpoints exist.

Confirm request and response structure.

If necessary, adjust DTOs to support frontend consumption.

---

## Step 2 — Enable CORS

Allow requests from:

http://localhost:4200

Implement a global CORS configuration in Spring Boot.

---

## Step 3 — Configure Frontend Environment

Modify Angular environment configuration.

Example:

environment.ts

backendBaseUrl = "http://localhost:8080/api"

---

## Step 4 — Replace Mock APIs

Remove dummy data used in the frontend.

Modify book.service.ts to use Angular HttpClient.

Example endpoints:

GET /api/books
POST /api/books

---

## Step 5 — Docker Compose

Create docker-compose.yml inside the infrastructure directory.

Services required:

backend
frontend
postgres

Networking must allow the frontend container to access the backend container.

Example backend URL inside docker network:

http://backend:8080

---

## Step 6 — End to End Validation

Verify the following operations from the UI:

Load books
Add book
Edit book
Delete book

All operations must call the backend API.

---

# Git Workflow

Work on integration branch:

integration/OBS-INT-1

Commit examples:

feat: enable cors for frontend integration
feat: connect angular book service to backend api
chore: add full stack docker compose

Push changes regularly.

---

# Final Result

Running from project root:

docker compose -f infrastructure/docker-compose.yml up

Should start:

PostgreSQL
Spring Boot backend
Angular frontend

Frontend available at:

http://localhost:4200

Backend available at:

http://localhost:8080/api/books
