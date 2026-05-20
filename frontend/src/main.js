import {createApp} from 'vue';
import App from './App.vue';
import router from './router';
import './assets/styles.css';
import './assets/pilots_styles.css';
import {createPinia} from "pinia";
import {isAuthenticated} from './utils.js'
import {usePeriodStore} from "@/stores/periodStore.js";
const pinia = createPinia()

createApp(App).use(router).use(pinia).mount('#app');
const periodStore = usePeriodStore()
if (await isAuthenticated()) {
    await periodStore.loadFromBackend();
    if (!periodStore.isInitialized) {
        periodStore.initialize().then(() => periodStore.isInitialized = true)
    }
}
