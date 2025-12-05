# 👨‍💼 ADMIN API - QUICK REFERENCE

**Backend URL:** `http://localhost:8080`  
**Database:** MySQL on port 3307  
**Authentication:** JWT Bearer token required (ROLE_ADMIN)

---

## 🔑 Create Admin Account

### Step 1: Generate Password Hash
```bash
POST http://localhost:8080/api/util/generate-password-hash
Content-Type: application/json

{
  "password": "Admin@123"
}

Response: "$2a$10$rOg3L7IvLKzJv..."
```

### Step 2: Insert into Database
```sql
USE transporteur_db;

INSERT INTO compte (email, password, role, date_creation) 
VALUES (
  'admin@transporteur.com',
  '$2a$10$YOUR_HASH_HERE',  -- From step 1
  'ADMIN',
  NOW()
);
```

### Step 3: Login
```bash
POST http://localhost:8080/api/auth/login

{
  "email": "admin@transporteur.com",
  "password": "Admin@123"
}

Response: { "token": "eyJhbG...", "role": "ADMIN" }
```

---

## 🔌 Admin Endpoints

### 1. Get All Accounts
```
GET /api/admin/accounts
Authorization: Bearer YOUR_ADMIN_TOKEN

Returns: Array of AccountResponse
```

### 2. Get All Transactions
```
GET /api/admin/transactions
Authorization: Bearer YOUR_ADMIN_TOKEN

Returns: Array of TransactionResponse
```

### 3. Get Platform Statistics
```
GET /api/admin/statistics
Authorization: Bearer YOUR_ADMIN_TOKEN

Returns: PlatformStatisticsResponse
```

---

## 📊 Statistics Response

```json
{
  "totalAccounts": 45,
  "totalClients": 30,
  "totalTransporteurs": 14,
  "totalAdmins": 1,
  
  "totalMissions": 120,
  "missionsEnAttente": 8,
  "missionsPrixPropose": 5,
  "missionsPrixConfirme": 3,
  "missionsAcceptees": 12,
  "missionsEnCours": 15,
  "missionsTerminees": 65,
  "missionsAnnulees": 12,
  
  "totalPayments": 80,
  "totalRevenue": 12500.00,
  "averageTransactionAmount": 156.25,
  
  "missionsToday": 3,
  "paymentsToday": 2,
  "revenueToday": 300.00,
  
  "missionsThisWeek": 18,
  "paymentsThisWeek": 12,
  "revenueThisWeek": 1850.00,
  
  "missionsThisMonth": 45,
  "paymentsThisMonth": 35,
  "revenueThisMonth": 5500.00
}
```

---

## 🎨 UI Dashboard Layout

```
┌──────────────────────────────────────────────┐
│  📊 Admin Dashboard                          │
├──────────────────────────────────────────────┤
│                                              │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐       │
│  │ 45   │ │ 30   │ │ 14   │ │  1   │       │
│  │ Total│ │Client│ │Trans │ │Admin │       │
│  └──────┘ └──────┘ └──────┘ └──────┘       │
│                                              │
├──────────────────────────────────────────────┤
│  Missions (120)    │  Payments (80)          │
│  ─────────────────│  ─────────────────      │
│  • Attente: 8     │  • Revenue: 12,500 TND  │
│  • Proposé: 5     │  • Average: 156 TND     │
│  • Confirmé: 3    │  • Paid: 80             │
│  • Acceptées: 12  │  • Unpaid: 40           │
│  • En Cours: 15   │                         │
│  • Terminées: 65  │                         │
│  • Annulées: 12   │                         │
├──────────────────────────────────────────────┤
│  Today    │  This Week  │  This Month        │
│  ──────   │  ─────────  │  ──────────        │
│  M: 3     │  M: 18      │  M: 45             │
│  P: 2     │  P: 12      │  P: 35             │
│  R: 300   │  R: 1,850   │  R: 5,500          │
└──────────────────────────────────────────────┘
```

---

## 🧪 Quick Test

```bash
# 1. Create admin (SQL)
mysql -u root -p -P 3307
> USE transporteur_db;
> INSERT INTO compte (email, password, role, date_creation) 
  VALUES ('admin@transporteur.com', '$2a$10$HASH', 'ADMIN', NOW());

# 2. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@transporteur.com","password":"Admin@123"}'

# 3. Get statistics
curl -X GET http://localhost:8080/api/admin/statistics \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## ⚠️ Security Notes

- ✅ Only ADMIN role can access these endpoints
- ✅ JWT token required in Authorization header
- ✅ Delete UtilityController in production
- ✅ Change default admin password after first login
- ❌ Never share admin credentials

---

## 📖 Full Documentation

**Complete Guide:** `ADMIN_DASHBOARD_INTEGRATION_GUIDE.md`

**Summary:** `ADMIN_DASHBOARD_SUMMARY.md`
