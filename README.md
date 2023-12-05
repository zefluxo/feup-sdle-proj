# Shopping List - SDLE

For now, just a "archetype" with working load balancer, with a simple ZeroMQ REQ/REP app)

- Needs docker installed (tested with docker desktop on widows v4.25.1)
- NEED jdk 17+ and maven installed

### CRDT
```
cd crdt
mvn clean install
```
### Cloud

```
cd cloud
./build.sh
./run.sh
```

### Client

on separate terminal

```
cd client
./build.sh
./run.sh
```

### Client command line examples (# only with ZeroMQ version of cloud)

```
java -jar target/client-1.0-SNAPSHOT.one-jar.jar tcp://127.0.0.1:7788 putList
java -jar target/client-1.0-SNAPSHOT.one-jar.jar tcp://127.0.0.1:7788 getList
java -jar target/client-1.0-SNAPSHOT.one-jar.jar tcp://127.0.0.1:7788 putItem <listHash> <itemName> <quantity>
```

### KNOW ISSUES

###

 