package ru.b1nd.namenode.services;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.b1nd.namenode.domain.File;
import ru.b1nd.namenode.domain.Partition;
import ru.b1nd.namenode.repositories.FileRepository;
import ru.b1nd.namenode.repositories.NodeRepository;
import ru.b1nd.namenode.repositories.PartitionRepository;
import ru.b1nd.namenode.utils.Converter;

import java.io.IOException;
import java.util.List;

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

    public void registerFile(String fileName) throws IOException {
        if (fileRepository.findFileByName(fileName) != null) {
            throw new IOException("File " + fileName + " already exists! You must change file name or delete old file.");
        }
        fileRepository.save(new File(fileName));
    }

    public File getFile(String fileName) throws IOException {
        File file = fileRepository.findFileByName(fileName);
        if (file == null) {
            throw new IOException("File " + fileName + " not found in file system!");
        } else {
            return file;
        }
    }

    public List<Partition> getPartitions(File file, int w, int h) throws IOException {
        var partitions = partitionRepository.findAllByFileAndWAndH(file, w, h);
        if (partitions.isEmpty()) {
            throw new IOException("No partitions " + file + " w=" + w + " h=" + h + " found in file system!");
        } else {
            return partitions;
        }
    }

    public List<Partition> getPartitions(File file) throws IOException {
        var partitions = partitionRepository.findAllByFile(file);
        if (partitions.isEmpty()) {
            throw new IOException("No file " + file + " partitions found in file system!");
        } else {
            return partitions;
        }
    }

    public List<Partition> getPartitions() throws IOException {
        var partitions = Lists.newArrayList(partitionRepository.findAll());
        if (partitions.isEmpty()) {
            throw new IOException("No partitions found in file system!");
        } else {
            return partitions;
        }
    }
}
