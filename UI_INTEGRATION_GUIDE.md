# 🚚 Guide d'Intégration UI - Tracking GPS Transporteur

## 📋 Vue d'Ensemble

Ce guide explique comment intégrer le système de tracking GPS en temps réel dans l'interface utilisateur Angular. Le système utilise un **polling HTTP simple** (pas de WebSocket) pour une implémentation facile et fiable.

---

## 🎯 Fonctionnalités

✅ Simulation GPS réaliste entre deux points en Tunisie  
✅ Trajet d'une heure (60 minutes) entre départ et arrivée  
✅ Pourcentage de progression en temps réel (0-100%)  
✅ Vitesse simulée aléatoire (50-80 km/h)  
✅ API REST simple - pas de WebSocket complexe  
✅ Fonctionne uniquement quand mission status = `EN_COURS`

---

## 📍 Coordonnées de Simulation

Le système simule automatiquement un trajet entre ces deux points en Tunisie:

- **Point de départ**: `35.669948, 10.591675`
- **Point d'arrivée**: `35.522941, 11.031608`
- **Durée totale**: 60 minutes
- **Mise à jour**: Polling toutes les 3-5 secondes recommandé

---

## 🔌 API Endpoint

### **GET** `/api/tracking/missions/{missionId}/location`

Récupère la position GPS actuelle d'une mission.

#### **URL complète**
```
http://localhost:8080/api/tracking/missions/{missionId}/location
```

#### **Paramètres**
- `{missionId}`: ID de la mission (Long)

#### **Authentification**
❌ **Non requise** pour cet endpoint (public)

#### **Réponse Success (200 OK)**

```json
{
  "missionId": 15,
  "latitude": 35.652341,
  "longitude": 10.723456,
  "timestamp": "2025-12-01T14:35:22",
  "progressPercentage": 45,
  "speed": 67.3,
  "status": "EN_COURS"
}
```

#### **Champs de la Réponse**

| Champ | Type | Description |
|-------|------|-------------|
| `missionId` | Long | ID de la mission |
| `latitude` | Double | Latitude GPS actuelle |
| `longitude` | Double | Longitude GPS actuelle |
| `timestamp` | LocalDateTime | Horodatage de la position |
| `progressPercentage` | Integer | Progression (0-100%) |
| `speed` | Double | Vitesse en km/h |
| `status` | String | Statut de la mission (EN_COURS, TERMINEE, etc.) |

#### **Cas Particuliers**

**Mission non EN_COURS:**
```json
{
  "missionId": 15,
  "latitude": null,
  "longitude": null,
  "timestamp": "2025-12-01T14:35:22",
  "progressPercentage": 0,
  "speed": 0.0,
  "status": "EN_ATTENTE"
}
```

**Mission non trouvée (404 Not Found):**
```
Status: 404
Body: (vide)
```

---

## 💻 Implémentation Angular

### **Étape 1: Créer le Service TypeScript**

Créez `src/app/services/tracking.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, interval, switchMap, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

export interface LocationUpdate {
  missionId: number;
  latitude: number | null;
  longitude: number | null;
  timestamp: string;
  progressPercentage: number;
  speed: number;
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class TrackingService {
  private readonly API_URL = 'http://localhost:8080/api/tracking';

  constructor(private http: HttpClient) {}

  /**
   * Obtenir la position actuelle d'une mission
   */
  getCurrentLocation(missionId: number): Observable<LocationUpdate> {
    return this.http.get<LocationUpdate>(
      `${this.API_URL}/missions/${missionId}/location`
    ).pipe(
      catchError(error => {
        console.error('Erreur récupération position:', error);
        return of(this.getEmptyLocation(missionId));
      })
    );
  }

  /**
   * Démarrer le polling automatique toutes les 3 secondes
   */
  startLocationPolling(missionId: number): Observable<LocationUpdate> {
    return interval(3000).pipe( // Polling toutes les 3 secondes
      switchMap(() => this.getCurrentLocation(missionId)),
      tap(location => console.log('📍 Position reçue:', location))
    );
  }

  private getEmptyLocation(missionId: number): LocationUpdate {
    return {
      missionId,
      latitude: null,
      longitude: null,
      timestamp: new Date().toISOString(),
      progressPercentage: 0,
      speed: 0,
      status: 'UNKNOWN'
    };
  }
}
```

