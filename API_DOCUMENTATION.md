# Journeo — API Documentation V1

> **Base URL:** `http://localhost:8080`
> **Interactive Swagger UI:** [`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html)
> **OpenAPI JSON spec:** [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs)

---

## Authentication

This API uses **HTTP Basic Authentication**.

All endpoints (except those explicitly marked as **Public**) require valid credentials.

| Header | Value |
|--------|-------|
| `Authorization` | `Basic <base64(email:password)>` |

**Default accounts (created automatically on startup):**

| Email | Password | Role |
|-------|----------|------|
| `admin@example.com` | `admin123` | `ADMIN` |
| `user@example.com` | `user123` | `USER` |

**Roles:**
- `ADMIN` — Full access to all resources
- `USER` — Read-only access to assigned guides

---

## Endpoints Overview

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/users/ping` | Public | Health check |
| `POST` | `/api/users` | Public | Create a user |
| `GET` | `/api/users` | Auth | List all users |
| `GET` | `/api/users/{id}` | Auth | Get user by ID |
| `PUT` | `/api/users/{id}` | Auth | Update a user |
| `DELETE` | `/api/users/{id}` | Auth | Delete a user |
| `GET` | `/api/users/{id}/guides` | Auth | Get guides of a user |
| `GET` | `/api/guides` | Auth | List all guides |
| `POST` | `/api/guides` | ADMIN | Create a guide |
| `GET` | `/api/guides/{id}` | Auth | Get guide by ID |
| `PUT` | `/api/guides/{id}` | ADMIN | Update a guide |
| `DELETE` | `/api/guides/{id}` | ADMIN | Delete a guide |
| `POST` | `/api/guides/{guideId}/users/{userId}` | ADMIN | Assign user to guide |
| `DELETE` | `/api/guides/{guideId}/users/{userId}` | ADMIN | Remove user from guide |
| `POST` | `/api/activities/guide/{guideId}` | ADMIN | Add activity to guide |
| `GET` | `/api/activities/guide/{guideId}` | Auth | List activities of guide |
| `PUT` | `/api/activities/{activityId}` | ADMIN | Update an activity |
| `DELETE` | `/api/activities/{activityId}` | ADMIN | Delete an activity |

---

## Users

### `GET /api/users/ping`
> **Auth:** Public

Health check endpoint.

**Response `200`**
```
pong
```

---

### `POST /api/users`
> **Auth:** Public

Create a new user account.

**Request Body**
```json
{
  "email": "john.doe@example.com",
  "password": "mySecurePassword",
  "role": "USER"
}
```

| Field | Type | Required | Values |
|-------|------|----------|--------|
| `email` | string | ✅ | Valid email |
| `password` | string | ✅ | Min 1 character |
| `role` | string | ✅ | `USER`, `ADMIN` |

**Response `201 Created`**
```json
{
  "id": 3,
  "email": "john.doe@example.com",
  "role": "USER"
}
```

**Response `400 Bad Request`** — Missing or invalid fields.

---

### `GET /api/users`
> **Auth:** Required

Retrieve all users.

**Response `200`**
```json
[
  { "id": 1, "email": "admin@example.com", "role": "ADMIN" },
  { "id": 2, "email": "user@example.com", "role": "USER" }
]
```

---

### `GET /api/users/{id}`
> **Auth:** Required

Retrieve a single user by ID.

**Path Parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | Long | User ID |

**Response `200`**
```json
{
  "id": 1,
  "email": "admin@example.com",
  "role": "ADMIN"
}
```

**Response `404 Not Found`** — User does not exist.

---

### `PUT /api/users/{id}`
> **Auth:** Required

Update an existing user. All fields are optional (only provided fields are updated).

**Path Parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | Long | User ID |

**Request Body**
```json
{
  "email": "new.email@example.com",
  "password": "newPassword",
  "role": "ADMIN"
}
```

**Response `200`**
```json
{
  "id": 2,
  "email": "new.email@example.com",
  "role": "ADMIN"
}
```

**Response `404 Not Found`** — User does not exist.

---

### `DELETE /api/users/{id}`
> **Auth:** Required

Delete a user.

**Path Parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | Long | User ID |

**Response `200`** — User deleted successfully.
**Response `404 Not Found`** — User does not exist.

---

### `GET /api/users/{id}/guides`
> **Auth:** Required

Retrieve all guides assigned to a specific user.

**Response `200`**
```json
[
  {
    "id": 1,
    "titre": "Tour de Bretagne",
    "description": "Découverte des côtes bretonnes",
    "jours": 5,
    "mobilite": "A_PIED",
    "saison": "ETE",
    "pourQui": "FAMILLE",
    "activities": []
  }
]
```

**Response `404 Not Found`** — User does not exist.

---

## Guides

### `GET /api/guides`
> **Auth:** Required

Retrieve all guides.

**Response `200`**
```json
[
  {
    "id": 1,
    "titre": "Tour de Bretagne",
    "description": "Découverte des côtes bretonnes",
    "jours": 5,
    "mobilite": "A_PIED",
    "saison": "ETE",
    "pourQui": "FAMILLE",
    "activities": [
      {
        "id": 1,
        "titre": "Mont Saint-Michel",
        "description": "Découverte de l'abbaye",
        "type": "MUSEE",
        "adresse": "50170 Le Mont-Saint-Michel",
        "telephone": "+33 2 33 60 12 34",
        "siteInternet": "https://www.ot-montsaintmichel.com/",
        "heureDebut": "09:00",
        "duree": 120,
        "ordre": 1,
        "jour": 1
      }
    ]
  }
]
```

---

### `POST /api/guides`
> **Auth:** ADMIN only

Create a new guide.

**Request Body**
```json
{
  "titre": "Tour de Bretagne",
  "description": "Découverte des côtes bretonnes",
  "jours": 5,
  "mobilite": "A_PIED",
  "saison": "ETE",
  "pourQui": "FAMILLE"
}
```

| Field | Type | Required | Values |
|-------|------|----------|--------|
| `titre` | string | ✅ | Non-empty |
| `description` | string | ❌ | — |
| `jours` | integer | ✅ | > 0 |
| `mobilite` | string | ✅ | `VOITURE`, `VELO`, `A_PIED`, `MOTO` |
| `saison` | string | ✅ | `ETE`, `PRINTEMPS`, `AUTOMNE`, `HIVER` |
| `pourQui` | string | ✅ | `FAMILLE`, `SEUL`, `EN_GROUPE`, `ENTRE_AMIS` |

**Response `201 Created`**
```json
{
  "id": 1,
  "titre": "Tour de Bretagne",
  "description": "Découverte des côtes bretonnes",
  "jours": 5,
  "mobilite": "A_PIED",
  "saison": "ETE",
  "pourQui": "FAMILLE",
  "activities": []
}
```

**Response `400 Bad Request`** — Validation error or invalid enum value.
**Response `401 Unauthorized`** — Not authenticated.
**Response `403 Forbidden`** — Authenticated but not ADMIN.

---

### `GET /api/guides/{id}`
> **Auth:** Required

Retrieve a guide and all its activities (sorted by day then order).

**Path Parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | Long | Guide ID |

**Response `200`** — See `POST /api/guides` response format above.
**Response `404 Not Found`** — Guide does not exist.

---

### `PUT /api/guides/{id}`
> **Auth:** ADMIN only

Update an existing guide.

**Request Body** — Same format as `POST /api/guides`.

**Response `200`** — Updated guide.
**Response `404 Not Found`** — Guide does not exist.

---

### `DELETE /api/guides/{id}`
> **Auth:** ADMIN only

Delete a guide and all its activities.

**Response `200`** — Guide deleted.
**Response `404 Not Found`** — Guide does not exist.

---

### `POST /api/guides/{guideId}/users/{userId}`
> **Auth:** ADMIN only

Assign a user to a guide (grants the user access to view it).

**Path Parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `guideId` | Long | Guide ID |
| `userId` | Long | User ID |

**Response `200`** — Updated guide with the new user assignment.
**Response `400 Bad Request`** — Guide or user not found.

---

### `DELETE /api/guides/{guideId}/users/{userId}`
> **Auth:** ADMIN only

Remove a user from a guide (revokes access).

**Response `200`** — Updated guide.
**Response `400 Bad Request`** — Guide or user not found.

---

## Activities

Activities belong to a guide and are identified by a **day** (`jour`) and an **order** (`ordre`), allowing multi-day itinerary planning.

### `POST /api/activities/guide/{guideId}`
> **Auth:** ADMIN only

Add an activity to an existing guide.

**Path Parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `guideId` | Long | Guide ID |

**Request Body**
```json
{
  "titre": "Visite du Mont Saint-Michel",
  "description": "Découverte de l'abbaye et des alentours",
  "type": "MUSEE",
  "adresse": "50170 Le Mont-Saint-Michel, France",
  "telephone": "+33 2 33 60 12 34",
  "siteInternet": "https://www.ot-montsaintmichel.com/",
  "heureDebut": "09:00",
  "duree": 120,
  "ordre": 1,
  "jour": 1
}
```

| Field | Type | Required | Values / Notes |
|-------|------|----------|----------------|
| `titre` | string | ✅ | Non-empty |
| `description` | string | ❌ | — |
| `type` | string | ✅ | `MUSEE`, `CHATEAU`, `ACTIVITE`, `PARC`, `GROTTE` |
| `adresse` | string | ❌ | — |
| `telephone` | string | ❌ | — |
| `siteInternet` | string | ❌ | — |
| `heureDebut` | string | ❌ | Format `HH:mm` |
| `duree` | integer | ❌ | Duration in minutes |
| `ordre` | integer | ❌ | Visit order within the day |
| `jour` | integer | ❌ | Day number (1 = Day 1) |

**Response `200`**
```json
{
  "id": 1,
  "titre": "Visite du Mont Saint-Michel",
  "description": "Découverte de l'abbaye et des alentours",
  "type": "MUSEE",
  "adresse": "50170 Le Mont-Saint-Michel, France",
  "telephone": "+33 2 33 60 12 34",
  "siteInternet": "https://www.ot-montsaintmichel.com/",
  "heureDebut": "09:00",
  "duree": 120,
  "ordre": 1,
  "jour": 1
}
```

**Response `404 Not Found`** — Guide does not exist.

---

### `GET /api/activities/guide/{guideId}`
> **Auth:** Required

List all activities of a guide.

**Response `200`** — Array of activity objects (see format above).

---

### `PUT /api/activities/{activityId}`
> **Auth:** ADMIN only

Update an existing activity.

**Path Parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `activityId` | Long | Activity ID |

**Request Body** — Same format as `POST /api/activities/guide/{guideId}`.

**Response `200`** — Updated activity.
**Response `404 Not Found`** — Activity does not exist.

---

### `DELETE /api/activities/{activityId}`
> **Auth:** ADMIN only

Delete an activity.

**Response `200`** — Activity deleted.
**Response `404 Not Found`** — Activity does not exist.

---

## Enum Reference

### `Mobilite`
| Value | Description |
|-------|-------------|
| `VOITURE` | By car |
| `VELO` | By bike |
| `A_PIED` | On foot |
| `MOTO` | By motorbike |

### `Saison`
| Value | Description |
|-------|-------------|
| `ETE` | Summer |
| `PRINTEMPS` | Spring |
| `AUTOMNE` | Autumn |
| `HIVER` | Winter |

### `PublicCible` (pourQui)
| Value | Description |
|-------|-------------|
| `FAMILLE` | Family |
| `SEUL` | Solo |
| `EN_GROUPE` | Group |
| `ENTRE_AMIS` | Friends |

### `Activity.Type`
| Value | Description |
|-------|-------------|
| `MUSEE` | Museum |
| `CHATEAU` | Castle |
| `ACTIVITE` | Activity |
| `PARC` | Park |
| `GROTTE` | Cave |

---

## HTTP Status Codes

| Code | Meaning |
|------|---------|
| `200 OK` | Success |
| `201 Created` | Resource created (with `Location` header) |
| `400 Bad Request` | Validation error or invalid data |
| `401 Unauthorized` | Missing or invalid credentials |
| `403 Forbidden` | Authenticated but insufficient role |
| `404 Not Found` | Resource does not exist |

---

## Quick Start with cURL

**1. Create a guide (as admin)**
```bash
curl -X POST http://localhost:8080/api/guides \
  -u admin@example.com:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Tour de Bretagne",
    "description": "Découverte des côtes bretonnes",
    "jours": 5,
    "mobilite": "A_PIED",
    "saison": "ETE",
    "pourQui": "FAMILLE"
  }'
```

**2. Add an activity to guide ID 1**
```bash
curl -X POST http://localhost:8080/api/activities/guide/1 \
  -u admin@example.com:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "titre": "Mont Saint-Michel",
    "type": "MUSEE",
    "adresse": "50170 Le Mont-Saint-Michel",
    "heureDebut": "09:00",
    "duree": 120,
    "ordre": 1,
    "jour": 1
  }'
```

**3. Assign user ID 2 to guide ID 1**
```bash
curl -X POST http://localhost:8080/api/guides/1/users/2 \
  -u admin@example.com:admin123
```

**4. Get guide with all activities**
```bash
curl -X GET http://localhost:8080/api/guides/1 \
  -u admin@example.com:admin123
```
