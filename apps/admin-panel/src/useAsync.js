import { ref } from 'vue'

// Wraps an API call: exposes a shared error message, reloads data after success.
export function useAsync(reload) {
  const error = ref('')
  async function run(fn) {
    error.value = ''
    try {
      await fn()
      await reload()
    } catch (e) {
      error.value = e.message
    }
  }
  return { error, run }
}
