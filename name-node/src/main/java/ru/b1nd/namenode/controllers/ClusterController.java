package ru.b1nd.namenode.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.b1nd.namenode.domain.Node;
import ru.b1nd.namenode.services.ClusterService;

import java.util.List;

@Controller
@RequestMapping("/api/cluster")
public class ClusterController {

    private final Logger logger = LoggerFactory.getLogger(ClusterController.class);

    private final ClusterService clusterService;

    @Autowired
    public ClusterController(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @PostMapping("/add")
    public @ResponseBody
    ResponseEntity<?> addNode(@RequestParam("host") String host, @RequestParam("port") String port) {
        if (clusterService.addNode(host, port)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Node " + host + ":" + port + " does not respond", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/del")
    public @ResponseBody
    ResponseEntity<?> deleteNode(@RequestParam("host") String host, @RequestParam("port") String port) {
        if (clusterService.deleteNode(host, port)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Node " + host + ":" + port + " not found", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/synchronize")
    public @ResponseBody
    ResponseEntity<?> synchronizeNodes() {
        clusterService.synchronizeNodes();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/all")
    public @ResponseBody
    List<Node> getNodes() {
        return clusterService.getNodes();
    }
}
