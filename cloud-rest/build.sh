mvn clean package
docker build -t cloud-node -f src/main/docker/Dockerfile.jvm .