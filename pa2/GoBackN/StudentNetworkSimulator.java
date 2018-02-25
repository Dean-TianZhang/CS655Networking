import java.util.*;
import java.io.*;

public class StudentNetworkSimulator extends NetworkSimulator
{
    /*
     * Predefined Constants (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and
     *                     Packet payload
     *
     *   int A           : a predefined integer that represents entity A
     *   int B           : a predefined integer that represents entity B 
     *
     * Predefined Member Methods:
     *
     *  void stopTimer(int entity): 
     *       Stops the timer running at "entity" [A or B]
     *  void startTimer(int entity, double increment): 
     *       Starts a timer running at "entity" [A or B], which will expire in
     *       "increment" time units, causing the interrupt handler to be
     *       called.  You should only call this with A.
     *  void toLayer3(int callingEntity, Packet p)
     *       Puts the packet "p" into the network from "callingEntity" [A or B]
     *  void toLayer5(String dataSent)
     *       Passes "dataSent" up to layer 5
     *  double getTime()
     *       Returns the current time in the simulator.  Might be useful for
     *       debugging.
     *  int getTraceLevel()
     *       Returns TraceLevel
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for
     *       debugging, but probably not.
     *
     *
     *  Predefined Classes:
     *
     *  Message: Used to encapsulate a message coming from layer 5
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      boolean setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *          returns true on success, false otherwise
     *      String getData():
     *          returns the data contained in the message
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet that is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload)
     *          creates a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and a
     *          payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and
     *          an empty payload
     *    Methods:
     *      boolean setSeqnum(int n)
     *          sets the Packet's sequence field to "n"
     *          returns true on success, false otherwise
     *      boolean setAcknum(int n)
     *          sets the Packet's ack field to "n"
     *          returns true on success, false otherwise
     *      boolean setChecksum(int n)
     *          sets the Packet's checksum to "n"
     *          returns true on success, false otherwise
     *      boolean setPayload(String newPayload)
     *          sets the Packet's payload to "newPayload"
     *          returns true on success, false otherwise
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      int getPayload()
     *          returns the Packet's payload
     *
     */

    /*   Please use the following variables in your routines.
     *   int WindowSize  : the window size
     *   double RxmtInterval   : the retransmission timeout
     *   int LimitSeqNo  : when sequence number reaches this value, it wraps around
     */

	public static final int STATE_WAIT_FOR_CALL_0_FROM_ABOVE = 0;
	public static final int STATE_WAIT_FOR_ACK_OR_NAK_0 = 1;
	public static final int STATE_WAIT_FOR_CALL_1_FROM_ABOVE = 2;
	public static final int STATE_WAIT_FOR_ACK_OR_NAK_1 = 3;
	
	public static final int STATE_WAIT_FOR_0_FROM_BELOW = 0;
	public static final int STATE_WAIT_FOR_1_FROM_BELOW = 1;
	
	
	public static final String DATA_EMPTY = "";
	public static final int ACK_NOT_USED =-2;
	public static final int ACK_ACKed_0 =-3;
	public static final int ACK_ACKed_1 =-4;
	public static final int ACK_NAKed =-5;
	public static final int ACK_ACKed =-6;
	
	private int count_original_packets_transmitted_by_A = 0;
	private int count_retransmissions_by_A =0;
	
    public static final int FirstSeqNo = 0;
    private int WindowSize;
    private double RxmtInterval;
    private int LimitSeqNo;
    
    private int state_sender;
    private int state_receiver;
    private Packet packetBuffer;
    private Packet[] packetBufferAry;
    private int[] isAckedWindow = new int[5];
    private ArrayList<Message> messageCongestionBuffer = new ArrayList<Message>();
    
    private int window_base;
    private int next_seq_num;
    private int expected_seq_num;
    
    // Add any necessary class variables here.  Remember, you cannot use
    // these variables to send messages error free!  They can only hold
    // state information for A or B.
    // Also add any necessary methods (e.g. checksum of a String)

