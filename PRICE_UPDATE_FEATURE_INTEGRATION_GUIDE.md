# Price Update Feature - Integration Guide for UI Team

## 📋 Overview
This document describes the new feature that allows **transporteurs** to modify the proposed price **before** the client confirms it. The system now tracks all price changes with complete history.

---

## 🎯 Feature Summary

### What's New?
- ✅ Transporteur can **update the proposed price** before client confirmation
- ✅ Complete **price change history** tracking
- ✅ Optional **reason** for price changes
- ✅ Automatic validation (only when status is `PRIX_PROPOSE`)
- ✅ Price history included in mission details

### User Flow
1. **Transporteur** proposes initial price → Mission status: `PRIX_PROPOSE`
2. **Transporteur** can update the price multiple times (optional reason)
3. Each change is saved in price history with timestamp
4. **Client** sees current price + full history when confirming
5. Once client confirms → Price locked (no more changes allowed)

---

## 🔧 New Endpoint

### PUT /api/missions/{id}/update-price
Update the proposed price for a mission (before client confirmation)

**Authorization:** Required - JWT Bearer Token with **TRANSPORTEUR** role

**URL Parameters:**
- `id` (Long) - Mission ID

**Request Body:**
```json
{
  "newPrice": 150.00,
  "reason": "Adjusted for fuel costs"
}
```

**Request Fields:**
| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `newPrice` | Double | ✅ Yes | Must be > 0 | The new price to propose |
| `reason` | String | ❌ No | - | Optional reason for the change |

**Success Response (200 OK):**
```json
{
  "idMission": 123,
  "clientId": 45,
  "clientNom": "Dupont",
  "clientPrenom": "Jean",
  "transporteurId": 67,
  "transporteurNom": "Martin",
  "transporteurPrenom": "Pierre",
  "dateMission": "2024-12-15T10:00:00",
  "lieuDepart": "Tunis",
  "lieuArrivee": "Sousse",
  "statut": "PRIX_PROPOSE",
  "dateCreation": "2024-12-01T08:30:00",
  "description": "Transport de marchandises",
  "proposedPrice": 150.00,
  "priceConfirmed": false,
  "isPaid": false,
  "priceHistory": [
    {
      "id": 2,
      "oldPrice": 120.00,
      "newPrice": 150.00,
      "changeReason": "Adjusted for fuel costs",
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
  ]
}
```

**Error Responses:**

**403 Forbidden:**
```json
"Vous n'êtes pas le transporteur de cette mission"
```

**400 Bad Request:**
```json
"Le prix ne peut plus être modifié (statut actuel: PRIX_CONFIRME)"
```
```json
"Le prix a déjà été confirmé par le client"
```
```json
"Prix invalide"
```
```json
"Mission non trouvée"
```

---

## 📊 Updated Data Structures

### MissionResponse (Updated)
```typescript
export interface MissionResponse {
  idMission: number;
  clientId: number;
  clientNom: string;
  clientPrenom: string;
  transporteurId: number;
  transporteurNom: string;
  transporteurPrenom: string;
  dateMission: string;
  lieuDepart: string;
  lieuArrivee: string;
  statut: string;
  dateCreation: string;
  description: string;
  proposedPrice: number | null;
  priceConfirmed: boolean;
  isPaid: boolean;
  priceHistory: PriceHistoryResponse[];  // ✨ NEW FIELD
}
```

### PriceHistoryResponse (New)
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

### UpdatePriceRequest (New)
```typescript
export interface UpdatePriceRequest {
  newPrice: number;
  reason?: string;  // Optional
}
```

---

## 🔄 Complete Payment Workflow

### Updated Mission Statuses
1. **EN_ATTENTE** - Waiting for price proposal
2. **PRIX_PROPOSE** - Price proposed (can be updated by transporteur)
3. **PRIX_CONFIRME** - Price confirmed by client (locked, no updates)
4. **ACCEPTEE** - Payment done
5. **EN_COURS** - In progress
6. **TERMINEE** - Completed
7. **ANNULEE** - Cancelled

