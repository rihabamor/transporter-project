# Real-Time Location Tracking - API Documentation for UI Team

## 📋 Table of Contents
1. [Overview](#overview)
2. [How It Works](#how-it-works)
3. [WebSocket Setup](#websocket-setup)
4. [REST API Endpoints](#rest-api-endpoints)
5. [WebSocket Messages](#websocket-messages)
6. [Frontend Implementation Guide](#frontend-implementation-guide)
7. [Complete Angular Example](#complete-angular-example)
8. [Testing Guide](#testing-guide)

---

## Overview

The **Location Tracking** feature allows clients to see the transporteur's real-time location when a mission is in progress.

### Key Features
- ✅ **Automatic start**: Tracking begins when transporteur sets mission status to `EN_COURS`
- ✅ **Real-time updates**: Location sent every 5 seconds via WebSocket
- ✅ **Realistic path**: Transporteur follows a simulated realistic path between departure and arrival
- ✅ **Progress tracking**: Shows percentage completion and distance remaining
- ✅ **Location history**: Full path history available via REST API
- ✅ **Auto-stop**: Tracking stops when mission is `TERMINEE` or `ANNULEE`

### Technologies
- **WebSocket** with STOMP protocol for real-time communication
- **SockJS** fallback for browsers without WebSocket support
- **Scheduled tasks** for periodic location updates every 5 seconds

---

## How It Works

### Flow Diagram

```
1. TRANSPORTEUR updates mission status to "EN_COURS"
   ↓
2. Backend automatically starts tracking
   ↓
3. Backend generates realistic path (100 points from departure to arrival)
   ↓
4. Every 5 seconds:
   - Backend sends next location point via WebSocket
   - Location saved to database
   - CLIENT receives real-time update
   ↓
5. When mission status changes to "TERMINEE" or "ANNULEE":
   - Tracking stops automatically
```

### Mission Lifecycle

| Status | Tracking Active | What Happens |
|--------|----------------|--------------|
| `EN_ATTENTE` | ❌ No | Mission created, waiting |
| `ACCEPTEE` | ❌ No | Transporteur accepted |
| `EN_COURS` | ✅ **YES** | **Tracking starts automatically** |
| `TERMINEE` | ❌ No | Tracking stopped, history preserved |
| `ANNULEE` | ❌ No | Tracking stopped |

---

## WebSocket Setup

### Connection Endpoint
```
ws://localhost:8080/ws-location
```

### STOMP Configuration
- **Message Broker Prefix**: `/topic`, `/queue`
- **Application Destination Prefix**: `/app`
- **SockJS Enabled**: Yes (fallback support)

### Subscribe to Location Updates
```
/topic/location/{missionId}
```

Example: `/topic/location/1` for mission with ID 1

---

## REST API Endpoints

### 1. Get Location History
**Endpoint:** `GET /api/tracking/history/{missionId}`  
**Authentication:** Required (ROLE_CLIENT or ROLE_TRANSPORTEUR)  
**Description:** Get the complete location history of a mission

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Path Parameters:**
- `missionId`: ID of the mission (Long)

**Success Response (200):**
```json
{
  "missionId": 1,
  "locations": [
    {
      "latitude": 48.8566,
      "longitude": 2.3522,
      "timestamp": "2025-12-01T10:00:00",
      "speed": 65.5
    },
    {
      "latitude": 48.8450,
      "longitude": 2.3700,
      "timestamp": "2025-12-01T10:00:05",
      "speed": 70.2
    }
  ],
  "currentLocation": {
    "latitude": 45.7640,
    "longitude": 4.8357,
    "timestamp": "2025-12-01T10:08:20",
    "speed": 55.0
  },
  "startTime": "2025-12-01T10:00:00",
  "lastUpdateTime": "2025-12-01T10:08:20"
}
```

**Use Case:**
- Display the complete path on a map
- Show movement history after mission completion

---

### 2. Start Tracking (Manual - For Testing)
**Endpoint:** `POST /api/tracking/start/{missionId}`  
**Authentication:** Required (ROLE_TRANSPORTEUR)  
**Description:** Manually start tracking (normally automatic when status = EN_COURS)

**Note:** This endpoint is mainly for testing. In normal operation, tracking starts automatically.

---

### 3. Stop Tracking (Manual - For Testing)
**Endpoint:** `POST /api/tracking/stop/{missionId}`  
**Authentication:** Required (ROLE_TRANSPORTEUR)  
**Description:** Manually stop tracking (normally automatic when status = TERMINEE/ANNULEE)

---

## WebSocket Messages

### Message Format (Received from Server)

When subscribed to `/topic/location/{missionId}`, you'll receive:

```typescript
interface LocationUpdateDTO {
  missionId: number;
  latitude: number;           // Current GPS latitude
  longitude: number;          // Current GPS longitude
  timestamp: string;          // ISO 8601 format
  speed: number;              // Speed in km/h (40-80 km/h)
  address: string | null;     // Address (null for now, can be added later)
  progressPercentage: number; // 0-100, mission completion percentage
  distanceRemaining: number;  // Remaining distance in km
}
```

### Example Message
```json
{
  "missionId": 1,
  "latitude": 48.8210,
  "longitude": 2.4150,
  "timestamp": "2025-12-01T10:02:15",
  "speed": 72.5,
  "address": null,
  "progressPercentage": 25,
  "distanceRemaining": 350.5
}
```

### Update Frequency
- **Every 5 seconds** while mission is `EN_COURS`
- **100 total points** in the path (approximately 8 minutes to complete)

---

## Frontend Implementation Guide

### Required Libraries

#### Install Dependencies
```bash
npm install sockjs-client @stomp/stompjs
npm install --save-dev @types/sockjs-client
```

### Step 1: Create WebSocket Service

```typescript
// websocket.service.ts
import { Injectable } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { Observable, Subject } from 'rxjs';

export interface LocationUpdate {
  missionId: number;
  latitude: number;
  longitude: number;
  timestamp: string;
  speed: number;
  address: string | null;
  progressPercentage: number;
  distanceRemaining: number;
}

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  
  private client: Client;
  private connected = false;
  private subscriptions: Map<number, StompSubscription> = new Map();
  
  constructor() {
    this.client = new Client();
  }
  
  /**
   * Initialize WebSocket connection
   */
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.client.webSocketFactory = () => {
        return new SockJS('http://localhost:8080/ws-location');
      };
      
      this.client.onConnect = (frame) => {
        console.log('WebSocket connected:', frame);
        this.connected = true;
        resolve();
      };
      
      this.client.onStompError = (frame) => {
        console.error('WebSocket error:', frame);
        reject(frame);
      };
      
      this.client.activate();
    });
  }
  
  /**
   * Subscribe to location updates for a specific mission
   */
  subscribeToMissionLocation(missionId: number): Observable<LocationUpdate> {
    const subject = new Subject<LocationUpdate>();
    
    if (!this.connected) {
      this.connect().then(() => {
        this.doSubscribe(missionId, subject);
      });
    } else {
      this.doSubscribe(missionId, subject);
    }
    
    return subject.asObservable();
  }
  
  private doSubscribe(missionId: number, subject: Subject<LocationUpdate>): void {
    const subscription = this.client.subscribe(
      `/topic/location/${missionId}`,
      (message) => {
        const locationUpdate: LocationUpdate = JSON.parse(message.body);
        subject.next(locationUpdate);
      }
    );
    
    this.subscriptions.set(missionId, subscription);
  }
  
  /**
   * Unsubscribe from a mission's location updates
   */
  unsubscribeFromMissionLocation(missionId: number): void {
    const subscription = this.subscriptions.get(missionId);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(missionId);
    }
  }
  
  /**
   * Disconnect WebSocket
   */
  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.connected = false;
    }
  }
}
```

---

### Step 2: Create Location Tracking Service

```typescript
// location-tracking.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LocationHistory {
  missionId: number;
  locations: LocationPoint[];
  currentLocation: LocationPoint;
  startTime: string;
  lastUpdateTime: string;
}

export interface LocationPoint {
  latitude: number;
  longitude: number;
  timestamp: string;
  speed: number;
}

@Injectable({
  providedIn: 'root'
})
export class LocationTrackingService {
  
  private apiUrl = 'http://localhost:8080/api/tracking';
  
  constructor(private http: HttpClient) {}
  
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }
  
  /**
   * Get location history for a mission
   */
  getLocationHistory(missionId: number): Observable<LocationHistory> {
    return this.http.get<LocationHistory>(
      `${this.apiUrl}/history/${missionId}`,
      { headers: this.getHeaders() }
    );
  }
  
  /**
   * Manually start tracking (for testing)
   */
  startTracking(missionId: number): Observable<string> {
    return this.http.post<string>(
      `${this.apiUrl}/start/${missionId}`,
      null,
      { headers: this.getHeaders() }
    );
  }
  
  /**
   * Manually stop tracking (for testing)
   */
  stopTracking(missionId: number): Observable<string> {
    return this.http.post<string>(
      `${this.apiUrl}/stop/${missionId}`,
      null,
      { headers: this.getHeaders() }
    );
  }
}
```

---

### Step 3: Create Mission Tracking Component

```typescript
// mission-tracking.component.ts
import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { WebSocketService, LocationUpdate } from './services/websocket.service';
import { LocationTrackingService, LocationHistory } from './services/location-tracking.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-mission-tracking',
  templateUrl: './mission-tracking.component.html',
  styleUrls: ['./mission-tracking.component.css']
})
export class MissionTrackingComponent implements OnInit, OnDestroy {
  
  @Input() missionId!: number;
  
  currentLocation: LocationUpdate | null = null;
  locationHistory: LocationHistory | null = null;
  isTracking = false;
  
  private locationSubscription: Subscription | null = null;
  
  constructor(
    private wsService: WebSocketService,
    private trackingService: LocationTrackingService
  ) {}
  
  ngOnInit(): void {
    // Load historical data first
    this.loadLocationHistory();
    
    // Subscribe to real-time updates
    this.subscribeToRealTimeUpdates();
  }
  
  ngOnDestroy(): void {
    this.unsubscribe();
  }
  
  /**
   * Load location history from REST API
   */
  loadLocationHistory(): void {
    this.trackingService.getLocationHistory(this.missionId).subscribe({
      next: (history) => {
        this.locationHistory = history;
        this.currentLocation = history.currentLocation as any;
      },
      error: (err) => {
        console.error('Error loading location history:', err);
      }
    });
  }
  
  /**
   * Subscribe to real-time WebSocket updates
   */
  subscribeToRealTimeUpdates(): void {
    this.locationSubscription = this.wsService
      .subscribeToMissionLocation(this.missionId)
      .subscribe({
        next: (update) => {
          console.log('Location update received:', update);
          this.currentLocation = update;
          this.isTracking = true;
          
          // Update map marker or path
          this.updateMapWithNewLocation(update);
        },
        error: (err) => {
          console.error('WebSocket error:', err);
          this.isTracking = false;
        }
      });
  }
  
  /**
   * Update map with new location (implement with your map library)
   */
  updateMapWithNewLocation(location: LocationUpdate): void {
    // Example with Leaflet or Google Maps
    // Update marker position
    // Add point to polyline path
    // Pan to new location
    console.log('Updating map with:', location);
  }
  
  /**
   * Unsubscribe from WebSocket
   */
  unsubscribe(): void {
    if (this.locationSubscription) {
      this.locationSubscription.unsubscribe();
    }
    this.wsService.unsubscribeFromMissionLocation(this.missionId);
  }
}
```

---

### Step 4: HTML Template

```html
<!-- mission-tracking.component.html -->
<div class="tracking-container">
  
  <!-- Status Badge -->
  <div class="tracking-status">
    <span *ngIf="isTracking" class="badge badge-success">
      🟢 Tracking en direct
    </span>
    <span *ngIf="!isTracking" class="badge badge-secondary">
      ⚪ Tracking arrêté
    </span>
  </div>
  
  <!-- Current Location Info -->
  <div *ngIf="currentLocation" class="location-info">
    <h4>Position actuelle</h4>
    <p><strong>Lat:</strong> {{ currentLocation.latitude | number:'1.4-4' }}</p>
    <p><strong>Lon:</strong> {{ currentLocation.longitude | number:'1.4-4' }}</p>
    <p><strong>Vitesse:</strong> {{ currentLocation.speed | number:'1.1-1' }} km/h</p>
    <p><strong>Progression:</strong> {{ currentLocation.progressPercentage }}%</p>
    <p><strong>Distance restante:</strong> {{ currentLocation.distanceRemaining | number:'1.1-1' }} km</p>
    <p><strong>Dernière mise à jour:</strong> {{ currentLocation.timestamp | date:'HH:mm:ss' }}</p>
  </div>
  
  <!-- Progress Bar -->
  <div *ngIf="currentLocation" class="progress">
    <div 
      class="progress-bar progress-bar-striped progress-bar-animated" 
      [style.width.%]="currentLocation.progressPercentage">
      {{ currentLocation.progressPercentage }}%
    </div>
  </div>
  
  <!-- Map Container -->
  <div id="map" style="width: 100%; height: 500px;">
    <!-- Integrate with Leaflet, Google Maps, or any map library -->
  </div>
  
  <!-- Location History Table (Optional) -->
  <div *ngIf="locationHistory" class="history">
    <h4>Historique ({{ locationHistory.locations.length }} points)</h4>
    <table class="table table-sm">
      <thead>
        <tr>
          <th>Heure</th>
          <th>Latitude</th>
          <th>Longitude</th>
          <th>Vitesse</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let loc of locationHistory.locations.slice(-10)">
          <td>{{ loc.timestamp | date:'HH:mm:ss' }}</td>
          <td>{{ loc.latitude | number:'1.4-4' }}</td>
          <td>{{ loc.longitude | number:'1.4-4' }}</td>
          <td>{{ loc.speed | number:'1.1-1' }} km/h</td>
        </tr>
      </tbody>
    </table>
  </div>
  
</div>
```

---

## Complete Angular Example

### Example: Integrate with Google Maps

```typescript
// Install: npm install @types/google.maps

import { Component, OnInit, AfterViewInit } from '@angular/core';
import { LocationUpdate } from './services/websocket.service';

@Component({
  selector: 'app-mission-map',
  template: `
    <div id="map" style="width: 100%; height: 600px;"></div>
    <div class="info-panel">
      <p *ngIf="currentLocation">
        Vitesse: {{ currentLocation.speed | number:'1.0-0' }} km/h | 
        Progression: {{ currentLocation.progressPercentage }}%
      </p>
    </div>
  `
})
export class MissionMapComponent implements OnInit, AfterViewInit {
  
  private map!: google.maps.Map;
  private marker!: google.maps.Marker;
  private path: google.maps.LatLng[] = [];
  private polyline!: google.maps.Polyline;
  
  currentLocation: LocationUpdate | null = null;
  
  ngAfterViewInit(): void {
    this.initMap();
  }
  
  initMap(): void {
    // Initial center (Paris)
    const center = { lat: 48.8566, lng: 2.3522 };
    
    this.map = new google.maps.Map(
      document.getElementById('map') as HTMLElement,
      {
        zoom: 8,
        center: center,
      }
    );
    
    // Create marker for transporteur
    this.marker = new google.maps.Marker({
      position: center,
      map: this.map,
      title: 'Transporteur',
      icon: {
        url: 'assets/truck-icon.png', // Custom icon
        scaledSize: new google.maps.Size(40, 40)
      }
    });
    
    // Create polyline for path
    this.polyline = new google.maps.Polyline({
      path: this.path,
      geodesic: true,
      strokeColor: '#FF0000',
      strokeOpacity: 1.0,
      strokeWeight: 3,
      map: this.map
    });
  }
  
  /**
   * Update map with new location from WebSocket
   */
  updateMapWithNewLocation(location: LocationUpdate): void {
    this.currentLocation = location;
    
    const newPosition = new google.maps.LatLng(
      location.latitude,
      location.longitude
    );
    
    // Update marker position
    this.marker.setPosition(newPosition);
    
    // Add to path
    this.path.push(newPosition);
    this.polyline.setPath(this.path);
    
    // Pan to new location
    this.map.panTo(newPosition);
  }
}
```

---

### Example: Integrate with Leaflet

```typescript
// Install: npm install leaflet @types/leaflet

import { Component, AfterViewInit } from '@angular/core';
import * as L from 'leaflet';
import { LocationUpdate } from './services/websocket.service';

@Component({
  selector: 'app-mission-map-leaflet',
  template: `<div id="map" style="width: 100%; height: 600px;"></div>`
})
export class MissionMapLeafletComponent implements AfterViewInit {
  
  private map!: L.Map;
  private marker!: L.Marker;
  private polyline!: L.Polyline;
  private path: L.LatLng[] = [];
  
  ngAfterViewInit(): void {
    this.initMap();
  }
  
  initMap(): void {
    // Create map
    this.map = L.map('map').setView([48.8566, 2.3522], 8);
    
    // Add tile layer (OpenStreetMap)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);
    
    // Create marker
    this.marker = L.marker([48.8566, 2.3522]).addTo(this.map);
    this.marker.bindPopup('Transporteur').openPopup();
    
    // Create polyline
    this.polyline = L.polyline(this.path, { color: 'red' }).addTo(this.map);
  }
  
  updateMapWithNewLocation(location: LocationUpdate): void {
    const newLatLng = L.latLng(location.latitude, location.longitude);
    
    // Update marker
    this.marker.setLatLng(newLatLng);
    this.marker.setPopupContent(`
      Vitesse: ${location.speed.toFixed(1)} km/h<br>
      Progression: ${location.progressPercentage}%
    `);
    
    // Update path
    this.path.push(newLatLng);
    this.polyline.setLatLngs(this.path);
    
    // Pan to new location
    this.map.panTo(newLatLng);
  }
}
```

---

## Testing Guide

### Test 1: Basic Flow

1. **CLIENT creates a mission**
```bash
POST http://localhost:8080/api/missions
{
  "transporteurId": 1,
  "dateMission": "2025-12-15T10:00:00",
  "lieuDepart": "Paris",
  "lieuArrivee": "Lyon",
  "description": "Test tracking"
}
```

2. **TRANSPORTEUR accepts mission**
```bash
PUT http://localhost:8080/api/missions/1/statut
{
  "statut": "ACCEPTEE"
}
```

3. **TRANSPORTEUR starts mission**
```bash
PUT http://localhost:8080/api/missions/1/statut
{
  "statut": "EN_COURS"
}
```
✅ Tracking starts automatically

4. **CLIENT subscribes to WebSocket**
```javascript
// Frontend
wsService.subscribeToMissionLocation(1).subscribe(update => {
  console.log('Location:', update);
});
```
✅ Should receive updates every 5 seconds

5. **TRANSPORTEUR completes mission**
```bash
PUT http://localhost:8080/api/missions/1/statut
{
  "statut": "TERMINEE"
}
```
✅ Tracking stops automatically

---

### Test 2: Check Location History

```bash
GET http://localhost:8080/api/tracking/history/1
Authorization: Bearer <token>
```

Expected: List of all location points recorded during the mission

---

### Test 3: WebSocket Connection (Browser Console)

```javascript
// Test in browser console
const socket = new SockJS('http://localhost:8080/ws-location');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  console.log('Connected:', frame);
  
  stompClient.subscribe('/topic/location/1', function(message) {
    console.log('Location update:', JSON.parse(message.body));
  });
});
```

---

## Important Notes

### Path Simulation
- The backend generates a **simulated realistic path** with 100 intermediate points
- Points are sent every **5 seconds** (100 points × 5 sec = ~8 minutes total)
- Speed varies between **40-80 km/h** to simulate realistic driving
- Path uses **linear interpolation** between start and end coordinates with slight random variations

### For Production
To use real GPS data from mobile app:
1. Modify `LocationTrackingService` to accept GPS coordinates from transporteur's device
2. Add endpoint: `POST /api/tracking/update-location` to receive GPS updates
3. Transporteur mobile app sends GPS every 5-10 seconds
4. Backend broadcasts to WebSocket subscribers

### Performance
- Each mission creates a **scheduled task** running every 5 seconds
- Tasks are **automatically cleaned up** when mission ends
- Use `ConcurrentHashMap` to prevent memory leaks
- Maximum **10 concurrent missions** tracked (configurable in scheduler pool size)

---

## Summary

✅ **WebSocket** endpoint: `ws://localhost:8080/ws-location`  
✅ **Subscribe to**: `/topic/location/{missionId}`  
✅ **Updates every**: 5 seconds  
✅ **Auto-start**: When status = `EN_COURS`  
✅ **Auto-stop**: When status = `TERMINEE` or `ANNULEE`  
✅ **REST API**: `/api/tracking/history/{missionId}` for historical data  
✅ **100 points** in simulated path  
✅ **Speed**: 40-80 km/h  
✅ **Progress**: Percentage and distance remaining included  

---

**Generated:** December 1, 2025  
**Feature Version:** 1.0  
**Backend URL:** http://localhost:8080  
**WebSocket URL:** ws://localhost:8080/ws-location
