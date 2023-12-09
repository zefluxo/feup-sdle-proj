# shellcheck disable=SC2145
mvn -q compile exec:java -Dexec.args="$@"
#docker compose -f src/docker/docker-compose.yaml up

# mvn -q exec:java -Dexec.args="incItem -id=8D6B478B2A5B13EF153B52A5A3C7680B --itemName=arroz --itemQuantity=3"
# ./run.sh "incItem -id=8D6B478B2A5B13EF153B52A5A3C7680B --itemName=arroz --itemQuantity=3"
