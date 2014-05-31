package co.uk.escape.service;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import co.uk.escape.domain.LoginRequest;
import co.uk.escape.domain.LoginResponse;

@Controller
public class AuthenticateUserService implements MessageListener {
	
	@Autowired
	RabbitTemplate template;

//	@Autowired
//	TopicExchange responseExchange;		
	
	@Override
    public void onMessage(Message message) {

        System.out.println("Message: " + message);      
        System.out.println("Message: " + message.getMessageProperties().getCorrelationId());
               
        Jackson2JsonMessageConverter jmc = new Jackson2JsonMessageConverter();
        Object messageObject = jmc.fromMessage(message);
      
		// Authenticate user and respond with a LoginResponse object
		LoginResponse loginResponse = new LoginResponse();
		loginResponse.setAuthorised(true);
		     
		System.out.println("Enter Authentication Service: " + (LoginRequest)messageObject);
		
		final byte[] correlationId = message.getMessageProperties().getCorrelationId();
		//CorrelationData correlationData = new CorrelationData(correlationId.toString());
		//System.out.print("correlationData: " + correlationData);
		
		MessagePostProcessor messagePostProcessor = new MessagePostProcessor() 
				{
			   		public Message postProcessMessage(Message message) throws AmqpException {
			   			message.getMessageProperties().setCorrelationId(correlationId);;
			   			return message;  
			   } 
		};
		
		
		//template.convertAndSend("replyexchange", "replyroutingkey", loginResponse, messagePostProcessor);		
		template.convertAndSend(loginResponse, messagePostProcessor);		
		
		System.out.println("UUID " + template.getUUID());		
		System.out.println("Exit Authentication Service: " + loginResponse);	
        
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
