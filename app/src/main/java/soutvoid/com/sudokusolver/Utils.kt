package soutvoid.com.sudokusolver

import android.graphics.Bitmap
import android.graphics.PointF
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point

fun Mat.toBitmap(): Bitmap =
        Bitmap.createBitmap(width(), height(), Bitmap.Config.ARGB_4444)
                .also { Utils.matToBitmap(this, it) }

fun pow(a: Float, b: Float): Float =
        Math.pow(a.toDouble(), b.toDouble()).toFloat()

fun sqrt(number: Float): Float =
        Math.sqrt(number.toDouble()).toFloat()

fun length(a: PointF, b: PointF): Float =
        sqrt(pow(a.x - b.x, 2f) + pow(a.y - b.y, 2f))

fun PointF.toCvPoint(): Point =
        Point(x.toDouble(), y.toDouble())

fun Point.toFloatArray(): FloatArray = floatArrayOf(x.toFloat(), y.toFloat())