package ru.b1nd.datanode.services

import com.google.gson.Gson
import com.google.gson.JsonParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.b1nd.operations.OperationUtils
import ru.b1nd.operations.model.DeleteOperation
import ru.b1nd.operations.model.NDVIOperation
import ru.b1nd.operations.model.UploadOperation
import ru.b1nd.operations.model.binary.AddOperation
import ru.b1nd.operations.model.binary.DivideOperation
import ru.b1nd.operations.model.binary.MultiplyOperation
import ru.b1nd.operations.model.binary.SubtractOperation
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

@Service
class MessageService @Autowired constructor(private val operationService: OperationService,
                                            private val gson: Gson,
                                            private val parser: JsonParser,
                                            @Value("\${cores.num}") private var cores: String) {

    private val numCores = cores.toInt()

    private val executor = ThreadPoolExecutor(numCores, numCores,
            0, TimeUnit.MICROSECONDS,
            ArrayBlockingQueue(numCores), ThreadPoolExecutor.CallerRunsPolicy())

    fun acceptMessage(json: String) {
        val jsonObject = parser.parse(json).asJsonObject
        val name = jsonObject.get("name").asString
        val body = jsonObject.get("body")
        val type = OperationUtils.getTypeByName(name)
        val op = gson.fromJson(body, type)

        executor.submit {
            when (op) {
                is UploadOperation   -> operationService.doUploadOperation(op)
                is DeleteOperation   -> operationService.doDeleteOperation(op)
                is AddOperation      -> operationService.doBinaryOperation(op, name)
                is SubtractOperation -> operationService.doBinaryOperation(op, name)
                is MultiplyOperation -> operationService.doBinaryOperation(op, name)
                is DivideOperation   -> operationService.doBinaryOperation(op, name)
                is NDVIOperation     -> operationService.doNDVIOperation(op)
            }
        }
    }
}