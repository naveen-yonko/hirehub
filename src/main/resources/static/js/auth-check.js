// File: static/js/auth-check.js
// Common authentication check for dashboards

function checkAuth() {
    const token = localStorage.getItem('hirehub_token');
    const userStr = localStorage.getItem('hirehub_user');
    
    if (!token || !userStr) {
        window.location.href = '/index.html';
        return false;
    }

    try {
        const user = JSON.parse(userStr);
        const payload = JSON.parse(atob(token.split('.')[1]));
        
        // Check token expiration
        if (payload.exp && Date.now() >= payload.exp * 1000) {
            console.log('Token expired');
            logout();
            return false;
        }

        // Check if on correct dashboard
        const currentPage = window.location.pathname;
        const isWorkerDash = currentPage.includes('dashboard-worker');
        const isCustomerDash = currentPage.includes('dashboard-customer');
        
        if (isWorkerDash && user.role.toLowerCase() !== 'worker' ||
            isCustomerDash && user.role.toLowerCase() !== 'customer') {
            window.location.href = '/index.html';
            return false;
        }

        return true;
    } catch (e) {
        console.error('Error checking auth:', e);
        logout();
        return false;
    }
}

function logout() {
    localStorage.removeItem('hirehub_token');
    localStorage.removeItem('hirehub_user');
    window.location.href = '/index.html';
}

// Run auth check on page load
if (!checkAuth()) {
    throw new Error('Authentication check failed');
}