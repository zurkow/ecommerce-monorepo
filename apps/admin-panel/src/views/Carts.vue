<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api, money } from '../api'
import { useAsync } from '../useAsync'

const carts = ref([])
const users = ref([])
const item = reactive({ userId: '', sku: 'MUG-1', name: 'Coffee mug', unitPrice: 29.99, quantity: 1 })

const load = async () => {
  ;[carts.value, users.value] = await Promise.all([api.carts.list(), api.users.list()])
  if (!item.userId && users.value.length) item.userId = users.value[0].id
}
const { error, run } = useAsync(load)
onMounted(() => run(() => {}))

const userName = (id) => users.value.find((u) => u.id === id)?.email ?? `user #${id}`
const add = () => {
  const { userId, ...body } = item
  return run(() => api.carts.addItem(userId, body))
}
const removeItem = (userId, sku) => run(() => api.carts.removeItem(userId, sku))
const checkout = (userId) => run(() => api.orders.checkout(userId))
</script>

<template>
  <h2>Carts <span class="muted">cart-service</span></h2>
  <div v-if="error" class="error">{{ error }}</div>
  <div class="card">
    <p class="muted">Put something in a customer's cart, then check out (order-service → payment-service).
      Payments above 1000 PLN are declined.</p>
    <form @submit.prevent="add">
      <select v-model="item.userId">
        <option v-for="u in users" :key="u.id" :value="u.id">{{ u.email }}</option>
      </select>
      <input v-model="item.sku" placeholder="sku" size="8" />
      <input v-model="item.name" placeholder="name" />
      <input v-model.number="item.unitPrice" type="number" step="0.01" min="0.01" style="width: 100px" />
      <input v-model.number="item.quantity" type="number" min="1" style="width: 70px" />
      <button>Add to cart</button>
    </form>
  </div>
  <div v-if="!carts.length" class="card muted">All carts are empty.</div>
  <div v-for="c in carts" :key="c.userId" class="card">
    <strong>{{ userName(c.userId) }}</strong> — total {{ money(c.total) }}
    <button style="float: right" @click="checkout(c.userId)">Checkout</button>
    <table>
      <tr><th>SKU</th><th>Name</th><th>Price</th><th>Qty</th><th></th></tr>
      <tr v-for="i in c.items" :key="i.sku">
        <td>{{ i.sku }}</td><td>{{ i.name }}</td><td>{{ money(i.unitPrice) }}</td><td>{{ i.quantity }}</td>
        <td><button class="secondary" @click="removeItem(c.userId, i.sku)">Remove</button></td>
      </tr>
    </table>
  </div>
</template>
