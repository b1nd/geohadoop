package ru.b1nd.namenode.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.b1nd.namenode.services.ClusterFileService;

@Controller
@RequestMapping("/cluster/partition")
public class ClusterFileController {

    private final Logger logger = LoggerFactory.getLogger(ClusterFileController.class);

    private final ClusterFileService clusterFileService;

    @Autowired
    public ClusterFileController(ClusterFileService clusterFileService) {
        this.clusterFileService = clusterFileService;
    }

    @PostMapping("/add")
    public @ResponseBody
    ResponseEntity<?> addPartition(@RequestParam String node, @RequestParam String file, @RequestParam Integer w, @RequestParam Integer h) {
        clusterFileService.updatePartition(node, file, w, h, ClusterFileService.AlterType.ADD);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/del")
    public @ResponseBody
    ResponseEntity<?> delPartition(@RequestParam String node, @RequestParam String file, @RequestParam Integer w, @RequestParam Integer h) {
        clusterFileService.updatePartition(node, file, w, h, ClusterFileService.AlterType.DELETE);
        return ResponseEntity.ok().build();
    }
}
