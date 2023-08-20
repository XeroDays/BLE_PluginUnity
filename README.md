# BLE Bluetooth Low Energy Plugin

## Introduction: 

One significant aspect of the merging mode of ubiquitous computing is the constantly changing execution environment. This work presents an experimental context-aware application that provides context-aware information to game servers and game players in a mobile distributed computing environment. Game players might access their computing resources from wireless portable machines and also through stationary devices and computers connected to local area networks. The hardware configuration is continually changing. Similarly, wireless portable machines may move from one location to another, requiring users to join or leave games, frequently interacting with other machines while changing location. One challenge for game development using mobile Bluetooth-based distributed computing is to exploit the changing environment with a class of game servers that are aware of the context. Such context-aware software adapts according to the location, the collection of nearby players, hosts, and accessible devices, as well as changes in the runtime environment over time.
 
**History**

In multiplayer gaming a user must need an internet connection or any LAN device such as Wi-Fi or mobile broadband connection, which is costly and difficult to be provided in some cases, not everyone has access to the LAN devices or internet

  
## Project summary 

In this gaming era multiplayer gaming is now trending still no one has ease of access to the internet also gaming via the internet or LAN requires lots of configuration and is time-consuming thus we came up with the solution of connecting the user with a single click and playing game with synchronous data transfer based on Bluetooth low energy. No LAN would be required and no internet would be required, two users can play the multiplayer game by single tab, one user will create a server, and the second user will search for all the currently active servers and will auto connect to the server based on the key to make it specific for each user. Bluetooth is a simple and easy way of communication, no big effort is required with Bluetooth, Also Bluetooth Low energy feature gives the advantage of low power consumption as well as great performance such as low latency, and high data transfer rate of up to 80~100kb per second which is efficient for multiplayer gaming


## Project Details Architecture & Environment 

Our project architecture is based on Bluetooth PAN Architecture and work will only be done in the application layer, application will have a controller responsible for all sending and receiving packets. 

![image](https://github.com/XeroDays/BLE_PluginUnity/assets/38852291/200581cc-6596-4176-8037-6b3aad7e9357)

Bluetooth protocol stack is followed below explaining different Bluetooth protocol layers and how they merge with other layers.

![image](https://github.com/XeroDays/BLE_PluginUnity/assets/38852291/be498768-9304-4b21-8cc3-04e54213aa5b)


**Software & Tools**
1.	Unity 3D: This will be used for game development and the interface for controlling the Bluetooth Plugin.
2.	Visual Studio: This IDE is used to code in Unity and create source files
3.	Blender: used to create graphic 3d designs for games.


## Language
1.	C#: Will be used to code in Unity.
2.	Java : Will be used to design plugin
3.	C++: will be used to communicate with Bluetooth Hardware


### Protocol

1. Bluetooth Low Energy Protocol


### Permissions

1.	Bluetooth
2.	BLUETOOTH_Admin
3.	Access Network State



## Project Conclusion

Mobile technology is improving day by day, Mobile users are more than PC users, and is now currently Mobile gaming is in trending, the industry of multiplayer gaming of mobile technology is in trend, we came up with the idea of creating a low-budget link between devices rather than using any local Wi-Fi LAN or internet WAN connectivity, and using mobile own Personal area network (PAN), this technology will connect two devices connect with each other and will be able to exchange synchronous data for games, the user will be able to play multiplayer game with a single tap. We will be using Unity to design an app with C# language and Plugin will be designed in a given supported language such as Java or C++.
