# Smart-Helmet
 Implementation of Smart Helmet based on IoT Technology

 ## Abstract

    Nowadays most of scooter riders navigate by cellphones and place it on the smart phone holder, but it is easy for the driver to distract causing an accident. 
    Due to the reason, our research design a smart helmet to display navigation instruction in rider’s sight and improve driving safety. 
    With the concept of IoT, our research connect android App with google data base to access navigation information. After getting the information, App sends it to Arduino nano. 
    As Arduino nano receive the information, it will display it at the OLED module. 
    In addition, alcohol sensing and accident sensing are added to prevent drunk driving and delayed first aid which establishs a more complete security.

## Procedure

1. Open App in an android phone
2. Connect to Arduino automatically
3. Input destination and emergency phone number
4. Press '導航'(Navigate button)

## Demo
![preview](https://i.imgur.com/8YbgVLa.jpg)
![preview](https://i.imgur.com/poEH3Mz.jpg)
![preview](https://imgur.com/EpU3ZYv.jpg)
![preview](https://imgur.com/qGEbeBE.jpg)

## module

![preview](https://i.imgur.com/8O3KjVG.png)

1. Communication module

    Use Arduino bluetooth gadget 'HC-06' to communicate between Android app and Arduino nano

2. Drunk driving detect module

    Put Arduino gadget 'MQ-3' in the helmet to detect whether driver is drunk. If the driver is drunk shut down the APP.

3. Navigation module

    Put http request to Google map API return map and parse the returning json file to draw route on the map in mobile phone APP. Send data to arduino nano through bluetooth.

4. Display module

    receive data from mobile phone and show it on Arduino gadget 'OLED screen'.

5. Collision detect module

    Use arduino gadget to dectect the three direction's acceleration and detect whether the driver has an accident or not. If yes the app will call emergency phone number automatically.

6. Control module

    Use arduino nano as the core to compute data from each module.



