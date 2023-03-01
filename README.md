## DNS Server Code

Rigo Sandoval, CSCI4211S23, 3/1/2023
Java, DNSServer, DNSServer.class

## Compilation
1. Download files DNSServer.java and DNSClient.java in the same directory
3. Navigate to directory using the command line
4. Run "javac DNSServer.java DNSClient.java" in the command line

## Execution/Running
1. Run "java DNSServer" in the command line
2. Open another command line terminal to run DNSClient.java
3. Navigate to directory DNSClient.java is saved in
4. Run "java DNSClient"
5. Read instructions given by DNSClient.java program

## Description
This Java code uses the ServerSocket class to create a DNS Server on port 9889 and waits for a client to connect to it using the same port. It will service a DNS request from the client given the client provides a hostname. The DNS Server code will then check it's cache to see if the hostname already exists there. If it does, it returns the relevant IP address. If it doesn't, it uses the InetAddress method getByName(String) to do a DNS lookup and return the hostname's IP address. Once, the IP address is found, it sends the IP address as a String type back to the client completing it's Domain Name Service Request

### Cache Implementation
This code uses an ArrayList to implement the cache where it stores the clients requests. The ArrayList uses a data structure called DNSEntry for it's entries in the list. The DNSEntry structure is comprised of a String called hostname and an ArrayList of type String ipaddresses which stores the IP addresses relevent to the hostname. This allows for every hostname in the cache to have multiple IP addresses.

### Saving to Files
In addition to sending the client an IP address in response to a hostname request, this Java code saves it's cache contents to a .txt file named "DNS_mapping.txt". If no such file exists in the directory, it will create one and store it's cache contents there. It does this each time it services a request which I have sinced learned is a very inefficient, since reading and writing to files is a very costly operation.

It also saves a log of every hostname DNS request from the client into a .csv file named "dns-server-log.csv". Again, if no such file exists in the directory, it creates one and saves it's logs there.
