# Simple Ping App
 
## Configuration
The application properties can be configured from resources/application.properties

## Services
There are three main services who are checking for connection:
1. ICMP Ping Service
2. TCP Ping Service
3. Trace Route Ping Service

We also have the scheduler which decides when the pings should be sent.

And a reporter which handles sending the reports.

## Logs
Logs are saved in logs/application.log
Logging behaviour can be configured from resources/logback.xml