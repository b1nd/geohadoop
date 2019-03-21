package ru.b1nd.namenode.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import ru.b1nd.filesystem.services.FileSystemService;
import ru.b1nd.namenode.domain.Node;
import ru.b1nd.operations.model.Message;
import ru.b1nd.operations.model.UploadOperation;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OperationService {

    private final Logger logger = LoggerFactory.getLogger(OperationService.class);

    @Value("${main.server.name}")
    private String myAddress;

    private final MessageService messageService;
    private final ClusterManagementService clusterManagementService;
    private final ClusterFileService clusterFileService;
    private final FileSystemService fileSystemService;

    @Autowired
    public OperationService(MessageService messageService, ClusterManagementService clusterManagementService, ClusterFileService clusterFileService, FileSystemService fileSystemService) {
        this.messageService = messageService;
        this.clusterManagementService = clusterManagementService;
        this.clusterFileService = clusterFileService;
        this.fileSystemService = fileSystemService;
    }

    public void performUploadOperation(String fileName, int partitionNum) throws Exception {
        List<File> filePartitions = fileSystemService.getFilePartitions(fileName);
        List<Pair<Integer, Integer>> wh = getFilesWH(filePartitions.stream().map(File::getName));
        List<Node> nodes = clusterManagementService.getNodes();
        if (nodes.isEmpty()) {
            throw new Exception("There are no available nodes to perform upload operation");
        }
        List<Node> workers = nodes.stream()
                .limit(partitionNum < 1 || partitionNum > nodes.size() ? nodes.size() : partitionNum)
                .collect(Collectors.toList());

        var del  = wh.size() / workers.size();
        var load = del % 10 == 0 ? del : del + 1;
        var it   = wh.iterator();

        clusterFileService.registerFile(fileName);

        workers.forEach(n -> {
            for (int i = 0; i < load && it.hasNext(); ++i) {
                sendUploadOperationMessage(n, fileName, it.next());
            }
        });
    }

    private void sendUploadOperationMessage(Node to, String fileName, Pair<Integer, Integer> wh) {
        var op = new UploadOperation(fileName, wh.getFirst(), wh.getSecond(), myAddress);
        messageService.sendMessage(to, new Message<>(op));
    }

    private List<Pair<Integer, Integer>> getFilesWH(Stream<String> fileNames) {
        var pattern = Pattern.compile("w(\\d+)h(\\d+)\\.tif{1,2}$");
        return fileNames.map(name -> {
            var matcher = pattern.matcher(name);
            if (matcher.matches()) {
                var w = Integer.parseInt(matcher.group(1));
                var h = Integer.parseInt(matcher.group(2));
                return Pair.of(w, h);
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
