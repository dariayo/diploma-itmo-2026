<template>
  <div class="selectBox" ref="selectBox">
    <input
        type="text"
        class="search-box"
        :placeholder="placeholderText"
        v-model="searchQuery"
        @focus="showOptions = true"
        @input="handleInput"
        ref="searchInput"
    >
    <div class="reis-main" v-show="showOptions" ref="reisMain">
      <ul class="options">
        <li
            v-for="option in filteredOptions"
            :key="option"
            @click="selectOption(option)"
            @mousedown.prevent
        >
          {{ option }}
        </li>
      </ul>
      <div class="selected-options">
        <button
            v-for="route in selectedRoutes"
            :key="route"
            class="remove-button"
            @click="removeRoute(route, $event)"
            type="button"
            @mousedown.prevent
        >
          {{ route }}
        </button>
      </div>
      <button
          id="clear-button"
          :class="{ 'hidden': selectedRoutes.length === 0 }"
          @click="clearSelection"
          type="button"
          @mousedown.prevent
      >
        Очистить выбор
      </button>
    </div>
  </div>

</template>

<script>
import config from '../../config.js';

export default {
  name: 'RouteSelector',
  props: ['modelValue'],
  emits: ['update:modelValue'],
  data() {
    return {
      searchQuery: '',
      showOptions: false,
      allOptions: [],
      selectedRoutes: [],
      state: {
        selectedRoutes: new Set()
      }
    }
  },
  computed: {
    filteredOptions() {
      if (!this.searchQuery) return this.allOptions;

      return this.allOptions.filter(option =>
          option.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
    },
    placeholderText() {
      if (this.selectedRoutes.length === 0) {
        return 'Введите маршрут';
      } else if (this.selectedRoutes.length === 1) {
        return `Выбран: ${this.selectedRoutes[0]}`;
      } else {
        return `Выбрано несколько`;
      }
    }
  },
  watch: {
    selectedRoutes: {
      handler(newRoutes) {
        this.state.selectedRoutes = new Set(newRoutes);
        this.$emit('routes-changed', Array.from(this.state.selectedRoutes));
        this.updatePlaceholder();
      },
      deep: true
    }
  },
  async mounted() {
    await this.loadOptions();
    this.selectedRoutes = []
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeDestroy() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    updateArray() {
      this.$emit('update:modelValue', this.selectedRoutes)
    },
    async loadOptions() {
      try {
        const response = await fetch(`http://${config.apiBaseUrl}/api/flights/routes`, {
          method: "GET",
          credentials: "include",
          headers: {
            "Content-Type": "application/json"
          },
        });
        const text = await response.text();
        const optionsArray = text
            .replace(/[$$]/g, '') // Remove unwanted characters
            .split(',')
        const options = optionsArray.splice(1, optionsArray.length - 2)
            .map(line => line.trim().replace(/"/g, '').replace(/→/g, ' → '))
            .filter(line => line !== '');

        options.push('SVO → GOJ')
        options.push('ZIA → ULY')
        this.allOptions = Array.from(new Set(options)).sort();
      } catch (err) {
        console.error('Error loading options:', err);
      }
    },
    handleInput() {
      if (this.searchQuery.length === 3 && !this.searchQuery.includes('→')) {
        this.searchQuery = this.searchQuery + ' → ';
      }

      if (this.searchQuery.length === 5) {
        this.searchQuery = this.searchQuery.replace(/\s*→\s*$/, '');
      }
    },
    selectOption(option) {
      if (!this.selectedRoutes.includes(option)) {
        this.selectedRoutes.push(option);
        this.searchQuery = '';
        this.updatePlaceholder();
        this.$refs.searchInput.focus();
        this.updateArray()
      }
    },
    removeRoute(route, event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation()
      }

      this.selectedRoutes = this.selectedRoutes.filter(r => r !== route);
      this.updatePlaceholder();
      this.$refs.searchInput.focus();
      this.updateArray()
    },
    clearSelection(event) {
      if (event) {
        event.preventDefault();
      }
      this.selectedRoutes = [];
      this.searchQuery = '';
      this.updatePlaceholder();
      //this.$refs.searchInput.focus();
      this.updateArray()
    },
    updatePlaceholder() {
      const searchBox = this.$el.querySelector('.search-box');
      if (searchBox) {
        searchBox.placeholder = this.placeholderText;
      }
    },
    handleClickOutside(event) {
      if (this.$refs.selectBox && !this.$refs.selectBox.contains(event.target)) {
        this.showOptions = false;
      }
    }
  },
  watch: {
    initialRoutes: {
      handler(newRoutes) {
        this.selectedRoutes = [...newRoutes];
      },
      deep: true
    }
  }

}
</script>

<style scoped>
.form-group {
  margin-bottom: 1rem;
}

.selectBox {
  position: relative;
}

.search-box {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--line);
  border-bottom: 1px solid #ccc;
  outline: none;
  font-size: 14px;
}

.reis-main {
  width: 100%;
  border: 1px solid var(--line, #ccc);
  border-radius: var(--border-radius, 4px);
  position: absolute;
  background-color: #fff;
  z-index: 1000;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  top: 100%;
  left: 0;
  margin-top: 5px;
}

.options {
  list-style: none;
  padding: 0;
  margin: 0;
  max-height: 250px;
  overflow-y: auto;
  border-bottom: 1px solid #eee;
}

.options li {
  padding: 10px;
  cursor: pointer;
  user-select: none;
}

.options li:hover {
  background-color: #f0f0f0;
}

.selected-options {
  display: flex;
  flex-wrap: wrap;
  padding: 5px;
  gap: 5px;
}

.remove-button {
  background-color: #1e40af;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 5px 10px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  user-select: none;
}

.remove-button::before {
  content: '✖';
  margin-right: 5px;
  font-size: 1.2em;
}

.remove-button:hover {
  background-color: #ff1a1a;
}

#clear-button {
  width: 100%;
  background-color: #EF6666;
  color: white;
  font-weight: 680;
  border-color: var(--line);
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  z-index: 100;
}

.hidden {
  display: none;
}
</style>
