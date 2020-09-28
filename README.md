# Easy Status Reporter
Easy Status Reporter, or ESR for short, is lightweight monitoring tool for your services, whether they're on a single home
server on a cluster of multiple nodes.

The tool will monitor the specified endpoints and report to you when there are problems. It also provides a web server
serving a single page with an overview of all your services.

You can find example screenshots [here](https://github.com/femastudios/easy-status-reporter/tree/master/screenshots).

# Installation
## Run with docker
Simply pull the image `femastudios/easy-status-reporter` and give a few parameters. Example using docker compose:
```yaml
version: "2"
services:
  easy-status-reporter:
    image: femastudios/easy-status-reporter
    container_name: easy-status-reporter
    volumes:
      - /path/to/your/config:/config
    ports:
      - 7828:7828 # or whatever port you're using
    restart: unless-stopped
```
## Run standalone
Make sure to have a JRE with at least version 13, download the .jar file and run it with the command `java -jar esr.jar`. 
You can change the config path by passing a VM option like so: `java -Dcom.femastudios.esr.configDir=/path/to/config -jar esr.jar`.
Then you can create a service that runs this command based on your OS.

# Configuration
To configure ESR, create a file named `config.yml` in the config directory. You can find a minimal example 
[here](https://github.com/femastudios/easy-status-reporter/blob/master/config-examples/config-example-minimal.yml).
If you want to go more in depth about the config file, take a look at the 
[full documented example](https://github.com/femastudios/easy-status-reporter/blob/master/config-examples/config-example-full.yml).

All expressions in the config are parsed using JEXL. 
For more information about its syntax please see [this page](https://commons.apache.org/proper/commons-jexl/reference/syntax.html). 

Last but not least: this software only opens a simple HTTP web server (no TLS/SSL). It is highly recommended running it 
behind a [reverse proxy](https://en.wikipedia.org/wiki/Reverse_proxy), like [Traefik](https://doc.traefik.io/traefik/v2.3/getting-started/quick-start/) 
or [Nginx](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/), that takes care of HTTPS.
For this task you can use easy to use docker containers such as [linuxserver/letsencrypt](https://docs.linuxserver.io/images/docker-letsencrypt).