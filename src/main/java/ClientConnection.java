import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONObject;

public class ClientConnection {
	private  BufferedReader reader;
	private  PrintWriter writer;
	private  Socket socket;
	public ClientConnection(){
		
	}
	public  void connect(int port , String address)throws IOException{
		socket = new Socket(address, port);
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new PrintWriter(socket.getOutputStream(), true);
		for(int i = 0; i < 2; i++)
			System.out.println(reader.readLine());

		
	}
	private  void writeTest() throws IOException{
		try{
			while(true){
				BufferedReader stdReader = new BufferedReader(new InputStreamReader(System.in));
				
				String input = stdReader.readLine();
				writer.println("message: " + input);
				try{
					String response = reader.readLine();
					//System.err.println("server response: " + response);
					if(response == null || response.equals("")){
						System.out.println("Closing connection to server");
						break;
					}
				}
				catch(IOException e){
					System.out.println("Connection error");
				}
			}
		}
		finally{
			socket.close();
		}
	}
	public String sendToServer(JSONObject request) throws IOException{
		writer.println(request.toString());
		String response = reader.readLine();
		if(response == null || response.equals(""))
			return null;
		return response;
	}
	public void closeConnection() throws IOException{
		socket.close();
	}
//	public static void main(String[] args) throws IOException{
//		ClientConnection cc = new ClientConnection();
//		cc.connect(2000, "127.0.0.1");
//		cc.writeTest();
//		
//	}
	
}

