/**
 * The test algorithm
 * Every process sends a message to the coordinator.
 * The coordinator waits messages from majority of processes.
 * 
 *   @author naohaya
 */


public class TwoPhaseCommit extends Process {
	int id;
	Object c = null;
	final int num = super.getMessageQueue().getTotalNum();
	final static Lock lock = new Lock();

	public TwoPhaseCommit(int id, MessageQueue mq) {
		/*
		 * call the constructor of superclass.
		 */
		super(id,mq);
		
		this.id = id;
	}

	public TwoPhaseCommit(int id, MessageQueue mq, boolean failure) {
		/*
		 * call the constructor of superclass.
		 */
		super(id,mq,failure);

		this.id = id;
	}

	public void sendRequest(){
		if(id == 0){
			for(int i = 1; i<num; i++){
				System.out.println("p" + id + " send REQUEST to p" + i);
				send(i, new DefaultMessage(id, new Object()));
			}
		}
	}

	public void sendAck(){
		lock.Lock(id);
		while(lock.checkCount(num)){
			if((c = receive()) != null){
				System.out.println("p" + id + " send ACK to p" +  ((Message) c).getSource());
				send(0, new DefaultMessage(id, new Object()));
				lock.incCount();
			} else {
				yield();
			}
		}
		lock.unLock(num);
	}

	public void sendCommit(){
		if((lock.nowCount() == num) && (id == 0)){
			for(int i = 1; i<num; i++){
				System.out.println("p" + id + " send COMMIT to p" +  i);
			}
		}
	}


	public void run(){
		/*
		 * call run() method in superclass.
		 * **必須**
		 */
		super.run();	
		
		/*
		 * 以下アルゴリズム本体
		 */

		sendRequest();
		sendAck();
		sendCommit();
	}
}
