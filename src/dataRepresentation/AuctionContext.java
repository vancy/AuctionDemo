package dataRepresentation;


import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class AuctionContext {
	public static enum AuctionType {SAA, CCA, LUA};
	private AuctionType type;
	private int round;
	private int duration_ms;
	private ArrayList<AuctionItem> itemList;
	private double minIncreament;
	//priceTick is used in CCA and LUA auction, keep track of current price for all items
	private double priceTick = 0;
	private boolean finalRound;
	public static int numberOfActivityRuleWaivers = 2;
	private boolean activityRuleAnnounced = false;
	private int activityRuleStartRound = -1;
	
	
	/* bidsProcessingFinished flag, to decide current round data processing is finished
	 * GUI check this flag, if true, GUI update auction_context to 
	 * table. Then set back to false
	 */
	public int roundTimeRemain = 0;
	public boolean bidsProcessingFinished = false;
	
	private BidderList bidderList;
	
	//This is only used by LUA. Bad programming style, but for making that work.
	public ConcurrentHashMap<Integer, ArrayList<LuaBid>> LuaBids;
	
	public AuctionContext(BidderList bidderList) {
		this.type = AuctionType.SAA;
		this.round = 0;
		this.duration_ms = 30000;
		this.itemList = new ArrayList<AuctionItem>();
		this.minIncreament = 0;
		this.finalRound = false;
		this.bidderList = bidderList;
	}
	
	//Copy Constructor
	public AuctionContext(AuctionContext ac) {
		this.type = ac.type;
		this.round = ac.round;
		this.duration_ms = ac.duration_ms;
		this.minIncreament = ac.minIncreament;
		this.finalRound = ac.finalRound;
		
		this.itemList = new ArrayList<AuctionItem>();
		
		for (AuctionItem item: ac.itemList) {
			this.itemList.add(new AuctionItem(item));
		}
		this.bidderList = ac.bidderList;
	}
	
	public AuctionContext(AuctionType type, int time, double min, ArrayList<AuctionItem> list) {
		this.type = type;
		this.round = 0;
		this.duration_ms = time;
		this.itemList = list;
		this.minIncreament = min;
		this.finalRound = false;
	}
	
	public void setData(int time, double min, ArrayList<AuctionItem> list) {
		this.round = 0;
		this.duration_ms = time;
		this.itemList = list;
		this.minIncreament = min;
		this.finalRound = false;
		//AuctionContext.activityRuleAnnounced = activityRuleAnnounced;
	}
	
	public void setType(String typeName) {
		 if (typeName.equals("SAA")) {
			 this.type = AuctionType.SAA;
			 return;
		 }
		 if (typeName.equals("CCA")){
			 this.type = AuctionType.CCA;
			 return;
		 }
		 if (typeName.equals("LUA")) {
			 this.type = AuctionType.LUA;
			 return;
		 }  
	}
	
	public AuctionType getType() {
		return this.type;
	}
	
	public double getMinIncrement() {
		return this.minIncreament;
	}
	
	public void setMinIncrement(double min) {
		this.minIncreament = min;
	}
	public void setFinalRound(boolean isFinal) {
		this.finalRound = isFinal;
	}
	public boolean isFinalRound() {
		return this.finalRound;
	}
	public void setDurationTime(int t) {
		this.duration_ms = t;
	}
	public int getDurationTime() {
		return this.duration_ms;
	}
	public void setNumberOfActivityRuleWaivers(int numberOfActivityRuleWaivers) {
		AuctionContext.numberOfActivityRuleWaivers = numberOfActivityRuleWaivers;
	}
	public int getNumberOfActivityRuleWaivers() {
		return numberOfActivityRuleWaivers;
	}
	public boolean isActivityRuleAnnounced() {
		return activityRuleAnnounced;
	}

	public void setActivityRuleAnnounced(boolean activityRuleAnnounced) {
		this.activityRuleAnnounced = activityRuleAnnounced;
	}

	public int getActivityRuleStartRound() {
		return activityRuleStartRound;
	}

	public void setActivityRuleStartRound(int activityRuleStartRound) {
		this.activityRuleStartRound = activityRuleStartRound;
	}

	public ArrayList<AuctionItem> getItemList() {
		return this.itemList;
	}
	
	public void setItemList(ArrayList<AuctionItem> itemList) {
		this.itemList = new ArrayList<AuctionItem>(itemList);
	}
	
	public int getRound() {
		return this.round;
	}
	
	public void setRound(int r) {
		this.round = r;
	}
	
	public void incrementRound() {
		round++;
	}
	
	public double getPriceTick() {
		return this.priceTick;
	}
	
	public void setPriceTick(double price) {
		this.priceTick = price;
	}
	
	public AuctionItem searchItem(int id) {
		for (AuctionItem item: this.itemList) {
			if (id ==  item.getID()) {
				return item;
			}
		}
		return null;
	}
	
	public void clearAuctionItems() {
		this.itemList = null;
		AuctionItem.number_of_items = 0;
	}
	
	public boolean allCCAItemBddingFinished() {
		for (AuctionItem item: this.itemList) {
			if (item.biddingFinised) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}
	
	public String generateXml() {  
	    Document doc = null;  
	    Element root = null;  
	    Element child = null; 
	    try {  
	    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
	    	DocumentBuilder builder = factory.newDocumentBuilder();  
	    	doc = builder.newDocument();  
	    	root = doc.createElement("auction_context");  
	    	doc.appendChild(root);  
	    } catch (Exception e) {  
	    	e.printStackTrace();  
	    	return null;
	    }
	   
	    String typeName = "";

	    switch (this.type){
	    case SAA:
	    	typeName = "SAA";
	    	break;
	    case CCA:
	    	typeName = "CCA";
	    	break;
	    case LUA:
	    	typeName = "LUA";
	    	break;
		default:
			break;
	    }
	    
	    child = doc.createElement("type");
	    child.setAttribute("value", typeName);
	    root.appendChild(child);
	    child = doc.createElement("round");
	    child.setAttribute("value", Integer.toString(this.round));
	    child.setAttribute("final", this.finalRound?"yes":"no");
	    root.appendChild(child);
	    child = doc.createElement("duration");
	    child.setAttribute("value", Integer.toString(this.duration_ms));
	    child.setAttribute("remain", Integer.toString(this.roundTimeRemain));
	    root.appendChild(child);
	    child = doc.createElement("minimum_increament");
	    child.setAttribute("value", Double.toString(this.minIncreament));
	    root.appendChild(child);
	    child = doc.createElement("activityRuleAnnounced");
	    child.setAttribute("value", Boolean.toString(this.activityRuleAnnounced));
	    root.appendChild(child);
	    child = doc.createElement("activityRuleStartRound");
	    child.setAttribute("value", Integer.toString(this.activityRuleStartRound));
	    root.appendChild(child);
	    
	    int len = itemList.size();
	    for (int i=0; i<len; i++) {  
	    	AuctionItem item = itemList.get(i);  
	    	child = doc.createElement("item");  
	    	child.setAttribute("id", Integer.toString(item.getID()));
	    	child.setAttribute("name", item.getName());  
	    	child.setAttribute("price", Double.toString(item.getPrice()));
	    	child.setAttribute("quantity", Integer.toString(item.getQuantity()));
	    	String ownerName = "";
	    	// if current Item has Owner and Owner has name, then assign owner name to Item
	    	if ( (null != item.getOwner()) && (null != item.getOwner().getName()) ) {
	    		ownerName = item.getOwner().getName();
	    	}
	    	child.setAttribute("owner", ownerName);  
	    	
	    	//this attribute is used for CCA Auction
	    	child.setAttribute("final", item.biddingFinised?"yes":"no");
	    	//this owner group is used for CCA Auction
	    	HashMap<String, Integer> owners = item.getOwners();
	    	if (item.biddingFinised) {
	    		for (String owner: owners.keySet()) {
	    			Element ownerNode = doc.createElement("owner");
	    			ownerNode.setAttribute("name", owner);
	    			ownerNode.setAttribute("quantity", Integer.toString(owners.get(owner)));
	    			child.appendChild(ownerNode);
	    		}
	    	}
	    	root.appendChild(child);  
	    }  
	    /* 
	     * Convert Document into String
	     */
	    StringWriter sw = new StringWriter();
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer;
		try {
			transformer = tf.newTransformer();

			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");  
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			this.generateJson();
			return sw.toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
    }
		
	public String generateJson() {
		Gson gson = new GsonBuilder()
		.registerTypeAdapter(Bidder.class, new JsonBidderAdapter())
		.create();

		String jsonData = gson.toJson(this);

		return jsonData;
	}
}
