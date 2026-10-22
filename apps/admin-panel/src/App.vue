<template>
  <div class="layout">
    <nav>
      <h1>Shop Admin</h1>
      <RouterLink to="/">Dashboard</RouterLink>
      <RouterLink to="/users">Users</RouterLink>
      <RouterLink to="/carts">Carts</RouterLink>
      <RouterLink to="/orders">Orders</RouterLink>
      <RouterLink to="/payments">Payments</RouterLink>
    </nav>
    <main><RouterView /></main>
  </div>
</template>
