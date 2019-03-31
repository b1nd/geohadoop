package ru.b1nd.namenode.services;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import ru.b1nd.filesystem.services.FileSystemService;
import ru.b1nd.namenode.domain.Node;
import ru.b1nd.namenode.domain.Partition;
import ru.b1nd.operations.OperationUtils;
import ru.b1nd.operations.model.BinaryOperation;
import ru.b1nd.operations.model.Message;
import ru.b1nd.operations.model.UploadOperation;
import ru.b1nd.operations.model.binary.AddOperation;
import ru.b1nd.operations.model.binary.DivideOperation;
import ru.b1nd.operations.model.binary.MultiplyOperation;
import ru.b1nd.operations.model.binary.SubtractOperation;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
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

    public void performBinaryOperation(String leftFileName, String rightFileName, String newFileName, OperationUtils.OperationType type) throws IOException {
        var operations = shuffle(leftFileName, rightFileName, newFileName);
        operations.forEach(op -> messageService.sendMessage(op.node, new Message<>(
                getBinaryOperation(op.binary, type))));
    }

    private BinaryOperation getBinaryOperation(BinaryOperation op, OperationUtils.OperationType type) {
        switch (type) {
            case ADD:
                return new AddOperation(op.getLeft(), op.getRight(), op.getW(), op.getH(), op.getFile());
            case SUBTRACT:
                return new SubtractOperation(op.getLeft(), op.getRight(), op.getW(), op.getH(), op.getFile());
            case MULTIPLY:
                return new MultiplyOperation(op.getLeft(), op.getRight(), op.getW(), op.getH(), op.getFile());
            case DIVIDE:
                return new DivideOperation(op.getLeft(), op.getRight(), op.getW(), op.getH(), op.getFile());
            default:
               throw new IllegalArgumentException(type + " not a binary operation!");
        }
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

    private List<NodeBinaryOperation> shuffle(String leftFileName, String rightFileName, String newFileName) throws IOException {
        var leftFile  = clusterFileService.getFile(leftFileName);
        var rightFile = clusterFileService.getFile(rightFileName);

        var leftPartitions = clusterFileService.getPartitions(leftFile);
        var rightPartitions = clusterFileService.getPartitions(rightFile);

        var leftWH = leftPartitions.stream().map(p -> Pair.of(p.getW(), p.getH())).distinct();
        var rightWH = rightPartitions.stream().map(p -> Pair.of(p.getW(), p.getH())).distinct().collect(Collectors.toList());

        var commonWH = leftWH.filter(rightWH::contains).distinct().collect(Collectors.toList());

        clusterFileService.registerFile(newFileName);

        List<NodeBinaryOperation> operations = Lists.newArrayList();

        for (var p : commonWH) {
            var op = shuffle(leftPartitions, rightPartitions, p.getFirst(), p.getSecond(), newFileName);
            operations.add(op);
        }
        return operations;
    }

    private NodeBinaryOperation shuffle(List<Partition> allLeftPartitions, List<Partition> allRightPartitions, int w, int h, String newFileName) throws IOException {
        var leftPartitions  = findPartitionsByWH(allLeftPartitions, w, h);
        var rightPartitions = findPartitionsByWH(allRightPartitions, w, h);

        var leftNodes  = leftPartitions.stream().map(Partition::getNode).distinct();
        var rightNodes = rightPartitions.stream().map(Partition::getNode).collect(Collectors.toSet());

        var node = leftNodes.filter(rightNodes::contains).findFirst();

        var leftFileName  = leftPartitions.get(0).getFile().getName();
        var rightFileName = rightPartitions.get(0).getFile().getName();

        if (node.isPresent()) {
            logger.info("Found node " + node.get() + " with similar partition w=" + w + " h=" + h + " left=" + leftFileName + " right=" + rightFileName);
            return new NodeBinaryOperation(node.get(), new BinaryOperation(leftFileName, rightFileName, w, h, newFileName));
        } else {
            var leftPart  = leftPartitions.get(0);
            var rightPart = rightPartitions.get(0);

            logger.info("Similar partition not found. Telling " + leftPart.getNode() + " to download file "
                    + rightFileName + "w" + w + "h" + h + " from " + rightPart.getNode());

            sendUploadMessage(leftPart.getNode(), rightPart.getNode(), rightFileName, Pair.of(leftPart.getW(), leftPart.getH()));

            return new NodeBinaryOperation(leftPart.getNode(), new BinaryOperation(leftFileName, rightFileName, w, h, newFileName));
        }
    }

    private List<Partition> findPartitionsByWH(Collection<Partition> partitions, int w, int h) throws IOException {
        List<Partition> res = partitions.stream().filter(p -> p.getW() == w && p.getH() == h).collect(Collectors.toList());
        if (res.isEmpty()) {
            throw new IOException("No partitions " + partitions.iterator().next().getFile().getName() + " w=" + w + " h=" + h + " found in file system!");
        } else {
            return res;
        }
    }

    private void sendUploadMessage(Node to, Node from, String fileName, Pair<Integer, Integer> wh) {
        var op = new UploadOperation(fileName, wh.getFirst(), wh.getSecond(), from.toString());
        messageService.sendMessage(to, new Message<>(op));
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

    private class NodeBinaryOperation {
        private Node node;
        private BinaryOperation binary;

        NodeBinaryOperation(Node node, BinaryOperation binary) {
            this.node = node;
            this.binary = binary;
        }
    }
}
