<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getStatus } from '../api/oauth'
import type { OAuthStatus } from '../types'

const status = ref<OAuthStatus>({ connected: false, username: null })

onMounted(async () => {
  try {
    status.value = await getStatus()
  } catch {
    status.value = { connected: false, username: null }
  }
})

defineExpose({ status, refresh: async () => {
  try {
    status.value = await getStatus()
  } catch {
    status.value = { connected: false, username: null }
  }
} })
</script>

<template>
  <span class="status">
    {{ status.connected ? `Connected as ${status.username}` : 'Disconnected' }}
  </span>
</template>
