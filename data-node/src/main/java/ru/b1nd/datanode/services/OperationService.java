package ru.b1nd.datanode.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.b1nd.filesystem.services.FileSystemService;
import ru.b1nd.operations.OperationUtils;
import ru.b1nd.operations.model.BinaryOperation;
import ru.b1nd.operations.model.UploadOperation;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;

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

    public void doBinaryOperation(BinaryOperation op, Class<?> opClass) throws IOException {
        String opName = OperationUtils.getNameByType(opClass);

        int w = op.getW();
        int h = op.getH();

        File left    = fileSystemService.getFilePartition(op.getLeft(), w, h);
        File right   = fileSystemService.getFilePartition(op.getRight(), w, h);
        File newFile = fileSystemService.createNewPartition(op.getFile(), w, h);

        var leftImage  = ImageIO.read(left);
        var rightImage = ImageIO.read(right);

        var params = new ParameterBlock();
        params.addSource(leftImage);
        params.addSource(rightImage);

        var image = JAI.create(opName, params).getAsBufferedImage();
        ImageIO.write(image, "tif", newFile);

        fileSystemService.registerPartition(op.getFile(), w, h);
    }
}
