import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Virtual_Bitmap {

	private static double Powerof = 256;

	private static int MAX_FLOW = 3000000;
	private static String fileName = System.getProperty("user.home")+"/VirtualBitmap_results.csv";

	/* Takes String arguments and returns integer 
	 * Unique across all ip addresses
	 * */
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

		/* To Track flows */
		Set<String> srcIP_String_Set = new HashSet<String>();
		Set<String> dstIP_String_Set = new HashSet<String>();
		HashMap<String,ArrayList<String>> input = new HashMap<String,ArrayList<String>>();
		HashMap<String,Double> actual_count = new HashMap<String,Double>();
		HashMap<String,Double> final_count = new HashMap<String,Double>();
		
		//long IP_Addr_num = 0;
		int Bitmap_Size = 8000000;
		int virtual_bits = 2056;

		System.out.println("Flow traffic start");

		try {
			Scanner sc = new Scanner(new FileReader("FlowTraffic.txt"));

			int[] Bit_Map = new int[Bitmap_Size];  
			int[] Random = new int[virtual_bits];
			ArrayList<Integer> list = new ArrayList<>(virtual_bits);
			for (int i = 0; i < virtual_bits; i++){
			    list.add(i);
			}
			for (int count = 0; count < virtual_bits; count++){
				Random[count] = list.remove((int)(Math.random() * list.size()));
			}
			sc.nextLine();
			//int num_of_flows = MAX_FLOW/FLOW_SIZE;
			int itr = 0;
			double count =0;
			ArrayList<String> dest_ip;
				while(sc.hasNext() && itr < MAX_FLOW) {
					String srcip = sc.next();
					String dstip = sc.next();
					if(srcIP_String_Set.add(srcip))
					{
						dest_ip = new ArrayList<String>();
						dest_ip.clear();
						dstIP_String_Set.clear();
						dest_ip.add(dstip);
						dstIP_String_Set.add(dstip);
						input.put(srcip,dest_ip);
						count = dstIP_String_Set.size();
						actual_count.put(srcip,count);
						//IP_Addr_num = jenkins_hash(dstip.getBytes());
					}
					else
					{
						input.get(srcip).add(dstip);
						dstIP_String_Set.add(dstip);
						count = dstIP_String_Set.size();
						actual_count.replace(srcip,count);
						//IP_Addr_num = jenkins_hash(dstip.getBytes());
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
				for(int j =0; j< Bitmap_Size; j++){
					Bit_Map[j] = 0;
				}
				//Online operation
			for (String src : input.keySet()) {

				//System.out.println("Flow size " +flow.size());
					
				for (String dst: input.get(src)) {
					long srcip = jenkins_hash(src.getBytes());
					long dstip = jenkins_hash(dst.getBytes());
					int hash1 = Math.abs((int)(srcip ^ Random[Math.abs((int) (dstip % virtual_bits))]) % Bitmap_Size) ;
					Bit_Map[hash1] = 1;
				}
			}
			double Vm =0;
			double Um = 0;
			for(int i=0;i<Bitmap_Size;i++)
			{
				if(Bit_Map[i] == 0)
					Um++;
			}
			Vm = Um/Bitmap_Size;
			//Offline operation
			for (String src : input.keySet()) 
			{

				double Vs =0;
				double Us =0;
				long srcip = jenkins_hash(src.getBytes());
			
				int virtual_hash =0;
				if(input.get(src).size() > 30)
				{
					for(int x = 0; x < virtual_bits; x++)
					{
						virtual_hash = Math.abs((int)(srcip ^ Random[x]) % Bitmap_Size);
						if(Bit_Map[virtual_hash] == 0)
							Us++;
					}
					Vs = Us/virtual_bits;
				}
				else
				{
					continue;
				}
				double expectedvalue = Math.abs(virtual_bits * (Math.log(Vm) - Math.log(Vs)));
				final_count.put(src,expectedvalue);
				//System.out.println(actual_count.get(src));
				//System.out.println(final_count.get(src));
				try {
				fileWriter.append(Double.toString(actual_count.get(src)));
				fileWriter.append(",");
				fileWriter.append(Double.toString(final_count.get(src)));
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