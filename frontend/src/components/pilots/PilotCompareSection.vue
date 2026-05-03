<template>
  <section id="pilot-compare" class="pilot-compare-root">
    <div class="compare-hero">
      <div class="compare-hero-main">
        <p class="compare-kicker">Аналитика</p>
        <h2 class="compare-heading">Сравнительный анализ пилотов</h2>
        <p class="compare-lead">
          Отфильтруйте рейсы по маршруту, выберите пилотов в таблице и нажмите
          «Обновить графики» — сравните среднюю топливную эффективность и пунктуальность на одном экране.
        </p>
      </div>
      <div class="compare-hero-aside">
        <div class="pulse-badge">
          <span class="pulse-dot" aria-hidden="true"></span>
          {{ embedded ? 'Период — как на странице выше' : 'Выберите период в панели ниже' }}
        </div>
        <p class="compare-hint">Радар ниже доступен при выборе от двух пилотов с загруженными рейсами.</p>
      </div>
    </div>

    <div v-if="!embedded" class="compare-toolbar-wrap">
      <PeriodBar/>
    </div>

    <div class="filters-surface">
      <div class="filters-surface-inner">
        <div class="form-group-route">
          <span class="field-label">
            <span class="field-icon" aria-hidden="true">⇄</span>
            Маршрут
          </span>
          <filters ref="routeSelector" v-model="selectedRoutes"/>
        </div>
        <div class="filters-actions">
          <button type="button" class="btn-ghost" @click="clearFilters">Сбросить маршрут</button>
          <button type="button" class="btn-accent" @click="BtnPrimary" :disabled="loading">
            {{ loading ? 'Загрузка списка…' : 'Загрузить пилотов' }}
          </button>
        </div>
      </div>
    </div>

    <div class="viz-area">
      <div v-if="loading" class="loading-surface">
        <div class="loading-spinner"></div>
        <p>Загружаем данные…</p>
      </div>

      <template v-else>
        <div v-if="showRadar" class="radar-panel">
          <div class="panel-heading">
            <h3>Сводный профиль</h3>
            <span class="panel-tag">Radar</span>
          </div>
          <p class="panel-caption">Нормированные показатели для наглядного сравнения формы «работы» каждого КВС.</p>
          <div class="radar-canvas-wrap">
            <canvas ref="radarChart"></canvas>
          </div>
          <p class="radar-footnote">* «Объём» и «Рейсы» приведены к шкале 0–100% относительно группы выбранных пилотов.</p>
        </div>

        <div class="chart-grid two-columns">
          <div class="chart-panel chart-panel--elevated">
            <div class="panel-heading">
              <h3>Столбчатые диаграммы</h3>
              <span class="panel-tag">Detail</span>
            </div>
            <div class="charts-container">
              <div class="chart-wrapper">
                <div class="chart-mini-title">Эффективность</div>
                <div class="chart-inner">
                  <canvas ref="efficiencyChart"></canvas>
                </div>
                <div class="chart-controls">
                  <div class="zoom-controls">
                    <div class="slider-container">
                      <img src="../../assets/images/mdi_mountain_big.svg" alt="" class="zoom-icon zoom-out">
                      <input
                          type="range"
                          min="10"
                          max="100"
                          v-model="efficiencyZoomLevel"
                          class="zoom-slider"
                          @input="handleEfficiencyZoomChange"
                      >
                      <img src="../../assets/images/mdi_mountain.svg" alt="" class="zoom-icon zoom-in">
                    </div>
                  </div>
                  <div class="scroll-controls">
                    <div
                        class="scroll-bar"
                        @mousedown="startEfficiencyDrag"
                        @touchstart.prevent="startEfficiencyDrag"
                        @click="onEfficiencyBarClick"
                        ref="efficiencyScrollBar"
                    >
                      <div
                          class="scroll-thumb"
                          :style="efficiencyScrollThumbStyle"
                          @mousedown.stop="startEfficiencyDrag"
                          @touchstart.stop.prevent="startEfficiencyDrag"
                      ></div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="chart-wrapper">
                <div class="chart-mini-title chart-mini-title--amber">Пунктуальность</div>
                <div class="chart-inner">
                  <canvas ref="punctualityChart"></canvas>
                </div>
                <div class="chart-controls">
                  <div class="zoom-controls">
                    <div class="slider-container">
                      <img src="../../assets/images/mdi_mountain_big.svg" alt="" class="zoom-icon zoom-out">
                      <input
                          type="range"
                          min="10"
                          max="100"
                          v-model="punctualityZoomLevel"
                          class="zoom-slider"
                          @input="handlePunctualityZoomChange"
                      >
                      <img src="../../assets/images/mdi_mountain.svg" alt="" class="zoom-icon zoom-in">
                    </div>
                  </div>
                  <div class="scroll-controls">
                    <div
                        class="scroll-bar"
                        @mousedown="startPunctualityDrag"
                        @touchstart.prevent="startPunctualityDrag"
                        @click="onPunctualityBarClick"
                        ref="punctualityScrollBar"
                    >
                      <div
                          class="scroll-thumb"
                          :style="punctualityScrollThumbStyle"
                          @mousedown.stop="startPunctualityDrag"
                          @touchstart.stop.prevent="startPunctualityDrag"
                      ></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="chart-panel pilot-list-panel pilot-list-panel--glass">
            <div class="panel-heading">
              <h3>В фокусе сравнения</h3>
              <span class="panel-chip">{{ selectedPilotChips.length }} КВС</span>
            </div>
            <p class="panel-caption">
              По умолчанию берётся топ-3 по эффективности. Отметьте пилотов в таблице ниже и нажмите «Обновить графики».
            </p>
            <div class="pilot-chips">
              <div v-if="!selectedPilotChips.length" class="muted empty-chips">
                Выберите пилотов галочками и загрузите детализацию рейсов.
              </div>
              <router-link
                  v-for="(row, idx) in selectedPilotChips"
                  :key="row.tabNumber"
                  :to="{ path: `/pilot/${row.tabNumber}` }"
                  class="pilot-chip"
              >
                <span class="chip-swatch" :style="{ background: palette[idx % palette.length] }"></span>
                <div class="chip-body">
                  <span class="chip-id">{{ row.tabNumber }}</span>
                  <span class="chip-metrics">
                    Эффект. {{ row.avgEff == null ? '—' : row.avgEff.toFixed(1) + '%' }}
                    · Пункт. {{ row.avgPunct == null ? '—' : row.avgPunct.toFixed(0) + '%' }}
                  </span>
                  <span class="chip-sub">{{ row.flightCount }} рейс. · {{ Math.round(row.flightHours || 0) }} ч</span>
                </div>
              </router-link>
            </div>
          </div>
        </div>
      </template>
    </div>

    <section class="table-section">
      <div class="pilots-card pilots-card--table">
        <div class="pilots-card-head pilots-card-head--bar">
          <div>
            <h3 class="table-title">Список пилотов периода</h3>
            <p class="table-subtitle">Отметьте нужных КВС — строки можно сортировать по столбцам.</p>
          </div>
          <div class="pilots-card-actions">
            <button type="button" class="btn-ghost" @click="resetTableSelection">Сбросить выбор</button>
            <button type="button" class="btn-accent btn-accent--narrow" @click="buildSelected">Обновить графики</button>
          </div>
        </div>

        <div class="table-scroll">
          <table class="table table--modern">
            <thead>
            <tr style="border-right: 1.5px solid var(--line)">
              <th class="col-checkbox"></th>
              <th
                  v-for="header in headersRoute"
                  :key="header.key"
                  @click="sortByColumn(header.key)"
                  :class="['sortable-header', { active: sortConfig.key === header.key }]"
                  style="border-right: 1.5px solid var(--line); color: #4B5563; cursor: pointer; user-select: none;"
              >
                <div class="header-content">
                  <span>{{ header.title }}</span>
                  <div class="sort-indicators">
                    <svg
                        v-if="sortConfig.key === header.key && sortConfig.direction === 'asc'"
                        width="16"
                        height="16"
                        viewBox="0 0 20 20"
                        fill="currentColor"
                        class="sort-icon sort-asc"
                    >
                      <path d="M12 19V5M5 12l7-7 7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                            stroke-linejoin="round"/>
                    </svg>
                    <svg
                        v-else-if="sortConfig.key === header.key && sortConfig.direction === 'desc'"
                        width="16"
                        height="16"
                        viewBox="0 0 20 20"
                        fill="currentColor"
                        class="sort-icon sort-desc"
                    >
                      <path d="M12 5v14M19 12l-7 7-7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                            stroke-linejoin="round"/>
                    </svg>
                    <svg
                        v-else
                        width="16"
                        height="16"
                        viewBox="0 0 20 20"
                        fill="#9CA3AF"
                        class="sort-icon sort-default"
                    >
                      <path d="M7 15l5 5 5-5M7 9l5-5 5 5" stroke="currentColor" stroke-width="1.5"
                            stroke-linecap="round"/>
                    </svg>
                  </div>
                </div>
              </th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="p in pilots" :key="p.tabNumber">
              <td class="col-checkbox"><input type="checkbox"
                                              :checked="selectedPilotTabs.includes(String(p.tabNumber))"
                                              @change="e => togglePilotSelection(p.tabNumber, e.target.checked)"/>
              </td>
              <td>{{ p.tabNumber }}</td>
              <td>{{ p.flightCount || 0 }}</td>
              <td>{{ Math.round(p.flightHours || 0) }} ч</td>
              <td>{{ p.averageEfficiency ? p.averageEfficiency.toFixed(2) + '%' : '-' }}</td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>
  </section>