### Workflow Diagram
```
┌─────────────────┐
│   EN_ATTENTE    │
└────────┬────────┘
         │ Transporteur proposes price
         ▼
┌─────────────────┐
│  PRIX_PROPOSE   │ ◄─┐
└────────┬────────┘    │
         │             │ Transporteur can update price
         │             │ (multiple times with reasons)
         │             └───────────────┐
         │                             │
         │ Client confirms            │
         ▼                             │
┌─────────────────┐                   │
│ PRIX_CONFIRME   │                   │
└────────┬────────┘                   │
         │ Client pays                │
         ▼                             │
┌─────────────────┐                   │
│    ACCEPTEE     │                   │
└─────────────────┘                   │
```

---

## 💻 Angular Implementation

### 1. Service Methods

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

  /**
   * Update the proposed price for a mission (Transporteur only)
   */
  updateProposedPrice(
    missionId: number, 
    newPrice: number, 
    reason?: string
  ): Observable<MissionResponse> {
    const token = localStorage.getItem('authToken');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    const body: UpdatePriceRequest = {
      newPrice: newPrice,
      reason: reason
    };

    return this.http.put<MissionResponse>(
      `${this.apiUrl}/${missionId}/update-price`,
      body,
      { headers }
    );
  }

  /**
   * Get mission details with price history
   */
  getMissionById(missionId: number): Observable<MissionResponse> {
    const token = localStorage.getItem('authToken');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.get<MissionResponse>(
      `${this.apiUrl}/${missionId}`,
      { headers }
    );
  }
}
```

---

### 2. Transporteur Component - Update Price Modal

**Component TypeScript:**
```typescript
import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MissionService } from '../services/mission.service';
import { MissionResponse } from '../models/mission-response.model';

@Component({
  selector: 'app-update-price-modal',
  templateUrl: './update-price-modal.component.html',
  styleUrls: ['./update-price-modal.component.css']
})
export class UpdatePriceModalComponent implements OnInit {
  @Input() mission!: MissionResponse;
  
  updatePriceForm!: FormGroup;
  loading = false;
  error: string | null = null;
  success = false;

  constructor(
    private fb: FormBuilder,
    private missionService: MissionService
  ) {}

  ngOnInit(): void {
    this.updatePriceForm = this.fb.group({
      newPrice: [
        this.mission.proposedPrice, 
        [Validators.required, Validators.min(0.01)]
      ],
      reason: ['']
    });
  }

  canUpdatePrice(): boolean {
    return this.mission.statut === 'PRIX_PROPOSE' && 
           !this.mission.priceConfirmed;
  }

  onSubmit(): void {
    if (this.updatePriceForm.invalid || !this.canUpdatePrice()) {
      return;
    }

    this.loading = true;
    this.error = null;

    const { newPrice, reason } = this.updatePriceForm.value;

    this.missionService.updateProposedPrice(
      this.mission.idMission,
      newPrice,
      reason || undefined
    ).subscribe({
      next: (updatedMission) => {
        this.success = true;
        this.loading = false;
        console.log('Price updated successfully:', updatedMission);
        
        // Update local mission data
        this.mission = updatedMission;
        
        // Close modal after 2 seconds
        setTimeout(() => {
          this.closeModal();
        }, 2000);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error || 'Erreur lors de la modification du prix';
        console.error('Error updating price:', err);
      }
    });
  }

