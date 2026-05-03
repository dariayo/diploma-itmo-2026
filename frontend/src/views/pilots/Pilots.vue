<template>
  <div class="layout">
    <main class="content">
     <div class="h1" style="font-weight: 700; font-size: 32px; line-height: 100%;">
                Управление пилотами
              </div>
      <div class="page-top">
         <PeriodBar style="margin-top: calc(7px + 1.17%);"/>

          <div class="metric-card">
            <h5>Средняя эффективность</h5>
            <div class="value">{{ averageEfficiency.toFixed(1) }}%</div>
          </div>
          <div class="metric-card">
            <h5>Общий налет</h5>
            <div class="value">{{ formatHours(totalFlightHours) }}</div>
          </div>
          <div class="metric-card">
            <h5>Количество пилотов</h5>
            <div class="value">{{ pilots.length }}</div>
          </div>
          <div class="metric-card">
            <h5>Всего рейсов</h5>
            <div class="value">{{ totalFlights }}</div>
          </div>

      </div>

      <div class="filter-toolbar">
        <div class="filter-left">
          <div class="limit-dropdown" @click.stop>
            <button
                class="limit-btn"
                :class="{ active: showLimitMenu }"
                @click="toggleLimitMenu"
            >
              Топ-{{ selectedLimit }}
              <span class="caret" :class="{ up: showLimitMenu }">▼</span>
            </button>

            <ul v-if="showLimitMenu" class="limit-menu">
              <li v-for="opt in limitOptions" :key="opt">
                <button class="limit-item" @click="chooseLimit(opt)">
                  Топ-{{ opt }}
                </button>
              </li>
            </ul>
          </div>

        </div>

        <div class="rating-type-toggle" style="margin-left: auto;">
          <div class="toggle-switch">
            <button
                class="toggle-btn"
                :class="{ active: ratingType === 'efficiency' }"
                @click="setRatingType('efficiency')"
            >
              По топливу
            </button>
            <button
                class="toggle-btn"
                :class="{ active: ratingType === 'rating' }"
                @click="setRatingType('rating')"
            >
              По рейтингу
            </button>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading-indicator">
        <i class="fa fa-spinner fa-spin" style="font-size:24px"></i>
        <p>Загрузка данных...</p>
      </div>

      <div class="lower-grid">
        <div class="chart-card">
          <button type="button" class="chart-section-title-btn" @click="scrollToCompare">
            Эффективность пилотов
            <span class="chart-section-chevron" aria-hidden="true">↓</span>
            <span class="chart-section-hint">К сравнительному анализу ниже</span>
          </button>
          <div style=" position: relative;">
            <canvas ref="chartCanvas"></canvas>
          </div>

          <div class="chart-controls">
            <div class="control-row">
              <div class="control-label">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="2">
                  <circle cx="11" cy="11" r="8"/>
                  <path d="M21 21l-4.35-4.35"/>
                  <path d="M11 8v6M8 11h6"/>
                </svg>
                <span>Масштаб</span>
              </div>
              <div class="zoom-controls">
                <div class="slider-container">
                  <img src="../../assets/images/mdi_mountain_big.svg" alt="Уменьшить" class="zoom-icon zoom-out">
                  <input
                      type="range"
                      min="10"
                      max="100"
                      v-model="zoomLevel"
                      class="zoom-slider"
                      @input="handleZoomChange"
                  >
                  <img src="../../assets/images/mdi_mountain.svg" alt="Увеличить" class="zoom-icon zoom-in">
                </div>
              </div>
            </div>

            <div class="control-row">
              <div class="control-label">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#64748b" stroke-width="2">
                  <path d="M5 12h14"/>
                  <path d="M5 12l4-4M5 12l4 4"/>
                  <path d="M19 12l-4-4M19 12l-4 4"/>
                </svg>
                <span>Положение</span>
              </div>
              <div class="scroll-controls">
                <div
                    class="scroll-bar"
                    @mousedown="startDrag"
                    @touchstart.prevent="startDrag"
                    @click="onBarClick"
                    ref="scrollBar"
                >
                  <div
                      class="scroll-thumb"
                      :style="scrollThumbStyle"
                      @mousedown.stop="startDrag"
                      @touchstart.stop.prevent="startDrag"
                  ></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="rating-card">
          <div class="rating-header">
            <h4 style="font-weight: 700; font-size: 24px; line-height: 14px; display: inline-block;">
              Пилоты
            </h4>

            <div class="rating-toggle-header">
              <div class="toggle-switch">
                <button
                    class="toggle-btn"
                    :class="{ active: ratingType === 'efficiency' }"
                    @click="setRatingType('efficiency')"
                >
                  По топливу
                </button>
                <button
                    class="toggle-btn"
                    :class="{ active: ratingType === 'rating' }"
                    @click="setRatingType('rating')"
                >
                  По рейтингу
                </button>
              </div>
            </div>
          </div>

          <div class="tab-buttons" style="display:flex;gap:8px;margin-bottom:10px;justify-content: center;">
            <button :class="['best-btn', { active: ratingTab === 'best' }]"
                    @click="ratingTab = 'best'; resetRatingScroll();">
              {{ 'Высокий' }}
            </button>
            <button :class="['best-btn', { active: ratingTab === 'worst' }]"
                    @click="ratingTab = 'worst'; resetRatingScroll();">
              {{ 'Низкий' }}
            </button>
          </div>

          <div class="rating-legend" v-if="ratingTab === 'best'">
            <div class="legend-item">
              <span class="legend-dot gold"></span>
              <span>1 место</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot silver"></span>
              <span>2 место</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot bronze"></span>
              <span>3 место</span>
            </div>
          </div>
          <div class="rating-legend" v-else>
            <div class="legend-item">
              <span class="legend-dot worst"></span>
              <span>{{ ratingType === 'efficiency' ? 'Низкая эффективность' : 'Низкий рейтинг' }}</span>
            </div>
          </div>

          <div class="rating-container">
            <div class="pilot-list-container" ref="pilotListContainer">
              <ul class="pilot-list">
                <li v-for="(pilot, index) in displayedPilots"
                    :key="pilot.tabNumber"
                    class="pilot card rating-item"
                    :class="ratingItemClass(index)"
                    @click="$router.push({path : `/pilot/${pilot.tabNumber}`})"
                >
                  <div class="rating-left">
                    <div class="pilot-id">{{ pilot.tabNumber }}</div>
                    <div class="pilot-sub">
                      Рейсов: {{ pilot.flightCount || 0 }} • Налет: {{ formatHours2(pilot.flightHours || 0) }}
                      <div v-if="ratingType === 'rating' && (pilot.pilotRating || 0) > 0"
                           style="font-size: 11px; color: #666; margin-top: 2px;">
                        Сложность: {{ ((pilot.averageDifficulty || 0) * 100).toFixed(1) }}%
                      </div>
                    </div>
                  </div>
                  <div class="rating-right">
                    <div class="percent-pill"
                         :class="percentPillClass(index)">
                      {{ getBadgeValue(pilot) }}
                    </div>
                  </div>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <PilotCompareSection embedded/>
    </main>
  </div>
