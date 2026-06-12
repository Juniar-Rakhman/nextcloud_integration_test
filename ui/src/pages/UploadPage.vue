<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getStatus } from '../api/oauth'
import { uploadFile } from '../api/files'
import type { OAuthStatus } from '../types'

const status = ref<OAuthStatus>({ connected: false, username: null })
const selectedFile = ref<File | null>(null)
const uploading = ref(false)
const result = ref('')

onMounted(async () => {
  try {
    status.value = await getStatus()
  } catch {
    status.value = { connected: false, username: null }
  }
})

function onFileSelected(event: Event) {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    selectedFile.value = target.files[0]
  }
}

async function handleUpload() {
  if (!selectedFile.value) return
  uploading.value = true
  result.value = ''
  try {
    const res = await uploadFile(selectedFile.value)
    if (res.success) {
      result.value = `Uploaded to ${res.path}`
    } else {
      result.value = `Error: ${res.error}`
    }
  } catch {
    result.value = 'Upload failed'
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div class="card">
    <h1>Upload File</h1>
    <div v-if="!status.connected" class="alert alert-info">
      Please connect first.
    </div>
    <div v-else>
      <div class="form-group">
        <input type="file" @change="onFileSelected" />
      </div>
      <button
        class="btn btn-primary"
        :disabled="!selectedFile || uploading"
        @click="handleUpload"
      >
        {{ uploading ? 'Uploading...' : 'Upload' }}
      </button>
      <div v-if="result" :class="result.startsWith('Error') ? 'alert alert-error' : 'alert alert-success'" style="margin-top: 1rem;">
        {{ result }}
      </div>
    </div>
  </div>
</template>
