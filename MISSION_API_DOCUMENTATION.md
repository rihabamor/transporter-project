# Mission Feature - API Documentation for UI Team

## 📋 Table of Contents
1. [Overview](#overview)
2. [Mission Flow](#mission-flow)
3. [API Endpoints](#api-endpoints)
4. [Data Models](#data-models)
5. [Frontend Implementation Guide](#frontend-implementation-guide)
6. [User Stories](#user-stories)
7. [Testing Scenarios](#testing-scenarios)

---

## Overview

The **Mission** feature allows clients to create transport missions and transporters to accept and manage them.

### Key Features
- ✅ Clients can view available transporters
- ✅ Clients can create missions with date, pickup location, and destination
- ✅ Transporters can view their assigned missions
- ✅ Transporters can update mission status
- ✅ Clients can cancel missions
- ✅ Real-time mission counts on dashboard

### Mission Statuses
| Status | Description | Who Can Set |
|--------|-------------|-------------|
| `EN_ATTENTE` | Waiting for transporteur confirmation | System (default) |
| `ACCEPTEE` | Accepted by transporteur | Transporteur |
| `EN_COURS` | Mission in progress | Transporteur |
| `TERMINEE` | Mission completed | Transporteur |
| `ANNULEE` | Mission cancelled | Client |

---

## Mission Flow

### Client Flow
```
1. Client logs in
2. Client clicks "Create Mission"
3. Client fills form:
   - Date/Time
   - Pickup location (from)
   - Destination (to)
   - Description (optional)
4. System shows available transporters
5. Client selects a transporteur
6. Client submits form
7. Mission created with status "EN_ATTENTE"
```

### Transporteur Flow
```
1. Transporteur logs in
2. Transporteur sees list of assigned missions
3. Transporteur can:
   - Accept mission → status becomes "ACCEPTEE"
   - Start mission → status becomes "EN_COURS"
   - Complete mission → status becomes "TERMINEE"
```

---

## API Endpoints

### 1. Get Available Transporteurs
**Endpoint:** `GET /api/missions/transporteurs/disponibles`  
**Authentication:** Required (ROLE_CLIENT)  
**Description:** Get list of transporteurs available to accept missions

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Success Response (200):**
```json
[
  {
    "idTransporteur": 1,
    "nom": "Martin",
    "prenom": "Pierre",
    "telephone": "+33687654321",
    "localisation": "Paris",
    "noteMoyenne": 4.5,
    "disponible": true
  },
  {
    "idTransporteur": 2,
    "nom": "Bernard",
    "prenom": "Sophie",
    "telephone": "+33698765432",
    "localisation": "Lyon",
    "noteMoyenne": 4.8,
    "disponible": true
  }
]
```

**Use Case:** 
Call this endpoint to populate the transporteur selection dropdown when creating a mission.

---

### 2. Create Mission
**Endpoint:** `POST /api/missions`  
**Authentication:** Required (ROLE_CLIENT)  
**Description:** Create a new transport mission

**Headers:**
```
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "transporteurId": 1,
  "dateMission": "2025-12-15T10:00:00",
  "lieuDepart": "123 Rue de Paris, Lyon",
  "lieuArrivee": "456 Avenue des Champs, Paris",
  "description": "Transport de meubles"
}
```

**Validation Rules:**
- `transporteurId`: Required, must exist and be available
- `dateMission`: Required, ISO 8601 format (YYYY-MM-DDTHH:mm:ss)
- `lieuDepart`: Required, non-empty string
- `lieuArrivee`: Required, non-empty string
- `description`: Optional

**Success Response (200):**
```json
{
  "idMission": 1,
  "clientId": 5,
  "clientNom": "Dupont",
  "clientPrenom": "Jean",
  "transporteurId": 1,
  "transporteurNom": "Martin",
  "transporteurPrenom": "Pierre",
  "dateMission": "2025-12-15T10:00:00",
  "lieuDepart": "123 Rue de Paris, Lyon",
  "lieuArrivee": "456 Avenue des Champs, Paris",
  "statut": "EN_ATTENTE",
  "dateCreation": "2025-12-01T14:30:00",
  "description": "Transport de meubles"
}
```

**Error Responses:**

*Transporteur not available (400):*
```json
"Ce transporteur n'est pas disponible"
```

*Transporteur not found (400):*
```json
"Transporteur non trouvé"
```

*Validation error (400):*
```json
{
  "timestamp": "2025-12-01T14:30:00",
  "message": "Validation failed",
  "details": "{dateMission=La date de la mission est obligatoire}"
}
```

---

### 3. Get Client's Missions
**Endpoint:** `GET /api/missions/client`  
**Authentication:** Required (ROLE_CLIENT)  
**Description:** Get all missions created by the logged-in client

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Success Response (200):**
```json
[
  {
    "idMission": 1,
    "clientId": 5,
    "clientNom": "Dupont",
    "clientPrenom": "Jean",
    "transporteurId": 1,
    "transporteurNom": "Martin",
    "transporteurPrenom": "Pierre",
    "dateMission": "2025-12-15T10:00:00",
    "lieuDepart": "123 Rue de Paris, Lyon",
    "lieuArrivee": "456 Avenue des Champs, Paris",
    "statut": "EN_ATTENTE",
    "dateCreation": "2025-12-01T14:30:00",
    "description": "Transport de meubles"
  },
  {
    "idMission": 2,
    "clientId": 5,
    "clientNom": "Dupont",
    "clientPrenom": "Jean",
    "transporteurId": 2,
    "transporteurNom": "Bernard",
    "transporteurPrenom": "Sophie",
    "dateMission": "2025-12-20T14:00:00",
    "lieuDepart": "789 Boulevard Victor Hugo, Marseille",
    "lieuArrivee": "321 Rue de la République, Nice",
    "statut": "TERMINEE",
    "dateCreation": "2025-11-28T09:15:00",
    "description": "Livraison urgente"
  }
]
```

**Use Case:**
Display mission history for the client with filtering by status.

---

### 4. Get Transporteur's Missions
**Endpoint:** `GET /api/missions/transporteur`  
**Authentication:** Required (ROLE_TRANSPORTEUR)  
**Description:** Get all missions assigned to the logged-in transporteur

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Success Response (200):**
```json
[
  {
    "idMission": 3,
    "clientId": 7,
    "clientNom": "Moreau",
    "clientPrenom": "Alice",
    "transporteurId": 1,
    "transporteurNom": "Martin",
    "transporteurPrenom": "Pierre",
    "dateMission": "2025-12-10T09:00:00",
    "lieuDepart": "50 Avenue de la Gare, Toulouse",
    "lieuArrivee": "100 Rue du Commerce, Bordeaux",
    "statut": "ACCEPTEE",
    "dateCreation": "2025-12-02T11:00:00",
    "description": "Transport de documents"
  }
]
```

**Use Case:**
Display pending and active missions for the transporteur.

---

### 5. Get Mission by ID
**Endpoint:** `GET /api/missions/{id}`  
**Authentication:** Required (ROLE_CLIENT or ROLE_TRANSPORTEUR)  
**Description:** Get details of a specific mission

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Path Parameters:**
- `id`: Mission ID (Long)

**Success Response (200):**
```json
{
  "idMission": 1,
  "clientId": 5,
  "clientNom": "Dupont",
  "clientPrenom": "Jean",
  "transporteurId": 1,
  "transporteurNom": "Martin",
  "transporteurPrenom": "Pierre",
  "dateMission": "2025-12-15T10:00:00",
  "lieuDepart": "123 Rue de Paris, Lyon",
  "lieuArrivee": "456 Avenue des Champs, Paris",
  "statut": "EN_ATTENTE",
  "dateCreation": "2025-12-01T14:30:00",
  "description": "Transport de meubles"
}
```

**Error Response (400):**
```json
"Vous n'avez pas accès à cette mission"
```

**Security:**
Only the client who created the mission or the assigned transporteur can view it.

---

### 6. Update Mission Status
**Endpoint:** `PUT /api/missions/{id}/statut`  
**Authentication:** Required (ROLE_TRANSPORTEUR)  
**Description:** Update the status of a mission (transporteur only)

**Headers:**
```
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

**Path Parameters:**
- `id`: Mission ID (Long)

**Request Body:**
```json
{
  "statut": "ACCEPTEE"
}
```

**Valid Status Values:**
- `ACCEPTEE` - Transporteur accepts the mission
- `EN_COURS` - Mission is in progress
- `TERMINEE` - Mission completed

**Success Response (200):**
```json
{
  "idMission": 1,
  "clientId": 5,
  "clientNom": "Dupont",
  "clientPrenom": "Jean",
  "transporteurId": 1,
  "transporteurNom": "Martin",
  "transporteurPrenom": "Pierre",
  "dateMission": "2025-12-15T10:00:00",
  "lieuDepart": "123 Rue de Paris, Lyon",
  "lieuArrivee": "456 Avenue des Champs, Paris",
  "statut": "ACCEPTEE",
  "dateCreation": "2025-12-01T14:30:00",
  "description": "Transport de meubles"
}
```

**Error Responses:**

*Not the assigned transporteur (400):*
```json
"Vous n'êtes pas le transporteur de cette mission"
```

*Invalid status (400):*
```json
"Statut invalide: INVALID_STATUS"
```

---

### 7. Cancel Mission
**Endpoint:** `PUT /api/missions/{id}/annuler`  
**Authentication:** Required (ROLE_CLIENT)  
**Description:** Cancel a mission (client only)

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Path Parameters:**
- `id`: Mission ID (Long)

**Success Response (200):**
```json
{
  "idMission": 1,
  "clientId": 5,
  "clientNom": "Dupont",
  "clientPrenom": "Jean",
  "transporteurId": 1,
  "transporteurNom": "Martin",
  "transporteurPrenom": "Pierre",
  "dateMission": "2025-12-15T10:00:00",
  "lieuDepart": "123 Rue de Paris, Lyon",
  "lieuArrivee": "456 Avenue des Champs, Paris",
  "statut": "ANNULEE",
  "dateCreation": "2025-12-01T14:30:00",
  "description": "Transport de meubles"
}
```

**Error Responses:**

*Not the mission creator (400):*
```json
"Vous n'êtes pas le client de cette mission"
```

*Mission already completed (400):*
```json
"Impossible d'annuler une mission terminée"
```

---

## Data Models

### MissionRequest (Create Mission)
```typescript
interface MissionRequest {
  transporteurId: number;        // Required
  dateMission: string;           // Required, ISO 8601 format
  lieuDepart: string;            // Required
  lieuArrivee: string;           // Required
  description?: string;          // Optional
}
```

### MissionResponse
```typescript
interface MissionResponse {
  idMission: number;
  clientId: number;
  clientNom: string;
  clientPrenom: string;
  transporteurId: number;
  transporteurNom: string;
  transporteurPrenom: string;
  dateMission: string;           // ISO 8601 format
  lieuDepart: string;
  lieuArrivee: string;
  statut: MissionStatus;
  dateCreation: string;          // ISO 8601 format
  description: string;
}

type MissionStatus = 
  | 'EN_ATTENTE' 
  | 'ACCEPTEE' 
  | 'EN_COURS' 
  | 'TERMINEE' 
  | 'ANNULEE';
```

### TransporteurAvailableResponse
```typescript
interface TransporteurAvailableResponse {
  idTransporteur: number;
  nom: string;
  prenom: string;
  telephone: string;
  localisation: string;
  noteMoyenne: number;          // 0.0 to 5.0
  disponible: boolean;
}
```

### StatusUpdateRequest
```typescript
interface StatusUpdateRequest {
  statut: 'ACCEPTEE' | 'EN_COURS' | 'TERMINEE';
}
```

---

## Frontend Implementation Guide

### 1. Create Mission Form (Client Side)

```typescript
// mission-create.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MissionService } from './services/mission.service';

@Component({
  selector: 'app-mission-create',
  templateUrl: './mission-create.component.html'
})
export class MissionCreateComponent implements OnInit {
  
  missionForm: FormGroup;
  availableTransporteurs: TransporteurAvailableResponse[] = [];
  isLoading = false;
  
  constructor(
    private fb: FormBuilder,
    private missionService: MissionService
  ) {
    this.missionForm = this.fb.group({
      transporteurId: ['', Validators.required],
      dateMission: ['', Validators.required],
      lieuDepart: ['', Validators.required],
      lieuArrivee: ['', Validators.required],
      description: ['']
    });
  }
  
  ngOnInit(): void {
    this.loadAvailableTransporteurs();
  }
  
  loadAvailableTransporteurs(): void {
    this.missionService.getAvailableTransporteurs().subscribe({
      next: (transporteurs) => {
        this.availableTransporteurs = transporteurs;
      },
      error: (err) => {
        console.error('Error loading transporteurs:', err);
      }
    });
  }
  
  onSubmit(): void {
    if (this.missionForm.valid) {
      this.isLoading = true;
      
      const request: MissionRequest = {
        ...this.missionForm.value,
        // Convert date to ISO 8601 format
        dateMission: new Date(this.missionForm.value.dateMission).toISOString()
      };
      
      this.missionService.createMission(request).subscribe({
        next: (mission) => {
          alert('Mission créée avec succès!');
          this.missionForm.reset();
          // Navigate to missions list or show success message
        },
        error: (err) => {
          alert('Erreur: ' + err.error);
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    }
  }
}
```

### 2. Mission Service

```typescript
// mission.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MissionService {
  
  private apiUrl = 'http://localhost:8080/api/missions';
  
  constructor(private http: HttpClient) {}
  
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }
  
  getAvailableTransporteurs(): Observable<TransporteurAvailableResponse[]> {
    return this.http.get<TransporteurAvailableResponse[]>(
      `${this.apiUrl}/transporteurs/disponibles`,
      { headers: this.getHeaders() }
    );
  }
  
  createMission(request: MissionRequest): Observable<MissionResponse> {
    return this.http.post<MissionResponse>(
      this.apiUrl,
      request,
      { headers: this.getHeaders() }
    );
  }
  
  getClientMissions(): Observable<MissionResponse[]> {
    return this.http.get<MissionResponse[]>(
      `${this.apiUrl}/client`,
      { headers: this.getHeaders() }
    );
  }
  
  getTransporteurMissions(): Observable<MissionResponse[]> {
    return this.http.get<MissionResponse[]>(
      `${this.apiUrl}/transporteur`,
      { headers: this.getHeaders() }
    );
  }
  
  getMissionById(id: number): Observable<MissionResponse> {
    return this.http.get<MissionResponse>(
      `${this.apiUrl}/${id}`,
      { headers: this.getHeaders() }
    );
  }
  
  updateMissionStatus(id: number, statut: string): Observable<MissionResponse> {
    return this.http.put<MissionResponse>(
      `${this.apiUrl}/${id}/statut`,
      { statut },
      { headers: this.getHeaders() }
    );
  }
  
  cancelMission(id: number): Observable<MissionResponse> {
    return this.http.put<MissionResponse>(
      `${this.apiUrl}/${id}/annuler`,
      null,
      { headers: this.getHeaders() }
    );
  }
}
```

### 3. HTML Template Example

```html
<!-- mission-create.component.html -->
<div class="mission-create-container">
  <h2>Créer une nouvelle mission</h2>
  
  <form [formGroup]="missionForm" (ngSubmit)="onSubmit()">
    
    <!-- Date and Time -->
    <div class="form-group">
      <label for="dateMission">Date et heure de la mission *</label>
      <input 
        type="datetime-local" 
        id="dateMission" 
        formControlName="dateMission"
        class="form-control">
    </div>
    
    <!-- Pickup Location -->
    <div class="form-group">
      <label for="lieuDepart">Lieu de départ *</label>
      <input 
        type="text" 
        id="lieuDepart" 
        formControlName="lieuDepart"
        placeholder="123 Rue de Paris, Lyon"
        class="form-control">
    </div>
    
    <!-- Destination -->
    <div class="form-group">
      <label for="lieuArrivee">Lieu d'arrivée *</label>
      <input 
        type="text" 
        id="lieuArrivee" 
        formControlName="lieuArrivee"
        placeholder="456 Avenue des Champs, Paris"
        class="form-control">
    </div>
    
    <!-- Description -->
    <div class="form-group">
      <label for="description">Description (optionnel)</label>
      <textarea 
        id="description" 
        formControlName="description"
        rows="3"
        placeholder="Détails supplémentaires..."
        class="form-control"></textarea>
    </div>
    
    <!-- Transporteur Selection -->
    <div class="form-group">
      <label for="transporteurId">Choisir un transporteur *</label>
      <select 
        id="transporteurId" 
        formControlName="transporteurId"
        class="form-control">
        <option value="">-- Sélectionner --</option>
        <option 
          *ngFor="let transporteur of availableTransporteurs" 
          [value]="transporteur.idTransporteur">
          {{ transporteur.prenom }} {{ transporteur.nom }} - 
          {{ transporteur.localisation }} 
          (Note: {{ transporteur.noteMoyenne }}/5)
        </option>
      </select>
    </div>
    
    <!-- Submit Button -->
    <button 
      type="submit" 
      [disabled]="!missionForm.valid || isLoading"
      class="btn btn-primary">
      {{ isLoading ? 'Création en cours...' : 'Créer la mission' }}
    </button>
  </form>
</div>
```

### 4. Mission List Component

```typescript
// mission-list.component.ts
import { Component, OnInit } from '@angular/core';
import { MissionService } from './services/mission.service';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-mission-list',
  templateUrl: './mission-list.component.html'
})
export class MissionListComponent implements OnInit {
  
  missions: MissionResponse[] = [];
  isClient: boolean = false;
  isTransporteur: boolean = false;
  
  constructor(
    private missionService: MissionService,
    private authService: AuthService
  ) {}
  
  ngOnInit(): void {
    const role = this.authService.getUserRole();
    this.isClient = role === 'CLIENT';
    this.isTransporteur = role === 'TRANSPORTEUR';
    
    this.loadMissions();
  }
  
  loadMissions(): void {
    if (this.isClient) {
      this.missionService.getClientMissions().subscribe({
        next: (missions) => {
          this.missions = missions;
        }
      });
    } else if (this.isTransporteur) {
      this.missionService.getTransporteurMissions().subscribe({
        next: (missions) => {
          this.missions = missions;
        }
      });
    }
  }
  
  getStatusBadgeClass(statut: string): string {
    switch (statut) {
      case 'EN_ATTENTE': return 'badge-warning';
      case 'ACCEPTEE': return 'badge-info';
      case 'EN_COURS': return 'badge-primary';
      case 'TERMINEE': return 'badge-success';
      case 'ANNULEE': return 'badge-danger';
      default: return 'badge-secondary';
    }
  }
  
  acceptMission(missionId: number): void {
    this.missionService.updateMissionStatus(missionId, 'ACCEPTEE').subscribe({
      next: () => {
        this.loadMissions();
      }
    });
  }
  
  startMission(missionId: number): void {
    this.missionService.updateMissionStatus(missionId, 'EN_COURS').subscribe({
      next: () => {
        this.loadMissions();
      }
    });
  }
  
  completeMission(missionId: number): void {
    this.missionService.updateMissionStatus(missionId, 'TERMINEE').subscribe({
      next: () => {
        this.loadMissions();
      }
    });
  }
  
  cancelMission(missionId: number): void {
    if (confirm('Êtes-vous sûr de vouloir annuler cette mission?')) {
      this.missionService.cancelMission(missionId).subscribe({
        next: () => {
          this.loadMissions();
        }
      });
    }
  }
}
```

---

## User Stories

### As a Client:
1. **Create Mission**
   - I want to see available transporters
   - I want to fill a form with date, from, to
   - I want to select a transporteur
   - I want to submit and create the mission

2. **View My Missions**
   - I want to see all my missions
   - I want to see the status of each mission
   - I want to filter by status

3. **Cancel Mission**
   - I want to cancel a mission if not completed
   - I want confirmation before cancelling

### As a Transporteur:
1. **View Assigned Missions**
   - I want to see missions assigned to me
   - I want to see mission details (when, where, client info)

2. **Manage Missions**
   - I want to accept pending missions
   - I want to mark missions as in progress
   - I want to mark missions as completed

3. **Toggle Availability**
   - I want to mark myself as available/unavailable
   - When unavailable, I should not appear in client's list

---

## Testing Scenarios

### Test 1: Create Mission Flow
1. Login as CLIENT
2. GET `/api/missions/transporteurs/disponibles` → Should return list
3. POST `/api/missions` with valid data → Should return created mission
4. GET `/api/missions/client` → Should include the new mission

### Test 2: Transporteur Mission Management
1. Login as TRANSPORTEUR
2. GET `/api/missions/transporteur` → Should return assigned missions
3. PUT `/api/missions/{id}/statut` with `ACCEPTEE` → Should update status
4. PUT `/api/missions/{id}/statut` with `TERMINEE` → Should complete mission

### Test 3: Dashboard Statistics
1. Login as CLIENT
2. Create 3 missions
3. GET `/api/profil/client` → Should show real counts
4. Complete 1 mission
5. GET `/api/profil/client` → Counts should update

### Test 4: Security
1. Login as CLIENT
2. Try to access `/api/missions/transporteur` → Should get 403
3. Try to update mission status → Should get 403

### Test 5: Cancel Mission
1. Login as CLIENT
2. Create a mission
3. PUT `/api/missions/{id}/annuler` → Should set status to ANNULEE
4. Try to cancel a TERMINEE mission → Should get error

---

## Notes for Frontend Team

### Date Format
- Backend expects: `2025-12-15T10:00:00` (ISO 8601 without timezone)
- Use `new Date().toISOString()` or format accordingly

### Error Handling
- Always check for 400 (business logic errors)
- Display error messages from response body
- Handle 403 (permission denied) by redirecting to unauthorized page

### Loading States
- Show spinner when fetching available transporteurs
- Disable submit button while creating mission
- Show loading indicator when fetching missions list

### Real-time Updates
- Consider polling `/api/missions/client` or `/api/missions/transporteur` every 30 seconds
- Or implement WebSocket for real-time notifications (future enhancement)

### Validation
- Validate date is in the future
- Validate addresses are not empty
- Show user-friendly error messages

---

## Summary

✅ **7 new endpoints** added for mission management  
✅ **Role-based access control** implemented  
✅ **Real mission counts** on dashboard  
✅ **Complete CRUD** operations for missions  
✅ **Status workflow** (EN_ATTENTE → ACCEPTEE → EN_COURS → TERMINEE)  

**Next Steps:**
1. Implement frontend forms and components
2. Test all endpoints with Postman
3. Add mission filtering and sorting
4. Consider adding notifications (future)
5. Add mission rating system (future)

---

**Generated:** December 1, 2025  
**Backend Version:** 1.0  
**Base URL:** http://localhost:8080
