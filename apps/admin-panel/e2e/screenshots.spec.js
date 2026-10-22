import { expect, test } from '@playwright/test'

const SHOTS = 'screenshots'

// Runs serially: the checkout steps build on each other (cart -> order -> payment).
test.describe.configure({ mode: 'serial' })

test('dashboard', async ({ page }) => {
  await page.goto('/')
  await expect(page.locator('.stat').first()).not.toHaveText('…')
  await expect(page.getByText('DOWN')).toHaveCount(0)
  await page.screenshot({ path: `${SHOTS}/01-dashboard.png`, fullPage: true })
})

test('users', async ({ page }) => {
  await page.goto('/users')
  await expect(page.getByRole('cell', { name: 'admin@shop.local' })).toBeVisible()
  await page.screenshot({ path: `${SHOTS}/02-users.png`, fullPage: true })
})

test('carts with an item', async ({ page }) => {
  await page.goto('/carts')
  await page.getByRole('button', { name: 'Add to cart' }).click()
  await expect(page.getByRole('button', { name: 'Checkout' })).toBeVisible()
  await page.screenshot({ path: `${SHOTS}/03-carts.png`, fullPage: true })
})

test('orders after checkout', async ({ page }) => {
  await page.goto('/carts')
  await page.getByRole('button', { name: 'Checkout' }).click()
  await expect(page.getByText('All carts are empty.')).toBeVisible()

  await page.goto('/orders')
  await expect(page.locator('.badge.PAID').first()).toBeVisible()
  await page.screenshot({ path: `${SHOTS}/04-orders.png`, fullPage: true })
})

test('payments', async ({ page }) => {
  await page.goto('/payments')
  await expect(page.locator('.badge.APPROVED').first()).toBeVisible()
  await page.screenshot({ path: `${SHOTS}/05-payments.png`, fullPage: true })
})
