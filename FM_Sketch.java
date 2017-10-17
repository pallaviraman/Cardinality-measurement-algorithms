import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.io.IOException;


public class FM_Sketch {
	
	private static double phi = 0.77351;
	private static double Powerof = 256;
	private static int MAX_FLOW = 300000;
	private static String fileName = System.getProperty("user.home")+"/FMSketch_results.csv";
	
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
		int Bitmap_Size = 32;
		int num_bitmaps = 32;
		System.out.println("Flow traffic start");
		try {
			Scanner sc = new Scanner(new FileReader("FlowTraffic.txt"));
			int[][] Bit_Map = new int[num_bitmaps][Bitmap_Size];  
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

				int rowindex =0;
				int columnindex =0;
				
				for (int i = 0; i < num_bitmaps; i++) {
					for(int j =0; j< Bitmap_Size; j++){
						Bit_Map[i][j] = 0;
					}
				}
				
				//System.out.println("Flow size " +flow.size());
				//Online Operation
				for (Long val: hash_flow.get(ip)) {
					rowindex =0;
					for(int i=0;i<5;i++)
					{
						long mask = 1<<i;
						long maskindex = val & mask;
						long bit = maskindex>>i;
						rowindex += bit*(java.lang.Math.pow(2,i));
					}
					for (int i = 5 ; i != Bitmap_Size ; i++) {
				        if ((val & (1 << i)) != 0) {
				            columnindex = i-5;
				            break;
				        }
				    }
					Bit_Map[rowindex][columnindex] = 1;
				}
				//Offline Operation
				double sum =0;
				for(int i =0; i< num_bitmaps;i++)
				{
					for(int j=0;j<Bitmap_Size;j++)
					{
						int rho =0;
						int mask = 1;
						while((mask & Bit_Map[i][j])!=0 && rho<Bitmap_Size)
						{
							rho++;
							sum++;
							mask = 1<<rho;
						}
					}
				}
				//double expectedvalue = (num_bitmaps / phi) * java.lang.Math.pow(2,(sum /num_bitmaps)); 
				double expectedvalue = (num_bitmaps / phi) * (java.lang.Math.pow(2,(sum /num_bitmaps))-java.lang.Math.pow(2,(-1.57 * sum /num_bitmaps)));
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
