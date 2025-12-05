# Transport/Mission API Documentation - Complete Reference

## 📋 Overview
Complete API documentation for all mission/transport-related endpoints in the Transporteur application.

**Base URL:** `http://localhost:8080`

**Authentication:** Most endpoints require JWT Bearer Token in the Authorization header.

---

## 🔐 Authentication

### Headers Required
```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

---

## 📍 Table of Contents
1. [Mission Endpoints](#mission-endpoints)
2. [Transporteur Endpoints](#transporteur-endpoints)
3. [Price Management](#price-management)
4. [Data Structures](#data-structures)
5. [Error Handling](#error-handling)

---

# Mission Endpoints

## 1. Get Available Transporteurs

**Endpoint:** `GET /api/missions/transporteurs-disponibles`

**Authorization:** CLIENT role required

**Description:** Get list of all available transporteurs for mission assignment

**Request:**
```http
GET /api/missions/transporteurs-disponibles
Authorization: Bearer <JWT_TOKEN>
```

**Response (200 OK):**
```json
[
  {
    "idTransporteur": 1,
    "nom": "Martin",
    "prenom": "Pierre",
    "telephone": "12345678",
    "localisation": "Tunis",
    "noteMoyenne": 4.5,
    "disponible": true
  },
  {
    "idTransporteur": 2,
    "nom": "Dubois",
    "prenom": "Jean",
    "telephone": "87654321",
    "localisation": "Sousse",
    "noteMoyenne": 4.8,
    "disponible": true
  }
]
```

---

## 2. Create New Mission

**Endpoint:** `POST /api/missions`

**Authorization:** CLIENT role required

**Description:** Create a new transport mission

**Request:**
```http
POST /api/missions
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "transporteurId": 1,
  "dateMission": "2024-12-15T10:00:00",
  "lieuDepart": "Tunis, Avenue Habib Bourguiba",
  "lieuArrivee": "Sousse, Centre Ville",
  "description": "Transport de marchandises fragiles"
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| transporteurId | Long | ✅ Yes | ID of the transporteur to assign |
| dateMission | String (ISO 8601) | ✅ Yes | Mission date and time |
| lieuDepart | String | ✅ Yes | Departure location |
| lieuArrivee | String | ✅ Yes | Arrival location |
| description | String | ❌ No | Mission description/notes |

**Response (200 OK):**
```json
{
  "idMission": 123,
  "clientId": 45,
  "clientNom": "Dupont",
  "clientPrenom": "Jean",
  "transporteurId": 1,
  "transporteurNom": "Martin",
  "transporteurPrenom": "Pierre",
  "dateMission": "2024-12-15T10:00:00",
  "lieuDepart": "Tunis, Avenue Habib Bourguiba",
  "lieuArrivee": "Sousse, Centre Ville",
  "statut": "EN_ATTENTE",
  "dateCreation": "2024-12-01T08:30:00",
  "description": "Transport de marchandises fragiles",
  "proposedPrice": null,
  "priceConfirmed": false,
  "isPaid": false,
  "priceHistory": []
}
```

**Error Responses:**
- `400 Bad Request` - Invalid data or transporteur not available
- `401 Unauthorized` - No JWT token
- `403 Forbidden` - Not a CLIENT

---

## 3. Get Client's Missions

**Endpoint:** `GET /api/missions/client`

**Authorization:** CLIENT role required

**Description:** Get all missions created by the logged-in client

**Request:**
```http
GET /api/missions/client
Authorization: Bearer <JWT_TOKEN>
```

**Response (200 OK):**
```json
[
  {
    "idMission": 123,
    "clientId": 45,
    "clientNom": "Dupont",
    "clientPrenom": "Jean",
    "transporteurId": 1,
    "transporteurNom": "Martin",
    "transporteurPrenom": "Pierre",
    "dateMission": "2024-12-15T10:00:00",
    "lieuDepart": "Tunis",
    "lieuArrivee": "Sousse",
    "statut": "PRIX_PROPOSE",
    "dateCreation": "2024-12-01T08:30:00",
    "description": "Transport de marchandises",
    "proposedPrice": 120.00,
    "priceConfirmed": false,
    "isPaid": false,
    "priceHistory": [...]
  }
]
```

---

## 4. Get Transporteur's Missions

**Endpoint:** `GET /api/missions/transporteur`

**Authorization:** TRANSPORTEUR role required

**Description:** Get all missions assigned to the logged-in transporteur

**Request:**
```http
GET /api/missions/transporteur
Authorization: Bearer <JWT_TOKEN>
```

