package ru.b1nd.namenode.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.b1nd.namenode.domain.File;
import ru.b1nd.namenode.domain.Partition;
import ru.b1nd.namenode.repositories.FileRepository;
import ru.b1nd.namenode.repositories.NodeRepository;
import ru.b1nd.namenode.repositories.PartitionRepository;
import ru.b1nd.namenode.utils.Converter;

@Service
public class ClusterFileService {

    private final NodeRepository nodeRepository;
    private final FileRepository fileRepository;
    private final PartitionRepository partitionRepository;

    @Autowired
    public ClusterFileService(NodeRepository nodeRepository, FileRepository fileRepository, PartitionRepository partitionRepository) {
        this.nodeRepository = nodeRepository;
        this.fileRepository = fileRepository;
        this.partitionRepository = partitionRepository;
    }

    public enum AlterType {
        ADD,
        DELETE
    }

    public void updatePartition(String nodeString, String fileName, int w, int h, AlterType type) {
        var hostPort = Converter.getHostPort(nodeString);
        var node = nodeRepository.findNodeByHostAndPort(hostPort.getFirst(), hostPort.getSecond());
        var file = fileRepository.findFileByName(fileName);
        switch (type) {
            case ADD:
                var newPartition = new Partition(file, node, w, h);
                partitionRepository.save(newPartition);
                break;
            case DELETE:
                var delPartition = partitionRepository.findByFileAndNodeAndWAndH(file, node, w, h);
                partitionRepository.delete(delPartition);
                break;
        }
    }

    public void registerFile(String fileName) {
        fileRepository.save(new File(fileName));
    }
}
