#!/usr/bin/env bash
# Walks through the whole purchase flow with curl. Run after ./scripts/start-all.sh
set -euo pipefail
step() { echo; echo "== $1"; }

step "1. Users (user-service :8081)"
curl -s localhost:8081/api/users; echo

step "2. Put items in the cart of user 2 (cart-service :8082)"
curl -s -X POST localhost:8082/api/carts/2/items -H 'Content-Type: application/json' \
  -d '{"sku":"MUG-1","name":"Coffee mug","unitPrice":29.99,"quantity":2}'; echo
curl -s -X POST localhost:8082/api/carts/2/items -H 'Content-Type: application/json' \
  -d '{"sku":"TEE-M","name":"T-shirt M","unitPrice":59.00,"quantity":1}'; echo

step "3. Checkout (order-service :8083 -> user, cart, payment services)"
curl -s -X POST localhost:8083/api/orders/checkout -H 'Content-Type: application/json' -d '{"userId":2}'; echo

step "4. Cart is now empty"
curl -s localhost:8082/api/carts/2; echo

step "5. Payment was recorded (payment-service :8084)"
curl -s localhost:8084/api/payments; echo

step "6. Checkout of an empty cart -> 409 in the shared error format from libs/common"
curl -s -X POST localhost:8083/api/orders/checkout -H 'Content-Type: application/json' -d '{"userId":2}'; echo

step "7. Unknown user -> 404 propagated from user-service"
curl -s -X POST localhost:8083/api/orders/checkout -H 'Content-Type: application/json' -d '{"userId":999}'; echo
