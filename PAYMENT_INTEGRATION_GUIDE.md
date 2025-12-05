# 💳 PAYMENT SYSTEM - INTEGRATION GUIDE FOR UI TEAM

## 📋 Table of Contents
1. [Overview](#overview)
2. [Payment Workflow](#payment-workflow)
3. [Mission Status Flow](#mission-status-flow)
4. [API Endpoints](#api-endpoints)
5. [Data Models](#data-models)
6. [Angular Implementation Examples](#angular-implementation-examples)
7. [Testing Workflow](#testing-workflow)
8. [Common Scenarios](#common-scenarios)
9. [Error Handling](#error-handling)

---

## 🎯 Overview

The payment system implements a **price proposal workflow** before mission acceptance:

1. **Transporteur** proposes a price for the mission
2. **Client** reviews and confirms the price
3. **Client** pays using credit card
4. **Mission** is automatically accepted and marked as paid
5. **Transporteur** can start the mission

### Key Features
- ✅ Price proposal by transporteur
- ✅ Price confirmation by client
- ✅ Secure credit card payment (only last 4 digits stored)
- ✅ Transaction ID generation
- ✅ Payment status verification
- ✅ Automatic mission acceptance after payment

---

## 🔄 Payment Workflow

```
┌─────────────────────────────────────────────────────────────────┐
│                     PAYMENT WORKFLOW                            │
└─────────────────────────────────────────────────────────────────┘

1. CLIENT creates mission
   └─> Mission status: EN_ATTENTE
       └─> proposedPrice: null
       └─> priceConfirmed: false
       └─> isPaid: false

2. TRANSPORTEUR proposes price
   POST /api/missions/{id}/propose-price
   └─> Mission status: PRIX_PROPOSE
       └─> proposedPrice: 150.00 (example)
       └─> priceConfirmed: false
       └─> isPaid: false

3. CLIENT confirms price
   POST /api/missions/{id}/confirm-price
   └─> Mission status: PRIX_CONFIRME
       └─> proposedPrice: 150.00
       └─> priceConfirmed: true
       └─> isPaid: false

4. CLIENT pays with credit card
   POST /api/payment/process
   └─> Mission status: ACCEPTEE
       └─> proposedPrice: 150.00
       └─> priceConfirmed: true
       └─> isPaid: true
       └─> Payment record created with transaction ID

5. TRANSPORTEUR starts mission
   PUT /api/missions/{id}/statut
   └─> Mission status: EN_COURS
       └─> (Can only start if isPaid = true)
```

---

## 📊 Mission Status Flow

### Complete Status Lifecycle

```
EN_ATTENTE (Waiting for price proposal)
    ↓
    │ Transporteur proposes price
    ↓
PRIX_PROPOSE (Price proposed, waiting for client confirmation)
    ↓
    │ Client confirms price
    ↓
PRIX_CONFIRME (Price confirmed, waiting for payment)
    ↓
    │ Client pays with credit card
    ↓
ACCEPTEE (Payment completed, mission accepted)
    ↓
    │ Transporteur starts mission (isPaid must be true)
    ↓
EN_COURS (Mission in progress)
    ↓
    │ Mission completed or cancelled
    ↓
TERMINEE / ANNULEE
```

### Mission Fields

| Field | Type | Description | Default |
|-------|------|-------------|---------|
| `proposedPrice` | Double | Price proposed by transporteur | null |
| `priceConfirmed` | Boolean | Client confirmed the price | false |
| `isPaid` | Boolean | Payment completed | false |

---

## 🔌 API Endpoints

### 1. Propose Price (Transporteur Only)

**Endpoint:** `POST /api/missions/{id}/propose-price`

**Authorization:** `ROLE_TRANSPORTEUR` (JWT token required)

**Request Body:**
```json
{
  "proposedPrice": 150.00
}
```

**Success Response (200 OK):**
```json
{
  "idMission": 1,
  "clientId": 5,
  "clientNom": "Dupont",
  "clientPrenom": "Jean",
  "transporteurId": 3,
  "transporteurNom": "Ben Ali",
  "transporteurPrenom": "Ahmed",
  "dateMission": "2024-12-15T10:00:00",
  "lieuDepart": "Tunis",
  "lieuArrivee": "Sousse",
  "statut": "PRIX_PROPOSE",
  "dateCreation": "2024-12-01T08:30:00",
  "description": "Transport de meubles",
  "proposedPrice": 150.00,
  "priceConfirmed": false,
  "isPaid": false
}
```

**Error Responses:**
- `400 Bad Request` - "Vous n'êtes pas le transporteur de cette mission"
- `400 Bad Request` - "Impossible de proposer un prix pour cette mission" (status not EN_ATTENTE)
- `400 Bad Request` - "Prix invalide" (price <= 0 or null)

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/missions/1/propose-price \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"proposedPrice": 150.00}'
```

---

### 2. Confirm Price (Client Only)

**Endpoint:** `POST /api/missions/{id}/confirm-price`

**Authorization:** `ROLE_CLIENT` (JWT token required)

**Request Body:** None

**Success Response (200 OK):**
```json
{
  "idMission": 1,
  "clientId": 5,
  "clientNom": "Dupont",
  "clientPrenom": "Jean",
  "transporteurId": 3,
  "transporteurNom": "Ben Ali",
  "transporteurPrenom": "Ahmed",
  "dateMission": "2024-12-15T10:00:00",
  "lieuDepart": "Tunis",
  "lieuArrivee": "Sousse",
  "statut": "PRIX_CONFIRME",
  "dateCreation": "2024-12-01T08:30:00",
  "description": "Transport de meubles",
  "proposedPrice": 150.00,
  "priceConfirmed": true,
  "isPaid": false
}
```

**Error Responses:**
- `400 Bad Request` - "Vous n'êtes pas le client de cette mission"
- `400 Bad Request` - "Aucun prix proposé pour cette mission" (status not PRIX_PROPOSE)
- `400 Bad Request` - "Prix non défini"

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/missions/1/confirm-price \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3. Process Payment (Client Only)

**Endpoint:** `POST /api/payment/process`

**Authorization:** `ROLE_CLIENT` (JWT token required)

**Request Body:**
```json
{
  "missionId": 1,
  "cardNumber": "4532123456789876",
  "cardHolderName": "JEAN DUPONT",
  "expiryMonth": "12",
  "expiryYear": "2026",
  "cvv": "123",
  "amount": 150.00
}
```

**Success Response (200 OK):**
```json
{
  "paymentId": 1,
  "missionId": 1,
  "amount": 150.00,
  "transactionId": "TXN-A7B9C2D4",
  "paymentStatus": "COMPLETED",
  "paymentDate": "2024-12-01T09:15:30",
  "cardLastFour": "9876",
  "message": "Paiement effectué avec succès"
}
```

**Error Responses:**
- `400 Bad Request` - "Mission non trouvée"
- `400 Bad Request` - "Vous n'êtes pas le client de cette mission"
- `400 Bad Request` - "Le prix doit être confirmé avant le paiement" (status not PRIX_CONFIRME)
- `400 Bad Request` - "Cette mission a déjà été payée"
- `400 Bad Request` - "Le montant ne correspond pas au prix proposé"

**Security Notes:**
- ⚠️ **NEVER** store the full credit card number in your frontend
- ✅ Only the **last 4 digits** are stored in the database
- ✅ Transaction ID is generated by the backend (format: TXN-XXXXXXXX)
- ✅ Payment simulation only - integrate real payment gateway in production

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/payment/process \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "missionId": 1,
    "cardNumber": "4532123456789876",
    "cardHolderName": "JEAN DUPONT",
    "expiryMonth": "12",
    "expiryYear": "2026",
    "cvv": "123",
    "amount": 150.00
  }'
```

---

### 4. Check Payment Status

**Endpoint:** `GET /api/payment/status/{missionId}`

**Authorization:** `ROLE_CLIENT` or `ROLE_TRANSPORTEUR` (JWT token required)

**Success Response (200 OK):**

**When Paid:**
```json
{
  "missionId": 1,
  "isPaid": true,
  "amount": 150.00,
  "paymentStatus": "COMPLETED",
  "message": "Mission payée"
}
```

**When Not Paid:**
```json
{
  "missionId": 1,
  "isPaid": false,
  "amount": null,
  "paymentStatus": null,
  "message": "Mission non payée"
}
```

**Error Responses:**
- `404 Not Found` - "Mission non trouvée"

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/payment/status/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 5. Update Mission Status (Updated for Payment Validation)

**Endpoint:** `PUT /api/missions/{id}/statut`

**Authorization:** `ROLE_TRANSPORTEUR` (JWT token required)

**Request Body:**
```json
{
  "statut": "EN_COURS"
}
```

**Success Response (200 OK):**
```json
{
  "idMission": 1,
  "statut": "EN_COURS",
  "isPaid": true,
  ...
}
```

**Error Responses:**
- `400 Bad Request` - "La mission doit être payée avant de pouvoir commencer" 
  (Attempting to set EN_COURS when isPaid = false)

**Important:** Transporteur can ONLY start the mission (EN_COURS) if `isPaid = true`

---

## 📦 Data Models

### PriceProposalRequest

```typescript
interface PriceProposalRequest {
  proposedPrice: number;  // Must be > 0
}
```

### PaymentRequest

```typescript
interface PaymentRequest {
  missionId: number;
  cardNumber: string;      // 16 digits
  cardHolderName: string;  // Uppercase recommended
  expiryMonth: string;     // "01" to "12"
  expiryYear: string;      // "2024", "2025", etc.
  cvv: string;             // 3-4 digits
  amount: number;          // Must match proposedPrice
}
```

### PaymentResponse

```typescript
interface PaymentResponse {
  paymentId: number;
  missionId: number;
  amount: number;
  transactionId: string;      // Format: "TXN-XXXXXXXX"
  paymentStatus: string;      // "COMPLETED", "PENDING", "FAILED"
  paymentDate: string;        // ISO 8601 format
  cardLastFour: string;       // Last 4 digits only
  message: string;
}
```

### PaymentStatusResponse

```typescript
interface PaymentStatusResponse {
  missionId: number;
  isPaid: boolean;
  amount: number | null;
  paymentStatus: string | null;
  message: string;
}
```

### MissionResponse (Updated)

```typescript
interface MissionResponse {
  idMission: number;
  clientId: number;
  clientNom: string;
  clientPrenom: string;
  transporteurId: number;
  transporteurNom: string;
  transporteurPrenom: string;
  dateMission: string;       // ISO 8601 format
  lieuDepart: string;
  lieuArrivee: string;
  statut: string;            // Mission status
  dateCreation: string;      // ISO 8601 format
  description: string;
  
  // New payment fields
  proposedPrice: number | null;
  priceConfirmed: boolean;
  isPaid: boolean;
}
```

---

## 🅰️ Angular Implementation Examples

### 1. Payment Service

Create `src/app/services/payment.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PriceProposalRequest {
  proposedPrice: number;
}

export interface PaymentRequest {
  missionId: number;
  cardNumber: string;
  cardHolderName: string;
  expiryMonth: string;
  expiryYear: string;
  cvv: string;
  amount: number;
}

export interface PaymentResponse {
  paymentId: number;
  missionId: number;
  amount: number;
  transactionId: string;
  paymentStatus: string;
  paymentDate: string;
  cardLastFour: string;
  message: string;
}

export interface PaymentStatusResponse {
  missionId: number;
  isPaid: boolean;
  amount: number | null;
  paymentStatus: string | null;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  // Transporteur proposes price
  proposePrice(missionId: number, request: PriceProposalRequest): Observable<any> {
    return this.http.post(
      `${this.apiUrl}/missions/${missionId}/propose-price`,
      request,
      { headers: this.getHeaders() }
    );
  }

  // Client confirms price
  confirmPrice(missionId: number): Observable<any> {
    return this.http.post(
      `${this.apiUrl}/missions/${missionId}/confirm-price`,
      {},
      { headers: this.getHeaders() }
    );
  }

  // Client processes payment
  processPayment(request: PaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/payment/process`,
      request,
      { headers: this.getHeaders() }
    );
  }

  // Check payment status
  checkPaymentStatus(missionId: number): Observable<PaymentStatusResponse> {
    return this.http.get<PaymentStatusResponse>(
      `${this.apiUrl}/payment/status/${missionId}`,
      { headers: this.getHeaders() }
    );
  }
}
```

---

### 2. Transporteur Component - Propose Price

Create `src/app/components/propose-price/propose-price.component.ts`:

```typescript
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentService } from '../../services/payment.service';
import { MissionService } from '../../services/mission.service';

@Component({
  selector: 'app-propose-price',
  templateUrl: './propose-price.component.html'
})
export class ProposePriceComponent implements OnInit {
  missionId: number;
  mission: any;
  proposedPrice: number = 0;
  loading: boolean = false;
  error: string = '';
  success: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService,
    private missionService: MissionService
  ) {}

  ngOnInit(): void {
    this.missionId = +this.route.snapshot.paramMap.get('id')!;
    this.loadMission();
  }

  loadMission(): void {
    this.missionService.getMissionById(this.missionId).subscribe({
      next: (data) => {
        this.mission = data;
        if (data.statut !== 'EN_ATTENTE') {
          this.error = 'Cette mission n\'est plus en attente';
        }
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement de la mission';
      }
    });
  }

  onSubmit(): void {
    if (this.proposedPrice <= 0) {
      this.error = 'Le prix doit être supérieur à 0';
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';

    const request = { proposedPrice: this.proposedPrice };

    this.paymentService.proposePrice(this.missionId, request).subscribe({
      next: (response) => {
        this.loading = false;
        this.success = 'Prix proposé avec succès!';
        setTimeout(() => {
          this.router.navigate(['/transporteur/missions']);
        }, 2000);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Erreur lors de la proposition du prix';
      }
    });
  }
}
```

**HTML Template** (`propose-price.component.html`):

```html
<div class="container mt-4">
  <h2>Proposer un Prix</h2>
  
  <div *ngIf="mission" class="card mb-3">
    <div class="card-body">
      <h5>Mission Details</h5>
      <p><strong>Client:</strong> {{ mission.clientNom }} {{ mission.clientPrenom }}</p>
      <p><strong>De:</strong> {{ mission.lieuDepart }}</p>
      <p><strong>À:</strong> {{ mission.lieuArrivee }}</p>
      <p><strong>Date:</strong> {{ mission.dateMission | date:'medium' }}</p>
      <p><strong>Description:</strong> {{ mission.description }}</p>
    </div>
  </div>

  <div *ngIf="error" class="alert alert-danger">{{ error }}</div>
  <div *ngIf="success" class="alert alert-success">{{ success }}</div>

  <form (ngSubmit)="onSubmit()">
    <div class="mb-3">
      <label for="price" class="form-label">Prix Proposé (TND)</label>
      <input 
        type="number" 
        class="form-control" 
        id="price"
        [(ngModel)]="proposedPrice"
        name="proposedPrice"
        min="1"
        step="0.01"
        required>
    </div>

    <button 
      type="submit" 
      class="btn btn-primary"
      [disabled]="loading || proposedPrice <= 0">
      <span *ngIf="loading" class="spinner-border spinner-border-sm me-2"></span>
      Proposer le Prix
    </button>
  </form>
</div>
```

---

### 3. Client Component - Confirm Price & Pay

Create `src/app/components/confirm-and-pay/confirm-and-pay.component.ts`:

```typescript
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentService, PaymentRequest } from '../../services/payment.service';
import { MissionService } from '../../services/mission.service';

@Component({
  selector: 'app-confirm-and-pay',
  templateUrl: './confirm-and-pay.component.html'
})
export class ConfirmAndPayComponent implements OnInit {
  missionId: number;
  mission: any;
  loading: boolean = false;
  error: string = '';
  success: string = '';
  
  // Payment form
  showPaymentForm: boolean = false;
  cardNumber: string = '';
  cardHolderName: string = '';
  expiryMonth: string = '';
  expiryYear: string = '';
  cvv: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentService,
    private missionService: MissionService
  ) {}

  ngOnInit(): void {
    this.missionId = +this.route.snapshot.paramMap.get('id')!;
    this.loadMission();
  }

  loadMission(): void {
    this.missionService.getMissionById(this.missionId).subscribe({
      next: (data) => {
        this.mission = data;
        
        // If price already confirmed, show payment form
        if (data.statut === 'PRIX_CONFIRME') {
          this.showPaymentForm = true;
        }
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement de la mission';
      }
    });
  }

  confirmPrice(): void {
    this.loading = true;
    this.error = '';
    this.success = '';

    this.paymentService.confirmPrice(this.missionId).subscribe({
      next: (response) => {
        this.loading = false;
        this.mission = response;
        this.showPaymentForm = true;
        this.success = 'Prix confirmé! Procédez au paiement.';
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Erreur lors de la confirmation du prix';
      }
    });
  }

  processPayment(): void {
    // Validate form
    if (!this.validatePaymentForm()) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';

    const paymentRequest: PaymentRequest = {
      missionId: this.missionId,
      cardNumber: this.cardNumber.replace(/\s/g, ''),
      cardHolderName: this.cardHolderName.toUpperCase(),
      expiryMonth: this.expiryMonth,
      expiryYear: this.expiryYear,
      cvv: this.cvv,
      amount: this.mission.proposedPrice
    };

    this.paymentService.processPayment(paymentRequest).subscribe({
      next: (response) => {
        this.loading = false;
        this.success = `Paiement réussi! Transaction: ${response.transactionId}`;
        
        // Clear sensitive data
        this.clearPaymentForm();
        
        setTimeout(() => {
          this.router.navigate(['/client/missions']);
        }, 3000);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Erreur lors du paiement';
      }
    });
  }

  validatePaymentForm(): boolean {
    // Card number validation (16 digits)
    const cardNumberClean = this.cardNumber.replace(/\s/g, '');
    if (!/^\d{16}$/.test(cardNumberClean)) {
      this.error = 'Numéro de carte invalide (16 chiffres requis)';
      return false;
    }

    // Cardholder name validation
    if (this.cardHolderName.trim().length < 3) {
      this.error = 'Nom du titulaire invalide';
      return false;
    }

    // Expiry validation
    const month = parseInt(this.expiryMonth);
    const year = parseInt(this.expiryYear);
    if (month < 1 || month > 12) {
      this.error = 'Mois d\'expiration invalide';
      return false;
    }
    if (year < new Date().getFullYear()) {
      this.error = 'Carte expirée';
      return false;
    }

    // CVV validation
    if (!/^\d{3,4}$/.test(this.cvv)) {
      this.error = 'CVV invalide (3-4 chiffres)';
      return false;
    }

    return true;
  }

  clearPaymentForm(): void {
    this.cardNumber = '';
    this.cardHolderName = '';
    this.expiryMonth = '';
    this.expiryYear = '';
    this.cvv = '';
  }

  formatCardNumber(): void {
    // Auto-format card number with spaces (XXXX XXXX XXXX XXXX)
    let value = this.cardNumber.replace(/\s/g, '');
    let formatted = value.match(/.{1,4}/g)?.join(' ') || value;
    this.cardNumber = formatted.substring(0, 19); // Max 16 digits + 3 spaces
  }
}
```

**HTML Template** (`confirm-and-pay.component.html`):

```html
<div class="container mt-4">
  <h2>Confirmer et Payer</h2>
  
  <div *ngIf="mission" class="card mb-3">
    <div class="card-body">
      <h5>Mission Details</h5>
      <p><strong>Transporteur:</strong> {{ mission.transporteurNom }} {{ mission.transporteurPrenom }}</p>
      <p><strong>De:</strong> {{ mission.lieuDepart }}</p>
      <p><strong>À:</strong> {{ mission.lieuArrivee }}</p>
      <p><strong>Date:</strong> {{ mission.dateMission | date:'medium' }}</p>
      <p><strong>Prix Proposé:</strong> <span class="h4 text-success">{{ mission.proposedPrice }} TND</span></p>
      <p><strong>Statut:</strong> 
        <span class="badge bg-info">{{ mission.statut }}</span>
      </p>
    </div>
  </div>

  <div *ngIf="error" class="alert alert-danger">{{ error }}</div>
  <div *ngIf="success" class="alert alert-success">{{ success }}</div>

  <!-- Step 1: Confirm Price -->
  <div *ngIf="!showPaymentForm && mission?.statut === 'PRIX_PROPOSE'" class="card">
    <div class="card-body">
      <h5>Étape 1: Confirmer le Prix</h5>
      <p>Êtes-vous d'accord avec le prix proposé de <strong>{{ mission.proposedPrice }} TND</strong>?</p>
      <button 
        class="btn btn-primary"
        (click)="confirmPrice()"
        [disabled]="loading">
        <span *ngIf="loading" class="spinner-border spinner-border-sm me-2"></span>
        Confirmer le Prix
      </button>
    </div>
  </div>

  <!-- Step 2: Payment Form -->
  <div *ngIf="showPaymentForm" class="card">
    <div class="card-body">
      <h5>Étape 2: Paiement</h5>
      
      <form (ngSubmit)="processPayment()">
        <div class="mb-3">
          <label for="cardNumber" class="form-label">Numéro de Carte</label>
          <input 
            type="text" 
            class="form-control" 
            id="cardNumber"
            [(ngModel)]="cardNumber"
            (input)="formatCardNumber()"
            name="cardNumber"
            placeholder="1234 5678 9012 3456"
            maxlength="19"
            required>
        </div>

        <div class="mb-3">
          <label for="cardHolderName" class="form-label">Nom du Titulaire</label>
          <input 
            type="text" 
            class="form-control" 
            id="cardHolderName"
            [(ngModel)]="cardHolderName"
            name="cardHolderName"
            placeholder="JEAN DUPONT"
            style="text-transform: uppercase;"
            required>
        </div>

        <div class="row">
          <div class="col-md-6 mb-3">
            <label for="expiryMonth" class="form-label">Mois d'Expiration</label>
            <select 
              class="form-select" 
              id="expiryMonth"
              [(ngModel)]="expiryMonth"
              name="expiryMonth"
              required>
              <option value="">--</option>
              <option value="01">01</option>
              <option value="02">02</option>
              <option value="03">03</option>
              <option value="04">04</option>
              <option value="05">05</option>
              <option value="06">06</option>
              <option value="07">07</option>
              <option value="08">08</option>
              <option value="09">09</option>
              <option value="10">10</option>
              <option value="11">11</option>
              <option value="12">12</option>
            </select>
          </div>

          <div class="col-md-6 mb-3">
            <label for="expiryYear" class="form-label">Année d'Expiration</label>
            <select 
              class="form-select" 
              id="expiryYear"
              [(ngModel)]="expiryYear"
              name="expiryYear"
              required>
              <option value="">--</option>
              <option value="2024">2024</option>
              <option value="2025">2025</option>
              <option value="2026">2026</option>
              <option value="2027">2027</option>
              <option value="2028">2028</option>
              <option value="2029">2029</option>
              <option value="2030">2030</option>
            </select>
          </div>
        </div>

        <div class="mb-3">
          <label for="cvv" class="form-label">CVV</label>
          <input 
            type="text" 
            class="form-control" 
            id="cvv"
            [(ngModel)]="cvv"
            name="cvv"
            placeholder="123"
            maxlength="4"
            pattern="\d{3,4}"
            style="width: 100px;"
            required>
          <small class="text-muted">3-4 chiffres au dos de la carte</small>
        </div>

        <div class="mb-3">
          <p class="h5">Montant à Payer: <strong class="text-success">{{ mission.proposedPrice }} TND</strong></p>
        </div>

        <button 
          type="submit" 
          class="btn btn-success btn-lg"
          [disabled]="loading">
          <span *ngIf="loading" class="spinner-border spinner-border-sm me-2"></span>
          Payer {{ mission.proposedPrice }} TND
        </button>
      </form>

      <div class="mt-3">
        <small class="text-muted">
          🔒 Paiement sécurisé. Vos informations sont protégées.
        </small>
      </div>
    </div>
  </div>
</div>
```

---

### 4. Payment Status Check Component

Create `src/app/components/payment-status/payment-status.component.ts`:

```typescript
import { Component, Input, OnInit } from '@angular/core';
import { PaymentService, PaymentStatusResponse } from '../../services/payment.service';

@Component({
  selector: 'app-payment-status',
  template: `
    <div *ngIf="status" class="payment-status">
      <div *ngIf="status.isPaid" class="alert alert-success">
        ✅ Mission Payée - {{ status.amount }} TND
        <br>
        <small>Statut: {{ status.paymentStatus }}</small>
      </div>
      <div *ngIf="!status.isPaid" class="alert alert-warning">
        ⚠️ Paiement en attente
      </div>
    </div>
  `
})
export class PaymentStatusComponent implements OnInit {
  @Input() missionId: number;
  status: PaymentStatusResponse | null = null;

  constructor(private paymentService: PaymentService) {}

  ngOnInit(): void {
    this.loadStatus();
  }

  loadStatus(): void {
    this.paymentService.checkPaymentStatus(this.missionId).subscribe({
      next: (data) => {
        this.status = data;
      },
      error: (err) => {
        console.error('Error loading payment status', err);
      }
    });
  }
}
```

**Usage in parent component:**

```html
<app-payment-status [missionId]="mission.idMission"></app-payment-status>
```

---

## ✅ Testing Workflow

### Complete End-to-End Test

Use these steps to test the entire workflow:

#### 1. Create Mission (Client)
```bash
POST http://localhost:8080/api/missions
Authorization: Bearer <CLIENT_TOKEN>

{
  "transporteurId": 3,
  "dateMission": "2024-12-15T10:00:00",
  "lieuDepart": "Tunis",
  "lieuArrivee": "Sousse",
  "description": "Transport de meubles"
}

Expected: Status 201, mission with statut = "EN_ATTENTE"
```

#### 2. Propose Price (Transporteur)
```bash
POST http://localhost:8080/api/missions/1/propose-price
Authorization: Bearer <TRANSPORTEUR_TOKEN>

{
  "proposedPrice": 150.00
}

Expected: Status 200, mission with statut = "PRIX_PROPOSE", proposedPrice = 150.00
```

#### 3. Confirm Price (Client)
```bash
POST http://localhost:8080/api/missions/1/confirm-price
Authorization: Bearer <CLIENT_TOKEN>

Expected: Status 200, mission with statut = "PRIX_CONFIRME", priceConfirmed = true
```

#### 4. Process Payment (Client)
```bash
POST http://localhost:8080/api/payment/process
Authorization: Bearer <CLIENT_TOKEN>

{
  "missionId": 1,
  "cardNumber": "4532123456789876",
  "cardHolderName": "JEAN DUPONT",
  "expiryMonth": "12",
  "expiryYear": "2026",
  "cvv": "123",
  "amount": 150.00
}

Expected: Status 200, PaymentResponse with transactionId, statut = "ACCEPTEE", isPaid = true
```

#### 5. Check Payment Status
```bash
GET http://localhost:8080/api/payment/status/1
Authorization: Bearer <CLIENT_TOKEN>

Expected: isPaid = true, amount = 150.00, paymentStatus = "COMPLETED"
```

#### 6. Start Mission (Transporteur)
```bash
PUT http://localhost:8080/api/missions/1/statut
Authorization: Bearer <TRANSPORTEUR_TOKEN>

{
  "statut": "EN_COURS"
}

Expected: Status 200, mission with statut = "EN_COURS"
```

---

## 🎬 Common Scenarios

### Scenario 1: Client Rejects Price

**Problem:** Client doesn't like the proposed price

**Solution:** Client can cancel the mission:

```bash
PUT /api/missions/{id}/annuler
Authorization: Bearer <CLIENT_TOKEN>

Result: Mission status → ANNULEE
```

### Scenario 2: Transporteur Changes Mind Before Proposal

**Problem:** Transporteur wants to cancel before proposing price

**Solution:** Transporteur can't cancel (not implemented). Only clients can cancel.

**Workaround:** Propose a very high price, client will likely refuse and cancel.

### Scenario 3: Payment Amount Mismatch

**Problem:** Client tries to pay different amount than proposed price

**Request:**
```json
{
  "missionId": 1,
  "amount": 100.00  // But proposedPrice is 150.00
  ...
}
```

**Response:** `400 Bad Request` - "Le montant ne correspond pas au prix proposé"

**Solution:** Always use `mission.proposedPrice` as the payment amount.

### Scenario 4: Transporteur Tries to Start Unpaid Mission

**Request:**
```bash
PUT /api/missions/1/statut
{ "statut": "EN_COURS" }
```

**Response:** `400 Bad Request` - "La mission doit être payée avant de pouvoir commencer"

**Solution:** Check `isPaid` flag before showing "Start Mission" button.

### Scenario 5: Duplicate Payment Attempt

**Problem:** Client clicks "Pay" button twice

**Response:** `400 Bad Request` - "Cette mission a déjà été payée"

**Solution:** Disable payment button after first successful payment, or redirect immediately.

---

## ⚠️ Error Handling

### Common Error Codes

| Code | Scenario | Message |
|------|----------|---------|
| 400 | Invalid price | "Prix invalide" |
| 400 | Wrong mission status | "Impossible de proposer un prix pour cette mission" |
| 400 | Not mission owner | "Vous n'êtes pas le transporteur/client de cette mission" |
| 400 | Already paid | "Cette mission a déjà été payée" |
| 400 | Amount mismatch | "Le montant ne correspond pas au prix proposé" |
| 400 | Cannot start unpaid | "La mission doit être payée avant de pouvoir commencer" |
| 401 | Invalid token | "Unauthorized" |
| 403 | Wrong role | "Access Denied" |
| 404 | Mission not found | "Mission non trouvée" |

### Error Handling in Angular

```typescript
this.paymentService.processPayment(request).subscribe({
  next: (response) => {
    // Success handling
    this.showSuccess(response.message);
  },
  error: (err) => {
    // Error handling
    if (err.status === 400) {
      this.showError(err.error.message || 'Requête invalide');
    } else if (err.status === 401) {
      this.router.navigate(['/login']);
    } else if (err.status === 404) {
      this.showError('Mission non trouvée');
    } else {
      this.showError('Erreur serveur, veuillez réessayer');
    }
  }
});
```

---

## 🔐 Security Recommendations

### ✅ DO:
- Store JWT token in `localStorage` or `sessionStorage`
- Clear sensitive payment data after submission
- Validate all form inputs before sending
- Use HTTPS in production
- Implement rate limiting on payment endpoint
- Log all payment transactions
- Display only last 4 digits of card number

### ❌ DON'T:
- Store full credit card numbers
- Log credit card details
- Display CVV after submission
- Allow payment without price confirmation
- Skip form validation
- Use HTTP in production

---

## 📞 Support & Questions

For any questions or issues:

1. Check this documentation first
2. Verify API endpoints are accessible: `http://localhost:8080/api/`
3. Check JWT token is valid and not expired
4. Verify user role matches endpoint requirements
5. Check network tab in browser DevTools for exact error responses

---

## 📝 Summary Checklist

Use this checklist when implementing the payment system:

**Backend (Already Implemented ✅):**
- [x] Mission entity updated with payment fields
- [x] Payment entity and repository created
- [x] PaymentService with payment processing logic
- [x] MissionService with price proposal/confirmation methods
- [x] PaymentController with REST endpoints
- [x] MissionController updated with price endpoints
- [x] Payment validation before mission start
- [x] Transaction ID generation

**Frontend (To Implement):**
- [ ] PaymentService in Angular
- [ ] Propose Price component (Transporteur)
- [ ] Confirm Price & Pay component (Client)
- [ ] Payment status check component
- [ ] Form validation for credit card inputs
- [ ] Error handling for all endpoints
- [ ] Success/error notifications
- [ ] Auto-formatting for card number input
- [ ] Secure data handling (clear sensitive data)
- [ ] Route guards based on user role
- [ ] Mission list filtering by payment status

---

**🎉 END OF PAYMENT INTEGRATION GUIDE 🎉**

This document provides everything needed to integrate the payment system with your Angular frontend. Follow the examples and adapt them to your specific UI/UX requirements.

**Database:** MySQL running on port **3307** (not default 3306)

**Backend:** Spring Boot 3.5.7 on port **8080**

**Frontend:** Angular on port **4200** (typical)

Good luck with the implementation! 🚀
