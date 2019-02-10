package ru.b1nd.namenode.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.b1nd.namenode.model.Message;
import ru.b1nd.operations.model.UploadOperation;

import static ru.b1nd.namenode.utils.Converter.getNodeByHostPort;

@Service
public class OperationService {

    private final Logger logger = LoggerFactory.getLogger(OperationService.class);

    private final MessageService messageService;

    @Autowired
    public OperationService(MessageService messageService) {
        this.messageService = messageService;
    }

    public void performUploadOperation(String file, int w, int h, String from, String to) {
        messageService.sendMessage(getNodeByHostPort(to), new Message<>(new UploadOperation(file, w, h, from)));
    }
}
