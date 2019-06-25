/**
 * A simulation must be started from this class.
 * usage: java Simulator [options] -c <class name>  
 * @author naohaya
 * @version 1.2
 */
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CancellationException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


public class Simulator {
	/* global variables */

	/**
	* 与えられたクラス名
	*/
	String cname;

	/**
	* 指定されたスレッド数（プロセス数，初期値は10）
	*/
	int num;

	/**
	* 時間計測用フラグ
	*/
	Boolean measure = false;

	/**
	* 故障発生用配列
	*/
	ArrayList<Integer> failure = new ArrayList<Integer>();

	/**
	* ランダム故障用フラグ
	*/
	Boolean randomFailure = false;

	/**
	* ランダム故障用故障確率
	*/
	double fprob = 0.2;

	/**
	* 静的故障用フラグ
	*/
	Boolean staticFailure = false;

	/**
	* 静的故障用故障プロセス数
	*/
	int fnum = 0;

	/**
	* プロセス故障用フラグ
	*/ 
	Boolean pfailure = false;


	/**
	* Log出力用フラグ
	*/
	Boolean log = false;
	
	public static void main(String[] args){
		Simulator sim = new Simulator();
		sim.dispatcher(args);

	}

	/**
	* コマンドライン引数を解析する	
	*/
    void parseArguments(String[] args){
    	Boolean className = false;
    	Boolean numOfProcesses = false;

        for(Integer i = 0; i < args.length; i++){
            if(Objects.equals(args[i], "-h")){
                printHelp();
                System.exit(0);
            }
            else if(Objects.equals(args[i], "-t")){
            	this.measure = true;
            }
            else if(Objects.equals(args[i], "-c")){          	
                this.cname = args[++i];
                className = true;
            }
            else if(Objects.equals(args[i], "-l")){
            	this.log = true;
            } 
            else if(Objects.equals(args[i], "-rf")){
            	if(this.staticFailure) {
            		System.out.println("Can not set random and static failure.");
            		printHelp();
            		System.exit(1);
            	}
            	this.randomFailure = true;
            	try{
            		this.fprob = Double.parseDouble(args[++i]);
            	} catch (NumberFormatException e) {
            		printHelp();
            		System.exit(1);
            	}
            }
            else if(Objects.equals(args[i], "-sf")){
            	if(this.randomFailure) {
            		System.out.println("Can not set random and static failure.");
            		printHelp();
            		System.exit(1);
            	} 

            	this.staticFailure = true;
            	try{
            		this.fnum = Integer.parseInt(args[++i]);
            	} catch (NumberFormatException e) {
            		printHelp();
            		System.exit(1);
            	}

            	
            }
            else if(Objects.equals(args[i], "-p")){

            	try{
            		//Illegal argument.
            		this.num = Integer.parseInt(args[++i]);
            	}catch(NumberFormatException e){
            		printHelp();
            		System.exit(1);
            	}

            	if(this.num < 1) {
            		// Illegal value.
            		printHelp();
            		System.exit(1);
            	}

           		numOfProcesses = true;

            }
        	else {
        		System.out.println("No such option: "+args[i]);
        		printHelp();
        		System.exit(1);
        	}
        }

        if (!numOfProcesses) {
        	num = 10;
        }
        if (this.num <= this.fnum) {
        	// if the num. of failed proc. exceeds the total num. of proc.
       		System.out.println("Too many failed processes.");
       		printHelp();
       		System.exit(1);
        }
        if (!className) {
        	printHelp();
        	System.exit(1);
        }

        // for failure
        failureConfiguration();
        
    }

    /**
    * 故障確率を返す
    */
    double getFprob(){
    	return this.fprob;
    }

    /**
    * 故障フラグを設定する
    */
 	void setFailure(boolean f) {
 		this.pfailure = f;
 	}

 	/**
 	* 故障フラグの情報を得る
 	*/
 	Boolean getFailure(){
 		return this.pfailure;
 	}

 	/**
 	*
 	*/
 	void failureConfiguration(){
 		// for failure
        if (randomFailure) {
        	for (int pnum = 0; pnum < this.num; pnum++) {
        		double d = 0.0;
        		d = Math.random();
        		if (d < getFprob()) {
        			failure.add(pnum);
        		}
        	}
        }

        // for failure
        if (staticFailure){
        	while(failure.size() < this.fnum) {
        		int p = (int) (Math.random()*this.num);
        		Iterator<Integer> iter = failure.iterator();
        		boolean dup = false;
	        	while(iter.hasNext()){
    	    		if(p == iter.next())
        				dup = true;
        		}
        		if (dup == false) {
        			failure.add(p);
        		}
        	}
        }
 	}


