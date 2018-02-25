Programming Assignment Part 3 
 
Adding Friendship / Multicast Capability to the Social Media App

Tian Zhang
U31287117

1. Description:

In this part you will extend Part 2 by adding multicast capability. A client should be able to post
status updates to only her friends (a subset of all clients). Thus clients should be able to add
each other as friends. 

2. Usage:

Server:

javac Server.java
java Server 

User:

javac User.java
java User

3. Fuction:

Adding following fuctions based on Part 2:
User can use @connect to request a friendship to other user.
User can choose use @friend to accept friend request, or use @deny to reject.
User could use @disconnect to delet one of his/her friend.
User could only recieve message from his/her friend

4. Tradeoffs & Extensions:

I should modify some parts of my current code that when one user choose to type @friend username directly to add friend. When this situation happen, the server should check if the user has sent request to current user. If YES, friendship is built, and if NO, the server should ask the user to send request first.