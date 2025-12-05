# 🔍 WebSocket Location Tracking Debugging Guide

## 🚨 Your Current Issue

**Symptoms:**
- ✅ WebSocket connection established
- ✅ Database has locations for mission ID 4
- ❌ UI is NOT receiving location updates

**This means:** The backend is working and sending data, but there's a mismatch between what the backend is sending and what the UI is listening for.

---

## 🔎 Diagnostic Steps

### Step 1: Verify Which Mission Has Tracking Active

**Run this SQL query in your database:**

```sql
-- Check which missions have tracking active
SELECT 
    id,
    statut,
    tracking_active,
    current_latitude,
    current_longitude,
    date_debut,
    date_fin
FROM mission
WHERE statut = 'EN_COURS' OR tracking_active = true
ORDER BY id DESC;
```

**Expected Result:**
```
| id | statut    | tracking_active | current_latitude | current_longitude |
|----|-----------|-----------------|------------------|-------------------|
| 4  | EN_COURS  | true            | 35.669948        | 10.591675         |
```

### Step 2: Check Location Data for Mission 4

```sql
-- Check if locations are being saved
SELECT 
    id,
    mission_id,
    latitude,
    longitude,
    speed,
    timestamp
FROM mission_location
WHERE mission_id = 4
ORDER BY timestamp DESC
LIMIT 10;
```

**Expected Result:**
```
| id | mission_id | latitude  | longitude | speed | timestamp           |
|----|------------|-----------|-----------|-------|---------------------|
| 50 | 4          | 35.669948 | 10.591675 | 65.2  | 2025-12-01 04:45:30 |
| 49 | 4          | 35.669850 | 10.591700 | 67.8  | 2025-12-01 04:45:25 |
| 48 | 4          | 35.669750 | 10.591725 | 62.1  | 2025-12-01 04:45:20 |
```

If you see recent timestamps (within last few seconds), **the backend is working perfectly!** ✅

### Step 3: Check Backend Logs

Look for these messages in your backend console:

```
✅ GOOD - Should see:
Sending location update to /topic/location/4

❌ BAD - If you see:
No active tracking tasks
Mission not found
Mission status is not EN_COURS
```

---

## 🐛 Common Problems and Solutions

### Problem 1: UI is Subscribed to Wrong Mission ID

**Cause:** Your Angular component is subscribing to `/topic/location/123` but the backend is sending to `/topic/location/4`

**Check in Browser Console:**
```javascript
// You should see:
📡 Subscribing to: /topic/location/4

// If you see a different number:
📡 Subscribing to: /topic/location/123  // ❌ WRONG!
```

**Solution:** Make sure your UI is using the correct mission ID.

**In Angular Component:**
```typescript
// Check your route parameter or mission ID source
ngOnInit(): void {
  // Make sure this matches the mission ID in database (4)
  this.missionId = Number(this.route.snapshot.paramMap.get('id'));
  console.log('🎯 Mission ID from route:', this.missionId);  // Should be 4
  
  // Or if you're hardcoding for testing:
  this.missionId = 4;  // Use the actual mission ID that has tracking
}
```

### Problem 2: Mission Status Not EN_COURS

**Check Mission Status:**
```sql
SELECT id, statut FROM mission WHERE id = 4;
```

**If status is NOT `EN_COURS`:**
```sql
-- Update mission status to EN_COURS
UPDATE mission SET statut = 'EN_COURS' WHERE id = 4;
```

**Or use the API:**
```bash
# Get your transporteur token first
TOKEN="your_transporteur_jwt_token"

# Update mission status to EN_COURS
curl -X PUT http://localhost:8080/api/missions/4/status \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '"EN_COURS"'
```

### Problem 3: WebSocket Connected but Wrong Topic

**Debug in Browser Console:**

