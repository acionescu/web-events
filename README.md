#Description

This is an extension to the [even-bus framework](https://github.com/acionescu/event-bus), that allows the transmission of events over websockets

It provides server side and client side customizable endpoints for events passing. 



##Bridging events between remote peers

Server endpoint < - > Server node

Client endpoint < - > Client node



#Secured connection

Allow endpoints to exchange events in a secure manner.

If we want to add, besides security, authentication, then PKI emerges as a possible solution.

A node may specify if it accepts only secured connections or it also allows plain connections. Here we're not talking about the secure http connection, but about an additional layer of security at the application level.

A node may also specify if it accepts anonymous connections too or only connections from known nodes


Endpoint - handles communication specific to a channel
Local node - a local representation of a remote node


