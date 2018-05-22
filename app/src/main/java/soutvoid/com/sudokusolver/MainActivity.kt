package soutvoid.com.sudokusolver

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.*


class MainActivity : AppCompatActivity() {

    val bitmaps = mutableListOf<Bitmap>()
    var currentImage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prev.onClick {
            if (currentImage != 0) {
                currentImage--
                showImage()
            }
        }

        next.onClick {
            if (currentImage < bitmaps.size - 1) {
                currentImage++
                showImage()
            }
        }

        loadImageBtn.onClick {
            val getIntent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
            }
            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
            val chooser = Intent.createChooser(getIntent, "Select picture").apply {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
            }
            startActivityForResult(chooser, 0)
        }
    }

    private fun showImage() {
        imageView2.setImageBitmap(bitmaps[currentImage])
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            data?.data?.let {
                bitmaps.clear()
                val inputStream = baseContext.contentResolver.openInputStream(it)
                val bmp = BitmapFactory.decodeStream(inputStream)
                bmp ?: return
                doOpenCvMagic(bmp)
            }
        }
    }

    fun doOpenCvMagic(bmp: Bitmap) {
        doUsingOpenCv {
            val img = Mat().apply { Utils.bitmapToMat(bmp, this) }
            val outerBox = Mat(img.size(), CvType.CV_8UC1)
            val original = img.clone()
            Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY)

            bitmaps.add(img.toBitmap())

            Imgproc.GaussianBlur(img, img, Size(11.0, 11.0), .0)

            bitmaps.add(img.toBitmap())

            Imgproc.adaptiveThreshold(img, outerBox, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2.0)

            bitmaps.add(outerBox.toBitmap())

            Core.bitwise_not(outerBox, outerBox)

            bitmaps.add(outerBox.toBitmap())

            val kernel = Mat(3, 3, CvType.CV_8UC1)
            val bytes = byteArrayOf(0, 1, 0, 1, 1, 1, 0, 1, 0)
            kernel.put(0, 0, bytes)
            Imgproc.dilate(outerBox, outerBox, kernel)

            bitmaps.add(outerBox.toBitmap())

            var maxArea = -1
            var point = Point(0.0, 0.0)

            var flooded = Mat.zeros(Size(outerBox.cols() + 2.0, outerBox.rows() + 2.0), CvType.CV_8UC1)
            for (y in 0..outerBox.size().height.toInt()) {
                for (x in 0..outerBox.size().width.toInt()) {
                    if (outerBox[y, x] != null && outerBox[y, x][0] >= 128) {
                        val area = Imgproc.floodFill(outerBox, flooded, Point(x.toDouble(), y.toDouble()), Scalar(64.0, .0, .0))
                        if (area > maxArea) {
                            maxArea = area
                            point = Point(x.toDouble(), y.toDouble())
                        }
                    }
                }
            }

            bitmaps.add(outerBox.toBitmap())

            flooded = Mat.zeros(Size(outerBox.cols() + 2.0, outerBox.rows() + 2.0), CvType.CV_8UC1)
            Imgproc.floodFill(outerBox, flooded, point, Scalar.all(255.0))

            bitmaps.add(outerBox.toBitmap())

            flooded = Mat.zeros(Size(outerBox.cols() + 2.0, outerBox.rows() + 2.0), CvType.CV_8UC1)
            for (y in 0..outerBox.size().height.toInt()) {
                for (x in 0..outerBox.size().width.toInt()) {
                    if (outerBox[y, x] != null && outerBox[y, x][0] == 64.0 && x.toDouble() != point.x && y.toDouble() != point.y) {
                        Imgproc.floodFill(outerBox, flooded, Point(x.toDouble(), y.toDouble()), Scalar(.0, .0, .0))
                    }
                }
            }

            bitmaps.add(outerBox.toBitmap())

            Imgproc.erode(outerBox, outerBox, kernel)

            bitmaps.add(outerBox.toBitmap())
            val outerBox2 = outerBox.clone()

            val linesMat = Mat()

            Imgproc.HoughLines(outerBox, linesMat, 1.0, Math.PI / 180, 200)

            val lines = (0..linesMat.rows()).mapNotNull { linesMat.get(it, 0) }

            mergeRelatedLines(lines, outerBox)

            lines.filterNot { it[0] == .0 && it[1] == -100.0 }.forEach {
                drawLine(it, outerBox, Scalar(128.0, 0.0, 0.0))
            }

            bitmaps.add(outerBox.toBitmap())

            var topEdge = doubleArrayOf(1000.0, 1000.0)
            var bottomEdge = doubleArrayOf(-1000.0, -1000.0)
            var leftEdge = doubleArrayOf(1000.0, 1000.0)
            var rightEdge = doubleArrayOf(-1000.0, -1000.0)
            var topYIntercept = 100000.0
            var toxXIntercept = .0
            var bottomYIntercept = .0
            var bottomXIntercept = .0
            var leftXIntercept = 100000.0
            var leftYIntercept = .0
            var rightXIntercept = .0
            var rightYIntercept = .0

            for (i in 0 until lines.size) {
                val current = lines[i]
                val p = current[0]
                val theta = current[1]

                if (p == .0 && theta == -100.0) continue

                var xIntercept = p / cos(theta)
                var yIntercept = p / (cos(theta) * sin(theta))

                if (theta > Math.PI * 80 / 180 && theta < Math.PI * 100 / 180) {
                    if (p < topEdge[0])
                        topEdge = current.clone()
                    if (p > bottomEdge[0])
                        bottomEdge = current.clone()
                } else if (theta < Math.PI * 10 / 180 || theta > Math.PI * 170 / 180) {
                    if (xIntercept > rightXIntercept) {
                        rightEdge = current.clone()
                        rightXIntercept = xIntercept
                    } else if (xIntercept <= leftXIntercept) {
                        leftEdge = current.clone()
                        leftXIntercept = xIntercept
                    }
                }
            }

            drawLine(leftEdge, outerBox2, Scalar(128.0, 0.0, 0.0))
            drawLine(rightEdge, outerBox2, Scalar(128.0, 0.0, 0.0))
            drawLine(topEdge, outerBox2, Scalar(128.0, 0.0, 0.0))
            drawLine(bottomEdge, outerBox2, Scalar(128.0, 0.0, 0.0))
            bitmaps.add(outerBox2.toBitmap())

            val left1 = Point()
            val left2 = Point()
            val right1 = Point()
            val right2 = Point()
            val bottom1 = Point()
            val bottom2 = Point()
            val top1 = Point()
            val top2 = Point()

            val height = outerBox.size().height
            val width = outerBox.size().width

            if (leftEdge[1] != .0) {
                left1.x = .0
                left1.y = leftEdge[0] / sin(leftEdge[1])
                left2.x = width
                left2.y = -left2.x / tan(leftEdge[1]) + left1.x
            } else {
                left1.y = .0
                left1.x = leftEdge[0] / cos(leftEdge[1])
                left2.y = height
                left2.x = left1.x - height * tan(leftEdge[1])
            }

            if (rightEdge[1] != .0) {
                right1.x = .0
                right1.y = rightEdge[0] / sin(rightEdge[1])
                right2.x = width
                right2.y = -right2.x / tan(rightEdge[1]) + right1.y
            } else {
                right1.y = .0
                right1.x = rightEdge[0] / cos(rightEdge[1])
                right2.y = height
                right2.x = right1.x - height * tan(rightEdge[1])
            }

            bottom1.x = .0
            bottom1.y = bottomEdge[0] / sin(bottomEdge[1])
            bottom2.x = width
            bottom2.y = -bottom2.x / tan(bottomEdge[1]) + bottom1.y

            top1.x = .0
            top1.y = topEdge[0] / sin(topEdge[1])
            top2.x = width
            top2.y = -top2.x / tan(topEdge[1]) + top1.y

            val leftA = left2.y - left1.y
            val leftB = left1.x - left2.x

            val leftC = leftA * left1.x + leftB * left1.y

            val rightA = right2.y - right1.y
            val rightB = right1.x - right2.x

            val rightC = rightA * right1.x + rightB * right1.y

            val topA = top2.y - top1.y
            val topB = top1.x - top2.x

            val topC = topA * top1.x + topB * top1.y

            val bottomA = bottom2.y - bottom1.y
            val bottomB = bottom1.x - bottom2.x

            val bottomC = bottomA * bottom1.x + bottomB * bottom1.y

            val detTopLeft = leftA * topB - leftB * topA

            val ptTopLeft = Point((topB * leftC - leftB * topC) / detTopLeft, (leftA * topC - topA * leftC) / detTopLeft)

            val detTopRight = rightA * topB - rightB * topA

            val ptTopRight = Point((topB * rightC - rightB * topC) / detTopRight, (rightA * topC - topA * rightC) / detTopRight)


            val detBottomRight = rightA * bottomB - rightB * bottomA
            val ptBottomRight = Point((bottomB * rightC - rightB * bottomC) / detBottomRight, (rightA * bottomC - bottomA * rightC) / detBottomRight)
            val detBottomLeft = leftA * bottomB - leftB * bottomA
            val ptBottomLeft = Point((bottomB * leftC - leftB * bottomC) / detBottomLeft, (leftA * bottomC - bottomA * leftC) / detBottomLeft)

            var maxLength = (ptBottomLeft.x - ptBottomRight.x) * (ptBottomLeft.x - ptBottomRight.x) + (ptBottomLeft.y - ptBottomRight.y) * (ptBottomLeft.y - ptBottomRight.y)
            var temp = (ptTopRight.x - ptBottomRight.x) * (ptTopRight.x - ptBottomRight.x) + (ptTopRight.y - ptBottomRight.y) * (ptTopRight.y - ptBottomRight.y)

            if (temp > maxLength) maxLength = temp

            temp = (ptTopRight.x - ptTopLeft.x) * (ptTopRight.x - ptTopLeft.x) + (ptTopRight.y - ptTopLeft.y) * (ptTopRight.y - ptTopLeft.y)

            if (temp > maxLength) maxLength = temp

            temp = (ptBottomLeft.x - ptTopLeft.x) * (ptBottomLeft.x - ptTopLeft.x) + (ptBottomLeft.y - ptTopLeft.y) * (ptBottomLeft.y - ptTopLeft.y)

            if (temp > maxLength) maxLength = temp

            maxLength = sqrt(maxLength)

            val src = MatOfPoint2f(ptTopLeft, ptTopRight, ptBottomRight, ptBottomLeft)
            val dst = MatOfPoint2f(Point(.0, .0), Point(maxLength - 1, .0), Point(maxLength - 1, maxLength - 1), Point(.0, maxLength - 1))

            val threshed = original.clone()
            Imgproc.cvtColor(threshed, threshed, Imgproc.COLOR_RGB2GRAY)
            threshed.convertTo(threshed, CvType.CV_8UC1)
            Imgproc.adaptiveThreshold(threshed, threshed, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 101, 1.0)

            bitmaps.add(threshed.toBitmap())


            //delete grid here
            maxArea = -1
            point = Point(0.0, 0.0)

            val threshedClone = threshed.clone()
            flooded = Mat.zeros(Size(threshedClone.cols() + 2.0, threshedClone.rows() + 2.0), CvType.CV_8UC1)
            for (y in 0..threshedClone.size().height.toInt()) {
                for (x in 0..threshedClone.size().width.toInt()) {
                    if (threshedClone[y, x] != null && threshedClone[y, x][0] >= 128) {
                        val area = Imgproc.floodFill(threshedClone, flooded, Point(x.toDouble(), y.toDouble()), Scalar(64.0, .0, .0))
                        if (area > maxArea) {
                            maxArea = area
                            point = Point(x.toDouble(), y.toDouble())
                        }
                    }
                }
            }
            flooded = Mat.zeros(Size(threshedClone.cols() + 2.0, threshedClone.rows() + 2.0), CvType.CV_8UC1)
            Imgproc.floodFill(threshed, flooded, point, Scalar.all(0.0))
            bitmaps.add(threshed.toBitmap())

            val undistortedThreshed = Mat(Size(maxLength, maxLength), CvType.CV_8UC1)
            Imgproc.warpPerspective(threshed, undistortedThreshed, Imgproc.getPerspectiveTransform(src, dst), Size(maxLength, maxLength))

            bitmaps.add(undistortedThreshed.toBitmap())

            val recognizer = DigitRecognizer()
            recognizer.train(this, bitmaps)
            //recognizer.train("train-images-idx3-ubyte", "train-labels-idx1-ubyte", this)

            var dist = ceil(maxLength / 9).toInt()
            val currentCell = Mat(dist, dist, CvType.CV_8UC1)
//            val stringBuilder = StringBuilder()
//            stringBuilder.append("-------------------------------\n")

            val sudokuMatrix = Array(9) { Array(9) { 0 } }

            for (j in 0 until 9) {
//                stringBuilder.append("|")
                for (i in 0 until 9) {
                    for (y in 0 until dist) {
                        if (j * dist + y >= undistortedThreshed.cols()) break
                        for (x in 0 until dist) {
                            if (i * dist + x >= undistortedThreshed.rows()) break
                            currentCell.put(y, x, *undistortedThreshed.get(j * dist + y, i * dist + x))
                        }
                    }

                    val area = currentCell.countWhitePixels()
                    if (area > currentCell.rows() * currentCell.cols() / 30) {
                        val number = recognizer.classify(currentCell, bitmaps, this)
                        sudokuMatrix[j][i] = number
//                        stringBuilder.append(" $number ")
                    } else {
//                        stringBuilder.append(" - ")
                    }
//                    if ((i + 1) % 3 == 0)
//                        stringBuilder.append("|")
                }
//                stringBuilder.append("\n")
//                if ((j + 1) % 3 == 0 && j != 9 - 1)
//                    stringBuilder.append("|-----------------------------|\n")
            }

//            stringBuilder.append("-------------------------------\n")
//            Logger.d("sudoku\n$stringBuilder")
            printSudoku(sudokuMatrix)
            SudokuSolver().solve(0, 0, sudokuMatrix)
            printSudoku(sudokuMatrix)

            sudokuResult.visibility = View.VISIBLE
            sudokuResult.addView(SudokuView(this, sudokuMatrix.map { it.toIntArray() }.toTypedArray()))

            showImage()
        }
    }

    private fun printSudoku(sudokuMatrix: Array<Array<Int>>) {
        val stringBuilder = StringBuilder()
        stringBuilder.append("-------------------------------\n")
        for (j in 0 until 9) {
            stringBuilder.append("|")
            for (i in 0 until 9) {
                if (sudokuMatrix[j][i] != 0) {
                    stringBuilder.append(" ${sudokuMatrix[j][i]} ")
                } else {
                    stringBuilder.append(" - ")
                }
                if ((i + 1) % 3 == 0)
                    stringBuilder.append("|")
            }
            stringBuilder.append("\n")
            if ((j + 1) % 3 == 0 && j != 9 - 1)
                stringBuilder.append("|-----------------------------|\n")
        }
        stringBuilder.append("-------------------------------\n")
        println(stringBuilder)
    }

    private fun mergeRelatedLines(lines: List<DoubleArray>, img: Mat) {
        for (line in lines) {
            if (line[0] == .0 && line[1] == -100.0) continue
            val p1 = line[0]
            val theta1 = line[1]
            val pt1Current = Point()
            val pt2Current = Point()
            if (theta1 > Math.PI * 45 / 180 && theta1 < Math.PI * 135 / 180) {
                pt1Current.x = .0
                pt1Current.y = p1 / sin(theta1)

                pt2Current.x = img.size().width
                pt2Current.y = -pt2Current.x / tan(theta1) + p1 / sin(theta1)
            } else {
                pt1Current.y = .0
                pt1Current.x = p1 / cos(theta1)

                pt2Current.y = img.size().height
                pt2Current.x = -pt2Current.y / tan(theta1) + p1 / cos(theta1)
            }

            for (secondLine in lines) {
                if (secondLine == line) continue
                if (abs(secondLine[0] - line[0]) < 20 && abs(secondLine[1] - line[1]) < Math.PI * 10 / 180) {
                    var p = secondLine[0]
                    var theta = secondLine[1]
                    val pt1 = Point()
                    val pt2 = Point()
                    if (secondLine[1] > Math.PI * 45 / 180 && secondLine[1] < Math.PI * 135 / 180) {
                        pt1.x = .0
                        pt1.y = p / sin(theta)

                        pt2.x = img.size().width
                        pt2.y = -pt2.x / tan(theta) + p / sin(theta)
                    } else {
                        pt1.y = .0
                        pt1.x = p / cos(theta)
                        pt2.y = img.size().height
                        pt2.x = -pt2.y / tan(theta) + p / cos(theta)
                    }

                    if (Math.pow(pt1.x - pt1Current.x, 2.0) + Math.pow(pt1.y - pt1Current.y, 2.0) < 64 * 64
                            && Math.pow(pt2.x - pt2Current.x, 2.0) + Math.pow(pt2.y - pt2Current.y, 2.0) < 64 * 64) {
                        //merge the two
                        line[0] = (line[0] + secondLine[0]) / 2
                        line[1] = (line[1] + secondLine[1]) / 2

                        secondLine[0] = .0
                        secondLine[1] = -100.0
                    }
                }
            }
        }
    }

    private fun drawLine(line: DoubleArray, img: Mat, rgb: Scalar) {
        if (line[1] != 0.0) {
            val m = -1 / tan(line[1])
            val c = line[0] / sin(line[1])

            Imgproc.line(img, Point(.0, c), Point(img.size().width, m * img.size().width + c), rgb)
        } else {
            Imgproc.line(img, Point(line[0], .0), Point(line[0], img.size().height), rgb)
        }
    }

    override fun onPause() {
        unloadOpenCv()
        super.onPause()
    }

}
