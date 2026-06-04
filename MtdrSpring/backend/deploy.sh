#!/bin/bash
set -e

SCRIPT_DIR=$(pwd)

if [ -z "$DOCKER_REGISTRY" ]; then
    export DOCKER_REGISTRY=$(state_get DOCKER_REGISTRY)
    echo "DOCKER_REGISTRY set."
fi
if [ -z "$DOCKER_REGISTRY" ]; then
    echo "Error: DOCKER_REGISTRY env variable needs to be set!"
    exit 1
fi

if [ -z "$TODO_PDB_NAME" ]; then
    export TODO_PDB_NAME=$(state_get MTDR_DB_NAME)
    echo "TODO_PDB_NAME set."
fi
if [ -z "$TODO_PDB_NAME" ]; then
    echo "Error: TODO_PDB_NAME env variable needs to be set!"
    exit 1
fi

if [ -z "$OCI_REGION" ]; then
    echo "OCI_REGION not set. Will get it with state_get"
    export OCI_REGION=$(state_get REGION)
fi
if [ -z "$OCI_REGION" ]; then
    echo "Error: OCI_REGION env variable needs to be set!"
    exit 1
fi

if [ -z "$UI_USERNAME" ]; then
    echo "UI_USERNAME not set. Will get it with state_get"
    export UI_USERNAME=$(state_get UI_USERNAME)
fi
if [ -z "$UI_USERNAME" ]; then
    echo "Error: UI_USERNAME env variable needs to be set!"
    exit 1
fi

if [ -z "$IMAGE_VERSION" ]; then
    export IMAGE_VERSION=$(git rev-parse --short HEAD)
fi

# Dirección pública = cómo entra el usuario (en OCI suele ser http://<IP-del-LoadBalancer>).
get_load_balancer_host() {
    kubectl get svc todolistapp-springboot-service -n mtdrworkshop \
        -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null \
        || kubectl get svc todolistapp-springboot-service -n mtdrworkshop \
        -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null \
        || true
}

resolve_lumen_public_url() {
    if [ -n "$LUMEN_PUBLIC_URL" ]; then
        return 0
    fi
    if state_done LUMEN_PUBLIC_URL 2>/dev/null; then
        export LUMEN_PUBLIC_URL=$(state_get LUMEN_PUBLIC_URL)
        echo "LUMEN_PUBLIC_URL desde state: ${LUMEN_PUBLIC_URL}"
        return 0
    fi
    local lb_host
    lb_host=$(get_load_balancer_host)
    if [ -n "$lb_host" ]; then
        export LUMEN_PUBLIC_URL="http://${lb_host}"
        echo "LUMEN_PUBLIC_URL desde LoadBalancer: ${LUMEN_PUBLIC_URL}"
        return 0
    fi
    return 1
}

wait_for_load_balancer_ip() {
    echo "Esperando IP del LoadBalancer (hasta ~6 min)..."
    local i=0
    local lb_host=""
    while [ "$i" -lt 36 ]; do
        lb_host=$(get_load_balancer_host)
        if [ -n "$lb_host" ]; then
            export LUMEN_PUBLIC_URL="http://${lb_host}"
            echo "IP asignada: ${LUMEN_PUBLIC_URL}"
            if command -v state_set &>/dev/null; then
                state_set LUMEN_PUBLIC_URL "$LUMEN_PUBLIC_URL" || true
            fi
            return 0
        fi
        sleep 10
        i=$((i + 1))
    done
    echo "Error: el LoadBalancer no obtuvo IP a tiempo."
    echo "  Revisa: kubectl get svc todolistapp-springboot-service -n mtdrworkshop"
    return 1
}

if ! kubectl get secret lumen-auth-secrets -n mtdrworkshop &>/dev/null; then
    echo "WARNING: secret lumen-auth-secrets missing in mtdrworkshop."
    echo "  kubectl create secret generic lumen-auth-secrets -n mtdrworkshop \\"
    echo "    --from-literal=better-auth-secret='...min-32-chars...' \\"
    echo "    --from-literal=invite-api-secret='...'"
    echo "  See backend/DEPLOY-AUTH.md"
fi

render_k8s_manifest() {
    local src=$1
    local dest=$2
    cp "$src" "$dest"
    sed -e "s|%DOCKER_REGISTRY%|${DOCKER_REGISTRY}|g" \
        -e "s|%IMAGE_VERSION%|${IMAGE_VERSION}|g" \
        -e "s|%LUMEN_PUBLIC_URL%|${LUMEN_PUBLIC_URL}|g" \
        -e "s|%TODO_PDB_NAME%|${TODO_PDB_NAME}|g" \
        -e "s|%OCI_REGION%|${OCI_REGION}|g" \
        -e "s|%UI_USERNAME%|${UI_USERNAME}|g" \
        "$dest" > "/tmp/$(basename "$dest")"
    mv -- "/tmp/$(basename "$dest")" "$dest"
}

kubectl_apply() {
    local file=$1
    if [ -z "$2" ]; then
        kubectl apply -f "$file" -n mtdrworkshop
    else
        kubectl apply -f <(istioctl kube-inject -f "$file") -n mtdrworkshop
    fi
}

export CURRENTTIME=$(date '+%F_%H:%M:%S')
echo "Deploying with IMAGE_VERSION=${IMAGE_VERSION}, CURRENTTIME=${CURRENTTIME}"

AUTH_YAML="$SCRIPT_DIR/todolistapp-auth-server-${CURRENTTIME}.yaml"
SPRING_YAML="$SCRIPT_DIR/todolistapp-springboot-${CURRENTTIME}.yaml"

BOOTSTRAP=false
if ! resolve_lumen_public_url; then
    BOOTSTRAP=true
    echo "Sin LUMEN_PUBLIC_URL: desplegando Spring primero para leer la IP del LoadBalancer."
    export LUMEN_PUBLIC_URL="http://bootstrap-pending"
    render_k8s_manifest src/main/resources/todolistapp-springboot.yaml "$SPRING_YAML"
    echo "Applying Spring Boot (fase bootstrap)..."
    kubectl_apply "$SPRING_YAML" "$1"
    unset LUMEN_PUBLIC_URL
    wait_for_load_balancer_ip
fi

echo "LUMEN_PUBLIC_URL=${LUMEN_PUBLIC_URL} (http://<IP> es correcto; no hace falta dominio)"

render_k8s_manifest src/main/resources/todolistapp-auth-server.yaml "$AUTH_YAML"
render_k8s_manifest src/main/resources/todolistapp-springboot.yaml "$SPRING_YAML"

echo "Applying auth-server (ClusterIP, interno)..."
kubectl_apply "$AUTH_YAML" "$1"

echo "Applying Spring Boot..."
kubectl_apply "$SPRING_YAML" "$1"

if [ "$BOOTSTRAP" = true ]; then
    echo "Primer deploy: abre la app en ${LUMEN_PUBLIC_URL}"
fi

echo "Deploy complete."
