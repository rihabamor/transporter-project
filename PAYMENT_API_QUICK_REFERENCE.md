# 💳 PAYMENT API - QUICK REFERENCE

**Backend URL:** `http://localhost:8080`  
**Database:** MySQL on port 3307  
**Authentication:** JWT Bearer token required

---

## 🔑 Authentication Headers

```typescript
headers: {
  'Content-Type': 'application/json',
  'Authorization': 'Bearer YOUR_JWT_TOKEN'
}
```

---

## 🔄 Complete Workflow (5 Steps)

### Step 1: Transporteur Proposes Price

```
POST /api/missions/{id}/propose-price
Role: TRANSPORTEUR
Body: { "proposedPrice": 150.00 }

Mission Status: EN_ATTENTE → PRIX_PROPOSE
```

### Step 2: Client Confirms Price

```
POST /api/missions/{id}/confirm-price
Role: CLIENT
Body: (empty)

Mission Status: PRIX_PROPOSE → PRIX_CONFIRME
```

### Step 3: Client Pays

```
POST /api/payment/process
Role: CLIENT
Body: {
  "missionId": 1,
  "cardNumber": "4532123456789876",
  "cardHolderName": "JEAN DUPONT",
  "expiryMonth": "12",
  "expiryYear": "2026",
  "cvv": "123",
  "amount": 150.00
}

Mission Status: PRIX_CONFIRME → ACCEPTEE
isPaid: false → true
Creates Payment record with transaction ID
```

### Step 4: Check Payment Status

```
GET /api/payment/status/{missionId}
Role: CLIENT or TRANSPORTEUR

Returns: {
  "missionId": 1,
  "isPaid": true,
  "amount": 150.00,
  "paymentStatus": "COMPLETED",
  "message": "Mission payée"
}
```

### Step 5: Transporteur Starts Mission

```
PUT /api/missions/{id}/statut
Role: TRANSPORTEUR
Body: { "statut": "EN_COURS" }

⚠️ ONLY works if isPaid = true
Mission Status: ACCEPTEE → EN_COURS
```

---

## 📊 Mission Status Values

| Status | Description | Can Proceed If |
|--------|-------------|----------------|
| `EN_ATTENTE` | Waiting for price | - |
| `PRIX_PROPOSE` | Price proposed | Transporteur proposed price |
| `PRIX_CONFIRME` | Price confirmed | Client confirmed |
| `ACCEPTEE` | Payment completed | isPaid = true |
| `EN_COURS` | Mission started | Payment completed |
| `TERMINEE` | Completed | - |
| `ANNULEE` | Cancelled | - |

---

## 📦 Response Objects

### MissionResponse (includes payment fields)

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
  "statut": "ACCEPTEE",
  "dateCreation": "2024-12-01T08:30:00",
  "description": "Transport de meubles",
  "proposedPrice": 150.00,      // ⬅️ NEW
  "priceConfirmed": true,        // ⬅️ NEW
  "isPaid": true                 // ⬅️ NEW
}
```

### PaymentResponse (after successful payment)

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

---

## ⚠️ Common Errors

| Code | Message | Solution |
|------|---------|----------|
| 400 | "Vous n'êtes pas le transporteur de cette mission" | Use correct JWT token |
| 400 | "Impossible de proposer un prix pour cette mission" | Check mission status is EN_ATTENTE |
| 400 | "Prix invalide" | Use price > 0 |
| 400 | "Aucun prix proposé pour cette mission" | Transporteur must propose price first |
| 400 | "Le prix doit être confirmé avant le paiement" | Client must confirm price first |
| 400 | "Cette mission a déjà été payée" | Check isPaid flag |
| 400 | "Le montant ne correspond pas au prix proposé" | Use exact proposedPrice amount |
| 400 | "La mission doit être payée avant de pouvoir commencer" | Cannot start unpaid mission |

---

## 🧪 Test Credit Card

```
Card Number: 4532 1234 5678 9876
Cardholder:  JEAN DUPONT
Expiry:      12/2026
CVV:         123
```

⚠️ **Only last 4 digits stored:** `9876`

---

## 🎨 UI Implementation Tips

### Show/Hide Buttons Based on Status

```typescript
// Transporteur view
showProposePrice(): boolean {
  return this.mission.statut === 'EN_ATTENTE' && !this.mission.proposedPrice;
}

showStartMission(): boolean {
  return this.mission.statut === 'ACCEPTEE' && this.mission.isPaid;
}

// Client view
showConfirmPrice(): boolean {
  return this.mission.statut === 'PRIX_PROPOSE' && !this.mission.priceConfirmed;
}

showPaymentForm(): boolean {
  return this.mission.statut === 'PRIX_CONFIRME' && !this.mission.isPaid;
}
```

### Display Payment Status Badge

```html
<!-- Paid badge -->
<span *ngIf="mission.isPaid" class="badge bg-success">
  ✅ Payé - {{ mission.proposedPrice }} TND
</span>

<!-- Waiting for payment -->
<span *ngIf="!mission.isPaid && mission.proposedPrice" class="badge bg-warning">
  ⚠️ En attente de paiement
</span>
```

### Validate Card Input

```typescript
validateCardNumber(cardNumber: string): boolean {
  const clean = cardNumber.replace(/\s/g, '');
  return /^\d{16}$/.test(clean);
}

formatCardNumber(input: string): string {
  const clean = input.replace(/\s/g, '');
  return clean.match(/.{1,4}/g)?.join(' ') || clean;
}
```

---

## 📖 Full Documentation

For complete implementation guide with Angular code examples:

👉 **Read: `PAYMENT_INTEGRATION_GUIDE.md`**

---

## 🚀 Quick Start

1. **Login** as TRANSPORTEUR → Get JWT token
2. **Propose price** for mission in EN_ATTENTE
3. **Login** as CLIENT → Get JWT token  
4. **Confirm price** for mission in PRIX_PROPOSE
5. **Pay** with test credit card
6. **Verify** isPaid = true
7. **Login** as TRANSPORTEUR again
8. **Start mission** (now allowed)

---

**Questions?** Check `PAYMENT_INTEGRATION_GUIDE.md` for detailed examples.
