# Torry

## Description

This repository contains an implementation of BitTorrent specification (https://wiki.theory.org/index.php/BitTorrentSpecification) with some adjustments.

Project documentation can be found [here](https://www.overleaf.com/16481102jgzcbwmyykyr).

## Building the project

In order to build the project you need to clone https://github.com/Ristop/torrent-parser and run `mvn clean install` on torrent-parser project. After this you can build Torry by invoking `mvn clean package` on Torry project.

## Running the project

Requires Java 8

### Tracker
* `java -jar tracker/target/tracker-*.jar`
### Client
* Set up `application.conf` file. Sample of this file can be found from `client/conf/application.conf.example`.
* `java -Dconfig.file=/path/to/application.conf -jar client/target/client-*.jar`
