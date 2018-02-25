import java.io.*;

/**
 * This is the class that students need to implement. The code skeleton is provided.
 * Students need to implement rtinit(), rtupdate() and linkhandler().
 * printdt() is provided to pretty print a table of the current costs for reaching
 * other nodes in the network.
 */ 
public class Node { 
    
    public static final int INFINITY = 9999;
    
    int[] lkcost;		/*The link cost between node 0 and other nodes*/
    int[][] costs;  		/*Define distance table*/
    int nodename;               /*Name of this node*/
    private int[] nexthop=new int[4];
    public int[] curcost=new int[4];
    
    /* Class constructor */
    public Node() { }
    
    /* students to write the following two routines, and maybe some others */
    void rtinit(int nodename, int[] initial_lkcost) { 
    	lkcost=new int[4];
    	costs=new int[4][4];
    	this.nodename=nodename;
    	for(int i=0;i<4;i++){//initialize the table
    		lkcost[i]=initial_lkcost[i];
    		curcost[i]=initial_lkcost[i];
    		costs[i][0]=INFINITY;
    		costs[i][1]=INFINITY;
    		costs[i][2]=INFINITY;
    		costs[i][3]=INFINITY;
    		costs[i][nodename]=lkcost[i];//used current cost
    		if(initial_lkcost[i]==INFINITY)
    			nexthop[i]=-1;
    		else{
    			nexthop[i]=i;
    			costs[i][i]=initial_lkcost[i];
    		}
    	}   
    	printdt();
    	int i=nodename;
    	for(int j=0;j<3;j++){//inform the neighbour
    		i=(i+1)%4;
    		if(lkcost[i]!=INFINITY)//must be a neighbour
    			NetworkSimulator.tolayer2(new Packet(nodename,i,curcost));
    	}
    	System.out.println("n"+nodename+"initialization complete!");    		
    }    
    
    protected min_parameter getmin(int []com){
    	int min=0;
    	int index=0;    	
    	if(com[0]>com[1]){
    		min=com[1];
    		index=1;
    	}
    	else{
    		min=com[0];
    		index=0;
    	}
    	if(min>com[2]){
    		min=com[2];
    		index=2;
    	}
    	return new min_parameter(index,min);
    }
    
    protected boolean changed(){
    	for(int i=0;i<4;i++){
    		if(costs[i][nodename]-curcost[i]!=0)
    			return true;
    	}
    	return false;
    }
    
    protected boolean Updateminsoct(int []temp,int id){
    	int []com=new int[3];
    	int j=0;
    	min_parameter mp;
    	for(int i=0;i<4;i++){
    		costs[i][id]=temp[i];
    		
    	}
    	for(int i1=0;i1<4;i1++){//i1 for destination
    		j=0;
    		if(i1==nodename)//do nothing
    			continue;
    		if(costs[i1][nodename]==temp[i1])//  ??????????a little strange
    			continue;
    		for(int i2=0;i2<4;i2++){//i2 for neighbour
    			if(i2!=nodename){
        			com[j++]=costs[i1][i2];
        		}
    		}
    		//get the min    		
    		mp=getmin(com);
    		//nexthop[i1]=mp.index;//bug!
    		if(costs[i1][nodename]!=mp.value){//correct one!      
    			for(int i3=0;i3<4;i3++){//find index
    				if(costs[i1][i3]==mp.value)
    					nexthop[i1]=i3;
        		}
    		}    		
    		costs[i1][nodename]=mp.value;    		
    	}
    	if(changed()){
    		for(int i=0;i<4;i++)
    			curcost[i]=costs[i][nodename];
    		return true;
    	}
    	else
    		return false;
    	//com[0]=
    	/*for(int i=0;i<4;i++){
    		if(costs[i][nodename]>temp[i]){
    			costs[i][nodename]=temp[i];
    			nexthop[i]=id;
    		}
    		else;
    	}*/
    }
    
    void rtupdate(Packet rcvdpkt) {  
    	int source=rcvdpkt.sourceid;
    	int []temp=rcvdpkt.mincost;
    	int []sendcost=new int[4];//send to neighbour    	
    	for(int i=0;i<4;i++)
    		temp[i]+=lkcost[source];//new Dij
    	int i=nodename;
    	if(Updateminsoct(temp,source)){
    //		for(int k=0;k<4;k++)
    //			System.out.print("cost:"+curcost[k]+"  ");
    //		System.out.println();
    		for(int j=0;j<3;j++){//inform the neighbour
        		i=(i+1)%4;
        		if(lkcost[i]!=INFINITY){//must be a neighbour
        			for(int i2=0;i2<4;i2++){//posion reverse
        				if(nexthop[i2]==i)
        					sendcost[i2]=INFINITY;
        				else
        					sendcost[i2]=curcost[i2];
        			}
        			NetworkSimulator.tolayer2(new Packet(nodename,i,sendcost));
        		}        		
        	}//for(int i
    	}//if(Updat
    	else
    		System.out.println("********silence: n"+nodename);
    }
    
    
    /* called when cost from the node to linkid changes from current value to newcost*/
    void linkhandler(int linkid, int newcost) {
    	int oldedge=lkcost[linkid];
    	int []fakecost=new int[4];
    	lkcost[linkid]=newcost;
    	for(int i=0;i<4;i++){
    		costs[i][linkid]-=oldedge;
    		fakecost[i]=costs[i][linkid];
    	}
    	rtupdate(new Packet(linkid,58,fakecost));
    }    


    /* Prints the current costs to reaching other nodes in the network */
    void printdt() {
        switch(nodename) {
	case 0:
	    System.out.printf("                via     \n");
	    System.out.printf("   D0 |    1     2    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][1], costs[0][2],costs[0][3]);
	    System.out.printf("     1|  %3d   %3d   %3d\n",costs[1][1], costs[1][2],costs[1][3]);
	    System.out.printf("dest 2|  %3d   %3d   %3d\n",costs[2][1], costs[2][2],costs[2][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][1], costs[3][2],costs[3][3]);
	    break;
	case 1:
	    System.out.printf("                via     \n");
	    System.out.printf("   D1 |    0     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d \n",costs[0][0], costs[0][2]);
	    System.out.printf("dest 2|  %3d   %3d \n",costs[2][0], costs[2][2]);
	    System.out.printf("     3|  %3d   %3d \n",costs[3][0], costs[3][2]);
	    break;
	    
	case 2:
	    System.out.printf("                via     \n");
	    System.out.printf("   D2 |    0     1    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n",costs[0][0], costs[0][1],costs[0][3]);
	    System.out.printf("dest 1|  %3d   %3d   %3d\n",costs[1][0], costs[1][1],costs[1][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n",costs[3][0], costs[3][1],costs[3][3]);
	    break;
	case 3:
	    System.out.printf("                via     \n");
	    System.out.printf("   D3 |    0     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d\n",costs[0][0],costs[0][2]);
	    System.out.printf("dest 1|  %3d   %3d\n",costs[1][0],costs[1][2]);
	    System.out.printf("     2|  %3d   %3d\n",costs[2][0],costs[2][2]);
	    break;
        }
    }
}


















