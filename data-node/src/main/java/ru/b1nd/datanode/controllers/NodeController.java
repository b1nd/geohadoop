package ru.b1nd.datanode.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/node")
public class NodeController {

    @Value("${queue.name}")
    private String queueName;

    @GetMapping("/heartbeat")
    public ResponseEntity<?> getHeartBeat() {
        return new ResponseEntity<>(queueName, HttpStatus.OK);
    }

}
