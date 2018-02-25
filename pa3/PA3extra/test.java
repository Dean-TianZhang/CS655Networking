

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class test {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		node_com n1=new node_com(1,3,5);
		node_com n2=new node_com(1,5,7);
		node_com n3=new node_com(1,1,8);
		ArrayList <node_com> list=new ArrayList<node_com>();
		list.add(n1);list.add(n2);
		list.add(n3);
		Collections.sort(list);
		for(node_com n:list)
			System.out.println(n.distance);
	}

}
