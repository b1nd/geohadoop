package ru.b1nd.namenode.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.b1nd.namenode.domain.File;
import ru.b1nd.namenode.domain.Node;
import ru.b1nd.namenode.domain.Partition;

import java.util.List;

public interface PartitionRepository extends CrudRepository<Partition, Long> {

    Partition findByFileAndNodeAndWAndH(File file, Node node, Integer w, Integer h);

    List<Partition> findAllByFileAndWAndH(File file, Integer w, Integer h);

    List<Partition> findAllByFile(File file);

    List<Partition> findAllByNode(Node node);
}