---

### **Étape 2: Créer le Composant de Suivi**

Créez `src/app/components/mission-tracking/mission-tracking.component.ts`:

```typescript
import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { Subscription } from 'rxjs';
import { TrackingService, LocationUpdate } from '../../services/tracking.service';

@Component({
  selector: 'app-mission-tracking',
  templateUrl: './mission-tracking.component.html',
  styleUrls: ['./mission-tracking.component.css']
})
export class MissionTrackingComponent implements OnInit, OnDestroy {
  @Input() missionId!: number;

  currentLocation: LocationUpdate | null = null;
  isTracking: boolean = false;
  private pollingSubscription?: Subscription;

  constructor(private trackingService: TrackingService) {}

  ngOnInit(): void {
    if (this.missionId) {
      this.startTracking();
    }
  }

  ngOnDestroy(): void {
    this.stopTracking();
  }

  startTracking(): void {
    this.isTracking = true;
    
    // Polling automatique toutes les 3 secondes
    this.pollingSubscription = this.trackingService
      .startLocationPolling(this.missionId)
      .subscribe(location => {
        this.currentLocation = location;
        
        // Arrêter le polling si mission terminée
        if (location.progressPercentage >= 100 || 
            location.status === 'TERMINEE' || 
            location.status === 'ANNULEE') {
          this.stopTracking();
        }
      });
  }

  stopTracking(): void {
    this.isTracking = false;
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
    }
  }

  refreshLocation(): void {
    this.trackingService.getCurrentLocation(this.missionId)
      .subscribe(location => {
        this.currentLocation = location;
      });
  }
}
```

---

### **Étape 3: Template HTML**

Créez `src/app/components/mission-tracking/mission-tracking.component.html`:

```html
<div class="tracking-container" *ngIf="currentLocation">
  <!-- Status Badge -->
  <div class="status-badge" [ngClass]="{
    'status-en-cours': currentLocation.status === 'EN_COURS',
    'status-terminee': currentLocation.status === 'TERMINEE',
    'status-annulee': currentLocation.status === 'ANNULEE'
  }">
    {{ currentLocation.status }}
  </div>

  <!-- Barre de progression -->
  <div class="progress-section">
    <h3>Progression du trajet</h3>
    <div class="progress-bar">
      <div class="progress-fill" 
           [style.width.%]="currentLocation.progressPercentage">
      </div>
    </div>
    <p class="progress-text">{{ currentLocation.progressPercentage }}% complété</p>
  </div>

  <!-- Informations GPS -->
  <div class="location-info" *ngIf="currentLocation.latitude">
    <div class="info-row">
      <span class="label">📍 Latitude:</span>
      <span class="value">{{ currentLocation.latitude | number:'1.6-6' }}</span>
    </div>
    <div class="info-row">
      <span class="label">📍 Longitude:</span>
      <span class="value">{{ currentLocation.longitude | number:'1.6-6' }}</span>
    </div>
    <div class="info-row">
      <span class="label">🚗 Vitesse:</span>
      <span class="value">{{ currentLocation.speed | number:'1.1-1' }} km/h</span>
    </div>
    <div class="info-row">
      <span class="label">⏰ Mise à jour:</span>
      <span class="value">{{ currentLocation.timestamp | date:'short' }}</span>
    </div>
  </div>

  <!-- Message si mission pas en cours -->
  <div class="no-tracking" *ngIf="!currentLocation.latitude">
    <p>❌ Mission non active - Tracking indisponible</p>
  </div>

  <!-- Bouton refresh manuel -->
  <button class="btn-refresh" (click)="refreshLocation()">
    🔄 Actualiser
  </button>
</div>
```

