package soutvoid.com.sudokusolver

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.sin
import kotlin.math.tan

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
            Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY)

            bitmaps.add(img.toBitmap())

            Imgproc.GaussianBlur(img, img, Size(11.0, 11.0), .0)

            bitmaps.add(img.toBitmap())

            Imgproc.adaptiveThreshold(img, img, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2.0)

            bitmaps.add(img.toBitmap())

            Core.bitwise_not(img, img)

            bitmaps.add(img.toBitmap())

            val kernel = Mat(3, 3, CvType.CV_8UC1)
            val bytes = byteArrayOf(0, 1, 0, 1, 1, 1, 0, 1, 0)
            kernel.put(0, 0, bytes)
            Imgproc.dilate(img, img, kernel)

            bitmaps.add(img.toBitmap())

            var maxArea = -1
            var point = Point(0.0, 0.0)

            var flooded = Mat.zeros(Size(img.cols() + 2.0, img.rows() + 2.0), CvType.CV_8UC1)
            for (y in 0..img.size().height.toInt()) {
                for (x in 0..img.size().width.toInt()) {
                    if (img[y, x] != null && img[y, x][0] >= 128) {
                        val area = Imgproc.floodFill(img, flooded, Point(x.toDouble(), y.toDouble()), Scalar(64.0, .0, .0))
                        if (area > maxArea) {
                            maxArea = area
                            point = Point(x.toDouble(), y.toDouble())
                        }
                    }
                }
            }

            bitmaps.add(img.toBitmap())

            flooded = Mat.zeros(Size(img.cols() + 2.0, img.rows() + 2.0), CvType.CV_8UC1)
            Imgproc.floodFill(img, flooded, point, Scalar.all(255.0))

            bitmaps.add(img.toBitmap())

            flooded = Mat.zeros(Size(img.cols() + 2.0, img.rows() + 2.0), CvType.CV_8UC1)
            for (y in 0..img.size().height.toInt()) {
                for (x in 0..img.size().width.toInt()) {
                    if (img[y, x] != null && img[y, x][0] == 64.0 && x.toDouble() != point.x && y.toDouble() != point.y) {
                        Imgproc.floodFill(img, flooded, Point(x.toDouble(), y.toDouble()), Scalar(.0, .0, .0))
                    }
                }
            }

            bitmaps.add(img.toBitmap())

            Imgproc.erode(img, img, kernel)

            bitmaps.add(img.toBitmap())

            val lines = Mat()

            Imgproc.HoughLines(img, lines, 1.0, Math.PI / 180, 200)

            (0..lines.rows()).mapNotNull { lines.get(it, 0) }.forEach {
                drawLine(it, img, Scalar(128.0, 0.0, 0.0))
            }

            bitmaps.add(img.toBitmap())

            showImage()
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
