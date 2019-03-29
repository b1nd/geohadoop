package ru.b1nd.datanode.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.b1nd.filesystem.services.FileSystemService;
import ru.b1nd.operations.model.UploadOperation;
import ru.b1nd.operations.model.binary.AddOperation;
import ru.b1nd.operations.model.binary.DivideOperation;
import ru.b1nd.operations.model.binary.MultiplyOperation;
import ru.b1nd.operations.model.binary.SubtractOperation;

@Service
public class OperationService {

    private final Logger logger = LoggerFactory.getLogger(OperationService.class);

    private final FileSystemService fileSystemService;

    @Autowired
    public OperationService(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    public void doUploadOperation(UploadOperation op) {
        fileSystemService.requestAndSaveFile(op.getFrom(), op.getFile(), op.getW(), op.getH());
    }

    public void doAddOperation(AddOperation op) {
        logger.info(op.toString());
    }

    public void doSubtractOperation(SubtractOperation op) {
        logger.info(op.toString());
    }

    public void doMultiplyOperation(MultiplyOperation op) {
        logger.info(op.toString());
    }

    public void doDivideOperation(DivideOperation op) {
        logger.info(op.toString());
    }
}
