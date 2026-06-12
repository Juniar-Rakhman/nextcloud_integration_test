<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getConfig, saveConfig } from '../api/admin'
import type { AdminConfig } from '../types'

const config = ref<AdminConfig>({
  nextcloudUrl: '',
  clientId: '',
  clientSecret: '',
  redirectUri: '',
})
const message = ref('')
const isError = ref(false)

onMounted(async () => {
  try {
    config.value = await getConfig()
  } catch {
    message.value = 'Failed to load config'
    isError.value = true
  }
})

async function handleSave() {
  try {
    await saveConfig(config.value)
    message.value = 'Configuration saved'
    isError.value = false
  } catch {
    message.value = 'Failed to save config'
    isError.value = true
  }
}

function handleReset() {
  config.value = {
    nextcloudUrl: '',
    clientId: '',
    clientSecret: '',
    redirectUri: '',
  }
}
</script>

<template>
  <div class="card">
    <h1>Admin Configuration</h1>
    <div v-if="message" :class="isError ? 'alert alert-error' : 'alert alert-success'">
      {{ message }}
    </div>
    <div class="form-group">
      <label>Nextcloud URL</label>
      <input type="text" v-model="config.nextcloudUrl" placeholder="http://localhost:8088" />
    </div>
    <div class="form-group">
      <label>Client ID</label>
      <input type="text" v-model="config.clientId" placeholder="client123" />
    </div>
    <div class="form-group">
      <label>Client Secret</label>
      <input type="text" v-model="config.clientSecret" placeholder="secret123" />
    </div>
    <div class="form-group">
      <label>Redirect URI</label>
      <input type="text" v-model="config.redirectUri" placeholder="http://localhost:8080/api/oauth/callback" />
    </div>
    <button class="btn btn-primary" @click="handleSave">Save</button>
    <button class="btn btn-secondary" @click="handleReset">Reset</button>
  </div>
</template>