</template>

<script setup>
import {computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch} from 'vue'
import Chart from 'chart.js/auto'
import zoomPlugin from 'chartjs-plugin-zoom'
import Filters from '@/views/FlightComponents/Filters.vue'
import {config} from '@/config.js'
import {usePeriodStore} from '@/stores/periodStore.js'
import PeriodBar from '@/components/PeriodBar.vue'

Chart.register(zoomPlugin)

defineProps({
  embedded: {
    type: Boolean,
    default: false,
  },
})

const periodStore = usePeriodStore()
const loading = ref(false)
const pilots = ref([])
const topPilots = ref([])
const topPilotsDetails = ref({})

const headersRoute = [
  {title: "КВС", key: "tabNumber"},
  {title: "Рейсов", key: "flightCount"},
  {title: "Налёт", key: "flightHours"},
  {title: "Средняя эффективность", key: "averageEfficiency"}]

const sortConfig = reactive({
  key: null,
  direction: 'asc',
})
const sortCache = new Map()
const selectedRoutes = ref([])
const routeSelector = ref(null)
const selectedPilotTabs = ref([])
const radarChart = ref(null)
const radarChartInstance = ref(null)
const efficiencyChart = ref(null)
const punctualityChart = ref(null)
const efficiencyChartInstance = ref(null)
const punctualityChartInstance = ref(null)
const efficiencyZoomLevel = ref(100)
const punctualityZoomLevel = ref(100)
const efficiencyScrollBar = ref(null)
const punctualityScrollBar = ref(null)
const efficiencyChartVisibleRange = ref({min: 0, max: 10})
const punctualityChartVisibleRange = ref({min: 0, max: 10})
const efficiencyIsDragging = ref(false)
const punctualityIsDragging = ref(false)
const efficiencyIsManualZoom = ref(false)
const punctualityIsManualZoom = ref(false)
const efficiencyIsManualScroll = ref(false)
const punctualityIsManualScroll = ref(false)
const chartedPilotsList = ref([]) // Список пилотов, для которых построены графики
const isSyncingZoom = ref(false) // Флаг для предотвращения рекурсии в syncZoom

const palette = [
  '#3b82f6', '#8b5cf6', '#ec4899', '#f97316', '#10b981', '#06b6d4',
  '#6366f1', '#a855f7', '#f43f5e', '#f59e0b', '#22c55e', '#0ea5e9',
  '#4f46e5', '#7c3aed', '#db2777', '#ea580c', '#16a34a', '#0891b2',
  '#2563eb', '#9333ea', '#be185d', '#c2410c', '#15803d', '#0e7490',
  '#1d4ed8', '#7e22ce', '#9f1239', '#9a3412', '#166534', '#155e75',
  '#1e40af', '#6b21a8', '#831843', '#7c2d12', '#14532d', '#164e63',
  '#ef4444', '#f97316', '#fbbf24', '#84cc16', '#22d3ee', '#3b82f6',
  '#8b5cf6', '#d946ef', '#f472b6', '#fb7185', '#fb923c', '#fbbf24',
  '#a3e635', '#34d399', '#2dd4bf', '#38bdf8', '#60a5fa', '#818cf8',
  '#a78bfa', '#c084fc', '#e879f9', '#f472b6', '#fb7185', '#fda4af',
  '#fdba74', '#fcd34d', '#bef264', '#86efac', '#5eead4', '#7dd3fc',
  '#93c5fd', '#a5b4fc', '#c4b5fd', '#ddd6fe', '#e9d5ff', '#f3e8ff'
]


function formatDateForInput(date) {
  return date.toISOString().split('T')[0]
}

