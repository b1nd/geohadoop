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

import java.util.List;

import static ru.b1nd.namenode.utils.Converter.getHostPortByNode;

@Service
public class ClusterService {

    private final Logger logger = LoggerFactory.getLogger(ClusterService.class);

    private final RestTemplate restTemplate;
    private final NodeRepository nodeRepository;
    private final RabbitService rabbitService;

    @Autowired
    public ClusterService(RestTemplate restTemplate, NodeRepository nodeRepository, RabbitService rabbitService) {
        this.restTemplate = restTemplate;
        this.nodeRepository = nodeRepository;
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
            rabbitService.deleteNodeQueue(node);
            nodeRepository.delete(node);

            logger.info(node + " deleted");
            return true;
        }
    }

    public void synchronizeNodes() {
        nodeRepository.findAll().forEach(n -> {
            if (!checkHeartbeat(n)) {
                nodeRepository.delete(n);
            }
        });
    }

    public List<Node> getNodes() {
        synchronizeNodes();
        return Lists.newArrayList(nodeRepository.findAll());
    }

    private boolean checkHeartbeat(Node node) {
        var entity = restTemplate.getForEntity(getHostPortByNode(node) + "/node/heartbeat", ResponseEntity.class);
        return entity.getStatusCode().equals(HttpStatus.OK);
    }
}
