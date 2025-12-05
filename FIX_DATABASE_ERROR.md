# 🔧 FIXING "Data truncated for column 'statut'" ERROR

## ❌ Problem

You're getting this error:
```
Data truncated for column 'statut' at row 1
```

**Root Cause:** The database column `statut` in the `mission` table is too small (probably VARCHAR(10)) to store the new status values like `PRIX_PROPOSE` (13 characters) and `PRIX_CONFIRME` (13 characters).

---

## ✅ Solution

You need to **update the database schema** to increase the column size.

---

## 🚀 Quick Fix (3 Steps)

### Step 1: Connect to MySQL

Open your terminal and connect to MySQL:

```bash
mysql -u root -p -P 3307
```

Enter your MySQL password when prompted.

### Step 2: Run This SQL Command

Copy and paste this SQL command:

```sql
USE transporteur_db;

ALTER TABLE mission 
MODIFY COLUMN statut VARCHAR(20) NOT NULL;
```

Press Enter. You should see:
```
Query OK, X rows affected (0.XX sec)
```

### Step 3: Verify the Change

```sql
DESCRIBE mission;
```

Check that `statut` column now shows `varchar(20)`:

```
+------------------+--------------+------+-----+---------+----------------+
| Field            | Type         | Null | Key | Default | Extra          |
+------------------+--------------+------+-----+---------+----------------+
| ...              | ...          | ...  | ... | ...     | ...            |
| statut           | varchar(20)  | NO   |     | NULL    |                |
| ...              | ...          | ...  | ... | ...     | ...            |
+------------------+--------------+------+-----+---------+----------------+
```

Exit MySQL:
```sql
EXIT;
```

### Step 4: Restart Spring Boot Application

Restart your backend application and try again!

---

## 📋 Complete Database Migration

For a complete migration including payment table creation, run the SQL script:

### Option A: Using MySQL Command Line

```bash
# Connect to MySQL
mysql -u root -p -P 3307

# Run the migration script
source D:/_5edma/rihebwchayma/back/DATABASE_MIGRATION_PAYMENT.sql

# Exit
EXIT;
```

### Option B: Using MySQL Workbench

1. Open MySQL Workbench
2. Connect to `localhost:3307`
3. Open the file: `DATABASE_MIGRATION_PAYMENT.sql`
4. Click **Execute** (⚡ lightning icon)
5. Verify no errors

### Option C: Manual SQL Commands

Connect to MySQL and run each command:

```sql
USE transporteur_db;

-- 1. Fix statut column size
ALTER TABLE mission 
MODIFY COLUMN statut VARCHAR(20) NOT NULL;

-- 2. Add payment columns (if not exists)
ALTER TABLE mission 
ADD COLUMN proposed_price DOUBLE DEFAULT NULL,
ADD COLUMN price_confirmed BOOLEAN DEFAULT FALSE,
ADD COLUMN is_paid BOOLEAN DEFAULT FALSE;

-- 3. Create payment table
CREATE TABLE payment (
    id_payment BIGINT AUTO_INCREMENT PRIMARY KEY,
    mission_id BIGINT NOT NULL UNIQUE,
    client_id BIGINT NOT NULL,
    transporteur_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    card_last_four VARCHAR(4),
    card_holder_name VARCHAR(255),
    payment_date DATETIME NOT NULL,
    transaction_id VARCHAR(50) UNIQUE NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    
    CONSTRAINT fk_payment_mission FOREIGN KEY (mission_id) REFERENCES mission(id_mission),
    CONSTRAINT fk_payment_client FOREIGN KEY (client_id) REFERENCES client(id_client),
    CONSTRAINT fk_payment_transporteur FOREIGN KEY (transporteur_id) REFERENCES transporteur(id_transporteur)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 🧪 Test After Migration

1. **Restart Backend Application**
2. **Login as Transporteur**
3. **Propose Price** for a mission
4. **Check Response** - should be 200 OK now!

Expected response:
```json
{
  "idMission": 17,
  "statut": "PRIX_PROPOSE",
  "proposedPrice": 150.00,
  "priceConfirmed": false,
  "isPaid": false,
  ...
}
```

---

## 🔍 Troubleshooting

### Error: "Unknown column 'proposed_price'"

Run:
```sql
ALTER TABLE mission 
ADD COLUMN proposed_price DOUBLE DEFAULT NULL,
ADD COLUMN price_confirmed BOOLEAN DEFAULT FALSE,
ADD COLUMN is_paid BOOLEAN DEFAULT FALSE;
```

### Error: "Table 'payment' doesn't exist"

Create the payment table using Step 3 from "Manual SQL Commands" above.

### Error: "Access denied for user"

Make sure you're using the correct MySQL user and password:
```bash
mysql -u root -p -P 3307
# Or use your custom MySQL user
mysql -u your_username -p -P 3307
```

### Still Getting 400 Error After Migration?

1. **Restart Spring Boot** application
2. **Check database** connection in `application.properties`
3. **Clear browser cache** and try again
4. **Check backend logs** for detailed error messages

---

## 📊 Status Values Reference

After migration, these are the valid status values:

| Status | Max Length | Description |
|--------|------------|-------------|
| `EN_ATTENTE` | 10 chars | Initial status |
| `PRIX_PROPOSE` | 13 chars | ⬅️ NEW |
| `PRIX_CONFIRME` | 13 chars | ⬅️ NEW |
| `ACCEPTEE` | 8 chars | Mission accepted |
| `EN_COURS` | 8 chars | In progress |
| `TERMINEE` | 8 chars | Completed |
| `ANNULEE` | 7 chars | Cancelled |

**Column Size:** VARCHAR(20) ✅ (sufficient for all values)

---

## ✅ Success Indicators

After running the migration:

1. ✅ No SQL errors in terminal
2. ✅ `DESCRIBE mission` shows `statut varchar(20)`
3. ✅ `SHOW TABLES` includes `payment`
4. ✅ Backend application starts without errors
5. ✅ Propose price endpoint returns 200 OK
6. ✅ Mission status changes to `PRIX_PROPOSE`

---

## 🆘 Need Help?

If you still get errors after running the migration:

1. **Check Backend Logs**: Look for SQL exceptions
2. **Verify Database**: Connect to MySQL and run `DESCRIBE mission`
3. **Check Spring Boot**: Ensure application.properties has correct DB settings
4. **Port Conflict**: Verify MySQL is running on port 3307

---

**After migration is complete, your payment system will work perfectly! 🎉**
