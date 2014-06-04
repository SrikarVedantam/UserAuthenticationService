package co.uk.escape.service;

import static co.uk.escape.RMQQueue.Type.REGISTRATION_RESPONSE;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.rabbitmq.client.Channel;

import co.uk.escape.RMQQueue;
import co.uk.escape.RMQTemplate;
import co.uk.escape.domain.LoginResponse;
import co.uk.escape.domain.LoginResponseMessageBundle;
import co.uk.escape.domain.MessageBundle;
import co.uk.escape.domain.RegistrationRequest;
import co.uk.escape.domain.RegistrationRequestMessageBundle;

import static co.uk.escape.RMQExchange.Type.*;
import static co.uk.escape.RMQQueue.Type.*;
import static co.uk.escape.RMQTemplate.Type.*;

@Controller
public class AuthenticateUserService implements ChannelAwareMessageListener {	
	
	@Autowired @RMQTemplate(REGISTER_USER)
	RabbitTemplate registerTemplate;
	
	@Autowired @RMQTemplate(LOGIN_USER)
	RabbitTemplate authorisationTemplate;
	
	@Autowired @RMQQueue(REGISTRATION_RESPONSE)
	Queue registrationResponseQueue;
	
	@Override
	public void onMessage(Message message, Channel channel) throws Exception {
		
		RabbitTemplate template = null;
		MessagePostProcessor messagePostProcessor = null;
		List<String> permissions = new ArrayList<>();
		LoginResponseMessageBundle loginResponseMessageBundle = null;
		LoginResponse loginResponse = null;
		MessageBundle messageBundle = null;
		final byte[] correlationId;
		String messageType = message.getMessageProperties().getHeaders().get("message-type").toString();
		            
        Jackson2JsonMessageConverter jmc = new Jackson2JsonMessageConverter();
        Object messageObject = jmc.fromMessage(message);
       
        // START DEBUG //
        System.out.println("Message: " + message);      
        System.out.println("CorrelationId: " + message.getMessageProperties().getCorrelationId().toString());
        System.out.println("messageType: " + messageType);       
		System.out.println("Enter Authentication Service: " + messageObject);
        // END DEBUG //       
		
		
		// TODO: Find a better more s way of doing this logic.
        switch(messageType){
        	case "login-request":    	
	        	// TODO: Extract credentials from message and authenticate user.
        		//MessageBundle mb = (MessageBundle)messageObject;
	    		//LoginRequest loginRequest = (LoginRequest) mb.getPayload();
	    		// TODO: If login request is authenticated, send back authentication OK response, otherwise NOK
	        	
	    		// Authenticate user and respond with a LoginResponse object
	    		loginResponse = new LoginResponse();
	    		loginResponse.setAuthorised(true); // Lets authenticate user
		
	    		// Get correlation id so that the response message goes back to the correct sender.
	    		correlationId = message.getMessageProperties().getCorrelationId();
	    		messagePostProcessor = new MessagePostProcessor() 
	    				{
	    			   		public Message postProcessMessage(Message message) throws AmqpException {
	    			   			message.getMessageProperties().setCorrelationId(correlationId);
	    			   		
	    			   			return message;  
	    			   } 
	    		};
	    		
	    		
	    		// Add permissions to message bundle based on user 'role'
	    		permissions.add("permission1");
	    		permissions.add("permission2");
	    		
	    		// Transform message payload into message bundle
	    		messageBundle = bundleMessage(loginResponse, permissions);
	        	     	
	    		// Select rabbit template
	    		template = authorisationTemplate;
	    		
	    		System.out.println("loginResponseMessageBundle: " + loginResponseMessageBundle);
        	break;
        case "registration-request":

        	// TODO: Extract credentials from message and authenticate user.
        	MessageBundle registrationRequestMessageBundle = (RegistrationRequestMessageBundle)messageObject;
    		RegistrationRequest registrationRequest = (RegistrationRequest) registrationRequestMessageBundle.getPayload();
  
    		// TODO: If login request is not authorised send back 401
    		// use the authorisationTemplate to send the 401 authorisation failed response.
    		// change the response queue on the template to the registration response queue
      		Boolean authorised = true;
    		if(!authorised){   			  			
    			authorisationTemplate.setQueue(registrationResponseQueue.getName());   			
    			template = authorisationTemplate;   			   					
    			break;
    		}
    		
    		// TODO: If login request is authenticated, forward registration request to message exchange  			
    		// Get correlation id so that the response message goes back to the correct sender.
    		correlationId = message.getMessageProperties().getCorrelationId();
    		messagePostProcessor = new MessagePostProcessor() 
    				{
    			   		public Message postProcessMessage(Message message) throws AmqpException {
    			   			message.getMessageProperties().setCorrelationId(correlationId);	   		
    			   			return message;  
    			   } 
    		};		
    		
    		// Add permissions to message bundle based on user 'role'
    		permissions.add("permission1");
    		permissions.add("permission2");
    		registrationRequestMessageBundle.setPermissions(permissions);
    			
    		// Select rabbit template
        	template = registerTemplate;
        	
        	messageBundle = registrationRequestMessageBundle;
        	
        	break;      
        default:
        	// code
        	break;
        }
        
	
		template.convertAndSend(messageBundle, messagePostProcessor);	
			

		// TODO: If not a login request and authentication OK put authenticated message on message queue. Ensure that correlationID is included.
	
		System.out.println("Exit Authentication Service: ");	
        
    }
	
	
	
	// Bundle message
	private MessageBundle bundleMessage(LoginResponse payload, List<String> permissions) {
		MessageBundle messageBundle = new LoginResponseMessageBundle(payload);
		//messageBundle.setPermissions(permissions);
		return messageBundle;		
	}

	
}
