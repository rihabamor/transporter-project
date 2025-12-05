# ⚡ QUICK FIX - Run This Now!

## The Problem
Your error: `Data truncated for column 'statut' at row 1`

**Cause:** Database column too small for new status `PRIX_PROPOSE` (13 characters)

---

## The Solution (2 Minutes)

### Step 1: Open Command Prompt or PowerShell

Press `Win + R`, type `cmd`, press Enter

### Step 2: Connect to MySQL

```bash
mysql -u root -p -P 3307
```

Enter your password when prompted.

### Step 3: Run This Command

```sql
USE transporteur_db;
ALTER TABLE mission MODIFY COLUMN statut VARCHAR(20) NOT NULL;
```

Press Enter. You should see:
```
Query OK, X rows affected
```

### Step 4: Exit MySQL

```sql
EXIT;
```

### Step 5: Restart Your Spring Boot Application

In VS Code, stop the running application and start it again.

### Step 6: Test Again

Go to your Angular app and try to propose a price again. **It should work now!** ✅

---

## Expected Result

After proposing price, you should see:

```json
{
  "idMission": 17,
  "statut": "PRIX_PROPOSE",  ✅
  "proposedPrice": 150.00,
  "priceConfirmed": false,
  "isPaid": false
}
```

---

## Still Getting Errors?

### If you get: "ERROR 1054 (42S22): Unknown column 'proposed_price'"

Run these additional commands:

```sql
USE transporteur_db;

ALTER TABLE mission 
ADD COLUMN proposed_price DOUBLE DEFAULT NULL,
ADD COLUMN price_confirmed BOOLEAN DEFAULT FALSE,
ADD COLUMN is_paid BOOLEAN DEFAULT FALSE;
```

### If you get: "ERROR 1146 (42S02): Table 'payment' doesn't exist"

Create the payment table:

```sql
USE transporteur_db;

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
    FOREIGN KEY (mission_id) REFERENCES mission(id_mission),
    FOREIGN KEY (client_id) REFERENCES client(id_client),
    FOREIGN KEY (transporteur_id) REFERENCES transporteur(id_transporteur)
);
```

---

**That's it! Your payment system should work now.** 🎉

For complete migration script, see: `DATABASE_MIGRATION_PAYMENT.sql`
