<template>
  <div class="layout">
    <main class="content pilot-detail">
      <header class="detail-toolbar">
        <button type="button" class="back-link" @click="goBack">
          ← К списку пилотов
        </button>
        <router-link to="/pilots#pilot-compare" class="link-secondary">
          Сравнительный анализ
        </router-link>
      </header>

      <section class="pilot-hero">
        <div class="hero-visual" aria-hidden="true">
          <div class="avatar-blob">{{ tabInitial }}</div>
          <div class="orbit orbit-a"></div>
          <div class="orbit orbit-b"></div>
        </div>

        <div class="hero-text">
          <p class="eyebrow">Карточка пилота · КВС</p>
          <h1 class="hero-title">{{ pilotId }}</h1>
          <p class="hero-lead">
            Сводка за выбранный период, фильтры по маршруту и типу ВС и детализация каждого направления.
          </p>
          <div class="period-chip">{{ periodStore.periodText }}</div>
        </div>

        <div class="kpi-grid">
          <div class="kpi-card kpi-accent">
            <span class="kpi-label">Средняя эффективность</span>
            <span class="kpi-value" :style="{ color: fuelEfficiencyColor(overallMetrics.fuel) }">
              {{ overallMetrics.count ? overallMetrics.fuel.toFixed(1) + '%' : '—' }}
            </span>
            <div class="kpi-strip" aria-hidden="true">
              <div class="kpi-strip-fill fuel" :style="{ width: clampPct(overallMetrics.fuel) + '%' }"/>
            </div>
          </div>
          <div class="kpi-card">
            <span class="kpi-label">Пунктуальность</span>
            <span class="kpi-value">{{ overallMetrics.count ? overallMetrics.punct.toFixed(1) + '%' : '—' }}</span>
            <div class="kpi-strip" aria-hidden="true">
              <div class="kpi-strip-fill punct" :style="{ width: clampPct(overallMetrics.punct) + '%' }"/>
            </div>
          </div>
          <div class="kpi-card">
            <span class="kpi-label">Сложность</span>
            <span class="kpi-value">{{ overallMetrics.count ? overallMetrics.diff.toFixed(1) + '%' : '—' }}</span>
            <div class="kpi-strip" aria-hidden="true">
              <div class="kpi-strip-fill diff" :style="{ width: clampPct(overallMetrics.diff) + '%' }"/>
            </div>
          </div>
          <div class="kpi-card kpi-mini">
            <span class="kpi-label">Рейсов / налёт</span>
            <span class="kpi-big">{{ flightCount }}</span>
            <span class="kpi-secondary">{{ flightHours }} ч за период</span>
          </div>
        </div>
      </section>

      <div class="period-bar-wrap">
        <PeriodBar/>
      </div>

      <section class="surface filters-surface">
        <h2 class="surface-title">Фильтры</h2>
        <div class="filters-grid">
          <div class="filter-field">
            <label class="filter-label">Маршрут</label>
            <select v-model="selectedRoute" class="filter-select" @change="applyFilters">
              <option value="">Все маршруты</option>
              <option v-for="route in routesList" :key="route">{{ route }}</option>
            </select>
          </div>
          <div class="filter-field">
            <label class="filter-label">Номер рейса</label>
            <select v-model="selectedFlight" class="filter-select" @change="applyFilters">
              <option value="">Все рейсы</option>
              <option v-for="flt in flights" :key="flt.flightNumber">{{ flt.flightNumber }}</option>
            </select>
          </div>
          <div class="filter-actions">
            <button type="button" class="btn-ghost-sm" @click="resetFilters">Сбросить</button>
            <button type="button" class="btn-solid-sm" @click="applyFilters">Обновить</button>
          </div>
        </div>
      </section>

      <section class="surface aircraft-surface">
        <span class="aircraft-caption">Тип воздушного судна · выберите одно или несколько</span>
        <div class="aircraft-pills">
          <button
              v-for="(name, i) in listTypeMod"
              :key="name"
              type="button"
              class="ac-pill"
              :class="{ active: typemodOn[i] }"
              @click="typebutton(i)"
          >
            <img src="@/assets/images/plane.svg" alt="" class="ac-plane"/>
            {{ name }}
          </button>
        </div>
      </section>

      <div v-if="loading" class="loading-block">
        <div class="spinner" aria-hidden="true"/>
        <p>Загрузка данных пилота…</p>
      </div>

      <section v-else class="routes-wrap">
        <div class="routes-head">
          <h2 class="routes-title">Направления и рейсы</h2>
          <p class="routes-hint">
            Нажмите на карточку, чтобы раскрыть рейсы в группе «маршрут + тип ВС».
          </p>
          <div v-if="groupedData.length" class="sort-strip" role="group" aria-label="Сортировка групп маршрутов">
            <span class="sort-label">Упорядочить группы по</span>
            <button
                v-for="h in headersRoute"
                :key="h.key"
                type="button"
                class="sort-chip"
                :class="{ active: sortConfig.key === h.key }"
                @click="sortByColumn(h.key)"
            >
              {{ shortSortLabel(h.title) }}
              <span v-if="sortConfig.key === h.key" class="sort-dir">{{
                  sortConfig.direction === 'asc' ? '↑' : '↓'
                }}</span>
            </button>
          </div>
        </div>

        <div v-if="!groupedData.length" class="empty-state">
          Нет записей под выбранные фильтры. Попробуйте сменить тип ВС или сбросить фильтры.
        </div>

        <article
            v-for="(group, index) in groupedData"
            :key="group.route + '|' + group.typeMod"
            class="route-card"
            :class="{ open: group.expanded }"
        >
          <button type="button" class="route-card-toggle" @click="toggleGroup(index)">
            <span class="chevron" aria-hidden="true">{{ group.expanded ? '⌄' : '›' }}</span>
            <div class="route-main">
              <span class="route-name">{{ group.route }}</span>
              <span class="route-type">{{ group.typeMod }}</span>
            </div>
            <dl class="route-metrics">
              <div class="route-metric">
                <dt>Эффект.</dt>
                <dd :style="{ color: fuelEfficiencyColor(Number(group.fuelEfficiency)) }">
                  {{ Number(group.fuelEfficiency).toFixed(1) }}%
                </dd>
              </div>
              <div class="route-metric">
                <dt>Пункт.</dt>
                <dd>{{ (Number(group.punctuality) * 100).toFixed(1) }}%</dd>
              </div>
              <div class="route-metric">
                <dt>Сложн.</dt>
                <dd>{{ (Number(group.difficulty) * 100).toFixed(1) }}%</dd>
              </div>
              <div class="route-metric dim">
                <dt>Рейсов</dt>
                <dd>{{ group.flights.length }}</dd>
              </div>
            </dl>
          </button>

          <transition name="fold">
            <div v-if="group.expanded" class="route-body">
              <div class="nested-scroll">
                <table class="detail-table">
                  <thead>
                  <tr>
                    <th>Рейс</th>
                    <th>Дата</th>
                    <th>Сезон</th>
                    <th>Метео</th>
                    <th>Пункт.</th>
                    <th>Дальн.</th>
                    <th>Ночь</th>
                    <th>Эффект.</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-for="flight in group.flights" :key="flight.flightNumber + String(flight.dateTime)">
                    <td>{{ flight.flightNumber }}</td>
                    <td>{{ formatDateTime(flight.dateTime) }}</td>
                    <td>{{ flight.season }}</td>
                    <td>{{ (Number(flight.calculatedResults?.weatherDifficulty) * 100).toFixed(0) }}%</td>
                    <td>{{ (Number(flight.calculatedResults?.punctuality) * 100).toFixed(0) }}%</td>
                    <td>{{ (Number(flight.calculatedResults?.length) * 100).toFixed(0) }}%</td>
                    <td>{{ (Number(flight.calculatedResults?.night) * 100).toFixed(0) }}%</td>
                    <td :style="{ fontWeight: 700, color: fuelEfficiencyColor(Number(flight.fuelEff)) }">
                      {{ Number(flight.fuelEff).toFixed(2) }}%
                    </td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </transition>
        </article>
      </section>

    </main>
  </div>
