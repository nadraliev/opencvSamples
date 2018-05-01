package soutvoid.com.sudokusolver

import android.content.Context
import android.graphics.Bitmap
import com.orhanobut.logger.Logger
import org.opencv.core.*
import org.opencv.core.CvType.CV_8UC1
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.floodFill
import org.opencv.ml.Ml
import org.opencv.ml.SVM
import java.nio.ByteBuffer


class DigitRecognizer {

    companion object {
        const val MAX_NUM_IMAGES = 60000
    }

    private val knn = SVM.create()
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

        val intlables = MatOfInt(*labelsData.map { it.toInt() }.toIntArray())

        Core.transpose(tempLabels, trainingLabels)
        trainingLabels.convertTo(trainingLabels, CvType.CV_32FC1)
        labelsFis.close()

        knn.type = SVM.C_SVC
        knn.setKernel(SVM.LINEAR)
        knn.train(trainingImages, Ml.ROW_SAMPLE, intlables)
        Logger.d("trained")
        return true
    }

    fun classify(img: Mat, bitmaps: MutableList<Bitmap>): Int {
        val cloneImg = preprocessImage(img, bitmaps)
        cloneImg.convertTo(cloneImg, CvType.CV_32F)
//        val results = Mat(1, 1, CvType.CV_8U)
//        knn.findNearest(cloneImg, 10, results, Mat(), Mat())
//        return results.get(0, 0)[0].toInt()
        return knn.predict(cloneImg).toInt()
    }

    private fun preprocessImage(img: Mat, bitmaps: MutableList<Bitmap>): Mat {
        var rowTop = -1
        var rowBottom = -1
        var colLeft = -1
        var colRight = -1

        var temp = Mat()
        var thresholdBottom = 50
        var thresholdTop = 50
        var thresholdLeft = 50
        var thresholdRight = 50
        var center = img.rows() / 2
        for (i in center until img.rows()) {
            if (rowBottom == -1) {
                temp = img.row(i)
                if (Core.sumElems(temp).`val`[0] < thresholdBottom || i == img.rows() - 1)
                    rowBottom = i

            }

            if (rowTop == -1) {
                temp = img.row(img.rows() - i)
                if (Core.sumElems(temp).`val`[0] < thresholdTop || i == img.rows() - 1)
                    rowTop = img.rows() - i

            }

            if (colRight == -1) {
                temp = img.col(i)
                if (Core.sumElems(temp).`val`[0] < thresholdRight || i == img.cols() - 1)
                    colRight = i

            }

            if (colLeft == -1) {
                temp = img.col(img.cols() - i)
                if (Core.sumElems(temp).`val`[0] < thresholdLeft || i == img.cols() - 1)
                    colLeft = img.cols() - i
            }
        }

        var newImg = Mat.zeros(img.rows(), img.cols(), CV_8UC1);

        var startAtX = (newImg.cols() / 2) - (colRight - colLeft) / 2

        var startAtY = (newImg.rows() / 2) - (rowBottom - rowTop) / 2

        for (y in startAtY until (newImg.rows() / 2) + (rowBottom - rowTop) / 2) {
            for (x in startAtX until (newImg.cols() / 2) + (colRight - colLeft) / 2) {
                newImg.put(y, x, *img.get(rowTop + (y - startAtY), colLeft + (x - startAtX)))
            }
        }

        var cloneImg = Mat(numRows, numCols, CV_8UC1)

        Imgproc.resize(newImg, cloneImg, Size(numCols.toDouble(), numRows.toDouble()))

        var flooded = Mat.zeros(Size(cloneImg.cols() + 2.0, cloneImg.rows() + 2.0), CvType.CV_8UC1)

        // Now fill along the borders
        for (i in 0 until cloneImg.rows()) {
            floodFill(cloneImg, flooded, Point(.0, i.toDouble()), Scalar(.0, .0, .0))
            flooded = Mat.zeros(Size(cloneImg.cols() + 2.0, cloneImg.rows() + 2.0), CvType.CV_8UC1)

            floodFill(cloneImg, flooded, Point(cloneImg.cols().toDouble() - 1, i.toDouble()), Scalar(.0, .0, .0))
            flooded = Mat.zeros(Size(cloneImg.cols() + 2.0, cloneImg.rows() + 2.0), CvType.CV_8UC1)

            floodFill(cloneImg, flooded, Point(i.toDouble(), .0), Scalar(.0))
            flooded = Mat.zeros(Size(cloneImg.cols() + 2.0, cloneImg.rows() + 2.0), CvType.CV_8UC1)
            floodFill(cloneImg, flooded, Point(i.toDouble(), cloneImg.rows().toDouble() - 1), Scalar(.0))
        }

        bitmaps.add(cloneImg.toBitmap())
        cloneImg = cloneImg.reshape(1, 1)
        return cloneImg
    }
}