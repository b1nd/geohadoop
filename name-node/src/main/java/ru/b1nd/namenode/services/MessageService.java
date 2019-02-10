package ru.b1nd.namenode.services;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.b1nd.namenode.domain.Node;
import ru.b1nd.namenode.model.Message;

@Service
public class MessageService {

    private final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final RabbitTemplate template;
    private final DirectExchange directExchange;
    private final Gson gson;

    @Autowired
    public MessageService(RabbitTemplate template, DirectExchange directExchange, Gson gson) {
        this.template = template;
        this.directExchange = directExchange;
        this.gson = gson;
    }

    public <T extends Message<?>> void sendMessage(Node node, T message) {
        String jsonMessage = gson.toJson(message);
        template.convertAndSend(directExchange.getName(), node.toString(), jsonMessage);
        logger.info("Message " + jsonMessage + " sent to node " + node);
    }
}
