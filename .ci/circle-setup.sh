#!/bin/bash

ps -ef | grep neo4j | grep java | grep -v grep | awk '{ print $2 }' | sudo xargs kill

#sudo sed -i.bak 's/auth_enabled=true/auth_enabled=false/' /var/lib/neo4j/conf/neo4j-server.properties
#sudo echo "dbms.pagecache.memory=100m" >>/var/lib/neo4j/conf/neo4j-server.properties#!/bin/bash

cd
curl 'https://neo4j.com/artifact.php?name=neo4j-community-3.1.3-unix.tar.gz' -o neo4j.tgz
tar zxvf neo4j.tgz
cd neo4j-community*

JAVA_HOME=$(find /usr/lib/jvm  -maxdepth 1 -type d -name 'jdk1.8*' | head -1)
PATH=$JAVA_HOME/bin:$PATH
export JAVA_HOME PATH

cat <<EOF >>./conf/neo4j.conf
dbms.security.auth_enabled=false
dbms.memory.pagecache.size=100m
EOF

./bin/neo4j start
