# ShazamLayoutManager
A custom RecyclerView Layout Manager similar to layout manager implemented in Shazam app.
Reference:https://medium.com/@veniosg/buttery-smooth-animations-on-a-stacking-recyclerview-17f6ef8df1c3

## Demo
<p float="left">
  <img src="demo/demo.gif" width="250" />
</p>


## Install
```gradle
dependencies {
  implementation 'com.viceboy.library:shazamlayoutmanager:1.0.0'
}
```


## Usage



### Kotlin

### Basic Usage
```kotlin

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
```

#### Credit
[StackLayoutManager](https://github.com/amyu/StackCardLayoutManager) 

## License

```
   Copyright 2020 Sumit Jha

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