**Response (200 OK):**
```json
[
  {
    "idMission": 123,
    "clientId": 45,
    "clientNom": "Dupont",
    "clientPrenom": "Jean",
    "transporteurId": 1,
    "transporteurNom": "Martin",
    "transporteurPrenom": "Pierre",
    "dateMission": "2024-12-15T10:00:00",
    "lieuDepart": "Tunis",
    "lieuArrivee": "Sousse",
    "statut": "EN_COURS",
    "dateCreation": "2024-12-01T08:30:00",
    "description": "Transport de marchandises",
    "proposedPrice": 120.00,
    "priceConfirmed": true,
    "isPaid": true,
    "priceHistory": [...]
  }
]
```

---

## 5. Get Mission by ID

**Endpoint:** `GET /api/missions/{id}`

**Authorization:** CLIENT or TRANSPORTEUR role required

**Description:** Get details of a specific mission

**Request:**
```http
GET /api/missions/123
Authorization: Bearer <JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "idMission": 123,
  "clientId": 45,
  "clientNom": "Dupont",
  "clientPrenom": "Jean",
  "transporteurId": 1,
  "transporteurNom": "Martin",
  "transporteurPrenom": "Pierre",
  "dateMission": "2024-12-15T10:00:00",
  "lieuDepart": "Tunis, Avenue Habib Bourguiba",
  "lieuArrivee": "Sousse, Centre Ville",
  "statut": "PRIX_PROPOSE",
  "dateCreation": "2024-12-01T08:30:00",
  "description": "Transport de marchandises fragiles",
  "proposedPrice": 120.00,
  "priceConfirmed": false,
  "isPaid": false,
  "priceHistory": [
    {
      "id": 1,
      "oldPrice": null,
      "newPrice": 120.00,
      "changeReason": "Initial price proposal",
      "changedBy": "transporteur@example.com",
      "changeDate": "2024-12-01T09:00:00"
    }
  ]
}
```

**Error Responses:**
- `400 Bad Request` - Mission not found
- `403 Forbidden` - User doesn't have access to this mission

---

## 6. Get Transporteur Contact Information

**Endpoint:** `GET /api/missions/{id}/transporteur/contact`

**Authorization:** CLIENT role required

**Description:** Get transporteur's phone number and name for a specific mission

**Request:**
```http
GET /api/missions/123/transporteur/contact
Authorization: Bearer <JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "telephone": "12345678",
  "nom": "Martin",
  "prenom": "Pierre"
}
```

**Error Responses:**
- `400 Bad Request` - Mission not found
- `403 Forbidden` - User is not the client of this mission
- `400 Bad Request` - "Aucun transporteur assigné à cette mission"

---

## 7. Update Mission Status

**Endpoint:** `PUT /api/missions/{id}/statut`

**Authorization:** TRANSPORTEUR role required

**Description:** Update the status of a mission (transporteur only)

**Request:**
```http
PUT /api/missions/123/statut
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "statut": "EN_COURS"
}
```

**Valid Status Values:**
- `EN_ATTENTE` - Waiting for price proposal
- `PRIX_PROPOSE` - Price proposed
- `PRIX_CONFIRME` - Price confirmed by client
- `ACCEPTEE` - Payment done, mission accepted
- `EN_COURS` - Mission in progress
- `TERMINEE` - Mission completed
- `ANNULEE` - Mission cancelled

**Response (200 OK):**
```json
{
  "idMission": 123,
  "statut": "EN_COURS",
  ...
}
```

**Error Responses:**
- `400 Bad Request` - "Statut invalide"
- `403 Forbidden` - "Vous n'êtes pas le transporteur de cette mission"

---

# Price Management

## 8. Propose Price (Transporteur)

**Endpoint:** `POST /api/missions/{id}/propose-price`

**Authorization:** TRANSPORTEUR role required

**Description:** Transporteur proposes a price for the mission

