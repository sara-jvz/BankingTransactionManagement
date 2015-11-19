import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.omg.CosNaming.NamingContextExtPackage.AddressHelper;

public class TransactionManagementServer {
	private ServerConnection connection;
	private static final Logger log = Logger.getLogger( TransactionManagerClient.class.getName());
	private int port;
	private String dataFileAddress;
	private String logFileAddress;
	private ArrayList<Deposite> deposits;
	public TransactionManagementServer(){
		//dataFileAddress = "src/main/resources/core.json";
		deposits = new ArrayList<Deposite>();
	}
	public String respondTo(JSONObject input){
		//if(input.class.equals("Transaction"))
		try {
			return applyTransaction(input);

		} catch (JSONException e) {
			log.log( Level.FINE,"Json exception while getting input {0}", input.toString());
		}
		return "Error while handling transaction";
	}
	public void start(String dataFileAddress) throws IOException{
		this.dataFileAddress = dataFileAddress;

		try {
			readData();

			log.addHandler(new FileHandler(logFileAddress,false));
			log.setLevel(Level.FINE);

			connection = new ServerConnection(this, logFileAddress);
		} catch (SecurityException e) {
			log.log( Level.FINE,"Error while creating logfile");
			throw new SecurityException();
		} catch (IOException e) {
			log.log( Level.FINE,"Input/Output problem while creating log file");
			throw new IOException();
		}

		connection.connect(port);
	}
	public void handleUserInput(String input){
		System.out.println(input);
		if (input.equals("sync")){
			try {
				updateDataFile();
			} catch (IOException e) {
				log.log( Level.FINE,"IO error while sync");
			}
		}
	}
	private void updateDataFile() throws IOException{
		String updatedData = "{\"port\": " + port + ", \"deposits\":[";
//		JSONObject JSONData = new JSONObject();
		
		try {
//			JSONData.put("port", port);
//			JSONArray depos = new JSONArray();
			for (int i = 0; i < deposits.size(); i++){
//				depos.put(deposit.toJSONString());
				//JSONData.to
//				updatedData = updatedData.concat("{");
				if(i != 0)
					updatedData = updatedData.concat(",");
				updatedData = updatedData.concat(deposits.get(i).toJSONString());
				
			}
			updatedData = updatedData.concat("], \"outLog\": \"" + dataFileAddress+ "\"}");

//			JSONData.put("deposits", depos);
//			JSONData.put("outLog", dataFileAddress);
//			updatedData = JSONData.toString();//.replace("\\\"", "\"");
			
		} catch (JSONException e) {
			log.log( Level.FINE,"Json exception while creating json from deposits");

		}
		//String dataFileTest = "src/main/resources/core.json";

		FileWriter dataFile = new FileWriter(dataFileAddress, false);	
		
		synchronized (this) {
			dataFile.write(updatedData);
		}
		dataFile.close();

	}
	private void readData() throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(dataFileAddress));

		String inputDataString = "", line;
		line = reader.readLine();
		while(line != null){
			inputDataString = inputDataString.concat(line);
			line = reader.readLine();

		}
		JSONObject jsonData;

		try {
			jsonData = new JSONObject(inputDataString);

			port = jsonData.getInt("port");

			logFileAddress = jsonData.getString("outLog");

			JSONArray jsonDeposits = jsonData.getJSONArray("deposits");

			for(int i = 0; i < jsonDeposits.length(); i++){
				Deposite current = getDepositeFromJson((JSONObject) jsonDeposits.get(i));
				deposits.add(current);
				System.out.println(current.getId().toString());
				log.log( Level.FINE,"deposit added: {0}", current.toString());
			}

		} catch (JSONException e) {
			log.log( Level.FINE,"Json exception while reading {0}", dataFileAddress);
			e.printStackTrace();
		}
	}
	private Deposite getDepositeFromJson(JSONObject jsonDeposit) throws JSONException{
		String customer = jsonDeposit.getString("customer");
		BigDecimal id = new BigDecimal(jsonDeposit.getString("id"));
		BigDecimal initBalance = new BigDecimal(jsonDeposit.getString("initialBalance").replace(",", ""));
		BigDecimal upperBound = new BigDecimal(jsonDeposit.getString("upperBound").replace(",", ""));

		return new Deposite(customer, id, initBalance, upperBound);
	} 
	private String applyTransaction(JSONObject jsonTrans) throws JSONException{
		String type = jsonTrans.getString("type");
		Integer id = jsonTrans.getInt("id");
		BigDecimal depositId = new BigDecimal(jsonTrans.getString("deposit"));
		BigDecimal amount = new BigDecimal(jsonTrans.getString("amount"));

		Deposite targetDeposit = findDeposit(depositId);
		String successMsg = "transaction "+ id +" : was successful";
		String unSuccessMsg =  "transaction "+ id +" : was not successful";
		try{
			targetDeposit.applyAction(type, amount);
			log.log(Level.FINE, "transaction {0} : was successful", id);
			System.err.println(successMsg);
			return successMsg;

		} catch (NullPointerException e){
			log.log(Level.FINE, "error in transaction {0} : deposit with id " + depositId + " not found", id);
		} catch (UpperBoundExceededException e) {
			log.log(Level.FINE, "error in transaction {0} : deposit with id " + depositId + " encountered upper bound exceeded ", id);

		} catch (ValueNotValidException e) {
			log.log(Level.FINE, "error in transaction {0} : input money amount on deposit not valid", id);

		} catch (LowBalanceException e) {
			log.log(Level.FINE, "error in transaction {0} : deposit with " + depositId + " encountered low balance", id);

		} catch (UnknownAction e){
			log.log(Level.FINE, "error in transaction {0} : requested action on deposit not valid", id);

		}
		System.out.println("transaction " + id + " not successful");
		return unSuccessMsg;


	}
	public BigDecimal getDepositBalance(BigDecimal id){
		return findDeposit(id).getBalance();
	}
	private Deposite findDeposit(BigDecimal id){
		for(Deposite deposit : deposits){
			if (deposit.getId().equals(id))
				return deposit;
		}
		return null;
	}
	public static void main(String[] args){
		try {

			TransactionManagementServer manager = new TransactionManagementServer();	
			manager.start("src/main/resources/core.json");
		} catch (IOException e) {
			System.out.println("Input/Output problem");
		}
	}
}
