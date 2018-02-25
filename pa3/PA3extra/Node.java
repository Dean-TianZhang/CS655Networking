
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;


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
    private int []curheader=new int[4];
    private int [][]header=new int[4][4];//-1 means null here
    public int []curcost=new int[4];
    private int[] nexthop=new int[4];
    //private boolean[]determine=new boolean[4]; 
    private int[] index1=new int[4];
//    private table_ds []rout_t=new table_ds[4];
    /* Class constructor */
    Node() { }
    
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
    		costs[i][nodename]=lkcost[i];//used current cost//c
    		costs[i][i]=initial_lkcost[i];//c
    		//rout_t[i].distance=initial_lkcost[i];//c
    		header[i][0]=-1;
    		header[i][1]=-1;
    		header[i][2]=-1;
    		header[i][3]=-1;
    		curheader[i]=-1;    		
    		//determine[i]=false;    		
    		//meaningless
    		if(initial_lkcost[i]==INFINITY){
    			nexthop[i]=-1;
    		//	rout_t[i].nexthop=-1;//c
    		//	rout_t[i].head=-1;//c
    			index1[i]=-1;
    		}
    		else{
    			nexthop[i]=i;
    			//rout_t[i].nexthop=i;//c
    			curheader[i]=nodename;//for source point,is that ok?    			
    			//rout_t[i].head=nodename;//c    			
    			header[i][i]=nodename;//c    			
    			index1[i]=i;
    			header[i][nodename]=nodename;
    		}
    		curheader[nodename]=-1;//remedy!bug!!!!!!!!!
    		header[nodename][0]=-1;
    		header[nodename][1]=-1;
    		header[nodename][2]=-1;
    		header[nodename][3]=-1;//bug
    	}
    	//curheader[nodename]=-1;//remedy!bug!!!!!!!!!
    	printdt();
    	int i=nodename;
    	for(int j=0;j<3;j++){//inform the neighbour
    		i=(i+1)%4;
    		if(lkcost[i]!=INFINITY)//must be a neighbour
    			NetworkSimulator.tolayer2(new Packet(nodename,i,curcost,curheader));
    	}
    	System.out.println("n"+nodename+"initialization complete!");
    }    

    
    protected boolean changed(){
    	for(int i=0;i<4;i++){
    		if(costs[i][nodename]!=curcost[i] || header[i][nodename]!=curheader[i] || nexthop[i]!=index1[i])
    			return true;
    	}
    	return false;
    }
    
    protected void rt_update(){
    	int h=0;
    	boolean visit=false;
    	//for(int i=0;i<4;i++)
    		//determine[i]=false;// not used here
    	System.out.println("in_rt_update!");
    	for(int i=0;i<4;i++){
    		if(i==nodename)
    			continue;
    		if(costs[i][nodename]>=INFINITY){//do not need to check
    			curheader[i]=-1;
    			curcost[i]=INFINITY;
    			nexthop[i]=-1;
    			continue;
    		}    			
    		//h=header[i][nodename];
    		visit=false;
    		h=i;
    		//int count=0;
    		while(true){
    			/*if(count<10){
	    			System.out.println("break!");
	    			System.out.println("h:"+h);
	    			count++;
    			}*/
    			if(costs[h][nodename]!=costs[h][index1[i]]||h==nodename)//different from the paper/*h==index1[i]||*/
    				break;
    			else if(h==index1[i])
    				visit=true;
    			h=header[h][nodename];
    		}
    		/*if(h==index1[i]){//not definitely valid
    			if(header[h][nodename]==nodename){//valid
	    			curcost[i]=costs[i][nodename];
	    			curheader[i]=header[i][nodename];
	    			nexthop[i]=index1[i];
	    			//nexthop[i]=index1[i];
    			}
    			else//?
    		}*/
    		if(h==nodename&&visit==true){
    			curcost[i]=costs[i][nodename];
    			curheader[i]=header[i][nodename];
    			nexthop[i]=index1[i];
    		}
    		else{
    			curcost[i]=INFINITY;
				curheader[i]=-1;
				nexthop[i]=-1;//invalid
    			/*if(h==nodename){//invalid
    				//curcost[i]=costs[i][nodename];
	    			//curheader[i]=header[i][nodename];
	    			//nexthop[i]=index1[i];
	    			curcost[i]=INFINITY;
    				curheader[i]=-1;
    				nexthop[i]=-1;//invalid
    			}    				
    			else{//transient period,waiting for new message
    				curcost[i]=INFINITY;
    				curheader[i]=-1;
    				nexthop[i]=-1;//invalid
    			}*/
    		}
    	}
    }
    
    protected boolean Need_change(int i){
    	if(curcost[i]==costs[i][nexthop[i]] && curheader[i]==header[i][nexthop[i]])
    		return false;
    	else
    		return true;
    }
    
    protected boolean Updateminsoct(/*int []temp,int id,int[]temphead*/){
    	/*int []com=new int[3];
    	int []com_s=new int[3];
    	int []com_i=new int[3];*/
    	//int j=0;
    	ArrayList <node_com> list=new ArrayList<node_com>();
    	//min_parameter mp;    	
    	//int []index1=new int[4];
    	/*for(int i=0;i<4;i++){
    		costs[i][id]=temp[i];
    		header[i][id]=temphead[i];
    	}*/
    	for(int i1=0;i1<4;i1++){//i1 for destination
    		//j=0;
    		if(i1==nodename)//do nothing
    			continue;
    		/*if(costs[i1][nodename]==temp[i1])//  ??????????a little strange
    			continue;*/
    		list=new ArrayList<node_com>();
    		for(int i2=0;i2<4;i2++){//i2 for neighbour
    			if(i2!=nodename){
        			/*com[j]=costs[i1][i2];
        			com_s[j]=header[i1][i2];
        			com_i[j]=i2;
        			j++;*/
        			list.add(new node_com(i2,costs[i1][i2],header[i1][i2]));
        		}
    		}
    		//get the min    		
    		Collections.sort(list);
    		if(list.get(0).distance==curcost[i1]){    			
    			if(Need_change(i1)){//
    				index1[i1]=list.get(0).index;
	    			//costs[i1][nodename]=mp.value;
	        		costs[i1][nodename]=list.get(0).distance;
	        		//header[i1][nodename]=com_s[index1[i1]];
	        		header[i1][nodename]=list.get(0).head;
    			}
    			else
    				;
    		}
    		else{
    			//costs[i1][nodename]=mp.value;
        		costs[i1][nodename]=list.get(0).distance;
        		//header[i1][nodename]=com_s[index1[i1]];
        		header[i1][nodename]=list.get(0).head;
    			//index1[i1]=com_i[mp.index];//problem
        		index1[i1]=list.get(0).index;
    		}    		
    		//mp=getmin(com);//discard
    		//nexthop[i1]=mp.index;//bug
    	//	int index=0;    		
    		/*if(costs[i1][nodename]!=mp.value){//correct one!      
    			for(int i3=0;i3<4;i3++){//find index
    				if(costs[i1][i3]==mp.value){
    					header[i1][nodename]=com_s[i3];
    					index1[i1]=i3;//index is very important here!!!
    					break;
    				}    				
        		}
    		}
    		else{
    //			index1[i1]=nexthop[i1];//
    		}*/    		
    	}    	
    	if(changed()){
    		rt_update();
    		//for(int i=0;i<4;i++)
    			//curcost[i]=costs[i][nodename];
    		return true;
    	}
    	else
    		return false;    	
    }
    
    
    protected boolean in_path(int nei,int des){
    	int h=-1;    	
    	h=curheader[des];
    	if(des==nodename)//??
			return false;
    	if(h==-1)//can not reach by this node
			return false;
    	h=des;
    	while(true){    		    		 
			if(h==nodename)//not in
				return false;
    		else{
    			if(h==nei)//in path!
    				return true;
    			else
    				h=curheader[h];
    		
    		}    			
    	}
    }
    
    void rtupdate(Packet rcvdpkt) {  
    	int source=rcvdpkt.sourceid;
    	
    	int []temp=rcvdpkt.mincost;
    	int []temphead=rcvdpkt.header;
    	
    	int []sendcost=new int[4];//send to neighbour
    	int []sendheader=new int[4];//send to neighbour
    	
    	for(int i=0;i<4;i++){//update distance table
    		if(temp[i]!=INFINITY)
    			temp[i]+=lkcost[source];//new Dij
    		costs[i][source]=temp[i];
    	//	System.out.println("distance:"+temp[i]+" ,head:"+temphead[i]);
    		if(temp[i]<INFINITY&&temphead[i]==-1){    			
    		//	System.out.println("get -1!!!!!!!!");
    			header[i][source]=nodename;
    		}
    		else
    			header[i][source]=temphead[i];
    	}
    	int i=nodename;
    	if(Updateminsoct()){
    //		for(int k=0;k<4;k++)
    //			System.out.print("cost:"+curcost[k]+"  ");
    //		System.out.println();
    		for(int j=0;j<3;j++){//inform the neighbour,j only serve as a counter
        		i=(i+1)%4;	
        		if(lkcost[i]!=INFINITY){//must be a neighbour
        			//for(int i2=0;i2<4;i2++){//pathi_in
    				for(int i3=0;i3<4;i3++){//check whther a neighbour is in a path
    					/*if(i3==nodename){
    						sendheader[i3]=-1;
        					sendcost[i3]=0;
    					}*/
        				if(in_path(i,i3)){
        					sendheader[i3]=-1;
        					sendcost[i3]=INFINITY;
        				}
        				else{
        					sendheader[i3]=curheader[i3];
        					sendcost[i3]=curcost[i3];
        				}
    				}
        				/*if(nexthop[i2]==i)
        					sendcost[i2]=INFINITY;
        				else
        					sendcost[i2]=curcost[i2];*/
        			//}
        			NetworkSimulator.tolayer2(new Packet(nodename,i,sendcost,sendheader));
        		}        		
        	}//for(int i
    	}//if(Updat
    	else
    		System.out.println("********silence: n"+nodename);
    	printpath();
    }
    
    
    /* called when cost from the node to linkid changes from current value to newcost*/        
    void linkhandler(int linkid, int newcost) {
    	int oldedge=lkcost[linkid];
    	int []fakecost=new int[4];
    	lkcost[linkid]=newcost;
    	int []fakehead=new int[4];
    	for(int i=0;i<4;i++)
    		fakehead[i]=header[i][linkid];
    	for(int i=0;i<4;i++){
    		costs[i][linkid]-=oldedge;
    		fakecost[i]=costs[i][linkid];
    	}
    	rtupdate(new Packet(linkid,58,fakecost,fakehead));
    }

    void printpath(){
    	Stack<Integer> s = new Stack<Integer>();
    	int temp=-1;
    	for(int i=0;i<4;i++){
    		if(i==nodename){
    			System.out.println("From "+nodename+" to "+nodename+":"+nodename);    			
    		}
    		else{
    			if(curheader[i]==-1)
    				System.out.println("From "+nodename+" to "+i+": Not determined!");
    			else{
    				temp=curheader[i];
    				s.push(i);
    				while(temp!=nodename){
    					s.push(temp);
    					temp=curheader[temp];
    				}
    				s.push(nodename);
    				System.out.print("From "+nodename+" to "+i+": ");
    				int size=s.size();
    				for(int j=0;j<size;j++){
    					if(j==size-1)
    						System.out.print(s.pop());
    					else
    						System.out.print(s.pop()+"->");
    				}
    				System.out.println();
    			}
    		}
    		//curheader[0]=1;
    	}
    }
    
    
    /* Prints the current costs to reaching other nodes in the network */
    void printdt() {
        switch(nodename) {
	case 0:
	    System.out.printf("                via     \n");
	    System.out.printf("   D0 |    1     2    3 \n");
	    System.out.printf("  ----|-----------------\n");
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