---

### **Étape 4: Styles CSS**

Créez `src/app/components/mission-tracking/mission-tracking.component.css`:

```css
.tracking-container {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 20px;
  max-width: 600px;
  margin: 20px auto;
}

.status-badge {
  display: inline-block;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 20px;
}

.status-en-cours {
  background: #4CAF50;
  color: white;
}

.status-terminee {
  background: #2196F3;
  color: white;
}

.status-annulee {
  background: #f44336;
  color: white;
}

.progress-section {
  margin-bottom: 30px;
}

.progress-section h3 {
  margin-bottom: 10px;
  color: #333;
}

.progress-bar {
  width: 100%;
  height: 30px;
  background: #e0e0e0;
  border-radius: 15px;
  overflow: hidden;
  margin-bottom: 10px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #8BC34A);
  transition: width 0.5s ease;
}

.progress-text {
  text-align: center;
  font-weight: bold;
  font-size: 18px;
  color: #4CAF50;
}

.location-info {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #ddd;
}

.info-row:last-child {
  border-bottom: none;
}

.label {
  font-weight: 600;
  color: #555;
}

.value {
  color: #333;
  font-family: monospace;
}

.no-tracking {
  text-align: center;
  padding: 30px;
  color: #999;
}

.btn-refresh {
  width: 100%;
  padding: 12px;
  background: #2196F3;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-refresh:hover {
  background: #1976D2;
}
```

---

### **Étape 5: Utiliser le Composant**

Dans votre composant parent (ex: `mission-detail.component.html`):

```html
<div class="mission-details">
  <!-- Autres informations de la mission -->
  
  <!-- Composant de tracking -->
  <app-mission-tracking 
    *ngIf="mission.statut === 'EN_COURS'"
    [missionId]="mission.idMission">
  </app-mission-tracking>
</div>
```

---

## 🗺️ Intégration avec Google Maps / Leaflet

### **Option A: Leaflet (Gratuit, Recommandé)**

```bash
npm install leaflet @types/leaflet
```

**Component TypeScript:**
```typescript
import * as L from 'leaflet';

export class MissionTrackingComponent implements OnInit {
  private map?: L.Map;
  private marker?: L.Marker;

  ngAfterViewInit(): void {
    this.initMap();
  }

  initMap(): void {
    // Initialiser la carte centrée sur Tunisia
    this.map = L.map('map').setView([35.67, 10.59], 10);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // Ajouter un marqueur initial
    this.marker = L.marker([35.669948, 10.591675]).addTo(this.map);
  }

  updateMapPosition(location: LocationUpdate): void {
    if (!location.latitude || !location.longitude || !this.map || !this.marker) return;

    const newLatLng = L.latLng(location.latitude, location.longitude);
    
    // Déplacer le marqueur
    this.marker.setLatLng(newLatLng);
    
    // Centrer la carte
    this.map.panTo(newLatLng);
    
    // Ajouter popup avec infos
    this.marker.bindPopup(`
      <b>Mission ${location.missionId}</b><br>
      Vitesse: ${location.speed.toFixed(1)} km/h<br>
      Progression: ${location.progressPercentage}%
    `).openPopup();
  }
}
```

**Template HTML:**
```html
<div id="map" style="height: 400px; border-radius: 8px;"></div>
```

---

## ⏱️ Fréquence de Polling Recommandée

| Cas d'usage | Fréquence | Raison |
|-------------|-----------|--------|
| **Vue détaillée mission** | 3 secondes | Mise à jour fluide |
| **Liste missions** | 10 secondes | Réduire charge réseau |
| **Mission terminée** | Arrêter | Économiser ressources |
| **App en arrière-plan** | Arrêter | Batterie mobile |

---

## 🔧 Gestion d'Erreurs

### **Exemple avec Retry Logic**

