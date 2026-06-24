<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getStatus } from '../api/oauth'
import { requestSignature } from '../api/files'
import type { OAuthStatus } from '../types'

const status = ref<OAuthStatus>({ connected: false, username: null })
const selectedFile = ref<File | null>(null)
const signerEmail = ref('')
const signerDisplayName = ref('')
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
  if (!selectedFile.value || !signerEmail.value) return
  uploading.value = true
  result.value = ''
  try {
    const res = await requestSignature(
      selectedFile.value,
      signerEmail.value,
      signerDisplayName.value || undefined,
    )
    if (res.success) {
      result.value = `Signature requested: ${res.name} (UUID: ${res.uuid})`
    } else {
      result.value = `Error: ${res.error}`
    }
  } catch (e) {
    result.value = 'Signature request failed'
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div class="card">
    <h1>Request Signature</h1>
    <div v-if="!status.connected" class="alert alert-info">
      Please connect first.
    </div>
    <div v-else>
      <div class="form-group">
        <label>File (PDF)</label>
        <input type="file" accept=".pdf" @change="onFileSelected" />
      </div>
      <div class="form-group">
        <label>Signer Email</label>
        <input type="email" v-model="signerEmail" placeholder="signer@example.com" />
      </div>
      <div class="form-group">
        <label>Signer Name (optional)</label>
        <input type="text" v-model="signerDisplayName" placeholder="John Doe" />
      </div>
      <button
        class="btn btn-primary"
        :disabled="!selectedFile || !signerEmail || uploading"
        @click="handleUpload"
      >
        {{ uploading ? 'Sending...' : 'Request Signature' }}
      </button>
      <div v-if="result" :class="result.startsWith('Error') ? 'alert alert-error' : 'alert alert-success'" style="margin-top: 1rem;">
        {{ result }}
      </div>
    </div>
  </div>
</template>