async function loadPilotsSummary() {
  const periodInfo = periodStore.periodInfo
  if (!periodInfo) return
  loading.value = true
  try {
    const startDate = periodInfo.startDate
    const endDate = new Date(startDate)
    endDate.setDate(periodInfo.days + parseInt(startDate.getDate()) - 1)
    const routesToUse = selectedRoutes.value || []

    const params = new URLSearchParams({
      startDate: formatDateForInput(startDate),
      endDate: formatDateForInput(endDate),
      routes: routesToUse
    })
    if (routesToUse && routesToUse.length) {
      routesToUse.forEach(r => params.append('routes', String(r).replace(/\s+/g, '')))
    }

    const url = `http://${config.apiBaseUrl}/api/pilots/statsByRoutes?${params.toString()}`
    const res = await fetch(url, {credentials: 'include'})
    if (!res.ok) throw new Error('Ошибка загрузки списка пилотов')
    const data = await res.json()
    pilots.value = data.pilots || []

    topPilots.value = [...pilots.value]
        .filter(p => typeof p.averageEfficiency === 'number')
        .slice(0, 3)

    if (topPilots.value.length > 0) {
      await loadTopPilotsDetails(topPilots.value.map(p => String(p.tabNumber)), routesToUse)
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadTopPilotsDetails(tabs, routes = null) {
  if (!tabs || !tabs.length) {
    topPilotsDetails.value = {}
    return
  }
  const periodInfo = periodStore.periodInfo
  if (!periodInfo) return
  loading.value = true
  try {
    const startDate = periodInfo.startDate
    const endDate = new Date(startDate)
    endDate.setDate(periodInfo.days + parseInt(startDate.getDate()) - 1)
    const params = new URLSearchParams({
      startDate: formatDateForInput(startDate),
      endDate: formatDateForInput(endDate)
    })
    tabs.forEach(t => params.append('tabs', String(t)))

    const routesToUse = Array.isArray(routes) ? routes : (selectedRoutes.value || [])
    if (routesToUse && routesToUse.length) {
      routesToUse.forEach(r => params.append('routes', String(r).replace(/\s+/g, '')))
    }

    const url = `http://${config.apiBaseUrl}/pilotStatistics/getPilotsData?${params.toString()}`
    const res = await fetch(url, {credentials: 'include'})
    if (!res.ok) throw new Error('Ошибка загрузки данных по пилотам')
    const data = await res.json()
    topPilotsDetails.value = data || {}
  } catch (e) {
    console.error(e)
    topPilotsDetails.value = {}
  } finally {
    loading.value = false
  }
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
  if (!sortConfig.key || pilots.value.length === 0) {
    return
  }

  const cacheKey = `${sortConfig.key}-${sortConfig.direction}`
  if (sortCache.has(cacheKey)) {
    pilots.value = [...sortCache.get(cacheKey)]
    return
  }

  const key = sortConfig.key
  const numericKeys = ['tabNumber', 'flightCount', 'flightHours', 'averageEfficiency']

  const sorted = [...pilots.value].sort((a, b) => {
    if (numericKeys.includes(key)) {
      const av = Number(a[key]) || 0
      const bv = Number(b[key]) || 0
      return sortConfig.direction === 'asc' ? av - bv : bv - av
    }
    const av = String(a[key] ?? '')
    const bv = String(b[key] ?? '')
    return sortConfig.direction === 'asc'
        ? av.localeCompare(bv, 'ru')
        : bv.localeCompare(av, 'ru')
  })

  sortCache.set(cacheKey, sorted)
  if (sortCache.size > 50) {
    sortCache.delete(sortCache.keys().next().value)
  }

  pilots.value = sorted
}

function avg(arr) {
  if (!Array.isArray(arr) || !arr.length) return 0
  const nums = arr.filter(n => typeof n === 'number')
  if (!nums.length) return 0
  return nums.reduce((s, v) => s + v, 0) / nums.length
}

const selectedPilotsList = computed(() => {
  const tabs = (selectedPilotTabs.value && selectedPilotTabs.value.length)
      ? selectedPilotTabs.value.map(t => String(t))
      : topPilots.value.map(p => String(p.tabNumber))

  const res = []
  for (const t of tabs) {
    const p = pilots.value.find(x => String(x.tabNumber) === String(t)) || topPilots.value.find(x => String(x.tabNumber) === String(t))
    if (p) res.push(p)
  }
  return res
})

const selectedPilotChips = computed(() => {
  const list = selectedPilotsList.value
  return list.map((p) => {
    const flights = topPilotsDetails.value[String(p.tabNumber)] || []
    const flightHours = flights.reduce((s, f) => s + Number(f.flightHours || 0), 0)
    const effs = flights
        .map(f => (typeof f.fuelEff === 'number' ? f.fuelEff : Number(f.fuelEff)))
        .filter(n => !isNaN(n))
    const puncts = flights
        .map(f => Number(f.calculatedResults?.punctuality || NaN))
        .filter(n => !isNaN(n))
    return {
      tabNumber: p.tabNumber,
      flightCount: flights.length,
      flightHours,
      avgEff: effs.length ? +avg(effs).toFixed(2) : null,
      avgPunct: puncts.length ? +(avg(puncts) * 100).toFixed(1) : null,
    }
  })
})

const showRadar = computed(() => chartedPilotsList.value.length >= 2)

function updateCharts() {
  const pilotsToUse = selectedPilotsList.value
  if (!pilotsToUse.length) {
    if (radarChartInstance.value) {
      radarChartInstance.value.destroy()
      radarChartInstance.value = null
    }
    return
  }

  // Проверяем, что canvas элементы готовы
  if (!efficiencyChart.value || !punctualityChart.value) {
    return
  }

  // Сохраняем список пилотов, для которых строятся графики
  chartedPilotsList.value = [...pilotsToUse]

  // Данные для эффективности
  const efficiencyLabels = pilotsToUse.map(p => String(p.tabNumber))
  const efficiencyData = pilotsToUse.map((p) => {
    const flights = topPilotsDetails.value[String(p.tabNumber)] || []
    const effs = flights.map(f => (typeof f.fuelEff === 'number' ? f.fuelEff : Number(f.fuelEff))).filter(n => !isNaN(n))
    return +avg(effs).toFixed(2)
  })
  const efficiencyColors = pilotsToUse.map((p, idx) => palette[idx % palette.length])

  // Данные для пунктуальности
  const punctualityLabels = pilotsToUse.map(p => String(p.tabNumber))
  const punctualityData = pilotsToUse.map((p) => {
    const flights = topPilotsDetails.value[String(p.tabNumber)] || []
    const puncts = flights.map(f => Number(f.calculatedResults?.punctuality || 0)).filter(n => !isNaN(n))
    return +(avg(puncts) * 100).toFixed(2)
  })
  const punctualityColors = pilotsToUse.map((p, idx) => palette[idx % palette.length])

  // Обновляем график эффективности
  if (efficiencyChartInstance.value) {
    efficiencyChartInstance.value.destroy()
  }
  if (efficiencyChart.value) {
    const ctx = efficiencyChart.value.getContext('2d')
    efficiencyChartInstance.value = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: efficiencyLabels,
        datasets: [{
          label: 'Эффективность (%)',
          data: efficiencyData,
          backgroundColor: efficiencyColors,
          borderColor: efficiencyColors,
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        barPercentage: 0.5,
        categoryPercentage: 0.7,
        plugins: {
          legend: {display: false},
          tooltip: {
            callbacks: {
              label(ctx) {
                return `Эффективность: ${ctx.formattedValue}%`
              }
            }
          },
          zoom: {
            zoom: {
              wheel: {enabled: true},
              pinch: {enabled: true},
              mode: 'x',
              onZoom: () => {
                if (!efficiencyIsManualZoom.value) {
                  syncEfficiencyControls()
                  syncZoom()
                }
              },
              onZoomComplete: () => {
                if (!efficiencyIsManualZoom.value) {
                  syncEfficiencyControls()
                  syncZoom()
                }
              }
            },
            pan: {
              enabled: true,
              mode: 'x',
              onPan: () => {
                if (!efficiencyIsManualZoom.value && !efficiencyIsManualScroll.value) {
                  syncEfficiencyControls()
                  syncZoom()
                }
              },
              onPanComplete: () => {
                if (!efficiencyIsManualZoom.value && !efficiencyIsManualScroll.value) {
                  syncEfficiencyControls()
                  syncZoom()
                }
              }
            },
            limits: {
              x: {min: 0, max: efficiencyLabels.length, minRange: 1}
            }
          }
        },
        scales: {
          x: {
            title: {display: true, text: 'Пилоты'},
            ticks: {color: 'var(--ink-soft)'},
            grid: {display: false}
          },
          y: {
            beginAtZero: false,
            title: {display: true, text: 'Эффективность (%)'},
            ticks: {color: 'var(--ink-soft)'},
            grid: {color: '#c7d2fe'},
            min: efficiencyData.length > 0 ? Math.max(0, Math.min(...efficiencyData) - 5) : 0,
            max: efficiencyData.length > 0 ? Math.max(...efficiencyData) + 5 : 100
          }
        }
      }
    })
  }

  // Обновляем график пунктуальности
  if (punctualityChartInstance.value) {
    punctualityChartInstance.value.destroy()
  }
  if (punctualityChart.value) {
    const ctx = punctualityChart.value.getContext('2d')
    punctualityChartInstance.value = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: punctualityLabels,
        datasets: [{
          label: 'Пунктуальность (%)',
          data: punctualityData,
          backgroundColor: punctualityColors,
          borderColor: punctualityColors,
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        barPercentage: 0.5,
        categoryPercentage: 0.7,
        plugins: {
          legend: {display: false},
          tooltip: {
            callbacks: {
              label(ctx) {
                return `Пунктуальность: ${ctx.formattedValue}%`
              }
            }
          },
          zoom: {
            zoom: {
              wheel: {enabled: true},
              pinch: {enabled: true},
              mode: 'x',
              onZoom: () => {
                if (!punctualityIsManualZoom.value) {
                  syncPunctualityControls()
                }
              },
              onZoomComplete: () => {
                if (!punctualityIsManualZoom.value) {
                  syncPunctualityControls()
                }
              }
            },
            pan: {
              enabled: true,
              mode: 'x',
              onPan: () => {
                if (!punctualityIsManualZoom.value && !punctualityIsManualScroll.value) {
                  syncPunctualityControls()
                }
              },
              onPanComplete: () => {
                if (!punctualityIsManualZoom.value && !punctualityIsManualScroll.value) {
                  syncPunctualityControls()
                }
              }
            },
            limits: {
              x: {min: 0, max: punctualityLabels.length, minRange: 1}
            }
          }
        },
        scales: {
          x: {
            title: {display: true, text: 'Пилоты'},
            ticks: {color: 'var(--ink-soft)'},
            grid: {display: false}
          },
          y: {
            beginAtZero: true,
            max: 100,
            title: {display: true, text: 'Пунктуальность (%)'},
            ticks: {color: 'var(--ink-soft)'},
            grid: {color: '#c7d2fe'}
          }
        }
      }
    })
  }

  syncEfficiencyControls()
  syncPunctualityControls()

  if (radarChartInstance.value) {
    radarChartInstance.value.destroy()
    radarChartInstance.value = null
  }

  if (pilotsToUse.length < 2) {
    return
  }

  const hrs = pilotsToUse.map((p) => {
    const flights = topPilotsDetails.value[String(p.tabNumber)] || []
    return flights.reduce((s, f) => s + Number(f.flightHours || 0), 0)
  })
  const counts = pilotsToUse.map((p) => (topPilotsDetails.value[String(p.tabNumber)] || []).length)
  const maxH = Math.max(...hrs, 1e-9)
  const maxC = Math.max(...counts, 1)
  const radarLabels = ['Эффективность, %', 'Пунктуальность, %', 'Объём полётов*', 'Рейсов на нагрузке*']

  const buildRadarDataset = () =>
      pilotsToUse.map((p, i) => {
        const border = palette[i % palette.length]
        return {
          label: String(p.tabNumber),
          data: [
            efficiencyData[i] ?? 0,
            punctualityData[i] ?? 0,
            Math.min(100, (hrs[i] / maxH) * 100),
            Math.min(100, (counts[i] / maxC) * 100),
          ],
          borderColor: border,
          backgroundColor: border + '2a',
          borderWidth: 2,
          pointBackgroundColor: border,
          pointBorderColor: '#fff',
          pointHoverRadius: 6,
          fill: true,
        }
      })

  nextTick(() => {
    if (!radarChart.value) return
    if (radarChartInstance.value) {
      radarChartInstance.value.destroy()
      radarChartInstance.value = null
    }

    radarChartInstance.value = new Chart(radarChart.value.getContext('2d'), {
      type: 'radar',
      data: {
        labels: radarLabels,
        datasets: buildRadarDataset(),
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 14,
              usePointStyle: true,
              font: {size: 12},
            },
          },
          tooltip: {
            callbacks: {
              label(ctx) {
                let suffix = ''
                if (ctx.dataIndex <= 1) suffix = '%'
                else if (ctx.dataIndex <= 3) suffix = ' (отн.)'
                return `${ctx.dataset.label}: ${ctx.formattedValue}${suffix}`
              },
            },
          },
        },
        scales: {
          r: {
            angleLines: {color: 'rgba(148,163,184,0.35)'},
            grid: {color: 'rgba(148,163,184,0.2)'},
            pointLabels: {
              font: {size: 11},
              color: '#475569',
            },
            suggestedMin: 0,
            suggestedMax: 100,
            ticks: {
              stepSize: 25,
              backdropColor: 'transparent',
            },
          },
        },
      },
    })
  })
}

