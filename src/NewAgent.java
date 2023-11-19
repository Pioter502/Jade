package jadelab1;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import javax.swing.*;
import java.util.HashMap;

public class NewAgent extends Agent {

	public HashMap<String, String> wordToTranslationMap = new HashMap<>();
	public int messageIdCounter = 0;
    protected void setup() {
        displayResponse("Hello, I am " + getAID().getLocalName());
        addBehaviour(new NewCyclicBehaviour(this));
        //doDelete();
    }

    protected void takeDown() {
        displayResponse("See you");
    }

    public void displayResponse(String message) {
        JOptionPane.showMessageDialog(null, message, "Message", JOptionPane.PLAIN_MESSAGE);
    }

    public void displayHtmlResponse(String html) {
        JTextPane tp = new JTextPane();
        JScrollPane js = new JScrollPane();
        js.getViewport().add(tp);
        JFrame jf = new JFrame();
        jf.getContentPane().add(js);
        jf.pack();
        jf.setSize(400, 500);
        jf.setVisible(true);
        tp.setContentType("text/html");
        tp.setEditable(false);
        tp.setText(html);
    }
}

class NewCyclicBehaviour extends CyclicBehaviour {
	NewAgent newAgent;
	public NewCyclicBehaviour(NewAgent newAgent) {
		this.newAgent = newAgent;
	}
	public void action() {
		ACLMessage message = newAgent.receive();
		if (message == null) {
			block();
		} else {
			String ontology = message.getOntology();
			String content = message.getContent();
			int performative = message.getPerformative();
			if (performative == ACLMessage.REQUEST)
			{
				//I cannot answer but I will search for someone who can
				DFAgentDescription dfad = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setName("test");
				dfad.addServices(sd);
				try
				{
					DFAgentDescription[] result = DFService.search(newAgent, dfad);
					if (result.length == 0) newAgent.displayResponse("No service has been found ...");
					else
					{
						String requestId = "Request" + newAgent.messageIdCounter++;
						String foundAgent = result[0].getName().getLocalName();
						newAgent.displayResponse("Agent " + foundAgent + " is a service provider. Sending message to " + foundAgent);
						ACLMessage forward = new ACLMessage(ACLMessage.REQUEST);
						forward.addReceiver(new AID(foundAgent, AID.ISLOCALNAME));
						forward.setContent(content);
						forward.setOntology(ontology);
						forward.setReplyWith(requestId);
						newAgent.wordToTranslationMap.put(requestId, content);
						newAgent.send(forward);
					}
				}
				catch (FIPAException ex)
				{
					ex.printStackTrace();
					newAgent.displayResponse("Problem occured while searching for a service ...");
				}
			}
			else
			{	//when it is an answer
				String originalWord = newAgent.wordToTranslationMap.get(message.getInReplyTo());
				newAgent.displayHtmlResponse("ID: " + message.getInReplyTo() + "<br/>Original word: " + originalWord + "<br/>Content: " + content);
			}
		}
	}
}