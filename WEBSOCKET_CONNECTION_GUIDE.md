# WebSocket Connection Guide - Complete Tutorial

## 📋 Table of Contents
1. [Understanding the Problem](#understanding-the-problem)
2. [Backend Configuration](#backend-configuration)
3. [Frontend Setup (Angular)](#frontend-setup-angular)
4. [Testing the Connection](#testing-the-connection)
5. [Troubleshooting Guide](#troubleshooting-guide)
6. [Best Practices](#best-practices)

---

## 🔍 Understanding the Problem

### What Was Wrong?
The `403 Forbidden` error occurred because:
- **Spring Security** was blocking the WebSocket handshake endpoint `/ws-location/info`
- SockJS tries to connect to `/ws-location/info` to get server capabilities
- The security filter chain didn't permit access to `/ws-location/**` endpoints

### How WebSocket Connection Works
```
┌─────────────┐                                    ┌─────────────┐
│   Angular   │                                    │   Spring    │
│   Frontend  │                                    │   Backend   │
└──────┬──────┘                                    └──────┬──────┘
       │                                                  │
       │ 1. HTTP GET /ws-location/info (SockJS)         │
       │─────────────────────────────────────────────────>│
       │                                                  │
       │ 2. Server responds with transport options       │
       │<─────────────────────────────────────────────────│
       │                                                  │
       │ 3. Upgrade to WebSocket connection             │
       │─────────────────────────────────────────────────>│
       │                                                  │
       │ 4. WebSocket connection established            │
       │<═══════════════════════════════════════════════>│
       │                                                  │
       │ 5. STOMP CONNECT frame                         │
       │─────────────────────────────────────────────────>│
       │                                                  │
       │ 6. STOMP CONNECTED frame                       │
       │<─────────────────────────────────────────────────│
       │                                                  │
       │ 7. SUBSCRIBE to /topic/location/123            │
       │─────────────────────────────────────────────────>│
       │                                                  │
       │ 8. Real-time location updates                  │
       │<─────────────────────────────────────────────────│
       │<─────────────────────────────────────────────────│
       │<─────────────────────────────────────────────────│
```

---

## ⚙️ Backend Configuration

### 1. SecurityConfig.java - **FIXED** ✅

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/api/auth/**", "/api/test").permitAll()
            // ⭐ CRITICAL: WebSocket endpoints MUST be accessible
            .requestMatchers("/ws-location/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/profil/client/**").hasRole("CLIENT")
            .requestMatchers("/api/profil/transporteur/**").hasRole("TRANSPORTEUR")
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .formLogin(form -> form.disable())
        .httpBasic(httpBasic -> httpBasic.disable());

    return http.build();
}
```

**Why `.requestMatchers("/ws-location/**").permitAll()` is needed:**
- WebSocket handshake is an HTTP request that happens BEFORE authentication
- SockJS sends multiple requests: `/ws-location/info`, `/ws-location/123/abc/websocket`, etc.
- The `**` pattern matches all sub-paths

### 2. WebSocketConfig.java - Already Correct ✅

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-location")
                .setAllowedOrigins("http://localhost:4200")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
```

**Key Points:**
- **Endpoint**: `/ws-location` - Base path for WebSocket connection
- **CORS**: `http://localhost:4200` - Your Angular frontend URL
- **SockJS**: Fallback for browsers that don't support WebSocket
- **Message Broker**: `/topic` for broadcast, `/queue` for point-to-point

### 3. CORS Configuration - Already Correct ✅

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

## 🎨 Frontend Setup (Angular)

### Step 1: Install Dependencies

```bash
npm install sockjs-client @stomp/stompjs --save
```

### Step 2: Create WebSocket Service

**File: `src/app/services/websocket.service.ts`**

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
  
  // Observable to track connection status
  public isConnected$: Observable<boolean> = this.connectionStatus.asObservable();

  constructor() {}

  /**
   * Connect to WebSocket server
   * Call this once when your app starts or when user logs in
   */
  connect(): void {
    // Avoid duplicate connections
    if (this.client && this.client.connected) {
      console.log('WebSocket already connected');
      return;
    }

    this.client = new Client({
      // WebSocket connection factory using SockJS
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-location'),
      
      // Reconnection configuration
      reconnectDelay: 5000, // Try to reconnect every 5 seconds
      heartbeatIncoming: 4000, // Server will send heartbeat every 4 seconds
      heartbeatOutgoing: 4000, // Client will send heartbeat every 4 seconds
      
      // Connection lifecycle callbacks
      onConnect: (frame) => {
        console.log('✅ WebSocket Connected:', frame);
        this.connectionStatus.next(true);
      },
      
      onStompError: (frame) => {
        console.error('❌ STOMP Error:', frame.headers['message']);
        console.error('Details:', frame.body);
        this.connectionStatus.next(false);
      },
      
      onWebSocketClose: (event) => {
        console.warn('⚠️ WebSocket Closed:', event);
        this.connectionStatus.next(false);
      },
      
      onWebSocketError: (event) => {
        console.error('❌ WebSocket Error:', event);
        this.connectionStatus.next(false);
      },
      
      // Enable debug logs (disable in production)
      debug: (str) => {
        console.log('🔧 STOMP Debug:', str);
      }
    });

    // Activate the connection
    this.client.activate();
  }

  /**
   * Subscribe to location updates for a specific mission
   * @param missionId The mission ID to track
   * @param callback Function to call when location update received
   * @returns Subscription object (call unsubscribe() to stop receiving updates)
   */
  subscribeToMissionLocation(
    missionId: number, 
    callback: (locationUpdate: any) => void
  ): any {
    if (!this.client || !this.client.connected) {
      console.error('❌ Cannot subscribe: WebSocket not connected');
      return null;
    }

    const destination = `/topic/location/${missionId}`;
    console.log(`📡 Subscribing to: ${destination}`);

    return this.client.subscribe(destination, (message: IMessage) => {
      try {
        const locationUpdate = JSON.parse(message.body);
        console.log('📍 Location Update:', locationUpdate);
        callback(locationUpdate);
      } catch (error) {
        console.error('❌ Failed to parse location update:', error);
      }
    });
  }

  /**
   * Disconnect from WebSocket server
   * Call this when user logs out or app is destroyed
   */
  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.connectionStatus.next(false);
      console.log('🔌 WebSocket Disconnected');
    }
  }

  /**
   * Check if WebSocket is currently connected
   */
  isConnected(): boolean {
    return this.client?.connected || false;
  }
}
```

### Step 3: Create Location Tracking Service

**File: `src/app/services/location-tracking.service.ts`**

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WebSocketService } from './websocket.service';

interface LocationUpdate {
  missionId: number;
  latitude: number;
  longitude: number;
  timestamp: string;
  speed: number;
  progressPercentage: number;
  distanceRemaining: number;
}

interface LocationHistory {
  missionId: number;
  totalPoints: number;
  locations: Array<{
    latitude: number;
    longitude: number;
    timestamp: string;
    speed: number;
    address: string;
  }>;
}

@Injectable({
  providedIn: 'root'
})
export class LocationTrackingService {
  private baseUrl = 'http://localhost:8080/api/tracking';

  constructor(
    private http: HttpClient,
    private websocketService: WebSocketService
  ) {}

  /**
   * Start tracking a mission (backend will start simulation)
   */
  startTracking(missionId: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    
    return this.http.post(`${this.baseUrl}/start/${missionId}`, {}, { headers });
  }

  /**
   * Stop tracking a mission
   */
  stopTracking(missionId: number): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    
    return this.http.post(`${this.baseUrl}/stop/${missionId}`, {}, { headers });
  }

  /**
   * Get location history for a mission
   */
  getLocationHistory(missionId: number): Observable<LocationHistory> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    
    return this.http.get<LocationHistory>(
      `${this.baseUrl}/history/${missionId}`, 
      { headers }
    );
  }

  /**
   * Subscribe to real-time location updates via WebSocket
   * @param missionId Mission to track
   * @param callback Function to call when location updates arrive
   */
  subscribeToLocationUpdates(
    missionId: number,
    callback: (update: LocationUpdate) => void
  ): any {
    return this.websocketService.subscribeToMissionLocation(missionId, callback);
  }
}
```

### Step 4: Use in Your Component

**File: `src/app/components/mission-tracking/mission-tracking.component.ts`**

```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { WebSocketService } from '../../services/websocket.service';
import { LocationTrackingService } from '../../services/location-tracking.service';

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

  constructor(
    private route: ActivatedRoute,
    private websocketService: WebSocketService,
    private locationService: LocationTrackingService
  ) {}

  ngOnInit(): void {
    // Get mission ID from route
    this.missionId = Number(this.route.snapshot.paramMap.get('id'));

    // 1. Connect to WebSocket server
    this.websocketService.connect();

    // 2. Monitor connection status
    this.websocketService.isConnected$.subscribe(connected => {
      this.isConnected = connected;
      
      if (connected) {
        console.log('✅ Connected! Subscribing to mission updates...');
        this.subscribeToUpdates();
      }
    });
  }

  subscribeToUpdates(): void {
    // 3. Subscribe to location updates for this mission
    this.subscription = this.locationService.subscribeToLocationUpdates(
      this.missionId,
      (locationUpdate) => {
        console.log('📍 New location:', locationUpdate);
        this.currentLocation = locationUpdate;
        
        // Update your map here
        this.updateMapMarker(locationUpdate.latitude, locationUpdate.longitude);
      }
    );
  }

  updateMapMarker(lat: number, lng: number): void {
    // Update Google Maps or Leaflet marker
    console.log(`Updating map to: ${lat}, ${lng}`);
    // Implementation depends on your map library
  }

  startTracking(): void {
    this.locationService.startTracking(this.missionId).subscribe({
      next: (response) => {
        console.log('✅ Tracking started:', response);
      },
      error: (error) => {
        console.error('❌ Failed to start tracking:', error);
      }
    });
  }

  stopTracking(): void {
    this.locationService.stopTracking(this.missionId).subscribe({
      next: (response) => {
        console.log('✅ Tracking stopped:', response);
      },
      error: (error) => {
        console.error('❌ Failed to stop tracking:', error);
      }
    });
  }

  ngOnDestroy(): void {
    // 4. Unsubscribe when component is destroyed
    if (this.subscription) {
      this.subscription.unsubscribe();
      console.log('🔌 Unsubscribed from location updates');
    }
    
    // Optional: disconnect WebSocket (if no other components need it)
    // this.websocketService.disconnect();
  }
}
```

### Step 5: HTML Template Example

**File: `src/app/components/mission-tracking/mission-tracking.component.html`**

```html
<div class="mission-tracking-container">
  <!-- Connection Status Indicator -->
  <div class="status-bar">
    <span [class.connected]="isConnected" [class.disconnected]="!isConnected">
      {{ isConnected ? '🟢 Connected' : '🔴 Disconnected' }}
    </span>
  </div>

  <!-- Mission Info -->
  <div class="mission-info">
    <h2>Mission #{{ missionId }}</h2>
    
    <!-- Current Location -->
    <div *ngIf="currentLocation" class="location-card">
      <h3>Current Location</h3>
      <p><strong>Latitude:</strong> {{ currentLocation.latitude }}</p>
      <p><strong>Longitude:</strong> {{ currentLocation.longitude }}</p>
      <p><strong>Speed:</strong> {{ currentLocation.speed }} km/h</p>
      <p><strong>Progress:</strong> {{ currentLocation.progressPercentage }}%</p>
      <p><strong>Distance Remaining:</strong> {{ currentLocation.distanceRemaining }} km</p>
    </div>

    <!-- Control Buttons -->
    <div class="controls">
      <button (click)="startTracking()" class="btn btn-primary">
        Start Tracking
      </button>
      <button (click)="stopTracking()" class="btn btn-danger">
        Stop Tracking
      </button>
    </div>
  </div>

  <!-- Map Container -->
  <div id="map" style="height: 500px;"></div>
</div>
```

---

## 🧪 Testing the Connection

### Test 1: Backend Server Status
```bash
# Start your backend server
mvn spring-boot:run

# Check if WebSocket endpoint is accessible
# Open browser and go to:
http://localhost:8080/ws-location/info

# You should see JSON response like:
{
  "entropy": "...",
  "websocket": true,
  "origins": ["*:*"],
  "cookie_needed": false
}
```

### Test 2: Browser Console Test
Open your Angular app's browser console and run:

```javascript
// Test 1: Check if SockJS can connect
var socket = new SockJS('http://localhost:8080/ws-location');
socket.onopen = function() {
    console.log('✅ SockJS Connected!');
};
socket.onerror = function(e) {
    console.error('❌ SockJS Error:', e);
};

// Test 2: Check STOMP connection
var stompClient = Stomp.over(socket);
stompClient.connect({}, function(frame) {
    console.log('✅ STOMP Connected:', frame);
    
    // Subscribe to test topic
    stompClient.subscribe('/topic/location/1', function(message) {
        console.log('📍 Message:', JSON.parse(message.body));
    });
});
```

### Test 3: Network Tab Verification
1. Open Chrome DevTools → Network tab
2. Filter by "WS" (WebSocket)
3. Reload your Angular app
4. You should see:
   - ✅ `ws-location/info` → Status 200
   - ✅ `ws-location/XXX/YYY/websocket` → Status 101 (Switching Protocols)

---

## 🐛 Troubleshooting Guide

### Problem 1: 403 Forbidden Error
**Symptoms:**
```
GET http://localhost:8080/ws-location/info 403 (Forbidden)
```

**Solution:**
✅ **FIXED** - Added `.requestMatchers("/ws-location/**").permitAll()` to SecurityConfig

**Verify Fix:**
```java
// Check your SecurityConfig.java has this line:
.requestMatchers("/ws-location/**").permitAll()
```

### Problem 2: CORS Error
**Symptoms:**
```
Access to XMLHttpRequest at 'http://localhost:8080/ws-location/info' 
from origin 'http://localhost:4200' has been blocked by CORS policy
```

**Solution:**
```java
// Ensure WebSocketConfig has correct origin:
.setAllowedOrigins("http://localhost:4200")

// AND SecurityConfig has proper CORS config:
configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
```

### Problem 3: Connection Refused
**Symptoms:**
```
WebSocket connection to 'ws://localhost:8080/ws-location/...' failed
```

**Possible Causes & Solutions:**

1. **Backend not running**
   ```bash
   # Solution: Start backend
   mvn spring-boot:run
   ```

2. **Wrong port**
   ```typescript
   // Check your WebSocket URL matches backend port
   webSocketFactory: () => new SockJS('http://localhost:8080/ws-location')
   ```

3. **Firewall blocking WebSocket**
   ```bash
   # Windows: Allow Java through firewall
   # Linux: Check iptables rules
   ```

### Problem 4: Connection Drops Immediately
**Symptoms:**
```
✅ WebSocket Connected
⚠️ WebSocket Closed immediately
```

**Solution:**
```typescript
// Add heartbeat configuration to keep connection alive
heartbeatIncoming: 4000,
heartbeatOutgoing: 4000,
reconnectDelay: 5000
```

### Problem 5: Messages Not Received
**Symptoms:**
- Connection works
- No location updates arrive

**Debugging Steps:**

1. **Check subscription topic:**
   ```typescript
   // Ensure topic matches backend
   const destination = `/topic/location/${missionId}`;
   ```

2. **Verify backend is sending:**
   ```bash
   # Check backend logs for:
   Sending location update to /topic/location/123
   ```

3. **Test with curl:**
   ```bash
   # Start tracking manually
   curl -X POST http://localhost:8080/api/tracking/start/123 \
     -H "Authorization: Bearer YOUR_TOKEN"
   ```

---

## ✅ Best Practices

### 1. Connection Management

```typescript
// ✅ GOOD: Connect once in AppComponent
@Component({ selector: 'app-root' })
export class AppComponent implements OnInit {
  constructor(private websocketService: WebSocketService) {}
  
  ngOnInit() {
    // Connect when app starts
    this.websocketService.connect();
  }
}

// ❌ BAD: Connecting in every component
@Component({ selector: 'app-mission' })
export class MissionComponent implements OnInit {
  ngOnInit() {
    this.websocketService.connect(); // Creates duplicate connections!
  }
}
```

### 2. Subscription Cleanup

```typescript
// ✅ GOOD: Always unsubscribe
export class MissionTrackingComponent implements OnDestroy {
  subscription: any;
  
  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}

// ❌ BAD: Forgetting to unsubscribe (memory leak!)
export class MissionTrackingComponent {
  // No ngOnDestroy = subscription keeps running
}
```

### 3. Error Handling

```typescript
// ✅ GOOD: Handle all error cases
this.locationService.startTracking(missionId).subscribe({
  next: (response) => {
    this.showSuccess('Tracking started');
  },
  error: (error) => {
    if (error.status === 404) {
      this.showError('Mission not found');
    } else if (error.status === 403) {
      this.showError('You don\'t have permission');
    } else {
      this.showError('Failed to start tracking');
    }
  }
});

// ❌ BAD: No error handling
this.locationService.startTracking(missionId).subscribe();
```

### 4. Production Configuration

```typescript
// ✅ GOOD: Use environment variables
import { environment } from '../environments/environment';

export class WebSocketService {
  connect(): void {
    this.client = new Client({
      webSocketFactory: () => new SockJS(environment.websocketUrl),
      debug: environment.production ? () => {} : (str) => console.log(str)
    });
  }
}

// environment.ts (development)
export const environment = {
  production: false,
  websocketUrl: 'http://localhost:8080/ws-location'
};

// environment.prod.ts (production)
export const environment = {
  production: true,
  websocketUrl: 'https://yourdomain.com/ws-location'
};
```

### 5. Security

```typescript
// ✅ GOOD: Use WSS (WebSocket Secure) in production
webSocketFactory: () => new SockJS('wss://yourdomain.com/ws-location')

// ❌ BAD: Using WS in production (not encrypted)
webSocketFactory: () => new SockJS('ws://yourdomain.com/ws-location')
```

---

## 📊 Connection Flow Summary

```
1. App Starts
   └─> AppComponent.ngOnInit()
       └─> websocketService.connect()
           └─> Create STOMP client
               └─> Attempt connection
                   ├─> Success: onConnect callback
                   │   └─> connectionStatus.next(true)
                   └─> Error: onStompError callback
                       └─> connectionStatus.next(false)

2. User Opens Mission Page
   └─> MissionComponent.ngOnInit()
       └─> Check if connected
           ├─> Yes: Subscribe immediately
           └─> No: Wait for connection
               └─> isConnected$.subscribe()
                   └─> Subscribe when ready

3. Backend Sends Location Update
   └─> LocationTrackingService.sendNextLocation()
       └─> messagingTemplate.convertAndSend()
           └─> WebSocket sends to /topic/location/123
               └─> Angular subscription callback
                   └─> Update UI / Map

4. User Leaves Mission Page
   └─> MissionComponent.ngOnDestroy()
       └─> subscription.unsubscribe()
           └─> Stop receiving updates

5. User Logs Out
   └─> LogoutComponent.logout()
       └─> websocketService.disconnect()
           └─> Close connection
```

---

## 🎯 Quick Start Checklist

- [ ] **Backend:** Added `.requestMatchers("/ws-location/**").permitAll()` to SecurityConfig
- [ ] **Backend:** Restart server: `mvn spring-boot:run`
- [ ] **Frontend:** Install dependencies: `npm install sockjs-client @stomp/stompjs`
- [ ] **Frontend:** Create `websocket.service.ts`
- [ ] **Frontend:** Create `location-tracking.service.ts`
- [ ] **Frontend:** Connect in `AppComponent`
- [ ] **Frontend:** Subscribe in your tracking component
- [ ] **Test:** Open browser console, check for "✅ WebSocket Connected"
- [ ] **Test:** Open Network tab, verify WebSocket connection (Status 101)
- [ ] **Test:** Start a mission with status "EN_COURS"
- [ ] **Test:** Verify location updates arriving every 5 seconds

---

## 📞 Need Help?

If you still encounter issues:

1. **Check backend logs** for errors
2. **Check browser console** for JavaScript errors
3. **Check Network tab** for failed requests
4. **Verify:** Backend running on port 8080
5. **Verify:** Frontend running on port 4200
6. **Verify:** No firewall blocking connections

---

**Last Updated:** December 1, 2025
**Backend Version:** Spring Boot 3.5.7
**Frontend Version:** Angular (any version compatible with TypeScript)
