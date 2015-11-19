import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.json.JSONException;

import junit.framework.Assert;

import org.junit.Test;


public class SyncTest {
	@SuppressWarnings("deprecation")
	public void depositSyncTest() throws InterruptedException{
		ServerTask serverT = new ServerTask("src/test/resources/core.json");
		serverT.start();
		ArrayList<SyncTest.ClientTask> clientTasks = new ArrayList<SyncTest.ClientTask>();
		for(int i = 0; i < 10; i++){
			clientTasks.add(new ClientTask("src/test/resources/terminal1.xml",  "src/test/resources/response" + i+ ".xml"));
			clientTasks.get(clientTasks.size()-1).start();
		}
			for(int i = 0; i < 10; i++)
				clientTasks.get(i).join(3000);
		
		String actual =  serverT.server.getDepositBalance(new BigDecimal("33227781")).toString();
		System.out.println("actual: " + actual);
		serverT.join(3000);

		Assert.assertEquals("2800",actual);

	}
	public static void main(String[] args){
		SyncTest stest = new SyncTest();
		try {
			stest.depositSyncTest();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private class ClientTask extends Thread{
		TransactionManagerClient client;
		String terminal;
		String response;

		public ClientTask(String terminal, String response){
			client = new TransactionManagerClient();
			this.terminal = terminal;
			this.response = response;
		}
		public void run(){
			try {
				client.start(terminal,response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	private class ServerTask extends Thread{
		public TransactionManagementServer server;
		String core;
		public ServerTask(String core){
			this.core = core;
			server = new TransactionManagementServer();
		}
		public void run(){
			try {
				server.start(core);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
