# Transporter Assignment API

> Optimally assign transporters to lanes across India — minimise total cost, ensure full lane coverage, within a configurable max-transporter limit.

Built with **Java 17 · Spring Boot 3.2.5 · PostgreSQL · Hibernate 6 · Swagger/OpenAPI 3**

---

## 🌐 Live Deployment

The API is deployed and live on Render:

| Service | Live URL | Status |
|---|---|---|
| **Interactive Swagger UI** | [https://transporter-assignment-api.onrender.com/swagger-ui.html](https://transporter-assignment-api.onrender.com/swagger-ui.html) | 🟢 Active |
| **OpenAPI Specification (JSON)** | [https://transporter-assignment-api.onrender.com/api-docs](https://transporter-assignment-api.onrender.com/api-docs) | 🟢 Active |
| **API Base URL** | [https://transporter-assignment-api.onrender.com](https://transporter-assignment-api.onrender.com) | 🟢 Active |

---

## 🛠️ Technology Stack & Versions

| Tool | Version | Description |
|---|---|---|
| **Java** | 17 (LTS) | Targeted bytecode release (builds on JDK 17+) |
| **Spring Boot** | 3.2.5 | Core web framework, Spring Data JPA, Jakarta Validation |
| **Springdoc OpenAPI** | 2.5.0 | OpenAPI 3 spec & interactive Swagger UI |
| **Database** | PostgreSQL 16+ / H2 | PostgreSQL for production/default, H2 in-memory for testing |
| **Build Tool** | Maven 3.9+ | Build and dependency management |

---

## 🚀 Running Locally

### Option 1: In-Memory H2 Database (Zero Setup)
Runs without needing PostgreSQL installed locally:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```
*App starts at `http://localhost:8080` with H2 console at `http://localhost:8080/h2-console`*

### Option 2: Local PostgreSQL
1. Create the database in PostgreSQL:
```sql
CREATE DATABASE transporter_db;
```
2. Run the application:
```bash
mvn spring-boot:run
```
*(Default settings in `application.yml`: `localhost:5432`, user `postgres`, password `postgre`)*

### Option 3: Docker
```bash
docker build -t transporter-assignment-api .
docker run -p 8080:8080 transporter-assignment-api
```

---

## 📖 Local Swagger UI & Documentation

When running locally:
* **Interactive Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **Raw OpenAPI JSON**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## 📡 API Reference

### API 1 — Submit Input Data

**POST** `/api/v1/transporters/input`

**Request Body:**

```json
{
  "lanes": [
    {"id": 1, "origin": "Mumbai",    "destination": "Delhi"},
    {"id": 2, "origin": "Delhi",     "destination": "Bangalore"},
    {"id": 3, "origin": "Chennai",   "destination": "Kolkata"},
    {"id": 4, "origin": "Pune",      "destination": "Hyderabad"},
    {"id": 5, "origin": "Ahmedabad", "destination": "Jaipur"}
  ],
  "transporters": [
    {"id": 1, "name": "Transporter T1", "laneQuotes": [{"laneId": 1, "quote": 20835}, {"laneId": 2, "quote": 10512}, {"laneId": 3, "quote": 22105}, {"laneId": 4, "quote": 42481}, {"laneId": 5, "quote": 19862}]},
    {"id": 2, "name": "Transporter T2", "laneQuotes": [{"laneId": 1, "quote": 48844}, {"laneId": 2, "quote": 31326}, {"laneId": 3, "quote": 18640}, {"laneId": 4, "quote": 45828}, {"laneId": 5, "quote": 18297}]},
    {"id": 3, "name": "Transporter T3", "laneQuotes": [{"laneId": 1, "quote": 39020}, {"laneId": 2, "quote": 20648}, {"laneId": 3, "quote": 31438}, {"laneId": 4, "quote": 36447}, {"laneId": 5, "quote": 12789}]},
    {"id": 4, "name": "Transporter T4", "laneQuotes": [{"laneId": 1, "quote": 14400}, {"laneId": 2, "quote": 44514}, {"laneId": 3, "quote": 14316}, {"laneId": 4, "quote": 10678}, {"laneId": 5, "quote": 13032}]},
    {"id": 5, "name": "Transporter T5", "laneQuotes": [{"laneId": 1, "quote": 11601}, {"laneId": 2, "quote": 19760}, {"laneId": 3, "quote": 40870}, {"laneId": 4, "quote": 20635}, {"laneId": 5, "quote": 26421}]},
    {"id": 6, "name": "Transporter T6", "laneQuotes": [{"laneId": 1, "quote": 35095}, {"laneId": 2, "quote": 12494}, {"laneId": 3, "quote": 17808}, {"laneId": 4, "quote": 36210}, {"laneId": 5, "quote": 39444}]},
    {"id": 7, "name": "Transporter T7", "laneQuotes": [{"laneId": 1, "quote": 26070}, {"laneId": 2, "quote": 41098}, {"laneId": 3, "quote": 20932}, {"laneId": 4, "quote": 16897}, {"laneId": 5, "quote": 27938}]}
  ]
}
```

**Response:**

```json
{
  "status": "success",
  "message": "Input data saved successfully."
}
```

---

### API 2 — Get Optimized Results

**POST** `/api/v1/transporters/assignment`

**Request Body:**

```json
{
  "maxTransporters": 3
}
```

**Response:**

```json
{
  "status": "success",
  "totalCost": 75000,
  "assignments": [
    {"laneId": 1, "transporterId": 4},
    {"laneId": 2, "transporterId": 6},
    {"laneId": 3, "transporterId": 2},
    {"laneId": 4, "transporterId": 4},
    {"laneId": 5, "transporterId": 3}
  ],
  "selectedTransporters": [4, 6, 3]
}
```

---

## 🧪 Sample Test Cases

### Test Case 1 — 5 Lanes, 7 Transporters

<details>
<summary>Click to expand</summary>

**POST /api/v1/transporters/input**
```json
{
  "lanes": [
    {"id": 1, "origin": "Mumbai",    "destination": "Delhi"},
    {"id": 2, "origin": "Delhi",     "destination": "Bangalore"},
    {"id": 3, "origin": "Chennai",   "destination": "Kolkata"},
    {"id": 4, "origin": "Pune",      "destination": "Hyderabad"},
    {"id": 5, "origin": "Ahmedabad", "destination": "Jaipur"}
  ],
  "transporters": [
    {"id": 1, "name": "Transporter T1", "laneQuotes": [{"laneId": 1, "quote": 20835}, {"laneId": 2, "quote": 10512}, {"laneId": 3, "quote": 22105}, {"laneId": 4, "quote": 42481}, {"laneId": 5, "quote": 19862}]},
    {"id": 2, "name": "Transporter T2", "laneQuotes": [{"laneId": 1, "quote": 48844}, {"laneId": 2, "quote": 31326}, {"laneId": 3, "quote": 18640}, {"laneId": 4, "quote": 45828}, {"laneId": 5, "quote": 18297}]},
    {"id": 3, "name": "Transporter T3", "laneQuotes": [{"laneId": 1, "quote": 39020}, {"laneId": 2, "quote": 20648}, {"laneId": 3, "quote": 31438}, {"laneId": 4, "quote": 36447}, {"laneId": 5, "quote": 12789}]},
    {"id": 4, "name": "Transporter T4", "laneQuotes": [{"laneId": 1, "quote": 14400}, {"laneId": 2, "quote": 44514}, {"laneId": 3, "quote": 14316}, {"laneId": 4, "quote": 10678}, {"laneId": 5, "quote": 13032}]},
    {"id": 5, "name": "Transporter T5", "laneQuotes": [{"laneId": 1, "quote": 11601}, {"laneId": 2, "quote": 19760}, {"laneId": 3, "quote": 40870}, {"laneId": 4, "quote": 20635}, {"laneId": 5, "quote": 26421}]},
    {"id": 6, "name": "Transporter T6", "laneQuotes": [{"laneId": 1, "quote": 35095}, {"laneId": 2, "quote": 12494}, {"laneId": 3, "quote": 17808}, {"laneId": 4, "quote": 36210}, {"laneId": 5, "quote": 39444}]},
    {"id": 7, "name": "Transporter T7", "laneQuotes": [{"laneId": 1, "quote": 26070}, {"laneId": 2, "quote": 41098}, {"laneId": 3, "quote": 20932}, {"laneId": 4, "quote": 16897}, {"laneId": 5, "quote": 27938}]}
  ]
}
```

**POST /api/v1/transporters/assignment** → `{ "maxTransporters": 3 }`

**Expected Response:**
```json
{
  "status": "success",
  "totalCost": 75000,
  "assignments": [
    {"laneId": 1, "transporterId": 4},
    {"laneId": 2, "transporterId": 6},
    {"laneId": 3, "transporterId": 2},
    {"laneId": 4, "transporterId": 4},
    {"laneId": 5, "transporterId": 3}
  ],
  "selectedTransporters": [4, 6, 3]
}
```

</details>

### Test Case 2 — Partial Coverage (Some Transporters Don't Quote All Lanes)

<details>
<summary>Click to expand</summary>

**POST /api/v1/transporters/input**
```json
{
  "lanes": [
    {"id": 1, "origin": "Chandigarh",  "destination": "Shimla"},
    {"id": 2, "origin": "Agra",        "destination": "Kanpur"},
    {"id": 3, "origin": "Varanasi",    "destination": "Gorakhpur"},
    {"id": 4, "origin": "Amritsar",    "destination": "Ludhiana"},
    {"id": 5, "origin": "Coimbatore",  "destination": "Madurai"}
  ],
  "transporters": [
    {"id": 1, "name": "Transporter X", "laneQuotes": [{"laneId": 1, "quote": 1000}, {"laneId": 2, "quote": 1500}]},
    {"id": 2, "name": "Transporter Y", "laneQuotes": [{"laneId": 3, "quote": 2000}, {"laneId": 4, "quote": 1800}]},
    {"id": 3, "name": "Transporter Z", "laneQuotes": [{"laneId": 5, "quote": 1200}]},
    {"id": 4, "name": "Transporter W", "laneQuotes": [{"laneId": 1, "quote": 1100}, {"laneId": 4, "quote": 1750}]}
  ]
}
```

**POST /api/v1/transporters/assignment** → `{ "maxTransporters": 3 }`

</details>

---

## 🧪 Running Automated Tests

Run the full suite of unit and integration tests (18 tests):
```bash
mvn test
```
