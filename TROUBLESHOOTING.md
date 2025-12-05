# Troubleshooting Guide

## Problem: Transporteur Availability Not Updating for Client

### Issue Description
When a transporteur marks themselves as "available" (disponible = true), the client doesn't see them in the list of available transporteurs immediately.

### Root Cause
This is a **frontend caching issue**, not a backend problem. The UI is likely caching the list of available transporteurs and not refreshing it when the transporteur's availability changes.

### Backend Solution (Already Implemented)
The backend has been updated to ensure immediate database persistence:

1. **Added `@Transactional` annotation** to `updateDisponibilite()` method
2. **Added `flush()` call** to force immediate database write
3. **No caching** on backend - always returns fresh data from database

```java
@Transactional
public Transporteur updateDisponibilite(Boolean disponible) {
    // ... code ...
    Transporteur saved = transporteurRepository.save(transporteur);
    transporteurRepository.flush(); // Force immediate write
    return saved;
}
```

### Frontend Solutions

#### Option 1: Refresh on Demand (Recommended)
Add a "Refresh" button on the client's transporteur selection page:

```typescript
// mission-create.component.ts
refreshTransporteurs(): void {
  this.loadAvailableTransporteurs();
}
```

```html
<!-- mission-create.component.html -->
<button (click)="refreshTransporteurs()" class="btn btn-secondary">
  🔄 Rafraîchir la liste
</button>
```

#### Option 2: Auto-Refresh with Polling
Automatically refresh the list every 10 seconds:

```typescript
// mission-create.component.ts
import { interval } from 'rxjs';

ngOnInit(): void {
  this.loadAvailableTransporteurs();
  
  // Auto-refresh every 10 seconds
  interval(10000).subscribe(() => {
    this.loadAvailableTransporteurs();
  });
}
```

#### Option 3: Clear HTTP Cache
Ensure HTTP requests are not cached:

```typescript
// mission.service.ts
getAvailableTransporteurs(): Observable<TransporteurAvailableResponse[]> {
  const headers = new HttpHeaders({
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
    'Cache-Control': 'no-cache',
    'Pragma': 'no-cache'
  });
  
  return this.http.get<TransporteurAvailableResponse[]>(
    `${this.apiUrl}/transporteurs/disponibles`,
    { headers }
  );
}
```

#### Option 4: Disable Angular HTTP Interceptor Cache
If you have an HTTP interceptor caching responses, disable it for this endpoint.

### Testing the Backend

You can test if the backend is working correctly using these curl commands:

#### 1. Transporteur marks themselves as available
```bash
curl -X PUT http://localhost:8080/api/profil/transporteur/disponibilite \
  -H "Authorization: Bearer YOUR_TRANSPORTEUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d "true"
```

#### 2. Client fetches available transporteurs (should see the change immediately)
```bash
curl -X GET http://localhost:8080/api/missions/transporteurs/disponibles \
  -H "Authorization: Bearer YOUR_CLIENT_JWT_TOKEN"
```

### Verification Steps

1. **Backend Test:**
   - Transporteur logs in → calls PUT `/api/profil/transporteur/disponibilite` with `true`
   - Immediately call GET `/api/missions/transporteurs/disponibles` → Should see the transporteur in the list
   - ✅ If this works, the backend is fine

2. **Frontend Test:**
   - Transporteur marks available in UI
   - Client clicks "Refresh" or waits 10 seconds (if using auto-refresh)
   - Client should see the transporteur
   - ❌ If this doesn't work, it's a UI caching issue

### Recommended Solution for Production

Implement **Option 1 (Refresh Button)** for immediate user control, combined with **Option 3 (Clear HTTP Cache)** to prevent browser caching.

---

## Problem: Client Can't See Their Missions (Loading Forever)

### Issue Description
Client sees infinite loading when trying to view "Mes missions" but transporteur can see their missions.

### Possible Causes

1. **Frontend Error** - Check browser console for errors
2. **JWT Token Role** - Ensure the JWT token contains `ROLE_CLIENT` correctly
3. **Database Issue** - No missions exist for this client
4. **CORS Issue** - Request blocked by CORS policy

### Debugging Steps

#### 1. Check Browser Console
Open browser DevTools (F12) → Console tab → Look for errors

#### 2. Check Network Tab
Open browser DevTools (F12) → Network tab:
- Look for the request to `/api/missions/client`
- Check status code (should be 200, not 403 or 500)
- Check response body

#### 3. Verify JWT Token Role
```typescript
// In your Angular app, decode the JWT token
const token = localStorage.getItem('authToken');
const payload = JSON.parse(atob(token.split('.')[1]));
console.log('User role:', payload.role); // Should be "CLIENT"
```

#### 4. Test Backend Directly
```bash
# Get client missions
curl -X GET http://localhost:8080/api/missions/client \
  -H "Authorization: Bearer YOUR_CLIENT_JWT_TOKEN"
```

Expected response:
```json
[]  // Empty array if no missions, or array of missions
```

#### 5. Create a Test Mission
As a client, create a mission first, then check if you can see it.

### Common Solutions

#### If Status Code is 403 Forbidden:
- JWT token doesn't have `ROLE_CLIENT`
- User logged in as transporteur, not client
- JWT token expired

**Solution:** Re-login as client

#### If Status Code is 500 Internal Server Error:
- Check backend logs for stack trace
- Likely a database or repository issue

**Solution:** Check application logs

#### If Status Code is 200 but empty array `[]`:
- No missions exist for this client
- This is normal behavior

**Solution:** Create a mission first

#### If Request Never Completes (Infinite Loading):
- CORS blocking the request
- Network connectivity issue
- Backend not running

**Solution:** 
1. Verify backend is running on port 8080
2. Check CORS configuration in SecurityConfig
3. Check browser console for CORS errors

---

## General Tips

### Enable Backend Logging
Add this to `application.properties` to see SQL queries:

```properties
# Show SQL queries
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Check Application is Running
```bash
netstat -ano | findstr :8080
```

### Test All Endpoints with Postman
Import the API documentation and test each endpoint individually.

---

**Last Updated:** December 1, 2025