  closeModal(): void {
    // Emit event or close modal logic
    // this.modalClosed.emit();
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString('fr-FR');
  }
}
```

**Component HTML:**
```html
<div class="modal-overlay">
  <div class="modal-content">
    <div class="modal-header">
      <h3>Modifier le prix proposé</h3>
      <button class="close-btn" (click)="closeModal()">&times;</button>
    </div>

    <div class="modal-body">
      <!-- Current Price Info -->
      <div class="current-price-info">
        <p><strong>Prix actuel:</strong> {{ mission.proposedPrice | number:'1.2-2' }} TND</p>
        <p><strong>Statut:</strong> {{ mission.statut }}</p>
      </div>

      <!-- Warning if cannot update -->
      <div *ngIf="!canUpdatePrice()" class="warning-message">
        ⚠️ Le prix ne peut plus être modifié (déjà confirmé par le client)
      </div>

      <!-- Update Form -->
      <form *ngIf="canUpdatePrice()" [formGroup]="updatePriceForm" (ngSubmit)="onSubmit()">
        <div class="form-group">
          <label for="newPrice">Nouveau prix (TND) *</label>
          <input 
            type="number" 
            id="newPrice" 
            formControlName="newPrice"
            class="form-control"
            step="0.01"
            min="0.01"
            placeholder="Entrez le nouveau prix">
          <div class="error" *ngIf="updatePriceForm.get('newPrice')?.invalid && updatePriceForm.get('newPrice')?.touched">
            Le prix doit être supérieur à 0
          </div>
        </div>

        <div class="form-group">
          <label for="reason">Raison du changement (optionnel)</label>
          <textarea 
            id="reason" 
            formControlName="reason"
            class="form-control"
            rows="3"
            placeholder="Ex: Ajustement pour coûts de carburant"></textarea>
        </div>

        <!-- Error Message -->
        <div *ngIf="error" class="error-message">
          {{ error }}
        </div>

        <!-- Success Message -->
        <div *ngIf="success" class="success-message">
          ✅ Prix mis à jour avec succès!
        </div>

        <!-- Action Buttons -->
        <div class="modal-actions">
          <button 
            type="button" 
            class="btn-secondary" 
            (click)="closeModal()"
            [disabled]="loading">
            Annuler
          </button>
          <button 
            type="submit" 
            class="btn-primary"
            [disabled]="loading || updatePriceForm.invalid">
            {{ loading ? 'Mise à jour...' : 'Confirmer' }}
          </button>
        </div>
      </form>

      <!-- Price History -->
      <div class="price-history-section" *ngIf="mission.priceHistory && mission.priceHistory.length > 0">
        <h4>Historique des prix</h4>
        <div class="history-timeline">
          <div 
            *ngFor="let history of mission.priceHistory" 
            class="history-item">
            <div class="history-icon">💰</div>
            <div class="history-details">
              <div class="price-change">
                <span *ngIf="history.oldPrice" class="old-price">
                  {{ history.oldPrice | number:'1.2-2' }} TND
                </span>
                <span class="arrow" *ngIf="history.oldPrice">→</span>
                <span class="new-price">{{ history.newPrice | number:'1.2-2' }} TND</span>
              </div>
              <div class="change-reason">{{ history.changeReason }}</div>
              <div class="change-meta">
                <span class="change-date">{{ formatDate(history.changeDate) }}</span>
                <span class="change-by">par {{ history.changedBy }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
```

**Component CSS:**
```css
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 2rem;
  cursor: pointer;
  color: #999;
  line-height: 1;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 1.5rem;
}

.current-price-info {
  background-color: #f0f4ff;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1.5rem;
}

.current-price-info p {
  margin: 0.5rem 0;
  color: #555;
}

.warning-message {
  background-color: #fff3cd;
  border: 1px solid #ffc107;
  color: #856404;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #333;
}

