package addGroup;

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
 * A server that delivers status messages to other threads.
 */
public class Server {

	// Create a socket for the server 
	private static ServerSocket serverSocket = null;
	// Create a socket for the server 
	private static Socket threadsocket = null;
	// Maximum number of threads 
	private static int maxthreadsCount = 5;
	// An array of threads for threads
	private static userThread[] threads = null;
	//A HashTable to store group list
	public static Hashtable<String, ArrayList<Boolean>> groups = new Hashtable<String, ArrayList<Boolean>>();


	public static void main(String args[]) {

		// The default port number.
		int portNumber = 58920;
		if (args.length < 2) {
			System.out.println("Usage: java Server <portNumber>\n"
					+ "Now using port number=" + portNumber + "\n" +
					"Maximum user count=" + maxthreadsCount);
		} else {
			portNumber = Integer.valueOf(args[0]).intValue();
			maxthreadsCount = Integer.valueOf(args[1]).intValue();
		}

		System.out.println("Server now using port number=" + portNumber + "\n" + "Maximum user count=" + maxthreadsCount);
		
		
		threads = new userThread[maxthreadsCount];


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
				threadsocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxthreadsCount; i++) {
					if (threads[i] == null) {
						threads[i] = new userThread(threadsocket, threads);
						threads[i].start();
						break;
					}
				}
				/*
				 * When the user quantity is approached to "maxthreadsCount", 
				 * the Server will tell the User edge that "Server busy!"
				 */
				if (i == maxthreadsCount) {
					PrintStream output_stream = new PrintStream(threadsocket.getOutputStream());
					output_stream.println("#busy");
					System.out.println("Server busy!\n");
					output_stream.close();
					threadsocket.close();
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
	private Socket threadsocket = null;
	private final userThread[] threads;
	private int maxthreadsCount;
	private boolean[] addFriends;
    private int Index;

	public userThread(Socket threadsocket, userThread[] threads) {
		this.threadsocket = threadsocket;
		this.threads = threads;
		maxthreadsCount = threads.length;
		this.addFriends = new boolean[maxthreadsCount];
	}

	public void run() {
		int maxthreadsCount = this.maxthreadsCount;
		userThread[] threads = this.threads;
		String userName = this.userName;
        boolean[] addFriends = this.addFriends;
        int Index = this.Index;

		try {
			/*
			 * Create input and output streams for this client.
			 * Read user name.
			 */
			input_stream = new BufferedReader(new InputStreamReader(threadsocket.getInputStream()));
			output_stream = new PrintStream(threadsocket.getOutputStream());
		    String input;
		    
		    if (input_stream != null)
	    		input = input_stream.readLine().trim();
            else input = "#join No user found!\n";
            if (!input.startsWith("#join"))
                output_stream.println("#join Invalid user name!\n");
	        this.userName = input.substring(6);
		    
			/* Welcome the new user. */
	        
	        for (int i = 0; i < maxthreadsCount; i++) {
	            if (threads[i] != null && threads[i] != this) {
	              this.addFriends[i] = false;
	              threads[i].output_stream.println("#newuser " + this.userName
	                  + " entered the chat room !!!");
	            }
	          }
	        for (int i = 0; i < maxthreadsCount; i++){
                if (threads[i] != null && threads[i] != this){
                    Index = i;	                	
                }
	          }
	        System.out.println("Server sent messages" + "\n");
		        
			/* Start the conversation. */
		        while (true) {
		        	try{
		        		String line = input_stream.readLine();
		                if (line.equals("Bye")){
		                    for (int i = 0; i < maxthreadsCount; i++)
		                        if (threads[i] != null && threads[i] != this)
		                            threads[i].output_stream.println("Leave " + this.userName + " is left!\n");
		                    output_stream.println("Bye" + userName);
		                    break;
		                }
		                else if (line.startsWith("#status")){
		                    output_stream.println("#statusPosted");
		                    System.out.println("#statusPosted" + "\n");
		                    for (int i = 0; i < maxthreadsCount; i++)
		                        if (threads[i] != null && threads[i] != this && threads[i].addFriends[Index] == true)
		                            threads[i].output_stream.println("#newStatus " + this.userName + " : " + line.substring(8) + "\n");
		                }
		                else if (line.startsWith("#friendme")){
		                	System.out.println("#friendme" + "\n");
		                    for (int i = 0; i < maxthreadsCount; i++)
		                        if (threads[i] != null && threads[i].userName.equals(line.substring(10)))
		                            threads[i].output_stream.println("#friendme " + this.userName);
		                }
		                else if (line.startsWith("#friends")){
		                    output_stream.println("#OKfriends " + this.userName + " " + line.substring(9));
		                    System.out.println("#OKfriends " + this.userName + " " + line.substring(9) + "\n");
		                    for (int i = 0; i < maxthreadsCount; i++){
		                        if (threads[i] != null && threads[i].userName.equals(line.substring(9))){
		                            threads[i].output_stream.println("#OKfriends " + this.userName + " " + line.substring(9));
		                            this.addFriends[i] = true;
		                            threads[i].addFriends[Index] = true;
		                        }
		                    }
		                }
		                else if (line.startsWith("#DenyFriendRequest")){
		                    for (int i = 0; i < maxthreadsCount; i++)
		                        if (threads[i] != null && threads[i].userName.equals(line.substring(19)))
		                            threads[i].output_stream.println("#FriendRequestDenied " + this.userName);
		                    System.out.println("#DenyFriendRequest");
		                }
		                else if (line.startsWith("#unfriend")){
		                    output_stream.println("#NotFriends " + this.userName + " " + line.substring(10));
		                    System.out.println("#NotFriends " + this.userName + " " + line.substring(10));
		                    for (int i = 0; i < maxthreadsCount; i++){
		                        if (threads[i] != null && threads[i].userName.equals(line.substring(10))){
		                            threads[i].output_stream.println("#NotFriends " + this.userName + " " + line.substring(10));
		                            threads[i].addFriends[Index] = false;
		                            this.addFriends[i] = false;
		                        }
		                    }
		                }
		                else if (line.startsWith("#group")){
	                        String[] temp = line.split(" ");
	                        if (!Server.groups.containsKey(temp[1])){
	                            ArrayList<Boolean> list = new ArrayList<>();
	                            for (int i = 0; i < maxthreadsCount; i++)
	                                list.add(false);
	                            list.set(Index, true);
	                            for (int i = 0; i < maxthreadsCount; i++)
	                                if (threads[i] != null && threads[i].userName.equals(temp[2]) && this.addFriends[i])
	                                    list.set(i, true);
	                            Server.groups.put(temp[1], list);
	                        }
	                        else {
	                            if(Server.groups.get(temp[1]).get(Index))
	                                for (int i = 0; i < maxthreadsCount; i++)
	                                    if (threads[i] != null && threads[i].userName.equals(temp[2]) && this.addFriends[i])
	                                        Server.groups.get(temp[1]).set(i, true);
	                        }
	                        ArrayList<Boolean> list = Server.groups.get(temp[1]);
	                        for (int i = 0; i < maxthreadsCount; i++)
	                            if (list.get(i))
	                            	threads[i].output_stream.println(line);
			            }
		                else if (line.startsWith("#gstatus")){
	                        String[] temp = line.split(" ");
	                        if (Server.groups.containsKey(temp[1])){
	                            ArrayList<Boolean> list = Server.groups.get(temp[1]);
	                            if (list.get(Index))
	                                for (int i = 0; i < maxthreadsCount; i++)
	                                    if (list.get(i))
	                                        threads[i].output_stream.println(line);
	                        }
	                    }
	                    else if (line.startsWith("#ungroup")){
	                        String[] temp = line.split(" ");
	                        if (Server.groups.containsKey(temp[1])){
	                            ArrayList<Boolean> list = Server.groups.get(temp[1]);
	                            if(list.get(Index)){
	                                for (int i = 0; i < maxthreadsCount; i++)
	                                    if (list.get(i))
	                                        threads[i].output_stream.println(line);
	                                for (int i = 0; i < maxthreadsCount; i++)
	                                    if (threads[i] != null && threads[i].userName.equals(temp[2]) && list.get(i))
	                                        list.set(i, false);
	                                //Careful here!
	                                if (!list.contains(true))
	                                    Server.groups.remove(temp[1]);
	                            }
	                        }
	                    }
		        	}catch (Exception e) {
	                    System.err.println("Exception:  " + e);
	                }
		        }
			// conversation ended.

			/*
			 * Clean up. Set the current thread variable to null so that a new user
			 * could be accepted by the server.
			 */
			synchronized (userThread.class) {
				for (int i = 0; i < maxthreadsCount; i++) {
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
			threadsocket.close();
		} catch (IOException e) {
		}
	}
}
