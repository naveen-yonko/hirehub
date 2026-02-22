// File: static/js/auth.js
// Handles combined login/register UI and basic interactions.
const metaApi = document.querySelector('meta[name="api-base"]');
const API_BASE = (metaApi && metaApi.content) ? metaApi.content.replace(/\/$/, '') + '/api/auth' : window.location.origin + '/api/auth';
let selectedType = 'customer';
let selectedLat = null, selectedLng = null;
let map = null, marker = null;

document.addEventListener('DOMContentLoaded', function() {
  // Theme toggle
  document.addEventListener('click', (e) => {
    if (e.target && e.target.id === 'theme-toggle') {
      document.documentElement.classList.toggle('dark');
      e.target.textContent = document.documentElement.classList.contains('dark') ? '☀️ Light' : '🌙 Night';
    }
  });

  // Tabs
  document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', e => {
      document.querySelectorAll('.tab').forEach(x => x.classList.remove('active'));
      e.target.classList.add('active');
      selectedType = e.target.dataset.type;
      if (selectedType === 'worker') showMap(); else hideMap();
    });
  });

  // Initialize the rest of the event listeners
  initializeEventListeners();
});

// Toggle login/register
document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('show-register').addEventListener('click', e => {
    e.preventDefault();
    document.getElementById('login-form').style.display = 'none';
    document.getElementById('register-form').style.display = 'block';
    document.getElementById('form-title').textContent = 'Create account';
    if (selectedType === 'worker') showMap();
  });
});

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('show-login').addEventListener('click', e => {
    e.preventDefault();
    document.getElementById('login-form').style.display = 'block';
    document.getElementById('register-form').style.display = 'none';
    document.getElementById('form-title').textContent = 'Login';
    hideMap();
  });
});

function showMap() {
  const mapDiv = document.getElementById('map');
  mapDiv.style.display = 'block';
  if (!map) {
    map = L.map('map').setView([20.5937, 78.9629], 5);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(pos => {
        const { latitude, longitude } = pos.coords;
        map.setView([latitude, longitude], 13);
        marker = L.marker([latitude, longitude]).addTo(map).bindPopup("Your location").openPopup();
        selectedLat = latitude; selectedLng = longitude;
      });
    }
    map.on('click', e => {
      selectedLat = e.latlng.lat; selectedLng = e.latlng.lng;
      if (marker) marker.remove();
      marker = L.marker([selectedLat, selectedLng]).addTo(map).bindPopup("Selected Location").openPopup();
    });
  }
}

function hideMap() {
  const mapDiv = document.getElementById('map');
  if (mapDiv) mapDiv.style.display = 'none';
}

// Register
document.getElementById('btn-register').addEventListener('click', async () => {
  const name = document.getElementById('name').value.trim();
  const email = document.getElementById('reg-email').value.trim();
  const password = document.getElementById('reg-password').value.trim();

  if (!name || !email || !password) return setStatus('Please fill all required fields.');
  if (password.length < 6) return setStatus('Password must be at least 6 characters.');

  const body = { name, email, password, role: selectedType };
  if (selectedType === 'worker') {
    if (!selectedLat || !selectedLng) return setStatus('Please select a location on the map.');
    body.location = { lng: selectedLng, lat: selectedLat };
  }

  // UI: show loader and disable
  document.getElementById('btn-register').disabled = true;
  document.getElementById('register-loading').style.display = 'block';
  try {
    const res = await fetch(API_BASE + '/register', {
      method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(body)
    });
    const data = await res.json();
    if (!res.ok) { setStatus(data.message || 'Signup failed.'); return; }
    // Auto-login: call /login to receive token (backend expects email,password,role)
    try {
      const loginRes = await fetch(API_BASE + '/login', {
        method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({ email, password, role: selectedType })
      });
      const loginData = await loginRes.json();
      if (!loginRes.ok) { setStatus('Registered but login failed: ' + (loginData.message || '')); return; }
      localStorage.setItem('hirehub_token', loginData.token);
      localStorage.setItem('hirehub_user', JSON.stringify({ id: loginData.userId, role: loginData.role }));
      setStatus('Registered and logged in. Redirecting...');
      window.setTimeout(() => {
        if (loginData.role && loginData.role.toLowerCase() === 'worker') window.location.href = 'dashboard-worker.html';
        else window.location.href = 'dashboard-customer.html';
      }, 600);
    } catch(err) {
      console.error(err); setStatus('Registered but network error during auto-login.');
    } finally {
      document.getElementById('btn-register').disabled = false;
      document.getElementById('register-loading').style.display = 'none';
    }
  } catch (err) {
    console.error(err); setStatus('Network error during register.');
    document.getElementById('btn-register').disabled = false;
    document.getElementById('register-loading').style.display = 'none';
  }
});

// Login
document.getElementById('btn-login').addEventListener('click', async () => {
  const email = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value.trim();
  document.getElementById('network-error').innerText = '';
  // UI: disable login and show loader
  document.getElementById('btn-login').disabled = true;
  document.getElementById('login-loading').style.display = 'block';
  try {
    const res = await fetch(API_BASE + '/login', {
      method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({ email, password, role: selectedType })
    });
    const data = await res.json();
    if (!res.ok) { document.getElementById('network-error').innerText = data.message || 'Login failed.'; return; }
    localStorage.setItem('hirehub_token', data.token);
    localStorage.setItem('hirehub_user', JSON.stringify({ id: data.userId, role: data.role }));
    // Redirect based on role
    if (data.role && data.role.toLowerCase() === 'worker') window.location.href = 'dashboard-worker.html';
    else window.location.href = 'dashboard-customer.html';
  } catch (err) { console.error(err); document.getElementById('network-error').innerText = 'Network error. Check backend.'; }
  finally { document.getElementById('btn-login').disabled = false; document.getElementById('login-loading').style.display = 'none'; }
});

