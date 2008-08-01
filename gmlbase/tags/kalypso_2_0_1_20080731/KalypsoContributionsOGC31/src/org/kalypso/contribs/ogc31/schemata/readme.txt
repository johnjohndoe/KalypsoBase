IMPORTANT NOTE:

the schemata should be located in the source tree because they need to be included
in the library jar created by eclipse once deployed.

this way, the URL mechanisms can be used and the catalog class is able to find
the schemata within the jar archive (it cannot look outside of it). 