// ========== ЭФФЕКТИВНОСТЬ ==========

const efficiencyScrollThumbStyle = computed(() => {
  const totalBars = Math.max(1, chartedPilotsList.value.length)
  const visibleBars = Math.max(1, efficiencyChartVisibleRange.value.max - efficiencyChartVisibleRange.value.min)
  const widthPercent = visibleBars >= totalBars ? 100 : (visibleBars / totalBars) * 100
  const leftPercent = totalBars === 0 ? 0 : (efficiencyChartVisibleRange.value.min / totalBars) * 100
  return {
    width: widthPercent + '%',
    left: leftPercent + '%',
    position: 'absolute'
  }
})

function handleEfficiencyZoomChange() {
  if (!efficiencyChartInstance.value) return
  efficiencyIsManualZoom.value = true

  const chart = efficiencyChartInstance.value
  if (chart && chart.scales && chart.scales.x) {
    const totalBars = Math.max(1, chartedPilotsList.value.length)
    const zoomFactor = efficiencyZoomLevel.value / 100
    const visibleBars = Math.max(1, Math.floor(totalBars * zoomFactor))

    const currentCenter = (chart.scales.x.min + chart.scales.x.max) / 2
    const newMin = Math.max(0, Math.floor(currentCenter - visibleBars / 2))
    const newMax = Math.min(totalBars, newMin + visibleBars)

    efficiencyChartVisibleRange.value = {min: newMin, max: newMax}

    if (typeof chart.zoomScale === 'function') {
      chart.zoomScale('x', {min: newMin, max: newMax})
    } else {
      chart.options.scales.x.min = newMin
      chart.options.scales.x.max = newMax
      chart.update('none')
    }
  }

  setTimeout(() => {
    efficiencyIsManualZoom.value = false
  }, 100)
}

