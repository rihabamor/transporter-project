# JWT Structure & Role Extraction Guide

## 📋 Table of Contents
1. [What is a JWT?](#what-is-a-jwt)
2. [JWT Structure Breakdown](#jwt-structure-breakdown)
3. [Your Application's JWT](#your-applications-jwt)
4. [How to Extract Role from JWT (Frontend)](#how-to-extract-role-from-jwt-frontend)
5. [How to Extract Role from JWT (Backend)](#how-to-extract-role-from-jwt-backend)
6. [Testing JWT Tokens](#testing-jwt-tokens)
7. [Security Best Practices](#security-best-practices)

---

## What is a JWT?

**JWT (JSON Web Token)** is a compact, URL-safe means of representing claims to be transferred between two parties. In your application, it's used for **authentication and authorization**.

### Why Use JWT?
- ✅ **Stateless**: No need to store sessions on the server
- ✅ **Self-contained**: Contains all user information
- ✅ **Secure**: Cryptographically signed
- ✅ **Cross-domain**: Works across different domains/services

---

## JWT Structure Breakdown

A JWT consists of **three parts** separated by dots (`.`):

```
HEADER.PAYLOAD.SIGNATURE
```

### Example JWT:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiVFJBTlNQT1JURVVSIiwic3ViIjoidHJhbnNwb3J0ZXJAZXhhbXBsZS5jb20iLCJpYXQiOjE3MzMwNzM2MDAsImV4cCI6MTczMzE2MDAwMH0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

### Part 1: Header (Algorithm & Type)
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```
- **alg**: Algorithm used to sign the token (HMAC SHA-256)
- **typ**: Token type (JWT)

### Part 2: Payload (Claims/Data)
```json
{
  "role": "TRANSPORTEUR",
  "sub": "transporter@example.com",
  "iat": 1733073600,
  "exp": 1733160000
}
```
- **role**: User's role (CLIENT, TRANSPORTEUR, or ADMIN) **← NEW!**
- **sub**: Subject (user's email)
- **iat**: Issued At (timestamp)
- **exp**: Expiration (timestamp)

### Part 3: Signature (Security)
```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  your-256-bit-secret
)
```

---

## Your Application's JWT

### Backend Changes Made

#### 1. **Updated `JwtUtil.java`**
Added methods to include and extract the role:

```java
// Generate token WITH role
public String generateToken(String username, String role) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", role);  // ← Role is now stored in the token
    
    return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(SignatureAlgorithm.HS256, secret.getBytes())
            .compact();
}

// Extract role FROM token
public String extractRole(String token) {
    Claims claims = Jwts.parser()
            .setSigningKey(secret.getBytes())
            .parseClaimsJws(token)
            .getBody();
    return claims.get("role", String.class);
}
```

#### 2. **Updated `AuthService.java`**
Now passes the role when generating tokens:

```java
// Registration
String token = jwtUtil.generateToken(savedCompte.getEmail(), savedCompte.getRole().name());

// Login
Compte compte = compteRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
String token = jwtUtil.generateToken(email, compte.getRole().name());
```

### JWT Token Example from Your App

When a TRANSPORTEUR logs in, they'll receive:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVFJBTlNQT1JURVVSIiwic3ViIjoidHJhbnNwb3J0ZXJAZXhhbXBsZS5jb20iLCJpYXQiOjE3MzMwNzM2MDAsImV4cCI6MTczMzE2MDAwMH0.abc123..."
}
```

Decoded Payload:
```json
{
  "role": "TRANSPORTEUR",
  "sub": "transporter@example.com",
  "iat": 1733073600,
  "exp": 1733160000
}
```

---

## How to Extract Role from JWT (Frontend)

### Option 1: Manual Decoding (No Libraries)

```typescript
// decode-jwt.ts
export function decodeJWT(token: string): any {
  try {
    // JWT has 3 parts: header.payload.signature
    const parts = token.split('.');
    
    if (parts.length !== 3) {
      throw new Error('Invalid JWT token');
    }
    
    // Decode the payload (part 2)
    const payload = parts[1];
    
    // Base64 decode
    const decodedPayload = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    
    // Parse JSON
    return JSON.parse(decodedPayload);
  } catch (error) {
    console.error('Error decoding JWT:', error);
    return null;
  }
}

// Usage
const token = localStorage.getItem('authToken');
const decoded = decodeJWT(token);

console.log('User email:', decoded.sub);
console.log('User role:', decoded.role);
console.log('Expires at:', new Date(decoded.exp * 1000));
```

### Option 2: Using jwt-decode Library (Recommended)

```bash
npm install jwt-decode
```

```typescript
import { jwtDecode } from 'jwt-decode';

interface JwtPayload {
  role: string;
  sub: string;
  iat: number;
  exp: number;
}

// In your auth service
export class AuthService {
  
  getUserRole(): string | null {
    const token = localStorage.getItem('authToken');
    
    if (!token) {
      return null;
    }
    
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      return decoded.role;
    } catch (error) {
      console.error('Invalid token:', error);
      return null;
    }
  }
  
  getUserEmail(): string | null {
    const token = localStorage.getItem('authToken');
    
    if (!token) {
      return null;
    }
    
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      return decoded.sub;
    } catch (error) {
      console.error('Invalid token:', error);
      return null;
    }
  }
  
  isTokenExpired(): boolean {
    const token = localStorage.getItem('authToken');
    
    if (!token) {
      return true;
    }
    
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      const currentTime = Date.now() / 1000;
      return decoded.exp < currentTime;
    } catch (error) {
      return true;
    }
  }
  
  isClient(): boolean {
    return this.getUserRole() === 'CLIENT';
  }
  
  isTransporteur(): boolean {
    return this.getUserRole() === 'TRANSPORTEUR';
  }
  
  isAdmin(): boolean {
    return this.getUserRole() === 'ADMIN';
  }
}
```

### Angular Component Example

```typescript
// profile.component.ts
import { Component, OnInit } from '@angular/core';
import { AuthService } from './services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
  
  userRole: string | null = null;
  
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    this.userRole = this.authService.getUserRole();
    
    // Redirect based on role
    if (this.userRole === 'CLIENT') {
      this.loadClientProfile();
    } else if (this.userRole === 'TRANSPORTEUR') {
      this.loadTransporteurProfile();
    } else {
      this.router.navigate(['/login']);
    }
  }
  
  loadClientProfile(): void {
    // Load client-specific data
  }
  
  loadTransporteurProfile(): void {
    // Load transporteur-specific data
  }
}
```

### Angular Route Guard Example

```typescript
// auth.guard.ts
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './services/auth.service';

export const clientGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (authService.isClient()) {
    return true;
  }
  
  router.navigate(['/unauthorized']);
  return false;
};

export const transporteurGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (authService.isTransporteur()) {
    return true;
  }
  
  router.navigate(['/unauthorized']);
  return false;
};

// Use in routes:
const routes: Routes = [
  {
    path: 'client/profile',
    component: ClientProfileComponent,
    canActivate: [clientGuard]
  },
  {
    path: 'transporteur/profile',
    component: TransporteurProfileComponent,
    canActivate: [transporteurGuard]
  }
];
```

---

## How to Extract Role from JWT (Backend)

The backend already extracts the role automatically through `CustomUserDetailsService`. But if you need to manually extract it:

```java
// In any service or controller
@Autowired
private JwtUtil jwtUtil;

public void someMethod(String token) {
    // Extract role
    String role = jwtUtil.extractRole(token);
    System.out.println("User role: " + role);
    
    // Extract username
    String username = jwtUtil.extractUsername(token);
    System.out.println("User email: " + username);
    
    // Get all claims
    Claims claims = jwtUtil.extractAllClaims(token);
    System.out.println("Issued at: " + claims.getIssuedAt());
    System.out.println("Expires at: " + claims.getExpiration());
}
```

---

## Testing JWT Tokens

### 1. Using jwt.io (Online Decoder)

1. Go to https://jwt.io/
2. Paste your JWT token in the "Encoded" field
3. See the decoded header and payload
4. **DO NOT** paste production tokens on public websites!

### 2. Using Browser Console

```javascript
// In browser console (F12)
const token = localStorage.getItem('authToken');

// Decode manually
const parts = token.split('.');
const payload = JSON.parse(atob(parts[1]));

console.log('Payload:', payload);
console.log('Role:', payload.role);
console.log('Email:', payload.sub);
console.log('Expires:', new Date(payload.exp * 1000));
```

### 3. Using Postman

1. **Login Request:**
   ```
   POST http://localhost:8080/api/auth/login
   Body:
   {
     "email": "transporter@example.com",
     "password": "password123"
   }
   ```

2. **Copy the token from response**

3. **Decode the token:**
   - Click on the token value
   - Select "JSON" view
   - Or use Postman's "Auth" tab → "Bearer Token"

4. **Test protected endpoint:**
   ```
   GET http://localhost:8080/api/profil/transporteur
   Headers:
     Authorization: Bearer <your-token>
   ```

---

## Security Best Practices

### ⚠️ Important Security Warnings

1. **Never Trust the Client**
   - The frontend can read and decode the JWT
   - The frontend can see the role
   - But the frontend **CANNOT** modify the role (signature will fail)
   - **Always verify permissions on the backend!**

2. **Token Storage**
   ```typescript
   // ❌ BAD: Vulnerable to XSS
   localStorage.setItem('authToken', token);
   
   // ✅ BETTER: Use httpOnly cookies (requires backend changes)
   // Or implement proper XSS protection
   ```

3. **Token Validation**
   ```typescript
   // Always check if token is expired
   if (authService.isTokenExpired()) {
     authService.logout();
     router.navigate(['/login']);
   }
   ```

4. **Sensitive Data**
   - **DO NOT** store sensitive data in JWT payload
   - JWT payload is readable by anyone (it's just Base64 encoded)
   - Only store: user ID, email, role, expiration

5. **HTTPS Only**
   - Always use HTTPS in production
   - Tokens can be intercepted over HTTP

### What's Safe in JWT?

✅ **Safe to store:**
- User ID
- Email
- Role
- Expiration time
- Issued time
- Username

❌ **NEVER store:**
- Passwords
- Credit card numbers
- Social security numbers
- Private keys
- API secrets

---

## Example: Complete Frontend Flow

```typescript
// 1. Login
login(email: string, password: string) {
  this.http.post<{token: string}>('http://localhost:8080/api/auth/login', {
    email,
    password
  }).subscribe({
    next: (response) => {
      // Store token
      localStorage.setItem('authToken', response.token);
      
      // Decode token to get role
      const decoded = jwtDecode<JwtPayload>(response.token);
      
      // Redirect based on role
      if (decoded.role === 'CLIENT') {
        this.router.navigate(['/client/dashboard']);
      } else if (decoded.role === 'TRANSPORTEUR') {
        this.router.navigate(['/transporteur/dashboard']);
      }
    },
    error: (err) => {
      console.error('Login failed:', err);
    }
  });
}

// 2. Make authenticated requests
getProfile() {
  const token = localStorage.getItem('authToken');
  const role = this.getUserRole();
  
  const url = role === 'CLIENT' 
    ? 'http://localhost:8080/api/profil/client'
    : 'http://localhost:8080/api/profil/transporteur';
  
  return this.http.get(url, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
}

// 3. Check permissions
canAccessTransporteurFeatures(): boolean {
  const role = this.getUserRole();
  return role === 'TRANSPORTEUR' || role === 'ADMIN';
}
```

---

## Summary

### Your JWT Now Contains:
- ✅ **Username/Email** (`sub` field)
- ✅ **Role** (`role` field) - **NEW!**
- ✅ **Issue time** (`iat` field)
- ✅ **Expiration time** (`exp` field)

### How to Use It:
1. **Backend**: Already configured to include role in token
2. **Frontend**: Decode the token to extract the role
3. **Routing**: Use role to show/hide features
4. **Security**: Always verify on backend, don't trust client

### Next Steps for Frontend Team:
1. Install `jwt-decode`: `npm install jwt-decode`
2. Create an `AuthService` with role extraction methods
3. Implement route guards based on role
4. Show/hide UI elements based on role
5. Always send `Authorization: Bearer <token>` header

---

**Note**: After these changes, **users must login again** to get a new token with the role included!
