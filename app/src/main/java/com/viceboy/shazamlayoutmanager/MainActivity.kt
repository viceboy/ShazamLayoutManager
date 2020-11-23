package com.viceboy.shazamlayoutmanager

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import com.viceboy.library.ShazamLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val listOfItem = arrayListOf(
            DataModel(R.drawable.img_env_green, R.color.grassGreen),
            DataModel(R.drawable.img_wolf_blue, R.color.skyBlue),
            DataModel(R.drawable.img_green_wild, R.color.green),
            DataModel(R.drawable.img_red, R.color.red),
            DataModel(R.drawable.img_grey, R.color.lightGrey)
        )
        rvShazam.adapter = DataAdapter(listOfItem)
        rvShazam.layoutManager = ShazamLayoutManager().apply {
            setOnItemSwipeListener(
                object : ShazamLayoutManager.ItemSwipeListener {
                    override fun onItemSwiped(position: Int, movePercent: Float) {
                        window.statusBarColor = Utils.darkenColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                listOfItem[position].colorTint
                            ), 0.4f
                        )
                        rootLayout?.setBackgroundColor(
                            Utils.darkenColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    listOfItem[position].colorTint
                                ), movePercent
                            )
                        )

                    }
                }
            )
        }
    }
}