function startEfficiencyDrag(e) {
  const pointer = (e.touches && e.touches[0]) ? e.touches[0] : e
  const scrollBar = efficiencyScrollBar.value
  if (!scrollBar) return

  const rect = scrollBar.getBoundingClientRect()
  const totalBars = Math.max(1, chartedPilotsList.value.length)
  const visibleBars = Math.max(1, efficiencyChartVisibleRange.value.max - efficiencyChartVisibleRange.value.min)
  const thumbWidthPx = Math.max(8, (visibleBars / totalBars) * rect.width)
  const currentThumbLeftPx = (efficiencyChartVisibleRange.value.min / totalBars) * rect.width
  const dragOffset = pointer.clientX - (rect.left + currentThumbLeftPx)

  efficiencyIsDragging.value = true
  efficiencyIsManualScroll.value = true

  function onDrag(e) {
    if (!efficiencyIsDragging.value) return
    const pointer = (e.touches && e.touches[0]) ? e.touches[0] : e
    if (e.touches) e.preventDefault()

    const rect = scrollBar.getBoundingClientRect()
    if (!rect) return

    let newLeftPx = pointer.clientX - rect.left - dragOffset
    newLeftPx = Math.max(0, Math.min(newLeftPx, rect.width - thumbWidthPx))
    const newLeftPercent = newLeftPx / rect.width
    let newMin = Math.round(newLeftPercent * totalBars)
    const maxScroll = Math.max(0, totalBars - visibleBars)
    newMin = Math.max(0, Math.min(newMin, maxScroll))

    efficiencyChartVisibleRange.value = {min: newMin, max: newMin + visibleBars}
    syncEfficiencyChartToRange()
  }

  function stopDrag() {
    efficiencyIsDragging.value = false
    efficiencyIsManualScroll.value = false
    document.removeEventListener('mousemove', onDrag)
    document.removeEventListener('mouseup', stopDrag)
    document.removeEventListener('touchmove', onDrag)
    document.removeEventListener('touchend', stopDrag)
  }

  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.addEventListener('touchmove', onDrag, {passive: false})
  document.addEventListener('touchend', stopDrag)
}

function onEfficiencyBarClick(e) {
  const pointer = (e.touches && e.touches[0]) ? e.touches[0] : e
  const rect = efficiencyScrollBar.value?.getBoundingClientRect()
  if (!rect) return

  const totalBars = Math.max(1, chartedPilotsList.value.length)
  const visibleBars = Math.max(1, efficiencyChartVisibleRange.value.max - efficiencyChartVisibleRange.value.min)
  const clickX = pointer.clientX - rect.left
  const thumbWidthPx = (visibleBars / totalBars) * rect.width
  let newLeftPx = clickX - thumbWidthPx / 2
  newLeftPx = Math.max(0, Math.min(newLeftPx, rect.width - thumbWidthPx))
  const newMin = Math.round((newLeftPx / rect.width) * totalBars)
  const maxScroll = Math.max(0, totalBars - visibleBars)
  const clampedMin = Math.max(0, Math.min(newMin, maxScroll))

  efficiencyChartVisibleRange.value = {min: clampedMin, max: clampedMin + visibleBars}
  syncEfficiencyChartToRange()
}

function syncEfficiencyChartToRange() {
  if (!efficiencyChartInstance.value || !efficiencyChartInstance.value.scales?.x) return
  const min = efficiencyChartVisibleRange.value.min
  const max = efficiencyChartVisibleRange.value.max

  efficiencyIsManualScroll.value = true
  try {
    if (typeof efficiencyChartInstance.value.zoomScale === 'function') {
      efficiencyChartInstance.value.zoomScale('x', {min, max})
    } else {
      efficiencyChartInstance.value.options.scales.x.min = min
      efficiencyChartInstance.value.options.scales.x.max = max
      efficiencyChartInstance.value.update('none')
    }
  } finally {
    setTimeout(() => {
      efficiencyIsManualScroll.value = false
    }, 80)
  }
}

function syncEfficiencyControls() {
  if (!efficiencyChartInstance.value || !efficiencyChartInstance.value.scales?.x || efficiencyIsManualZoom.value || efficiencyIsManualScroll.value) return

  const scale = efficiencyChartInstance.value.scales.x
  const totalBars = Math.max(1, chartedPilotsList.value.length)
  const visibleBars = scale.max - scale.min

  efficiencyZoomLevel.value = Math.min(100, Math.max(10, Math.round((totalBars / visibleBars) * 100)))

  efficiencyChartVisibleRange.value = {min: scale.min, max: scale.max}
}

// ========== ПУНКТУАЛЬНОСТЬ ==========

const punctualityScrollThumbStyle = computed(() => {
  const totalBars = Math.max(1, chartedPilotsList.value.length)
  const visibleBars = Math.max(1, punctualityChartVisibleRange.value.max - punctualityChartVisibleRange.value.min)
  const widthPercent = visibleBars >= totalBars ? 100 : (visibleBars / totalBars) * 100
  const leftPercent = totalBars === 0 ? 0 : (punctualityChartVisibleRange.value.min / totalBars) * 100
  return {
    width: widthPercent + '%',
    left: leftPercent + '%',
    position: 'absolute'
  }
})

function handlePunctualityZoomChange() {
  if (!punctualityChartInstance.value) return
  punctualityIsManualZoom.value = true

  const chart = punctualityChartInstance.value
  if (chart && chart.scales && chart.scales.x) {
    const totalBars = Math.max(1, chartedPilotsList.value.length)
    const zoomFactor = punctualityZoomLevel.value / 100
    const visibleBars = Math.max(1, Math.floor(totalBars * zoomFactor))

    const currentCenter = (chart.scales.x.min + chart.scales.x.max) / 2
    const newMin = Math.max(0, Math.floor(currentCenter - visibleBars / 2))
    const newMax = Math.min(totalBars, newMin + visibleBars)

    punctualityChartVisibleRange.value = {min: newMin, max: newMax}

    if (typeof chart.zoomScale === 'function') {
      chart.zoomScale('x', {min: newMin, max: newMax})
    } else {
      chart.options.scales.x.min = newMin
      chart.options.scales.x.max = newMax
      chart.update('none')
    }
  }

  setTimeout(() => {
    punctualityIsManualZoom.value = false
  }, 100)
}

