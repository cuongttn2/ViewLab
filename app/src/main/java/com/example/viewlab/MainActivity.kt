package com.example.viewlab

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.viewlab.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var _alpha = 0f
    private var _color = 0
    private var _hue = 0f
    private var _argbColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.colorPicker.alphaSliderView = binding.colorAlphaSlider
        binding.colorPicker.hueSliderView = binding.hueSlider

        binding.colorPicker.color = Color.rgb(81,81,81)

        binding.preview.setBackgroundColor(binding.colorPicker.color)

//        listenColorChanged()
        listenColorChangeEnd()


    }

    private fun listenColorChangeEnd() {
        // KavehColorPicker
        binding.colorPicker.setOnColorChangeEndListener { color ->
            Log.d("DEBUG_2", "color end: $color")
            _color = color

            binding.preview.setBackgroundColor(binding.colorPicker.color)

        }

        // KavehHueSlider
        binding.hueSlider.setOnHueChangeEndListener { hue, argbColor ->
            // Hue value is between [0..360]
            // argbColor is just the color int representation of hue value with full brightness and saturation.
            Log.d("DEBUG_2", "hue end: $hue\n argbColor end: $argbColor")
            _hue = hue
            _argbColor = argbColor

            binding.preview.setBackgroundColor(binding.colorPicker.color)

        }
        // KavehColorAlphaSlider
        binding.colorAlphaSlider.setOnAlphaChangeEndListener { alpha ->
            // Alpha value between [0..1]
            Log.d("DEBUG_2", "alpha end: $alpha")
            _alpha = alpha

            binding.preview.setBackgroundColor(binding.colorPicker.color)

        }

    }

    private fun listenColorChanged() {
        // KavehColorPicker
        binding.colorPicker.setOnColorChangedListener { color ->
            Log.d("DEBUG_1", "color changed: $color")
            _color = color

            binding.preview.setBackgroundColor(binding.colorPicker.color)
        }
/*
        // KavehHueSlider
        binding.hueSlider.setOnHueChangedListener { hue, argbColor ->
            // Hue value is between [0..360]
            // argbColor is just the color int representation of hue value with full brightness and saturation.
            Log.d("DEBUG_1", "hue changed: $hue\n argbColor changed: $argbColor")
            _hue = hue
            _argbColor = argbColor

            binding.preview.setBackgroundColor(binding.colorPicker.color)
        }
        // KavehColorAlphaSlider
        binding.colorAlphaSlider.setOnAlphaChangedListener { alpha ->
            // Alpha value between [0..1]
            Log.d("DEBUG_1", "alpha changed: $alpha")
            _alpha = alpha

            binding.preview.setBackgroundColor(binding.colorPicker.color)
        }
*/

    }

}