</template>

<script setup>
import {computed, nextTick, onMounted, reactive, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import config from '@/config.js'
import {usePeriodStore} from '@/stores/periodStore.js'
import PeriodBar from '@/components/PeriodBar.vue'

const periodStore = usePeriodStore()
const route = useRoute()
const router = useRouter()
const pilotId = computed(() => String(route.params.id || '—'))

const tabInitial = computed(() =>
    pilotId.value.replace(/\D/g, ''),
)

function clampPct(value) {
  const v = Number(value)
  if (Number.isNaN(v)) return 0
  return Math.min(100, Math.max(0, v))
}

const listTypeMod = ['SJ-100', 'SSJ-100', 'B738', 'B773', 'B747', 'B777', 'B739', 'A319', 'A320']
const typemodOn = ref(listTypeMod.map(() => false))

/** Резервная сетка столбцов для совместимости / будущего расширения */
const headersRoute = [
  {title: 'Маршрут', key: 'route'},
  {title: 'Тип ВС', key: 'typeMod'},
  {title: 'Топливная эффективность', key: 'fuelEfficiency'},
  {title: 'Пунктуальность', key: 'punctuality'},
  {title: 'Сложность рейса', key: 'difficulty'},
]

const selectedRoute = ref('')
const selectedFlight = ref('')
const selectedType = ref([])

const sortConfig = reactive({
  key: null,
  direction: 'asc',
})
const sortCache = new Map()

const flights = ref([])
const groupedData = ref([])

const loading = ref(false)

const families = [
  {prefix: 'A3', family: 1, name: 'A320'},
  {prefix: 'B73', family: 2, name: 'B738'},
  {prefix: 'B74', family: 3, name: 'B747'},
  {prefix: 'B77', family: 4, name: 'B777'},
  {prefix: 'SSJ', family: 5, name: 'SSJ-100'},
]

function fuelEfficiencyColor(value) {
  if (value >= 100) return '#15803d'
  if (value >= 95) return '#ea580c'
  return '#b91c1c'
}

function getFamily(type) {
  const found = families.find(f => String(type || '').startsWith(f.prefix))
  return found ? found.family : 0
}

function fuelEfficienciesFrom(rows) {
  return rows.map(f => f.fuelEff).filter(x => typeof x === 'number' && !Number.isNaN(x))
}

const flightHours = computed(() =>
    Math.round(flights.value.reduce((sum, f) => sum + (f.flightHours || 0), 0)),
)

const flightCount = computed(() => flights.value.length)

const routesList = computed(() => {
  const s = new Set(flights.value.map(f => f.route).filter(Boolean))
  return [...s].sort()
})

/** Агрегаты по текущему фильтру: пересборка после applyFilters задаёт актуальный groupedData */
const overallMetrics = computed(() => {
  const rows = [...flights.value].filter(row =>
      (!selectedRoute.value || row.route === selectedRoute.value) &&
      (!selectedFlight.value || row.flightNumber === selectedFlight.value) &&
      (!selectedType.value.length ||
          selectedType.value.some(t => getFamily(row.typeMod) === getFamily(t))),
  )
  const fe = fuelEfficienciesFrom(rows)
  const punctuality = rows.map(f => Number(f.calculatedResults?.punctuality)).filter(n => !Number.isNaN(n))
  const difficulties = rows.map(f => Number(f.calculatedResults?.difficulty)).filter(n => !Number.isNaN(n))
  const avg = arr => arr.length ? arr.reduce((a, b) => a + b, 0) / arr.length : null
  return {
    count: rows.length,
    fuel: fe.length ? (avg(fe) ?? 0) : 0,
    punct: punctuality.length ? (avg(punctuality) ?? 0) * 100 : 0,
    diff: difficulties.length ? (avg(difficulties) ?? 0) * 100 : 0,
  }
})

function goBack() {
  router.push({path: '/pilots'})
}

function resetFilters() {
  selectedRoute.value = ''
  selectedFlight.value = ''
  selectedType.value = []
  typemodOn.value = typemodOn.value.map(() => false)
  applyFilters()
}

function typebutton(i) {
  typemodOn.value[i] = !typemodOn.value[i]
  selectedType.value = typemodOn.value
      .map((on, idx) => (on ? listTypeMod[idx] : null))
      .filter(Boolean)
  applyFilters()
}

function toggleGroup(index) {
  groupedData.value[index].expanded = !groupedData.value[index].expanded
}

function formatDateTime(dt) {
  return dt ? new Date(dt).toLocaleString('ru-RU') : '—'
}

function formatDateForInput(date) {
  return date.toISOString().split('T')[0]
}

function shortSortLabel(title) {
  const map = {
    'Топливная эффективность': 'Эффективность',
    'Пунктуальность': 'Пунктуальность',
    'Сложность рейса': 'Сложность',
    'Маршрут': 'Маршрут',
    'Тип ВС': 'Тип ВС',
  }
  return map[title] || title
}

function getGroupedData(data) {
  const grouped = {}

  data.forEach(f => {
    const key = `${f.route}_${f.typeMod}`
    if (!grouped[key]) {
      grouped[key] = {
        tabNo: f.tabNo,
        route: f.route,
        typeMod: f.typeMod,
        fuelEffs: [],
        difficulties: [],
        punctualities: [],
        flights: [],
        expanded: false,
      }
    }

    grouped[key].flights.push(f)

    if (typeof f.fuelEff === 'number') grouped[key].fuelEffs.push(f.fuelEff)
    if (typeof f.calculatedResults?.difficulty === 'number') grouped[key].difficulties.push(f.calculatedResults.difficulty)
    if (typeof f.calculatedResults?.punctuality === 'number') grouped[key].punctualities.push(f.calculatedResults.punctuality)
  })

  return Object.values(grouped).map(g => {
    const avg = arr => (arr.length ? arr.reduce((a, b) => a + b, 0) / arr.length : 0)

    return {
      ...g,
      fuelEfficiency: avg(g.fuelEffs),
      difficulty: avg(g.difficulties),
      punctuality: avg(g.punctualities),
    }
  })
}

function applyFilters() {
  const filtered = flights.value.filter(f =>
      (!selectedRoute.value || f.route === selectedRoute.value) &&
      (!selectedFlight.value || f.flightNumber === selectedFlight.value) &&
      (!selectedType.value.length ||
          selectedType.value.some(t => getFamily(f.typeMod) === getFamily(t))),
  )
  groupedData.value = getGroupedData(filtered).map(g => ({...g, expanded: false}))
  sortedData()
}

function sortByColumn(key) {
  sortCache.clear()
  if (sortConfig.key === key) {
    sortConfig.direction = sortConfig.direction === 'asc' ? 'desc' : 'asc'
  } else {
    sortConfig.key = key
    sortConfig.direction = 'asc'
  }
  sortedData()
}

function sortedData() {
  if (!sortConfig.key || groupedData.value.length === 0) {
    return
  }

  const cacheKey = `${sortConfig.key}-${sortConfig.direction}`
  if (sortCache.has(cacheKey)) {
    groupedData.value = [...sortCache.get(cacheKey)]
    return
  }

  const key = sortConfig.key
  const numericKeys = ['fuelEfficiency', 'punctuality', 'difficulty']

  const sorted = [...groupedData.value].sort((a, b) => {
    let av = a[key]
    let bv = b[key]
    if (numericKeys.includes(key)) {
      av = Number(av) || 0
      bv = Number(bv) || 0
      return sortConfig.direction === 'asc' ? av - bv : bv - av
    }
    if (key === 'route' || key === 'typeMod') {
      av = String(av || '')
      bv = String(bv || '')
      return sortConfig.direction === 'asc'
          ? av.localeCompare(bv, 'ru')
          : bv.localeCompare(av, 'ru')
    }
    return 0
  })

  sortCache.set(cacheKey, sorted)
  if (sortCache.size > 50) {
    sortCache.delete(sortCache.keys().next().value)
  }
  groupedData.value = sorted
}

async function loadPilotData() {
  try {
    loading.value = true

    const periodInfo = periodStore.periodInfo
    if (!periodInfo) {
      return
    }

    const startDate = periodInfo.startDate
    const endDate = new Date(startDate)
    endDate.setDate(
        parseInt(periodInfo.days, 10) + parseInt(startDate.getDate(), 10) - 1,
    )

    const params = new URLSearchParams({
      startDate: formatDateForInput(startDate),
      endDate: formatDateForInput(endDate),
    })

    const url = `http://${config.apiBaseUrl}/pilotStatistics/getData/${pilotId.value}?${params}`

    const res = await fetch(url, {
      method: 'GET',
      credentials: 'include',
      headers: {'Content-Type': 'application/json'},
    })

    const data = await res.json()

    flights.value = Array.isArray(data) ? data : []

    const grouped = getGroupedData(flights.value)
    groupedData.value = grouped
    await nextTick()
    applyFilters()
  } catch (e) {
    console.error('Ошибка при загрузке данных пилота:', e)
  } finally {
    loading.value = false
  }
}

watch(
    () => periodStore.periodChanged,
    (changed) => {
      if (changed) {
        loadPilotData()
        periodStore.resetPeriodChanged()
      }
    },
)

watch(
    () => route.params.id,
    async () => {
      sortConfig.key = null
      sortConfig.direction = 'asc'
      selectedRoute.value = ''
      selectedFlight.value = ''
      selectedType.value = []
      typemodOn.value = typemodOn.value.map(() => false)
      await loadPilotData()
    },
)

onMounted(async () => {
  await periodStore.initialize()
  await loadPilotData()
})
</script>

<style scoped>
.pilot-detail {
  padding-bottom: 48px;
}

.detail-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}

