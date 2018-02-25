public class Packet
{
    private int seqnum;
    private int acknum;
    private int checksum;
    private String payload;
    private int []sack;
    
    public Packet(Packet p)
    {
        seqnum = p.getSeqnum();
        acknum = p.getAcknum();
        checksum = p.getChecksum();
        payload = new String(p.getPayload());
        sack = p.getSack();
    }
    
    public Packet(int seq, int ack, int check, String newPayload)
    {
        seqnum = seq;
        acknum = ack;
        checksum = check;
        int[] sackAry = new int[]{-1,-1,-1,-1,-1};
        sack = sackAry;
        if (newPayload == null)
        {
            payload = "";
        }        
        else if (newPayload.length() > NetworkSimulator.MAXDATASIZE)
        {
            payload = null;
        }
        else
        {
            payload = new String(newPayload);
        }
    }
    
    
    
    public Packet(int seq, int ack, int check, String newPayload,int[] sackAry)
    {
        seqnum = seq;
        acknum = ack;
        checksum = check;
        sack = sackAry;
        if (newPayload == null)
        {
            payload = "";
        }        
        else if (newPayload.length() > NetworkSimulator.MAXDATASIZE)
        {
            payload = null;
        }
        else
        {
            payload = new String(newPayload);
        }
    }
    
    public Packet(int seq, int ack, int check)
    {
        seqnum = seq;
        acknum = ack;
        checksum = check;
        int[] sackAry = new int[] {-1,-1,-1,-1,-1};
        sack = sackAry;
        payload = "";
    } 
    
    public Packet(int seq, int ack, int check,int[] sackAry)
    {
        seqnum = seq;
        acknum = ack;
        checksum = check;
        sack = sackAry;
        payload = "";
    }    
        

    public boolean setSeqnum(int n)
    {
        seqnum = n;
        return true;
    }
    
    public boolean setAcknum(int n)
    {
        acknum = n;
        return true;
    }
    
    public boolean setChecksum(int n)
    {
        checksum = n;
        return true;
    }
    
    public boolean setPayload(String newPayload)
    {
        if (newPayload == null)
        {
            payload = "";
            return false;
        }        
        else if (newPayload.length() > NetworkSimulator.MAXDATASIZE)
        {
            payload = "";
            return false;
        }
        else
        {
            payload = new String(newPayload);
            return true;
        }
    }
    
    public int getSeqnum()
    {
        return seqnum;
    }
    
    public int getAcknum()
    {
        return acknum;
    }
    
    public int getChecksum()
    {
        return checksum;
    }
    
    public String getPayload()
    {
        return payload;
    }
    
    public int[] getSack() 
    {
    		return sack;
    }
    
    public String toString()
    {
        return("seqnum: " + seqnum + "  acknum: " + acknum + "  checksum: " +
               checksum + "  payload: " + payload);
    }
    
}