**Request:**
```http
POST /api/missions/123/propose-price
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "proposedPrice": 120.00
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| proposedPrice | Double | ✅ Yes | Proposed price (must be > 0) |

**Response (200 OK):**
```json
{
  "idMission": 123,
  "proposedPrice": 120.00,
  "statut": "PRIX_PROPOSE",
  "priceConfirmed": false,
  "isPaid": false,
  "priceHistory": [
    {
      "id": 1,
      "oldPrice": null,
      "newPrice": 120.00,
      "changeReason": "Initial price proposal",
      "changedBy": "transporteur@example.com",
      "changeDate": "2024-12-01T09:00:00"
    }
  ],
  ...
}
```

**Error Responses:**
- `400 Bad Request` - "Impossible de proposer un prix pour cette mission" (wrong status)
- `400 Bad Request` - "Prix invalide" (price <= 0)
- `403 Forbidden` - "Vous n'êtes pas le transporteur de cette mission"

---

## 9. Update Proposed Price (Transporteur) ✨ NEW

**Endpoint:** `PUT /api/missions/{id}/update-price`

**Authorization:** TRANSPORTEUR role required

**Description:** Transporteur can modify the proposed price BEFORE client confirmation

**Request:**
```http
PUT /api/missions/123/update-price
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "newPrice": 150.00,
  "reason": "Distance recalculée - route plus longue"
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| newPrice | Double | ✅ Yes | New proposed price (must be > 0) |
| reason | String | ❌ No | Reason for price change (optional) |

**Response (200 OK):**
```json
{
  "idMission": 123,
  "proposedPrice": 150.00,
  "statut": "PRIX_PROPOSE",
  "priceConfirmed": false,
  "isPaid": false,
  "priceHistory": [
    {
      "id": 2,
      "oldPrice": 120.00,
      "newPrice": 150.00,
      "changeReason": "Distance recalculée - route plus longue",
      "changedBy": "transporteur@example.com",
      "changeDate": "2024-12-01T14:30:00"
    },
    {
      "id": 1,
      "oldPrice": null,
      "newPrice": 120.00,
      "changeReason": "Initial price proposal",
      "changedBy": "transporteur@example.com",
      "changeDate": "2024-12-01T09:00:00"
    }
  ],
  ...
}
```

**When It Works:**
- ✅ Mission status is `PRIX_PROPOSE`
- ✅ Price NOT confirmed by client (`priceConfirmed = false`)
- ✅ User is the assigned transporteur

**Error Responses:**
- `400 Bad Request` - "Le prix ne peut plus être modifié (statut actuel: X)"
- `400 Bad Request` - "Le prix a déjà été confirmé par le client"
- `400 Bad Request` - "Prix invalide" (price <= 0)
- `403 Forbidden` - "Vous n'êtes pas le transporteur de cette mission"

---

## 10. Confirm Price (Client)

**Endpoint:** `POST /api/missions/{id}/confirm-price`

**Authorization:** CLIENT role required

**Description:** Client confirms the proposed price

**Request:**
```http
POST /api/missions/123/confirm-price
Authorization: Bearer <JWT_TOKEN>
```

**Response (200 OK):**
```json
{
  "idMission": 123,
  "proposedPrice": 120.00,
  "statut": "PRIX_CONFIRME",
  "priceConfirmed": true,
  "isPaid": false,
  ...
}
```

**Error Responses:**
- `400 Bad Request` - "Aucun prix proposé pour cette mission"
- `400 Bad Request` - "Prix non défini"
- `403 Forbidden` - "Vous n'êtes pas le client de cette mission"

---

# Data Structures

## Mission Statuses

```typescript
enum MissionStatus {
  EN_ATTENTE = 'EN_ATTENTE',           // Waiting for price proposal
  PRIX_PROPOSE = 'PRIX_PROPOSE',       // Price proposed by transporteur
  PRIX_CONFIRME = 'PRIX_CONFIRME',     // Price confirmed by client
  ACCEPTEE = 'ACCEPTEE',               // Payment done, mission accepted
  EN_COURS = 'EN_COURS',               // Mission in progress
  TERMINEE = 'TERMINEE',               // Mission completed
  ANNULEE = 'ANNULEE'                  // Mission cancelled
}
```

## TypeScript Interfaces

### TransporteurAvailableResponse
```typescript
export interface TransporteurAvailableResponse {
  idTransporteur: number;
  nom: string;
  prenom: string;
  telephone: string;
  localisation: string;
  noteMoyenne: number;
  disponible: boolean;
}
```

### MissionRequest
```typescript
export interface MissionRequest {
  transporteurId: number;
  dateMission: string;  // ISO 8601 format
  lieuDepart: string;
  lieuArrivee: string;
  description?: string;
}
```

### MissionResponse
```typescript
export interface MissionResponse {
  idMission: number;
  clientId: number;
  clientNom: string;
  clientPrenom: string;
  transporteurId: number;
  transporteurNom: string;
  transporteurPrenom: string;
  dateMission: string;  // ISO 8601 format
  lieuDepart: string;
  lieuArrivee: string;
  statut: string;  // MissionStatus enum value
  dateCreation: string;  // ISO 8601 format
  description: string;
  proposedPrice: number | null;
  priceConfirmed: boolean;
  isPaid: boolean;
  priceHistory: PriceHistoryResponse[];
}
```