function startPunctualityDrag(e) {
  const pointer = (e.touches && e.touches[0]) ? e.touches[0] : e
  const scrollBar = punctualityScrollBar.value
  if (!scrollBar) return

  const rect = scrollBar.getBoundingClientRect()
  const totalBars = Math.max(1, chartedPilotsList.value.length)
  const visibleBars = Math.max(1, punctualityChartVisibleRange.value.max - punctualityChartVisibleRange.value.min)
  const thumbWidthPx = Math.max(8, (visibleBars / totalBars) * rect.width)
  const currentThumbLeftPx = (punctualityChartVisibleRange.value.min / totalBars) * rect.width
  const dragOffset = pointer.clientX - (rect.left + currentThumbLeftPx)

  punctualityIsDragging.value = true
  punctualityIsManualScroll.value = true

  function onDrag(e) {
    if (!punctualityIsDragging.value) return
    const pointer = (e.touches && e.touches[0]) ? e.touches[0] : e
    if (e.touches) e.preventDefault()

    const rect = scrollBar.getBoundingClientRect()
    if (!rect) return

    let newLeftPx = pointer.clientX - rect.left - dragOffset
    newLeftPx = Math.max(0, Math.min(newLeftPx, rect.width - thumbWidthPx))
    const newLeftPercent = newLeftPx / rect.width
    let newMin = Math.round(newLeftPercent * totalBars)
    const maxScroll = Math.max(0, totalBars - visibleBars)
    newMin = Math.max(0, Math.min(newMin, maxScroll))

    punctualityChartVisibleRange.value = {min: newMin, max: newMin + visibleBars}
    syncPunctualityChartToRange()
  }

  function stopDrag() {
    punctualityIsDragging.value = false
    punctualityIsManualScroll.value = false
    document.removeEventListener('mousemove', onDrag)
    document.removeEventListener('mouseup', stopDrag)
    document.removeEventListener('touchmove', onDrag)
    document.removeEventListener('touchend', stopDrag)
  }

  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.addEventListener('touchmove', onDrag, {passive: false})
  document.addEventListener('touchend', stopDrag)
}

function onPunctualityBarClick(e) {
  const pointer = (e.touches && e.touches[0]) ? e.touches[0] : e
  const rect = punctualityScrollBar.value?.getBoundingClientRect()
  if (!rect) return

  const totalBars = Math.max(1, chartedPilotsList.value.length)
  const visibleBars = Math.max(1, punctualityChartVisibleRange.value.max - punctualityChartVisibleRange.value.min)
  const clickX = pointer.clientX - rect.left
  const thumbWidthPx = (visibleBars / totalBars) * rect.width
  let newLeftPx = clickX - thumbWidthPx / 2
  newLeftPx = Math.max(0, Math.min(newLeftPx, rect.width - thumbWidthPx))
  const newMin = Math.round((newLeftPx / rect.width) * totalBars)
  const maxScroll = Math.max(0, totalBars - visibleBars)
  const clampedMin = Math.max(0, Math.min(newMin, maxScroll))

  punctualityChartVisibleRange.value = {min: clampedMin, max: clampedMin + visibleBars}
  syncPunctualityChartToRange()
}

function syncPunctualityChartToRange() {
  if (!punctualityChartInstance.value || !punctualityChartInstance.value.scales?.x) return
  const min = punctualityChartVisibleRange.value.min
  const max = punctualityChartVisibleRange.value.max

  punctualityIsManualScroll.value = true
  try {
    if (typeof punctualityChartInstance.value.zoomScale === 'function') {
      punctualityChartInstance.value.zoomScale('x', {min, max})
    } else {
      punctualityChartInstance.value.options.scales.x.min = min
      punctualityChartInstance.value.options.scales.x.max = max
      punctualityChartInstance.value.update('none')
    }
  } finally {
    setTimeout(() => {
      punctualityIsManualScroll.value = false
    }, 80)
  }
}

function syncPunctualityControls() {
  if (!punctualityChartInstance.value || !punctualityChartInstance.value.scales?.x || punctualityIsManualZoom.value || punctualityIsManualScroll.value) return

  const scale = punctualityChartInstance.value.scales.x
  const totalBars = Math.max(1, chartedPilotsList.value.length)
  const visibleBars = scale.max - scale.min

  punctualityZoomLevel.value = Math.min(100, Math.max(10, Math.round((totalBars / visibleBars) * 100)))
  Math.max(0, totalBars - visibleBars);
  punctualityChartVisibleRange.value = {min: scale.min, max: scale.max}
}

function syncZoom() {
  if (!efficiencyChartInstance.value || !punctualityChartInstance.value ||
      efficiencyIsManualZoom.value || punctualityIsManualZoom.value ||
      isSyncingZoom.value) return

  const effScale = efficiencyChartInstance.value.scales?.x
  const punctScale = punctualityChartInstance.value.scales?.x

  if (effScale && punctScale) {
    isSyncingZoom.value = true
    punctualityIsManualZoom.value = true
    try {
      punctScale.min = effScale.min
      punctScale.max = effScale.max
      punctualityChartInstance.value.update('none')
    } finally {
      setTimeout(() => {
        isSyncingZoom.value = false
        punctualityIsManualZoom.value = false
      }, 100)
    }
  }
}

function togglePilotSelection(tab, checked) {
  const s = String(tab)
  if (checked) {
    if (!selectedPilotTabs.value.includes(s)) selectedPilotTabs.value.push(s)
  } else {
    selectedPilotTabs.value = selectedPilotTabs.value.filter(t => String(t) !== s)
  }
}

async function buildSelected() {
  const tabs = selectedPilotTabs.value.length ? selectedPilotTabs.value : topPilots.value.map(p => String(p.tabNumber))
  let routesSource = selectedRoutes.value
  if (routeSelector.value && routeSelector.value && Array.isArray(routeSelector.value.selectedRoutes)) {
    routesSource = routeSelector.value.selectedRoutes
  }
  const cleanedRoutes = (routesSource || []).map(r => String(r).replace(/\s+/g, ''))
  console.debug('PilotCompare.buildSelected: tabs=', tabs, 'routesSource=', routesSource, 'cleanedRoutes=', cleanedRoutes)
  await loadTopPilotsDetails(tabs, cleanedRoutes)
}

function resetTableSelection() {
  selectedPilotTabs.value = []
  topPilotsDetails.value = {}
}


