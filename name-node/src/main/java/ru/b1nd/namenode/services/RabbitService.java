package ru.b1nd.namenode.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.b1nd.namenode.domain.Node;

import static ru.b1nd.namenode.utils.Converter.getQueueNameByNode;

@Service
public class RabbitService {

    private final Logger logger = LoggerFactory.getLogger(RabbitService.class);

    private final AmqpAdmin amqpAdmin;
    private final DirectExchange directExchange;

    @Autowired
    public RabbitService(AmqpAdmin amqpAdmin, DirectExchange directExchange) {
        this.amqpAdmin = amqpAdmin;
        this.directExchange = directExchange;
    }

    public void addNodeQueue(Node node) {
        Queue queue = new Queue(getQueueNameByNode(node));
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(getBindingByQueue(queue));

        logger.info("Queue " + queue.getActualName() + " added");
    }

    public void deleteNodeQueue(Node node) {
        Queue queue = new Queue(getQueueNameByNode(node));
        amqpAdmin.removeBinding(getBindingByQueue(queue));
        amqpAdmin.deleteQueue(queue.getActualName());

        logger.info("Queue " + queue.getActualName() + " deleted");
    }

    private Binding getBindingByQueue(Queue queue) {
        return BindingBuilder.bind(queue).to(directExchange).with(queue.getActualName());
    }
}
