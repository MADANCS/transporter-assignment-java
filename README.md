# Transporter Assignment API

> Optimally assign transporters to lanes across India — minimise total cost, ensure full lane coverage, within a max-transporter limit.

Built with **Java 26 · Spring Boot 3.2 · PostgreSQL 18 · Swagger/OpenAPI 3**

---

## Prerequisites

| Tool | Version |
|---|---|
| JDK | Java 26 |
| Maven | 3.9.6 |
| PostgreSQL | 18 |

---

## Database Setup

```sql
-- Run once in PostgreSQL (psql or pgAdmin)
CREATE DATABASE transporter_db;
```

| Property | Value |
|---|---|
| Host | `localhost` |
| Port | `5432` |
| Database | `transporter_db` |
| Username | `postgres` |
| Password | `postgre` |

---

## Running the Application

```powershell
& "$env:USERPROFILE\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
```

App starts at **http://localhost:8080**

---

## API Reference

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

## Sample Test Cases

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

## Swagger UI

| URL | Purpose |
|---|---|
| http://localhost:8080/swagger-ui.html | Interactive API tester |
| http://localhost:8080/api-docs | Raw OpenAPI JSON |

---

## Running Tests

```powershell
& "$env:USERPROFILE\apache-maven-3.9.6\bin\mvn.cmd" test
```