### PriceHistoryResponse
```typescript
export interface PriceHistoryResponse {
  id: number;
  oldPrice: number | null;  // null for initial proposal
  newPrice: number;
  changeReason: string;
  changedBy: string;  // Email of transporteur
  changeDate: string;  // ISO 8601 format
}
```

### PriceProposalRequest
```typescript
export interface PriceProposalRequest {
  proposedPrice: number;
}
```

### UpdatePriceRequest
```typescript
export interface UpdatePriceRequest {
  newPrice: number;
  reason?: string;  // Optional
}
```

### TransporteurContactResponse
```typescript
export interface TransporteurContactResponse {
  telephone: string;
  nom: string;
  prenom: string;
}
```

---

# Angular Service Example

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MissionService {
  private apiUrl = 'http://localhost:8080/api/missions';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  // 1. Get available transporteurs
  getAvailableTransporteurs(): Observable<TransporteurAvailableResponse[]> {
    return this.http.get<TransporteurAvailableResponse[]>(
      `${this.apiUrl}/transporteurs-disponibles`,
      { headers: this.getAuthHeaders() }
    );
  }

  // 2. Create new mission
  createMission(mission: MissionRequest): Observable<MissionResponse> {
    return this.http.post<MissionResponse>(
      this.apiUrl,
      mission,
      { headers: this.getAuthHeaders() }
    );
  }

  // 3. Get client's missions
  getClientMissions(): Observable<MissionResponse[]> {
    return this.http.get<MissionResponse[]>(
      `${this.apiUrl}/client`,
      { headers: this.getAuthHeaders() }
    );
  }

  // 4. Get transporteur's missions
  getTransporteurMissions(): Observable<MissionResponse[]> {
    return this.http.get<MissionResponse[]>(
      `${this.apiUrl}/transporteur`,
      { headers: this.getAuthHeaders() }
    );
  }

  // 5. Get mission by ID
  getMissionById(id: number): Observable<MissionResponse> {
    return this.http.get<MissionResponse>(
      `${this.apiUrl}/${id}`,
      { headers: this.getAuthHeaders() }
    );
  }

  // 6. Get transporteur contact
  getTransporteurContact(missionId: number): Observable<TransporteurContactResponse> {
    return this.http.get<TransporteurContactResponse>(
      `${this.apiUrl}/${missionId}/transporteur/contact`,
      { headers: this.getAuthHeaders() }
    );
  }

  // 7. Update mission status
  updateMissionStatus(missionId: number, status: string): Observable<MissionResponse> {
    return this.http.put<MissionResponse>(
      `${this.apiUrl}/${missionId}/statut`,
      { statut: status },
      { headers: this.getAuthHeaders() }
    );
  }

  // 8. Propose price
  proposePrice(missionId: number, price: number): Observable<MissionResponse> {
    return this.http.post<MissionResponse>(
      `${this.apiUrl}/${missionId}/propose-price`,
      { proposedPrice: price },
      { headers: this.getAuthHeaders() }
    );
  }

  // 9. Update proposed price (NEW)
  updateProposedPrice(
    missionId: number, 
    newPrice: number, 
    reason?: string
  ): Observable<MissionResponse> {
    return this.http.put<MissionResponse>(
      `${this.apiUrl}/${missionId}/update-price`,
      { newPrice, reason },
      { headers: this.getAuthHeaders() }
    );
  }

  // 10. Confirm price
  confirmPrice(missionId: number): Observable<MissionResponse> {
    return this.http.post<MissionResponse>(
      `${this.apiUrl}/${missionId}/confirm-price`,
      {},
      { headers: this.getAuthHeaders() }
    );
  }
}
```

---

# Error Handling

## Standard Error Responses

### 400 Bad Request
```json
"Error message describing what went wrong"
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required"
}
```

### 403 Forbidden
```json
"Access denied message"
```

### 404 Not Found
```json
"Resource not found"
```

## Error Handling in Angular

```typescript
this.missionService.createMission(missionData).subscribe({
  next: (mission) => {
    console.log('Mission created:', mission);
    this.showSuccess('Mission créée avec succès!');
  },
  error: (err) => {
    console.error('Error creating mission:', err);
    
    if (err.status === 400) {
      this.showError(err.error || 'Données invalides');
    } else if (err.status === 401) {
      this.showError('Session expirée. Veuillez vous reconnecter.');
      this.router.navigate(['/login']);
    } else if (err.status === 403) {
      this.showError(err.error || 'Accès refusé');
    } else {
      this.showError('Erreur serveur. Veuillez réessayer.');
    }
  }
});
```

---

# Complete Workflow Examples

## Workflow 1: Client Creates Mission and Pays

```typescript
// Step 1: Get available transporteurs
this.missionService.getAvailableTransporteurs().subscribe({
  next: (transporteurs) => {
    this.availableTransporteurs = transporteurs;
  }
});

