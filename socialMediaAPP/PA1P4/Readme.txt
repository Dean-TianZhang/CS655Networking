Programming Assignment Part 4 
 
Adding Group Capability 

Tian Zhang
U31287117

1. Description:

In this part you will extend Part 3 so a user can create groups among her friends.

2. Usage:

Server:

javac Server.java
java Server 

User:

javac User.java
java User

3. Fuction:

Do some extention based on Part 2 and 3.
@add <groupname> <username>: add one of friend to the group. Creating a new group when adding first friend
@send <groupname> <message>: send boardcast message to all group members.
@delete <groupname> <username>：each group member has the right to delete someone else from that group.

4. Tradeoffs & Extensions:

(1) I should add limit to the delete of group user that only the group creator could delete group member.

(2) Each time, when user create a new group, we should check it from a list of groupname. If there has a group with same name, the user should change the creating groupname. 

(3) In the future, I will use a map to store the chatting records, or use a database to store.