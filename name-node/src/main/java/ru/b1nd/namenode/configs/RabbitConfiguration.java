package ru.b1nd.namenode.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.b1nd.namenode.domain.Node;
import ru.b1nd.namenode.services.ClusterService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@EnableRabbit
@Configuration
public class RabbitConfiguration {

    Logger logger = LoggerFactory.getLogger(RabbitConfiguration.class);

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchangeName;

    private final ClusterService clusterService;

    @Autowired
    public RabbitConfiguration(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @Bean
    public List<Declarable> directBindings() {
        List<Node> nodes = clusterService.getNodes();
        List<Queue> queues = nodes.stream()
                .map(n -> new Queue(n.getHost() + ":" + n.getPort()))
                .collect(Collectors.toList());

        var directExchange = new DirectExchange(exchangeName);
        var bindings = queues.stream()
                .map(q -> BindingBuilder.bind(q).to(directExchange).with(q.getActualName()))
                .collect(Collectors.toList());

        List<Declarable> beans = new ArrayList<>(queues);
        beans.add(directExchange);
        beans.addAll(bindings);

        return beans;
    }
}
