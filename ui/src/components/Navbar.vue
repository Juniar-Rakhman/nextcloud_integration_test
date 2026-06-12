<script setup lang="ts">
import { RouterLink } from 'vue-router'
import ConnectionStatus from './ConnectionStatus.vue'
</script>

<template>
  <nav>
    <RouterLink to="/app/admin">Admin</RouterLink>
    <RouterLink to="/app/connect">Connect</RouterLink>
    <RouterLink to="/app/upload">Upload</RouterLink>
    <ConnectionStatus ref="connectionStatus" />
  </nav>
</template>
