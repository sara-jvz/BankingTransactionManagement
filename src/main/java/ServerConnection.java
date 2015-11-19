import java.io.BufferedReader;

import org.json.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerConnection {
	private static final Logger log = Logger.getLogger( TransactionManagerClient.class.getName() );
	private TransactionManagementServer transactionManagement;
	public ServerConnection(TransactionManagementServer tm, String logFileAddress) throws SecurityException, IOException {
		transactionManagement = tm;
		log.addHandler(new FileHandler(logFileAddress,false));
		log.setLevel(Level.FINE);

	}
	public void connect(int port) throws IOException{
		ServerSocket serverSocket = new ServerSocket(port);
		int clientNumber = 0;
		try{
			UserInputTask userInput = new UserInputTask();
			userInput.start();
			while(true){
				Socket currSocket = serverSocket.accept();
				clientNumber ++;
				ClientTask currClientTask = new ClientTask(currSocket, clientNumber);
				currClientTask.start();
			}
		}
		finally{
			serverSocket.close();
		}
	}
	private class UserInputTask extends Thread{
		public void run(){
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			try {
				while(true)
				{
					String input = reader.readLine();
					transactionManagement.handleUserInput(input);
				}
			} catch (IOException e) {
				log.log( Level.FINE,"standart input IO Error");
			}
			
		}
	}
	private class ClientTask extends Thread{
		private int clientNumber;
		private Socket socket;
		BufferedReader reader;
		PrintWriter writer;
		public ClientTask(Socket socket, int clientNum){
			this.socket = socket;
			this.clientNumber = clientNum;
		}
		private void printWelcome(){
			writer.println("client:" + this.clientNumber);
			writer.println("to quit, print exit");			
		}
		public void run(){
			
			try {
				//System.err.println("in run");
				 reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));				
				 writer = new PrintWriter(socket.getOutputStream(),true);
				printWelcome();
				
				while(true){
					//System.err.println("before reading");
					String input = reader.readLine();
					log.log( Level.FINE,"raw input: {0}", input);
					
					//TODO: log input
					if(input == null || input.equals("exit")){
						System.out.println("Client" + clientNumber + " closing connection");
						log.log( Level.FINE,"Client {0} closing connection", clientNumber);

						break;
					}
					JSONObject inputJSON = new JSONObject(input);

					String respond = transactionManagement.respondTo(inputJSON);
					log.log( Level.FINE,"input: {0}", inputJSON.toString());
					log.log( Level.FINE,"respond: {0}", respond);

					writer.println(respond);
				}
			} catch (IOException e) {
				System.out.println("Error on handling connection on client " + clientNumber);
			} catch(JSONException e){
				System.out.println("Client data format not valid");
			}
			finally{
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("Error on closing connection for client " + clientNumber);
				}
			}
			
		}
	}
}