// Step 2: Create mission
const missionData: MissionRequest = {
  transporteurId: selectedTransporteur.idTransporteur,
  dateMission: '2024-12-15T10:00:00',
  lieuDepart: 'Tunis',
  lieuArrivee: 'Sousse',
  description: 'Transport de marchandises'
};

this.missionService.createMission(missionData).subscribe({
  next: (mission) => {
    console.log('Mission created with status:', mission.statut); // EN_ATTENTE
  }
});

// Step 3: Wait for transporteur to propose price
// (Transporteur calls proposePrice endpoint)

// Step 4: Client confirms price
this.missionService.confirmPrice(missionId).subscribe({
  next: (mission) => {
    console.log('Price confirmed:', mission.proposedPrice);
    console.log('Status:', mission.statut); // PRIX_CONFIRME
  }
});

// Step 5: Client pays (separate payment endpoint)
// Mission status becomes ACCEPTEE
```

## Workflow 2: Transporteur Proposes and Updates Price

```typescript
// Step 1: Transporteur proposes initial price
this.missionService.proposePrice(missionId, 120.00).subscribe({
  next: (mission) => {
    console.log('Price proposed:', mission.proposedPrice);
    console.log('Status:', mission.statut); // PRIX_PROPOSE
  }
});

// Step 2: Transporteur realizes price needs adjustment
this.missionService.updateProposedPrice(
  missionId, 
  150.00, 
  'Distance recalculée - route plus longue'
).subscribe({
  next: (mission) => {
    console.log('New price:', mission.proposedPrice); // 150.00
    console.log('Price history:', mission.priceHistory);
    // Shows both price changes with reasons
  }
});

// Step 3: Client sees current price + history and confirms
// (Client calls confirmPrice endpoint)
```

## Workflow 3: Mission Execution

```typescript
// Step 1: Transporteur starts mission
this.missionService.updateMissionStatus(missionId, 'EN_COURS').subscribe({
  next: (mission) => {
    console.log('Mission started');
  }
});

// Step 2: Transporteur completes mission
this.missionService.updateMissionStatus(missionId, 'TERMINEE').subscribe({
  next: (mission) => {
    console.log('Mission completed');
  }
});
```

---

# Testing Checklist

## Client Tests
- [ ] Can get list of available transporteurs
- [ ] Can create a new mission
- [ ] Can view all their missions
- [ ] Can get mission details by ID
- [ ] Can get transporteur contact information
- [ ] Can confirm proposed price
- [ ] Cannot access transporteur-only endpoints

## Transporteur Tests
- [ ] Can view all assigned missions
- [ ] Can get mission details by ID
- [ ] Can propose price for mission
- [ ] Can update proposed price before client confirmation
- [ ] Cannot update price after client confirmation
- [ ] Can update mission status
- [ ] Cannot access client-only endpoints

## Price Update Tests
- [ ] Transporteur can update price when status is PRIX_PROPOSE
- [ ] Price history is created for each change
- [ ] Optional reason field works
- [ ] Cannot update price when status is PRIX_CONFIRME
- [ ] Client can see full price history

---

# Important Notes

1. **Date Format:** All dates use ISO 8601 format: `2024-12-15T10:00:00`

2. **JWT Token:** Must be included in Authorization header for all protected endpoints

3. **Role Checking:** Endpoints enforce role-based access via `@PreAuthorize` annotations

4. **Price History:** Automatically tracked when transporteur updates price (new feature)

5. **Mission Flow:** 
   ```
   EN_ATTENTE → PRIX_PROPOSE → PRIX_CONFIRME → ACCEPTEE → EN_COURS → TERMINEE
   ```

6. **Error Messages:** Always in French to match backend

---

**API Version:** 2.0  
**Last Updated:** December 4, 2025  
**Build Status:** ✅ BUILD SUCCESS

---

## Related Documentation

- **Price Update Feature:** `PRICE_UPDATE_FEATURE_INTEGRATION_GUIDE.md`
- **Payment Integration:** `PAYMENT_INTEGRATION_GUIDE.md`
- **Admin API:** `ADMIN_DASHBOARD_INTEGRATION_GUIDE.md`
