package ru.b1nd.datanode.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.b1nd.filesystem.services.FileSystemService
import ru.b1nd.operations.OperationUtils
import ru.b1nd.operations.model.BinaryOperation
import ru.b1nd.operations.model.DeleteOperation
import ru.b1nd.operations.model.NDVIOperation
import ru.b1nd.operations.model.UploadOperation
import ru.b1nd.operations.model.binary.DivideOperation
import java.awt.image.BufferedImage
import java.awt.image.DataBuffer
import java.awt.image.renderable.ParameterBlock
import javax.imageio.ImageIO
import javax.media.jai.JAI
import javax.media.jai.operator.FormatDescriptor


@Service
class OperationService @Autowired
constructor(private val fileSystemService: FileSystemService) {

    private val logger = LoggerFactory.getLogger(OperationService::class.java)

    fun doUploadOperation(op: UploadOperation) {
        fileSystemService.requestAndSaveAndRegisterFile(op.from, op.file, op.w, op.h)
    }

    fun doDeleteOperation(op: DeleteOperation) {
        fileSystemService.deleteFile(op.file)
    }

    fun doBinaryOperation(op: BinaryOperation, opName: String) {
        val w = op.w!!
        val h = op.h!!

        val left    = fileSystemService.getFilePartition(op.left, w, h)
        val right   = fileSystemService.getFilePartition(op.right, w, h)
        val newFile = fileSystemService.createNewPartition(op.file, w, h)

        val leftImage  = ImageIO.read(left)
        val rightImage = ImageIO.read(right)

        val leftType  = when(op) {
            is DivideOperation -> FormatDescriptor.create(leftImage, DataBuffer.TYPE_FLOAT, null)
            else -> leftImage
        }
        val rightType = when(op) {
            is DivideOperation -> FormatDescriptor.create(rightImage, DataBuffer.TYPE_FLOAT, null)
            else -> rightImage
        }

        val params = ParameterBlock()
        params.addSource(leftType)
        params.addSource(rightType)

        val image = JAI.create(opName, params).asBufferedImage
        ImageIO.write(image, "tif", newFile)

        logger.info("$opName successfully done!")

        fileSystemService.registerPartition(op.file, w, h)
    }

    fun doNDVIOperation(op: NDVIOperation) = fileSystemService.getFilePartitions(op.file).forEach {
        val opName = OperationUtils.getNameByType(op.javaClass)
        val wh = getWHbyName(it.name)
        if (wh != null) {
            val image  = ImageIO.read(it)
            val pixels = image.data.getPixels(image.minX, image.minY, image.data.width, image.data.height, null as FloatArray?)
            val ndviIm = BufferedImage(image.data.width, image.data.height, BufferedImage.TYPE_INT_RGB)

            for (x in 0 until ndviIm.width) {
                for (y in 0 until ndviIm.height) {
                    ndviIm.setRGB(x, y, getNDVIColor(pixels[y * ndviIm.width + x]))
                }
            }
            val file = fileSystemService.createNewPartition(op.fileName, wh.first, wh.second)
            ImageIO.write(ndviIm, "tif", file)

            logger.info("$opName successfully done!")

            fileSystemService.registerPartition(op.fileName, wh.first, wh.second)
        }
    }

    private fun getNDVIColor(v: Float) = when (v) {
        in 0.9..1.0     -> int(4, 18, 4)
        in 0.8..0.9     -> int(4, 38, 4)
        in 0.7..0.8     -> int(4, 58, 4)
        in 0.6..0.7     -> int(4, 74, 4)
        in 0.5..0.6     -> int(4, 98, 4)
        in 0.45..0.5    -> int(28, 114, 4)
        in 0.4..0.45    -> int(60, 134, 4)
        in 0.35..0.4    -> int(68, 142, 4)
        in 0.3..0.35    -> int(92, 154, 4)
        in 0.25..0.3    -> int(116, 170, 4)
        in 0.2..0.25    -> int(148, 182, 20)
        in 0.166..0.2   -> int(132, 162, 44)
        in 0.133..0.166 -> int(148, 144, 60)
        in 0.1..0.133   -> int(164, 130, 76)
        in 0.066..0.1   -> int(172, 144, 92)
        in 0.033..0.066 -> int(204, 190, 172)
        in 0.0..0.033   -> int(252, 254, 252)
        in -1.0..0.0    -> int(4, 18, 60)
        else            -> 0
    }

    private fun int(r: Int, g: Int, b: Int) = (r and 0x0ff shl 16) or (g and 0x0ff shl 8) or (b and 0x0ff)

    private fun getWHbyName(fileName: String): Pair<Int, Int>? {
        val pattern = "w(\\d+)h(\\d+)\\.tif{1,2}\$".toRegex()
        val groups = pattern.matchEntire(fileName)?.groups
        return if (groups != null) Pair(groups[1]!!.value.toInt(), groups[2]!!.value.toInt()) else null
    }

}
