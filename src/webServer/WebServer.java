package webServer;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import dataRepresentation.AuctionEnvironment;

public class WebServer extends Thread{
	private static final int maxQueueSize = 50;
	private static final int port = 8080;
	
	private Server server = null;
	private WebAppContext webappContextHandler = null;
	private ServletContextHandler servletContextHandler = null;
	private AuctionEnvironment auctionEnvironment = null;
	
	public WebServer(AuctionEnvironment ae) {
		QueuedThreadPool threadPool = new QueuedThreadPool(maxQueueSize, 10);
		server = new Server(threadPool);
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(port);
		server.setConnectors(new Connector[] {connector});
		
		auctionEnvironment = ae;
		webappContextHandler = new WebAppContext();
        webappContextHandler.setResourceBase("webapps/pages");
        webappContextHandler.setDescriptor("webapp/WEB-INF/web.xml");

        webappContextHandler.setContextPath("/");
		servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletContextHandler.setContextPath("/WEB-INF");
		this.addServlets();
	    ContextHandlerCollection contexts = new ContextHandlerCollection();
	    contexts.setHandlers(new Handler[] { webappContextHandler, servletContextHandler });
		server.setHandler(contexts);
	}
	
	private void addServlets() {
		servletContextHandler.addServlet(new ServletHolder(new LoginServlet(auctionEnvironment)),"/login.xml");
		servletContextHandler.addServlet(new ServletHolder(new BidServlet(auctionEnvironment)),"/bid.xml");
		servletContextHandler.addServlet(new ServletHolder(new UpdateServlet(auctionEnvironment)),"/update.xml");
	}
	
	@Override
	public void run() {
		try {
			server.start();
		} catch (Exception e) {
			System.err.println("Web Server starting failed!");
			e.printStackTrace();
		}
	}
	
	public void stopServer() throws Exception {
		server.stop();
		server.join();
		System.err.println("Web Server closed!");
	}
	
	public boolean isStarted() {
		return server.isStarted();
	}
	
	public boolean isStopped() {
		return server.isStopped();
	}
}
