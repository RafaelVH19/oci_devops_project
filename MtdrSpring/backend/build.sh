#!/bin/bash
set -e

TAG=$(git rev-parse --short HEAD)
export IMAGE_VERSION=$TAG

if [ -z "$DOCKER_REGISTRY" ]; then
    export DOCKER_REGISTRY=$(state_get DOCKER_REGISTRY)
    echo "DOCKER_REGISTRY set."
fi
if [ -z "$DOCKER_REGISTRY" ]; then
    echo "Error: DOCKER_REGISTRY env variable needs to be set!"
    exit 1
fi

echo "Building images with tag: ${IMAGE_VERSION}"

# Spring Boot
export IMAGE_NAME=todolistapp-springboot
export IMAGE=${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_VERSION}

mvn clean package spring-boot:repackage
docker build -f Dockerfile -t "$IMAGE" .
docker push "$IMAGE"
if [ $? -eq 0 ]; then
    docker rmi "$IMAGE" || true
fi

# Better Auth (internal service)
AUTH_IMAGE=${DOCKER_REGISTRY}/lumen-auth-server:${IMAGE_VERSION}
docker build -t "$AUTH_IMAGE" ./auth-server
docker push "$AUTH_IMAGE"
if [ $? -eq 0 ]; then
    docker rmi "$AUTH_IMAGE" || true
fi

echo "Pushed ${IMAGE} and ${AUTH_IMAGE}"
