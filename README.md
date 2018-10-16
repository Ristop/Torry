# XTorrent

## Glossary

. . .

## Description

This repository contains an implementation of BitTorrent specification (https://wiki.theory.org/index.php/BitTorrentSpecification).

### Tracker


### Client

## Building the project

In order to build the project you need to clone https://github.com/Ristop/torrent-parser and run `mvn clean install` on torrent-parser project. After this you can build XTorrent by invoking `mvn clean package` on XTorrent project.

## Running the project

Requires Java 8

### Tracker
* `java -jar tracker/target/xtorrent-tracker-*.jar`
### Client
* `java -jar client/target/xtorrent-client-*.jar`