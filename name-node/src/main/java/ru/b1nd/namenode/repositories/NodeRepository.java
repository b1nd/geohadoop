package ru.b1nd.namenode.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.b1nd.namenode.domain.Node;

public interface NodeRepository extends CrudRepository<Node, Long> {

    Node findNodeByHostAndPort(String host, String port);
}
