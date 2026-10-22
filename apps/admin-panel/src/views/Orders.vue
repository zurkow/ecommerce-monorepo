<script setup>
import { onMounted, ref } from 'vue'
import { api, date, money } from '../api'
import { useAsync } from '../useAsync'

// Mirrors OrderStatus.next() in order-service; the backend still enforces it.
const NEXT = { PAID: ['SHIPPED', 'CANCELLED'], SHIPPED: ['DELIVERED'] }

const orders = ref([])
const load = async () => (orders.value = (await api.orders.list()).reverse())
const { error, run } = useAsync(load)
onMounted(() => run(() => {}))

const setStatus = (id, status) => run(() => api.orders.setStatus(id, status))
</script>

<template>
  <h2>Orders <span class="muted">order-service</span></h2>
  <div v-if="error" class="error">{{ error }}</div>
  <div class="card">
    <table>
      <tr><th>ID</th><th>User</th><th>Items</th><th>Total</th><th>Status</th><th>Payment</th><th>Created</th><th></th></tr>
      <tr v-for="o in orders" :key="o.id">
        <td>{{ o.id }}</td>
        <td>#{{ o.userId }}</td>
        <td>
          <div v-for="l in o.lines" :key="l.sku">{{ l.quantity }} × {{ l.name }}</div>
        </td>
        <td>{{ money(o.total) }}</td>
        <td><span class="badge" :class="o.status">{{ o.status }}</span></td>
        <td>#{{ o.paymentId }}</td>
        <td>{{ date(o.createdAt) }}</td>
        <td>
          <button v-for="s in NEXT[o.status] ?? []" :key="s" :class="s === 'CANCELLED' ? 'danger' : ''"
                  @click="setStatus(o.id, s)">{{ s === 'CANCELLED' ? 'Cancel & refund' : s }}</button>
        </td>
      </tr>
      <tr v-if="!orders.length"><td colspan="8" class="muted">No orders yet - check out a cart first.</td></tr>
    </table>
  </div>
</template>
