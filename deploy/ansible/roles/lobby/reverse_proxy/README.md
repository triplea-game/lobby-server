## reverse-proxy

NGINX sits in front of lobby and is exposed to the internet. The lobby is 'behind' NGINX. 
This reverse proxy config provides two key benefits:
1) HTTPS endpoint, then communicates to lobby via HTTP
2) port forwarding based on triplea-version HTTP header. We can then run
   multiple lobbies that are different by port, partitioned by game version.


