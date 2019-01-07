package ru.b1nd.namenode.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileService {
    private final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Value("${upload.dir}")
    private String uploadDir;

    public void saveUploadedFiles(List<MultipartFile> files, String dirName) throws IOException {
        String finalDir = getRightDirPath(dirName);
        if (new File(finalDir).mkdirs()) {
            logger.debug("New dirs were created");
        }
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                byte[] bytes = file.getBytes();
                File newFile = new File(finalDir + file.getOriginalFilename());
                Files.write(newFile.toPath(), bytes);
            }
        }
    }

    public void splitTiffFile(String fileName) {
    }

    public List<String> getFileNames(String dirName) throws IOException {
        String finalDir = getRightDirPath(dirName);
        return Files.list(Paths.get(finalDir))
                .map(f -> f.toFile().getName())
                .collect(Collectors.toList());
    }

    private String getRightDirPath(String dirName) {
        if (dirName == null || dirName.isEmpty()) {
            return uploadDir;
        } else {
            return uploadDir + (dirName.charAt(dirName.length() - 1) == '/' ? dirName : dirName + "/");
        }
    }
}
