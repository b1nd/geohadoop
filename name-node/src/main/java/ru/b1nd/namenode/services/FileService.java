package ru.b1nd.namenode.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {
    private final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Value("${upload.dir}")
    private String uploadDir;
    @Value("${tile.width}")
    private Integer tileWidth;
    @Value("${tile.height}")
    private Integer tileHeight;

    @EventListener(ApplicationReadyEvent.class)
    public void createUploadDirectory() {
        if (new File(uploadDir).mkdirs()) {
            logger.debug("Dir " + uploadDir + " init");
        }
    }

    public void saveUploadedFiles(List<MultipartFile> files) throws IOException {
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                byte[] bytes = file.getBytes();
                String newFilePath = uploadDir + file.getOriginalFilename();
                String newFileName = "" + file.getOriginalFilename();
                File newFile = new File(newFilePath);
                String ext = getFileExtension(newFileName);

                Files.write(newFile.toPath(), bytes);
                logger.info("File " + newFileName + " saved");

                if (ext.equals("tif") || ext.equals("tiff")) {
                    splitTifFile(newFileName);

                    if (new File(newFilePath).delete()) {
                        logger.info("File " + newFileName + " was successfully slitted and deleted");
                    } else {
                        logger.info("Can't delete file " + newFileName);
                    }
                }
            }
        }
    }

    public List<String> getFileNames() throws IOException {
        return Files.list(new File(uploadDir).toPath())
                .filter(Files::isDirectory)
                .map(f -> f.toFile().getName())
                .collect(Collectors.toList());
    }

    private void splitTifFile(String fileName) throws IOException {
        String path = uploadDir + fileName;
        try (var imageInputStream = new FileImageInputStream(new File(path))) {
            var reader = ImageIO.getImageReaders(imageInputStream).next();
            reader.setInput(imageInputStream);
            var param = reader.getDefaultReadParam();

            for (int w = 0; w < reader.getWidth(0); w += tileWidth) {
                for (int h = 0; h < reader.getHeight(0); h += tileHeight) {
                    param.setSourceRegion(new Rectangle(w, h, tileWidth, tileHeight));

                    BufferedImage tile = reader.read(0, param);

                    String filesDir = uploadDir + getFileNameWithoutExtension(fileName) + "/";
                    if (new File(filesDir).mkdirs()) {
                        logger.debug("Dir " + filesDir + " created");
                    }
                    ImageIO.write(tile, "tif", new File(filesDir + fileName + "w" + w + "h" + h + ".tif"));
                }
            }
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1);
    }

    private String getFileNameWithoutExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? "" : fileName.substring(0, dotIndex);
    }
}
