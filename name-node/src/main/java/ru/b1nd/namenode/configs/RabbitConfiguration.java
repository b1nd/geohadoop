package ru.b1nd.namenode.configs;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import ru.b1nd.namenode.domain.Node;
import ru.b1nd.namenode.services.ClusterManagementService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@EnableRabbit
@Configuration
public class RabbitConfiguration {

    @Value("${spring.rabbitmq.template.exchange}")
    private String exchangeName;

    private final ClusterManagementService clusterManagementService;

    @Autowired
    public RabbitConfiguration(@Lazy ClusterManagementService clusterManagementService) {
        this.clusterManagementService = clusterManagementService;
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public List<Declarable> directBindings(DirectExchange directExchange) {
        List<Node> nodes = clusterManagementService.getNodes();
        List<Queue> queues = nodes.stream()
                .map(n -> new Queue(n.toString()))
                .collect(Collectors.toList());

        var bindings = queues.stream()
                .map(q -> BindingBuilder.bind(q).to(directExchange).with(q.getActualName()))
                .collect(Collectors.toList());

        List<Declarable> beans = new ArrayList<>(queues);
        beans.addAll(bindings);

        return beans;
    }
}
