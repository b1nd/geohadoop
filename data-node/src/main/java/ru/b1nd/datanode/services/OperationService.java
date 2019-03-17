package ru.b1nd.datanode.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.b1nd.filesystem.services.FileSystemService;
import ru.b1nd.operations.model.UploadOperation;

@Service
public class OperationService {

    private final FileSystemService fileSystemService;

    @Autowired
    public OperationService(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    public void doUploadOperation(UploadOperation op) {
        fileSystemService.requestAndSaveFile(op.getFrom(), op.getFile(), op.getW(), op.getH());
    }
}
