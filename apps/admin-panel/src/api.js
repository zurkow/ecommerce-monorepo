// One tiny client for all four services. Errors come back in the shared ApiError
// format (libs/common), so a single error handler works for every service.
async function request(method, url, body) {
  const res = await fetch(url, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : {},
    body: body ? JSON.stringify(body) : undefined,
  })
  if (res.status === 204) return null
  const data = await res.json().catch(() => null)
  if (!res.ok) {
    const details = data?.details?.length ? ` (${data.details.join(', ')})` : ''
    throw new Error((data?.message ?? `HTTP ${res.status}`) + details)
  }
  return data
}

export const api = {
  users: {
    list: () => request('GET', '/api/users'),
    create: (u) => request('POST', '/api/users', u),
    update: (id, u) => request('PUT', `/api/users/${id}`, u),
    remove: (id) => request('DELETE', `/api/users/${id}`),
  },
  carts: {
    list: () => request('GET', '/api/carts'),
    addItem: (userId, item) => request('POST', `/api/carts/${userId}/items`, item),
    removeItem: (userId, sku) => request('DELETE', `/api/carts/${userId}/items/${encodeURIComponent(sku)}`),
  },
  orders: {
    list: () => request('GET', '/api/orders'),
    checkout: (userId) => request('POST', '/api/orders/checkout', { userId }),
    setStatus: (id, status) => request('PATCH', `/api/orders/${id}/status`, { status }),
  },
  payments: {
    list: () => request('GET', '/api/payments'),
    refund: (id) => request('POST', `/api/payments/${id}/refund`),
  },
}

export const money = (v) => Number(v ?? 0).toFixed(2) + ' PLN'
export const date = (v) => (v ? new Date(v).toLocaleString() : '')
