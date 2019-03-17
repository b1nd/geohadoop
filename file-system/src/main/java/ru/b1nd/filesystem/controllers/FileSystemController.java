package ru.b1nd.filesystem.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.b1nd.filesystem.services.FileSystemService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import static ru.b1nd.filesystem.utils.Converter.partName;

@Controller
@RequestMapping("/file")
public class FileSystemController {
    private final Logger logger = LoggerFactory.getLogger(FileSystemController.class);

    private final FileSystemService fileSystemService;

    @Autowired
    public FileSystemController(FileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    @PostMapping("/{from:.+}")
    public @ResponseBody
    ResponseEntity<?> uploadFile(@PathVariable String from, @RequestParam String file, @RequestParam Integer w, @RequestParam Integer h) {
        fileSystemService.requestAndSaveFile(from, file, w, h);
        return ResponseEntity.ok().body("File " + file + partName(w, h) + " successfully uploaded from " + from);
    }

    @GetMapping("/{file:.+}")
    public @ResponseBody
    ResponseEntity<Resource> downloadFile(@PathVariable String file, @RequestParam Integer w, @RequestParam Integer h,
                                          HttpServletRequest request) {
        Resource resource;
        try {
            resource = fileSystemService.getFileAsResource(file, w, h);
        } catch (IOException e) {
            logger.error("Cannot get requested file " + file + w + h);
            return ResponseEntity.badRequest().build();
        }

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }
        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
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