    /**
    * 与えられたクラスを指定された数のスレッドで実行する
    */
    void dispatcher(String[] args) {
		MeasureTime timer = new MeasureTime(); // timer
		parseArguments(args);

		
		// create a global message queue
		/**
		* メッセージ・キュー
		*/
		MessageQueue mq = new MessageQueue(getNum());
		
		// type of callee class
		Class<?>[] types = {int.class, MessageQueue.class, boolean.class};
		
		// args of callee class
		Object[] arg = {null, null, null};
		
		// add the message queue to share in all processes
		arg[1] = mq;

		ExecutorService exec = Executors.newCachedThreadPool();	
		List<Future<?>> list = new ArrayList<Future<?>>();
		if(isMeasure())
			timer.start();

		
		try{	
			for (int i = 0; i < getNum(); i++) {
				//add the id of process
				arg[0] = Integer.valueOf(i);
				
				// for failure
				Iterator<Integer> iter = failure.iterator();
				boolean fflag = false;
				while(iter.hasNext()){
					if(iter.next() == i) {
						setFailure(true);
						arg[2] = getFailure(); // failed
						fflag = true;
						break;
					}
				}
				if (fflag == false) {
					setFailure(false);
					arg[2] = getFailure(); // not failed
				}
				
				// get the constructor
				Constructor<?> cnst = Class.forName(getCname()).getConstructor(types);

				// instanciate the class then start it
				Future<?> future = exec.submit((Process)cnst.newInstance(arg));	
				list.add(future);
				
				// for failure
				Iterator<Integer> flistIter = failure.iterator(); // for failure
				if(randomFailure || staticFailure) {
					while(flistIter.hasNext()){
						if(flistIter.next() == i) {
							try{
								//long time = future.get(500, TimeUnit.MILLISECONDS);
								future.cancel(true); // return true
								future.get(50, TimeUnit.MILLISECONDS);
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (ExecutionException e){
								e.printStackTrace();
							} catch (TimeoutException e) {
								e.printStackTrace();
							} catch (CancellationException can) {
								// nothing to do
							}

						}
					}
					
				}
				

			}
		} catch (ClassNotFoundException e){
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} 

		exec.shutdown();
		// create processes as threads --obsolete--
		/*
		try {
			for (int i = 0; i < sim.getNum(); i++){
				//add the id of process
				arg[0] = Integer.valueOf(i);
				
				// get the constructor
				Constructor<?> cnst = Class.forName(sim.getCname()).getConstructor(types);

				// instanciate the class then start it
				((Process)cnst.newInstance(arg)).start();
			}
		}
		catch(RuntimeException re) {
			re.printStackTrace();
			System.err.println("Runtime Error.");
			System.exit(1);
		}
		catch(ClassNotFoundException cnf) {
			cnf.printStackTrace();
			System.err.println("Class Not Found Exception.");
			System.exit(1);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		*/



			// wait for all threads dispatched.
		try {
			for (Future<?> future : list) {
				try{
					future.get();
				} catch (CancellationException can) {

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		printSummary(); // print the execution summary.

		if (isMeasure()) {			
			timer.end();
			System.out.println("Execution Time: " + timer.getTime() + " msec.");

		}

    }

    /**
    * ヘルプを表示する
    */
    void printHelp(){
        System.out.println("java Simulator [OPTIONS] -c <Classname> [-p <Num. of processes>]");
        System.out.println("OPTIONS");
        System.out.println("    -h: Show help messages");
        System.out.println("    -t: Measure time elapsed");        
        System.out.println("    -p <num. of processes>: Give a num. of processes (greater than 1)"); 
    }

    /**
    * 実行のサマリを出力する
    */
    void printSummary(){
    	System.out.println("-----");
    	System.out.println("Execution of "+cname+".class completed with "+num+" processes.");
    	if(randomFailure||staticFailure){
    		failureSummary();
    	}

    }

    /**
    * 故障のサマリを出力する
    */
    void failureSummary(){
    	System.out.print("Failed: ");
    	Iterator<Integer> iter = failure.iterator();
    	while(iter.hasNext()){
    		System.out.print(iter.next()+" ");
    	}
    	System.out.println("");
    }

    /**
    * 指定されたスレッド数を参照する
    */
    int getNum() {
    	return this.num;
    }

    /**
    * 指定されたクラス名を参照する
    */
    String getCname(){
    	return this.cname;
    }

    /**
    * 時間計測をオプションで指定しているか確認する
    */
    Boolean isMeasure() {
    	return measure;
    }

}
