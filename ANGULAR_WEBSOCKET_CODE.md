# 🚀 Angular WebSocket Quick Start Code

## Copy-Paste Ready Implementation

### Step 1: Install Dependencies

```bash
npm install sockjs-client @stomp/stompjs --save
```

---

### Step 2: Create WebSocket Service

**File:** `src/app/services/websocket.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client | null = null;
  private connectionStatus = new BehaviorSubject<boolean>(false);
  
  public isConnected$: Observable<boolean> = this.connectionStatus.asObservable();

  constructor() {}

  connect(): void {
    if (this.client && this.client.connected) {
      console.log('✅ WebSocket already connected');
      return;
    }

    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-location'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      
      onConnect: (frame) => {
        console.log('✅ WebSocket Connected:', frame);
        this.connectionStatus.next(true);
      },
      
      onStompError: (frame) => {
        console.error('❌ STOMP Error:', frame.headers['message']);
        this.connectionStatus.next(false);
      },
      
      onWebSocketClose: (event) => {
        console.warn('⚠️ WebSocket Closed');
        this.connectionStatus.next(false);
      },
      
      debug: (str) => {
        console.log('🔧 STOMP:', str);
      }
    });

    this.client.activate();
  }

  subscribeToMissionLocation(missionId: number, callback: (data: any) => void): any {
    if (!this.client || !this.client.connected) {
      console.error('❌ WebSocket not connected');
      return null;
    }

    const destination = `/topic/location/${missionId}`;
    console.log(`📡 Subscribing to: ${destination}`);

    return this.client.subscribe(destination, (message: IMessage) => {
      try {
        const locationUpdate = JSON.parse(message.body);
        callback(locationUpdate);
      } catch (error) {
        console.error('❌ Parse error:', error);
      }
    });
  }

  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.connectionStatus.next(false);
      console.log('🔌 WebSocket Disconnected');
    }
  }

  isConnected(): boolean {
    return this.client?.connected || false;
  }
}
```

---

### Step 3: Create Location Service

**File:** `src/app/services/location-tracking.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WebSocketService } from './websocket.service';

@Injectable({
  providedIn: 'root'
})
export class LocationTrackingService {
  private baseUrl = 'http://localhost:8080/api/tracking';

  constructor(
    private http: HttpClient,
    private websocketService: WebSocketService
  ) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  startTracking(missionId: number): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/start/${missionId}`, 
      {}, 
      { headers: this.getHeaders() }
    );
  }

  stopTracking(missionId: number): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/stop/${missionId}`, 
      {}, 
      { headers: this.getHeaders() }
    );
  }

  getLocationHistory(missionId: number): Observable<any> {
    return this.http.get(
      `${this.baseUrl}/history/${missionId}`, 
      { headers: this.getHeaders() }
    );
  }

  subscribeToLocationUpdates(missionId: number, callback: (update: any) => void): any {
    return this.websocketService.subscribeToMissionLocation(missionId, callback);
  }
}
```

---

### Step 4: Connect in AppComponent

**File:** `src/app/app.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { WebSocketService } from './services/websocket.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'transporteur-frontend';

  constructor(private websocketService: WebSocketService) {}

  ngOnInit(): void {
    // Connect to WebSocket when app starts
    console.log('🚀 Connecting to WebSocket...');
    this.websocketService.connect();

    // Monitor connection status
    this.websocketService.isConnected$.subscribe(connected => {
      if (connected) {
        console.log('✅ WebSocket ready for subscriptions');
      } else {
        console.log('🔴 WebSocket disconnected');
      }
    });
  }
}
```

---

### Step 5: Use in Your Tracking Component

**File:** `src/app/components/mission-tracking/mission-tracking.component.ts`

