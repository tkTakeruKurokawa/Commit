public class Lock{
	private Boolean flag = false;
	private int count = 1;
	private int sendingMessages = 0;

	public synchronized void Lock(int id){
		sendingMessages++;
		if(id == 0){
			flag = false;
			try{
				while(flag == false){
					wait();
				}
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}

	public synchronized void unLock(int num){
		if(sendingMessages == num){
			flag = true;
			notifyAll();
		}
	}

	public synchronized Boolean checkCount(int num){
		if(count < num)
			return true;
		else 
			return false;
	}

	public synchronized Integer nowCount(){
		return count;
	}

	public synchronized void incCount(){
		count++;
	}
}