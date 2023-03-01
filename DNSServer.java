/* Spring 2023 CSci4211: Introduction to Computer Networks
** This program serves as the server of DNS query.
** Written in Java. */

/*
 * DNSServer.java
 * This file creates a DNS Server which will service DNS request from a 
 * client that provides a hostname. It has a cache that will store a request's
 * hostname, address pair and writes it to a file in named "DNS_Mapping.txt".
 * This Server also creates a CSV file that logs a client's every request to 
 * a file named "dns-server-log.csv"
 * 
 * Modified By: Rigo Sandoval
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Random;
import java.util.Scanner;
import java.io.FileWriter;

/* 
 * DNSENtry class that stores a String hostname and an String ArrayList
 * this way a hostname can hold many ip addresses.
*/
class DNSEntry {
	String hostname;
	ArrayList<String> ipaddresses = new ArrayList<String>();

	public DNSEntry(String hostname, String ipaddress) {
		this.hostname = hostname;
		ipaddresses.add(ipaddress);
	}
	//adds specified ipaddress to hostname's ipaddress ArrayList
	public void addIPaddress(String ipaddress) {
		ipaddresses.add(ipaddress);
	}
	//print entry function for debugging purposes
	public void printEntry() {
		System.out.println(hostname + ":\t" + ipaddresses.get(0));
		for(int i=1; i<ipaddresses.size(); i++) {
			System.out.println("\t" + ipaddresses.get(i));
		}
	}
	
}

class DNSServer {

	public static String cacheFile = "DNS_mapping.txt";
	public static String csvLogFile = "dns-server-log.csv";

	//ArrayList will be used for local DNS cache.
	//DNSEntry object allows for a hostname entry to have multiple IP addresses
	public static ArrayList<DNSEntry> cache = new ArrayList<DNSEntry>();
	//temporary cache for checking if cacheFile is up to date
	public static ArrayList<DNSEntry> tempCache = new ArrayList<DNSEntry>();
	
	public static void main(String[] args) throws Exception {
		int port = 9889;
		ServerSocket sSock = null;

		try {
			sSock = new ServerSocket(port); // Try to open server socket 5001.
		} catch (Exception e) {
			System.out.println("Error: cannot open socket");
			System.exit(1); // Handle exceptions.
		}

		System.out.println("Server is listening...");
		new monitorQuit().start(); // Start a new thread to monitor exit signal.

		while (true) {
			new dnsQuery(sSock.accept()).start();
		}

	}
	// function to print the DNS cache for debugging purposess
	public static void printLocalCache(){
		System.out.println("");
		System.out.println("Contents-of-localCache:");
		for(DNSEntry entry : DNSServer.cache) {
			entry.printEntry();
		}
		System.out.println("");
	}
}

class dnsQuery extends Thread {
	Socket sSock = null;

    dnsQuery(Socket sSock) {
    	this.sSock = sSock;
    }


    public String IPselection(String[] ipList){
    //checking the number of IP addresses in the cache
	//if there is only one IP address, return the IP address
	//if there are multiple IP addresses, select one and return.
	////optional: return the IP address according to the Ping value for better performance (lower latency)
		
		//returns the first ip address in the ArrayList associated with a hostname;
		if(ipList.length == 1) {
			return ipList[0];
		} else {
			return ipList[0];
		}
    }

