<template>
  <div id="app" class="app-layout">
    <div class="main-content">
      <router-view/>
    </div>
  </div>
</template>

<script>

export default {
  name: 'App',
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.app-layout {
  display: flex;
  width: 100%;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  transition: all 0.3s;

}

.sidebar.collapsed + .main-content {
  margin-left: 100px;
  transition: all 0.3s;
  /* ширина схлопнутого сайдбара */
}

#app {
  min-height: 100vh;
}

</style>