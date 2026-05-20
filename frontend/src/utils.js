import config from './config.js';

const API_ORIGIN = `http://${config.apiBaseUrl}`;

export async function getCurrentUser() {
    try {
        const res = await fetch(`${API_ORIGIN}/auth/me`, {
            method: 'GET',
            credentials: 'include'
        });
        if (!res.ok) return null;
        const data = await res.json();
        return {
            fullName: data.fullName
        };
    } catch (err) {
        console.error('Failed to fetch current user:', err);
        return null;
    }
}

export async function isAdmin() {
    try {
        const res = await fetch(`${API_ORIGIN}/auth/check-admin`, {
            method: 'GET',
            credentials: 'include'
        });
        return res.ok;
    } catch (err) {
        console.error('Admin check failed:', err);
        return false;
    }
}

export async function isAuthenticated() {
    try {
        const res = await fetch(`${API_ORIGIN}/auth/protected-endpoint`, {
            method: 'GET',
            credentials: 'include'
        });
        return res.ok;
    } catch (err) {
        console.error('Auth check failed:', err);
        return false;
    }
}

const INACTIVITY_LIMIT = 60 * 60 * 1000;
let inactivityTimer = null;

function logoutDueToInactivity() {
    fetch(`${API_ORIGIN}/auth/logout`, {method: 'POST', credentials: 'include'}).finally(() => {
        if (window.location.pathname !== '/login') {
            window.location.href = '/login';
        }
    });
}

function resetInactivityTimer() {
    clearTimeout(inactivityTimer);
    inactivityTimer = setTimeout(logoutDueToInactivity, INACTIVITY_LIMIT);
}

const originalFetch = window.fetch;
window.fetch = async (...args) => {
    const [resource, options = {}] = args;
    const url = typeof resource === 'string' ? resource : resource?.url;
    const isApiRequest = typeof url === 'string' && url.startsWith(API_ORIGIN);
    const requestOptions = isApiRequest
        ? {...options, credentials: options.credentials || 'include'}
        : options;

    const response = await originalFetch(resource, requestOptions);
    resetInactivityTimer();

    const isAuthRequest = typeof url === 'string' && url.includes('/auth/');
    if (isApiRequest && response.status === 401 && !isAuthRequest && window.location.pathname !== '/login') {
        window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search + window.location.hash)}`;
    }

    return response;
};

resetInactivityTimer();
