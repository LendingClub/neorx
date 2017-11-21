#!/bin/bash

THIS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

IMAGE_NAME=lendingclub/neo4j-build-env:latest

# If we have the source available for this, go ahead and rebuild the container
if [ -f "$THIS_DIR/../neo4j-build-env/Dockerfile" ]; then
    $THIS_DIR/../neo4j-build-env/build.sh
fi
docker pull $IMAGE_NAME

docker run -v $(pwd):/build -it ${IMAGE_NAME} ./gradlew clean check build

