<script setup>
import { onMounted, ref } from 'vue'
import { api, money } from '../api'

// Each tile comes from a different service; one being down does not break the others.
const services = [
  { name: 'user-service', port: 8081, key: 'users', load: api.users.list },
  { name: 'cart-service', port: 8082, key: 'carts', load: api.carts.list },
  { name: 'order-service', port: 8083, key: 'orders', load: api.orders.list },
  { name: 'payment-service', port: 8084, key: 'payments', load: api.payments.list },
]
const data = ref({})
const down = ref({})

onMounted(() => {
  for (const s of services) {
    s.load()
      .then((rows) => (data.value[s.key] = rows))
      .catch(() => (down.value[s.key] = true))
  }
})

const revenue = (orders) =>
  orders.filter((o) => ['PAID', 'SHIPPED', 'DELIVERED'].includes(o.status)).reduce((sum, o) => sum + Number(o.total), 0)
</script>

<template>
  <h2>Dashboard</h2>
  <div class="stats">
    <div v-for="s in services" :key="s.key" class="card">
      <div class="muted">{{ s.name }} :{{ s.port }}</div>
      <div v-if="down[s.key]" class="stat" style="color: #dc2626">DOWN</div>
      <div v-else class="stat">{{ data[s.key]?.length ?? '…' }}</div>
      <div class="muted">{{ s.key }}</div>
    </div>
    <div class="card">
      <div class="muted">revenue (paid orders)</div>
      <div class="stat">{{ data.orders ? money(revenue(data.orders)) : '…' }}</div>
    </div>
  </div>
</template>
