package ru.b1nd.namenode.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.b1nd.filesystem.services.FileSystemService;
import ru.b1nd.namenode.services.FileService;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/api/file")
public class FileController {
    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;
    private final FileSystemService fileSystemService;

    @Autowired
    public FileController(FileService fileService, FileSystemService fileSystemService) {
        this.fileService = fileService;
        this.fileSystemService = fileSystemService;
    }

    @PostMapping("/upload")
    public @ResponseBody
    ResponseEntity<?> uploadFile(@RequestParam("file") List<MultipartFile> uploadFiles) {
        if (uploadFiles.isEmpty()) {
            return new ResponseEntity<>("please select a file!", HttpStatus.BAD_REQUEST);
        }
        try {
            fileService.saveUploadedFiles(uploadFiles);
        } catch (IOException e) {
            logger.error("File could not be saved", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("Successfully uploaded", HttpStatus.OK);
    }

    @GetMapping("/all")
    public @ResponseBody
    List<String> getFileNames() {
        try {
            return fileSystemService.getFileNames();
        } catch (IOException e) {
            logger.error("Can't get file names", e);
        }
        return null;
    }

}
