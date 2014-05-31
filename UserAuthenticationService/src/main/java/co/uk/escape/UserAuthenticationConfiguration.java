package co.uk.escape;

import java.io.IOException;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
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
	
    
    @Bean
    Queue requestQueue() {
        return new Queue("requestqueue", false);
    }
    
    
    @Bean
    DirectExchange exchange() {
        return new DirectExchange("requestexchange");
    }
    
        
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(requestQueue()).to(exchange()).with("requestroutingkey");
    }
    
    
    @Bean
    RabbitTemplate template(DirectExchange exchange, ConnectionFactory connectionFactory){
        Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);    
        rabbitTemplate.setMessageConverter(jsonConverter);
        rabbitTemplate.setExchange("replyexchange");
        rabbitTemplate.setQueue("replyqueue");
        rabbitTemplate.setRoutingKey("replyroutingkey");
        return rabbitTemplate;
    }
    
    
	@Bean
	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, AuthenticateUserService receiver, Queue authorisationRequestQueue) throws IOException {			
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueues(new Queue("requestqueue", false));
		container.setMessageListener(receiver);
		return container;
	}

	@Bean
	AuthenticateUserService receiver() {
		return new AuthenticateUserService();
	}  

    
    
    
    
    
    
//	
//	@Bean
//	RabbitTemplate template(TopicExchange responseExchange, Queue responseQueue, ConnectionFactory connectionFactory) {
//		Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
//		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//		rabbitTemplate.setMessageConverter(jsonConverter);
//		rabbitTemplate.setQueue(responseQueue.getName());
//		rabbitTemplate.setRoutingKey("authenticate");
//		rabbitTemplate.setExchange(responseExchange.getName());
//		return rabbitTemplate;
//	}
//	
//
//	
//	
//	@Bean
//	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, AuthenticateUserService receiver, Queue authorisationRequestQueue) throws IOException {			
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
//		container.setConnectionFactory(connectionFactory);
//		container.setQueues(authorisationRequestQueue);
//		container.setMessageListener(receiver);
//		return container;
//	}
//
//	@Bean
//	AuthenticateUserService receiver() {
//		return new AuthenticateUserService();
//	}
	
	
	
	
	
	
	
	
	
//	@Bean
//	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter, Queue authorisationRequestQueue) throws IOException {			
//		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
//		container.setConnectionFactory(connectionFactory);
//		container.setQueues(authorisationRequestQueue);
//		container.setMessageListener(listenerAdapter);
//		return container;
//	}

//	@Bean
//	MessageListenerAdapter listenerAdapter(	AuthenticateUserService receiver) {
//		MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(receiver, "authenticateUser");	
//		Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
//		messageListenerAdapter.setMessageConverter(jsonConverter);
//		return messageListenerAdapter;
//	}
//	
//	@Bean
//	AuthenticateUserService receiver() {
//		return new AuthenticateUserService();
//	}
	
}

	