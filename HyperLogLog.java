import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.io.IOException;


public class HyperLogLog {
	
	private static double phi = 0.77351;
	private static double alpha = 0.709;
	private static double Powerof = 256;
	private static int MAX_FLOW = 300000;
	private static String fileName = System.getProperty("user.home")+"/HyperLog_results.csv";
	
	/* Takes String arguments and returns integer 
	 * Unique across all ip addresses
	 * */
	private static long Convert_IP_to_Int(String IP_Addr){

		int length = 4;
		long result = 0;
		int[] num = new int[4];
		double j = 0, k = 0;
		String[] Sub_part= IP_Addr.split("\\.");

		for (int i = 0; i < length; i++) {
			num[i] = Integer.parseInt(Sub_part[i]);
		}

		for (int i = length-1; i >= 0 ; i--) {
			k = Math.pow(Powerof, j);
			result += (num[i]*k);
			j++;
		}
		return result;
	}
	private static long jenkins_hash(byte[] key) {
	    long hash = 0;

	    for (byte b : key) {
	        hash += (b & 0xFF);
	        hash += (hash << 10);
	        hash ^= (hash >>> 6);
	    }
	    hash += (hash << 3);
	    hash ^= (hash >>> 11);
	    hash += (hash << 15);
	    return hash;
	}
	
	public static void main(String[] args) {
		
		/* To Track flows */
		Set<String> srcIP_String_Set = new HashSet<String>();
		Set<String> dstIP_String_Set = new HashSet<String>();
		HashMap<String,ArrayList<String>> input = new HashMap<String,ArrayList<String>>();
		HashMap<String,Double> actual_count = new HashMap<String,Double>();
		HashMap<String,ArrayList<Long>> hash_flow = new HashMap<String,ArrayList<Long>>();
		HashMap<String,Double> final_count = new HashMap<String,Double>();
		
		long IP_Addr_num = 0;
		int bitmap_size = 64;
		System.out.println("Flow traffic start");
		try {
			Scanner sc = new Scanner(new FileReader("FlowTraffic.txt"));
			int[]bitmap = new int[bitmap_size];  
			sc.nextLine();
			//int num_of_flows = MAX_FLOW/FLOW_SIZE;
			int itr = 0;
			double count =0;
			ArrayList<String> dest_ip;
			ArrayList<Long> hash;
				while(sc.hasNext() && itr < MAX_FLOW) {
					String srcip = sc.next();
					String dstip = sc.next();
					if(srcIP_String_Set.add(srcip))
					{
						dest_ip = new ArrayList<String>();
						hash = new ArrayList<Long>();
						dest_ip.clear();
						dstIP_String_Set.clear();
						hash.clear();
						dest_ip.add(dstip);
						dstIP_String_Set.add(dstip);
						input.put(srcip,dest_ip);
						count = dstIP_String_Set.size();
						actual_count.put(srcip,count);
						IP_Addr_num = jenkins_hash(dstip.getBytes());
						hash.add(IP_Addr_num);
						hash_flow.put(srcip,hash);
					}
					else
					{
						input.get(srcip).add(dstip);
						dstIP_String_Set.add(dstip);
						count = dstIP_String_Set.size();
						actual_count.replace(srcip,count);
						IP_Addr_num = jenkins_hash(dstip.getBytes());
						hash_flow.get(srcip).add(IP_Addr_num);
					}
					sc.nextLine();
					itr++;
				}
				FileWriter fileWriter = null;
				try {
					fileWriter = new FileWriter(fileName);
				} catch (IOException e) {
					System.out.println("File not generated ");
					e.printStackTrace();
				} 
			for (String ip : hash_flow.keySet()) {

				int index1 ;
				int index2 ;
				
				for (int i = 0; i < bitmap_size; i++) {
						bitmap[i]= 0;
				}
				//Online Operation
				//System.out.println("Flow size " +flow.size());
				for (Long val: hash_flow.get(ip)) {
					index1 =0;
					index2 =0;
					for(int i=0;i<6;i++)
					{
						long mask = 1<<i;
						long maskindex = val & mask;
						long bit = maskindex>>i;
						index1 += bit*(java.lang.Math.pow(2,i));
					}
					for (int i = 6 ; i != bitmap_size ; i++) {
				        if ((val & (1 << i)) != 0) {
				            index2 = i-5;
				            break;
				        }
				    }
					int max=0;
					if(bitmap[index1]>index2)
						max = bitmap[index1];
					else
						max = index2;
					bitmap[index1]= max;
				}
				//Offline operation
				double sum =0;
				for (int i = 0; i < bitmap_size; i++) {
					sum += java.lang.Math.pow(2,-bitmap[i]);
				}
				double estexpectedvalue = alpha*java.lang.Math.pow(bitmap_size,2)* (1.0/sum);
				double expectedvalue= 0;
				double No_of_zeros = 0;
				if(estexpectedvalue < 5/2 * bitmap_size)// # small range correction
				{
					//System.out.println(estexpectedvalue);
					for (int i = 0 ; i <bitmap_size ; i++) {
						if(bitmap[i] == 0)
							No_of_zeros++;
					}
					if(No_of_zeros == 0)
					{
						expectedvalue = estexpectedvalue;
					}
					else
					{
						expectedvalue = bitmap_size * Math.log(bitmap_size/No_of_zeros);
					}
				}
				else if(estexpectedvalue <= (1/30)*(java.lang.Math.pow(2,32)) ) // # intermediate range, no correction
				{
					expectedvalue = estexpectedvalue;
				}
				//if(estexpectedvalue > (1/30)*(java.lang.Math.pow(2,32))) // # large range correction
				else
				{
					expectedvalue = -(java.lang.Math.pow(2,32))*Math.log(1 -(estexpectedvalue/java.lang.Math.pow(2,32) ));
				}
				//System.out.println(expectedvalue);
				final_count.put(ip,expectedvalue);
				//System.out.println(actual_count.get(ip));
				//System.out.println(final_count.get(ip));
				try {
				fileWriter.append(Double.toString(actual_count.get(ip)));
				fileWriter.append(",");
				fileWriter.append(Double.toString(final_count.get(ip)));
				fileWriter.append("\n");
				} catch (IOException e) {
					System.out.println("File not generated ");
					e.printStackTrace();
				}
				
			}
			try {
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("File Closure failed");
				e.printStackTrace();
			}
			sc.close();
		}
		catch (FileNotFoundException e) {
			System.out.println("Input file not found" +e);
			e.printStackTrace();
		}
		//System.out.println("Given list Size: " +flow_list.size());

	}
}