async function BtnPrimary(event) {
  if (event && event.preventDefault) event.preventDefault()
  await loadPilotsSummary()
}

async function clearFilters() {
  if (routeSelector.value && routeSelector.value && routeSelector.value.$el && typeof routeSelector.value.clearSelection === 'function') {
    try {
      routeSelector.value.clearSelection()
      console.debug('PilotCompare: called child.clearSelection()')
      await nextTick()
    } catch (err) {
      console.warn('routeSelector.clearSelection failed', err)
      selectedRoutes.value = []
    }
  } else {
    selectedRoutes.value = []
    console.debug('PilotCompare: child.clearSelection not available, cleared parent selectedRoutes')
    await nextTick()
  }

  selectedPilotTabs.value = []

  await loadPilotsSummary()
  console.debug('PilotCompare: clearFilters done', {selectedRoutes: selectedRoutes.value})
}

watch(topPilotsDetails, async () => {
  await nextTick()
  setTimeout(() => {
    if (efficiencyChart.value && punctualityChart.value) {
      updateCharts()
    } else {
      setTimeout(() => {
        if (efficiencyChart.value && punctualityChart.value) {
          updateCharts()
        }
      }, 200)
    }
  }, 100)
}, {deep: true})

watch(
    () => periodStore.periodChanged,
    (periodChanged) => {
      if (periodChanged) {
        loadPilotsSummary()
        periodStore.resetPeriodChanged()
      }
    },
    {immediate: false}
)

onMounted(async () => {
  await periodStore.initialize()
  await loadPilotsSummary()
})

onBeforeUnmount(() => {
  if (radarChartInstance.value) {
    radarChartInstance.value.destroy()
    radarChartInstance.value = null
  }
  if (efficiencyChartInstance.value) {
    efficiencyChartInstance.value.destroy()
    efficiencyChartInstance.value = null
  }
  if (punctualityChartInstance.value) {
    punctualityChartInstance.value.destroy()
    punctualityChartInstance.value = null
  }
})

</script>

<style scoped>
.pilot-compare-root {
  margin-top: 32px;
  padding-top: 28px;
  border-top: 1px solid rgba(148, 163, 184, 0.45);
}

.compare-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(220px, 320px);
  gap: 24px;
  align-items: start;
  margin-bottom: 20px;
  padding: 24px 26px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #eff6ff 45%, #f1f5f9 100%);
  border: 1px solid rgba(226, 232, 240, 0.9);
  box-shadow: 0 12px 40px rgba(15, 23, 42, 0.06);
}

.compare-kicker {
  margin: 0 0 6px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #64748b;
}

.compare-heading {
  margin: 0 0 10px;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: #0f172a;
}

.compare-lead {
  margin: 0;
  font-size: 14px;
  line-height: 1.55;
  color: #475569;
  max-width: 52rem;
}

.compare-hero-aside {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pulse-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #1e3a8a;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(191, 219, 254, 0.9);
}

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  animation: pulse-dot 2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.55;
    transform: scale(0.92);
  }
}

.compare-hint {
  margin: 0;
  font-size: 12px;
  line-height: 1.45;
  color: #64748b;
}

.filters-surface {
  margin-bottom: 20px;
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: #fff;
  box-shadow: 0 4px 18px rgba(15, 23, 42, 0.04);
}

.filters-surface-inner {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 16px 24px;
  padding: 18px 20px;
}

.form-group-route {
  flex: 1 1 260px;
  min-width: 200px;
}

.field-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 10px;
}

.field-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 8px;
  background: linear-gradient(135deg, #dbeafe, #eff6ff);
  color: #1d4ed8;
  font-size: 13px;
}

.filters-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.btn-ghost {
  padding: 10px 16px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  font-size: 13px;
  font-weight: 600;
  color: #334155;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.btn-ghost:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.btn-accent {
  padding: 10px 20px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #1e40af 0%, #2563eb 100%);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.35);
  transition: transform 0.15s ease, box-shadow 0.2s ease;
}

.btn-accent:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(37, 99, 235, 0.4);
}

.btn-accent:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.btn-accent--narrow {
  padding-left: 16px;
  padding-right: 16px;
}

.viz-area {
  min-height: 120px;
}

.loading-surface {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 48px 20px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fafc, #ffffff);
  border: 1px dashed #cbd5e1;
  color: #64748b;
  font-weight: 500;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 3px solid #e2e8f0;
  border-top-color: #1e40af;
  animation: spin-compare 0.75s linear infinite;
}

@keyframes spin-compare {
  to {
    transform: rotate(360deg);
  }
}

.radar-panel {
  margin-bottom: 20px;
  padding: 20px 22px 16px;
  border-radius: 16px;
  background: radial-gradient(1200px 400px at 10% -20%, rgba(99, 102, 241, 0.12), transparent 55%),
  linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid rgba(226, 232, 240, 0.95);
  box-shadow: 0 10px 32px rgba(15, 23, 42, 0.07);
}

.panel-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.panel-heading h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 800;
  color: #0f172a;
}

.panel-tag {
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #6366f1;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(129, 140, 248, 0.15);
  border: 1px solid rgba(165, 180, 252, 0.5);
}

.panel-chip {
  font-size: 12px;
  font-weight: 700;
  color: #0369a1;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(14, 165, 233, 0.12);
  border: 1px solid rgba(125, 211, 252, 0.65);
}

.panel-caption {
  margin: 4px 0 14px;
  font-size: 13px;
  line-height: 1.5;
  color: #64748b;
}

.radar-canvas-wrap {
  height: 340px;
  max-width: 520px;
  margin: 0 auto;
}

.radar-footnote {
  margin: 8px 0 0;
  font-size: 11px;
  color: #94a3b8;
}

.chart-panel--elevated {
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: #fff;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.05);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chart-mini-title {
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #1e40af;
  margin: 8px 0 4px 4px;
}

.chart-mini-title--amber {
  color: #c2410c;
}

.chart-grid.two-columns {
  display: grid;
  grid-template-columns: 7fr 3fr;
  gap: 20px;
}

.pilot-list-panel--glass {
  background: linear-gradient(160deg, #ffffff 0%, #f1f5f9 100%);
  padding: 16px !important;
  display: flex;
  flex-direction: column;
  gap: 8px;
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.05);
}

.pilot-chips {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 4px 0 8px;
  max-height: 420px;
  overflow-y: auto;
}

.empty-chips {
  padding: 12px;
  border-radius: 12px;
  background: rgba(248, 250, 252, 0.9);
  border: 1px dashed #cbd5e1;
  text-align: center;
}

.pilot-chip {
  display: flex;
  gap: 12px;
  align-items: stretch;
  padding: 12px 14px;
  border-radius: 12px;
  text-decoration: none;
  color: inherit;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(226, 232, 240, 0.95);
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.04);
  transition: transform 0.15s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.pilot-chip:hover {
  transform: translateY(-2px);
  border-color: rgba(99, 102, 241, 0.45);
  box-shadow: 0 8px 22px rgba(79, 70, 229, 0.12);
}