```javascript
// Add this to your Angular component to debug
this.subscription = this.locationService.subscribeToLocationUpdates(
  this.missionId,
  (locationUpdate) => {
    console.log('📍 RECEIVED Location Update:', locationUpdate);
    console.log('📍 Mission ID in update:', locationUpdate.missionId);
    console.log('📍 Expected Mission ID:', this.missionId);
    
    // Check if they match
    if (locationUpdate.missionId !== this.missionId) {
      console.error('❌ MISMATCH! Backend sent wrong mission ID!');
    }
    
    this.currentLocation = locationUpdate;
  }
);
```

### Problem 4: Firewall or Network Issue

**Test WebSocket with Browser DevTools:**

1. Open Chrome DevTools → Network Tab
2. Filter by "WS"
3. Look for WebSocket connection
4. Click on it and check "Messages" tab
5. You should see incoming messages like:

```json
{
  "missionId": 4,
  "latitude": 35.669948,
  "longitude": 10.591675,
  "timestamp": "2025-12-01T04:45:30",
  "speed": 65.2,
  "progressPercentage": 25,
  "distanceRemaining": 35.5
}
```

---

## 🔧 Manual Testing Procedure

### Test 1: Ensure Mission 4 Exists and is Trackable

**SQL Check:**
```sql
SELECT 
    m.id,
    m.statut,
    m.tracking_active,
    c.nom as client_name,
    t.nom as transporteur_name
FROM mission m
JOIN client c ON m.client_id = c.id_client
JOIN transporteur t ON m.transporteur_id = t.id_transporteur
WHERE m.id = 4;
```

### Test 2: Start Tracking Manually via API

```bash
# Get transporteur token (replace with actual credentials)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "transporteur@example.com",
    "password": "password123"
  }'

# Copy the token from response, then:
TOKEN="paste_token_here"

# Manually start tracking for mission 4
curl -X POST http://localhost:8080/api/tracking/start/4 \
  -H "Authorization: Bearer $TOKEN"

# Check backend logs - should see:
# "Tracking démarré pour la mission 4"
```

### Test 3: Subscribe to WebSocket from Browser Console

**Paste this in your browser console (F12) while on your Angular app:**

```javascript
// Test if WebSocket can receive messages
const socket = new SockJS('http://localhost:8080/ws-location');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('✅ Connected to WebSocket');
    
    // Subscribe to mission 4 (change if needed)
    stompClient.subscribe('/topic/location/4', function(message) {
        const update = JSON.parse(message.body);
        console.log('📍 Location Update Received:', update);
        console.log('   Latitude:', update.latitude);
        console.log('   Longitude:', update.longitude);
        console.log('   Speed:', update.speed, 'km/h');
        console.log('   Progress:', update.progressPercentage, '%');
    });
    
    console.log('📡 Subscribed to /topic/location/4 - waiting for updates...');
});
```

**Expected Output (every 5 seconds):**
```
📍 Location Update Received: {missionId: 4, latitude: 35.66995, ...}
   Latitude: 35.66995
   Longitude: 10.59168
   Speed: 65.2 km/h
   Progress: 25 %
```

---

## 🎯 Quick Fix Checklist

If UI is not receiving locations but DB has them:

- [ ] **Backend is running** - Check `mvn spring-boot:run` is active
- [ ] **Mission ID matches** - UI subscribes to same ID as backend sends (4)
- [ ] **Mission status is EN_COURS** - Check `SELECT statut FROM mission WHERE id = 4`
- [ ] **WebSocket connected** - Browser console shows "WebSocket Connected"
- [ ] **Correct topic** - UI subscribes to `/topic/location/4`
- [ ] **No JavaScript errors** - Check browser console for errors
- [ ] **Token is valid** - If using auth, check token not expired

---

## 📊 Expected Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│ BACKEND (Every 5 seconds)                                       │
│                                                                 │
│ 1. LocationTrackingService.sendNextLocation(4)                 │
│    └─> Generate location point (lat: 35.67, lon: 10.59)       │
│    └─> Save to database (mission_location table)              │
│    └─> Create LocationUpdateDTO                               │
│    └─> messagingTemplate.convertAndSend(                      │
│           "/topic/location/4",                                 │
│           locationUpdate                                       │
│        )                                                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              │ WebSocket
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│ FRONTEND (Angular)                                              │
│                                                                 │
│ 2. WebSocketService is connected                              │
│    └─> Subscribed to /topic/location/4                        │
│    └─> Receives message                                       │
│    └─> Calls callback(locationUpdate)                         │
│    └─> Component updates UI                                   │
│        └─> this.currentLocation = locationUpdate              │
│        └─> updateMap(lat, lon)                                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 💡 Most Likely Solution

