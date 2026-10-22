#!/usr/bin/env bash
# Builds the whole monorepo once, then starts all 4 services in the background.
# Logs: .run/<service>.log    Stop: ./scripts/stop-all.sh
set -euo pipefail
cd "$(dirname "$0")/.."

mvn -q -B package -DskipTests

mkdir -p .run
for svc in user-service cart-service order-service payment-service; do
  java -jar "services/$svc/target/$svc-1.0.0-SNAPSHOT.jar" > ".run/$svc.log" 2>&1 &
  echo $! > ".run/$svc.pid"
  echo "started $svc (pid $!)"
done

echo "waiting for services to become healthy..."
for port in 8081 8082 8083 8084; do
  until curl -sf "http://localhost:$port/actuator/health" > /dev/null; do sleep 1; done
  echo "  :$port UP"
done
echo "All services are up. Start the admin panel with: npm run admin:dev"
