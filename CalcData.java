import java.io.*;
import java.util.*;

public class CalcData{
	String proceses;
	Integer trials;
	ArrayList<Integer> list = new ArrayList<Integer>();

	void run(String[] args) throws IOException{	
		BufferedReader file = new BufferedReader(new FileReader(args[0]));
		trials = Integer.parseInt(args[1]);

		String line;
		while((line = file.readLine()) != null){
			String[] words = line.split("\\s");
			Integer count = 1;
			for(String s: words){
				if(count == 3){
					list.add(Integer.parseInt(s));
				}
				count++;
			}
		}
		file.close();
		numOfProcess(args[0]);
		calculator();
	}

	void numOfProcess(String args){
		Integer size,num;
		char[] c = args.toCharArray();

		if(Objects.equals(c[6],'0')){
			size = 3;
			num = 7;
		}
		else{
			size = 2;
			num = 6;
		}
		char[] c2 = new char[size];

		for(Integer i = 0; i<num; i++){
			if(i == 4)
				c2[0] = c[i];
			if(i == 5)
				c2[1] = c[i];
			if(i == 6){
				c2[2] = c[i];
			}
		}
		proceses = String.valueOf(c2);
	}

	void calculator(){
		Double total = 0.0;
		Integer max = list.get(0); 
		Integer min = list.get(0);;

		for(Integer i: list){
			total += i;
			if(max < i)
				max = i;
			if(min > i)
				min = i;
		}
		total= total / trials;

		System.out.println((Integer.parseInt(proceses)) + "," + total + "," + max + "," + min);
	}


	public static void main(String[] args) throws IOException{
		CalcData cal = new CalcData();
		cal.run(args);
	}
}