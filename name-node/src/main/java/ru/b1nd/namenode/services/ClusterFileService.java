package ru.b1nd.namenode.services;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.b1nd.filesystem.services.FileSystemService;
import ru.b1nd.namenode.domain.File;
import ru.b1nd.namenode.domain.Partition;
import ru.b1nd.namenode.repositories.FileRepository;
import ru.b1nd.namenode.repositories.NodeRepository;
import ru.b1nd.namenode.repositories.PartitionRepository;
import ru.b1nd.namenode.utils.Converter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClusterFileService {

    private final Logger logger = LoggerFactory.getLogger(ClusterFileService.class);

    private final FileSystemService fileSystemService;
    private final NodeRepository nodeRepository;
    private final FileRepository fileRepository;
    private final PartitionRepository partitionRepository;

    @Autowired
    public ClusterFileService(FileSystemService fileSystemService, NodeRepository nodeRepository, FileRepository fileRepository, PartitionRepository partitionRepository) {
        this.fileSystemService = fileSystemService;
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

    public List<String> getFileNames() {
        return Lists.newArrayList(fileRepository.findAll()).stream().map(File::getName).collect(Collectors.toList());
    }

    public void downloadFile(String fileName) {
        var file = fileRepository.findFileByName(fileName);
        var partitions = new HashSet<>(partitionRepository.findAllByFile(file));

        new Thread(() -> partitions.forEach(p -> {
            try {
                fileSystemService.requestAndSaveFile(p.getNode().toString(), p.getFile().getName(), p.getW(), p.getH());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        })).start();
    }

    public void deletePartitions(Iterable<Partition> partitions) {
        partitionRepository.deleteAll(partitions);
    }

    public void deleteFile(File file) {
        fileRepository.delete(file);
    }
}
