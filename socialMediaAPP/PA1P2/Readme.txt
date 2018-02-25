Programming Assignment Part 2 
 
Implementing a Broadcast Status Update Using Multi-threading

Tian Zhang
U31287117

1. Description:

For this part, you will use multi-threading to implement a social media app with one server and
multiple clients. We assume here that all clients are interested in getting notified of any status
update posted by any one of them. The clients and server will communicate over the network
using TCP.

2. Usage:

Server:

javac Server.java
java Server 

User:

javac User.java
java User

3. Fuction:

Address and portNumber is defaulted to csa2.bu.edu and 58920
The maxthreadsCount is set to be 5.
Sever will ask for the useName while connect with a user.
The userName should not contain “@“. 
The user message will automatically broadcasted to all users.
The user can type in “Exit” to quit the Chatroom.

4. Tradeoffs & Extensions:

Duplicate username: in the case of duplicate username, the program can simply implement a loop of checking all the existed userName and a counter of #users in the while(true) loop for checking “@“. If all the existed users have different name from the input, break the while loop. Otherwise ask the user to enter the name again.