```typescript
import { retry, delay } from 'rxjs/operators';

startLocationPolling(missionId: number): Observable<LocationUpdate> {
  return interval(3000).pipe(
    switchMap(() => this.getCurrentLocation(missionId)),
    retry({
      count: 3,  // Réessayer 3 fois
      delay: 2000  // Attendre 2 secondes entre chaque tentative
    }),
    catchError(error => {
      console.error('Impossible de récupérer la position après 3 tentatives');
      return of(this.getEmptyLocation(missionId));
    })
  );
}
```

---

## 🧪 Test de l'API

### **Test avec cURL**

```bash
# Remplacer {missionId} par un ID réel (ex: 15)
curl -X GET "http://localhost:8080/api/tracking/missions/15/location"
```

### **Test avec Postman**

1. Créer une nouvelle requête GET
2. URL: `http://localhost:8080/api/tracking/missions/15/location`
3. Cliquer sur **Send**
4. Vérifier la réponse JSON

### **Test dans le navigateur**

Ouvrir directement:
```
http://localhost:8080/api/tracking/missions/15/location
```

---

## 📊 Logique de Simulation

### **Comment ça Marche?**

1. **Démarrage**: Quand une mission passe à statut `EN_COURS`, le système enregistre l'heure de départ
2. **Calcul Position**: À chaque appel API, le backend calcule le temps écoulé (en minutes)
3. **Interpolation**: Position = Départ + (Arrivée - Départ) × (Temps Écoulé / 60 minutes)
4. **Progression**: Pourcentage = (Temps Écoulé / 60) × 100
5. **Fin**: Après 60 minutes, la position reste fixe à l'arrivée

### **Exemple Chronologique**

| Temps Écoulé | Progression | Position Approximative |
|--------------|-------------|------------------------|
| 0 min | 0% | 35.669948, 10.591675 (Départ) |
| 15 min | 25% | 35.654, 10.70 |
| 30 min | 50% | 35.596, 10.81 |
| 45 min | 75% | 35.560, 10.92 |
| 60 min | 100% | 35.522941, 11.031608 (Arrivée) |

---

## ⚠️ Points Importants

### ✅ **À FAIRE**
- Arrêter le polling quand le composant est détruit (`ngOnDestroy`)
- Vérifier que `mission.statut === 'EN_COURS'` avant d'afficher le tracking
- Gérer les erreurs réseau (retry, fallback)
- Afficher un loader pendant le chargement initial

### ❌ **À NE PAS FAIRE**
- Polling à moins de 1 seconde (charge serveur)
- Oublier de unsubscribe (memory leak)
- Afficher le tracking pour missions non EN_COURS
- Bloquer l'UI pendant le chargement

---

## 🚀 Améliorations Futures Possibles

1. **Historique de trajet**: Afficher le chemin parcouru sur la carte
2. **Notifications**: Alerter quand progression atteint 25%, 50%, 75%, 100%
3. **ETA**: Calculer l'heure d'arrivée estimée
4. **Distance restante**: Afficher les km restants
5. **Geocoding inversé**: Convertir lat/lon en adresse lisible

---

## 📞 Support

Si vous rencontrez des problèmes:
1. Vérifier que le backend est démarré (`http://localhost:8080`)
2. Tester l'API avec cURL ou Postman
3. Vérifier la console navigateur pour les erreurs CORS
4. S'assurer que la mission a bien le statut `EN_COURS`

---

## ✅ Checklist de Déploiement

- [ ] Service Angular créé et testé
- [ ] Composant de tracking fonctionnel
- [ ] Polling démarre/arrête correctement
- [ ] Carte affichée avec position mise à jour
- [ ] Gestion d'erreurs implémentée
- [ ] Tests effectués avec missions EN_COURS
- [ ] Performance vérifiée (pas de memory leak)
- [ ] UI responsive sur mobile

---

**Version**: 1.0  
**Date**: 2025-12-01  
**Backend API Version**: Spring Boot 3.5.7
