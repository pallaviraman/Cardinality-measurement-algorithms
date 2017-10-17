//package bitmap;
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

public class bitmap {

	private static double Powerof = 256;

	/* Actual and estimated set values */
	private static List<Double> Actual_Set = new ArrayList<Double> ();
	private static List<Double> Estimated_Set = new ArrayList<Double> ();

	/* Map that maintains list of dest address for each flow, i.e each source address */
	private static HashMap<String, ArrayList<Long>> Each_flow = new HashMap<String, ArrayList<Long>>();

	/* To track n and n^ */
	private static Set<Long> Count_set = new HashSet<Long>();

	/* Bitmap and Bitmap size */ 
	private static int[] Bit_Map;
	private static int Bitmap_Size;

	/* Write into a CSV file */
	private static String fileName = System.getProperty("user.home")+"/Bitmap_results.csv";


	/* Takes String arguments and returns long value 
	 * for IP address 
	 */
	private static long Hash_IP(String IP_Addr){

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

	
	/* For Bitmap use Hashing such that it returns uniform
	 * has values across all IP addresses possible 
	 */
	
	private static long Uniform_Hash_For_Bitmap(String IP_Addr) {
			
			long hash_value = 0;
			
			hash_value = Hash_IP(IP_Addr);
		
			return hash_value;
	}
	
	/* Estimate the cardinality of the flows */
	
	private static void Estimate_Cardinality() {

		/* Parsing flows and Count n and n^ for each flow	 */
		for (String source_ip: Each_flow.keySet()) {

			ArrayList<Long> dest_list = Each_flow.get(source_ip);

			for (int i = 0; i < Bitmap_Size; i++) {
				Bit_Map[i] = 0;
			}

			/* The actual algorithm for each flow */
			for (long val:dest_list) {
				//Add into Count set
				Count_set.add(val);

				val = val % Bitmap_Size;
				Bit_Map[(int) val]++;

			}

			/* Count number of zeros */
			double No_of_zeros = 0;
			double Expected_num = 0;

			for (int i = 0 ; i <Bitmap_Size ; i++) {
				if(Bit_Map[i] == 0)
					No_of_zeros++;
			}


			double Vm = No_of_zeros/Bitmap_Size;

			Expected_num = -(Bitmap_Size * Math.log(Vm));

			/* Algorithm ends */

			//Add these numbers into lists		
			Actual_Set.add((double)Count_set.size());
			Estimated_Set.add(Expected_num);

			Count_set.clear();
		}
	}

	/* Write the Actual values and Estimated values into a *.CSV file */
	private static void Write_to_File() {
		
		FileWriter fileWriter = null;

		try {
			fileWriter = new FileWriter(fileName);

			for (int i = 0; i < Actual_Set.size(); i++) {
				fileWriter.append(Double.toString(Actual_Set.get(i)));
				fileWriter.append(",");
				fileWriter.append(Double.toString(Estimated_Set.get(i)));
				fileWriter.append("\n");
			}

		} catch (IOException e) {
			System.out.println("File not generated ");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("File Closure failed");
				e.printStackTrace();
			}
		}

	}
	public static void main(String[] args) {

		long dest_ip_addr_num = 0;
		

		System.out.println("Flow traffic start, Estimating the cardinality of flows");

		try {
			Scanner sc = new Scanner(new FileReader("FlowTraffic.txt"));

			System.out.println("Enter the size of bitmap");

			Scanner in_read = new Scanner(System.in);
			Bitmap_Size = in_read.nextInt();

			Bit_Map = new int[Bitmap_Size];  

			sc.nextLine();

			/* Divide the stream into flow as per source address */

			/* Adding into flows */
			while(sc.hasNext()) {

				String source_ip = sc.next();
				String dest_ip   = sc.next();

				dest_ip_addr_num = Uniform_Hash_For_Bitmap(dest_ip);

				if (!Each_flow.containsKey(source_ip)) {
					ArrayList<Long> dest_list = new ArrayList<Long>();
					dest_list.add(dest_ip_addr_num);
					Each_flow.put(source_ip, dest_list);

				} else {
					ArrayList<Long> dest_list = Each_flow.get(source_ip);
					dest_list.add(dest_ip_addr_num);
				}

				sc.nextLine();
			}

			
			Estimate_Cardinality();


			sc.close();
			in_read.close();

		} catch (FileNotFoundException e) {
			System.out.println("Input file not found" +e);
			e.printStackTrace();
		}


		Write_to_File();

		System.out.println("**Done with the Estimation Please check Excel file for the results**");
	}

}
