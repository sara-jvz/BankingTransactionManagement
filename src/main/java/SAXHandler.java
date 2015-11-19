import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


class SAXHandler extends DefaultHandler{
	private static final Logger log = Logger.getLogger( TransactionManagerClient.class.getName() );

	public int terminalId;
	public String type;
	public int serverPort;
	public String serverAddress;
	public String outLogPath;
	public ArrayList<JSONObject> transactions = new ArrayList<JSONObject>();
	public JSONObject currTrans;
	public SAXHandler() throws SecurityException, IOException{
		log.addHandler(new FileHandler("a.log",false));
		log.setLevel(Level.FINE);
		
	}
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		log.log( Level.FINE, "XML Parser: starting {0}", qName);
		if(qName.equalsIgnoreCase("terminal")) {
//			System.err.println("in terminal tag");
			terminalId = Integer.parseInt(attributes.getValue("id"));
			type = attributes.getValue("type");
		}
		if(qName.equalsIgnoreCase("server")){
			serverAddress = attributes.getValue("ip");
			serverPort = Integer.parseInt(attributes.getValue("port"));
		}
		if(qName.equalsIgnoreCase("outLog")){
			outLogPath = attributes.getValue("path");
		}
		if(qName.equalsIgnoreCase("transaction")){
			currTrans = new JSONObject();

			try {
				currTrans.put("id",  Integer.parseInt(attributes.getValue("id")));
				currTrans.put("type", attributes.getValue("type"));
//				log.log(Level.FINE, "Trans amount :  {0}", amountStr);
				currTrans.put("amount", attributes.getValue("amount").replace(",", ""));
				currTrans.put("deposit", attributes.getValue("deposit"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				log.log(Level.FINE, "Error while converting XML Transaction to json");
			}

		}

	}
	public void endElement(String uri, String localName,
			String qName) throws SAXException {

		if(qName.equalsIgnoreCase("transaction")) {
			//add it to the list
			log.log( Level.FINE, "XML Parser: end transaction {0}", currTrans.toString());

			transactions.add(currTrans);

		}
	}
	public void characters(char[] ch, int start, int length) throws SAXException {
		//throw new SAXException();
	}
	public void updateServerData(){

	}

}

