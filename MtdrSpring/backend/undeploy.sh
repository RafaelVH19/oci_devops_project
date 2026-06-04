#!/bin/bash
set -e

echo "Deleting auth deployment and service (PVC lumen-auth-data is kept)..."
kubectl -n mtdrworkshop delete deployment lumen-auth-server-deployment --ignore-not-found=true
kubectl -n mtdrworkshop delete service lumen-auth-service --ignore-not-found=true

echo "Deleting Spring deployment and services..."
kubectl -n mtdrworkshop delete deployment todolistapp-springboot-deployment --ignore-not-found=true
kubectl -n mtdrworkshop delete service todolistapp-springboot-service --ignore-not-found=true
kubectl -n mtdrworkshop delete service todolistapp-backend-router --ignore-not-found=true