Based on your symptoms, **the most likely issue is:**

### **Your UI is trying to track a different mission ID than 4**

**Fix:**

1. **Check your Angular route:**
   ```typescript
   // In your component
   ngOnInit(): void {
     const routeId = this.route.snapshot.paramMap.get('id');
     console.log('🎯 Route Mission ID:', routeId);  // Check this!
     
     // Temporarily hardcode to test
     this.missionId = 4;  // Force to use mission 4
     
     this.subscribeToUpdates();
   }
   ```

2. **Or navigate to the correct URL:**
   ```
   http://localhost:4200/mission/4  
   # Make sure URL has /4 not /123 or other number
   ```

3. **Or check your mission list:**
   ```typescript
   // If loading from a list, verify the mission ID
   getMissions().subscribe(missions => {
     console.log('📋 Available missions:', missions);
     // Find mission 4 and click on it
   });
   ```

---

## 🧪 Test with Multiple Browsers

**Test 1: Backend Logs**
```
Terminal: Backend Server
Watch for: "Sending location update to /topic/location/4"
Every 5 seconds while tracking is active
```

**Test 2: Browser 1 - Admin View**
```
Open: http://localhost:4200
Console: Should show WebSocket connected
Console: Should show subscribing to /topic/location/4
```

**Test 3: Browser 2 - Developer Tools**
```
Open: http://localhost:4200
DevTools → Network → WS Tab
Click on websocket connection
Check "Messages" tab
Should see incoming messages every 5 seconds
```

---

## 🎬 Step-by-Step Resolution

### Step 1: Verify Backend is Sending
```bash
# In backend terminal, you should see logs like:
INFO  c.t.s.LocationTrackingService : Sending location for mission 4
INFO  c.t.s.LocationTrackingService : Progress: 25%, Distance: 35.5km
```

### Step 2: Verify UI is Listening
```javascript
// In browser console, type:
console.log('Current Mission ID:', this.missionId);
// Should output: 4
```

### Step 3: Force UI to Track Mission 4
```typescript
// Temporary fix in your component:
ngOnInit(): void {
  this.missionId = 4;  // Hardcode for testing
  
  console.log('🎯 Forcing mission ID to 4');
  
  this.websocketService.isConnected$.subscribe(connected => {
    if (connected) {
      console.log('✅ Connected, subscribing to mission 4');
      this.subscribeToUpdates();
    }
  });
}
```

### Step 4: Verify Reception
```typescript
subscribeToUpdates(): void {
  this.subscription = this.locationService.subscribeToLocationUpdates(
    4,  // Hardcode to 4
    (locationUpdate) => {
      console.log('✅ SUCCESS! Received update:', locationUpdate);
      this.currentLocation = locationUpdate;
    }
  );
}
```

---

## 📞 Still Not Working?

If after all these steps you still don't receive updates:

1. **Restart backend server**
   ```bash
   # Ctrl+C to stop
   mvn spring-boot:run
   ```

2. **Clear browser cache and reload**
   ```
   Ctrl+Shift+Delete → Clear cache
   F5 to reload
   ```

3. **Check if multiple missions are tracked**
   ```sql
   SELECT id, statut, tracking_active FROM mission WHERE tracking_active = true;
   ```

4. **Manually trigger tracking**
   ```sql
   -- Reset all tracking
   UPDATE mission SET tracking_active = false;
   
   -- Set only mission 4 to EN_COURS
   UPDATE mission SET statut = 'EN_COURS', tracking_active = true WHERE id = 4;
   ```

5. **Use the manual test script** (see Test 3 above)

---

**Next Step:** Run the SQL queries above and share the results so we can pinpoint the exact issue!
