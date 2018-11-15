#!/bin/sh

docker pull swaggerapi/swagger-ui
PWD=`pwd`
echo $PWD
docker run -p 81:8080 -e SWAGGER_JSON=/api-doc/openapi.json -v $PWD/api-doc:/api-doc swaggerapi/swagger-ui
