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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    ResponseEntity<?> uploadFile(@RequestParam("file") List<MultipartFile> uploadFiles,
                                 @RequestParam(name = "dir", required = false) String dirName) {
        String uploadedFileNames = uploadFiles.stream()
                .map(MultipartFile::getOriginalFilename)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(", "));

        if (uploadedFileNames.isEmpty()) {
            return new ResponseEntity<>("please select a file!", HttpStatus.BAD_REQUEST);
        }
        try {
            fileService.saveUploadedFiles(uploadFiles, dirName);
        } catch (IOException e) {
            logger.error("File could not be saved", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("File " + uploadedFileNames + " uploaded");

        return new ResponseEntity<>("Successfully uploaded - " +
                uploadedFileNames, HttpStatus.OK);
    }

    @PostMapping("/split")
    public @ResponseBody
    ResponseEntity<?> splitTiffFile(@RequestParam("filename") String fileName) {
        fileService.splitTiffFile(fileName);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/all")
    public @ResponseBody
    List<String> getFileNames(@RequestParam(name = "dir", required = false) String dirName) {
        try {
            return fileService.getFileNames(dirName);
        } catch (IOException e) {
            logger.error("Can't get files in dir " + dirName, e);
        }
        return Collections.emptyList();
    }

}
