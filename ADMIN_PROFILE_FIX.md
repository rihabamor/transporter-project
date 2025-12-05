# Admin Profile Fix - Documentation

## Problem Resolved
When an admin user tried to view their profile, they received a **403 Forbidden** error on `/api/profil/transporteur`. This happened because admin users have a `Compte` record but no `Client` or `Transporteur` profile entity.

## Solution Implemented

### 1. Added Admin Profile DTO
**File:** `src/main/java/com/transporteur/dto/AdminProfileResponse.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileResponse {
    private Long accountId;
    private String email;
    private String role;
    private Instant dateCreation;
    private List<String> permissions;
}
```

### 2. Added Admin Profile Endpoint
**File:** `src/main/java/com/transporteur/controller/ProfilController.java`

**New Endpoint:**
```http
GET /api/profil/admin
Authorization: Bearer <JWT_TOKEN>
```

**Response Example:**
```json
{
  "accountId": 1,
  "email": "admin@transporteur.com",
  "role": "ADMIN",
  "dateCreation": "2024-01-15T10:30:00Z",
  "permissions": ["VIEW_ACCOUNTS", "VIEW_TRANSACTIONS", "VIEW_STATISTICS"]
}
```

## Architecture Difference

### Client/Transporteur Users
- Have `Compte` + separate profile entity (`Client` or `Transporteur`)
- Profile endpoints: `/api/profil/client` or `/api/profil/transporteur`
- Return detailed profile with missions statistics

### Admin Users
- Have only `Compte` (no separate profile entity)
- Profile endpoint: `/api/profil/admin`
- Return basic account info with permissions list

## Frontend Integration

### 1. TypeScript Interface
```typescript
export interface AdminProfileResponse {
  accountId: number;
  email: string;
  role: string;
  dateCreation: string;
  permissions: string[];
}
```

### 2. Angular Service Method
```typescript
getAdminProfile(): Observable<AdminProfileResponse> {
  return this.http.get<AdminProfileResponse>(`${this.apiUrl}/profil/admin`);
}
```

### 3. Role-Based Profile Loading
```typescript
loadUserProfile() {
  const userRole = this.authService.getUserRole(); // from JWT
  
  if (userRole === 'ADMIN') {
    this.profilService.getAdminProfile().subscribe({
      next: (profile) => {
        this.adminProfile = profile;
        console.log('Admin profile loaded:', profile);
      },
      error: (err) => console.error('Error loading admin profile:', err)
    });
  } else if (userRole === 'CLIENT') {
    this.profilService.getClientProfile().subscribe(/* ... */);
  } else if (userRole === 'TRANSPORTEUR') {
    this.profilService.getTransporteurProfile().subscribe(/* ... */);
  }
}
```

### 4. Route Guard Example
```typescript
@Injectable({ providedIn: 'root' })
export class ProfileGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): boolean {
    const role = this.authService.getUserRole();
    
    if (role === 'ADMIN') {
      this.router.navigate(['/admin/profile']);
      return true;
    } else if (role === 'CLIENT') {
      this.router.navigate(['/client/profile']);
      return true;
    } else if (role === 'TRANSPORTEUR') {
      this.router.navigate(['/transporteur/profile']);
      return true;
    }
    
    return false;
  }
}
```

## Testing the Fix

### Step 1: Create Admin User
```bash
# Generate password hash
curl -X POST http://localhost:8080/api/util/generate-password-hash \
  -H "Content-Type: application/json" \
  -d '{"password": "Admin@123"}'
```

### Step 2: Insert Admin Account
```sql
INSERT INTO compte (email, password, role, date_creation)
VALUES ('admin@transporteur.com', '$2a$10$YOUR_HASH_HERE', 'ADMIN', NOW());
```

### Step 3: Login as Admin
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@transporteur.com",
    "password": "Admin@123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "ADMIN"
}
```

### Step 4: Get Admin Profile
```bash
curl -X GET http://localhost:8080/api/profil/admin \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**
```json
{
  "accountId": 1,
  "email": "admin@transporteur.com",
  "role": "ADMIN",
  "dateCreation": "2024-01-15T10:30:00Z",
  "permissions": ["VIEW_ACCOUNTS", "VIEW_TRANSACTIONS", "VIEW_STATISTICS"]
}
```

## Files Modified

1. **AdminProfileResponse.java** (Updated)
   - Added `permissions` field
   - Changed `id` to `accountId` for consistency
   - Added `List<String>` import

2. **ProfilController.java** (Updated)
   - Added `CompteRepository` dependency
   - Added `getProfilAdmin()` endpoint
   - Added necessary imports (SecurityContextHolder, Arrays, Compte, AdminProfileResponse)

## Compilation Status
✅ **BUILD SUCCESS** - All 51 source files compiled successfully

## Security Configuration
✅ `/api/util/**` endpoints are already permitted in SecurityConfig
✅ Admin profile endpoint requires `hasRole('ADMIN')`
✅ JWT authentication required for all profile endpoints

## Related Files
- Original implementation: `ADMIN_DASHBOARD_INTEGRATION_GUIDE.md`
- API reference: `ADMIN_API_QUICK_REFERENCE.md`
- Implementation summary: `ADMIN_DASHBOARD_SUMMARY.md`
- Admin user creation: `CREATE_ADMIN_USER.sql`
- Password hash utility: `UtilityController.java`

## Notes for Production

### Security Consideration
The `/api/util/generate-password-hash` endpoint should be **disabled in production**. Options:
1. Comment out the endpoint in `UtilityController.java`
2. Add IP whitelist to only allow localhost
3. Remove the controller entirely after admin user creation

### Password Hash Generation (Development Only)
```java
// Use this only in development environment
String hash = new BCryptPasswordEncoder().encode("YourPassword");
```

For production, generate hashes offline using a secure script or tool.

---

## Summary
The admin profile endpoint is now fully functional. Admin users can successfully:
- ✅ Generate password hash via `/api/util/generate-password-hash`
- ✅ Login via `/api/auth/login`
- ✅ View their profile via `/api/profil/admin`
- ✅ Access all admin dashboard features

Frontend team should update the profile loading logic to call `/api/profil/admin` for admin users instead of `/api/profil/transporteur` or `/api/profil/client`.
