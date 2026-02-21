import io.github.rwpp.game.base.GamePaint
import io.github.rwpp.game.base.Rect


interface GameCanvas {
    fun drawText(text: String, x: Float, y: Float, paint: GamePaint)

    fun drawRect(rect: Rect, paint: GamePaint)

    fun drawCircle(x: Float, y: Float, radius: Float, paint: GamePaint)

    fun drawLine(startX: Float, startY: Float, endX: Float, endY: Float, paint: GamePaint)

    fun scale(scaleX: Float, scaleY: Float)
}