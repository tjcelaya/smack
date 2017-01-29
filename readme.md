# minismack


## running the cluster

`docker-compose up cassandra`

## connecting to cassandra

`docker-compose run --rm cqlsh`

## build docker container for spark app

`sbt docker:publishLocal`
