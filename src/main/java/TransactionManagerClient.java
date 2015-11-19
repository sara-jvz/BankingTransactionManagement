import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class TransactionManagerClient{
	private static final Logger log = Logger.getLogger( TransactionManagerClient.class.getName() );
	private ClientConnection connection;
	private Integer serverPort;
	private String serverAddress;
	private Integer terminalId;
	private String type;
	private ArrayList<String> responses;
	private ArrayList<JSONObject> transactions;
	private String dataFileAddress;
	private String logFileAddress;
	private String responseFileAddress;
	public TransactionManagerClient() {
		connection = new ClientConnection();
		responses = new ArrayList<String>();
		transactions = new ArrayList<JSONObject>();
		//dataFileAddress = "src/main/resources/terminal.xml";
		//responseFileAddress = "response.xml";
	}
	public void start(String dataFileAddress, String responseFileAddress) throws IOException{
		this.dataFileAddress = dataFileAddress;
		this.responseFileAddress = responseFileAddress;
		
		try {
			readData();
		} catch (ParserConfigurationException e) {
			System.out.println("Cannot create XML parser");
		} catch (SAXException e) {
			System.out.println("Cannot parse terminal.xml");
			e.printStackTrace();
		}
		log.addHandler(new FileHandler(logFileAddress,false));
		log.setLevel(Level.FINE);

		connection.connect(serverPort, serverAddress);
		sendTransactionRequests();
		saveResponses();
	}
	private void sendTransactionRequests() throws IOException{
		for(JSONObject transaction : transactions) {
			try {
				log.log( Level.FINE,"sending transaction {0} to server", transaction.toString());

				responses.add(connection.sendToServer(transaction));
			} catch (IOException e) {
				log.log( Level.FINE,"Input/Output problem while requesting a transaction");
				throw new IOException();
			}
		}
	}
	private void saveResponses(){
		Document dom;

	    try {
		    DocumentBuilderFactory dbfact = DocumentBuilderFactory.newInstance();

	        DocumentBuilder db = dbfact.newDocumentBuilder();
	        dom = db.newDocument();
	        
	        Element respondXML = dom.createElement("transactions");

	       

	        for(String response : responses){
			    Element currElement = dom.createElement("transaction");
				currElement.appendChild(dom.createTextNode(response));
			    respondXML.appendChild(currElement);
	        }
	        dom.appendChild(respondXML);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(new DOMSource(dom), 
                    new StreamResult(new FileOutputStream(responseFileAddress)));
				
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  catch (TransformerException e){
				log.log( Level.FINE, "TransformerException encountered while writing response.xml");
				e.printStackTrace();


			}catch (FileNotFoundException e){
				log.log( Level.FINE, "FileNotFoundException encountered while writing response.xml");
				e.printStackTrace();


			}
			
	
	}
	private void readData() throws ParserConfigurationException, SAXException, IOException{
//		log.log( Level.FINE, "entring readData");
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		File dataFile = new File(dataFileAddress);

		SAXHandler handler = new SAXHandler();
		try {
			parser.parse(dataFile, handler);
		} catch (IOException e) {
//			log.log( Level.FINE, "IOException while parsing terminal.xml {0}", e.getStackTrace());
//			e.printStackTrace();
			System.err.println("IOException while parsing terminal.xml");
			throw new IOException();
		}
		log.log( Level.FINE, "Data parsed from terminal.xml");
		serverAddress = handler.serverAddress;
		serverPort = handler.serverPort;
		terminalId = handler.terminalId;
		type = handler.type;
		logFileAddress = handler.outLogPath;
		transactions = new ArrayList<JSONObject>(handler.transactions);
//		log.log( Level.FINE, "Data imported from terminal.xml");
	
		serverAddress = handler.serverAddress;
		
	}
	public static void main(String[] args)
	{
		TransactionManagerClient manager = new TransactionManagerClient();
		try {
			manager.start("src/main/resources/terminal.xml", "src/main/resources/response.xml");
		} catch (IOException e) {
			System.out.println("Input/Output problem");
		}
	}
}
