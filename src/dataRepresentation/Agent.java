package dataRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Agent extends Bidder {
<<<<<<< HEAD
    
    private double sunkAwarenessConstant = 1;
    
    private List<Bid> memory = new ArrayList<Bid>();
    List<AuctionItem> items;
    
    List<List<AuctionItem>> powerSet;
    
    Map<List<AuctionItem>, Integer> valuations = new HashMap<List<AuctionItem>, Integer>();
    
    /**
     * 
     * @param name
     * @param ip
     * @param items A list of items in the auction
     * @param valuations A list of integers representing valuations. Every index must be mapped to the items index.
     * 					EG: items = {item2, item3, item1} => valuations = {item2valuation, item3valuation, item1valuation}
     * 					Notice the order of items and valuations in the lists are the same!
     * @param k sunk-awareness constant
     */
    public Agent(String name, String ip, List<AuctionItem> items, List<Integer> valuations, double k) {
        super(name, ip);
        this.items = items;
        powerSet = getPowerSet(items);
        setSunkAwarenessConstant(k);
        assignValuations(valuations);
    }
    
    public double getSunkAwarenessConstant() {
        return sunkAwarenessConstant;
    }
    
    public void setSunkAwarenessConstant(double sunkAwarenessConstant) {
        this.sunkAwarenessConstant = sunkAwarenessConstant;
    }
    
    public Bid auctionResponse(AuctionContext ac) {
        List<AuctionItem> bidderItemList = new ArrayList<AuctionItem>();
        Map<AuctionItem, Double> behaviour = getNextRoundBehaviour(ac);
        
        for (AuctionItem i : behaviour.keySet()) {
            bidderItemList.add(new AuctionItem(i));
        }
        
        Bid bid = new Bid(this, bidderItemList);
        memory.add(bid);
        return bid;
    }
    
    private List<List<AuctionItem>> getPowerSet(List<AuctionItem> itemList) {
        List<List<AuctionItem>> ps = new ArrayList<List<AuctionItem>>();
        ps.add(new ArrayList<AuctionItem>()); // add the empty set
        
        // for every item in the original list
        for (AuctionItem i : itemList) {
            List<List<AuctionItem>> newPs = new ArrayList<List<AuctionItem>>();
            
            for (List<AuctionItem> subset : ps) {
                // copy all of the current powerset's subsets
                newPs.add(subset);
                
                // plus the subsets appended with the current item
                List<AuctionItem> newSubset = new ArrayList<AuctionItem>(subset);
                newSubset.add(i);
                newPs.add(newSubset);
            }
            
            // powerset is now powerset of list.subList(0, list.indexOf(item)+1)
            ps = newPs;
        }
        ps.remove(0); // remove the emptyset
        return ps;
    }
    
    private void assignValuations(List<Integer> singleItemValuations) {
        // this for loop assigns valuations to every single item
        for (int i = 0; i < singleItemValuations.size(); i++) {
            List<AuctionItem> singleItemPack = new ArrayList<AuctionItem>();
            singleItemPack.add(items.get(i));
            valuations.put(singleItemPack, singleItemValuations.get(i));
        }
        // this for loop assigns valuations to every combination of items
        // by summing the individual valuations
        for (List<AuctionItem> itemPack : powerSet) {
            if (itemPack.size() == 0) {
                continue;
            }
            int currentValuation = 0;
            for (AuctionItem item : itemPack) {
            	List<AuctionItem> i = new ArrayList<AuctionItem>();
                i.add(item);
                currentValuation += valuations.get(i);
            }
            valuations.put(itemPack, currentValuation);
        }
    }
    
    private double calculateSurplus(List<AuctionItem> itemSet, double increment) {
        
        double valuation = valuations.get(itemSet);
        double perceivedPriceTotal = 0;
        
        for (AuctionItem item : itemSet) {
            if (this.getID() == item.getOwner().getID()) {
                perceivedPriceTotal += sunkAwarenessConstant * item.getPrice();
            } else {
                perceivedPriceTotal += item.getPrice() + increment;
            }
        } 
        
        // some debugging statements
        System.out.print("{");
        for (AuctionItem i : itemSet) {
            System.out.print(i.getID() + " ");
        }
        System.out.print("}");
        System.out.println("Valuation: " + valuation + " perceviedTotal: " + perceivedPriceTotal + " surplus: " + (valuation - perceivedPriceTotal));
        
        return valuation - perceivedPriceTotal;
    }
    
    /**
     * This method specifies the agents behaviour for the first round. This needs
     * to be separated from the other rounds because the agent usually responds to
     * other bidders' bids and the first round does not have any bids to begin with.
     * 
     * The behaviour will simply be bidding the agents valuations on all items.
     * @return
     */
    /*protected Map<Integer, Double> getFirstRoundBehaviour() {
        
        Map<Integer, Double> nextRoundBehaviour = new HashMap<Integer, Double>();
        
        for (int i = 0; i < numberOfItems; i++) {
            List<Integer> vAuctionItem = new ArrayList<Integer>();
            vAuctionItem.add(i);
            nextRoundBehaviour.put(i, (double)valuations.get(vAuctionItem));
        }
        
        return nextRoundBehaviour;
    }*/
    
    protected Map<AuctionItem, Double> getNextRoundBehaviour(AuctionContext ac) {
        
        Map<AuctionItem, Double> nextRoundBehaviour = new HashMap<AuctionItem, Double>();
        
        List<List<AuctionItem>> optimalSetsToBidOn = new ArrayList<List<AuctionItem>>();
        List<AuctionItem> finalSetToBidOn;
        
        double maxSurplus = Double.NEGATIVE_INFINITY;
        
        // calculate surpluses for all combinations of items.
        for (List<AuctionItem> set : powerSet) {
            double currentSurplus = calculateSurplus(set, ac.getMinIncrement());
            if (currentSurplus >= maxSurplus) {
                optimalSetsToBidOn.add(new ArrayList<AuctionItem>(set));
                maxSurplus = currentSurplus;
            }
        }
        // from the combinations of items with the highest surplus -
        // find the greatest sized set to be the set to bid on
        // eg. {1, 2} surplus = 30. {1, 2, 3} surplus = 30 too. Pick the larger set.
        int greatestSize = -1;
        int indexOfLargestSet = 0;
        for (int i = 0; i < optimalSetsToBidOn.size(); i++) {
            if (optimalSetsToBidOn.get(i).size() > greatestSize) {
                greatestSize = optimalSetsToBidOn.get(i).size();
                indexOfLargestSet = i;
            }
        }
        finalSetToBidOn = optimalSetsToBidOn.get(indexOfLargestSet);
        
        for (AuctionItem item : items) {
            if (finalSetToBidOn.contains(item)) {
                if (this.getID() != item.getOwner().getID()) {
                    // losing bid on desired item. Outbid it.
                    nextRoundBehaviour.put(item, item.getPrice() + ac.getMinIncrement());
                } else {
                    // agent is winning the bid for the item - does not need to bid again
                    nextRoundBehaviour.put(item, 0.0);
                }
            } else {
                nextRoundBehaviour.put(item, 0.0);
            }
        }
        
        return nextRoundBehaviour;
    }
    
}
=======
	
	private double sunkAwarenessConstant = 1;
	
	private List<Bid> memory;
	
	private int numberOfItems;
	
	List<List<AuctionItem>> powerSet;
	
	Map<List<AuctionItem>, Integer> valuations = new HashMap<List<AuctionItem>, Integer>();
	
	public Agent(String name, String ip, List<AuctionItem> items) {
		super(name, ip);
		this.memory = new ArrayList<Bid>();
		powerSet = getPowerSet(items);
		assignValuationsAndSunkAwareness();
	}
	
	/*
	 * Xing adds this constructor just for testing
	 */
	public Agent(String name, List<AuctionItem> items, double sunkAwarenessConstant) {
		super(name, "Agent");
		this.memory = new ArrayList<Bid>();
		this.setSunkAwarenessConstant(sunkAwarenessConstant);
		//powerSet = getPowerSet(items);
//		assignValuationsAndSunkAwareness();
	}
	
	public Agent(String name, String ip, List<AuctionItem> items, double sunkAwarenessConstant) {
		super(name, ip);
		this.memory = new ArrayList<Bid>();
		this.setSunkAwarenessConstant(sunkAwarenessConstant);
		powerSet = getPowerSet(items);
		assignValuationsAndSunkAwareness();
	}

	public double getSunkAwarenessConstant() {
		return sunkAwarenessConstant;
	}

	public void setSunkAwarenessConstant(double sunkAwarenessConstant) {
		this.sunkAwarenessConstant = sunkAwarenessConstant;
	}
	
	public Bid auctionResponse(AuctionEnvironment ae) {
		List<AuctionItem> bidderItemList = new ArrayList<AuctionItem>();
		Map<AuctionItem, Double> behaviour = getNextRoundBehaviour(ae);
		
		for (AuctionItem i : behaviour.keySet()) {
			bidderItemList.add(new AuctionItem(i));
		}
		
		Bid bid = new Bid(this, bidderItemList);
		memory.add(bid);
		return bid;
	}
	
	private List<List<AuctionItem>> getPowerSet(List<AuctionItem> itemList) {
		List<List<AuctionItem>> ps = new ArrayList<List<AuctionItem>>();
		ps.add(new ArrayList<AuctionItem>()); // add the empty set

		// for every item in the original list
		for (AuctionItem i : itemList) {
			List<List<AuctionItem>> newPs = new ArrayList<List<AuctionItem>>();

			for (List<AuctionItem> subset : ps) {
				// copy all of the current powerset's subsets
				newPs.add(subset);

				// plus the subsets appended with the current item
				List<AuctionItem> newSubset = new ArrayList<AuctionItem>(subset);
				newSubset.add(i);
				newPs.add(newSubset);
			}

			// powerset is now powerset of list.subList(0, list.indexOf(item)+1)
			ps = newPs;
		}
		return ps;
	}
	
	private void assignValuationsAndSunkAwareness() {
		for (List<AuctionItem> powerSetList : powerSet) {
			if (powerSetList.size() == 1) {
				System.out.print("Enter agent " + this.getID() + "'s value for item " + powerSetList.get(0) + ": ");
				// can't use the command line to enter valuations, going to have to do it another way
				int value = Integer.parseInt(ScannerSingleton.getInstance().nextLine());
				valuations.put(powerSetList, value);
			} else {
				valuations.put(powerSetList, 0);
			}
		}
		System.out.print("Enter agent " + this.getID() + "'s sunk-awareness constant: ");
		int value = Integer.parseInt(ScannerSingleton.getInstance().nextLine());
		setSunkAwarenessConstant(value);
	}
	
	private double calculateSurplus(List<AuctionItem> itemSet, double increment) {
		
		// ignore the empty set of items generated from the power set
		if (itemSet.isEmpty()) {
			return Double.NEGATIVE_INFINITY;
		}
		
		double valuation = valuations.get(itemSet);
		double perceivedPriceTotal = 0;
		
		for (AuctionItem item : itemSet) {
			if (this.getID() == item.getOwner().getID()) {
				// I need access to an auctioneer object so I can fetch the item price for a given item
				perceivedPriceTotal += sunkAwarenessConstant * item.getPrice();
			} else {
				perceivedPriceTotal += item.getPrice() + increment;
			}
		}	
		
		// some debugging statements
		System.out.print("{");
		for (AuctionItem i : itemSet) {
			System.out.print(i.getID() + " ");
		}
		System.out.print("}");
		System.out.println("Valuation: " + valuation + " perceviedTotal: " + perceivedPriceTotal);
		
		return valuation - perceivedPriceTotal;
	}
	
	/**
	 * This method specifies the agents behaviour for the first round. This needs
	 * to be separated from the other rounds because the agent usually responds to
	 * other bidders' bids and the first round does not have any bids to begin with.
	 * 
	 * The behaviour will simply be bidding the agents valuations on all items.
	 * @return
	 */
	protected Map<Integer, Double> getFirstRoundBehaviour() {
		
		Map<Integer, Double> nextRoundBehaviour = new HashMap<Integer, Double>();
			
		for (int i = 0; i < numberOfItems; i++) {
			List<Integer> vAuctionItem = new ArrayList<Integer>();
			vAuctionItem.add(i);
			nextRoundBehaviour.put(i, (double)valuations.get(vAuctionItem));
		}
		
		return nextRoundBehaviour;
	}
	
	protected Map<AuctionItem, Double> getNextRoundBehaviour(AuctionEnvironment ae) {
		
		Map<AuctionItem, Double> nextRoundBehaviour = new HashMap<AuctionItem, Double>();
		
		List<AuctionItem> optimalAuctionItemsToBidOn = new ArrayList<AuctionItem>();
		double maxSurplus = Double.NEGATIVE_INFINITY;
		
		for (List<AuctionItem> set : powerSet) {
			double currentSurplus = calculateSurplus(set, ae.context.getMinIncrement());
			if (currentSurplus > maxSurplus) {
				optimalAuctionItemsToBidOn = new ArrayList<AuctionItem>(set);
				maxSurplus = currentSurplus;
			}
		}
		
		List<AuctionItem> itemSet = ae.context.getItemList();
		
		for (AuctionItem item : itemSet) {
			if (optimalAuctionItemsToBidOn.contains(item)) {
				if (this.getID() != item.getOwner().getID()) {
					nextRoundBehaviour.put(item, item.getPrice() + ae.context.getMinIncrement());
				} else {
					// agent is winning the bid for the item - does not need to bid again
					nextRoundBehaviour.put(item, 0.0);
				}
			} else {
				nextRoundBehaviour.put(item, 0.0);
			}
		}
		
		return nextRoundBehaviour;
	}

}
>>>>>>> 7a001ad127b31b0d3edfd0ae15e1cbe1479e99d0
