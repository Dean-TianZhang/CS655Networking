package addFriends;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;

/*
 * A server that delivers status messages to other users.
 */
public class Server {

	// Create a socket for the server 
	private static ServerSocket serverSocket = null;
	// Create a socket for the server 
	private static Socket userSocket = null;
	// Maximum number of users 
	private static int maxUsersCount = 5;
	// An array of threads for users
	private static userThread[] threads = null;


	public static void main(String args[]) {

		// The default port number.
		int portNumber = 58920;
		if (args.length < 2) {
			System.out.println("Usage: java Server <portNumber>\n"
					+ "Now using port number=" + portNumber + "\n" +
					"Maximum user count=" + maxUsersCount);
		} else {
			portNumber = Integer.valueOf(args[0]).intValue();
			maxUsersCount = Integer.valueOf(args[1]).intValue();
		}

		System.out.println("Server now using port number=" + portNumber + "\n" + "Maximum user count=" + maxUsersCount);
		
		
		threads = new userThread[maxUsersCount];


		/*
		 * Open a server socket on the portNumber (default 8000). 
		 */
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println(e);
		}

		/*
		 * Create a user socket for each connection and pass it to a new user
		 * thread.
		 */
		while (true) {
			try {
				userSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxUsersCount; i++) {
					if (threads[i] == null) {
						threads[i] = new userThread(userSocket, threads);
						threads[i].start();
						break;
					}
				}
				/*
				 * When the user quantity is approached to "maxUsersCount", 
				 * the Server will tell the User edge that "Server busy!"
				 */
				if (i == maxUsersCount) {
					PrintStream output_stream = new PrintStream(userSocket.getOutputStream());
					output_stream.println("#busy");
					System.out.println("Server busy!\n");
					output_stream.close();
					userSocket.close();
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

/*
 * Threads
 */
class userThread extends Thread {

	private String userName;
	private BufferedReader input_stream = null;
	private PrintStream output_stream = null;
	private Socket userSocket = null;
	private final userThread[] threads;
	private int maxUsersCount;
	private boolean[] addFriends;
    private int Index;

	public userThread(Socket userSocket, userThread[] threads) {
		this.userSocket = userSocket;
		this.threads = threads;
		maxUsersCount = threads.length;
		this.addFriends = new boolean[maxUsersCount];//According "maxUsersCount", every ChatRoom use could only add maximum number of friends as "maxUsersCount".
	}

	public void run() {
		int maxUsersCount = this.maxUsersCount;
		userThread[] threads = this.threads;
		String userName = this.userName;
        boolean[] addFriends = this.addFriends;
        int Index = this.Index;

		try {
			/*
			 * Create input and output streams for this client.
			 * Read user name.
			 */
			input_stream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
			output_stream = new PrintStream(userSocket.getOutputStream());
		    String input;
		    
		    if (input_stream != null)
	    		input = input_stream.readLine().trim();
            else input = "#join No user found!\n";
            if (!input.startsWith("#join") && !input.startsWith("@"))
                output_stream.println("#join Invalid user name!\n");
	        this.userName = input.substring(6);
		    
			/* Welcome the new user. */
	        PrintWriter pw = new PrintWriter(output_stream);
            BufferedWriter bw = new BufferedWriter(pw);
            String feedback = "Welcome!\n";
            bw.write("#welcome " + feedback);
	        for (int i = 0; i < maxUsersCount; i++) {
	            if (threads[i] != null && threads[i] != this) {
	              this.addFriends[i] = false;
	              threads[i].output_stream.println("#newuser " + this.userName
	                  + " entered the chat room !!!");
	            }
	          }
	        for (int i = 0; i < maxUsersCount; i++){
                if (threads[i] != null && threads[i] != this){
                    Index = i;	                	
                }
	          }
	        System.out.println("Server sent messages" + "\n");
	        bw.flush();
		        
			/* Start the conversation. */
		        while (true) {
		            String line = input_stream.readLine();
	                if (line.equals("Bye")){
	                    for (int i = 0; i < maxUsersCount; i++)
	                        if (threads[i] != null && threads[i] != this)
	                            threads[i].output_stream.println("Leave " + this.userName + " is left!\n");
	                    output_stream.println("Bye" + userName);
	                    break;
	                }
	                else if (line.startsWith("#status")){
	                    output_stream.println("#statusPosted");
	                    System.out.println("#statusPosted" + "\n");
	                    for (int i = 0; i < maxUsersCount; i++)
	                        if (threads[i] != null && threads[i] != this && threads[i].addFriends[Index] == true)
	                            threads[i].output_stream.println("#newStatus " + this.userName + " : " + line.substring(8) + "\n");
	                }
	                else if (line.startsWith("#friendme")){
	                    for (int i = 0; i < maxUsersCount; i++)
	                        if (threads[i] != null && threads[i].userName.equals(line.substring(10)))
	                            threads[i].output_stream.println("#friendme " + this.userName);
	                }
	                else if (line.startsWith("#friends")){
	                    output_stream.println("#OKfriends " + this.userName + " " + line.substring(9));
	                    for (int i = 0; i < maxUsersCount; i++){
	                        if (threads[i] != null && threads[i].userName.equals(line.substring(9))){
	                            threads[i].output_stream.println("#OKfriends " + this.userName + " " + line.substring(9));
	                            this.addFriends[i] = true;
	                            threads[i].addFriends[Index] = true;
	                        }
	                    }
	                }
	                else if (line.startsWith("#DenyFriendRequest")){
	                    for (int i = 0; i < maxUsersCount; i++)
	                        if (threads[i] != null && threads[i].userName.equals(line.substring(19)))
	                            threads[i].output_stream.println("#FriendRequestDenied " + this.userName);
	                }
	                else if (line.startsWith("#unfriend")){
	                    output_stream.println("#NotFriends " + this.userName + " " + line.substring(10));
	                    for (int i = 0; i < maxUsersCount; i++){
	                        if (threads[i] != null && threads[i].userName.equals(line.substring(10))){
	                            threads[i].output_stream.println("#NotFriends " + this.userName + " " + line.substring(10));
	                            threads[i].addFriends[Index] = false;
	                            this.addFriends[i] = false;
	                        }
	                    }
	                }
		          }
			// conversation ended.

			/*
			 * Clean up. Set the current thread variable to null so that a new user
			 * could be accepted by the server.
			 */
			synchronized (userThread.class) {
				for (int i = 0; i < maxUsersCount; i++) {
					if (threads[i] == this) {
						threads[i] = null;
					}
				}
			}
			/*
			 * Close the output stream, close the input stream, close the socket.
			 */
			input_stream.close();
			output_stream.close();
			userSocket.close();
		} catch (IOException e) {
		}
	}
}
