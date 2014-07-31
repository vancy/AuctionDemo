package auctionEngine;
import dataRepresentation.AuctionEnvironment;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.awt.FlowLayout;

public class AuctionPanel extends JPanel {

private AuctionEnvironment environment;
	
	private AuctionListPanel auctionListPanel;

	
	public AuctionPanel(AuctionEnvironment ae) {
		this.environment = ae;
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		this.auctionListPanel = new AuctionListPanel(this.environment.context);
		
		add(auctionListPanel);
		
		JPanel panel = new JPanel();
		add(panel);
		
		JLabel lblMinimunIncrement = new JLabel("Minimun Increment: ");
		panel.add(lblMinimunIncrement);
		
		JSpinner spinner_Increment = new JSpinner();
		panel.add(spinner_Increment);
		
		JButton btnNewButton_endAuction = new JButton("End Auction");
		panel.add(btnNewButton_endAuction);
		
		JButton btnNewButton_Stop = new JButton("Stop");
		panel.add(btnNewButton_Stop);
	}

}
