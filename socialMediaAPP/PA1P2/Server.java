package broadcast;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.BufferedReader;
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
	private static int maxthreadsCount = 5;
	// An array of threads for users
	private static userThread[] threads = null;


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
		
		
		userThread[] threads = new userThread[maxthreadsCount];


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
				for (i = 0; i < maxthreadsCount; i++) {
					if (threads[i] == null) {
						threads[i] = new userThread(userSocket, threads);
						threads[i].start();
						break;
					}
				}
				if (i == maxthreadsCount) {
					PrintStream output_stream = new PrintStream(userSocket.getOutputStream());
					output_stream.println("#busy");
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

	private String userName = null;
	private BufferedReader input_stream = null;
	private PrintStream output_stream = null;
	private Socket userSocket = null;
	private final userThread[] threads;
	private int maxthreadsCount;

	public userThread(Socket userSocket, userThread[] threads) {
		this.userSocket = userSocket;
		this.threads = threads;
		maxthreadsCount = threads.length;
	}

	public void run() {
		int maxthreadsCount = this.maxthreadsCount;
		userThread[] threads = this.threads;

		try {
			/*
			 * Create input and output streams for this client.
			 * Read user name.
			 */
			input_stream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
			output_stream = new PrintStream(userSocket.getOutputStream());
			output_stream.println("Enter your name.");
		    String name = input_stream.readLine().trim();
		    
		    while (true) {
		    	output_stream.println("Enter your name.");
		        if (name.indexOf('@') == -1) {
		          break;
		        } else {
		        	output_stream.println("The name should not contain '@' character.");
		        }
		      }
		    
			/* Welcome the new user. */
		    output_stream.println("Welcome " + name
		            + " to our chat room.\nTo leave enter Exit in a new line.");
		        synchronized (this) {
		          for (int i = 0; i < maxthreadsCount; i++) {
		            if (threads[i] != null && threads[i] == this) {
		            	userName = "@" + name;
		              break;
		            }
		          }
		          for (int i = 0; i < maxthreadsCount; i++) {
		            if (threads[i] != null && threads[i] != this) {
		              threads[i].output_stream.println("*** A new user " + name
		                  + " entered the chat room !!! ***");
		            }
		          }
		        }
			/* Start the conversation. */
		        while (true) {
		            String line = input_stream.readLine();
		            if (line.startsWith("Exit")) {
		              break;
		            }
		            /* If the message is private sent it to the given client. */
		            if (line.startsWith("@")) {
		              String[] words = line.split("\\s", 2);
		              if (words.length > 1 && words[1] != null) {
		                words[1] = words[1].trim();
		                if (!words[1].isEmpty()) {
		                  synchronized (this) {
		                    for (int i = 0; i < maxthreadsCount; i++) {
		                      if (threads[i] != null && threads[i] != this
		                          && threads[i].userName != null
		                          && threads[i].userName.equals(words[0])) {
		                        threads[i].output_stream.println("<" + name + "> " + words[1]);
		                        /*
		                         * Echo this message to let the client know the private
		                         * message was sent.
		                         */
		                        this.output_stream.println("<" + name + "> " + words[1]);
		                        break;
		                      }
		                    }
		                  }
		                }
		              }
		            } else {
		              /* The message is public, broadcast it to all other clients. */
		              synchronized (this) {
		                for (int i = 0; i < maxthreadsCount; i++) {
		                  if (threads[i] != null && threads[i].userName != null) {
		                    threads[i].output_stream.println("<" + name + "> " + line);
		                  }
		                }
		              }
		            }
		          }
			// conversation ended.
		        synchronized (this) {
		            for (int i = 0; i < maxthreadsCount; i++) {
		              if (threads[i] != null && threads[i] != this
		                  && threads[i].userName != null) {
		                threads[i].output_stream.println("*** The user " + name
		                    + " is leaving the chat room !!! ***");
		              }
		            }
		          }
		        output_stream.println("*** Bye " + name + " ***");

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
			userSocket.close();
		} catch (IOException e) {
		}
	}
}



