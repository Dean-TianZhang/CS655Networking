
public class node_com implements Comparable<node_com>{
	public int index=-1;
	public int distance=-1;
	public int head=-1;
	
	node_com(int i,int d,int h){
		index=i;
		distance=d;
		head=h;
	}

	@Override
	public int compareTo(node_com other) {
		// TODO Auto-generated method stub
		if(distance<other.distance)
			return -1;
		if(distance<other.distance)
			return 1;
		else
			return 0;		
	}
}
