# 📍 Tunisia Coordinates - Location Tracking Configuration

## Updated Coordinates

The location tracking simulation now uses **real Tunisia coordinates** from Google Maps:

### Route Details

**Starting Point (Départ):**
- **Coordinates:** `35.669948, 10.591675`
- **Location:** Tunisia - Point 1

**Ending Point (Arrivée):**
- **Coordinates:** `35.522941, 11.031608`
- **Location:** Tunisia - Point 2

### Route Characteristics

- **Distance:** Approximately **45 km** (calculated using Haversine formula)
- **Direction:** East-Southeast
- **Simulation Points:** 100 intermediate points
- **Update Frequency:** Every 5 seconds
- **Total Duration:** ~8.3 minutes (100 points × 5 seconds)
- **Speed Range:** 40-80 km/h (variable)

### Map View

When you open the tracking on a map (Google Maps or Leaflet), you'll see:

```
Tunisia
     
     📍 Start (35.669948, 10.591675)
        │
        │  🚛 Transporteur follows this path
        │     (100 intermediate points)
        │
        ▼
     📍 End (35.522941, 11.031608)
```

### Google Maps Links

**View Starting Point:**
```
https://www.google.com/maps?q=35.669948,10.591675
```

**View Ending Point:**
```
https://www.google.com/maps?q=35.522941,11.031608
```

**View Route:**
```
https://www.google.com/maps/dir/35.669948,10.591675/35.522941,11.031608
```

### For Frontend Map Centering

When initializing your map in Angular, center it on Tunisia:

```typescript
// Google Maps
const center = { lat: 35.596445, lng: 10.811642 }; // Center between start and end
this.map = new google.maps.Map(document.getElementById('map'), {
  zoom: 10, // Zoom level to see whole route
  center: center,
});

// Leaflet
this.map = L.map('map').setView([35.596445, 10.811642], 10);
```

### Testing the Route

1. **Create a mission** with:
   ```json
   {
     "lieuDepart": "Tunisia - Point 1 (35.669948, 10.591675)",
     "lieuArrivee": "Tunisia - Point 2 (35.522941, 11.031608)"
   }
   ```

2. **Start mission** (status = EN_COURS)

3. **Watch on map** - The transporteur will move along the route between these two points

### Path Calculation

The system uses **linear interpolation** with slight random variations:

```
For each of 100 points:
  - Calculate ratio: i / 99
  - Latitude: 35.669948 + (35.522941 - 35.669948) × ratio
  - Longitude: 10.591675 + (11.031608 - 10.591675) × ratio
  - Add random variation: ±0.001 degrees (~100 meters)
  - Speed: Random between 40-80 km/h
```

### Distance Calculation

Using the Haversine formula:
```
Distance ≈ 45 km
Estimated time at average 60 km/h: ~45 minutes real-time
Simulation time: 8.3 minutes (accelerated)
```

---

**Updated:** December 1, 2025  
**Coordinates Source:** Google Maps  
**Build Status:** ✅ SUCCESS