.back-link {
  border: none;
  background: none;
  font-size: 14px;
  font-weight: 700;
  color: #1e40af;
  cursor: pointer;
  padding: 8px 0;
}

.back-link:hover {
  text-decoration: underline;
}

.link-secondary {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  text-decoration: none;
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  background: #fff;
  transition: border-color 0.2s, color 0.2s;
}

.link-secondary:hover {
  color: #1e40af;
  border-color: #bfdbfe;
}

.pilot-hero {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 24px 32px;
  padding: 28px 28px 32px;
  border-radius: 20px;
  background: linear-gradient(125deg, #f8fafc 0%, #eff6ff 42%, #f1f5f9 100%);
  border: 1px solid rgba(226, 232, 240, 0.95);
  box-shadow: 0 16px 48px rgba(15, 23, 42, 0.08);
  margin-bottom: 24px;
}

.hero-visual {
  position: relative;
  width: 112px;
  height: 112px;
  grid-row: span 2;
}

.avatar-blob {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100%;
  border-radius: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 900;
  letter-spacing: -0.04em;
  color: #fff;
  background: linear-gradient(145deg, #1e40af 0%, #6366f1 55%, #0ea5e9 100%);
  box-shadow: 0 12px 32px rgba(30, 64, 175, 0.35);
}

.orbit {
  position: absolute;
  border-radius: 50%;
  border: 2px dashed rgba(99, 102, 241, 0.35);
  pointer-events: none;
}

.orbit-a {
  inset: -10px;
  animation: spin-slow 24s linear infinite;
}

.orbit-b {
  inset: -22px;
  border-color: rgba(14, 165, 233, 0.25);
  animation: spin-slow 32s linear infinite reverse;
}

@keyframes spin-slow {
  to {
    transform: rotate(360deg);
  }
}

.hero-text {
  min-width: 0;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #64748b;
}

.hero-title {
  margin: 0 0 10px;
  font-size: 36px;
  font-weight: 900;
  letter-spacing: -0.03em;
  color: #0f172a;
  line-height: 1.05;
}

.hero-lead {
  margin: 0 0 14px;
  font-size: 14px;
  line-height: 1.55;
  color: #475569;
  max-width: 40rem;
}

.period-chip {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  color: #1e3a8a;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(191, 219, 254, 0.9);
}

.kpi-grid {
  grid-column: 2;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

@media (max-width: 1100px) {
  .pilot-hero {
    grid-template-columns: 1fr;
  }
  .hero-visual {
    grid-row: auto;
    margin: 0 auto;
  }
  .kpi-grid {
    grid-column: 1;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.kpi-card {
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(226, 232, 240, 0.95);
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 96px;
}

.kpi-accent {
  background: linear-gradient(180deg, #fff 0%, #eff6ff 100%);
  border-color: #bfdbfe;
}

.kpi-label {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #64748b;
}

.kpi-value {
  font-size: 22px;
  font-weight: 900;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.kpi-big {
  font-size: 28px;
  font-weight: 900;
  color: #1e40af;
}

.kpi-secondary {
  font-size: 12px;
  color: #64748b;
}

.kpi-strip {
  height: 5px;
  border-radius: 999px;
  background: #e2e8f0;
  margin-top: auto;
  overflow: hidden;
}

.kpi-strip-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.45s ease;
}

.kpi-strip-fill.fuel {
  background: linear-gradient(90deg, #1e40af, #22c55e);
}

.kpi-strip-fill.punct {
  background: linear-gradient(90deg, #0ea5e9, #6366f1);
}

.kpi-strip-fill.diff {
  background: linear-gradient(90deg, #f59e0b, #ef4444);
}

.period-bar-wrap {
  margin-bottom: 20px;
}

.surface {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 18px 20px;
  margin-bottom: 16px;
  box-shadow: 0 4px 18px rgba(15, 23, 42, 0.04);
}

.surface-title {
  margin: 0 0 14px;
  font-size: 15px;
  font-weight: 800;
  color: #0f172a;
}

.filters-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
}

.filter-field {
  flex: 1 1 200px;
  min-width: 180px;
}

.filter-label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 6px;
}

.filter-select {
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  font-size: 14px;
  background: #fafafa;
}

.filter-select:focus {
  outline: none;
  border-color: #1e40af;
  background: #fff;
}

.filter-actions {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

.btn-ghost-sm,
.btn-solid-sm {
  padding: 10px 16px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  border: none;
}

.btn-ghost-sm {
  background: #f1f5f9;
  color: #334155;
  border: 1px solid #e2e8f0;
}

.btn-solid-sm {
  background: linear-gradient(135deg, #1e40af, #2563eb);
  color: #fff;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.aircraft-surface {
  margin-bottom: 24px;
}

.aircraft-caption {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 12px;
}

.aircraft-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.ac-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  font-size: 13px;
  font-weight: 600;
  color: #334155;
  cursor: pointer;
  transition: all 0.18s ease;
}

.ac-pill.active {
  background: #1e40af;
  color: #fff;
  border-color: #1e40af;
  box-shadow: 0 4px 12px rgba(30, 64, 175, 0.25);
}

.ac-plane {
  width: 16px;
  height: 16px;
  opacity: 0.85;
}

.ac-pill.active .ac-plane {
  filter: brightness(0) invert(1);
}

.loading-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 20px;
  color: #64748b;
  font-weight: 500;
}

.spinner {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 3px solid #e2e8f0;
  border-top-color: #1e40af;
  animation: spin 0.75s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.routes-wrap {
  margin-top: 8px;
}

.routes-head {
  margin-bottom: 16px;
}

.routes-title {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 900;
  color: #0f172a;
}

.routes-hint {
  margin: 0;
  font-size: 13px;
  color: #64748b;
}

.sort-strip {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 10px;
  margin-top: 14px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.sort-label {
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
}

.sort-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  background: #fff;
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s, box-shadow 0.15s;
}

.sort-chip:hover {
  border-color: #bfdbfe;
  color: #1e40af;
}

.sort-chip.active {
  border-color: #1e40af;
  color: #1e40af;
  background: #eff6ff;
  box-shadow: 0 2px 8px rgba(30, 64, 175, 0.15);
}

.sort-dir {
  font-size: 11px;
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
  border-radius: 16px;
  border: 1px dashed #cbd5e1;
  background: #f8fafc;
  color: #64748b;
  font-size: 14px;
}

.route-card {
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  background: #fff;
  margin-bottom: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.04);
  transition: box-shadow 0.2s;
}

.route-card.open {
  box-shadow: 0 8px 28px rgba(30, 64, 175, 0.1);
  border-color: #bfdbfe;
}

.route-card-toggle {
  width: 100%;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 16px;
  padding: 16px 18px;
  border: none;
  background: linear-gradient(180deg, #fafbfc 0%, #fff 100%);
  cursor: pointer;
  text-align: left;
  font: inherit;
}

.route-card-toggle:hover {
  background: #f8fafc;
}

.chevron {
  font-size: 20px;
  font-weight: 700;
  color: #94a3b8;
  width: 24px;
}

.route-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.route-name {
  font-size: 17px;
  font-weight: 800;
  color: #0f172a;
}

.route-type {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
}

.route-metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 24px;
  margin: 0;
}

.route-metric {
  display: grid;
  gap: 2px;
  min-width: 64px;
}

.route-metric dt {
  margin: 0;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #94a3b8;
}

.route-metric dd {
  margin: 0;
  font-size: 15px;
  font-weight: 800;
  color: #0f172a;
}

.route-metric.dim dd {
  color: #64748b;
  font-weight: 700;
}

.route-body {
  border-top: 1px solid #f1f5f9;
  background: #fafbfc;
}

.nested-scroll {
  overflow-x: auto;
  padding: 12px 14px 16px;
}

.detail-table {
  width: 100%;
  min-width: 720px;
  border-collapse: collapse;
  font-size: 13px;
}

.detail-table th,
.detail-table td {
  padding: 10px 12px;
  text-align: center;
  border-bottom: 1px solid #e2e8f0;
}

.detail-table th {
  background: #fff;
  font-size: 11px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: #64748b;
}

.detail-table tbody tr:hover td {
  background: rgba(239, 246, 255, 0.7);
}

.fold-enter-active,
.fold-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fold-enter-from,
.fold-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

@media (max-width: 720px) {
  .route-metrics {
    grid-column: 1 / -1;
    width: 100%;
  }

  .route-card-toggle {
    grid-template-columns: auto 1fr;
    grid-template-rows: auto auto;
  }

  .route-metrics {
    grid-column: 2;
  }
}
</style>
