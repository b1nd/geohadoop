package ru.b1nd.datanode.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

    @RabbitListener(queues = "${queue.name}")
    public void acceptMessage(String message) {
        logger.info("Received message: " + message);
        // TODO: Implement, do some job declared in message
    }
}
