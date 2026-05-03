import config from './config.js';

export async function getCurrentUser() {
    try {
        const res = await fetch(`http://${config.apiBaseUrl}/auth/me`, {
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
        const res = await fetch(`http://${config.apiBaseUrl}/auth/check-admin`, {
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
        const res = await fetch(`http://${config.apiBaseUrl}/auth/protected-endpoint`, {
            method: 'GET',
            credentials: 'include'
        });
        return res.ok;
    } catch (err) {
        console.error('Auth check failed:', err);
        return false;
    }
}

import router from './router';

const INACTIVITY_LIMIT = 60 * 60 * 1000;
let inactivityTimer = null;

function logoutDueToInactivity() {
    document.cookie = "jwt=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
    router.push('/login');
}

function resetInactivityTimer() {
    clearTimeout(inactivityTimer);
    inactivityTimer = setTimeout(logoutDueToInactivity, INACTIVITY_LIMIT);
}

const originalFetch = window.fetch;
window.fetch = async (...args) => {
    const response = await originalFetch(...args);
    resetInactivityTimer();
    return response;
};

resetInactivityTimer();