</template>

<script>
import Chart from 'chart.js/auto';
import zoomPlugin from 'chartjs-plugin-zoom';
import config from '@/config.js';
import {usePeriodStore} from '@/stores/periodStore.js'
import PeriodBar from "@/components/PeriodBar.vue";
import PilotCompareSection from '@/components/pilots/PilotCompareSection.vue';

Chart.register(zoomPlugin);

export default {
  name: 'Pilots',
  components: {PeriodBar, PilotCompareSection},
  setup() {
    const periodStore = usePeriodStore()
    return {periodStore}
  },
  data() {
    return {
      loading: false,
      pilots: [],
      selectedLimit: 50,
      showLimitMenu: false,
      averageEfficiency: 0,
      totalFlightHours: 0,
      periodText: '',
      chart: null,
      ratingTab: 'best',
      zoomLevel: 100,
      scrollPosition: 0,
      chartStartIndex: 0,
      chartVisibleCount: 10,
      limitOptions: [5, 10, 20, 50],
      configs: {
        cacheKey: 'pilotStats'
      },
      chartVisibleRange: {min: 0, max: 10},
      isDragging: false,
      _scrollBarRect: null,
      _thumbWidthPx: 0,
      _dragOffset: 0,
      _totalBars: 1,
      isManualScroll: false,
      ratingScrollPosition: 0,
      ratingVisibleCount: 5,
      ratingType: 'rating',
      currentChartPilots: []
    };
  },
  computed: {
    periods() {
      return [
        {value: 'month', label: 'Месяц'},
        {value: 'lastMonth', label: 'Прошлый месяц'},
        {value: 'quarter', label: 'Квартал'},
        {value: 'year', label: 'Год'},
        {value: 'custom', label: this.periodStore.periodText},
      ];
    },

    maxRatingScroll() {
      return Math.max(0, this.displayedPilots.length - this.ratingVisibleCount);
    },

    canScrollUp() {
      return this.ratingScrollPosition > 0;
    },

    canScrollDown() {
      return this.ratingScrollPosition < this.maxRatingScroll;
    },
    totalFlights() {
      return this.pilots.reduce((sum, pilot) => sum + pilot.flightCount, 0);
    },

    displayedPilots() {
      if (!this.sortedPilots.length) return [];

      if (this.ratingTab === 'worst') {
        const startIndex = Math.max(0, this.sortedPilots.length - this.selectedLimit);
        return [...this.sortedPilots].slice(startIndex).reverse();
      } else {
        return this.sortedPilots.slice(0, this.selectedLimit);
      }
    },

    chartPilots() {
      return this.displayedPilots;
    },

    sortedPilots() {
      if (!this.pilots.length) return [];

      const pilotsCopy = [...this.pilots];

      if (this.ratingType === 'efficiency') {
        return pilotsCopy.sort((a, b) => b.averageEfficiency - a.averageEfficiency);
      } else {
        return pilotsCopy.sort((a, b) => {
          const ratingA = (a.pilotRating || 0);
          const ratingB = (b.pilotRating || 0);
          return ratingB - ratingA;
        });
      }
    },

    scrollThumbStyle() {
      const totalBars = Math.max(1, this.chartPilots.length);
      const visibleBars = Math.max(1, (this.chartVisibleRange.max - this.chartVisibleRange.min) || totalBars);
      const widthPercent = visibleBars >= totalBars ? 100 : (visibleBars / totalBars) * 100;
      const leftPercent = totalBars === 0 ? 0 : (this.chartVisibleRange.min / totalBars) * 100;

      return {
        width: widthPercent + '%',
        left: leftPercent + '%',
        position: 'absolute'
      };
    }
  },
  watch: {
    'periodStore.periodChanged': {
      handler(periodChanged) {
        if (periodChanged) {
          this.loadPilotData();
          this.periodStore.resetPeriodChanged();
        }
      },
      immediate: true
    },

    ratingType() {
      this.updateChart();
    },

    ratingTab() {
      this.$nextTick(() => {
        this.updateChart();
      });
    },

    selectedLimit() {
      this.$nextTick(() => {
        this.updateChart();
      });
    }
  },

  mounted() {
    this.initializeDates();
    this.initCustomScrollbar();
    const savedRatingType = localStorage.getItem('pilotRatingType');
    if (savedRatingType && (savedRatingType === 'efficiency' || savedRatingType === 'rating')) {
      this.ratingType = savedRatingType;
    }
    if (this.periodStore.periodInfo) {
      this.loadPilotData();
    }
    this.periodStore.initialize().then(() => {
    if (this.periodStore.hasCachedData('pilots')) {
      const cachedData = this.periodStore.getCachedData('pilots')
      this.pilots = cachedData.pilots
      this.averageEfficiency = cachedData.averageEfficiency
      this.totalFlightHours = cachedData.totalFlightHours
      this.updateChart()
    } else if (this.periodStore.periodInfo) {
      this.loadPilotData();
    }
    }).catch(error => {
      console.error('Ошибка инициализации store:', error);
    });

    window.addEventListener('beforeunload', this.saveDataToLocalStorage);
    document.addEventListener('click', this.closeLimitMenu);

    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible') {
        if (this.periodStore.periodChanged) {
          this.loadPilotData();
          this.periodStore.resetPeriodChanged();
        }
      }
    });
    this.$nextTick(() => {
      const ratingContainer = this.$el.querySelector('.rating-container');
      if (ratingContainer) {
        ratingContainer.addEventListener('wheel', this.handleRatingWheel);
      }
      if (this.$route.hash === '#pilot-compare') {
        setTimeout(() => this.scrollToCompare(), 150);
      }
    });
  },
  beforeUnmount() {
    if (this.chart) {
      this.chart.destroy();
    }
    window.removeEventListener('beforeunload', this.saveDataToLocalStorage);
    document.removeEventListener('click', this.closeLimitMenu);
    const ratingContainer = this.$el.querySelector('.rating-container');
    if (ratingContainer) {
      ratingContainer.removeEventListener('wheel', this.handleRatingWheel);
    }
  },
  methods: {
    setRatingType(type) {
      this.ratingType = type;
      localStorage.setItem('pilotRatingType', type);
    },

    getBadgeValue(pilot) {
      if (this.ratingType === 'efficiency') {
        return `${(pilot.averageEfficiency || 0).toFixed(1)}%`;
      } else {
        return (pilot.pilotRating || 0).toFixed(1);
      }
    },

    percentPillClass(index) {
      if (this.ratingTab === 'worst') return 'worst-neutral';
      if (index === 0) return 'gold';
      if (index === 1) return 'silver';
      if (index === 2) return 'bronze';
      return 'neutral';
    },

    ratingItemClass(index) {
      if (this.ratingTab === 'worst') return 'worst-regular';
      if (index === 0) return 'first';
      if (index === 1) return 'second';
      if (index === 2) return 'third';
      return '';
    },

    initCustomScrollbar() {
      this.$nextTick(() => {
        const container = this.$refs.pilotListContainer;
        if (container) {
          container.addEventListener('wheel', this.handlePilotListScroll);
        }
      });
    },

    handlePilotListScroll(event) {
      const container = this.$refs.pilotListContainer;
      if (container) {
        container.scrollTop += event.deltaY;
        event.preventDefault();
      }
    },

    resetRatingScroll() {
      this.$nextTick(() => {
        const container = this.$refs.pilotListContainer;
        if (container) {
          container.scrollTop = 0;
        }
      });
    },

    handleRatingWheel(event) {
      event.preventDefault();

      if (event.deltaY < 0) {
        this.scrollRatingUp();
      } else {
        this.scrollRatingDown();
      }
    },

    scrollRatingUp() {
      if (this.canScrollUp) {
        this.ratingScrollPosition = Math.max(0, this.ratingScrollPosition - 1);
      }
    },

    scrollRatingDown() {
      if (this.canScrollDown) {
        this.ratingScrollPosition = Math.min(
            this.maxRatingScroll,
            this.ratingScrollPosition + 1
        );
      }
    },

    startDrag(e) {
      const pointer = (e.touches && e.touches[0]) ? e.touches[0] : e;
      const scrollBar = this.$refs.scrollBar;
      if (!scrollBar) return;

      this._scrollBarRect = scrollBar.getBoundingClientRect();
      this._totalBars = Math.max(1, this.chartPilots.length);
      const visibleBars = Math.max(1, this.chartVisibleRange.max - this.chartVisibleRange.min);
      this._thumbWidthPx = Math.max(8, (visibleBars / this._totalBars) * this._scrollBarRect.width);

      const currentThumbLeftPx = (this.chartVisibleRange.min / this._totalBars) * this._scrollBarRect.width;
      this._dragOffset = pointer.clientX - (this._scrollBarRect.left + currentThumbLeftPx);
      this.isDragging = true;

      document.addEventListener('mousemove', this.onDrag);
      document.addEventListener('mouseup', this.stopDrag);
      document.addEventListener('touchmove', this.onDrag, {passive: false});
      document.addEventListener('touchend', this.stopDrag);
    },

    onDrag(e) {
      if (!this.isDragging) return;
      const pointer = (e.touches && e.touches[0]) ? e.touches[0] : e;
      if (e.touches) e.preventDefault();

      const rect = this._scrollBarRect || (this.$refs.scrollBar && this.$refs.scrollBar.getBoundingClientRect());
      if (!rect) return;

      const totalBars = Math.max(1, this._totalBars || this.chartPilots.length);
      const visibleBars = Math.max(1, this.chartVisibleRange.max - this.chartVisibleRange.min);

      let newLeftPx = pointer.clientX - rect.left - this._dragOffset;
      newLeftPx = Math.max(0, Math.min(newLeftPx, rect.width - this._thumbWidthPx));

      const newLeftPercent = newLeftPx / rect.width;

      let newMin = Math.round(newLeftPercent * totalBars);
      const maxScroll = Math.max(0, totalBars - visibleBars);
      newMin = Math.max(0, Math.min(newMin, maxScroll));

      this.chartVisibleRange.min = newMin;
      this.chartVisibleRange.max = newMin + visibleBars;
      this.syncChartToRange();
    },

    stopDrag() {
      this.isDragging = false;
      document.removeEventListener('mousemove', this.onDrag);
      document.removeEventListener('mouseup', this.stopDrag);
      document.removeEventListener('touchmove', this.onDrag);
      document.removeEventListener('touchend', this.stopDrag);
    },

    onBarClick(e) {
      const pointer = (e.touches && e.touches[0]) ? e.touches[0] : e;
      const rect = this.$refs.scrollBar.getBoundingClientRect();
      const totalBars = Math.max(1, this.chartPilots.length);
      const visibleBars = Math.max(1, this.chartVisibleRange.max - this.chartVisibleRange.min);

      const clickX = pointer.clientX - rect.left;
      const thumbWidthPx = (visibleBars / totalBars) * rect.width;
      let newLeftPx = clickX - thumbWidthPx / 2;
      newLeftPx = Math.max(0, Math.min(newLeftPx, rect.width - thumbWidthPx));

      const newMin = Math.round((newLeftPx / rect.width) * totalBars);
      const maxScroll = Math.max(0, totalBars - visibleBars);
      const clampedMin = Math.max(0, Math.min(newMin, maxScroll));

      this.chartVisibleRange.min = clampedMin;
      this.chartVisibleRange.max = clampedMin + visibleBars;
      this.syncChartToRange();
    },

    syncChartToRange() {
      if (!this.chart || !this.chart.scales || !this.chart.scales.x) return;
      const min = this.chartVisibleRange.min;
      const max = this.chartVisibleRange.max;

      this.isManualScroll = true;
      try {
        if (typeof this.chart.zoomScale === 'function') {
          this.chart.zoomScale('x', {min, max});
        } else {
          this.chart.options.scales.x.min = min;
          this.chart.options.scales.x.max = max;
          this.chart.update('none');
        }
      } finally {
        setTimeout(() => {
          this.isManualScroll = false;
        }, 80);
      }
    },

    toggleLimitMenu() {
      this.showLimitMenu = !this.showLimitMenu;
    },

    closeLimitMenu() {
      this.showLimitMenu = false;
    },

    chooseLimit(limit) {
      this.selectedLimit = limit;
      this.showLimitMenu = false;
      this.updateChart();
    },

    initializeDates() {
      const cachedData = localStorage.getItem(this.configs.cacheKey);
      if (cachedData) {
        try {
          const data = JSON.parse(cachedData);
          this.pilots = data.pilots || [];
          this.averageEfficiency = data.averageEfficiency || 0;
          this.totalFlightHours = data.totalFlightHours || 0;
        } catch (e) {
          console.error('Error parsing cached data:', e);
        }
      }
    },

    formatDateForInput(date) {
      return date.toISOString().split('T')[0];
    },

    formatHours(hours) {
      if (hours >= 1000) return (hours).toFixed(1) + 'ч';
      return hours.toFixed(1) + ' ч';
    },

    formatHours2(hours) {
      if (hours >= 1000) return (hours).toFixed(1) + 'k ч';
      return hours + ' ч';
    },

    setPeriod(period) {
      this.periodStore.setPeriod(period);
      if (period !== 'custom') {
        this.loadPilotData();
      }
    },

    applyCustomPeriod() {
      this.periodStore.applyCustomPeriod();
      this.loadPilotData();
    },

    handleZoomChange() {
      this.isManualZoom = true;

      if (this.chart && this.chart.scales.x) {
        const totalBars = this.chartPilots.length;

        const zoomFactor = this.zoomLevel / 100;
        const visibleBars = Math.max(1, Math.floor(totalBars * zoomFactor));

        const currentCenter = (this.chart.scales.x.min + this.chart.scales.x.max) / 2;
        const newMin = Math.max(0, Math.floor(currentCenter - visibleBars / 2));
        const newMax = Math.min(totalBars, newMin + visibleBars);

        this.chart.zoomScale('x', {
          min: newMin,
          max: newMax
        });

        this.chartVisibleRange = {min: newMin, max: newMax};
      }

      setTimeout(() => {
        this.isManualZoom = false;
      }, 100);
    },

    async loadPilotData() {
      if (this.periodStore.hasCachedData('pilots')) {
        const cachedData = this.periodStore.getCachedData('pilots')
        this.pilots = cachedData.pilots
        this.averageEfficiency = cachedData.averageEfficiency
        this.totalFlightHours = cachedData.totalFlightHours
        this.updateChart()
        return
      }

      const periodInfo = this.periodStore.periodInfo;
      if (!periodInfo) {
        return;
      }

      this.loading = true;

      try {
        let url = `http://${config.apiBaseUrl}/api/pilots/statsNew`;

        let startDate, endDate;

        const periodInfo = this.periodStore.periodInfo;
        if (!periodInfo) {
          this.loading = false;
          return;
        }

        startDate = periodInfo.startDate;
        endDate = new Date(startDate);
        endDate.setDate(periodInfo.days + parseInt(startDate.getDate()) - 1);
        const params = new URLSearchParams({
          startDate: this.formatDateForInput(startDate),
          endDate: this.formatDateForInput(endDate)
        });

        url += `?${params.toString()}`;

        const response = await fetch(url, {credentials: "include"});
        if (!response.ok) throw new Error('Ошибка загрузки данных');

        const data = await response.json();
        this.pilots = data.pilots || [];
        this.averageEfficiency = data.averageEfficiency || 0;
        this.totalFlightHours = data.totalFlightHours || 0;

        this.updateChart();
        this.updatePeriodDisplay(startDate, endDate);

        this.saveDataToLocalStorage();
        this.periodStore.setCachedData('pilots', {
          pilots: this.pilots,
          averageEfficiency: this.averageEfficiency,
          totalFlightHours: this.totalFlightHours
        });

      } catch (error) {
        console.error('Error loading pilot data:', error);
      } finally {
        this.loading = false;
      }
    },

    updateChart() {
      if (this.chart) {
        this.chart.destroy();
      }

      if (!this.$refs.chartCanvas) return;
      const ctx = this.$refs.chartCanvas.getContext('2d');
      if (!ctx) return;

      const chartPilots = this.chartPilots;
      const labels = chartPilots.map(p => p.tabNumber.toString());

      const dataValues = chartPilots.map(p => {
        if (this.ratingType === 'efficiency') {
          return p.averageEfficiency || 0;
        } else {
          return p.pilotRating || 0;
        }
      }).filter(v => typeof v === 'number' && !isNaN(v));

      if (dataValues.length === 0 || labels.length === 0) {
        return;
      }

      const backgroundColors = chartPilots.map((pilot, idx) => {
        return '#1E40AF';
      });

      const minValue = Math.max(0, Math.min(...dataValues) * 0.9);
      const maxValue = Math.max(...dataValues) * 1.1;

      const yAxisTitle = this.ratingType === 'efficiency' ? 'Эффективность (%)' : 'Рейтинг';

      this.chart = new Chart(ctx, {
        type: 'bar',
        data: {
          labels,
          datasets: [{
            label: yAxisTitle,
            data: dataValues,
            backgroundColor: backgroundColors,
            borderColor: backgroundColors.map(c => c.replace('0.7', '1')),
            borderWidth: 1,
            barPercentage: 0.6,
            categoryPercentage: 0.8,
            hoverBackgroundColor: '#EB2A2A',
            hoverBorderColor: '#EB2A2A',
            hoverBorderWidth: 2
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          onClick: (event, elements) => {
            if (elements.length > 0) {
              const index = elements[0].index;
              const pilot = this.chartPilots[index];
              if (pilot && pilot.tabNumber) {
                this.$router.push({path: `/pilot/${pilot.tabNumber}`});
              }
            }
          },
          plugins: {
            legend: {display: false},
            tooltip: {
              enabled: false
            },
            zoom: {
              zoom: {
                wheel: {
                  enabled: true,
                },
                pinch: {
                  enabled: true,
                },
                mode: 'x',
                scaleMode: 'x',
                onZoom: ({chart}) => {
                  if (!this.isManualZoom && !this.isManualScroll) {
                    this.syncControlsWithChart();
                  }
                },
                onZoomComplete: ({chart}) => {
                  if (!this.isManualZoom && !this.isManualScroll) {
                    this.syncControlsWithChart();
                  }
                }
              },
              pan: {
                enabled: true,
                mode: 'x',
                onPan: ({chart}) => {
                  if (!this.isManualZoom && !this.isManualScroll) {
                    this.syncControlsWithChart();
                  }
                },
                onPanComplete: ({chart}) => {
                  if (!this.isManualZoom && !this.isManualScroll) {
                    this.syncControlsWithChart();
                  }
                }
              },
              limits: {
                x: {min: 0, max: this.chartPilots.length, minRange: 1}
              }
            }
          },
          scales: {
            x: {
              title: {display: true, text: 'Пилоты'},
              ticks: {maxRotation: 45, minRotation: 0, color: '#1e3a8a'},
              grid: {display: false},
              min: 0,
            },
            y: {
              beginAtZero: false,
              min: minValue,
              max: maxValue,
              title: {display: true, text: yAxisTitle},
              grid: {color: '#c7d2fe', drawBorder: false},
              ticks: {color: '#1e3a8a'}
            }
          },
          animation: {
            duration: 0
          }
        },
        plugins: [{
          id: 'chartAreaBackground'
        }]
      });
      this.syncControlsWithChart();

      this.chart.options.plugins.zoom.zoom.onZoomComplete = ({chart}) => {
        if (!this.isManualZoom && !this.isManualScroll) {
          this.syncControlsWithChart();
        }
      };

      this.chart.options.plugins.zoom.pan.onPanComplete = ({chart}) => {
        if (!this.isManualZoom && !this.isManualScroll) {
          this.syncControlsWithChart();
        }
      };
    },

    syncControlsWithChart() {
      if (!this.chart || !this.chart.scales.x || this.isManualZoom || this.isManualScroll) return;

      const scale = this.chart.scales.x;
      const totalBars = this.chartPilots.length;
      const visibleBars = scale.max - scale.min;

      const zoomPercent = Math.min(100, Math.max(10, Math.round((totalBars / visibleBars) * 100)));
      this.zoomLevel = zoomPercent;

      const maxScroll = Math.max(0, totalBars - visibleBars);
      let scrollPercent = 0;

      if (maxScroll > 0) {
        scrollPercent = Math.round((scale.min / maxScroll) * 100);
      }

      this.scrollPosition = Math.max(0, Math.min(100, scrollPercent));

      this.chartVisibleRange = {min: scale.min, max: scale.max};
    },

    renderExternalTooltip(context) {
      const {chart, tooltip} = context;
      let tooltipEl = chart.canvas.parentNode.querySelector('.chartjs-tooltip');

      if (!tooltipEl) {
        tooltipEl = document.createElement('div');
        tooltipEl.className = 'chartjs-tooltip';
        tooltipEl.innerHTML = '<div class="tooltip-content"></div><div class="tooltip-arrow"></div>';
        chart.canvas.parentNode.style.position = 'relative';
        chart.canvas.parentNode.appendChild(tooltipEl);
      }

      if (tooltip.opacity === 0) {
        tooltipEl.style.opacity = 0;
        return;
      }

      const dataIndex = tooltip.dataPoints && tooltip.dataPoints[0] ? tooltip.dataPoints[0].dataIndex : null;
      if (dataIndex == null) return;
      const pilot = this.chartPilots[dataIndex];
      const value = tooltip.dataPoints[0].formattedValue;

      let html = `
        <div class="tooltip-title">${pilot.tabNumber}</div>
        <div class="tooltip-line" style="color: #FF9393">
          <span style="color: #FF9393">${this.ratingType === 'efficiency' ? 'Эффективность:' : 'Рейтинг:'}</span>
          <b>${value}${this.ratingType === 'efficiency' ? '%' : ''}</b>
        </div>`;

      if (this.ratingType === 'rating') {
        html += `
          <div class="tooltip-line"><span>Эффективность:</span> <b>${(pilot.averageEfficiency || 0).toFixed(1)}%</b></div>
          <div class="tooltip-line"><span>Ср. сложность:</span> <b>${((pilot.averageDifficulty || 0) * 100).toFixed(1)}%</b></div>`;
      }

      html += `
        <div class="tooltip-line"><span>Налёт:</span> <b>${this.formatHours(pilot.flightHours || 0)}</b></div>
        <div class="tooltip-line"><span>Рейсов:</span> <b>${pilot.flightCount || 0}</b></div>
      `;

      const content = tooltipEl.querySelector('.tooltip-content');
      content.innerHTML = html;

      const canvasRect = chart.canvas.getBoundingClientRect();
      const container = chart.canvas.parentNode;
      const containerRect = container.getBoundingClientRect();

      const relX = tooltip.caretX + (canvasRect.left - containerRect.left);
      const relY = tooltip.caretY + (canvasRect.top - containerRect.top);

      tooltipEl.style.opacity = 1;

      const tooltipWidth = tooltipEl.offsetWidth || 160;
      const margin = 8;
      const centeredX = relX;
      const minX = tooltipWidth / 2 + margin;
      const maxX = containerRect.width - tooltipWidth / 2 - margin;
      const clampedX = Math.max(minX, Math.min(centeredX, maxX));

      tooltipEl.style.left = clampedX + 'px';
      tooltipEl.style.top = (relY - (tooltipEl.offsetHeight || 0) - 12) + 'px';
    },

    updatePeriodDisplay(startDate, endDate) {
      if (!startDate || !endDate) return;

      const formatDate = (date) => {
        return date.toLocaleDateString('ru-RU', {
          day: 'numeric',
          month: 'long',
          year: 'numeric'
        });
      };

      switch (this.periodStore.selectedPeriod) {
        case 'month':
          this.periodText = `Текущий месяц: ${formatDate(startDate)} - ${formatDate(endDate)}`;
          break;
        case 'lastMonth':
          this.periodText = `Прошлый месяц: ${formatDate(startDate)} - ${formatDate(endDate)}`;
          break;
        case 'quarter':
          this.periodText = `Квартал: ${formatDate(startDate)} - ${formatDate(endDate)}`;
          break;
        case 'year':
          this.periodText = `Год: ${formatDate(startDate)} - ${formatDate(endDate)}`;
          break;
        case 'custom':
          this.periodText = `Период: ${formatDate(startDate)} - ${formatDate(endDate)}`;
          break;
        default:
          this.periodText = 'Период не выбран';
      }
    },

    saveDataToLocalStorage() {
      const dataToSave = {
        pilots: this.pilots,
        averageEfficiency: this.averageEfficiency,
        totalFlightHours: this.totalFlightHours,
        timestamp: new Date().toISOString(),
        period: this.periodStore.selectedPeriod
      };
      localStorage.setItem(this.configs.cacheKey, JSON.stringify(dataToSave));
    },

    scrollToCompare() {
      const el = document.getElementById('pilot-compare')
      el?.scrollIntoView({behavior: 'smooth', block: 'start'})
    },

    goToCompare() {
      this.scrollToCompare()
    }
  }
};
</script>

<style scoped>

.rating-type-toggle {
  display: flex;
  align-items: center;
}

.toggle-switch {
  display: flex;
  gap: 4px;
  background: #f3f4f6;
  border-radius: 20px;
  padding: 4px;
  border: 1px solid #e5e7eb;
}

.toggle-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 16px;
  background: transparent;
  color: #6b7280;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.toggle-btn:hover {
  background: #e5e7eb;
  color: #374151;
}

