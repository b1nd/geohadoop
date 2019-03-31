package ru.b1nd.datanode.services

import com.google.gson.Gson
import com.google.gson.JsonParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.b1nd.operations.OperationUtils
import ru.b1nd.operations.model.UploadOperation
import ru.b1nd.operations.model.binary.AddOperation
import ru.b1nd.operations.model.binary.DivideOperation
import ru.b1nd.operations.model.binary.MultiplyOperation
import ru.b1nd.operations.model.binary.SubtractOperation

@Service
class MessageService @Autowired constructor(private val operationService: OperationService,
                                            private val gson: Gson,
                                            private val parser: JsonParser) {

    private val logger: Logger = LoggerFactory.getLogger(MessageService::class.java)

    fun acceptMessage(json: String) {
        val jsonObject = parser.parse(json).asJsonObject
        val name = jsonObject.get("name").asString
        val body = jsonObject.get("body")
        val type = OperationUtils.getTypeByName(name)
        val op = gson.fromJson(body, type)

        when (op) {
            is UploadOperation -> operationService.doUploadOperation(op)
            is AddOperation -> operationService.doBinaryOperation(op, op.javaClass)
            is SubtractOperation -> operationService.doBinaryOperation(op, op.javaClass)
            is MultiplyOperation -> operationService.doBinaryOperation(op, op.javaClass)
            is DivideOperation -> operationService.doBinaryOperation(op, op.javaClass)
        }
    }
}