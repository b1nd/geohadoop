package ru.b1nd.namenode.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.b1nd.namenode.services.OperationService;
import ru.b1nd.operations.OperationUtils;

import java.io.IOException;

@Controller
@RequestMapping("/api/operation")
public class OperationController {
    private final Logger logger = LoggerFactory.getLogger(OperationController.class);

    private final OperationService operationService;

    @Autowired
    public OperationController(OperationService operationService) {
        this.operationService = operationService;
    }

    @PostMapping("/upload")
    public @ResponseBody
    ResponseEntity<?> uploadOperation(@RequestParam("file") String file,
                                      @RequestParam(value = "partitions", required = false, defaultValue = "0") Integer partitionNum) {
        try {
            operationService.performUploadOperation(file, partitionNum);
        } catch (IOException e) {
            logger.error("Cannot perform upload operation with file " + file, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error in filesystem, upload operation could not be performed with file " + file);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete")
    public @ResponseBody
    ResponseEntity<?> deleteOperation(@RequestParam("file") String file) {
        try {
            operationService.performDeleteOperation(file);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/add")
    public @ResponseBody
    ResponseEntity<?> addOperation(@RequestParam String left, @RequestParam String right, @RequestParam String file) {
        return binaryOperationResponseEntity(left, right, file, OperationUtils.OperationType.ADD);
    }

    @PostMapping("/subtract")
    public @ResponseBody
    ResponseEntity<?> subtractOperation(@RequestParam String left, @RequestParam String right, @RequestParam String file) {
        return binaryOperationResponseEntity(left, right, file, OperationUtils.OperationType.SUBTRACT);
    }

    @PostMapping("/multiply")
    public @ResponseBody
    ResponseEntity<?> multiplyOperation(@RequestParam String left, @RequestParam String right, @RequestParam String file) {
        return binaryOperationResponseEntity(left, right, file, OperationUtils.OperationType.MULTIPLY);
    }

    @PostMapping("/divide")
    public @ResponseBody
    ResponseEntity<?> divideOperation(@RequestParam String left, @RequestParam String right, @RequestParam String file) {
        return binaryOperationResponseEntity(left, right, file, OperationUtils.OperationType.DIVIDE);
    }

    @PostMapping("/ndvi")
    public @ResponseBody
    ResponseEntity<?> ndviOperation(@RequestParam String file, @RequestParam String name) {
        try {
            operationService.performNDVIOperation(file, name);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Operation could not be performed: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<?> binaryOperationResponseEntity(String left, String right, String file, OperationUtils.OperationType type) {
        try {
            operationService.performBinaryOperation(left, right, file, type);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Operation could not be performed: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
