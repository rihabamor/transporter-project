# The Transporter - API Documentation
**Version:** 1.0  
**Base URL:** `http://localhost:8080`  
**Last Updated:** December 1, 2025

---

## Table of Contents
1. [Authentication](#authentication)
2. [General Information](#general-information)
3. [API Endpoints](#api-endpoints)
   - [Public Endpoints](#public-endpoints)
   - [Authentication Endpoints](#authentication-endpoints)
   - [Client Profile Endpoints](#client-profile-endpoints)
   - [Transporter Profile Endpoints](#transporter-profile-endpoints)
   - [Transporter Management Endpoints](#transporter-management-endpoints)
4. [Error Responses](#error-responses)
5. [Data Models](#data-models)

---

## Authentication

### JWT Token-Based Authentication
Most endpoints require a valid JWT token obtained from the login endpoint.

**How to authenticate:**
1. Login via `/api/auth/login`
2. Receive JWT token in response
3. Include token in subsequent requests:
   ```
   Authorization: Bearer <your-jwt-token>
   ```

**Token Expiration:** 24 hours (86400000 ms)

---

## General Information

### CORS Configuration
- **Allowed Origin:** `http://localhost:4200` (Angular development server)
- **Allowed Methods:** GET, POST, PUT, DELETE, OPTIONS
- **Allowed Headers:** Authorization, Content-Type

### Content Type
- **Request:** `application/json`
- **Response:** `application/json`

### HTTP Status Codes
- `200 OK` - Request successful
- `400 Bad Request` - Validation error or business logic error
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Insufficient permissions
- `500 Internal Server Error` - Server error

---

## API Endpoints

## Public Endpoints

### 1. Welcome Message
**Endpoint:** `GET /`  
**Authentication:** Not required  
**Description:** Returns a welcome message

**Response:**
```json
"Bienvenue sur l'API The Transporter 🚚"
```

---

### 2. Health Check
**Endpoint:** `GET /api/test`  
**Authentication:** Not required  
**Description:** API health check

**Response:**
```json
"✅ API is running correctly!"
```

---

## Authentication Endpoints

### 3. User Registration
**Endpoint:** `POST /api/auth/register`  
**Authentication:** Not required  
**Description:** Register a new user (Client or Transporter)

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "role": "CLIENT",
  "nom": "Dupont",
  "prenom": "Jean",
  "telephone": "+33612345678",
  "adresse": "123 Rue de Paris",
  "ville": "Lyon"
}
```

**Request Body (Transporter):**
```json
{
  "email": "transporter@example.com",
  "password": "password123",
  "role": "TRANSPORTEUR",
  "nom": "Martin",
  "prenom": "Pierre",
  "telephone": "+33687654321",
  "localisation": "Paris"
}
```

**Validation Rules:**
- `email`: Required, must be valid email format, unique
- `password`: Required, minimum 6 characters
- `role`: Required, must be "CLIENT" or "TRANSPORTEUR"
- `nom`: Required for both roles
- `prenom`: Required for both roles
- `telephone`: Optional
- `adresse`: Optional (Client only)
- `ville`: Optional (Client only)
- `localisation`: Optional (Transporter only)

**Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQ0xJRU5UIiwic3ViIjoidXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTczMzA3MzYwMCwiZXhwIjoxNzMzMTYwMDAwfQ.abc123..."
}
```

**Decoded Token Payload:**
```json
{
  "role": "CLIENT",
  "sub": "user@example.com",
  "iat": 1733073600,
  "exp": 1733160000
}
```

**Important**: The JWT token contains the user's role. You can decode it on the frontend to determine which features to show. See `JWT_STRUCTURE_GUIDE.md` for complete details on decoding and using JWT tokens.

**Error Response (400):**
```json
{
  "timestamp": "2025-12-01T10:30:00",
  "message": "Validation failed",
  "details": "{email=Format d'email invalide, password=Le mot de passe doit contenir au moins 6 caractères}"
}
```

---

### 4. User Login
**Endpoint:** `POST /api/auth/login`  
**Authentication:** Not required  
**Description:** Authenticate user and receive JWT token

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error Response (400):**
```json
{
  "timestamp": "2025-12-01T10:30:00",
  "message": "Erreur",
  "details": "Email ou mot de passe incorrect"
}
```

---

### 5. User Logout
**Endpoint:** `POST /api/auth/logout`  
**Authentication:** Not required (stateless)  
**Description:** Logout user (client-side should discard token)

**Success Response (200):**
```json
"Déconnexion réussie"
```

**Note:** Since this is a stateless JWT implementation, the token remains valid until expiration. The client should delete the token from storage.

---

## Client Profile Endpoints

### 6. Get Client Dashboard
**Endpoint:** `GET /api/profil/client`  
**Authentication:** Required (ROLE_CLIENT)  
**Description:** Retrieve client profile and dashboard information

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Success Response (200):**
```json
{
  "profil": {
    "idClient": 1,
    "compte": {
      "id": 1,
      "email": "client@example.com",
      "role": "CLIENT",
      "dateCreation": "2025-12-01T10:00:00Z"
    },
    "nom": "Dupont",
    "prenom": "Jean",
    "telephone": "+33612345678",
    "adresse": "123 Rue de Paris",
    "ville": "Lyon"
  },
  "missionsEffectuees": 0,
  "missionsEnCours": 0,
  "messageBienvenue": "Bienvenue sur votre tableau de bord client"
}
```

**Error Response (400):**
```json
{
  "timestamp": "2025-12-01T10:30:00",
  "message": "Erreur",
  "details": "Profil client non trouvé"
}
```

---

### 7. Update Client Profile
**Endpoint:** `PUT /api/profil/client`  
**Authentication:** Required (ROLE_CLIENT)  
**Description:** Update client profile information

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Request Body:**
```json
{
  "nom": "Dupont",
  "prenom": "Jean",
  "telephone": "+33612345678",
  "adresse": "456 Avenue des Champs",
  "ville": "Paris",
  "email": "newemail@example.com"
}
```

**Validation Rules:**
- `nom`: Required
- `prenom`: Required
- `telephone`: Required
- `adresse`: Optional
- `ville`: Optional
- `email`: Optional, must be valid email format

**Success Response (200):**
```json
{
  "idClient": 1,
  "compte": {
    "id": 1,
    "email": "newemail@example.com",
    "role": "CLIENT",
    "dateCreation": "2025-12-01T10:00:00Z"
  },
  "nom": "Dupont",
  "prenom": "Jean",
  "telephone": "+33612345678",
  "adresse": "456 Avenue des Champs",
  "ville": "Paris"
}
```

---

## Transporter Profile Endpoints

### 8. Get Transporter Dashboard
**Endpoint:** `GET /api/profil/transporteur`  
**Authentication:** Required (ROLE_TRANSPORTEUR)  
**Description:** Retrieve transporter profile and dashboard information

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Success Response (200):**
```json
{
  "profil": {
    "idTransporteur": 1,
    "compte": {
      "id": 2,
      "email": "transporter@example.com",
      "role": "TRANSPORTEUR",
      "dateCreation": "2025-12-01T10:00:00Z"
    },
    "nom": "Martin",
    "prenom": "Pierre",
    "telephone": "+33687654321",
    "localisation": "Paris",
    "noteMoyenne": 4.5,
    "disponible": true
  },
  "missionsEffectuees": 0,
  "missionsEnCours": 0,
  "messageBienvenue": "Bienvenue sur votre tableau de bord transporteur"
}
```

**Error Response (400):**
```json
{
  "timestamp": "2025-12-01T10:30:00",
  "message": "Erreur",
  "details": "Profil transporteur non trouvé"
}
```

---

### 9. Update Transporter Profile
**Endpoint:** `PUT /api/profil/transporteur`  
**Authentication:** Required (ROLE_TRANSPORTEUR)  
**Description:** Update transporter profile information

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Request Body:**
```json
{
  "nom": "Martin",
  "prenom": "Pierre",
  "telephone": "+33687654321",
  "localisation": "Lyon",
  "email": "newemail@example.com"
}
```

**Validation Rules:**
- `nom`: Required
- `prenom`: Required
- `telephone`: Required
- `localisation`: Optional
- `email`: Optional, must be valid email format

**Success Response (200):**
```json
{
  "idTransporteur": 1,
  "compte": {
    "id": 2,
    "email": "newemail@example.com",
    "role": "TRANSPORTEUR",
    "dateCreation": "2025-12-01T10:00:00Z"
  },
  "nom": "Martin",
  "prenom": "Pierre",
  "telephone": "+33687654321",
  "localisation": "Lyon",
  "noteMoyenne": 4.5,
  "disponible": true
}
```

---

### 10. Update Transporter Availability
**Endpoint:** `PUT /api/profil/transporteur/disponibilite`  
**Authentication:** Required (ROLE_TRANSPORTEUR)  
**Description:** Toggle transporter availability status

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Request Body:**
```json
true
```
or
```json
false
```

**Success Response (200):**
```json
{
  "idTransporteur": 1,
  "compte": {
    "id": 2,
    "email": "transporter@example.com",
    "role": "TRANSPORTEUR",
    "dateCreation": "2025-12-01T10:00:00Z"
  },
  "nom": "Martin",
  "prenom": "Pierre",
  "telephone": "+33687654321",
  "localisation": "Paris",
  "noteMoyenne": 4.5,
  "disponible": false
}
```

---

## Transporter Management Endpoints

⚠️ **Warning:** These endpoints currently have NO AUTHENTICATION. This is a security issue that should be fixed before production.

### 11. Get All Transporters
**Endpoint:** `GET /api/transporteurs`  
**Authentication:** Currently not required (⚠️ SECURITY ISSUE)  
**Description:** Retrieve list of all transporters

**Success Response (200):**
```json
[
  {
    "idTransporteur": 1,
    "compte": {
      "id": 2,
      "email": "transporter1@example.com",
      "role": "TRANSPORTEUR",
      "dateCreation": "2025-12-01T10:00:00Z"
    },
    "nom": "Martin",
    "prenom": "Pierre",
    "telephone": "+33687654321",
    "localisation": "Paris",
    "noteMoyenne": 4.5,
    "disponible": true
  },
  {
    "idTransporteur": 2,
    "compte": {
      "id": 3,
      "email": "transporter2@example.com",
      "role": "TRANSPORTEUR",
      "dateCreation": "2025-12-01T09:00:00Z"
    },
    "nom": "Bernard",
    "prenom": "Sophie",
    "telephone": "+33698765432",
    "localisation": "Lyon",
    "noteMoyenne": 4.8,
    "disponible": false
  }
]
```

---

### 12. Create Transporter (Direct)
**Endpoint:** `POST /api/transporteurs`  
**Authentication:** Currently not required (⚠️ SECURITY ISSUE)  
**Description:** Create a new transporter directly (bypasses normal registration)

**Request Body:**
```json
{
  "compte": {
    "email": "newTransporter@example.com",
    "password": "hashedPassword",
    "role": "TRANSPORTEUR"
  },
  "nom": "Nouveau",
  "prenom": "Transporteur",
  "telephone": "+33612345678",
  "localisation": "Marseille",
  "noteMoyenne": 0.0,
  "disponible": true
}
```

**Success Response (200):**
```json
{
  "idTransporteur": 3,
  "compte": {
    "id": 4,
    "email": "newTransporter@example.com",
    "role": "TRANSPORTEUR",
    "dateCreation": "2025-12-01T11:00:00Z"
  },
  "nom": "Nouveau",
  "prenom": "Transporteur",
  "telephone": "+33612345678",
  "localisation": "Marseille",
  "noteMoyenne": 0.0,
  "disponible": true
}
```

⚠️ **Note:** This endpoint should be secured or removed. Use `/api/auth/register` instead for creating transporters.

---

## Error Responses

### Validation Error (400)
```json
{
  "timestamp": "2025-12-01T10:30:00",
  "message": "Validation failed",
  "details": "{fieldName=Error message, anotherField=Another error}"
}
```

### Business Logic Error (400)
```json
{
  "timestamp": "2025-12-01T10:30:00",
  "message": "Erreur",
  "details": "Email déjà utilisé !"
}
```

### Unauthorized (401)
```json
{
  "timestamp": "2025-12-01T10:30:00",
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/profil/client"
}
```

### Forbidden (403)
```json
{
  "timestamp": "2025-12-01T10:30:00",
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/profil/transporteur"
}
```

### Internal Server Error (500)
```json
{
  "timestamp": "2025-12-01T10:30:00",
  "message": "Erreur interne du serveur",
  "details": "Error message details"
}
```

---

## Data Models

### Compte (Account)
```typescript
{
  id: number;
  email: string;
  password: string; // Hashed, never returned in responses
  role: "CLIENT" | "TRANSPORTEUR" | "ADMIN";
  dateCreation: string; // ISO 8601 format
}
```

### Client
```typescript
{
  idClient: number;
  compte: Compte;
  nom: string;
  prenom: string;
  telephone: string;
  adresse: string;
  ville: string;
}
```

### Transporteur
```typescript
{
  idTransporteur: number;
  compte: Compte;
  nom: string;
  prenom: string;
  telephone: string;
  localisation: string;
  noteMoyenne: number; // 0.0 to 5.0
  disponible: boolean;
}
```

### AuthResponse
```typescript
{
  token: string; // JWT token
}
```

### DashboardResponse
```typescript
{
  profil: Client | Transporteur;
  missionsEffectuees: number;
  missionsEnCours: number;
  messageBienvenue: string;
}
```

### ErrorResponse
```typescript
{
  timestamp: string; // ISO 8601 format
  message: string;
  details: string;
}
```

---

## Integration Examples

### Example 1: User Registration and Login Flow

```javascript
// 1. Register a new client
const registerResponse = await fetch('http://localhost:8080/api/auth/register', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    email: 'client@example.com',
    password: 'securePassword123',
    role: 'CLIENT',
    nom: 'Dupont',
    prenom: 'Jean',
    telephone: '+33612345678',
    adresse: '123 Rue de Paris',
    ville: 'Lyon'
  })
});

const { token } = await registerResponse.json();
// Store token in localStorage or sessionStorage
localStorage.setItem('authToken', token);
```

### Example 2: Accessing Protected Endpoint

```javascript
// 2. Get client dashboard
const token = localStorage.getItem('authToken');

const dashboardResponse = await fetch('http://localhost:8080/api/profil/client', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

const dashboardData = await dashboardResponse.json();
console.log(dashboardData);
```

### Example 3: Update Profile

```javascript
// 3. Update client profile
const updateResponse = await fetch('http://localhost:8080/api/profil/client', {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    nom: 'Dupont',
    prenom: 'Jean',
    telephone: '+33612345678',
    adresse: '456 Avenue Nouvelle',
    ville: 'Paris',
    email: 'client@example.com'
  })
});

const updatedProfile = await updateResponse.json();
```

### Example 4: Toggle Transporter Availability

```javascript
// 4. Set transporter as unavailable
const availabilityResponse = await fetch('http://localhost:8080/api/profil/transporteur/disponibilite', {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(false)
});

const updatedTransporter = await availabilityResponse.json();
```

---

## Important Notes for UI Team

### 1. Token Management
- Store JWT token securely (consider using httpOnly cookies for production)
- Include token in `Authorization` header for all protected endpoints
- Implement token refresh mechanism (not yet available in backend)
- Clear token on logout

### 2. Error Handling
- Always check HTTP status codes
- Parse error responses and display appropriate messages to users
- Handle 401 (redirect to login) and 403 (show access denied message)

### 3. CORS
- Development server must run on `http://localhost:4200`
- For production, backend CORS configuration needs to be updated

### 4. Date Formats
- All dates are in ISO 8601 format
- Example: `2025-12-01T10:30:00Z`
- Parse using `new Date()` in JavaScript

### 5. Role-Based UI
- Check user role after login to display appropriate interface
- CLIENT users should only see client features
- TRANSPORTEUR users should only see transporter features

### 6. Current Limitations
- Mission/booking functionality not yet implemented (hardcoded to 0)
- No pagination on list endpoints (will return all records)
- No file upload capabilities (profile pictures, documents)
- No real-time updates (WebSocket not implemented)

### 7. Testing Credentials
Create test accounts using the registration endpoint, or contact backend team for pre-created test accounts.

---

## Changelog

### Version 1.0 (December 1, 2025)
- Initial API documentation
- Basic authentication and profile management endpoints
- JWT token-based security

---

## Contact

For questions or issues regarding the API, please contact the backend development team.

**Backend Repository:** transporter-backend  
**Owner:** rihabamor  
**Branch:** master
