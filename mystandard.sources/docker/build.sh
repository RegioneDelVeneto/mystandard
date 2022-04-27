#!/bin/sh

set -e


cd `dirname $0`

ROOT_PATH=`pwd`


echo "\n################################################################################"
echo "building __YOUR_DATA__/rve-mystandard ..."
echo "################################################################################\n"



docker build -f dockerfile_mod --build-arg http_proxy="${http_proxy}" --build-arg https_proxy="${https_proxy}" --build-arg MYPLACE_VERSION="${MYPLACE_VERSION}" --force-rm -t "__YOUR_DATA__/mystandard:0.0.1" .
