package co.uk.escape;

import static co.uk.escape.RMQExchange.Type.*;
import static co.uk.escape.RMQQueue.Type.*;
import static co.uk.escape.RMQTemplate.Type.*;

import java.io.IOException;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import co.uk.escape.service.AuthenticateUserService;


@Configuration
@EnableAutoConfiguration
@ComponentScan
public class UserAuthenticationConfiguration {
	
	
	// QUEUES //	
	@Bean @RMQQueue(LOGIN_RESPONSE)
	Queue loginResponseQueue() {
		return new Queue("LoginResponseQueue", true);
	}

	@Bean @RMQQueue(LOGIN_REQUEST)
	Queue loginRequestQueue() {
		return new Queue("LoginRequestQueue", true);
	}	

	@Bean @RMQQueue(REGISTRATION_REQUEST)
	Queue registrationRequestQueue() {
		return new Queue("RegistrationRequestQueue", true);
	}
	
	@Bean @RMQQueue(REGISTRATION_RESPONSE)
	Queue registrationResponseQueue() {
		return new Queue("RegistrationResponseQueue", true);
	}
	
	@Bean @RMQQueue(USER_SERVICE)
	Queue userServcieQueue() {
		return new Queue("UserServiceQueue", true);
	}
	

	// EXCHANGE //
	@Bean @RMQExchange(AUTHORISATION)
	TopicExchange authorisationExchange() {
		return new TopicExchange("AuthorisationExchange");
	}
	
	@Bean @RMQExchange(RESPONSE)
	TopicExchange responseExchange() {
		return new TopicExchange("ResponseExchange");
	}
	
	@Bean @RMQExchange(MESSAGE)
	TopicExchange messageExchange() {
		return new TopicExchange("MessageExchange");
	}
	
	
	// BINDINGS //	
	// request queue bindings //
    @Bean
    public Binding loginRequestBinding() {
        return BindingBuilder.bind(loginRequestQueue()).to(authorisationExchange()).with("AuthorisationRoutingKey");
    }

	@Bean
	Binding registrationRequestBind(){
		return BindingBuilder.bind(registrationRequestQueue()).to(authorisationExchange()).with("RegistrationRoutingKey");
	}

	// service bindings //
	@Bean
	Binding userServiceBind(){
		return BindingBuilder.bind(userServcieQueue()).to(messageExchange()).with("RegistrationRoutingKey");
	}	
	
    // response queue bindings //
	@Bean
	Binding loginResponseBind(){
		return BindingBuilder.bind(loginResponseQueue()).to(responseExchange()).with("ResponseRoutingKey");
	}
	
	@Bean
	Binding registrationResponseBind(){
		return BindingBuilder.bind(registrationResponseQueue()).to(responseExchange()).with("ResponseRoutingKey");
	}	 
       
        

    /////////////////////////////////////////////
	// authenticate user : Receiver and Sender //
    /////////////////////////////////////////////
	@Bean @RMQTemplate(LOGIN_USER)
    RabbitTemplate authorisationTemplate(ConnectionFactory connectionFactory, 
    		@RMQQueue(LOGIN_RESPONSE) Queue loginResponseQueue,
    		@RMQExchange(RESPONSE) TopicExchange responseExchange){
        Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);    
        rabbitTemplate.setMessageConverter(jsonConverter);
        rabbitTemplate.setExchange(responseExchange.getName());
        rabbitTemplate.setQueue(loginResponseQueue.getName()); // Change queue in authorisation service to appropriate queue.
        rabbitTemplate.setRoutingKey("ResponseRoutingKey");
        return rabbitTemplate;
    }
    
    
	@Bean
	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, AuthenticateUserService receiver, 
			@RMQQueue(LOGIN_REQUEST) Queue loginRequestQueue,
			@RMQQueue(REGISTRATION_REQUEST) Queue registrationRequestQueue) throws IOException {			
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueues(loginRequestQueue, registrationRequestQueue);
		container.setMessageListener(receiver);
		return container;
	}

	@Bean
	AuthenticateUserService receiver() {
		return new AuthenticateUserService();
	}  
  
    
	
    ////////////////////////////////////
	// Register New User :  Forwarder //
    ////////////////////////////////////
	@Bean @RMQTemplate(REGISTER_USER)
	RabbitTemplate registerTemplate(ConnectionFactory connectionFactory,
			@RMQQueue(USER_SERVICE) Queue userServcieQueue,
			@RMQQueue(REGISTRATION_RESPONSE) Queue registrationResponseQueue,
			@RMQExchange(MESSAGE) TopicExchange messageExchange){
		Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonConverter);
		rabbitTemplate.setExchange(messageExchange.getName());
		rabbitTemplate.setQueue(userServcieQueue.getName());
		rabbitTemplate.setRoutingKey("RegistrationRoutingKey");
		rabbitTemplate.setReplyQueue(registrationResponseQueue);
		return rabbitTemplate;
	}
	
//    @Bean
//    public SimpleMessageListenerContainer replyListenerContainer(ConnectionFactory connectionFactory,
//    		@RMQTemplate(REGISTER_USER) RabbitTemplate registerTemplate,
//    		@RMQQueue(REGISTRATION_REQUEST) Queue registrationRequestQueue) {
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        container.setQueues(registrationRequestQueue);
//        container.setMessageListener(registerTemplate);
//        container.setReceiveTimeout(200000);
//        return container;
//    }
	
	
}

	