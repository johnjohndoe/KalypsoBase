Example: jaxr-publish

This example demonstrates how one can use the JAXR APIs to publish data to a 
registry. In this example, we demonstrate how to publish an organization's 
information to the registry, including information about services provided by 
the organization and contact information. The example also illustrates how to
classify registry entries using well-known classification schemes like the 
North American Industry Classification System (NAICS) and standard identifiers 
like the Dun & Bradstreet DUNS identifier.

Configuration:
--------------
Before running the example, you need to change the publish.properties as follows:
1. If you are running the example from behind a firewall, please add the proxy
   information:
       http.proxy.host 
       http.proxy.port
       
2. You can publish to any registry you wish, by
   setting the following properties in the publish.properties:
       query.url : the URL for sending queries to the registries
       publish.url : the URL for publishing data to the registries
   (The URLs for the public registries can be found here:
       http://www.uddi.org/find.html 
    Note: To publish data to any of the public registries, you need to register
          first with the registry provider.)
       
3. Next, you need to provide the username and password for publishing to the 
   registry. To do this, set the user.name and user.password  variables 
   
   For the public registries, you can obtain this data during the registration 
   process mentioned in Step 2. 
   
Running the sample:
-------------------
On Windows systems, at the prompt, type .\BLCM
On Unix systems, at the prompt, type ./BLCM.sh

Results:
--------
You should see the following message for a successful run:
Organization Saved
[Please ignore the warning messages about PostalAddressMappings]

   
   
