package com.example.gestureapp

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat

class MainActivity : AppCompatActivity(), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var gestureText: TextView
    private var doubleTapCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gestureText = findViewById(R.id.gesture_text)
        gestureDetector = GestureDetectorCompat(this, this)
        gestureDetector.setOnDoubleTapListener(this)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean = true

    override fun onShowPress(e: MotionEvent) {}

    override fun onSingleTapUp(e: MotionEvent): Boolean = true

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = true

    override fun onLongPress(e: MotionEvent) {}

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (e1 != null) {
            val diffX = e2.x - e1.x
            if (diffX > 0) {
                Toast.makeText(this, "Right Swipe", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Left Swipe", Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean = true

    override fun onDoubleTap(e: MotionEvent): Boolean {
        if (doubleTapCount == 0) {
            gestureText.visibility = View.VISIBLE
            doubleTapCount++
        } else {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean = true
}