package jadelab1;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class ServiceAgent2 extends Agent {
    protected void setup() {
        //services registration at DF
        DFAgentDescription dfad = new DFAgentDescription();
        dfad.setName(getAID());

        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType("answers");
        sd1.setName("test");

        //add them all
        dfad.addServices(sd1);

        try {
            DFService.register(this, dfad);
        } catch (FIPAException ex) {
            ex.printStackTrace();
        }

        addBehaviour(new New2CyclicBehaviour(this));
        //doDelete();
    }

    protected void takeDown() {
        //services deregistration before termination
        try {
            DFService.deregister(this);
        } catch (FIPAException ex) {
            ex.printStackTrace();
        }
    }

    public String makeRequest(String serviceName, String word) {
        StringBuffer response = new StringBuffer();
        try {
            URL url;
            URLConnection urlConn;
            DataOutputStream printout;
            DataInputStream input;
            url = new URL("http://dict.org/bin/Dict");
            urlConn = url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String content = "Form=Dict1&Strategy=*&Database=" + URLEncoder.encode(serviceName) + "&Query=" + URLEncoder.encode(word) + "&submit=Submit+query";
            //forth
            printout = new DataOutputStream(urlConn.getOutputStream());
            printout.writeBytes(content);
            printout.flush();
            printout.close();
            //back
            input = new DataInputStream(urlConn.getInputStream());
            String str;
            while (null != ((str = input.readLine()))) {
                response.append(str);
            }
            input.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        //cut what is unnecessary
        return response.substring(response.indexOf("<hr>") + 4, response.lastIndexOf("<hr>"));
    }
}

class New2CyclicBehaviour extends CyclicBehaviour
{
	ServiceAgent2 agent;
	public New2CyclicBehaviour(ServiceAgent2 agent)
	{
		this.agent = agent;
	}
	public void action()
	{
		//MessageTemplate template = MessageTemplate.MatchOntology("test");
		ACLMessage message = agent.receive();
		if (message == null)
		{
			block();
		}
		else
		{
			//process the incoming message
			String content = message.getContent();
			ACLMessage reply = message.createReply();
			reply.setPerformative(ACLMessage.INFORM);
			String response = "";
			try
			{
				response = agent.makeRequest(message.getOntology(),content);
			}
			catch (NumberFormatException ex)
			{
				response = ex.getMessage();
			}
			reply.setContent(response);
			agent.send(reply);
		}
	}
}