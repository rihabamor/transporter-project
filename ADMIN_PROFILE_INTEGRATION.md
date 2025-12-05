# Admin Profile Endpoint - Update to Integration Guide

## New Endpoint Added

### GET /api/profil/admin - Get Admin Profile
Retrieves the profile information for the currently authenticated admin user.

**Endpoint:** `GET /api/profil/admin`

**Authorization:** Required - JWT Bearer Token with ADMIN role

**Request Headers:**
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
{
  "accountId": 1,
  "email": "admin@transporteur.com",
  "role": "ADMIN",
  "dateCreation": "2024-01-15T10:30:00.000Z",
  "permissions": [
    "VIEW_ACCOUNTS",
    "VIEW_TRANSACTIONS",
    "VIEW_STATISTICS"
  ]
}
```

**Error Response (403 Forbidden):**
```json
"Accès refusé"
```

**Error Response (400 Bad Request):**
```json
"Compte non trouvé"
```

---

## TypeScript Interface

Add this interface to your Angular models:

```typescript
export interface AdminProfileResponse {
  accountId: number;
  email: string;
  role: string;
  dateCreation: string;
  permissions: string[];
}
```

---

## Angular Service Method

Add this method to your `ProfilService`:

```typescript
getAdminProfile(): Observable<AdminProfileResponse> {
  const token = localStorage.getItem('authToken');
  const headers = new HttpHeaders({
    'Authorization': `Bearer ${token}`
  });
  
  return this.http.get<AdminProfileResponse>(
    `${this.apiUrl}/profil/admin`,
    { headers }
  );
}
```

---

## Admin Profile Component

### Component TypeScript
```typescript
import { Component, OnInit } from '@angular/core';
import { ProfilService } from '../services/profil.service';
import { AdminProfileResponse } from '../models/admin-profile-response.model';

@Component({
  selector: 'app-admin-profile',
  templateUrl: './admin-profile.component.html',
  styleUrls: ['./admin-profile.component.css']
})
export class AdminProfileComponent implements OnInit {
  adminProfile: AdminProfileResponse | null = null;
  loading = false;
  error: string | null = null;

  constructor(private profilService: ProfilService) {}

  ngOnInit(): void {
    this.loadAdminProfile();
  }

  loadAdminProfile(): void {
    this.loading = true;
    this.error = null;

    this.profilService.getAdminProfile().subscribe({
      next: (profile) => {
        this.adminProfile = profile;
        this.loading = false;
        console.log('Admin profile loaded:', profile);
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement du profil admin';
        this.loading = false;
        console.error('Error loading admin profile:', err);
      }
    });
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }
}
```

### Component HTML
```html
<div class="admin-profile-container">
  <h2>Profil Administrateur</h2>

  <!-- Loading State -->
  <div *ngIf="loading" class="loading-spinner">
    <p>Chargement du profil...</p>
  </div>

  <!-- Error State -->
  <div *ngIf="error" class="error-message">
    <p>{{ error }}</p>
    <button (click)="loadAdminProfile()">Réessayer</button>
  </div>

  <!-- Profile Data -->
  <div *ngIf="adminProfile && !loading" class="profile-card">
    <div class="profile-header">
      <div class="profile-icon">
        <i class="fas fa-user-shield"></i>
      </div>
      <h3>{{ adminProfile.email }}</h3>
    </div>

    <div class="profile-details">
      <div class="detail-row">
        <span class="label">ID du compte:</span>
        <span class="value">{{ adminProfile.accountId }}</span>
      </div>

      <div class="detail-row">
        <span class="label">Rôle:</span>
        <span class="value badge-admin">{{ adminProfile.role }}</span>
      </div>

      <div class="detail-row">
        <span class="label">Date de création:</span>
        <span class="value">{{ formatDate(adminProfile.dateCreation) }}</span>
      </div>

      <div class="detail-row">
        <span class="label">Permissions:</span>
        <div class="permissions-list">
          <span 
            *ngFor="let permission of adminProfile.permissions" 
            class="permission-badge">
            {{ permission }}
          </span>
        </div>
      </div>
    </div>

    <div class="profile-actions">
      <button class="btn-primary" routerLink="/admin/dashboard">
        Tableau de bord
      </button>
      <button class="btn-secondary" routerLink="/admin/accounts">
        Gérer les comptes
      </button>
    </div>
  </div>
</div>
```

### Component CSS
```css
.admin-profile-container {
  max-width: 800px;
  margin: 2rem auto;
  padding: 2rem;
}

.loading-spinner {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.error-message {
  background-color: #fee;
  border: 1px solid #fcc;
  border-radius: 4px;
  padding: 1rem;
  margin: 1rem 0;
  color: #c33;
  text-align: center;
}

.error-message button {
  margin-top: 0.5rem;
  padding: 0.5rem 1rem;
  background-color: #c33;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.profile-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.profile-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 2rem;
  text-align: center;
}

.profile-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.profile-header h3 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 500;
}

.profile-details {
  padding: 2rem;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  border-bottom: 1px solid #eee;
}

.detail-row:last-child {
  border-bottom: none;
}

