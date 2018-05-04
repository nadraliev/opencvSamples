package soutvoid.com.sudokusolver

import android.graphics.Bitmap
import android.graphics.PointF
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

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

fun File.readBytes(): ByteArray {
    val size = length().toInt()
    val result = ByteArray(size)
    val buff = ByteArray(size)
    FileInputStream(this).use {
        var read = it.read(result, 0, size)
        if (read < size) {
            var remain = size - read
            while (remain > 0) {
                read = it.read(buff, 0, remain)
                System.arraycopy(buff, 0, result, size - remain, read)
                remain -= read
            }
        }
    }
    return result
}

fun InputStream.readBytes(): ByteArray =
        toString().toByteArray()


fun ByteArray.toInt(): Int {
    val first = this[3].toInt()
    val second = this[2].toInt() shl 8
    val third = this[1].toInt() shl 16
    val fourth = this[0].toInt() shl 24
    return first or second or third or fourth
}

fun Mat.countWhitePixels(): Int {
    var result = 0
    for (y in 0 until rows())
        for (x in 0 until cols())
            if (get(y, x) != null && get(y, x).isNotEmpty() && get(y, x)[0] == 255.0)
                result++
    return result
}

fun Bitmap.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 100, stream)
    val result = stream.toByteArray()
    stream.close()
    return result
}