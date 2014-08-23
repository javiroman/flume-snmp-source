#!/bin/bash

pushd ../target/test-classes
sudo java -cp .:${HOME}/.m2/repository/org/snmp4j/snmp4j/1.10.1/snmp4j-1.10.1.jar testSNMPTrap
popd