.label {
  font-weight: 600;
  color: #555;
}

.value {
  color: #333;
}

.badge-admin {
  background-color: #667eea;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.9rem;
  font-weight: 600;
}

.permissions-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.permission-badge {
  background-color: #e0e7ff;
  color: #4c51bf;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.85rem;
  font-weight: 500;
}

.profile-actions {
  display: flex;
  gap: 1rem;
  padding: 2rem;
  background-color: #f9fafb;
}

.btn-primary,
.btn-secondary {
  flex: 1;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background-color: #667eea;
  color: white;
}

.btn-primary:hover {
  background-color: #5568d3;
}

.btn-secondary {
  background-color: white;
  color: #667eea;
  border: 1px solid #667eea;
}

.btn-secondary:hover {
  background-color: #f0f4ff;
}
```

---

## Route Configuration

Add this route to your Angular routing module:

```typescript
import { AdminProfileComponent } from './components/admin-profile/admin-profile.component';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';

const routes: Routes = [
  // ... other routes
  {
    path: 'admin/profile',
    component: AdminProfileComponent,
    canActivate: [AuthGuard, AdminGuard]
  },
  // ... other routes
];
```

---

## Admin Guard

Create an admin guard to protect admin-only routes:

```typescript
import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): boolean {
    const userRole = this.authService.getUserRole();
    
    if (userRole === 'ADMIN') {
      return true;
    }
    
    // Redirect non-admin users
    this.router.navigate(['/unauthorized']);
    return false;
  }
}
```

---

## Role-Based Profile Loading

Update your profile loading logic to handle different user roles:

```typescript
export class ProfileComponent implements OnInit {
  constructor(
    private authService: AuthService,
    private profilService: ProfilService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const userRole = this.authService.getUserRole();
    this.loadProfileByRole(userRole);
  }

  loadProfileByRole(role: string): void {
    switch (role) {
      case 'ADMIN':
        this.loadAdminProfile();
        break;
      case 'CLIENT':
        this.loadClientProfile();
        break;
      case 'TRANSPORTEUR':
        this.loadTransporteurProfile();
        break;
      default:
        console.error('Unknown role:', role);
        this.router.navigate(['/login']);
    }
  }

  loadAdminProfile(): void {
    this.profilService.getAdminProfile().subscribe({
      next: (profile) => {
        console.log('Admin profile:', profile);
        // Handle admin profile
      },
      error: (err) => console.error('Error loading admin profile:', err)
    });
  }

  loadClientProfile(): void {
    this.profilService.getClientProfile().subscribe({
      next: (profile) => {
        console.log('Client profile:', profile);
        // Handle client profile
      },
      error: (err) => console.error('Error loading client profile:', err)
    });
  }

  loadTransporteurProfile(): void {
    this.profilService.getTransporteurProfile().subscribe({
      next: (profile) => {
        console.log('Transporteur profile:', profile);
        // Handle transporteur profile
      },
      error: (err) => console.error('Error loading transporteur profile:', err)
    });
  }
}
```

---

## Testing the Admin Profile

### 1. Login as Admin
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@transporteur.com",
  "password": "Admin@123"
}
```

### 2. Get Admin Profile
```bash
GET http://localhost:8080/api/profil/admin
Authorization: Bearer YOUR_JWT_TOKEN
```

### 3. Expected Response
```json
{
  "accountId": 1,
  "email": "admin@transporteur.com",
  "role": "ADMIN",
  "dateCreation": "2024-01-15T10:30:00.000Z",
  "permissions": [
    "VIEW_ACCOUNTS",
    "VIEW_TRANSACTIONS",
    "VIEW_STATISTICS"
  ]
}
```

---

## Important Notes

### Architecture Differences

**Admin Users:**
- Have only `Compte` entity (no separate profile table)
- Use `/api/profil/admin` endpoint
- Get basic account info with permissions list

**Client/Transporteur Users:**
- Have `Compte` + separate profile entity
- Use `/api/profil/client` or `/api/profil/transporteur` endpoints
- Get detailed profile with mission statistics

### Frontend Implementation

Your frontend should:
1. Detect user role from JWT token after login
2. Call the appropriate profile endpoint based on role
3. Display different UI components for each role type
4. Use role-based guards to protect admin routes

### Security

- ✅ JWT token required for authentication
- ✅ `hasRole('ADMIN')` authorization check
- ✅ Email extracted from SecurityContext (prevents spoofing)
- ✅ Role verification (double-check admin role)

---

## Complete Workflow

1. **Admin Creation** → Use `/api/util/generate-password-hash` to create BCrypt hash
2. **Database Insert** → Run CREATE_ADMIN_USER.sql with generated hash
3. **Login** → POST to `/api/auth/login` to get JWT token
4. **Profile** → GET from `/api/profil/admin` with JWT token
5. **Dashboard** → Access admin dashboard features

---

This document should be provided to the UI team along with:
- `ADMIN_DASHBOARD_INTEGRATION_GUIDE.md` (main admin features)
- `ADMIN_API_QUICK_REFERENCE.md` (API reference)
- `ADMIN_PROFILE_FIX.md` (this profile endpoint documentation)
