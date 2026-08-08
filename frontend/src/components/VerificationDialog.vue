<script setup lang="ts">
import { ref } from 'vue'
import { api } from '@/api'
import { Shield, Loader2 } from '@lucide/vue'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'

const emit = defineEmits<{
  verified: []
  cancel: []
}>()

const { toast } = useToast()
const userStore = useUserStore()

const step = ref<'idle' | 'challenge' | 'verify'>('idle')
const challengeId = ref('')
const showCode = ref('')
const inputCode = ref('')
const loading = ref(false)

const startVerification = async () => {
  loading.value = true
  const uid = userStore.currentUser?.id ?? 1
  try {
    const res = await api.biometrics.challenge(uid)
    if (res.success && res.data) {
      challengeId.value = res.data.challengeId
      showCode.value = res.data.code
      step.value = 'verify'
    } else {
      toast(res.message || '验证码生成失败', 'error')
      emit('cancel')
    }
  } catch (e: any) {
    toast(e?.message || '未注册生物特征，请先注册', 'error')
    emit('cancel')
  } finally {
    loading.value = false
  }
}

const submitCode = async () => {
  if (!inputCode.value) {
    toast('请输入验证码', 'warning')
    return
  }
  loading.value = true
  const uid = userStore.currentUser?.id ?? 1
  try {
    const res = await api.biometrics.verifyChallenge(uid, challengeId.value, inputCode.value)
    if (res.success && res.data) {
      toast('生物验证通过', 'success')
      emit('verified')
    } else {
      toast(res.message || '验证失败', 'error')
    }
  } catch {
    toast('验证失败', 'error')
  } finally {
    loading.value = false
  }
}

const cancel = () => {
  step.value = 'idle'
  emit('cancel')
}
</script>

<template>
  <div class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="cancel">
    <div class="bg-white rounded-xl shadow-2xl w-full max-w-sm p-6 mx-4">
      <div class="text-center mb-6">
        <div class="w-14 h-14 bg-primary-500 rounded-xl flex items-center justify-center mx-auto mb-3">
          <Shield class="w-7 h-7 text-white" />
        </div>
        <h3 class="text-lg font-bold text-gray-800">生物特征验证</h3>
        <p class="text-sm text-gray-500 mt-1">此操作需要验证身份</p>
      </div>

      <!-- Step 1: Start -->
      <div v-if="step === 'idle'" class="text-center">
        <button @click="startVerification" :disabled="loading"
          class="w-full py-3 bg-primary-500 text-white font-medium rounded-lg hover:bg-primary-600 transition-colors disabled:opacity-50">
          <span v-if="loading" class="flex items-center justify-center gap-2">
            <Loader2 class="w-4 h-4 animate-spin" /> 生成验证码...
          </span>
          <span v-else>开始验证</span>
        </button>
        <button @click="cancel" class="mt-3 text-sm text-gray-400 hover:text-gray-600">取消</button>
      </div>

      <!-- Step 2: Enter code -->
      <div v-else-if="step === 'verify'">
        <div class="text-center mb-4">
          <p class="text-xs text-gray-400 mb-2">验证码（模拟发送到设备端）：</p>
          <div class="bg-gray-50 rounded-lg py-3 px-4 mb-1">
            <span class="text-2xl font-mono font-bold tracking-widest text-primary-600">{{ showCode }}</span>
          </div>
          <p class="text-xs text-gray-400">2 分钟内有效</p>
        </div>

        <div class="mb-4">
          <input
            v-model="inputCode"
            type="text"
            maxlength="6"
            placeholder="请输入 6 位验证码"
            class="w-full px-4 py-3 text-center text-lg font-mono tracking-widest border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 outline-none"
            @keyup.enter="submitCode"
          />
        </div>

        <button @click="submitCode" :disabled="loading || inputCode.length !== 6"
          class="w-full py-3 bg-primary-500 text-white font-medium rounded-lg hover:bg-primary-600 transition-colors disabled:opacity-50">
          {{ loading ? '验证中...' : '确认验证' }}
        </button>
        <button @click="cancel" class="mt-3 w-full text-sm text-gray-400 hover:text-gray-600">取消</button>
      </div>
    </div>
  </div>
</template>