    // This is the constructor.  Don't touch!
    protected boolean isCorrupted(Packet packet)
    {
    		int check = packet.getChecksum();
		//protected int makeCheckSum(int seqNum,int ackNum, String data)
    		
		int check2 = makeCheckSum(packet.getSeqnum(),packet.getAcknum(),packet.getPayload());
		if(check!=check2) //Ack = -1 means NAK
		{
			return true;
		}
		else
		{
			return false;
		}
    }
    
    protected void resendPacket(Packet packet)
    {
		toLayer3(0,packet); //udt_send
		System.out.println("resendPacket: packet "+Integer.toString(packet.getSeqnum())+" resend");
    }
    
    protected int makeCheckSum(int seqNum,int ackNum, String data)
    {
    		int dataSum = 0;
    		char[] dataChar = data.toCharArray();
    		if(data.length()>0)
    		{
    			for(int i=0;i<data.length();i++)
    				{
    					dataSum += (int)dataChar[i];
    				}
    		}
    		dataSum+=seqNum;
    		dataSum+=ackNum;
    		return dataSum;
    		
    }
    
    public StudentNetworkSimulator(int numMessages,
                                   double loss,
                                   double corrupt,
                                   double avgDelay,
                                   int trace,
                                   int seed,
                                   int winsize,
                                   double delay)
    {
        super(numMessages, loss, corrupt, avgDelay, trace, seed);
	WindowSize = winsize;
	LimitSeqNo = winsize*2; // set appropriately; assumes SR here!
	RxmtInterval = delay;
    }

    
    // This routine will be called whenever the upper layer at the sender [A]
    // has a message to send.  It is the job of your protocol to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving upper layer.
    protected void aOutput(Message message)
    {
    	//rdt_send(data)
    	count_original_packets_transmitted_by_A++;
    	System.out.println("|aOutput| : message received from above.");
    	messageCongestionBuffer.add(message);
    	System.out.println("|aOutput| : messageCongestionBuffer add new message, buffer size now is: "+Integer.toString(messageCongestionBuffer.size()));
    	if(next_seq_num<window_base+WindowSize)
    	{
    		/*
    		if(messageCongestionBuffer.size()>0) //something has already been in the buffer
    		{
    			System.out.println("something has already been in the buffer");
    			messageCongestionBuffer.add(message);
    			message = messageCongestionBuffer.get(0);
    			messageCongestionBuffer.remove(0);
    		}
    		*/
    		String data = messageCongestionBuffer.get(0).getData();
    		messageCongestionBuffer.remove(0);
    		
    		//public Packet(int seq, int ack, int check, String newPayload)
    		
    		int seq = next_seq_num % LimitSeqNo;
    		int ack = ACK_NOT_USED;
    		int check = makeCheckSum(seq,ack,data);
    		packetBufferAry[next_seq_num % LimitSeqNo] = new Packet(seq,ack,check,data);
    		System.out.println("|aOutput| : packet with seq number:"+Integer.toString(next_seq_num)+" is made");
    		toLayer3(0,packetBufferAry[next_seq_num % LimitSeqNo]); //udt_send
    		System.out.println("|aOutput| : packet with seq number:"+Integer.toString(next_seq_num)+" is sent");
    		
    		if(next_seq_num==window_base) 
    		{
    			startTimer(0,RxmtInterval);
    			System.out.println("|aOutput| : timer is started");
    		}
    		
    		
    		next_seq_num = (next_seq_num+1) % LimitSeqNo;	
    		System.out.println("|aOutput| : next_seq_num now becomes: "+next_seq_num+".");
    		
    	}
    	else
    	{
    		System.out.println("|aOutput| : windows is full, it is saved in a buffer.");
    		System.out.println("|aOutput| : messageCongestionBuffer size now is: "+Integer.toString(messageCongestionBuffer.size()));

    	}
    	
    //	public Packet(int seq, int ack, int check,int[] sackAry)
    	/*
    		if(state_sender == STATE_WAIT_FOR_CALL_0_FROM_ABOVE)
    		{
    			
    			int seq = 0; //seq = 0
    			int ack = ACK_NOT_USED; //0 for not using
    			String dataStr = message.getData();
    			int check;
    			check = makeCheckSum(seq,ack,dataStr); //checksum
    			Packet p = new Packet(seq,ack,check,message.getData()); //make_pkt
    			
    			packetBuffer = p; //save packets for resend
    			
    			toLayer3(0,p); //udt_send
    			state_sender = STATE_WAIT_FOR_ACK_OR_NAK_0;
    			System.out.println("aOutput: packet0 successfully send.");
    			
    			startTimer(0,RxmtInterval);
    			System.out.println("aOutput: start timer");
    			
    			
    			
    		}
    		else if (state_sender == STATE_WAIT_FOR_CALL_1_FROM_ABOVE)
    		{
    			int seq = 1; //seq = 0
    			int ack = ACK_NOT_USED; //0 for not using
    			String dataStr = message.getData();
    			int check;
    			check = makeCheckSum(seq,ack,dataStr); //checksum
    			Packet p = new Packet(seq,ack,check,message.getData()); //make_pkt
    			
    			packetBuffer = p; //save packets for resend
    			
    			toLayer3(0,p); //udt_send
    			state_sender = STATE_WAIT_FOR_ACK_OR_NAK_1;
    			System.out.println("aOutput: packet1 successfully send.");	
    			startTimer(0,RxmtInterval);
    			System.out.println("aOutput: start sender timer");
    		}
    		*/
    }
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by a B-side procedure)
    // arrives at the A-side.  "packet" is the (possibly corrupted) packet
    // sent from the B-side.
    

    
    protected void aInput(Packet packet)
    {
    		if(!isCorrupted(packet))
    			{
    				System.out.println("|aInput| : packet"+ packet.getSeqnum()+"is received without corruption.");
    				int offset = ((packet.getSeqnum()+1) % LimitSeqNo - window_base);
    				if(offset>1)
    				{
    					System.out.println("|aInput| : window_base: "+window_base);
    					System.out.println("|aInput| : next sequence number: "+ Integer.toString((packet.getSeqnum()+1) % LimitSeqNo));
    					System.out.println("|aInput| : offset: "+offset);
    				}
    				window_base = (packet.getSeqnum()+1) % LimitSeqNo;;
    				
    				if(messageCongestionBuffer.size()>0)
    				{
    					String data = messageCongestionBuffer.get(0).getData();
    		    			messageCongestionBuffer.remove(0);
    		    		
    		    		//public Packet(int seq, int ack, int check, String newPayload)
    		    		
    		    			int seq = next_seq_num % LimitSeqNo;
    		    			int ack = ACK_NOT_USED;
    		    			int check = makeCheckSum(seq,ack,data);
    		    			packetBufferAry[next_seq_num % LimitSeqNo] = new Packet(seq,ack,check,data);
    		    			System.out.println("|aInput| : packet with seq number:"+Integer.toString(next_seq_num)+" is made");
    		    			toLayer3(0,packetBufferAry[next_seq_num % LimitSeqNo]); //udt_send
    		    			System.out.println("|aInput| : packet with seq number:"+Integer.toString(next_seq_num)+" is sent");
    		    			
    		    			next_seq_num = (next_seq_num+1)% LimitSeqNo;	
    		        		System.out.println("|aInput| : next_seq_num now becomes: "+next_seq_num+".");
    				}
    				
    				System.out.println("|aInput| : window_base becomes: "+ window_base+".");
    				
    				if(window_base == next_seq_num)
    					{
    						
    						System.out.println("|aInput| : timer is stopped");
    						stopTimer(0);
    					}
    				else
    					{
    						System.out.println("|aInput| : timer is restarted");
    						stopTimer(0);
    						startTimer(0,RxmtInterval);
    					}
    			}
    	
    	/*
    		if(state_sender==STATE_WAIT_FOR_ACK_OR_NAK_0)
    		{
    			
    			if(isCorrupted(packet)) //corrupted 
    			{
    				System.out.println("aInput: received packet 0 is corrupted");
    				//resendPacket(packetBuffer);
    			}
    			else if(packet.getAcknum()== ACK_ACKed_1) 
    			{
    				System.out.println("aInput: ACKed 1 is received");
    				//resendPacket(packetBuffer);
    			}
    			else //Ack = 1 or bigger mean ACK
    			{
    				System.out.println("aInput: ACKed 0 is received");
    				
    				state_sender = STATE_WAIT_FOR_CALL_1_FROM_ABOVE;
    				System.out.println("aInput: sender timer is stopped");
    				stopTimer(0); 
    			}
    			
    		}
    		else if(state_sender==STATE_WAIT_FOR_ACK_OR_NAK_1)
    		{
    			if(isCorrupted(packet)) //corrupted
    			{
    				System.out.println("aInput: received packet 1 is corrupted");
    				//esendPacket(packetBuffer);
    			}
    			else if(packet.getAcknum()==ACK_ACKed_0)//Ack = -1 means NAK
    			{
    				System.out.println("aInput: ACKed 0 is received");
    				//resendPacket(packetBuffer);
    			}
    			else //Ack = 1 or bigger mean ACK
    			{
    				System.out.println("aInput: ACKed 1 is received");
    				state_sender = STATE_WAIT_FOR_CALL_0_FROM_ABOVE;
    				System.out.println("aInput: sender timer is stopped");
    				stopTimer(0); 
    			}
    		}
    		*/
    		

    }
    
    // This routine will be called when A's timer expires (thus generating a 
    // timer interrupt). You'll probably want to use this routine to control 
    // the retransmission of packets. See startTimer() and stopTimer(), above,
    // for how the timer is started and stopped. 
    protected void aTimerInterrupt()
    {
    		System.out.println("|aTimerInterrupt| : time out.");
    		startTimer(0,RxmtInterval);
    		System.out.println("|aTimerInterrupt| : timer is started");
    		
    		for(int i=window_base% LimitSeqNo;i<next_seq_num % LimitSeqNo;i++)
    		{
    			if(packetBufferAry[i]!=null)
    			{
    				count_retransmissions_by_A++;
    				System.out.println("|aTimerInterrupt| : packet with seq number:"+Integer.toString(packetBufferAry[i].getSeqnum())+" is resent.");
    				toLayer3(0,packetBufferAry[i]);
    			}
    		}
    		/*
    		if(state_sender == STATE_WAIT_FOR_ACK_OR_NAK_0)
    		{
    			System.out.println("aTimerInterrupt: time up for STATE_WAIT_FOR_ACK_OR_NAK_0");
    			resendPacket(packetBuffer);
    			startTimer(0,RxmtInterval);
    			System.out.println("aTimerInterrupt: start sender timer");
    			
    		}
    		else if(state_sender == STATE_WAIT_FOR_ACK_OR_NAK_1)
    		{
    			System.out.println("aTimerInterrupt: time up for STATE_WAIT_FOR_ACK_OR_NAK_1");
    			resendPacket(packetBuffer);
    			startTimer(0,RxmtInterval);
    			System.out.println("aTimerInterrupt: start sender timer");
    		}
    		*/
    }
    
    // This routine will be called once, before any of your other A-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity A).
    protected void aInit()
    {
    		//state_sender = STATE_WAIT_FOR_CALL_0_FROM_ABOVE;
    		packetBufferAry = new Packet[LimitSeqNo];
    		window_base = FirstSeqNo;
    		next_seq_num = FirstSeqNo;
    		System.out.println("|aInit| : window_base: "+Integer.toString(window_base));
    		System.out.println("|aInit| : next_seq_num: "+Integer.toString(next_seq_num));    		
    }
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side.  "packet" is the (possibly corrupted) packet
    // sent from the A-side.
    protected void bInput(Packet packet)
    {
    		
    		if(!isCorrupted(packet)&&packet.getSeqnum()==expected_seq_num)
    		{
    			
        		System.out.println("|bInput| : packet "+Integer.toString(packet.getSeqnum())+"is received correctly.");  
    			String data = packet.getPayload();
    			toLayer5(data);
    			System.out.println("|bInput| : payload is sent to Layer 5");  
    			int check = makeCheckSum(expected_seq_num,ACK_ACKed,DATA_EMPTY);
    			packetBuffer = new Packet(expected_seq_num,ACK_ACKed,check,DATA_EMPTY);
    			System.out.println("|bInput| : packet with seq number:"+Integer.toString(expected_seq_num)+" is stored in Buffer");  
    			toLayer3(1,packetBuffer);
    			System.out.println("|bInput| : packet with seq number:"+Integer.toString(expected_seq_num)+" is sent"); 
    			expected_seq_num = (expected_seq_num+1)% LimitSeqNo;
			System.out.println("|bInput| : expected_seq_num becomes: "+ expected_seq_num+".");
    			
    		}
    		else
    		{
    			
        		System.out.println("packet is not correct or corrupted, sent ACK "+packetBuffer.getSeqnum()+" back to sender");  
    			toLayer3(1,packetBuffer);
    			System.out.println("|bInput| : packet with seq number:"+Integer.toString(packetBuffer.getSeqnum())+" is sent");
    		}
    	
    	/*
    		if(state_receiver == STATE_WAIT_FOR_0_FROM_BELOW)
    		{
    			if(isCorrupted(packet)) //Ack = -1 means NAK
    			{
    				System.out.println("bInput: received packet is corrupted");
    				//public Packet(int seq, int ack, int check)
    				int seq = -1;
    				int ack = ACK_ACKed_1;
    				int check = makeCheckSum(seq,ack,DATA_EMPTY);
    				Packet resendpkt = new Packet(seq,ack,check,DATA_EMPTY); //NAK: seq = -1, ack = -1
    				toLayer3(1,resendpkt);
    				System.out.println("bInput: send ACK 1");
    			}
    	    		else 
    	    		{
    	    			System.out.println("bInput: received packet is not corrupted");
    	    			if(packet.getSeqnum()==1)
    	    			{
    	    				int seq = -1;
        	    			int ack = ACK_ACKed_1;
        	    			int check = makeCheckSum(seq,ack,DATA_EMPTY); //ACK: seq = -1, ack = 1
        	    			Packet resendpkt = new Packet(seq,ack,check,DATA_EMPTY);
        	    			toLayer3(1,resendpkt); //send back ACK
        	    			System.out.println("bInput: send ACK 1");
    	    			}
    	    			else if(packet.getSeqnum()==0)
    	    			{
    	    				String data = packet.getPayload();
        	    			toLayer5(data);
        	    			System.out.println("bInput: layer5 received packet 0");
    	    				int seq = -1;
        	    			int ack = ACK_ACKed_0;
        	    			int check = makeCheckSum(seq,ack,DATA_EMPTY); //ACK: seq = -1, ack = 1
        	    			Packet resendpkt = new Packet(seq,ack,check,DATA_EMPTY);
        	    			toLayer3(1,resendpkt); //send back ACK
        	    			System.out.println("bInput: send ACK 0");
        	    			state_receiver = STATE_WAIT_FOR_1_FROM_BELOW;
    	    			}
    	    		}
    		}
    		
    		else if(state_receiver == STATE_WAIT_FOR_1_FROM_BELOW)
    		{
    			if(isCorrupted(packet)) 
    			{
    				System.out.println("bInput: received packet is corrupted");
    				//public Packet(int seq, int ack, int check)
    				int seq = -1;
    				int ack = ACK_ACKed_0;
    				int check = makeCheckSum(seq,ack,DATA_EMPTY);
    				Packet resendpkt = new Packet(seq,ack,check,DATA_EMPTY); //NAK: seq = -1, ack = -1
    				toLayer3(1,resendpkt);
    				System.out.println("bInput: send ACK 0");
    			}
    	    		else 
    	    		{
    	    			System.out.println("bInput: received packet is not corrupted");
    	    			if(packet.getSeqnum()==0)
    	    			{
    	    				int seq = -1;
        	    			int ack = ACK_ACKed_0;
        	    			int check = makeCheckSum(seq,ack,DATA_EMPTY); //ACK: seq = -1, ack = 1
        	    			Packet resendpkt = new Packet(seq,ack,check,DATA_EMPTY);
        	    			toLayer3(1,resendpkt); //send back ACK
        	    			System.out.println("bInput: send ACK 0");
    	    			}
    	    			else if(packet.getSeqnum()==1)
    	    			{
    	    				String data = packet.getPayload();
        	    			toLayer5(data);
        	    			System.out.println("bInput: layer5 received packet 1");
    	    				int seq = -1;
        	    			int ack = ACK_ACKed_1;
        	    			int check = makeCheckSum(seq,ack,DATA_EMPTY); //ACK: seq = -1, ack = 1
        	    			Packet resendpkt = new Packet(seq,ack,check,DATA_EMPTY);
        	    			toLayer3(1,resendpkt); //send back ACK
        	    			System.out.println("bInput: send ACK 1");
        	    			state_receiver = STATE_WAIT_FOR_0_FROM_BELOW;
    	    			}
    	    		}
    		}
    		*/
    }
    
    // This routine will be called once, before any of your other B-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity B).
    protected void bInit()
    {
    	System.out.println("|bInit| : expected_seq_num is: "+expected_seq_num+"."); 
    	for(int i=0;i<5;i++)
    		{
    			isAckedWindow[i] =ACK_NAKed;
    		}
    	expected_seq_num = FirstSeqNo;
    	int check = makeCheckSum(FirstSeqNo,ACK_ACKed,DATA_EMPTY);
    	packetBuffer = new Packet (-1,ACK_ACKed,check,DATA_EMPTY);
    	System.out.println("|bInit| : packet with seq number:"+Integer.toString(packetBuffer.getSeqnum())+" is stored in Buffer");  
    	//	state_receiver = STATE_WAIT_FOR_0_FROM_BELOW;
    }

    // Use to print final statistics
    protected void Simulation_done()
    {
    	// TO PRINT THE STATISTICS, FILL IN THE DETAILS BY PUTTING VARIBALE NAMES. DO NOT CHANGE THE FORMAT OF PRINTED OUTPUT
    	System.out.println("\n\n===============STATISTICS=======================");
    	System.out.println("Number of original packets transmitted by A:" + "<YourVariableHere>");
    	System.out.println("Number of retransmissions by A:" + "<YourVariableHere>");
    	System.out.println("Number of data packets delivered to layer 5 at B:" + "<YourVariableHere>");
    	System.out.println("Number of ACK packets sent by B:" + "<YourVariableHere>");
    	System.out.println("Number of corrupted packets:" + "<YourVariableHere>");
    	System.out.println("Ratio of lost packets:" + "<YourVariableHere>" );
    	System.out.println("Ratio of corrupted packets:" + "<YourVariableHere>");
    	System.out.println("Average RTT:" + "<YourVariableHere>");
    	System.out.println("Average communication time:" + "<YourVariableHere>");
    	System.out.println("==================================================");

    	// PRINT YOUR OWN STATISTIC HERE TO CHECK THE CORRECTNESS OF YOUR PROGRAM
    	System.out.println("\nEXTRA:");
    	// EXAMPLE GIVEN BELOW
    	//System.out.println("Example statistic you want to check e.g. number of ACK packets received by A :" + "<YourVariableHere>"); 
    }	

}
