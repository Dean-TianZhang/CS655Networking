package addGroup;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class User extends Thread {

	// The user socket
	private static Socket userSocket = null;
	// The output stream
	private static PrintStream output_stream = null;
	// The input stream
	private static BufferedReader input_stream = null;

	private static BufferedReader inputLine = null;
	
	private static boolean closed = false;

	public static void main(String[] args) {

		// The default port.
		int portNumber = 58920;
		// The default host.
		String host = "csa2.bu.edu";

		if (args.length < 2) {
			System.out
			.println("Usage: java User <host> <portNumber>\n"
					+ "Now using host=" + host + ", portNumber=" + portNumber);
		} else {
			host = args[0];
			portNumber = Integer.valueOf(args[1]).intValue();
		}

		/*
		 * Open a socket on a given host and port. Open input and output streams.
		 */
		try {
			userSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			output_stream = new PrintStream(userSocket.getOutputStream());
			input_stream = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + host);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to the host "
					+ host);
		}

		/*
		 * If everything has been initialized then we want to write some data to the
		 * socket we have opened a connection to on port portNumber.
		 */
		if (userSocket != null && output_stream != null && input_stream != null) {
			try {                
				/* Create a thread to read from the server. */
				new Thread(new User()).start();

				// Get user name and join the social net
				System.out.print("Please enter your username\n");
                Scanner scanner = new Scanner(System.in);
                String userName = scanner.next();
                String protocolHead = "#join ";
                PrintWriter pw = new PrintWriter(output_stream);
                BufferedWriter bw = new BufferedWriter(pw);
                bw.write(protocolHead + userName + "\n");
                System.out.println(userName + " is joined! \n");
                bw.flush();
				

				while (!closed) {
					try{
						String userMessage;
						String userInput = inputLine.readLine().trim();
						
	                    // Read user input and send protocol messagePrint to server
	                    if (userInput.startsWith("@connect"))
	                        userMessage = "#friendme " + userInput.substring(9);
	                    else if (userInput.startsWith("@friend"))
	                        userMessage = "#friends " + userInput.substring(8);
	                    else if (userInput.startsWith("@deny"))
	                        userMessage = "#DenyFriendRequest " + userInput.substring(6);
	                    else if (userInput.startsWith("@disconnect"))
	                        userMessage = "#unfriend " + userInput.substring(12);
	                    else if (userInput.startsWith("@add")){
	                        String[] temp = userInput.split(" ");
	                        userMessage = "#group " + temp[1] + " " + temp[2];
	                    }
	                    else if (userInput.equals("Exit"))    
	                    	userMessage = "Bye";
	                    else if (userInput.startsWith("@send")){
	                        String[] temp = userInput.split(" ");
	                        userMessage = "#gstatus " + temp[1] + " " + userName + " " + userInput.substring(7 + temp[1].length());
	                    }
	                    else if (userInput.startsWith("@delete")){
	                        String[] temp = userInput.split(" ");
	                        userMessage = "#ungroup " + temp[1] + " " + temp[2];
	                    }
	                    else    userMessage = "#status " + userInput;
	                    
	                    PrintWriter messagePrint = new PrintWriter(output_stream);
	                    BufferedWriter databw = new BufferedWriter(messagePrint);
	                    databw.write(userMessage + "\n");
	                    databw.flush();
	                    
					}catch (Exception e){
                        System.err.println("Exception:  " + e);
                        System.out.println("Invalid input!");
                    }
				}
				/*
				 * Close the output stream, close the input stream, close the socket.
				 */
			} catch (IOException e) {
				System.err.println("IOException:  " + e);
			}
		}
	}

	/*
	 * Create a thread to read from the server.
	 */
	public void run() {
		/*
		 * Keep on reading from the socket till we receive a Bye from the
		 * server. Once we received that then we want to break.
		 */
		String responseLine;
		
		try {
			boolean flag = false;
			while ((responseLine = input_stream.readLine()) != null) {
				
				// Display on console based on what protocol messagePrint we get from server.
				if (responseLine.startsWith("#busy")){
                    System.out.println("Try Later");
                    flag = false;
                }
                else if (responseLine.startsWith("#statusPosted")){
                    System.out.println("Your message has been sent!\n");
                    flag = false;
                }
                else if (responseLine.startsWith("#newuser")){
                    System.out.println( responseLine.substring(9) + "\n");
                    flag = false;
                }
                else if (responseLine.startsWith("#newStatus")){
                    System.out.println(responseLine.substring(11) + "\n");
                    flag = false;
                }
                else if (responseLine.startsWith("Leave")){
                    System.out.println(responseLine.substring(6) + "\n");
                    flag = false;
                }
                else if (responseLine.startsWith("#friendme")){
                    System.out.println(responseLine.substring(10) + " just sent you friend request!\n");
                    flag = false;
                }
                else if (responseLine.startsWith("#OKfriends")){
                    String[] temp = responseLine.split(" ");
                    System.out.println(temp[1] + " and " + temp[2] + " are now friends!\n");
                    flag = false;
                }
                else if (responseLine.startsWith("#FriendRequestDenied")){
                    System.out.println("Sorry, " + responseLine.substring(21) + " rejected your friend request! \n");
                    flag = false;
                }
                else if (responseLine.startsWith("#NotFriends")){
                    String[] temp = responseLine.split(" ");
                    System.out.println(temp[1] + " and " + temp[2] + " are not friends anymore!\n");
                    flag = false;
                }
                else if (responseLine.startsWith("#group")){
                    String[] temp = responseLine.split(" ");
                    System.out.println("\n" + temp[2] + " is now in group " + temp[1] + "\n");
                    flag = false;
                }
                else if (responseLine.startsWith("#gstatus")){
                    String[] temp = responseLine.split(" ");
                    System.out.println("\n[" + temp[1] + "][" + temp[2] + "]: " + responseLine.substring(11 + temp[1].length() + + temp[2].length()) + "\n");
                    flag = false;
                }
                else if (responseLine.startsWith("#ungroup")){
                    String[] temp = responseLine.split(" ");
                    System.out.println("\n" + temp[2] + " is no longer member of the group " + temp[1] + "\n");
                    flag = false;
                }
                else if (responseLine.startsWith("Bye")){
                    System.out.println("Goodbye!\n");
                    break;
                }
                if (flag == false){
                    System.out.print("please enter words you want to say: ");
                    flag = true;
                }
			}
			closed = true;
			output_stream.close();
			input_stream.close();
			userSocket.close();
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}
	}
}