// Logout and token helpers
function logout() {
  localStorage.removeItem('hirehub_token');
  localStorage.removeItem('hirehub_user');
  document.getElementById('logout-btn').style.display = 'none';
  // reset UI to login
  document.getElementById('login-form').style.display = 'block';
  document.getElementById('register-form').style.display = 'none';
}

function getToken() { return localStorage.getItem('hirehub_token') || localStorage.getItem('token'); }

function isTokenExpired(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const exp = payload.exp; if (!exp) return false;
    return Date.now() >= exp * 1000;
  } catch(e){ return false; }
}

// Init: if token present and not expired, redirect to appropriate dashboard
(function initAuthUi(){
  const token = getToken();
  const userStr = localStorage.getItem('hirehub_user');
  if (token && !isTokenExpired(token) && userStr) {
    try {
      const user = JSON.parse(userStr);
      if (user.role.toLowerCase() === 'worker') {
        window.location.href = 'dashboard-worker.html';
      } else {
        window.location.href = 'dashboard-customer.html';
      }
    } catch (e) {
      console.error('Error parsing user data:', e);
      logout();
    }
  }
})();

function setStatus(msg) { document.getElementById('status').textContent = msg; }

function initializeEventListeners() {
  // Register button
  const btnRegister = document.getElementById('btn-register');
  if (btnRegister) {
    btnRegister.addEventListener('click', async () => {
      const name = document.getElementById('name').value.trim();
      const email = document.getElementById('reg-email').value.trim();
      const password = document.getElementById('reg-password').value.trim();

      if (!name || !email || !password) return setStatus('Please fill all required fields.');
      if (password.length < 6) return setStatus('Password must be at least 6 characters.');

      const body = { name, email, password, role: selectedType };
      if (selectedType === 'worker') {
        if (!selectedLat || !selectedLng) return setStatus('Please select a location on the map.');
        body.location = { lng: selectedLng, lat: selectedLat };
      }

      document.getElementById('btn-register').disabled = true;
      document.getElementById('register-loading').style.display = 'block';
      try {
        const res = await fetch(API_BASE + '/register', {
          method: 'POST',
          headers: {'Content-Type':'application/json'},
          body: JSON.stringify(body)
        });
        const data = await res.json();
        if (!res.ok) {
          setStatus(data.message || 'Signup failed.');
          return;
        }
        // Auto-login
        try {
          const loginRes = await fetch(API_BASE + '/login', {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify({ email, password, role: selectedType })
          });
          const loginData = await loginRes.json();
          if (!loginRes.ok) {
            setStatus('Registered but login failed: ' + (loginData.message || ''));
            return;
          }
          localStorage.setItem('hirehub_token', loginData.token);
          localStorage.setItem('hirehub_user', JSON.stringify({ id: loginData.userId, role: loginData.role }));
          setStatus('Registered and logged in. Redirecting...');
          window.setTimeout(() => {
            if (loginData.role && loginData.role.toLowerCase() === 'worker') {
              window.location.href = 'dashboard-worker.html';
            } else {
              window.location.href = 'dashboard-customer.html';
            }
          }, 600);
        } catch(err) {
          console.error(err);
          setStatus('Registered but network error during auto-login.');
        }
      } catch (err) {
        console.error(err);
        setStatus('Network error during register.');
      } finally {
        document.getElementById('btn-register').disabled = false;
        document.getElementById('register-loading').style.display = 'none';
      }
    });
  }

  // Login button
  const btnLogin = document.getElementById('btn-login');
  if (btnLogin) {
    btnLogin.addEventListener('click', async () => {
      const email = document.getElementById('email').value.trim();
      const password = document.getElementById('password').value.trim();
      document.getElementById('network-error').innerText = '';
      
      document.getElementById('btn-login').disabled = true;
      document.getElementById('login-loading').style.display = 'block';
      
      try {
        const res = await fetch(API_BASE + '/login', {
          method: 'POST',
          headers: {'Content-Type':'application/json'},
          body: JSON.stringify({ email, password, role: selectedType })
        });
        const data = await res.json();
        if (!res.ok) {
          document.getElementById('network-error').innerText = data.message || 'Login failed.';
          return;
        }
        localStorage.setItem('hirehub_token', data.token);
        localStorage.setItem('hirehub_user', JSON.stringify({ id: data.userId, role: data.role }));
        
        if (data.role && data.role.toLowerCase() === 'worker') {
          window.location.href = 'dashboard-worker.html';
        } else {
          window.location.href = 'dashboard-customer.html';
        }
      } catch (err) {
        console.error(err);
        document.getElementById('network-error').innerText = 'Network error. Check backend.';
      } finally {
        document.getElementById('btn-login').disabled = false;
        document.getElementById('login-loading').style.display = 'none';
      }
    });
  }

  // Show register form
  const showRegister = document.getElementById('show-register');
  if (showRegister) {
    showRegister.addEventListener('click', e => {
      e.preventDefault();
      document.getElementById('login-form').style.display = 'none';
      document.getElementById('register-form').style.display = 'block';
      document.getElementById('form-title').textContent = 'Create account';
      if (selectedType === 'worker') showMap();
    });
  }

  // Show login form
  const showLogin = document.getElementById('show-login');
  if (showLogin) {
    showLogin.addEventListener('click', e => {
      e.preventDefault();
      document.getElementById('login-form').style.display = 'block';
      document.getElementById('register-form').style.display = 'none';
      document.getElementById('form-title').textContent = 'Login';
      hideMap();
    });
  }
}