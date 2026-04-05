# 🚀 Legitify – Legal Document Analysis Platform

Legitify is a **microservices-based platform** that enables users to upload legal or business documents, analyze them using AI, and generate structured **risk reports in PDF format**.

---

## 🧠 Overview

Legitify allows users to:

* Upload documents (PDF, DOCX, TXT)
* Extract structured text (with OCR fallback)
* Analyze clauses and risks using AI (Gemini)
* Generate professional PDF reports
* Track analysis jobs asynchronously

---

## 🏗️ Architecture

The system follows a **microservices architecture**:

```
Client (Frontend)
        ↓
   API Gateway
        ↓
 ┌───────────────┬────────────────────────┐
 │ Auth Service  │ Document Analysis Svc │
 └───────────────┴────────────────────────┘
```

---

## 🔐 Auth Service

Handles user authentication and authorization.

**Features**

* Registration & login
* JWT authentication (RSA)
* Refresh tokens (stored hashed)
* Protected endpoint: `/me`

**Tech**

* Spring Boot, Spring Security
* JWT (RSA)
* PostgreSQL (Neon)

---

## 🚪 API Gateway

Acts as the entry point for all client requests.

**Responsibilities**

* Validate JWT
* Route requests to services
* Enforce security rules

**Tech**

* Spring WebFlux
* Custom JWT filter

---

## 📄 Document Analysis Service

Processes documents and generates AI-based reports.

**Workflow**

1. Upload file
2. Create async job
3. Extract text (PDF / DOCX / TXT + OCR)
4. Analyze using Gemini
5. Generate PDF report
6. Update job status

---

## ⚙️ Tech Stack

### Backend

* Java 21
* Spring Boot
* Spring Security
* Spring WebFlux
* JPA / Hibernate

### Database

* PostgreSQL (Neon)

### Deployment

* Render

---

## 🔑 Authentication Flow

1. User registers or logs in
2. Receives:

   * Access Token (JWT)
   * Refresh Token (cookie)
3. API Gateway validates JWT
4. Protected routes require a valid token

---

## 📡 API Endpoints

### Auth Service

| Method | Endpoint                  | Description      |
| ------ | ------------------------- | ---------------- |
| POST   | `/legitify/auth/register` | Register user    |
| POST   | `/legitify/auth/sign-in`  | Login            |
| GET    | `/legitify/auth/me`       | Get current user |

---

### Document Analysis

| Method | Endpoint                       | Description               |
| ------ | ------------------------------ | ------------------------- |
| POST   | `/legitify/service/analyze`    | Upload & analyze document |
| GET    | `/legitify/service/jobs/{id}`  | Get job status            |
| GET    | `/legitify/service/pdf/{file}` | View generated PDF        |

---

## 📊 Job Status

Each analysis job can be:

* `PENDING`
* `PROCESSING`
* `DONE`
* `FAILED`

---

## ⚠️ Known Limitations

* **Render cold start latency**
  Services on Render (free tier) may become inactive.
  The first request can take **~30–60 seconds** to respond.

* **Gemini API free tier limits**
  Limited number of requests per day (≈20 for some models), which may restrict testing.

---