	@Override public void run(){
		BufferedReader inputStream;
        PrintWriter outStream;

        try {
		//Open an input stream and an output stream for the socket
		//Read requested query from socket input stream
		//Parse input from the input stream
		//Check the requested query

			//Output Stream
			//Input Stream
	    	PrintWriter sendOut = new PrintWriter(sSock.getOutputStream(), true);
	    	BufferedReader readIn = new BufferedReader(
	    	new InputStreamReader(sSock.getInputStream()));

			//Read requested query from client
			String request = readIn.readLine();
			System.out.println(request);

			//Parsing query
			
			//Check the requested query
            boolean hostFound = false;
            try {
			//check the DNS_mapping.txt to see if the host name exists
			//set local file cache to predetermined file.
            //create file if it doesn't exist 

				//Open file that Scanner object will use
				//will create a new file if one doesn't exist
				File localCache = new File(DNSServer.cacheFile);
				if(!localCache.exists()) {
					localCache.createNewFile();
				}

				//Scanner will scan in every hostname ipaddress pair into the localCache
				Scanner scanIn = new Scanner(localCache);
				while(scanIn.hasNext()) {					
					try {
						//scan in hostname ipaddress pair
						DNSEntry temp = new DNSEntry(scanIn.next(), scanIn.next());
						boolean duplicateHost = false;

						//check if scanned hostname already exists in cache
						for(DNSEntry ele : DNSServer.cache) {					
							if(ele.hostname.equals(temp.hostname)) {
								duplicateHost = true;
								//if it does, check if hostname entry already contains scanned ipaddress
								if(!ele.ipaddresses.contains(temp.ipaddresses.get(0))) {
									//if it doesn't add it to the ipaddresses ArrayList
									ele.addIPaddress(temp.ipaddresses.get(0));
								}
								break;
							}
						}
						if(!duplicateHost) {
							DNSServer.cache.add(temp);
						}
					} catch (Exception e) {
						e.printStackTrace();
            			System.err.println("File opens ends on an empty line!\n" + e);
					}
				}
				scanIn.close();
				//DNSServer.printLocalCache();
				
			//if it does exist, read the file line by line to look for a
            //match with the query sent from the client
            //If match, use the entry in cache.
            //However, we may get multiple IP addresses in cache, so call IPselection to select one. 
			//If no lines match, query the local machine DNS lookup to get the IP resolution
			//write the response in DNS_mapping.txt
				
				String host = request;
				String ip = "";
				String resolved = "";

				//look for a match for the requested hostname in the cache
				for(DNSEntry entry : DNSServer.cache) {
					//if there is a match, return IP address
					if(entry.hostname.equals(request)) {
						hostFound = true;
						ip = IPselection(entry.ipaddresses.toArray(new String[entry.ipaddresses.size()]));
						resolved = "CACHE";
						break;
					}
				}
				//if no match, try to do a DNS lookup given the requested hostname
				if(!hostFound) {
					try {
						//dnsLookup will return: hostname/ipaddress
						//string so use a Scanner object with "/" delimiter
						//to seperate hostname and ipaddress
						InetAddress dnsLookup = InetAddress.getByName(request);
						Scanner dnsScan = new Scanner(dnsLookup.toString()).useDelimiter("/");
						hostFound = true;

						host = dnsScan.next();		// host = hostname
						ip = dnsScan.next();		// ip = ipaddress

						//write hostname and ipaddress to DNS_mapping.txt
						BufferedWriter localCacheBW = new BufferedWriter( new FileWriter(localCache, true));
						localCacheBW.append("\n" + host + " " + ip);
						localCacheBW.close();

					} catch (Exception e) {

					}
					if(!hostFound) {
						System.out.println("requested hostname does not exist");
						ip = "AddressNotFound";
					}
					//write hostname and ipaddress to local cache
					DNSServer.cache.add(new DNSEntry(host, ip));
					//request resolved by API
					resolved = "API";
				}

				//DNSServer.printLocalCache();

				//create File object for writing to "dns-server-log.csv"
				File dnsLog = new File(DNSServer.csvLogFile);
				//create "dns-server-log.csv" if it doesn't exist
				if(!dnsLog.exists()) {
					dnsLog.createNewFile();
				}
				//use BufferedWriter for more efficient file writes
				BufferedWriter dnsLogCSV = new BufferedWriter( new FileWriter(dnsLog, true));

				//append hostname, ipaddress, and resolved format into csv file
				//flush and close the BufferedWriter
				dnsLogCSV.append(host + "," + ip + "," + resolved + "\n");
				dnsLogCSV.flush();
				dnsLogCSV.close();

			//print response to the terminal
			//send the response back to the client
			//Close the server socket.
				System.out.println(host + ":" + ip + ":" + resolved);
				sendOut.println(host + ":" + ip + ":" + resolved);
				sSock.close();
            
            } catch (Exception e) {
                System.out.println("exception: " + e);
            }
			//Close the input and output streams.
			sendOut.close();
        	readIn.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Host not found.\n" + e);
        }
	}
}

class monitorQuit extends Thread {
	@Override
	public void run() {
		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(System.in)); // Get input from user.
		String st = null;
		while(true){
			try{
				st = inFromClient.readLine();
			} catch (IOException e) {
			}
            if(st.equalsIgnoreCase("exit")){
                System.exit(0);
            }
        }
	}
}
