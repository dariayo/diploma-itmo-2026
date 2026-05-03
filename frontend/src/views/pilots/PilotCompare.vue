<script setup>
import {onMounted} from 'vue'
import {useRouter} from 'vue-router'

const router = useRouter()

onMounted(() => {
  router.replace({path: '/pilots', hash: '#pilot-compare'})
})
</script>

<template>
  <div class="compare-redirect" role="status">
    Переход к сравнению пилотов…
  </div>
</template>

<style scoped>
.compare-redirect {
  padding: 3rem 1rem;
  text-align: center;
  color: var(--muted, #64748b);
  font-size: 15px;
}
</style>
