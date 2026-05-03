<template>
  <div class="period">
    <div class="period-buttons" ref="periodButtonsRef">
      <button
          v-for="p in periods"
          :key="p.value"
          class="period-btn"
          type="button"
          :class="{
            active: periodStore.selectedPeriod === p.value,
            'period-btn-custom': p.value === 'custom'
          }"
          @click="setPeriod(p.value)"
      >
        <template v-if="p.value === 'custom'">
          <span class="period-label">Период:</span> {{ p.label }}
        </template>
        <template v-else>
          {{ p.label }}
        </template>
      </button>
    </div>

    <!-- Выпадающее меню для месяца -->
    <div
        class="period-dropdown-wrapper"
        v-if="showMonthDropdown"
        :style="monthDropdownStyle"
    >
      <div class="period-dropdown" @click.stop>
        <div
            v-for="item in monthOptions"
            :key="item.key"
            class="period-dropdown-item"
            :class="{ 'divider': item.isDivider }"
            @click="!item.isDivider && selectMonth(item)"
        >
          {{ item.label }}
        </div>
      </div>
    </div>

    <!-- Выпадающее меню для квартала -->
    <div
        class="period-dropdown-wrapper"
        v-if="showQuarterDropdown"
        :style="quarterDropdownStyle"
    >
      <div class="period-dropdown" @click.stop>
        <div
            v-for="item in quarterOptions"
            :key="item.key"
            class="period-dropdown-item"
            :class="{ 'divider': item.isDivider }"
            @click="!item.isDivider && selectQuarter(item)"
        >
          {{ item.label }}
        </div>
      </div>
    </div>

    <div
        class="custom-period-wrapper"
        v-if="periodStore.showCustom"
        :style="customPeriodStyle"
    >
     <div id="customPeriod" class="custom-period" @click.stop>
         <div class="custom-period-row">
             <label>С</label>
             <input type="date" v-model="periodStore.customStart" class="filter-select"/>
             <label>По</label>
             <input type="date" v-model="periodStore.customEnd" class="filter-select"/>

             <button
                 id="applyCustomPeriod"
                 type="button"
                 class="button-primary"
                 :disabled="isButtonDisabled"
                 @click="applyCustomPeriod"
             >
                 Применить
             </button>

             <!-- Error message -->
             <p v-if="startAfterEnd" class="error-message">
                 Конечная дата раньше начальной

             </p>
         </div>
     </div>
    </div>

    <div
        id="period-display"
        class="period-display"
        v-show="periodStore.periodText"
        style="margin: 15px 0"
    >
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, onBeforeUnmount, computed, nextTick, watch} from 'vue'
import {usePeriodStore} from '@/stores/periodStore'

const periodStore = usePeriodStore()
const periodButtonsRef = ref(null)
const customPeriodStyle = ref({})
const showMonthDropdown = ref(false)
const showQuarterDropdown = ref(false)
const monthDropdownStyle = ref({})
const quarterDropdownStyle = ref({})

const customPeriodLabel = computed(() => {
  if (periodStore.customStart && periodStore.customEnd && periodStore.customStart < periodStore.customEnd) {
    const formatDate = (dateString) => {
      const date = new Date(dateString)
      const day = String(date.getDate()).padStart(2, '0')
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const year = date.getFullYear()
      return `${day}.${month}.${year}`
    }
    return `${formatDate(periodStore.customStart)} - ${formatDate(periodStore.customEnd)}`
  }
  return 'Не корректный период'
})

const periods = computed(() => [
  {value: 'month', label: 'Месяц'},
  {value: 'lastMonth', label: 'Прошлый месяц'},
  {value: 'quarter', label: 'Квартал'},
  {value: 'year', label: 'Год'},
  {value: 'custom', label: customPeriodLabel.value},
])

function setPeriod(period) {
  if (period === 'custom') {
    closeAllDropdowns()
    periodStore.selectedPeriod = 'custom'
    periodStore.showCustom = true
    periodStore.periodChanged = false
    nextTick(() => {
      updateCustomPeriodPosition()
    })
  } else if (period === 'month') {
    closeAllDropdowns()
    showMonthDropdown.value = true
    nextTick(() => {
      updateMonthDropdownPosition()
    })
  } else if (period === 'quarter') {
    closeAllDropdowns()
    showQuarterDropdown.value = true
    nextTick(() => {
      updateQuarterDropdownPosition()
    })
  } else {
    closeAllDropdowns()
    periodStore.setPeriod(period)
  }
}

