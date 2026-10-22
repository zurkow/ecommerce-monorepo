<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api, date } from '../api'
import { useAsync } from '../useAsync'

const users = ref([])
const empty = () => ({ email: '', firstName: '', lastName: '', role: 'CUSTOMER' })
const form = reactive(empty())
const editingId = ref(null)

const load = async () => (users.value = await api.users.list())
const { error, run } = useAsync(load)
onMounted(() => run(() => {}))

function edit(u) {
  editingId.value = u.id
  Object.assign(form, { email: u.email, firstName: u.firstName, lastName: u.lastName, role: u.role })
}
function reset() {
  editingId.value = null
  Object.assign(form, empty())
}
const save = () =>
  run(async () => {
    if (editingId.value) await api.users.update(editingId.value, { ...form })
    else await api.users.create({ ...form })
    reset()
  })
const remove = (id) => run(() => api.users.remove(id))
</script>

<template>
  <h2>Users <span class="muted">user-service</span></h2>
  <div v-if="error" class="error">{{ error }}</div>
  <div class="card">
    <form @submit.prevent="save">
      <input v-model="form.email" placeholder="email" />
      <input v-model="form.firstName" placeholder="first name" />
      <input v-model="form.lastName" placeholder="last name" />
      <select v-model="form.role">
        <option>CUSTOMER</option>
        <option>ADMIN</option>
      </select>
      <button>{{ editingId ? 'Save' : 'Add user' }}</button>
      <button v-if="editingId" type="button" class="secondary" @click="reset">Cancel</button>
    </form>
  </div>
  <div class="card">
    <table>
      <thead><tr><th>ID</th><th>Email</th><th>Name</th><th>Role</th><th>Created</th><th></th></tr></thead>
      <tbody>
      <tr v-for="u in users" :key="u.id">
        <td>{{ u.id }}</td>
        <td>{{ u.email }}</td>
        <td>{{ u.firstName }} {{ u.lastName }}</td>
        <td><span class="badge">{{ u.role }}</span></td>
        <td>{{ date(u.createdAt) }}</td>
        <td>
          <button class="secondary" @click="edit(u)">Edit</button>
          <button class="danger" @click="remove(u.id)">Delete</button>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>
