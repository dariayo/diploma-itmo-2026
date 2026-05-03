import {defineStore} from 'pinia'
import {config} from '@/config.js'

export const usePeriodStore = defineStore('period', {
    state: () => ({
        selectedPeriod: 'custom',
        customStart: '',
        customEnd: '',
        showCustom: false,
        periodChanged: false,
        cachedData: {
            mainPage: null,
            pilots: null,
            flights: null,
            commercial: null,
            stages: null
        },
        route: null,
        family: null,
        choosed_family: null,
        tentacles: 5,
        flightStart: null,
        currentPeriodForPage: {
            mainPage: null,
            pilots: null,
            flights: null,
            commercial: null,
            stages: null
        },
        cacheTimestamp: {
            mainPage: null,
            pilots: null,
            flights: null,
            commercial: null,
            stages: null
        },
        _version: 1,
        selectedRoutes: [],
        isInitialized: false,
        expandedRoutesStages: [],
        pageFiltersStages: {
            date_from: '',
            tentacles: 1,
            selectedStage: 'taxi_before',
            KVS: '',
            SelectedRoutes: [],
            typemodOn: []
        }
    }),

    getters: {
        periodText: (state) => {
            const now = new Date()
            let startDate, endDate

            switch (state.selectedPeriod) {
                case 'month':
                    startDate = new Date(now.getFullYear(), now.getMonth(), 1)
                    endDate = new Date(now.getFullYear(), now.getMonth() + 1, 0)
                    return `Текущий месяц: ${formatDisplayDate(startDate)} - ${formatDisplayDate(endDate)}`
                case 'lastMonth':
                    startDate = new Date(now.getFullYear(), now.getMonth() - 1, 1)
                    endDate = new Date(now.getFullYear(), now.getMonth(), 0)
                    return `Прошлый месяц: ${formatDisplayDate(startDate)} - ${formatDisplayDate(endDate)}`
                case 'quarter':
                    const quarterStartMonth = Math.floor(now.getMonth() / 3) * 3
                    startDate = new Date(now.getFullYear(), quarterStartMonth, 1)
                    endDate = new Date(now.getFullYear(), quarterStartMonth + 3, 0)
                    return `Квартал: ${formatDisplayDate(startDate)} - ${formatDisplayDate(endDate)}`
                case 'year':
                    startDate = new Date(now.getFullYear(), 0, 1)
                    endDate = new Date(now.getFullYear(), 11, 31)
                    return `Год: ${formatDisplayDate(startDate)} - ${formatDisplayDate(endDate)}`
                case 'custom':
                    if (state.customStart && state.customEnd) {
                        return `${state.customStart} - ${state.customEnd}`
                    } else {
                        const defaultStart = new Date(now.getFullYear(), now.getMonth(), 1)
                        const defaultEnd = new Date(now.getFullYear(), now.getMonth() + 1, 0)
                        return `Текущий месяц: ${formatDisplayDate(defaultStart)} - ${formatDisplayDate(defaultEnd)}`
                    }
                default:
                    return 'Период не выбран'
            }
        },

        periodInfo: (state) => {
            const now = new Date()
            let startDate, endDate

            switch (state.selectedPeriod) {
                case 'month':
                    startDate = new Date(now.getFullYear(), now.getMonth(), 1)
                    endDate = new Date(now.getFullYear(), now.getMonth() + 1, 0)
                    break
                case 'lastMonth':
                    startDate = new Date(now.getFullYear(), now.getMonth() - 1, 1)
                    endDate = new Date(now.getFullYear(), now.getMonth(), 0)
                    break
                case 'quarter':
                    const q = Math.floor(now.getMonth() / 3) * 3
                    startDate = new Date(now.getFullYear(), q, 1)
                    endDate = new Date(now.getFullYear(), q + 3, 0)
                    break
                case 'year':
                    startDate = new Date(now.getFullYear(), 0, 1)
                    endDate = new Date(now.getFullYear(), 11, 31)
                    break
                case 'custom':
                    if (state.customStart && state.customEnd) {
                        startDate = new Date(state.customStart)
                        endDate = new Date(state.customEnd)
                    } else {
                        startDate = new Date(now.getFullYear(), now.getMonth(), 1)
                        endDate = new Date(now.getFullYear(), now.getMonth() + 1, 0)
                    }
                    break
                default:
                    return null
            }

            const days = Math.floor((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1
            return {startDate, days}
        },

        hasCachedData: (state) => (page) => {
            const cacheKey = page;
            if (!state.cachedData[cacheKey]) {
                return false
            }

            const currentPeriod = JSON.stringify({
                selectedPeriod: state.selectedPeriod,
                customStart: state.customStart,
                customEnd: state.customEnd
            })

            const periodMatches = state.currentPeriodForPage[page] === currentPeriod
            if (!periodMatches) {
                return false
            }

            const cacheAge = Date.now() - (state.cacheTimestamp[page] || 0)
            const isFresh = cacheAge < 60 * 60 * 1000

            return periodMatches && isFresh
        },

        isCustomPeriodValid: (state) => {
            return state.selectedPeriod === 'custom' &&
                state.customStart &&
                state.customEnd
        }
    },

    actions: {
        async resetPeriod() {
            try {
                const resp = await fetch(`http://${config.apiBaseUrl}/settings/resetDts`, {
                    method: "PUT",
                    credentials: "include"
                })

                if (resp.ok) {
                    const data = await resp.json()

                    this.selectedPeriod = 'custom'
                    this.customStart = data.startDt
                    this.customEnd = data.endDt
                    this.showCustom = false

                    this.clearCache()
                    this.saveToLocalStorage()
                }
            } catch (e) {
                console.error("Ошибка resetPeriod:", e)
            }
        },

        setPeriod(period) {
            this.selectedPeriod = period
            this.showCustom = period === 'custom'
            this.periodChanged = true

            this.clearCache()
            if (!(period === 'custom')) {
                const {start, end} = calculateDatesForPeriod(period)
                this.customStart = start
                this.customEnd = end
                this.saveToLocalStorage()

                this.saveToBackend()
            }
        },
        setFlightStart(date) {
            this.flightStart = date
        },
        setCustomPeriod(start, end) {
            this.customStart = start
            this.customEnd = end
            this.periodChanged = true
            this.clearCache()
            this.saveToLocalStorage()
        },
        getExpandedRoutesStages() {
            return this.expandedRoutesStages || [];
        },
        getPageFiltersStages() {
            return this.pageFiltersStages;
        },
        filtersMatchStages(filters) {
            const savedFilters = this.pageFiltersStages;
            if (!savedFilters) return false;

            const keysToCheck = ['date_from', 'tentacles', 'selectedStage', 'KVS'];
            for (const key of keysToCheck) {
                if (savedFilters[key] !== filters[key]) {
                    return false;
                }
            }

            if (JSON.stringify(savedFilters.SelectedRoutes) !== JSON.stringify(filters.SelectedRoutes)) {
                return false;
            }

            if (JSON.stringify(savedFilters.typemodOn) !== JSON.stringify(filters.typemodOn)) {
                return false;
            }

            return true;
        },

        setCachedDataStages(data, filters) {
            const page = 'stages';
            this.cachedData[page] = data;
            this.currentPeriodForPage[page] = JSON.stringify({
                selectedPeriod: this.selectedPeriod,
                customStart: this.customStart,
                customEnd: this.customEnd
            });
            this.cacheTimestamp[page] = Date.now();

            if (filters) {
                this.pageFiltersStages = filters;
            }

            this.saveCacheToLocalStorage();
        },

        clearCacheStages() {
            const page = 'stages';
            this.cachedData[page] = null;
            this.cacheTimestamp[page] = null;
            this.expandedRoutesStages = [];
            this.pageFiltersStages = {
                date_from: '',
                tentacles: 1,
                selectedStage: 'taxi_before',
                KVS: '',
                SelectedRoutes: [],
                typemodOn: []
            };
            this.saveCacheToLocalStorage();
        },

        applyCustomPeriod() {
            this.selectedPeriod = 'custom'
            this.showCustom = false
            this.periodChanged = true
            this.clearCache()
            this.saveToLocalStorage()
            this.saveToBackend()
        },

        resetPeriodChanged() {
            this.periodChanged = false
        },

        setCachedData(page, data) {
            this.cachedData[page] = data
            this.currentPeriodForPage[page] = JSON.stringify({
                selectedPeriod: this.selectedPeriod,
                customStart: this.customStart,
                customEnd: this.customEnd,
                tentacles: this.tentacles
            })
            this.cacheTimestamp[page] = Date.now()
            this.saveCacheToLocalStorage()
        },

        getCachedData(page) {
            if (this.hasCachedData(page)) {
                return this.cachedData[page]
            }
            return null
        },

        clearCache() {
            this.cachedData.mainPage = null
            this.cachedData.pilots = null
            this.cachedData.flights = null
            this.route = null
            this.family = null
            this.cachedData.commercial = null
            this.currentPeriodForPage.mainPage = null
            this.currentPeriodForPage.pilots = null
            this.currentPeriodForPage.flights = null
            this.currentPeriodForPage.commercial = null
            this.cacheTimestamp.mainPage = null
            this.cacheTimestamp.pilots = null
            this.cacheTimestamp.flights = null
            this.cacheTimestamp.commercial = null
            const pages = ['mainPage', 'pilots', 'flights', 'commercial', 'stages'];

            pages.forEach(page => {
                this.cachedData[page] = null;
                this.currentPeriodForPage[page] = null;
                this.cacheTimestamp[page] = null;
            });

            this.expandedRoutesStages = [];
            this.pageFiltersStages = {
                date_from: '',
                tentacles: 1,
                selectedStage: 'taxi_before',
                KVS: '',
                SelectedRoutes: [],
                typemodOn: []
            };

            this.route = null;
            this.family = null;

        },

        saveToLocalStorage() {
            const settings = {
                selectedPeriod: this.selectedPeriod,
                customStart: this.customStart,
                customEnd: this.customEnd,
                timestamp: Date.now(),
                tentacles: this.tentacles,
                selectedRoutes: this.selectedRoutes,
                _version: this._version
            }
            localStorage.setItem('periodSettings', JSON.stringify(settings))
        },

        saveCacheToLocalStorage() {
            const cacheData = {
                cachedData: this.cachedData,
                currentPeriodForPage: this.currentPeriodForPage,
                cacheTimestamp: this.cacheTimestamp,
                expandedRoutesStages: this.expandedRoutesStages,
                pageFiltersStages: this.pageFiltersStages,
                timestamp: Date.now(),
                tentacles: this.tentacles,
                _version: this._version,
                selectedRoutes: this.selectedRoutes
            }
            localStorage.setItem('periodCache', JSON.stringify(cacheData))
        },

        loadFromLocalStorage() {

            const savedSettings = localStorage.getItem('periodSettings')
            if (savedSettings) {
                try {
                    const settings = JSON.parse(savedSettings)
                    this.selectedPeriod = settings.selectedPeriod || 'custom'
                    this.customStart = settings.customStart || ''
                    this.tentacles = settings.tentacles,
                    this.selectedRoutes = settings.selectedRoutes
                    this.customEnd = settings.customEnd || ''
                } catch (e) {
                    console.error('Ошибка загрузки настроек:', e)
                }
            }

            this.loadCacheFromLocalStorage()
        },

        loadCacheFromLocalStorage() {
            const cachedData = localStorage.getItem('periodCache');
            if (!cachedData) {
                return
            }

            try {
                const cache = JSON.parse(cachedData)
                const now = Date.now()

                Object.keys(this.cachedData).forEach(page => {
                    if (cache.cachedData && cache.cachedData[page]) {
                        const cacheAge = now - (cache.cacheTimestamp?.[page] || 0)

                        if (cacheAge < 60 * 60 * 1000) { // 1 час
                            this.cachedData[page] = cache.cachedData[page]
                            this.currentPeriodForPage[page] = cache.currentPeriodForPage[page]
                            this.cacheTimestamp[page] = cache.cacheTimestamp[page]
                        } else {
                            this.cachedData[page] = null
                        }
                    }
                });

                if (cache.expandedRoutesStages) {
                    this.expandedRoutesStages = cache.expandedRoutesStages;
                }

                // Загружаем фильтры
                if (cache.pageFiltersStages) {
                    this.pageFiltersStages = cache.pageFiltersStages;
                }

            } catch (e) {
                this.clearCache()
            }
        },

        async initialize() {
            if (this.isInitialized) return

            this.loadFromLocalStorage()

            if (!this.customStart || !this.customEnd) {
                try {
                    const resp = await fetch(`http://${config.apiBaseUrl}/settings/getDts`, {
                        method: "GET",
                        credentials: "include"
                    })
                    if (resp.ok) {
                        const data = await resp.json()
                        this.customStart = this.formatDateForInput(data.startDt)
                        this.customEnd = this.formatDateForInput(data.endDt)

                        this.selectedPeriod = 'custom'
                    }
                } catch (e) {
                    console.error('Ошибка загрузки периода с бекенда:', e)
                }
            }

            this.isInitialized = true
        },
        async loadFromBackend() {
            const resp = await fetch(`http://${config.apiBaseUrl}/settings/getDts`, {
                method: "GET",
                credentials: "include"
            })

            if (!resp.ok) return

            const data = await resp.json()

            this.customStart = data.startDt
            this.customEnd = data.endDt

            this.saveToLocalStorage()
        },

        async saveToBackend() {
            try {
                await fetch(`http://${config.apiBaseUrl}/settings/setDts`, {
                    method: "PUT",
                    credentials: "include",
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({
                        startDt: this.customStart,
                        endDt: this.customEnd,
                    })
                })
            } catch (e) {
                console.error('Ошибка сохранения периода на бекенд:', e)
            }
        },
        setExpandedRoutesStages(routes) {
            this.expandedRoutesStages = routes;
            this.saveCacheToLocalStorage();
        },

    }
})

function formatDisplayDate(date) {
    return date.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'long',
        year: 'numeric'
    })
}

function calculateDatesForPeriod(period) {
    const now = new Date()
    let start, end
    switch (period) {
        case 'month':
            start = new Date(now.getFullYear(), now.getMonth(), 1)
            end = new Date(now.getFullYear(), now.getMonth() + 1, 0)
            break
        case 'lastMonth':
            start = new Date(now.getFullYear(), now.getMonth() - 1, 1)
            end = new Date(now.getFullYear(), now.getMonth(), 0)
            break
        case 'quarter':
            const q = Math.floor(now.getMonth() / 3) * 3
            start = new Date(now.getFullYear(), q, 1)
            end = new Date(now.getFullYear(), q + 3, 0)
            break
        case 'year':
            start = new Date(now.getFullYear(), 0, 1)
            end = new Date(now.getFullYear(), 11, 31)
            break
        case 'custom':
            return {
                start: this.customStart,
                end: this.customEnd
            }
    }
    return {
        start: format(start),
        end: format(end)
    }
}

function format(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}