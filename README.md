# Hardware RMA Management System

A full-stack RMA (Return Merchandise Authorization) management system for tracking hardware service requests, repairs, warehouse operations, and shipments.

Built with **Java 25**, **Spring Boot 4**, **PostgreSQL 16**, and **Docker Compose**.

## Features

* Role-based access control with `ADMIN`, `SERVICE`, `WAREHOUSE`, and `EMPLOYEE` roles
* RMA lifecycle management: creation → assignment → pickup → repair → warehouse return → shipment
* REST API for complaint and user management
* Real-time updates using WebSocket, STOMP, and SockJS
* Session-based authentication with Spring Security
* Flyway database migrations and data seeding
* JPA entity auditing
* Dockerized application with PostgreSQL and Nginx
* Light/Dark frontend themes

## Tech Stack

* **Backend:** Java 25, Spring Boot 4, Spring Security, Spring Data JPA
* **Database:** PostgreSQL 16, Flyway
* **Real-time:** WebSocket, STOMP, SockJS
* **Frontend:** React, TypeScript, Vite
* **Infrastructure:** Docker, Docker Compose, Nginx
* **Build:** Gradle (backend), npm (frontend)

## RMA Workflow

`Created → Assigned → Pickup → Repair → Return → Shipment`

## Quick Start

### Prerequisites

* Docker
* Docker Compose
* Git

### Clone the repository

```bash
git clone https://github.com/vadymtrach/rma-system-showcase.git
cd rma-system-showcase
```

### Configure environment

```bash
cp .env.example .env
```

Adjust the values in `.env` if necessary.

### Start the application

```bash
docker compose up -d --build
```

Open:

```text
https://localhost
```

The project uses a self-signed SSL certificate for local development, so the browser may display a certificate warning.

### Stop the application

```bash
docker compose down
```

To remove the database volume as well:

```bash
docker compose down -v
```

## Demo Credentials

On first start, an admin account is created from `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` in `.env`. With the values from `.env.example`, that is:

| Role               | Email                        | Password      |
| ------------------ | ---------------------------- | ------------- |
| Administrator      | `admin123@gmail.com`         | `admin123`    |

Change these values before deploying anywhere public. The admin is only created while no active admin exists, so editing `.env` afterwards does not change an existing admin's password.

## API

### Authentication

| Method | Endpoint      | Access        |
| ------ | ------------- | ------------- |
| `POST` | `/api/login`  | Public        |
| `POST` | `/api/logout` | Authenticated |

### Complaints

| Method  | Endpoint                        | Access               |
| ------- | ------------------------------- | -------------------- |
| `GET`   | `/api/complaints`               | Authenticated        |
| `POST`  | `/api/complaints`               | `ADMIN`, `SERVICE`   |
| `PATCH` | `/api/complaints/{id}/assign`   | `ADMIN`, `SERVICE`   |
| `PATCH` | `/api/complaints/{id}/pickup`   | `ADMIN`, `SERVICE`   |
| `PATCH` | `/api/complaints/{id}/repair`   | `ADMIN`, `SERVICE`   |
| `PATCH` | `/api/complaints/{id}/return`   | `ADMIN`, `SERVICE`   |
| `PATCH` | `/api/complaints/{id}/shipment` | `ADMIN`, `WAREHOUSE` |

### Users

| Method  | Endpoint                 | Access             |
| ------- | ------------------------ | ------------------ |
| `GET`   | `/api/users/me`          | Authenticated      |
| `PATCH` | `/api/users/me/password` | Authenticated      |
| `GET`   | `/api/users`             | `ADMIN`, `SERVICE` |

## Docker Services

| Service    | Description                                    |
| ---------- | ----------------------------------------------- |
| `app`      | Spring Boot application                         |
| `frontend` | React app, built and served via nginx           |
| `db`       | PostgreSQL 16                                   |
| `nginx`    | Public reverse proxy and SSL termination        |

The public `nginx` service routes `/api` and `/ws` to `app`, and everything else to `frontend`. The PostgreSQL container is named `rma-postgres`.

Useful commands:

```bash
docker compose ps
docker compose logs -f app
docker compose down
```

## Project Structure

```text
rma-system-showcase/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   │       └── db/
│   │   │           └── migration/
│   │   └── test/
│   ├── build.gradle
│   └── Dockerfile
├── frontend/
│   ├── src/
│   ├── package.json
│   └── Dockerfile
├── certs/
├── nginx.conf
├── docker-compose.yml
├── .env.example
└── README.md
```

## Local Development

### Backend

Run the application with Gradle from `backend/`:

```bash
cd backend
./gradlew bootRun
```

Windows:

```powershell
cd backend
.\gradlew.bat bootRun
```

Build the project:

```bash
./gradlew build
```

When running outside Docker, a PostgreSQL instance and the required environment variables must be available.

### Frontend

Run the Vite dev server from `frontend/` (proxies `/api` and `/ws` to `http://localhost:8080`, so run the backend alongside it):

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:3000`.