.form-control {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-control:focus {
  outline: none;
  border-color: #667eea;
}

.error {
  color: #dc3545;
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.error-message {
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}

.success-message {
  background-color: #d4edda;
  border: 1px solid #c3e6cb;
  color: #155724;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  text-align: center;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.btn-primary, .btn-secondary {
  flex: 1;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background-color: #667eea;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #5568d3;
}

.btn-primary:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: white;
  color: #667eea;
  border: 1px solid #667eea;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #f0f4ff;
}

/* Price History */
.price-history-section {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #eee;
}

.price-history-section h4 {
  margin-bottom: 1rem;
  color: #333;
}

.history-timeline {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.history-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background-color: #f9fafb;
  border-radius: 4px;
  border-left: 3px solid #667eea;
}

.history-icon {
  font-size: 1.5rem;
}

.history-details {
  flex: 1;
}

.price-change {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.old-price {
  text-decoration: line-through;
  color: #999;
}

.arrow {
  margin: 0 0.5rem;
  color: #667eea;
}

.new-price {
  color: #28a745;
}

.change-reason {
  color: #555;
  margin-bottom: 0.5rem;
  font-style: italic;
}

.change-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.875rem;
  color: #777;
}
```

---

### 3. Client Component - View Price History

```typescript
import { Component, Input } from '@angular/core';
import { MissionResponse, PriceHistoryResponse } from '../models/mission-response.model';

@Component({
  selector: 'app-price-history-display',
  templateUrl: './price-history-display.component.html',
  styleUrls: ['./price-history-display.component.css']
})
export class PriceHistoryDisplayComponent {
  @Input() mission!: MissionResponse;

  hasPriceHistory(): boolean {
    return this.mission.priceHistory && this.mission.priceHistory.length > 0;
  }

  getPriceChangeCount(): number {
    return this.mission.priceHistory?.length || 0;
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getPriceChangeIndicator(history: PriceHistoryResponse): string {
    if (!history.oldPrice) return 'new';
    return history.newPrice > history.oldPrice ? 'increase' : 'decrease';
  }
}
```

**HTML Template:**
```html
<div class="price-history-container" *ngIf="hasPriceHistory()">
  <div class="price-summary">
    <h4>Prix proposé: {{ mission.proposedPrice | number:'1.2-2' }} TND</h4>
    <p class="change-count">
      {{ getPriceChangeCount() }} modification(s) de prix
    </p>
  </div>

  <div class="history-list">
    <div 
      *ngFor="let history of mission.priceHistory; let i = index" 
      class="history-entry"
      [ngClass]="getPriceChangeIndicator(history)">
      
      <div class="entry-number">{{ i + 1 }}</div>
      
      <div class="entry-content">
        <div class="price-info">
          <span *ngIf="history.oldPrice" class="price-from">
            De {{ history.oldPrice | number:'1.2-2' }} TND
          </span>
          <span class="price-to">
            {{ history.oldPrice ? 'à' : 'Prix initial:' }} {{ history.newPrice | number:'1.2-2' }} TND
          </span>
          <span 
            *ngIf="history.oldPrice" 
            class="price-diff"
            [ngClass]="getPriceChangeIndicator(history)">
            {{ history.newPrice > history.oldPrice ? '+' : '' }}
            {{ (history.newPrice - history.oldPrice) | number:'1.2-2' }} TND
          </span>
        </div>
        
        <div class="change-info">
          <p class="reason">{{ history.changeReason }}</p>
          <p class="meta">
            <span>{{ formatDate(history.changeDate) }}</span>
          </p>
        </div>
      </div>
    </div>
  </div>
</div>
```

---

## 🧪 Testing Examples

### Example 1: Update Price Successfully

**Request:**
```bash
PUT http://localhost:8080/api/missions/123/update-price
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "newPrice": 180.00,
  "reason": "Distance recalculée - route plus longue"
}
```

**Response (200 OK):**
```json
{
  "idMission": 123,
  "proposedPrice": 180.00,
  "statut": "PRIX_PROPOSE",
  "priceConfirmed": false,
  "priceHistory": [
    {
      "id": 3,
      "oldPrice": 150.00,
      "newPrice": 180.00,
      "changeReason": "Distance recalculée - route plus longue",
      "changedBy": "transporteur@example.com",
      "changeDate": "2024-12-01T15:45:00"
    }
  ]
}
```

### Example 2: Update Price Without Reason

**Request:**
```bash
PUT http://localhost:8080/api/missions/123/update-price
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "newPrice": 160.00
}
```

**Response (200 OK):**
```json
{
  "priceHistory": [
    {
      "changeReason": "Modification du prix par le transporteur",
      "newPrice": 160.00
    }
  ]
}
```

### Example 3: Try to Update After Confirmation (Error)

**Request:**
```bash
PUT http://localhost:8080/api/missions/123/update-price
Content-Type: application/json

