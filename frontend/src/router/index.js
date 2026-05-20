import {createRouter, createWebHistory} from 'vue-router';
import Pilots from '@/views/pilots/Pilots.vue';
import LoginPage from '@/views/LoginPage.vue';
import {isAuthenticated} from '@/utils.js';
import PilotEfficiency from '@/views/pilots/PilotEfficiency.vue';
import PilotCompare from '@/views/pilots/PilotCompare.vue';

const routes = [
    {path: '/', redirect: '/pilots'},
    {
        path: '/login',
        name: 'Login',
        component: LoginPage,
        meta: {guestOnly: true}
    },
    {
        path: '/pilots',
        name: 'Pilots',
        component: Pilots,
        meta: {requiresAuth: true}
    },
    {
        path: '/pilot/:id',
        name: 'PilotEfficiency',
        component: PilotEfficiency,
        props: true,
        meta: {requiresAuth: true}
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach(async (to) => {
    const loggedIn = await isAuthenticated();

    if (to.meta.requiresAuth && !loggedIn) {
        return {path: '/login', query: {redirect: to.fullPath}};
    }

    if (to.meta.guestOnly && loggedIn) {
        return '/pilots';
    }
});

export default router;
