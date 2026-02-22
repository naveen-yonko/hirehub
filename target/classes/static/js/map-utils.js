// Haversine formula for calculating distance between coordinates
function haversine(lat1, lon1, lat2, lon2) {
    const R = 6371; // Earth's radius in kilometers
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = 
        Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * 
        Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c; // Distance in kilometers
}

// Enhanced custom markers for map
const createCustomMarker = (type, color) => {
    return L.divIcon({
        html: `
            <div class="marker-container">
                <i class="fas fa-${type}" style="color: ${color}; font-size: 24px;"></i>
                <div class="marker-pulse" style="border-color: ${color}"></div>
            </div>
        `,
        className: 'custom-marker',
        iconSize: [40, 40],
        iconAnchor: [20, 40],
        popupAnchor: [0, -40]
    });
};

// Location marker icons with improved visibility
const locationMarker = createCustomMarker('map-marker-alt', '#2563eb');
const workerMarker = createCustomMarker('hard-hat', '#059669');

// Create an enhanced worker popup with rating stars and better formatting
function createWorkerPopup(worker, distance) {
    const rating = worker.averageRating || 0;
    const fullStars = Math.floor(rating);
    const halfStar = rating % 1 >= 0.5;
    const emptyStars = 5 - Math.ceil(rating);
    
    const stars = Array(fullStars).fill('<i class="fas fa-star"></i>').join('') +
                 (halfStar ? '<i class="fas fa-star-half-alt"></i>' : '') +
                 Array(emptyStars).fill('<i class="far fa-star"></i>').join('');
                 
    const workerTypeCapitalized = worker.workerType.charAt(0).toUpperCase() + worker.workerType.slice(1);
    
    return `
        <div class="worker-popup">
            <div class="popup-header">
                <h4>${workerTypeCapitalized}</h4>
                <span class="distance-badge">
                    <i class="fas fa-location-arrow"></i> ${distance.toFixed(1)} km
                </span>
            </div>
            <div class="popup-details">
                <p class="rate">
                    <i class="fas fa-rupee-sign"></i> 
                    <strong>₹${worker.charge || 0}</strong>/hour
                </p>
                <p class="rating">
                    <span class="stars">${stars}</span>
                    <span class="rating-text">${rating.toFixed(1)} (${worker.ratingCount || 0} reviews)</span>
                </p>
                <p class="availability">
                    <i class="fas fa-clock"></i> Available Now
                </p>
            </div>
            <div class="popup-actions">
                <button onclick="showServiceLocationForm('${worker.id}')" 
                        class="btn btn-primary btn-sm">
                    <i class="fas fa-handshake"></i> Request Service
                </button>
            </div>
        </div>
    `;
}

// Create an enhanced worker card with better visual hierarchy and interactivity
function createWorkerCard(worker, userLat, userLng) {
    const distance = haversine(userLat, userLng, worker.location.y, worker.location.x);
    const rating = worker.averageRating || 0;
    
    // Generate star rating with full, half, and empty stars
    const fullStars = Math.floor(rating);
    const halfStar = rating % 1 >= 0.5;
    const emptyStars = 5 - Math.ceil(rating);
    
    const stars = Array(fullStars).fill('<i class="fas fa-star"></i>').join('') +
                 (halfStar ? '<i class="fas fa-star-half-alt"></i>' : '') +
                 Array(emptyStars).fill('<i class="far fa-star"></i>').join('');
    
    const div = document.createElement('div');
    div.className = 'worker-card';
    
    const workerTypeCapitalized = worker.workerType.charAt(0).toUpperCase() + worker.workerType.slice(1);
    
    // Add status indicator based on distance
    const statusClass = distance <= 2 ? 'status-nearby' : 
                       distance <= 5 ? 'status-medium' : 'status-far';
    
    div.innerHTML = `
        <div class="worker-header">
            <div class="worker-main-info">
                <h4>
                    <i class="fas fa-hard-hat"></i> 
                    ${workerTypeCapitalized}
                    <span class="status-dot ${statusClass}"></span>
                </h4>
                <div class="worker-rating">
                    <div class="stars">${stars}</div>
                    <span class="rating-count">(${worker.ratingCount || 0})</span>
                </div>
            </div>
            <span class="distance-badge">
                <i class="fas fa-location-arrow"></i> 
                ${distance.toFixed(1)} km
            </span>
        </div>
        
        <div class="worker-details">
            <div class="detail-item">
                <i class="fas fa-rupee-sign"></i>
                <div class="detail-content">
                    <span class="detail-label">Rate</span>
                    <span class="detail-value">₹${worker.charge || 0}/hour</span>
                </div>
            </div>
            <div class="detail-item">
                <i class="fas fa-clock"></i>
                <div class="detail-content">
                    <span class="detail-label">Status</span>
                    <span class="detail-value available">Available Now</span>
                </div>
            </div>
            <div class="detail-item">
                <i class="fas fa-star"></i>
                <div class="detail-content">
                    <span class="detail-label">Rating</span>
                    <span class="detail-value">${rating.toFixed(1)} out of 5</span>
                </div>
            </div>
        </div>
        
        <div class="worker-actions">
            <button onclick="showServiceLocationForm('${worker.id}')" 
                    class="btn btn-primary request-btn">
                <i class="fas fa-handshake"></i> 
                Request Service
            </button>
        </div>
    `;
    return div;
}

// Global variable to store markers
let workerMarkers = [];

// Function to clear all worker markers from the map
function clearWorkerMarkers() {
    workerMarkers.forEach(marker => marker.remove());
    workerMarkers = [];
}

// Function to create and add a worker marker to the map
function addWorkerMarker(worker, map) {
    const marker = L.marker([worker.location.y, worker.location.x], {
        icon: workerMarker
    });
    marker.worker = worker;
    workerMarkers.push(marker);
    return marker;
}

// Function to format the distance
function formatDistance(meters) {
    if (meters < 1000) {
        return `${Math.round(meters)}m`;
    }
    return `${(meters/1000).toFixed(1)}km`;
}