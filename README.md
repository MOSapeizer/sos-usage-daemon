# SOS-Usage-daemon


For some reasons, I have to statistic connections of SOS.

This daemon simply use [pcap lib] to caputre packets and filter only *on port 8080*	
It'll save the http request *method* and *URI*, *ip*, *connection timestamp* in Mysql	
So you have to create database named of sos_usage.

there is the DB outline.

	+----+----------------+--------+--------------------------+---------------------+
	| id | ip             | method | url                      | ts                  |
	+----+----------------+--------+--------------------------+---------------------+
	|  1 | 127.100.100.1  | GET    | /twed_waterLevel/service | 2016-07-20 15:15:21 |
	|  2 | 120.163.20.11  | GET    | /twed_waterLevel/service | 2016-07-20 17:21:36 |
	|  3 | 192.168.0.1    | GET    | /twed_waterLevel/service | 2016-07-20 17:21:46 |
	+----+----------------+--------+--------------------------+---------------------+

[pcap lib]: http://example.com/  "Optional Title Here"

# installation

I'm too lazy to speak.

# Future work

1. generate JSON
2. integrate with a tomcat webservice
3. can show barchart or something else.