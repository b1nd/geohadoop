package ru.b1nd.namenode.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.b1nd.namenode.services.ClusterFileService;

import java.util.List;

@Controller
@RequestMapping("/api/cluster/file")
public class ClusterFileController {

    private final Logger logger = LoggerFactory.getLogger(ClusterManagementController.class);

    private final ClusterFileService clusterFileService;

    @Autowired
    public ClusterFileController(ClusterFileService clusterFileService) {
        this.clusterFileService = clusterFileService;
    }

    @GetMapping("/all")
    public @ResponseBody
    List<String> getFiles() {
        return clusterFileService.getFileNames();
    }

    @GetMapping("/download")
    public @ResponseBody
    ResponseEntity<?> downloadFileFromCluster(@RequestParam("file") String file) {
        clusterFileService.downloadFile(file);
        return ResponseEntity.ok().body("File " + file + " will be downloaded from cluster");
    }
}
