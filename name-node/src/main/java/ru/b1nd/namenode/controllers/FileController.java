package ru.b1nd.namenode.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.b1nd.filesystem.services.FileSystemService;
import ru.b1nd.namenode.services.FileService;

import javax.servlet.http.HttpServletRequest;
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

    @GetMapping("/{file:.+}")
    public @ResponseBody
    ResponseEntity<?> downloadFile(@PathVariable String file, HttpServletRequest request) {
        Resource resource;
        try {
            resource = fileService.getFileAsResource(file);
        } catch (Exception e) {
            logger.error("Cannot get requested file " + file);
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        String contentType;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/merge")
    public @ResponseBody
    ResponseEntity<?> mergeTifFile(@RequestParam("file") String file, @RequestParam(value = "type", defaultValue = "1") Integer type) {
        try {
            fileService.mergeTifFile(file, type);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
        return ResponseEntity.ok().body("File " + file + " merged!");
    }

    @GetMapping("/output")
    public @ResponseBody
    List<String> getOutputFiles() {
        try {
            return fileService.getOutputFileNames();
        } catch (IOException e) {
            logger.error("Can't get output file names", e);
        }
        return null;
    }

    @GetMapping("/all")
    public @ResponseBody
    List<String> getFileNames() {
        try {
            return fileSystemService.getFileNames();
        } catch (IOException e) {
            logger.error("Can't get local file names", e);
        }
        return null;
    }

}
