package soutvoid.com.sudokusolver

import android.content.Context
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.ml.KNearest
import org.opencv.ml.Ml
import java.nio.ByteBuffer

class DigitRecognizer {

    companion object {
        const val MAX_NUM_IMAGES = 60000
    }

    private val knn = KNearest.create()
    public var numRows = 0
    public var numCols = 0
    public var numImages = 0

    fun train(trainPath: String, labelsPath: String, context: Context): Boolean {
        val trainFis = context.assets.open(trainPath)
        val labelsFis = context.assets.open(labelsPath)

        //Read bytes in flipped order
        val header = ByteArray(16)
        trainFis.read(header, 0, 16)
        val byteByffer = ByteBuffer.wrap(header, 4, 12)
        numImages = byteByffer.int
        numRows = byteByffer.int
        numCols = byteByffer.int

        labelsFis.skip(8) //skip two integers (magic number and number of labels

        if (numImages > MAX_NUM_IMAGES) numImages = MAX_NUM_IMAGES

        val size = numRows * numCols

        val trainingImages = Mat(numImages, size, CvType.CV_8U)

        for (i in 0 until numImages) {
            val image = ByteArray(size)
            trainFis.read(image, 0, size)
            trainingImages.put(i, 0, image)
        }
        trainingImages.convertTo(trainingImages, CvType.CV_32FC1)
        trainFis.close()

        val labelsData = ByteArray(numImages)
        val trainingLabels = Mat(numImages, 1, CvType.CV_8U)
        val tempLabels = Mat(1, numImages, CvType.CV_8U)
        labelsFis.read(labelsData, 0, numImages)
        tempLabels.put(0, 0, labelsData)

        Core.transpose(tempLabels, trainingLabels)
        trainingLabels.convertTo(trainingLabels, CvType.CV_32FC1)
        labelsFis.close()

        knn.train(trainingImages, Ml.ROW_SAMPLE, trainingLabels)
        return true
    }

    fun classify(img: Mat): Int {
        val cloneImg = img
        val results = Mat(1, 1, CvType.CV_8U)
        knn.findNearest(cloneImg, 10, results, Mat(), Mat())
        return results.get(0, 0)[0].toInt()
    }

    private fun preprocessImage(img: Mat): Mat {
        return Mat()
    }
}