```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { LocationTrackingService } from '../../services/location-tracking.service';
import { WebSocketService } from '../../services/websocket.service';

@Component({
  selector: 'app-mission-tracking',
  templateUrl: './mission-tracking.component.html',
  styleUrls: ['./mission-tracking.component.css']
})
export class MissionTrackingComponent implements OnInit, OnDestroy {
  missionId: number = 0;
  currentLocation: any = null;
  subscription: any = null;
  isConnected = false;
  isTracking = false;

  constructor(
    private route: ActivatedRoute,
    private locationService: LocationTrackingService,
    private websocketService: WebSocketService
  ) {}

  ngOnInit(): void {
    // Get mission ID from route parameter
    this.missionId = Number(this.route.snapshot.paramMap.get('id'));

    // Wait for WebSocket connection, then subscribe
    this.websocketService.isConnected$.subscribe(connected => {
      this.isConnected = connected;
      
      if (connected && this.missionId) {
        console.log('✅ WebSocket connected, subscribing to mission:', this.missionId);
        this.subscribeToUpdates();
      }
    });
  }

  subscribeToUpdates(): void {
    this.subscription = this.locationService.subscribeToLocationUpdates(
      this.missionId,
      (locationUpdate) => {
        console.log('📍 Location Update:', locationUpdate);
        this.currentLocation = locationUpdate;
        
        // Update your map here
        this.updateMap(locationUpdate.latitude, locationUpdate.longitude);
      }
    );
  }

  startTracking(): void {
    this.locationService.startTracking(this.missionId).subscribe({
      next: (response) => {
        console.log('✅ Tracking started:', response);
        this.isTracking = true;
      },
      error: (error) => {
        console.error('❌ Start tracking failed:', error);
        alert('Failed to start tracking: ' + error.message);
      }
    });
  }

  stopTracking(): void {
    this.locationService.stopTracking(this.missionId).subscribe({
      next: (response) => {
        console.log('✅ Tracking stopped:', response);
        this.isTracking = false;
      },
      error: (error) => {
        console.error('❌ Stop tracking failed:', error);
        alert('Failed to stop tracking: ' + error.message);
      }
    });
  }

  updateMap(lat: number, lng: number): void {
    console.log(`🗺️ Updating map to: ${lat}, ${lng}`);
    // TODO: Implement your map update logic here
    // Example for Google Maps:
    // this.mapMarker.setPosition({ lat, lng });
    // this.map.panTo({ lat, lng });
  }

  ngOnDestroy(): void {
    // Unsubscribe when component is destroyed
    if (this.subscription) {
      this.subscription.unsubscribe();
      console.log('🔌 Unsubscribed from location updates');
    }
  }
}
```

---

### Step 6: HTML Template

**File:** `src/app/components/mission-tracking/mission-tracking.component.html`

```html
<div class="mission-tracking-container">
  <!-- Connection Status -->
  <div class="status-bar" [class.connected]="isConnected" [class.disconnected]="!isConnected">
    <span *ngIf="isConnected">🟢 Connected to Server</span>
    <span *ngIf="!isConnected">🔴 Disconnected from Server</span>
  </div>

  <!-- Mission Header -->
  <div class="mission-header">
    <h2>Mission #{{ missionId }}</h2>
    <span class="tracking-badge" *ngIf="isTracking">🚚 Tracking Active</span>
  </div>

  <!-- Current Location Info -->
  <div class="location-info" *ngIf="currentLocation">
    <h3>Current Location</h3>
    <div class="info-grid">
      <div class="info-item">
        <label>Latitude:</label>
        <span>{{ currentLocation.latitude | number:'1.6-6' }}</span>
      </div>
      <div class="info-item">
        <label>Longitude:</label>
        <span>{{ currentLocation.longitude | number:'1.6-6' }}</span>
      </div>
      <div class="info-item">
        <label>Speed:</label>
        <span>{{ currentLocation.speed | number:'1.0-0' }} km/h</span>
      </div>
      <div class="info-item">
        <label>Progress:</label>
        <span>{{ currentLocation.progressPercentage | number:'1.0-0' }}%</span>
      </div>
      <div class="info-item">
        <label>Distance Remaining:</label>
        <span>{{ currentLocation.distanceRemaining | number:'1.2-2' }} km</span>
      </div>
      <div class="info-item">
        <label>Last Update:</label>
        <span>{{ currentLocation.timestamp | date:'HH:mm:ss' }}</span>
      </div>
    </div>

    <!-- Progress Bar -->
    <div class="progress-container">
      <div class="progress-bar" [style.width.%]="currentLocation.progressPercentage"></div>
    </div>
  </div>

  <!-- Control Buttons -->
  <div class="controls">
    <button 
      (click)="startTracking()" 
      [disabled]="!isConnected || isTracking"
      class="btn btn-primary">
      ▶️ Start Tracking
    </button>
    <button 
      (click)="stopTracking()" 
      [disabled]="!isConnected || !isTracking"
      class="btn btn-danger">
      ⏹️ Stop Tracking
    </button>
  </div>

  <!-- Map Container -->
  <div id="map" style="height: 500px; width: 100%; border: 1px solid #ccc;"></div>
</div>
```

