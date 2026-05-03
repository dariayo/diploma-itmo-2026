import {createRouter, createWebHistory} from 'vue-router';
import Pilots from '@/views/pilots/Pilots.vue';
import LoginPage from '@/views/LoginPage.vue';
import {isAuthenticated} from '@/utils.js';
import PilotEfficiency from '@/views/pilots/PilotEfficiency.vue';
import PilotCompare from '@/views/pilots/PilotCompare.vue';

const routes = [
    {path: '/', redirect: '/pilots'},
    {
        path: '/pilots',
        name: 'Pilots',
        component: Pilots
    },
    {
        path: '/pilot/:id',
        name: 'PilotEfficiency',
        component: PilotEfficiency,
        props: true
    },
    {
        path: '/pilotcompare',
        name: 'PilotCompare',
        component: PilotCompare
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;
