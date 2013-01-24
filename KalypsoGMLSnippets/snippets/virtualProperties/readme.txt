Examples of virtual properties:

1) A function property i.e. a 'real' property backed up by a function
A feature that has a property that is always calculated as the difference of two other properties.

The function property is still a 'real' property of the feature and is written to the gml file.

2) A pruely virtual property

The same property is now ommitted from the gml-application-schema, but still defined as a function property.

This leas to the following result:
- in memory the property still exists and is shown to the user
- in the gml file, the virtual property is not visible (i.e. it is not written into the file) 