import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import Dashboard from './views/Dashboard.vue'
import Users from './views/Users.vue'
import Carts from './views/Carts.vue'
import Orders from './views/Orders.vue'
import Payments from './views/Payments.vue'
import './style.css'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: Dashboard },
    { path: '/users', component: Users },
    { path: '/carts', component: Carts },
    { path: '/orders', component: Orders },
    { path: '/payments', component: Payments },
  ],
})

createApp(App).use(router).mount('#app')
