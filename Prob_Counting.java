import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.IOException;

public class Prob_Counting {

	private static double Powerof = 256;

	private static int MAX_FLOW = 3000000;
	private static String fileName = System.getProperty("user.home")+"/Probabilistic_results.csv";

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
			//System.out.println(num[i]);
			k = Math.pow(Powerof, j);
			result += (num[i]*k);
			j++;
		}


		return result;
	}

	public static void main(String[] args) {

		/* To track n and n^ */
		/* To count the actual n in each flow */
		Set<String> srcIP_String_Set = new HashSet<String>();
		Set<String> dstIP_String_Set = new HashSet<String>();
		HashMap<String,ArrayList<String>> input = new HashMap<String,ArrayList<String>>();
		HashMap<String,Double> actual_count = new HashMap<String,Double>();
		HashMap<String,ArrayList<Long>> hash_flow = new HashMap<String,ArrayList<Long>>();
		HashMap<String,Double> final_count = new HashMap<String,Double>();
		long IP_Addr_num = 0;
		int Bitmap_Size = 0;

		System.out.println("Flow traffic start");

		try {
			Scanner sc = new Scanner(new FileReader("FlowTraffic.txt"));

			//System.out.println("Enter the size of bitmap");
			//Scanner in_read = new Scanner(System.in);

			Bitmap_Size = 10000;

			int[] Bit_Map = new int[Bitmap_Size];  

			sc.nextLine();

			//int num_of_flows = MAX_FLOW/FLOW_SIZE;
			
			//num_of_flows = 1;

			/* Divide the stream into flows */
				int j = 0;
				double count =0;
				ArrayList<String> dest_ip;
				ArrayList<Long> hash;
				while(sc.hasNext() && j < MAX_FLOW) {
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
						IP_Addr_num = Convert_IP_to_Int(dstip);
						hash.add(IP_Addr_num);
						hash_flow.put(srcip,hash);
					}
					else
					{
						input.get(srcip).add(dstip);
						dstIP_String_Set.add(dstip);
						count = dstIP_String_Set.size();
						actual_count.replace(srcip,count);
						IP_Addr_num = Convert_IP_to_Int(dstip);
						hash_flow.get(srcip).add(IP_Addr_num);
					}
					sc.nextLine();
					j++;
				}
				//System.out.println(IP_Addr_num);

			/* Count n and n^ for each flow	 */
				FileWriter fileWriter = null;
				try {
					fileWriter = new FileWriter(fileName);
				} catch (IOException e) {
					System.out.println("File not generated ");
					e.printStackTrace();
				} 
			for (String ip : hash_flow.keySet()) {

				for (int i = 0; i < Bitmap_Size; i++) {
					Bit_Map[i] = 0;
				}
				
				//System.out.println("Flow size " +flow.size());
				for (Long val: hash_flow.get(ip)) {
					val = val % Bitmap_Size;
					Bit_Map[val .intValue()] = 1;
					//System.out.println("Index : " + val + "Bitmap_value :" +Bit_Map[val]);
				}


				/* Count number of zeros */
				double No_of_zeros = 0;
				double Expected_num = 0;

				for (int i = 0 ; i <Bitmap_Size ; i++) {
					if(Bit_Map[i] == 0)
						No_of_zeros++;
				}
				
				//System.out.println("Number of zeros: " +No_of_zeros);

				double Vm = No_of_zeros/Bitmap_Size;

				//System.out.println("fraction of zeros: " +Vm);
				
				Expected_num = -(Bitmap_Size * Math.log(Vm));

				final_count.put(ip,Expected_num);
				
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
			//in_read.close();

		} catch (FileNotFoundException e) {
			System.out.println("Input file not found" +e);
			e.printStackTrace();
		}

	}

}
