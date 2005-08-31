Description
-----------
This sample implements a SOAPProcessor which reads the headers of an incoming 
SOAPMessage according to the SOAP 1.1 Processing Model and gives the header to 
a Targetted SOAPRecipient for processing. A Targetted SOAP Recipient is a recipient 
whose  role attribute matches the role of the particular header-element.

The SOAPProcessor also provides a mechanism to register SOAPAnnotator objects with it. 
The SOAPAnnotators are called when an Outgoing message is being prepared .

The purpose of this sample is to showcase how SOAP Processing model can be implemented.

