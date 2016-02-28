#!/bin/bash

sudo sed -i.bak 's/auth_enabled=true/auth_enabled=false/' /var/lib/neo4j/conf/neo4j-server.properties
sudo echo "dbms.pagecache.memory=100m" >>/var/lib/neo4j/conf/neo4j-server.properties