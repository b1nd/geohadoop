package ru.b1nd.namenode.services;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.b1nd.namenode.domain.Node;
import ru.b1nd.namenode.repositories.NodeRepository;

import java.util.List;

@Service
public class ClusterService {

    private final Logger logger = LoggerFactory.getLogger(ClusterService.class);

    private final NodeRepository nodeRepository;

    @Autowired
    public ClusterService(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    public boolean addNode(String host, String port) {
        // TODO: implement. Check node heart beat, then add.
        logger.info("Node added, host: " + host + ", port: " + port);
        nodeRepository.save(new Node(host, port));
        return false;
    }

    public boolean deleteNode(String host, String port) {
        Node node = nodeRepository.findNodeByHostAndPort(host, port);
        if (node == null) {
            return false;
        } else {
            nodeRepository.delete(node);
            logger.info(node + " deleted");
            return true;
        }
    }

    public void synchronizeNodes() {
        Iterable<Node> nodes = nodeRepository.findAll();
        // TODO: implement. Check every node heart beat, del from repo if node is not active
    }

    public List<Node> getNodes() {
        synchronizeNodes();
        Iterable<Node> nodes = nodeRepository.findAll();
        return Lists.newArrayList(nodes);
    }
}