.chip-swatch {
  flex-shrink: 0;
  width: 6px;
  border-radius: 999px;
  align-self: stretch;
}

.chip-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.chip-id {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.chip-metrics {
  font-size: 12px;
  font-weight: 600;
  color: #334155;
}

.chip-sub {
  font-size: 11px;
  color: #64748b;
}

.table-section {
  margin-top: 28px;
}

.pilots-card--table {
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  overflow: hidden;
  background: #fff;
  box-shadow: 0 8px 26px rgba(15, 23, 42, 0.05);
}

.pilots-card-head--bar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding: 18px 20px;
  margin: 0;
  border-bottom: 1px solid #f1f5f9;
  background: linear-gradient(180deg, #fafbfc 0%, #ffffff 100%);
}

.table-title {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
}

.table-subtitle {
  margin: 0;
  font-size: 12px;
  color: #64748b;
  line-height: 1.4;
}

.pilots-card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.table-scroll {
  max-height: 460px;
  overflow: auto;
  padding: 0;
}

.table--modern thead th {
  position: sticky;
  top: 0;
  z-index: 1;
  background: #f8fafc;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: #64748b;
  border-bottom: 2px solid #e2e8f0;
}

.table--modern tbody tr:hover {
  background: rgba(239, 246, 255, 0.7);
}

.table--modern td,
.table--modern th {
  border-color: #f1f5f9 !important;
}

@media (max-width: 960px) {
  .compare-hero {
    grid-template-columns: 1fr;
  }

  .chart-grid.two-columns {
    grid-template-columns: 1fr;
  }

  .charts-container {
    grid-template-columns: 1fr !important;
  }
}

.chip-label {
  font-size: 12px;
  color: var(--muted)
}

.btn.apply {
  background: var(--brand);
  color: #fff;
  border-radius: 8px;
  padding: 8px 14px;
  border: none
}

.btn {
  background: #f3f4f6;
  border-radius: 8px;
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  cursor: pointer;
}

.charts-section {
  margin-top: 12px;
  border-radius: 10px;
}

.chart-panel:not(.chart-panel--elevated) {
  background: var(--bg);
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.charts-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.chart-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}


.chart-inner {
  height: 320px;
  position: relative;
}

.chart-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 16px;
  margin-top: 8px;
}

.zoom-controls {
  width: 100%;
  max-width: 400px;
  display: flex;
  justify-content: center;
}

.slider-container {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.zoom-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.zoom-slider {
  flex: 1;
  height: 3px;
  -webkit-appearance: none;
  appearance: none;
  background: #cbd5e1;
  outline: none;
  border-radius: 3px;
  cursor: pointer;
}

.zoom-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 28px;
  height: 4px;
  background: #1e40af;
  border-radius: 2px;
  border: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  cursor: grab;
  transition: background 0.2s ease;
}

.zoom-slider::-webkit-slider-thumb:active {
  cursor: grabbing;
  background: #1e3a8a;
}

.zoom-slider::-moz-range-thumb {
  width: 28px;
  height: 4px;
  background: #1e40af;
  border-radius: 2px;
  border: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  cursor: grab;
}

.zoom-slider::-moz-range-thumb:active {
  cursor: grabbing;
  background: #1e3a8a;
}

.chart-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 16px;
  margin-top: -30px;
}

.zoom-controls {
  width: 100%;
  max-width: 400px;
  display: flex;
  justify-content: center;
  margin: 0 auto;
}

.slider-container {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.zoom-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.zoom-slider {
  flex: 1;
  height: 3px;
  -webkit-appearance: none;
  appearance: none;
  background: #cbd5e1;
  outline: none;
  border-radius: 3px;
  cursor: pointer;
}

.zoom-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 28px;
  height: 4px;
  background: #1e40af;
  border-radius: 2px;
  border: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  cursor: grab;
  transition: background 0.2s ease;
}

.zoom-slider::-webkit-slider-thumb:active {
  cursor: grabbing;
  background: #1e3a8a;
}

.zoom-slider::-moz-range-thumb {
  width: 28px;
  height: 4px;
  background: #1e40af;
  border-radius: 2px;
  border: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  cursor: grab;
}

.zoom-slider::-moz-range-thumb:active {
  cursor: grabbing;
  background: #1e3a8a;
}

.scroll-controls {
  width: 100%;
  margin-top: 4px;
  padding: 4px 0;
}

.scroll-bar {
  position: relative;
  width: 100%;
  height: 2px;
  background: #e2e8f0;
  border-radius: 4px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6);
  user-select: none;
  cursor: pointer;
}

.scroll-thumb {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  height: 2px;
  background: #1e40af;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(30, 64, 175, 0.15);
  cursor: grab;
  transition: left 0.08s linear, width 0.12s ease;
}

.scroll-thumb:active {
  cursor: grabbing;
  box-shadow: 0 6px 12px rgba(30, 64, 175, 0.22);
}

.card {
  margin-top: 16px;
  background: #fff;
  border-radius: 10px;
  padding: 16px
}

.table {
  width: 100%;
  border-collapse: collapse
}

th, td {
  border: 1px solid #e5e7eb;
  padding: 10px;
  text-align: left;
  font-size: 14px
}

.pilot-tab {
  font-weight: 700;
  font-size: 18px;
  color: var(--ink);
}

.pilot-meta {
  font-size: 12px;
  color: var(--muted);
  font-weight: 400;
}

.pilot-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px 4px;
  max-height: 420px; /* keep right column from stretching page */
  overflow-y: auto;
}

.pilot-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12px;
  padding: 8px 6px;
  border-radius: 6px;
  background: transparent;
  border: none;
}

.pilot-color-indicator {
  width: 16px;
  height: 16px;
  border-radius: 3px;
  flex-shrink: 0;
}

.pilot-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.chart-inner {
  height: 360px;
  max-height: 360px;
}

.muted {
  color: var(--muted);
}

.pilots-card {
  margin-top: 16px
}

.filter-toolbar[data-v-ddbe4eef] {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding: 10px;
}

.col-checkbox {
  width: 36px;
  max-width: 36px;
  padding: 6px 4px;
  text-align: center;
}

.first-row .btn {
  height: 38px
}

.filter-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  width: 100%;
}

.btn-primary {
  background: #1e40af;
  color: white;
  border: none;
  padding: 10px 13px;
  border-radius: 8px;
  cursor: pointer;
  transition: 0.2s;
  font-weight: 700;
  font-size: 14px;
}

.btn-primary:hover {
  background: #1d4ed8;
}


.header-content {
  display: flex;
  align-items: center;

  gap: 8px;
}

.sort-indicators {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.sort-icon {
  transition: transform 0.2s ease, color 0.2s ease;
}

.sort-icon.sort-default {
  opacity: 0.5;
}

.sortable-header:hover .sort-icon.sort-default {
  opacity: 0.8;
}

</style>