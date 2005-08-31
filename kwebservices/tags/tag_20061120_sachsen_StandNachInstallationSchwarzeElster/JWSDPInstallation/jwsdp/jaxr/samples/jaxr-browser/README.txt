Example: jaxr-browser

This is a more sophisticated example showing how to use the JAXR APIs to
provide a common interface to heterogenous registries. The registry interactions
using the JAXR APIs are all illustrated in the JAXRClient source code.

Configuration:
--------------
If you are running the browser behind a firewall, specify the command line
arguments as shown in the section "Running the sample"

Running the sample:
-------------------
The scripts for starting the browser are located under:
${jwsdp.home}/jaxr/bin

1. Use the following commands if you are not running the samples from behind
   a firewall.
   On Windows systems:
      .\jaxr-browser
   On Unix systems:
      ./jaxr-browser.sh
2. If you are running these samples from behind the firewall add the following
   arguments to specify proxy information
      ./jaxr-browser.sh <proxy-host> <proxy-port>
   or
      ./jaxr-browser.sh <http-proxy-host> <http-proxy-port> \
                        <https-proxy-host> <https-proxy-port>
   [Note: 1. The entire command should be typed on one line. The \ at the end of
          the first line is only used here to indicate continuation.
          2. For Windows systems, use .\jaxr-browser instead of 
          ./jaxr-browser.sh]
          
Using the JAXR Browser:
-----------------------
1. Select a Registry Location URL (Use inquiry URLs for querying the registry,
   and publishing URLs for publishing to the registry)
   Several of the commonly used URLs are pre-configured in the list, but you can
   also specify your own here.
2. To query the registry select the Browse panel, add the information and click 
   on "Search".
3. To publish information to the registry, select the Submissions Panel. Make
   sure you have the publishing URL selected for the Registry Location. Fill in
   the information to publish and click on "Submit". It brings up an 
   authentication dialog. Type in the username and password for your account on
   the registry and click on "OK" to publish the entry to the registry.
4. For complete details on the browser usage, refer to the JWSDP tutorial.
   
   