#!/usr/bin/env sh
docker build -t balpoi/bal-server ./ \
&& docker push balpoi/bal-server
