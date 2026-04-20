# Jobs AWS

A full-stack job recruitment web application deployed on AWS EC2 with CI/CD via GitHub Actions.

## 🚀 Live Demo

Access: `http://<EC2_IP>`

**Demo accounts** (password: `12345` for all):

| Role | Email |
|------|-------|
| Employer | john@gmail.com |
| Employer | susan@gmail.com |
| Candidate | mary@gmail.com |
| Candidate | alex@gmail.com |

---

## 🛠 Tech Stack

**Backend**
- Java 25 + Spring Boot 4.0.5
- Spring Security (JWT-less, session-based)
- Spring Data JPA + PostgreSQL
- JavaMailSender (Gmail SMTP, optional)

**Frontend**
- Vue 3 + TypeScript + Vite
- Vue Router, Axios

**Infrastructure**
- Docker + Docker Compose
- AWS EC2 (Ubuntu)
- Nginx (reverse proxy + SPA fallback)
- GitHub Actions (CI/CD)
- Docker Hub (image registry)

---

## ✨ Features

- Job listing, search, filter by category/location/type
- Employer: post/manage recruitments, manage company profile
- Candidate: apply for jobs, save jobs, follow companies, upload CV
- Account registration with email verification (Gmail SMTP)
- Password reset via email
- File upload (avatars, company logos, CVs)

---

## 🏗 Architecture

```
Browser → Nginx (port 80)
              ├── /api/*      → Spring Boot (port 8080)
              ├── /uploads/*  → Spring Boot (static files)
              └── /*          → Vue SPA (index.html)
```

All services run via Docker Compose on EC2:
- `jobs-frontend` — Nginx + Vue dist
- `jobs-backend` — Spring Boot JAR
- `jobs-postgres` — PostgreSQL 15

---

## ⚙️ CI/CD Pipeline

Push to `main` → GitHub Actions:
1. Build & push `jobs-backend` image to Docker Hub
2. Build & push `jobs-frontend` image to Docker Hub
3. SCP `docker-compose.production.yml` to EC2
4. SSH to EC2 → pull new images → `docker compose up -d`

---

## 🔐 Environment / Secrets

All sensitive config is stored as **GitHub Secrets** and injected at deploy time. No credentials are committed to the repository.

| Secret | Description |
|--------|-------------|
| `DOCKERHUB_USERNAME` / `DOCKERHUB_TOKEN` | Docker Hub credentials |
| `EC2_HOST` / `EC2_USER` / `EC2_SSH_KEY` | EC2 SSH access |
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | Database |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` | Gmail SMTP |
| `APP_MAIL_ENABLED` | `true` to enable email, `false` to disable |

---

## 🧑‍💻 Local Development

```bash
# Start all services with hot-reload
docker compose up

# Frontend: http://localhost:5173
# Backend:  http://localhost:8080
# DB:       localhost:5432
```

Requires Docker Desktop installed.