function applyCustomPeriod() {
  periodStore.applyCustomPeriod()
  periodStore.showCustom = false
}

const monthOptions = computed(() => {
  const now = new Date()
  const currentYear = now.getFullYear()
  const currentMonth = now.getMonth()
  const options = []

  options.push({
    key: `${currentYear}-${currentMonth}`,
    label: formatMonthLabel(currentYear, currentMonth),
    year: currentYear,
    month: currentMonth
  })

  for (let i = currentMonth - 1; i >= 0; i--) {
    options.push({
      key: `${currentYear}-${i}`,
      label: formatMonthLabel(currentYear, i),
      year: currentYear,
      month: i
    })
  }

  options.push({
    key: 'divider',
    label: '',
    isDivider: true
  })

  const lastYear = currentYear - 1
  for (let i = 11; i >= 0; i--) {
    options.push({
      key: `${lastYear}-${i}`,
      label: formatMonthLabel(lastYear, i),
      year: lastYear,
      month: i
    })
  }

  return options
})

const quarterOptions = computed(() => {
  const now = new Date()
  const currentYear = now.getFullYear()
  const currentMonth = now.getMonth()
  const currentQuarter = Math.floor(currentMonth / 3)
  const options = []

  options.push({
    key: `${currentYear}-Q${currentQuarter + 1}`,
    label: formatQuarterLabel(currentYear, currentQuarter),
    year: currentYear,
    quarter: currentQuarter
  })

  for (let i = currentQuarter - 1; i >= 0; i--) {
    options.push({
      key: `${currentYear}-Q${i + 1}`,
      label: formatQuarterLabel(currentYear, i),
      year: currentYear,
      quarter: i
    })
  }

  options.push({
    key: 'divider',
    label: '',
    isDivider: true
  })

  const lastYear = currentYear - 1
  for (let i = 3; i >= 0; i--) {
    options.push({
      key: `${lastYear}-Q${i + 1}`,
      label: formatQuarterLabel(lastYear, i),
      year: lastYear,
      quarter: i
    })
  }

  return options
})

const isButtonDisabled = computed(() => {
            return !periodStore.customStart ||
                   !periodStore.customEnd ||
                   startAfterEnd.value;
        });

const startAfterEnd = computed(() => {
            const start = periodStore.customStart;
            const end = periodStore.customEnd;
            const condition = start && end && (start > end);
            return condition;
});

function formatMonthLabel(year, month) {
  const monthNames = [
    'Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь',
    'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'
  ]
  const now = new Date()
  const currentYear = now.getFullYear()
  const currentMonth = now.getMonth()

  if (year === currentYear && month === currentMonth) {
    return monthNames[month] + ' (текущий)'
  } else if (year === currentYear && month === currentMonth - 1) {
    return monthNames[month] + ' (прошлый)'
  } else {
    return `${monthNames[month]} ${year}`
  }
}

function formatQuarterLabel(year, quarter) {
  const quarterNames = ['I квартал', 'II квартал', 'III квартал', 'IV квартал']
  const now = new Date()
  const currentYear = now.getFullYear()
  const currentMonth = now.getMonth()
  const currentQuarter = Math.floor(currentMonth / 3)

  if (year === currentYear && quarter === currentQuarter) {
    return quarterNames[quarter] + ' ' + year + ' (текущий)'
  } else if (year === currentYear && quarter === currentQuarter - 1) {
    return quarterNames[quarter] + ' ' + year + ' (прошлый)'
  } else {
    return quarterNames[quarter] + ' ' + year
  }
}

function selectMonth(item) {
  const startDate = new Date(item.year, item.month, 1)
  const endDate = new Date(item.year, item.month + 1, 0)

  periodStore.customStart = formatDateForInput(startDate)
  periodStore.customEnd = formatDateForInput(endDate)
  periodStore.selectedPeriod = 'custom'
  periodStore.applyCustomPeriod()
  showMonthDropdown.value = false
}

