package ru.b1nd.datanode.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.b1nd.datanode.services.MessageService;

@Component
public class MessageListener {

    private final Logger logger = LoggerFactory.getLogger(MessageListener.class);

    private final MessageService messageService;

    @Autowired
    public MessageListener(MessageService messageService) {
        this.messageService = messageService;
    }

    @RabbitListener(queues = "#{queue.name}")
    public void acceptMessage(String message) {
        logger.info("Received message: " + message);
        messageService.acceptMessage(message);
    }
}
