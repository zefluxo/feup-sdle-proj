# Shopping List - SDLE

For now, just a "archetype" with working load balancer, with a simple ZeroMQ REQ/REP app)

- Needs docker installed (tested with docker desktop on widows v4.25.1)
- for running without docker, need jdk 17+ and maven installed

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

### Client command line examples

```
java -jar target/client-1.0-SNAPSHOT.one-jar.jar 127.0.0.1 putList
java -jar target/client-1.0-SNAPSHOT.one-jar.jar 127.0.0.1 getList
java -jar target/client-1.0-SNAPSHOT.one-jar.jar 127.0.0.1 putItem <listHash> <itemName> <quantity>
```

### KNOW ISSUES

- concurrency problems (some will be resolved with CRDT implementation)
- occasional ZeroMQ connecting problems not been proper treated
- need a heartbeat mechanism
- replication: need a proper evaluation, maybe will not needed, after CRDT are implemented

 