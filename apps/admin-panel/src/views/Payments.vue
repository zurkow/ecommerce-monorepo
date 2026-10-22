<script setup>
import { onMounted, ref } from 'vue'
import { api, date, money } from '../api'
import { useAsync } from '../useAsync'

const payments = ref([])
const load = async () => (payments.value = (await api.payments.list()).reverse())
const { error, run } = useAsync(load)
onMounted(() => run(() => {}))

const refund = (id) => run(() => api.payments.refund(id))
</script>

<template>
  <h2>Payments <span class="muted">payment-service</span></h2>
  <div v-if="error" class="error">{{ error }}</div>
  <div class="card">
    <table>
      <tr><th>ID</th><th>Order</th><th>User</th><th>Amount</th><th>Status</th><th>Created</th><th></th></tr>
      <tr v-for="p in payments" :key="p.id">
        <td>{{ p.id }}</td>
        <td>#{{ p.orderId }}</td>
        <td>#{{ p.userId }}</td>
        <td>{{ money(p.amount) }}</td>
        <td><span class="badge" :class="p.status">{{ p.status }}</span></td>
        <td>{{ date(p.createdAt) }}</td>
        <td><button v-if="p.status === 'APPROVED'" class="danger" @click="refund(p.id)">Refund</button></td>
      </tr>
      <tr v-if="!payments.length"><td colspan="7" class="muted">No payments yet.</td></tr>
    </table>
  </div>
</template>
