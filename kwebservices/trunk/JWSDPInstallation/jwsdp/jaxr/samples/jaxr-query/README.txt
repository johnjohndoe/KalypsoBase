Example: jaxr-query

This example demonstrates how one can use the JAXR APIs to query a registry for
data. In this example, we demonstrate how to query for an organization by name.
The example also illustrates how to parse the retrieved information for details
about the organization and the services it provides.

Configuration:
--------------
Before running the example, you need to change the query.properties as follows:

1. If you are running the example from behind a firewall, please add the proxy
   information:
       http.proxy.host 
       http.proxy.port

2. You can query any registry you wish, by setting the
       query.url : the URL for sending queries to the registries
   (The URLs for the public registries can be found here:
       http://www.uddi.org/find.html )
        
3. You can edit the pattern being searched for, by editing the settings for the
   "company" variable. For example, to search for the organization published 
   in the jaxr-publish sample, you could replace %USA% by USA-Works
   
Running the sample:
-------------------
On Windows systems, at the prompt, type .\BQM
On Unix systems, at the prompt, type ./BQM.sh

Results:
--------
You should see the following message for a successful run:
"Successfully queried the registry for organizatons matching ...."
followed by information about the registry entries retrieved.
   
   
