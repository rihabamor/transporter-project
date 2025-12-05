# Price Update Feature - Quick Reference

## 🎯 Feature Overview
Transporteurs can now **update proposed prices before client confirmation**, with full **price change history tracking**.

---

## 📌 Key Endpoint

### PUT /api/missions/{id}/update-price
**Authorization:** TRANSPORTEUR role required

**Request:**
```json
{
  "newPrice": 150.00,
  "reason": "Optional reason"
}
```

**Response:**
```json
{
  "idMission": 123,
  "proposedPrice": 150.00,
  "statut": "PRIX_PROPOSE",
  "priceHistory": [...]
}
```

---

## 🔄 When Can Price Be Updated?

✅ **CAN UPDATE:**
- Mission status is `PRIX_PROPOSE`
- Price NOT confirmed by client (`priceConfirmed = false`)
- User is the assigned transporteur

❌ **CANNOT UPDATE:**
- Status is `PRIX_CONFIRME` or later
- Price already confirmed by client
- User is not the transporteur

---

## 📊 New Data Structure

### MissionResponse (Updated)
```typescript
{
  // ... existing fields
  priceHistory: [
    {
      id: 1,
      oldPrice: 100.00,  // null for initial
      newPrice: 120.00,
      changeReason: "Fuel cost adjustment",
      changedBy: "transporteur@email.com",
      changeDate: "2024-12-01T10:30:00"
    }
  ]
}
```

---

## 🚀 Quick Integration

### Service Method
```typescript
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
```

### Usage
```typescript
this.missionService.updateProposedPrice(123, 180.00, "Distance recalculée")
  .subscribe({
    next: (mission) => console.log('Updated!', mission),
    error: (err) => console.error(err)
  });
```

---

## ⚠️ Error Messages

| Error | Meaning |
|-------|---------|
| `"Vous n'êtes pas le transporteur de cette mission"` | Wrong user |
| `"Le prix ne peut plus être modifié (statut actuel: X)"` | Wrong status |
| `"Le prix a déjà été confirmé par le client"` | Already confirmed |
| `"Prix invalide"` | Price ≤ 0 |
| `"Mission non trouvée"` | Invalid mission ID |

---

## 📦 Database Migration

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

---

## 🎨 UI Components Needed

1. **Update Price Modal** (Transporteur)
   - Price input field
   - Optional reason textarea
   - Submit button (disabled if cannot update)

2. **Price History Display** (Client & Transporteur)
   - Timeline of price changes
   - Show old → new price
   - Display reason and timestamp

3. **Validation Indicators**
   - Show if price can be updated
   - Display warning if already confirmed

---

## ✅ Testing Checklist

- [ ] Transporteur can update price when status is `PRIX_PROPOSE`
- [ ] Price history is displayed correctly
- [ ] Cannot update after client confirmation
- [ ] Error messages show correctly
- [ ] Price validation works (must be > 0)
- [ ] Optional reason field works
- [ ] Multiple updates create correct history

---

## 📁 Files for Reference

**Main Integration Guide:**  
`PRICE_UPDATE_FEATURE_INTEGRATION_GUIDE.md` - Complete documentation with Angular examples

**Related Guides:**  
- `PAYMENT_INTEGRATION_GUIDE.md` - Original payment feature
- `PAYMENT_API_QUICK_REFERENCE.md` - Payment API reference

---

**Version:** 1.0  
**Date:** December 1, 2025  
**Compilation:** ✅ BUILD SUCCESS (55 source files)