{
  "newPrice": 200.00
}
```

**Response (400 Bad Request):**
```json
"Le prix ne peut plus être modifié (statut actuel: PRIX_CONFIRME)"
```

---

## 🔐 Security & Validation

### Backend Validations
✅ User must be authenticated (JWT token required)  
✅ User must have TRANSPORTEUR role  
✅ User must be the transporteur assigned to the mission  
✅ Mission status must be `PRIX_PROPOSE`  
✅ Price must not be confirmed by client  
✅ New price must be positive (> 0)  

### Frontend Validations
- Disable update button if status is not `PRIX_PROPOSE`
- Show warning if price already confirmed
- Validate price input (minimum 0.01)
- Show appropriate error messages

---

## 📝 Database Changes

### New Table: `price_history`

```sql
CREATE TABLE price_history (
  id_price_history BIGINT AUTO_INCREMENT PRIMARY KEY,
  mission_id BIGINT NOT NULL,
  old_price DOUBLE NULL,
  new_price DOUBLE NOT NULL,
  change_reason VARCHAR(500),
  changed_by VARCHAR(255) NOT NULL,
  change_date DATETIME NOT NULL,
  FOREIGN KEY (mission_id) REFERENCES mission(id_mission)
);
```

### Migration Script
```sql
-- Add price history table
CREATE TABLE IF NOT EXISTS price_history (
  id_price_history BIGINT AUTO_INCREMENT PRIMARY KEY,
  mission_id BIGINT NOT NULL,
  old_price DOUBLE NULL,
  new_price DOUBLE NOT NULL,
  change_reason VARCHAR(500),
  changed_by VARCHAR(255) NOT NULL,
  change_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (mission_id) REFERENCES mission(id_mission) ON DELETE CASCADE
);

-- Create index for performance
CREATE INDEX idx_mission_id ON price_history(mission_id);
CREATE INDEX idx_change_date ON price_history(change_date);
```

---

## 📦 Files Created/Modified

### New Files Created
1. ✅ `PriceHistory.java` - Entity for price change tracking
2. ✅ `PriceHistoryRepository.java` - Repository interface
3. ✅ `PriceHistoryResponse.java` - DTO for price history
4. ✅ `UpdatePriceRequest.java` - DTO for update request

### Modified Files
1. ✅ `MissionResponse.java` - Added `priceHistory` field
2. ✅ `MissionService.java` - Added `updateProposedPrice()` method and price history mapping
3. ✅ `MissionController.java` - Added `PUT /update-price` endpoint

---

## 🚀 Quick Start for UI Team

### 1. Add TypeScript Interfaces
```typescript
// src/app/models/price-history-response.model.ts
export interface PriceHistoryResponse {
  id: number;
  oldPrice: number | null;
  newPrice: number;
  changeReason: string;
  changedBy: string;
  changeDate: string;
}

// src/app/models/update-price-request.model.ts
export interface UpdatePriceRequest {
  newPrice: number;
  reason?: string;
}

// Update existing MissionResponse
export interface MissionResponse {
  // ... existing fields
  priceHistory: PriceHistoryResponse[];  // ADD THIS
}
```

### 2. Add Service Method
```typescript
// In mission.service.ts
updateProposedPrice(
  missionId: number, 
  newPrice: number, 
  reason?: string
): Observable<MissionResponse> {
  const token = localStorage.getItem('authToken');
  const headers = new HttpHeaders({
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  });

  return this.http.put<MissionResponse>(
    `${this.apiUrl}/${missionId}/update-price`,
    { newPrice, reason },
    { headers }
  );
}
```

### 3. Use in Component
```typescript
onUpdatePrice(): void {
  this.missionService.updateProposedPrice(
    this.missionId,
    this.newPriceForm.value.newPrice,
    this.newPriceForm.value.reason
  ).subscribe({
    next: (mission) => {
      console.log('Price updated!', mission);
      this.showSuccess = true;
    },
    error: (err) => {
      console.error('Error:', err);
      this.errorMessage = err.error;
    }
  });
}
```

---

## 📞 Support & Questions

If you have questions about this feature, please contact the backend team or refer to:
- **Payment Integration Guide**: `PAYMENT_INTEGRATION_GUIDE.md`
- **API Quick Reference**: `PAYMENT_API_QUICK_REFERENCE.md`

---

## ✅ Compilation Status
**BUILD SUCCESS** ✅  
All 55 source files compiled successfully.

---

**Document Version:** 1.0  
**Last Updated:** December 1, 2025  
**Author:** Backend Development Team
