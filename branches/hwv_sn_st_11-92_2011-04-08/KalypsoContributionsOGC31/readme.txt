Contributions to OGC XML Schemas

Location:
the OGC XML Schemas are located in the source tree so that they get deployed together
with the other source artifacts in the jar created by eclipse on plugin export.

UrlCatalog:
there is an UrlCatalog for the schemas (see source code).

Binding:
- there is a specific builder that creates the binding classes over the schemas. The classes are
generated in the srcbind source tree directory (not in cvs).
- schemas are transformed before generation (directory is etc/schemas/generated)