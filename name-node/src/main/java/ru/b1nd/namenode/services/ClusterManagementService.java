package ru.b1nd.namenode.services;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.b1nd.namenode.domain.Node;
import ru.b1nd.namenode.repositories.NodeRepository;
import ru.b1nd.namenode.repositories.PartitionRepository;

import java.util.List;

@Service
public class ClusterManagementService {

    private final Logger logger = LoggerFactory.getLogger(ClusterManagementService.class);

    private final RestTemplate restTemplate;
    private final NodeRepository nodeRepository;
    private final PartitionRepository partitionRepository;
    private final RabbitService rabbitService;

    @Autowired
    public ClusterManagementService(RestTemplate restTemplate, NodeRepository nodeRepository, PartitionRepository partitionRepository, RabbitService rabbitService) {
        this.restTemplate = restTemplate;
        this.nodeRepository = nodeRepository;
        this.partitionRepository = partitionRepository;
        this.rabbitService = rabbitService;
    }

    public boolean addNode(String host, String port) {
        Node node = new Node(host, port);
        if (nodeRepository.findNodeByHostAndPort(host, port) == null) {
            if (checkHeartbeat(node)) {
                rabbitService.addNodeQueue(node);
                nodeRepository.save(node);

                logger.info("Node added, host: " + host + ", port: " + port);
                return true;
            }
        }
        return false;
    }

    public boolean deleteNode(String host, String port) {
        Node node = nodeRepository.findNodeByHostAndPort(host, port);
        if (node == null) {
            return false;
        } else {
            deleteNode(node);
            return true;
        }
    }

    public void synchronizeNodes() {
        nodeRepository.findAll().forEach(n -> {
            if (!checkHeartbeat(n)) {
                deleteNode(n);
            }
        });
    }

    public List<Node> getNodes() {
        synchronizeNodes();
        return Lists.newArrayList(nodeRepository.findAll());
    }

    private void deleteNode(Node node) {
        var partitions = partitionRepository.findAllByNode(node);
        partitionRepository.deleteAll(partitions);
        rabbitService.deleteNodeQueue(node);
        nodeRepository.delete(node);

        logger.info(node + " deleted");
    }

    private boolean checkHeartbeat(Node node) {
        ResponseEntity<String> entity;
        try {
            entity = restTemplate.getForEntity("http://" + node + "/node/heartbeat", String.class);
        } catch (RuntimeException e) {
            logger.info("Node " + node + " does not respond");
            return false;
        }
        return entity.getStatusCode() == HttpStatus.OK;
    }
}
