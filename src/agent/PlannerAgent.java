package agent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ai.Plan;
import ai.TransportationAlgorithm;
import units.Planner;
import map.GarbageContainer;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class PlannerAgent extends Agent {
	
	private static final long serialVersionUID = -8343519877395972050L;
	
	private Planner planner;
	
	public Planner getPlanner() {
		return planner;
	}

	public void setPlanner(Planner planner) {
		this.planner = planner;
	}
	
	protected void setup() {
		Object args[] = getArguments();
		this.planner = (Planner) args[0];
		System.out.println("My name is " + getAID().getName() + " and I'm active now.");
		
		this.buildInitialMessageForTrucks();
		
		addBehaviour(new receiveReadyReply(this, this.planner));
		
		addBehaviour(new sendOptimalPlan(this, this.planner));
	}
	
	
	/**
	 * 
	 */
	private void buildInitialMessageForTrucks() {
		
		final List<String> trucksTemp = new ArrayList<String>();
		Iterator<String> itTruck = this.planner.getTruckAgents().iterator();
		while(itTruck.hasNext()){
			String t = itTruck.next();
			trucksTemp.add(t);
		}
		
		addBehaviour(new SimpleBehaviour(this) {
			
			private static final long serialVersionUID = 3729878192721244478L;
			private boolean finished = false;
			private int nMessages = 0;
			
			public int getnMessages() {
				return nMessages;
			}

			public void setnMessages(int nMessages) {
				this.nMessages = nMessages;
			}

			@Override
			public void action() {
				ACLMessage initialMsg = new ACLMessage(ACLMessage.QUERY_IF);
				Iterator<String> itTruck = trucksTemp.iterator();
				while(itTruck.hasNext()){
					
					AMSAgentDescription [] agents = null;
			        try {
			            SearchConstraints c = new SearchConstraints();
			            c.setMaxResults ( new Long(-1) );
			            agents = AMSService.search( this.getAgent(), new AMSAgentDescription (), c );
			        }
			        catch (Exception e) { e.printStackTrace();}
					
			        String t = itTruck.next();
			        for (int i=0; i<agents.length;i++) {
			            AID agentID = agents[i].getName();
			            if(agentID.getName().equals(t)) {
			            	initialMsg.addReceiver(agentID);
			            	System.out.println(getAID().getLocalName() + ": Ready? message to " + agentID.getLocalName());
			            }
			            else continue;
			        }
				}
		
				initialMsg.setContent("Ready?");
				initialMsg.setOntology("InitialMessage");
				initialMsg.setReplyWith("Yes. I'm ready.");
				send(initialMsg);
				
				finished = true;
			}

			@Override
			public boolean done() {
				return finished;
			}
		});
	}
	
	
	/**
	 * 
	 * @author ruivalentemaia
	 *
	 */
	class receiveReadyReply extends SimpleBehaviour {

		private static final long serialVersionUID = -2282058182688651444L;
		private boolean finished = false;
		private int nMessagesReceived = 0;
		private Planner planner;
		
		public receiveReadyReply(Agent a, Planner planner){
			super(a);
			this.planner = planner;
		}

		@Override
		public void action() {
			MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF);
			MessageTemplate m2 = MessageTemplate.MatchOntology("InitialMessage");
			
			MessageTemplate m1_and_m2 = MessageTemplate.and(m1, m2);
			
			ACLMessage msg = receive(m1_and_m2);
			if(msg != null){
				System.out.println(this.getAgent().getLocalName() + ": Received the \"" + msg.getContent() + "\" from " + msg.getSender().getLocalName() + ".");
				nMessagesReceived++;
				finished = true;
			}
			else block();
		}

		@Override
		public boolean done() {
			return finished;
		}
	}

	/**
	 * 
	 * @author ruivalentemaia
	 *
	 */
	class sendOptimalPlan extends SimpleBehaviour {

		private static final long serialVersionUID = 2881600653030040628L;
		private boolean finished = false;
		private Planner planner;
		private String tempFilePath = System.getProperty("user.dir") + "/temp";
		private int nMessages = 0;
		
		public Planner getPlanner() {
			return planner;
		}

		public void setPlanner(Planner planner) {
			this.planner = planner;
		}

		public String getOptionsFilePath() {
			return tempFilePath;
		}

		public void setOptionsFilePath(String optionsFilePath) {
			this.tempFilePath = optionsFilePath;
		}

		public int getnMessages() {
			return nMessages;
		}

		public void setnMessages(int nMessages) {
			this.nMessages = nMessages;
		}

		public sendOptimalPlan(Agent a, Planner p){
			super(a);
			this.planner = p;
		}
		
		/**
		 * 
		 * @param min
		 * @param max
		 * @return
		 */
		private String generateXMLFilename(int min, int max) {
			String f = "";
			Random r = new Random();
			int value = r.nextInt(max-min) + min;
			f = String.valueOf(value) + "-" + planner.getTrucks().size() + planner.getTransportPlan().size();
			f += ".xml";
			return f;
		}
		
		
		public String exportPlanObject(Plan p) throws ParserConfigurationException, TransformerException{
			String filename = this.generateXMLFilename(1000, 10000);
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			Document doc = docBuilder.newDocument();
			
			Element rootElement = doc.createElement("plan");
			doc.appendChild(rootElement);
			
			Element truckElement = doc.createElement("truck");
			truckElement.appendChild(doc.createTextNode(p.getTruck().getTruckName()));
			rootElement.appendChild(truckElement);
			
			Element assignmentsElement = doc.createElement("assignments");
			
			HashMap<GarbageContainer, Double> map = new HashMap<GarbageContainer, Double>();
			map = p.getAssignment();
			Iterator it = map.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<GarbageContainer, Double> pairs = (Map.Entry<GarbageContainer, Double>)it.next();
				
				Element assignmentElement = doc.createElement("assignment");
				
				Element gcElement = doc.createElement("garbageContainer");
				gcElement.appendChild(doc.createTextNode(String.valueOf(pairs.getKey().getId())));
				assignmentElement.appendChild(gcElement);
				
				Element amount = doc.createElement("amountToCollect");
				amount.appendChild(doc.createTextNode(String.valueOf(pairs.getValue())));
				assignmentElement.appendChild(amount);
				
				assignmentsElement.appendChild(assignmentElement);
			}
			rootElement.appendChild(assignmentsElement);
			
			File f = new File(this.tempFilePath);
			f.setExecutable(true);
			f.setReadable(true);
			f.setWritable(true);
			File file = new File(f.toString() + "/" + filename);
		
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
			
			transformer.transform(source, result);
			
			return filename;
		}

		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
			msg.setOntology("PlanOntology");
			
			Iterator<TransportationAlgorithm> itTA = this.planner.getTransportPlan().iterator();
			while(itTA.hasNext()){
				
				TransportationAlgorithm TA = itTA.next();
				Iterator<Plan> itPlan = TA.getOptimalPlans().iterator();
				
				while(itPlan.hasNext()){
					Plan plan = itPlan.next();
					
					AMSAgentDescription [] agents = null;
			        try {
			            SearchConstraints c = new SearchConstraints();
			            c.setMaxResults ( new Long(-1) );
			            agents = AMSService.search( this.getAgent(), new AMSAgentDescription (), c );
			        }
			        catch (Exception e) { e.printStackTrace();}
					
			        String t = plan.getTruck().getTruckName();
			        for (int i=0; i<agents.length;i++) {
			            AID agentID = agents[i].getName();
			            if(agentID.getLocalName().equals(t)) {
			            	msg.addReceiver(agentID);
			            	String filename = "";
			            	try {
			            		filename = this.exportPlanObject(plan);
							} catch (ParserConfigurationException | TransformerException e) {
								e.printStackTrace();
							}
			            	
			            	msg.setContent(filename);
			            	send(msg);
			            	
			            	System.out.println(getAID().getLocalName() + ": Optimal plan sent to " + agentID.getLocalName() + ".");
			            	
			            	finished = true;
			            }
			            else continue;
			        }
				}
			}
		}

		@Override
		public boolean done() {
			return finished;
		}
	}
}
