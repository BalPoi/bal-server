#!/usr/bin/env sh
echo 'Building image:' && \
docker build -t balpoi/bal-server ./ && \

echo 'Pushing image:' && \
docker push balpoi/bal-server && \

echo 'Deleting old deployment in k8s:' && \
kubectl delete deployment bal-server