.toggle-btn.active {
  background: #1e40af;
  color: white;
  box-shadow: 0 1px 3px rgba(30, 64, 175, 0.2);
}

@media (max-width: 768px) {
  .filter-toolbar {
    flex-direction: column;
    gap: 12px;
  }

  .rating-type-toggle {
    margin-left: 0;
    width: 100%;
    justify-content: center;
  }

  .toggle-switch {
    width: 100%;
    justify-content: center;
  }

  .toggle-btn {
    flex: 1;
    text-align: center;
  }
}

.pilot-sub {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

.percent-pill {
  padding: 6px 10px;
  border-radius: 999px;
  font-weight: 700;
  font-size: 12px;
}

.lower-grid {
  display: flex; /* делаем контейнер flex */
  gap: 16px; /* расстояние между блоками */
  align-items: stretch; /* растягиваем оба блока по высоте */
}

.lower-grid .chart-card {
  flex: 7; /* график занимает 80% ширины */
  display: flex;
  flex-direction: column;
  max-height: 500px;
  border: 1px solid #ccc;
  padding: 16px;
  box-sizing: border-box;
}

.lower-grid .rating-card {
  flex: 3; /* рейтинг занимает 20% ширины */
  display: flex;
  flex-direction: column;
  max-height: 500px;
  border: 1px solid #ccc;
  padding: 16px;
  box-sizing: border-box;
}


/* Контейнер для графика и списка пилотов */
.lower-grid .chart-card > div:first-child,
.lower-grid .rating-container,
.pilot-list-container {
  flex: 1; /* занимает всё доступное пространство */
  overflow-y: auto; /* прокрутка, если контента больше */
}

.loading-indicator {
  text-align: center;
  padding: 20px;
}

/* Панель фильтра по периоду */
.filter-toolbar {
  display: flex;
  align-items: center; /* выравниваем всё по центру */
  gap: 10px;
  flex-wrap: wrap;
}

.custom-period-row input,
.custom-period-row button {
  height: 40px; /* добавлено */
  box-sizing: border-box;
}

.custom-period-row button {
  background: var(--brand);
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 0 12px; /* выровнено по высоте */
  cursor: pointer;
  transition: background 0.2s ease;
}

.custom-period-row button:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.custom-period-row button:hover:not(:disabled) {
  background: #15327a;
}


/* ограничение топ-рейтинга */
.limit-dropdown {
  position: relative;
}

.limit-btn {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--border-radius);
  padding: 6px 10px;
  cursor: pointer;
}

