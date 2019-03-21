package ru.b1nd.namenode.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.b1nd.namenode.domain.Node;
import ru.b1nd.namenode.services.ClusterManagementService;

import java.util.List;

@Controller
@RequestMapping("/api/cluster")
public class ClusterManagementController {

    private final Logger logger = LoggerFactory.getLogger(ClusterManagementController.class);

    private final ClusterManagementService clusterManagementService;

    @Autowired
    public ClusterManagementController(ClusterManagementService clusterManagementService) {
        this.clusterManagementService = clusterManagementService;
    }

    @PostMapping("/add")
    public @ResponseBody
    ResponseEntity<?> addNode(@RequestParam("host") String host, @RequestParam("port") String port) {
        if (clusterManagementService.addNode(host, port)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            String error = "Node " + host + ":" + port + " does not respond or already exists";
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/del")
    public @ResponseBody
    ResponseEntity<?> deleteNode(@RequestParam("host") String host, @RequestParam("port") String port) {
        if (clusterManagementService.deleteNode(host, port)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Node " + host + ":" + port + " not found", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/synchronize")
    public @ResponseBody
    ResponseEntity<?> synchronizeNodes() {
        clusterManagementService.synchronizeNodes();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/all")
    public @ResponseBody
    List<Node> getNodes() {
        return clusterManagementService.getNodes();
    }
}
