package com.viceboy.library

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

//Inspired from STACK_LAYOUT_MANAGER https://github.com/LittleMango/StackLayoutManager
class ShazamLayoutManager : RecyclerView.LayoutManager() {

    private var mFixScroll = false
    private var marginCalculated = false

    private var mScrollOffset: Int = Int.MAX_VALUE

    private var mItemOffset = 0
    private var mStartMargin = 0

    private var mDecoratedHeight = 0
    private var mDecoratedWidth = 0

    private var firstVisibleItem = 0

    private var mScrollDir: ScrollDir? = null
    private var mFlingOrientation: FlingOrientation? = null
    private var mItemSwipeListener: ItemSwipeListener? = null

    override fun canScrollHorizontally(): Boolean = false

    override fun canScrollVertically(): Boolean = true

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams =
        RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    override fun onAttachedToWindow(view: RecyclerView) {
        super.onAttachedToWindow(view)
        //Move view to bottom as per the fling orientation
        val flingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                mFlingOrientation = when {
                    velocityY < 0 -> FlingOrientation.TOP_TO_BOTTOM
                    velocityY > 0 -> FlingOrientation.BOTTOM_TO_TOP
                    else -> FlingOrientation.NONE
                }

                calculateAndScroll(view)

                return true
            }
        }
        view.onFlingListener = flingListener

        //Adding scroll listener to fix end scrolling effect
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mScrollDir = ScrollDir.NONE
                    mFixScroll = if (!mFixScroll && getMovePercent() != 0f) {
                        calculateAndScroll(recyclerView)
                        true
                    } else {
                        false
                    }
                }
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING)
                    mFixScroll = false
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                mScrollDir = when {
                    dy > 0 -> ScrollDir.BOTTOM_TO_TOP
                    dy < 0 -> ScrollDir.TOP_TO_BOTTOM
                    else -> ScrollDir.NONE
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        }
        view.addOnScrollListener(scrollListener)
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.isPreLayout || itemCount == 0) return

        removeAndRecycleAllViews(recycler)

        mScrollOffset = calculateValidOffset(mScrollOffset)

        layoutItems(recycler)
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        removeAndRecycleAllViews(recycler)

        val expectOffset = mScrollOffset + dy
        mScrollOffset = calculateValidOffset(expectOffset)

        val exactMove = mScrollOffset - expectOffset + dy

        layoutItems(recycler)

        return exactMove
    }

    private fun calculateAndScroll(view: RecyclerView) {
        val centerPosition = getTargetScrollPosition(getMovePercent())
        val targetOffset = getTargetOffset(centerPosition)
        view.smoothScrollBy(0, targetOffset)
    }

    private fun getTargetOffset(centerPosition: Int): Int {
        val offset = (itemCount - 1 - centerPosition) * height
        return offset - mScrollOffset
    }

    private fun getTargetScrollPosition(movePercent: Float): Int {
        return when (mFlingOrientation) {
            FlingOrientation.TOP_TO_BOTTOM -> if (movePercent > 0.2) firstVisibleItem + 1 else firstVisibleItem
            FlingOrientation.BOTTOM_TO_TOP -> firstVisibleItem
            else -> firstVisibleItem
        }
    }

    private fun calculateValidOffset(offset: Int): Int {
        return max(min((itemCount - 1) * height, offset), 0)
    }

    private fun layoutItems(recycler: RecyclerView.Recycler) {
        firstVisibleItem = getFirstVisibleItem()

        val movePercent = getMovePercent()

        val lastItem = getLastVisibleItem()

        val downToRange = calculateDownToRange(movePercent)

        val requiredColorPercent = 2 * movePercent

        val colorPosition =
            if (requiredColorPercent > 0.5f) firstVisibleItem + 1 else firstVisibleItem

        val colorChangePercent = calculateColorChangePercent(colorPosition, requiredColorPercent)


        for (index in lastItem downTo downToRange) {
            val targetView = recycler.getViewForPosition(index)

            addView(targetView)

            measureChild(targetView, 0, 0)

            layoutItem(targetView, index, movePercent)

            animateView(targetView, index, movePercent)
        }

        resetAnimationProperty(recycler)

        mItemSwipeListener?.onItemSwiped(colorPosition, colorChangePercent)
    }

    private fun calculateDownToRange(movePercent: Float): Int {
        val downToRange = max(firstVisibleItem - 1, 0)
        return if ((movePercent > 0.1 && mScrollDir == ScrollDir.TOP_TO_BOTTOM) || (mScrollDir == ScrollDir.BOTTOM_TO_TOP && movePercent > 0.1)) firstVisibleItem else downToRange
    }

    private fun getLastVisibleItem(): Int = min(firstVisibleItem + 2, itemCount - 1)

    private fun getFirstVisibleItem(): Int {
        return itemCount - 1 - (ceil(mScrollOffset.toFloat() / height * 1f)).toInt()
    }

    private fun getMovePercent(): Float {
        val movePercent = (height - mScrollOffset % height).toFloat() / height
        return if (movePercent == 1f) 0f else movePercent
    }

    private fun animateView(targetView: View, index: Int, movePercent: Float) {
        val actualIndex = index - firstVisibleItem
        val scaleFactor = if (minScale * actualIndex <= 0) 1f else
            min(
                max(
                    minScale / actualIndex,
                    0.6f
                ) + (movePercent / (actualIndex * 4f)) * actualIndex, 1f
            )
        val alphaFactor = if (minScale * actualIndex <= 0) 1f else
            min(
                min(actualIndex * movePercent - movePercent, 1f).toDouble()
                    .pow(actualIndex.toDouble()) + (1 / actualIndex).toFloat(), 1.0
            )

        targetView.scaleX = scaleFactor
        targetView.scaleY = scaleFactor
        targetView.alpha = alphaFactor.toFloat()
    }

    private fun layoutItem(
        targetView: View,
        currentIndex: Int,
        movePercent: Float
    ) {
        if (!marginCalculated) {
            mDecoratedWidth = getDecoratedMeasuredWidth(targetView)
            mDecoratedHeight = getDecoratedMeasuredHeight(targetView)
            mStartMargin = calculateStartMargin()
            mItemOffset = calculateItemOffset()
            marginCalculated = true
        }

        val frame = getFrame(currentIndex, movePercent)

        layoutDecorated(targetView, frame.left, frame.top, frame.right, frame.bottom)
    }

    private fun getFrame(currentIndex: Int, movePercent: Float): Rect {
        return Rect().apply {
            val top = getTopMargin(currentIndex, movePercent)

            set(mStartMargin, top, mStartMargin + mDecoratedWidth, top + mDecoratedHeight)
        }
    }

    private fun getTopMargin(index: Int, movePercent: Float): Int {
        val actualIndex = index - firstVisibleItem
        return when (index) {
            firstVisibleItem ->
                if (mScrollOffset % height == 0)
                    initialTopMargin
                else
                    initialTopMargin +
                            min(
                                (height - mScrollOffset % height),
                                (height - (mItemOffset / 2))
                            )
            firstVisibleItem - 1 -> initialTopMargin + (height - (mItemOffset / 2))
            else -> (initialTopMargin + actualIndex * mItemOffset) - (min(
                movePercent * 4,
                1f
            ) * mItemOffset).toInt()
        }
    }

    private fun calculateItemOffset(): Int {
        return ((mDecoratedHeight - (minScale * mDecoratedHeight).toInt()) * 0.6).toInt()
    }

    private fun calculateStartMargin(): Int {
        return (width - mDecoratedWidth) / 2
    }

    private fun calculateColorChangePercent(colorPosition: Int, movePercent: Float): Float =
        if (colorPosition == firstVisibleItem) min(1f - movePercent, 1f) else min(movePercent, 1f)

    private fun resetAnimationProperty(recycler: RecyclerView.Recycler) {
        if (firstVisibleItem > 0) {
            val view = recycler.getViewForPosition(firstVisibleItem - 1)
            view.scaleX = 1f
            view.scaleY = 1f
            view.alpha = 1f
        } else {
            val view = recycler.getViewForPosition(0)
            view.scaleX = 1f
            view.scaleY = 1f
            view.alpha = 1f
        }
    }


    fun setOnItemSwipeListener(listener: ItemSwipeListener) {
        mItemSwipeListener = listener
    }


    interface ItemSwipeListener {
        fun onItemSwiped(position: Int, movePercent: Float)
    }

    companion object {
        private const val minScale = 0.85f
        private const val initialTopMargin = 20

        private enum class FlingOrientation {
            NONE, TOP_TO_BOTTOM, BOTTOM_TO_TOP
        }

        private enum class ScrollDir {
            NONE, TOP_TO_BOTTOM, BOTTOM_TO_TOP
        }
    }
}