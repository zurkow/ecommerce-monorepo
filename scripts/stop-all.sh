#!/usr/bin/env bash
cd "$(dirname "$0")/.."
for pidfile in .run/*.pid; do
  [ -e "$pidfile" ] || continue
  kill "$(cat "$pidfile")" 2>/dev/null && echo "stopped $(basename "$pidfile" .pid)"
  rm -f "$pidfile"
done
