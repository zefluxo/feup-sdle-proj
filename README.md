# Shopping List - SDLE

For now, just a "archetype" with working load balancer, with a simple ZeroMQ REQ/REP app

- Needs docker installed (tested with docker desktop on widows v4.25.1)
- NEED jdk 17+ and maven installed

Video and presentation can be found in the [docs](https://github.com/zefluxo/feup-sdle-proj/tree/main/docs) folder.

### CRDT
```
cd crdt
mvn clean install
```
### Cloud

```
cd cloud

mvn clean package
docker build -t cloud-node -f src/main/docker/Dockerfile.jvm .
docker compose -p sdle -f src/main/docker/docker-compose.yaml up
```

### Client

on separate terminal

``` 
cd client
mvn clean package

# Client command line examples
# criar nova lista
# linux
./runClient.sh "getList EC9DA33DBC159D8B7ECDF7898409BB41"
# windows
.\runClient.cmd getList EC9DA33DBC159D8B7ECDF7898409BB41

# adicionar/incrementar items 
# linux
./runClient.sh "incItem EC9DA33DBC159D8B7ECDF7898409BB41 feijao 1"
# windows
.\runClient.cmd incItem EC9DA33DBC159D8B7ECDF7898409BB41 feijao 1

# diminuir quantidade de um determinado item (caso o resultado seja negativo, fica 0) 
# linux
./runClient.sh "decItem EC9DA33DBC159D8B7ECDF7898409BB41 arroz 2"
# windows
.\runClient.cmd decItem EC9DA33DBC159D8B7ECDF7898409BB41 feijao 1

# apagar item de uma lista
./runClient.sh "deleteItem D573D4EB5D7A9539427D6483A3DBB546 arroz" 
# windows
.\runClient.cmd deleteItem D573D4EB5D7A9539427D6483A3DBB546 arroz

# apagar uma lista
./runClient.sh deleteList
# windows
.\runClient.cmd deleteList

# mostrar todas as listas que localmente armazenadas na pasta "data" (apos tentativa de sincronização com a cloud)
./runClient.sh allLists 
# windows
.\runClient.cmd allLists


all client commands can be do by calling mvn, eg:
mvn -q compile exec:java -Dexec.args="decItem EC9DA33DBC159D8B7ECDF7898409BB41 arroz 2"

```

#### Para simular outro cliente, pode-se copiar a pasta "client" para outro local e executar os mesmo comandos 

 