function selectQuarter(item) {
  const quarterStartMonth = item.quarter * 3
  const startDate = new Date(item.year, quarterStartMonth, 1)
  const endDate = new Date(item.year, quarterStartMonth + 3, 0)

  periodStore.customStart = formatDateForInput(startDate)
  periodStore.customEnd = formatDateForInput(endDate)
  periodStore.selectedPeriod = 'custom'
  periodStore.applyCustomPeriod()
  showQuarterDropdown.value = false
}

function formatDateForInput(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function closeAllDropdowns() {
  showMonthDropdown.value = false
  showQuarterDropdown.value = false
  periodStore.showCustom = false
}

function updateMonthDropdownPosition() {
  if (periodButtonsRef.value && showMonthDropdown.value) {
    const buttons = periodButtonsRef.value.querySelectorAll('.period-btn')
    const monthButton = Array.from(buttons).find(btn => {
      return btn.textContent.includes('Месяц')
    })

    if (monthButton) {
      const rect = monthButton.getBoundingClientRect()
      const toolbar = monthButton.closest('.filter-toolbar')
      if (toolbar) {
        const toolbarRect = toolbar.getBoundingClientRect()
        monthDropdownStyle.value = {
          position: 'absolute',
          left: `${rect.left - toolbarRect.left}px`,
          top: `${rect.bottom - toolbarRect.top + 8}px`,
        }
      }
    }
  }
}

function updateQuarterDropdownPosition() {
  if (periodButtonsRef.value && showQuarterDropdown.value) {
    const buttons = periodButtonsRef.value.querySelectorAll('.period-btn')
    const quarterButton = Array.from(buttons).find(btn => {
      return btn.textContent.includes('Квартал')
    })

    if (quarterButton) {
      const rect = quarterButton.getBoundingClientRect()
      const toolbar = quarterButton.closest('.filter-toolbar')
      if (toolbar) {
        const toolbarRect = toolbar.getBoundingClientRect()
        quarterDropdownStyle.value = {
          position: 'absolute',
          left: `${rect.left - toolbarRect.left}px`,
          top: `${rect.bottom - toolbarRect.top + 8}px`,
        }
      }
    }
  }
}

function updateCustomPeriodPosition() {
  if (periodButtonsRef.value && periodStore.selectedPeriod === 'custom') {
    const buttons = periodButtonsRef.value.querySelectorAll('.period-btn')
    const customButton = buttons[buttons.length - 1]

    if (customButton) {
      const rect = customButton.getBoundingClientRect()
      const toolbar = customButton.closest('.filter-toolbar')
      if (toolbar) {
        const toolbarRect = toolbar.getBoundingClientRect()
        customPeriodStyle.value = {
          position: 'absolute',
          left: `${rect.left - toolbarRect.left}px`,
          top: `${rect.bottom - toolbarRect.top + 8}px`,
        }
      }
    }
  }
}

function handleClickOutside(event) {
  const customPeriod = document.getElementById('customPeriod')
  const customPeriodWrapper = event.target.closest('.custom-period-wrapper')
  const periodDropdownWrapper = event.target.closest('.period-dropdown-wrapper')
  const periodButtons = event.target.closest('.period-buttons')

  if (!periodButtons && !customPeriodWrapper && !periodDropdownWrapper) {
    if (!event.target.closest('.period-btn')) {
      closeAllDropdowns()
    }
  }
}

watch(
  () => periodStore.showCustom,
  (show) => {
    if (show) {
      nextTick(() => {
        updateCustomPeriodPosition()
        window.addEventListener('resize', updateCustomPeriodPosition)
        window.addEventListener('scroll', updateCustomPeriodPosition, true)
      })
    } else {
      window.removeEventListener('resize', updateCustomPeriodPosition)
      window.removeEventListener('scroll', updateCustomPeriodPosition, true)
    }
  }
)

watch(
  () => showMonthDropdown.value,
  (show) => {
    if (show) {
      nextTick(() => {
        updateMonthDropdownPosition()
        window.addEventListener('resize', updateMonthDropdownPosition)
        window.addEventListener('scroll', updateMonthDropdownPosition, true)
      })
    } else {
      window.removeEventListener('resize', updateMonthDropdownPosition)
      window.removeEventListener('scroll', updateMonthDropdownPosition, true)
    }
  }
)

watch(
  () => showQuarterDropdown.value,
  (show) => {
    if (show) {
      nextTick(() => {
        updateQuarterDropdownPosition()
        window.addEventListener('resize', updateQuarterDropdownPosition)
        window.addEventListener('scroll', updateQuarterDropdownPosition, true)
      })
    } else {
      window.removeEventListener('resize', updateQuarterDropdownPosition)
      window.removeEventListener('scroll', updateQuarterDropdownPosition, true)
    }
  }
)

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  window.removeEventListener('resize', updateCustomPeriodPosition)
  window.removeEventListener('scroll', updateCustomPeriodPosition, true)
  window.removeEventListener('resize', updateMonthDropdownPosition)
  window.removeEventListener('scroll', updateMonthDropdownPosition, true)
  window.removeEventListener('resize', updateQuarterDropdownPosition)
  window.removeEventListener('scroll', updateQuarterDropdownPosition, true)
})

