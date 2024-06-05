package ru.russianpost.digitalperiodicals.features.reader

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.compose.runtime.MutableState
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.ScrollHandle
import com.github.barteksc.pdfviewer.util.Util

class CustomScrollHandler @JvmOverloads constructor(
    screenContext: Context,
    private val isVisible: MutableState<Boolean>,
    private val inverted: Boolean = false,
    private val progressChangeListener: (page: Int) -> Unit,
) : RelativeLayout(screenContext), ScrollHandle {
    protected var textView: TextView = TextView(screenContext)
    protected var seekbar: SeekBar = SeekBar(context)
    private var pdfView: PDFView? = null

    @get:JvmName("getCustomHandler")
    private val handler = Handler()
    private var swipeLock = false
    private var scrollLock = false
    override fun setupLayout(
        pdfView: PDFView,
    ) {
        val align: Int
        val width: Int
        val height: Int
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar, p1: Int, p2: Boolean) {
                if (!swipeLock) {
                    progressChangeListener(p0.progress/MULTIPLICATOR)
                }

            }

            override fun onStartTrackingTouch(p0: SeekBar) {
                scrollLock = true
            }

            override fun onStopTrackingTouch(p0: SeekBar) {
                handler.postDelayed({ scrollLock = false }, 1000)
            }

        })
        seekbar.max = (pdfView.pageCount - 1) * MULTIPLICATOR

        // determine handler position, default is right (when scrolling vertically) or bottom (when scrolling horizontally)
        if (pdfView.isSwipeVertical) {
            width = HANDLE_LONG
            height = HANDLE_SHORT
            if (inverted) { // left
                align = ALIGN_PARENT_LEFT
            } else { // right
                align = ALIGN_PARENT_RIGHT
            }
        } else {
            width = HANDLE_SHORT
            height = HANDLE_LONG
            if (inverted) { // top
                align = ALIGN_PARENT_TOP
            } else { // bottom
                align = ALIGN_PARENT_BOTTOM
            }
        }
        val lp = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, Util.getDP(context, height)
        )
        lp.setMargins(0, 0, 0, 0)
        val tvlp = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        addView(textView, tvlp)
        val sblp = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        addView(seekbar, sblp)
        lp.addRule(align)
        sblp.addRule(ALIGN_PARENT_TOP)
        sblp.setMargins(0, 50, 0, 0)
        tvlp.addRule(ALIGN_PARENT_BOTTOM)
        tvlp.addRule(CENTER_HORIZONTAL)
        tvlp.setMargins(0, 0, 0, 20)
        pdfView.addView(this, lp)
        this.pdfView = pdfView
    }

    override fun destroyLayout() {
        pdfView!!.removeView(this)
    }

    override fun setScroll(position: Float) {

        if (isVisible.value) {
            show()
        } else hide()

        if (pdfView != null && !scrollLock) {
            swipeLock = true
            seekbar.progress = (position * pdfView!!.pageCount * MULTIPLICATOR).toInt()
            swipeLock = false
        }
    }

    override fun hideDelayed() {

    }

    override fun setPageNum(pageNum: Int) {
        val text = if (scrollLock) {
            (seekbar.progress / MULTIPLICATOR + 1).toString()
        } else pageNum.toString()
        if (textView.text != text) {
            textView.text = text
        }
    }

    override fun shown(): Boolean {
        return visibility == VISIBLE
    }

    override fun show() {
        visibility = VISIBLE
    }

    override fun hide() {
        visibility = INVISIBLE
    }

    fun setTextColor(color: Int) {
        textView.setTextColor(color)
    }

    /**
     * @param size text size in dp
     */
    fun setTextSize(size: Int) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size.toFloat())
    }

    companion object {
        private const val HANDLE_LONG = 65
        private const val HANDLE_SHORT = 40
        private const val DEFAULT_TEXT_SIZE = 16
        private const val MULTIPLICATOR = 1000
    }

    init {
        visibility = RelativeLayout.INVISIBLE
        setTextColor(Color.BLACK)
        setTextSize(CustomScrollHandler.DEFAULT_TEXT_SIZE)
    }
}