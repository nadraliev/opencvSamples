package soutvoid.com.sudokusolver

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import com.orhanobut.logger.Logger
import org.jetbrains.anko.image

/**
 * Created by andrew on 18.03.18.
 */
class ImageCropper : ImageView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val paint = Paint()
    var coords: MutableList<PointF> = mutableListOf()
    var draggingPointIndex = -1

    init {
        paint.strokeWidth = 4f
        paint.color = Color.RED
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null)
            return false
        val index = event.actionIndex
//        val coords: FloatArray = floatArrayOf(event.getX(index), event.getY(index))
//        findPointerCoords(firstChild { it is ImageView } as ImageView, coords)
//        invalidate()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                draggingPointIndex = coords
                        .indexOfFirst {
                            pow(it.x - event.getX(index), 2f)
                            +pow(it.y - event.getY(index), 2f) <= pow(30f, 2f)
                        }
            }
            MotionEvent.ACTION_MOVE -> {
                Logger.d("index $draggingPointIndex")
                if (draggingPointIndex != -1) {
                    val pointF = PointF(event.getX(index), event.getY(index))
                    coords[draggingPointIndex] = pointF
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> draggingPointIndex = -1
        }

        return true
    }


    fun findImageCoords(initPointF: PointF) {
        val matrix = Matrix()
        imageMatrix.invert(matrix)
        val coords = floatArrayOf(initPointF.x, initPointF.y)
        matrix.mapPoints(coords)
        initPointF.set(coords[0], coords[1])
    }

    fun findLayoutCoords(initPointF: PointF) {
        val initCoords = floatArrayOf(initPointF.x, initPointF.y)
        imageMatrix.mapPoints(initCoords)
        initPointF.set(initCoords[0], initCoords[1])
    }

    fun init() {
        if (image == null)
            return
        val bitmap = (image as BitmapDrawable).bitmap
        coords.clear()
        coords.add(PointF(0f, 0f))
        coords.add(PointF(bitmap.width.toFloat(), 0f))
        coords.add(PointF(bitmap.width.toFloat(), bitmap.height.toFloat()))
        coords.add(PointF(0f, bitmap.height.toFloat()))
        coords.forEach { findLayoutCoords(it) }
        invalidate()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        init()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        init()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        init()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        init()
    }

    fun getCroppedCoords(): List<PointF> =
            coords.apply { forEach { findImageCoords(it) } }

    fun getBitmap(): Bitmap? =
            (image as BitmapDrawable?)?.bitmap

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (coords.size < 4)
            return

        canvas?.drawLine(coords[0].x, coords[0].y, coords[1].x, coords[1].y, paint)
        canvas?.drawLine(coords[1].x, coords[1].y, coords[2].x, coords[2].y, paint)
        canvas?.drawLine(coords[2].x, coords[2].y, coords[3].x, coords[3].y, paint)
        canvas?.drawLine(coords[3].x, coords[3].y, coords[0].x, coords[0].y, paint)

        coords.forEach {
            canvas?.drawCircle(it.x, it.y, 30f, paint)
        }
    }
}