.limit-btn.active {
  border-color: var(--brand);
  box-shadow: 0 0 0 2px rgba(30, 64, 175, 0.25);
}

.caret {
  font-size: 10px;
  transition: transform 0.2s;
}

.caret.up {
  transform: rotate(180deg);
}

.limit-menu {
  list-style: none;
  margin: 0;
  padding: 0;
  position: absolute;
  top: 100%;
  left: 0;
  background: #fff;
  border: 1px solid #1e40af;
  border-radius: 8px;
  width: 97px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.limit-item {
  width: 100%;
  height: 32px;
  display: flex;
  align-items: center;
  padding: 8px 8px 8px 17px;
  font-size: 14px;
  color: #1e40af;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  box-sizing: border-box;
}

.best-btn {
  width: 150px;
  height: 32px;
  border-radius: 8px;
  padding: 8px;
  gap: 10px;
  background: none;
  border: none;
  font-size: 14px;
  cursor: pointer;
  color: #111827;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.best-btn.active {
  color: #1E40AF;
}

/* Легенда рейтинга */
.rating-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
  padding: 8px 12px;
  border-radius: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #64748b;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-dot.gold {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  box-shadow: 0 2px 4px rgba(251, 191, 36, 0.4);
}

.legend-dot.silver {
  background: linear-gradient(135deg, #e5e7eb 0%, #9ca3af 100%);
  box-shadow: 0 2px 4px rgba(156, 163, 175, 0.4);
}

.legend-dot.bronze {
  background: linear-gradient(135deg, #fdba74 0%, #ea580c 100%);
  box-shadow: 0 2px 4px rgba(234, 88, 12, 0.4);
}

.legend-dot.worst {
  background: linear-gradient(135deg, #fca5a5 0%, #ef4444 100%);
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.4);
}

/* Контролы графика с подписями */
.control-row {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
  max-width: 500px;
}

.control-label {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 110px;
  font-size: 12px;
  color: #64748b;
  white-space: nowrap;
}

.control-label svg {
  flex-shrink: 0;
}

.control-row .zoom-controls,
.control-row .scroll-controls {
  flex: 1;
  margin-top: 0;
  padding: 0;
}

.zoom-controls {
  margin-top: 16px;
  padding: 12px;
  width: 550px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.scroll-controls {
  margin-top: 8px;
  padding: 12px;

  border-radius: 8px;
}

.slider-container {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.slider-container label {
  font-size: 14px;
  color: #64748b;
  min-width: 70px;
}

.zoom-slider, .scroll-slider {
  top: 10px;
  flex: 1;
  height: 3px;
  -webkit-appearance: none;
  appearance: none;
  background: #cbd5e1;
  outline: none;
  border-radius: 3px;
}

.zoom-slider::-webkit-slider-thumb, .scroll-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #1e40af;
  cursor: pointer;
}

.zoom-slider, .scroll-slider {
  transition: all 0.2s ease;
}

.zoom-slider:active, .scroll-slider:active {
  cursor: grabbing;
}

.tooltip-title {
  font-weight: 700;
  margin-bottom: 8px;
  color: #1e293b;
}

.tooltip-line {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 4px;
  font-size: 14px;
}

.tooltip-line span {
  color: #64748b;
}

.tooltip-line b {
  color: #1e293b;
}

/* Общая дорожка */
.zoom-slider, .scroll-slider {
  flex: 1;
  height: 3px; /* высота трека */
  -webkit-appearance: none;
  appearance: none;
  background: #e2e8f0; /* светло-серый фон */
  border-radius: 3px;
  cursor: pointer;
}

/* Дорожка для Firefox */
.zoom-slider::-moz-range-track,
.scroll-slider::-moz-range-track {
  width: 100%;
  height: 3px;
  background: #e2e8f0;
  border-radius: 3px;
  border: none;
}

/* Горизонтальная полоска-бегунок для Chrome/Safari/Edge */
.zoom-slider::-webkit-slider-thumb,
.scroll-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 28px; /* длина полоски */
  height: 4px; /* толщина полоски */
  background: #1e40af; /* цвет бегунка */
  border-radius: 2px;
  border: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  cursor: grab;
  transition: background 0.2s ease;
  margin-top: -2px; /* чтобы выровнять по центру трека */
}

/* Для Firefox */
.zoom-slider::-moz-range-thumb,
.scroll-slider::-moz-range-thumb {
  width: 28px;
  height: 4px;
  background: #1e40af;
  border-radius: 2px;
  border: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  cursor: grab;
}

/* Hover/Active эффекты */
.zoom-slider:hover::-webkit-slider-thumb,
.scroll-slider:hover::-webkit-slider-thumb {
  background: #1e3a8a;
}

.zoom-slider:active::-webkit-slider-thumb,
.scroll-slider:active::-webkit-slider-thumb {
  background: #1e3a8a;
  cursor: grabbing;
}

.zoom-slider:hover::-moz-range-thumb,
.scroll-slider:hover::-moz-range-thumb {
  background: #1e3a8a;
}

.zoom-slider:active::-moz-range-thumb,
.scroll-slider:active::-moz-range-thumb {
  background: #1e3a8a;
  cursor: grabbing;
}

.scroll-bar {
  position: relative;
  width: 100%;
  height: 3px;
  background: #e2e8f0; /* фон трека */
  border-radius: 3px;
  margin-top: 10px;
  cursor: pointer;
}

.scroll-thumb {
  position: absolute;
  top: 0;
  height: 3px;
  background: #1e40af; /* цвет полоски */
  border-radius: 3px;
  cursor: grab;
  transition: width 0.2s ease, left 0.1s ease;
}

.scroll-thumb:active {
  cursor: grabbing;
}

.scroll-bar {
  position: relative;
  width: 100%;
  height: 3px;
  background: #e2e8f0; /* светлая дорожка */
  border-radius: 6px;
  margin-top: 0;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6);
  user-select: none;
}

.scroll-thumb {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  height: 3px;
  background: #1e40af;
  border-radius: 6px;
  box-shadow: 0 2px 6px rgba(30, 64, 175, 0.18);
  cursor: grab;
  transition: left 0.08s linear, width 0.12s ease;
}

/* визуально при нажатии */
.scroll-thumb:active {
  cursor: grabbing;
  box-shadow: 0 6px 12px rgba(30, 64, 175, 0.22);
}

.chart-card {
  padding: 16px;
  border-radius: 10px;
  border: 1px solid rgba(6, 95, 70, 0.03);
  position: relative; /* Добавьте это */
}

.chart-controls { /* Светло-синий фон как у графика */
  border-radius: 8px;
  padding: 16px;
  margin-top: -30px;

}

.rating-container {
  position: relative;
  height: 415px; /* Фиксированная высота контейнера */
  display: flex;
  flex-direction: column;
}

.pilot-list-container {
  flex: 1;
  overflow-y: auto; /* Включаем вертикальный скролл */
  overflow-x: hidden;
  padding-right: 8px; /* Место для скроллбара */

  /* Кастомный скроллбар для Webkit браузеров (Chrome, Safari, Edge) */

  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f5f9;
    border-radius: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: #cbd5e1;
    border-radius: 4px;
    transition: background 0.3s ease;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: #94a3b8;
  }

  /* Для Firefox */
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 #f1f5f9;
}

