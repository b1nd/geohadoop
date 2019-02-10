package ru.b1nd.namenode.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.b1nd.namenode.services.FileService;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/api/file")
public class FileController {
    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
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
            return fileService.getFileNames();
        } catch (IOException e) {
            logger.error("Can't get file names", e);
        }
        return null;
    }

}
