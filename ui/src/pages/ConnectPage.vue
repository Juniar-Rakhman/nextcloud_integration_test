<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getStatus, getLoginUrl, disconnect } from '../api/oauth'
import type { OAuthStatus } from '../types'

const status = ref<OAuthStatus>({ connected: false, username: null })
const loading = ref(false)
const message = ref('')

onMounted(async () => {
  try {
    status.value = await getStatus()
  } catch {
    message.value = 'Failed to load status'
  }
})

async function handleConnect() {
  try {
    loading.value = true
    const resp = await getLoginUrl()
    window.location.href = resp.redirectUrl
  } catch {
    message.value = 'Failed to start OAuth'
    loading.value = false
  }
}

async function handleDisconnect() {
  try {
    await disconnect()
    status.value = { connected: false, username: null }
    message.value = 'Disconnected'
  } catch {
    message.value = 'Failed to disconnect'
  }
}
</script>

<template>
  <div class="card">
    <h1>Connect to Nextcloud</h1>
    <div v-if="message" class="alert alert-info">{{ message }}</div>
    <div v-if="!status.connected">
      <p style="margin-bottom: 1rem;">Connect your Nextcloud account to enable file uploads.</p>
      <button class="btn btn-primary" :disabled="loading" @click="handleConnect">
        {{ loading ? 'Redirecting...' : 'Connect to Nextcloud' }}
      </button>
    </div>
    <div v-else>
      <p style="margin-bottom: 1rem;">
        Connected as <strong>{{ status.username }}</strong>
      </p>
      <button class="btn btn-danger" @click="handleDisconnect">Disconnect</button>
    </div>
  </div>
</template>
