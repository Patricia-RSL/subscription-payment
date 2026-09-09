package com.patricia.paymentSimulator;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PaymentSimulatorConfig {

    public static final String PAYMENT_REQUEST_QUEUE = "payment.request";
    public static final String PAYMENT_REQUEST_DLQ = "payment.request.dlq";
    public static final String PAYMENT_REQUEST_EXCHANGE = "payment.request.exchange";
    public static final String PAYMENT_RESULT_QUEUE = "payment.result";
    public static final String PAYMENT_RESULT_DLQ = "payment.result.dlq";
    public static final String PAYMENT_RESULT_EXCHANGE = "payment.result.exchange";

    @Bean
    public Queue paymentRequestQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", PAYMENT_REQUEST_EXCHANGE);
        args.put("x-dead-letter-routing-key", PAYMENT_REQUEST_DLQ);
        return new Queue(PAYMENT_REQUEST_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue paymentRequestDlq() {
        return new Queue(PAYMENT_REQUEST_DLQ, true);
    }

    @Bean
    public DirectExchange paymentRequestExchange() {
        return new DirectExchange(PAYMENT_REQUEST_EXCHANGE);
    }

    @Bean
    public Binding paymentRequestDlqBinding(Queue paymentRequestDlq, DirectExchange paymentRequestExchange) {
        return BindingBuilder.bind(paymentRequestDlq).to(paymentRequestExchange).with(PAYMENT_REQUEST_DLQ);
    }

    @Bean
    public Queue paymentResultQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", PAYMENT_RESULT_EXCHANGE);
        args.put("x-dead-letter-routing-key", PAYMENT_RESULT_DLQ);
        return new Queue(PAYMENT_RESULT_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue paymentResultDlq() {
        return new Queue(PAYMENT_RESULT_DLQ, true);
    }

    @Bean
    public DirectExchange paymentResultExchange() {
        return new DirectExchange(PAYMENT_RESULT_EXCHANGE);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxRetries(3)
                        .backOffOptions(1000L, 2.0, 10000L)
                        .recoverer((message, cause) -> {
                            throw new AmqpRejectAndDontRequeueException("Retries exhausted for payment.request consumer", cause);
                        })
                        .build()
        );

        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