---

### Step 7: CSS Styling (Optional)

**File:** `src/app/components/mission-tracking/mission-tracking.component.css`

```css
.mission-tracking-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.status-bar {
  padding: 10px;
  border-radius: 5px;
  margin-bottom: 20px;
  text-align: center;
  font-weight: bold;
}

.status-bar.connected {
  background-color: #d4edda;
  color: #155724;
}

.status-bar.disconnected {
  background-color: #f8d7da;
  color: #721c24;
}

.mission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.tracking-badge {
  background-color: #007bff;
  color: white;
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 14px;
}

.location-info {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-top: 15px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-item label {
  font-weight: bold;
  color: #6c757d;
  font-size: 12px;
  text-transform: uppercase;
  margin-bottom: 5px;
}

.info-item span {
  font-size: 16px;
  color: #212529;
}

.progress-container {
  width: 100%;
  height: 30px;
  background-color: #e9ecef;
  border-radius: 15px;
  overflow: hidden;
  margin-top: 15px;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #28a745, #20c997);
  transition: width 0.5s ease;
}

.controls {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: opacity 0.3s;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn-danger {
  background-color: #dc3545;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background-color: #c82333;
}

#map {
  border-radius: 8px;
}
```

---

## ✅ Testing Checklist

After implementing the above code:

1. **Backend:** Restart server
   ```bash
   mvn spring-boot:run
   ```

2. **Frontend:** Install dependencies and run
   ```bash
   npm install
   ng serve
   ```

3. **Browser:** Open `http://localhost:4200` and check console

4. **Expected Console Output:**
   ```
   🚀 Connecting to WebSocket...
   🔧 STOMP: Opening Web Socket...
   🔧 STOMP: Web Socket Opened...
   ✅ WebSocket Connected
   ✅ WebSocket ready for subscriptions
   📡 Subscribing to: /topic/location/123
   📍 Location Update: { missionId: 123, latitude: 35.67, ... }
   ```

5. **Network Tab:** Should show:
   - ✅ `ws-location/info` → 200 OK
   - ✅ `ws-location/.../websocket` → 101 Switching Protocols

---

## 🎯 Quick Tips

1. **Token Storage:** Make sure you store JWT token after login:
   ```typescript
   localStorage.setItem('token', response.token);
   ```

2. **Route Configuration:** Ensure your routing has mission ID parameter:
   ```typescript
   { path: 'mission/:id', component: MissionTrackingComponent }
   ```

3. **Import HttpClientModule:** In `app.module.ts`:
   ```typescript
   import { HttpClientModule } from '@angular/common/http';
   
   @NgModule({
     imports: [HttpClientModule, ...]
   })
   ```

4. **Provide Services:** Services are already `providedIn: 'root'`, no need to add to providers array.

---

## 🐛 Common Issues

### "SockJS is not defined"
**Solution:** Add to `tsconfig.json`:
```json
{
  "compilerOptions": {
    "allowSyntheticDefaultImports": true
  }
}
```

### "Cannot find module 'sockjs-client'"
**Solution:** Install dependencies:
```bash
npm install --save-dev @types/sockjs-client
```

### Connection works but no messages received
**Solution:** Check that mission status is "EN_COURS":
```typescript
// Mission must have status = EN_COURS for tracking to start automatically
```

---

**Ready to Use!** 🚀 Copy the code above and start tracking your missions in real-time!