watch(
    () => periodStore.periodChanged,
    (periodChanged) => {
      if (periodChanged) {
        periodStore.resetPeriodChanged()
      }
    },
    {immediate: true}
)
</script>

<style scoped>
.period {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  position: relative;
}

.period-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.period-btn {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  background: #fff;
  color: #0f172a;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  min-height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  text-align: center;
  word-wrap: break-word;
  white-space: nowrap;
}

.period-btn:hover:not(.active) {
  background: #f3f4f6;
}

.period-btn.active {
  background: #1e40af;
  color: #fff;
  border-color: #1e40af;
}

.period-btn-custom {
  padding: 8px 50px;
  min-width: 40px;
}

.error-message {
    color: #C41E3A;
    font-size: 14px;
    margin-top: 4px;
}

.period-label {
  font-weight: 600;
  margin-right: 4px;
}

.period-btn.active .period-label {
  color: #fff;
}

.period-badge {
  background: #1e40af;
  color: #fff;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 13px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  border: none;
  cursor: pointer;
}

.period-dropdown-wrapper {
  position: absolute;
  z-index: 100;
}

.period-dropdown {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  min-width: 200px;
  max-height: 400px;
  overflow-y: auto;
  padding: 4px 0;
}

.period-dropdown-item {
  padding: 10px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #374151;
  transition: background-color 0.2s ease;
}

.period-dropdown-item:hover:not(.divider) {
  background-color: #f3f4f6;
}

.period-dropdown-item.divider {
  height: 1px;
  padding: 0;
  margin: 8px 12px;
  background-color: #e5e7eb;
  cursor: default;
}

.custom-period-wrapper {
  position: absolute;
  z-index: 100;
}

.custom-period {
  background: #fff;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  min-width: 250px;
}

.custom-period-row {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  flex-wrap: nowrap;
}

.custom-period-row label {
  font-size: 13px;
  color: #374151;
  white-space: nowrap;
}

.filter-select {
  height: 36px;
  padding: 6px 8px;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  font-size: 14px;
  box-sizing: border-box;
}

.filter-select:focus {
  outline: none;
  border-color: #1e40af;
}

.button-primary {
  background: #1e40af;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 6px 10px;
  cursor: pointer;
  font-size: 14px;
  outline: none;
  height: 32px;
  margin-top: 4px;
  border: 1px solid #d1d5db;
  transition: background 0.2s ease;
}

.button-primary:hover:not(:disabled) {
  background: #15327a;
}

.button-primary:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.period-display {
  margin: 15px 0;
  padding: 0;
}

/* Адаптивность */
@media (max-width: 768px) {
  .filter-toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .period-buttons {
    width: 100%;
    justify-content: flex-start;
  }

  .period-btn {
    flex: 1;
    min-width: 80px;
  }

  .custom-period-wrapper {
    position: relative !important;
    left: auto !important;
    top: auto !important;
    width: 100%;
  }

  .custom-period {
    width: 100%;
    margin-top: 0;
  }

  .custom-period-row {
    flex-wrap: wrap;
    height: auto;
  }
}

@media (max-width: 480px) {
  .period-btn {
    padding: 6px 8px;
    font-size: 11px;
    height: 32px;
    min-height: 32px;
  }

  .custom-period-row {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .custom-period-row label,
  .custom-period-row input {
    width: 100%;
  }
}
</style>
