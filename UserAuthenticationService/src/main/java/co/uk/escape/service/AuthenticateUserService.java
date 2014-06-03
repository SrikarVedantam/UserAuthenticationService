package co.uk.escape.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.rabbitmq.client.Channel;

import co.uk.escape.RMQTemplate;
import co.uk.escape.domain.LoginResponse;
import co.uk.escape.domain.LoginResponseMessageBundle;

@Controller
public class AuthenticateUserService implements ChannelAwareMessageListener {	
	
//	@Autowired @RMQTemplate(RMQTemplate.Type.REGISTER_USER)
//	RabbitTemplate registerTemplate;
	
	@Autowired @RMQTemplate(RMQTemplate.Type.LOGIN_USER)
	RabbitTemplate loginTemplate;
	
	@Override
	public void onMessage(Message message, Channel channel) throws Exception {
		
		RabbitTemplate template = null;
		MessagePostProcessor messagePostProcessor = null;
		List<String> permissions = new ArrayList<>();
		LoginResponseMessageBundle loginResponseMessageBundle = null;
		LoginResponse loginResponse = null;
		String messageType = message.getMessageProperties().getHeaders().get("message-type").toString();
		            
        Jackson2JsonMessageConverter jmc = new Jackson2JsonMessageConverter();
        Object messageObject = jmc.fromMessage(message);
       
        // START DEBUG //
        System.out.println("Message: " + message);      
        System.out.println("CorrelationId: " + message.getMessageProperties().getCorrelationId().toString());
        System.out.println("messageType: " + messageType);       
		System.out.println("Enter Authentication Service: " + messageObject);
        // END DEBUG //       
		
        switch(messageType){
        	case "login-request":    	
	        	// TODO: Extract credentials from message and authenticate user.
	    		// TODO: If login request is authenticated, send back authentication OK response, otherwise NOK
	        	
	    		// Authenticate user and respond with a LoginResponse object
	    		loginResponse = new LoginResponse();
	    		loginResponse.setAuthorised(true); // Lets authenticate user
		
	    		// Get correlation id so that the response message goes back to the correct sender.
	    		final byte[] correlationId = message.getMessageProperties().getCorrelationId();
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
	    		// Transform message payload into message payload
	    		
	    		//MessageBundle mb = (MessageBundle)messageObject;
	    		//LoginRequest loginRequest = (LoginRequest) mb.getPayload();
	    		
	    		
	    		//messageBundle = bundleMessage(loginRequest, permissions);
	    		loginResponseMessageBundle = bundleMessage(loginResponse, permissions);
	        	     	
	    		// Select rabbit template
	    		template = loginTemplate;
	    		
        	break;
        case "add-user-request":
        	// code
        	
        	//template = registerTemplate;
        	break;      
        default:
        	// code
        	break;
        }
        
		System.out.println("loginResponseMessageBundle: " + loginResponseMessageBundle);
      
		
		template.convertAndSend(loginResponseMessageBundle, messagePostProcessor);	
		
		//template.convertAndSend(loginResponse, messagePostProcessor);
		
		

		// TODO: If not a login request and authentication OK put authenticated message on message queue. Ensure that correlationID is included.
	
		System.out.println("Exit Authentication Service: ");	
        
    }
	
	
	
	// Bundle message
	private LoginResponseMessageBundle bundleMessage(LoginResponse payload, List<String> permissions) {
		LoginResponseMessageBundle messageBundle = new LoginResponseMessageBundle(payload);
		//messageBundle.setPermissions(permissions);
		return messageBundle;		
	}




	
	
	
	
//	public void authenticateUser(LoginRequest loginRequest) {
//		
//		System.out.println("Enter Authentication Service: " + loginRequest);
//		
//		// Authenticate user and respond with a LoginResponse object
//		LoginResponse loginResponse = new LoginResponse();
//		loginResponse.setAuthorised(true);
//
//		
//
//		//template.setCorrelationKey(correlationKey);
//		template.convertAndSend(responseExchange.getName(), "authorisation", loginResponse );
//		System.out.println("UUID " + template.getUUID());
//		
//		System.out.println("Exit Authentication Service: " + loginResponse);
//    }
	

	
}