.pilot-list {
  list-style: none;
  padding: 0;
  margin: 0;
}


.chart-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin-top: -20px;
}

.zoom-controls {
  width: 100%;
  max-width: 400px; /* Ограничиваем максимальную ширину */
  display: flex;
  justify-content: center;
}

.slider-container {
  width: 100%;
  display: flex;
  justify-content: center;
}

.zoom-slider {
  width: 100%;
  max-width: 300px; /* Фиксированная ширина для центрирования */
  height: 3px;
  -webkit-appearance: none;
  appearance: none;
  background: #cbd5e1;
  outline: none;
  border-radius: 3px;
}

.scroll-controls {
  width: 100%;
  max-width: 900px; /* Такая же ширина как у zoom-controls */
  display: flex;
  justify-content: center;
}

.filter-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  width: 100%;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 5px;
  flex-wrap: wrap;
  flex: 1;
}

.periods-group {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.period-btn {
  padding: 6px 12px !important;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: #fff;
  color: var(--ink);
  font-size: 12px !important;
  cursor: pointer;
  transition: all 0.2s ease;
  height: 32px !important;
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
}

.period-btn.active {
  background: var(--brand);
  color: #fff;
  border-color: var(--brand);
  box-shadow: 0 0 0 2px rgba(30, 64, 175, 0.2);
}

.limit-dropdown {
  position: relative;
}

.limit-btn {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 6px;
  cursor: pointer;
  height: 32px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  white-space: nowrap;
}

.limit-btn.active {
  border-color: var(--brand);
  box-shadow: 0 0 0 2px rgba(30, 64, 175, 0.25);
}

.caret {
  font-size: 10px;
  transition: transform 0.2s;
}

.caret.up {
  transform: rotate(180deg);
}

.limit-menu {
  list-style: none;
  margin: 0;
  padding: 0;
  position: absolute;
  top: 100%;
  left: 0;
  background: #fff;
  border: 1px solid #1e40af;
  border-radius: 8px;
  width: 97px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.limit-item {
  width: 100%;
  height: 32px;
  display: flex;
  align-items: center;
  padding: 8px 8px 8px 17px;
  font-size: 14px;
  color: #1e40af;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  box-sizing: border-box;
}

/* Адаптивность для мобильных устройств */
@media (max-width: 768px) {
  .filter-toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .filter-left {
    justify-content: center;
    gap: 12px;
  }
}

/* Для очень маленьких экранов */
@media (max-width: 480px) {
  .filter-left {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
}

.rating-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  width: 100%;
}

.rating-toggle-header {
  display: flex;
  align-items: center;
}

.rating-toggle-header .toggle-switch {
  background: #f3f4f6;
  border-radius: 20px;
  padding: 4px;
  border: 1px solid #e5e7eb;
  display: flex;
  gap: 4px;
}

.rating-toggle-header .toggle-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 16px;
  background: transparent;
  color: #6b7280;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.rating-toggle-header .toggle-btn:hover {
  background: #e5e7eb;
  color: #374151;
}

.rating-toggle-header .toggle-btn.active {
  background: #1e40af;
  color: white;
  box-shadow: 0 1px 3px rgba(30, 64, 175, 0.2);
}

.filter-toolbar .rating-type-toggle {
  display: none;
}

.chart-section-title-btn {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 6px 10px;
  width: 100%;
  margin: 0 0 14px;
  padding: 0;
  border: none;
  background: none;
  cursor: pointer;
  text-align: left;
  font-weight: 700;
  font-size: 22px;
  color: #0f172a;
  line-height: 1.25;
}

.chart-section-title-btn:hover .chart-section-chevron {
  color: #1e40af;
  transform: translateY(2px);
}

.chart-section-chevron {
  font-size: 14px;
  color: #64748b;
  transition: color 0.2s ease, transform 0.2s ease;
}

.chart-section-hint {
  flex-basis: 100%;
  font-size: 12px;
  font-weight: 500;
  color: #64748b;
}
</style>