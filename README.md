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

### Client command line examples

``` 
mvn clean package

# criar nova lista
java -jar .\target\client-1.0-SNAPSHOT.one-jar.jar new  

# adicionar/incrementar novo item 
java -jar .\target\client-1.0-SNAPSHOT.one-jar.jar incItem -id 9CF0689CE73223EFE490A5089A04BFE4 -n=arroz -q=2 

# diminuir quatidade de um determinado item (em caso do resultado fosse negativo, fica 0) 
java -jar .\target\client-1.0-SNAPSHOT.one-jar.jar decItem -id 9CF0689CE73223EFE490A5089A04BFE4 -n=arroz -q=1 

# lista todos as listas (as listas ficam localmente gravadas na pasta "data")
java -jar .\target\client-1.0-SNAPSHOT.one-jar.jar all 

```

### KNOW ISSUES

###

 