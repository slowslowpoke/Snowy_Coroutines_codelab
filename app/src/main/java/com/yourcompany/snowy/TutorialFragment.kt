/*
 * Copyright (c) 2023 Kodeco Inc.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 *  distribute, sublicense, create a derivative work, and/or sell copies of the
 *  Software in any work that is designed, intended, or marketed for pedagogical or
 *  instructional purposes related to programming, coding, application development,
 *  or information technology.  Permission for such use, copying, modification,
 *  merger, publication, distribution, sublicensing, creation of derivative works,
 *  or sale is expressly withheld.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package com.yourcompany.snowy

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.yourcompany.snowy.databinding.FragmentTutorialBinding
import com.yourcompany.snowy.model.Tutorial
import com.yourcompany.snowy.utils.SnowFilter
import kotlinx.coroutines.*
import java.net.URL

class TutorialFragment : Fragment(R.layout.fragment_tutorial) {
    private var binding: FragmentTutorialBinding? = null


    private val coroutineExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            showError("CoroutineExceptionHandler: ${throwable.message}")
            throwable.printStackTrace()
            println("Caught $throwable")
        }
    private val tutorialLifecycleScope = lifecycleScope + coroutineExceptionHandler


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTutorialBinding.bind(view)
        val tutorial = arguments?.getParcelable<Tutorial>(TUTORIAL_KEY) as Tutorial
        binding?.let {
            with(it) {
                tutorialName.text = tutorial.name
                tutorialDesc.text = tutorial.description
                reload.setOnClickListener { downloadTutorialImages(tutorial) }
            }
        }
        downloadTutorialImages(tutorial)
    }

    private fun downloadTutorialImages(tutorial: Tutorial) {
        if (tutorial.imageTwoUrl.isBlank()) {
            downloadSingleImage(tutorial)
        } else {
            downloadTwoImages(tutorial)
        }
    }

    private fun downloadSingleImage(tutorial: Tutorial) {
        tutorialLifecycleScope.launch {
            val originalBitmap = getOriginalBitmap(tutorial)
            val snowFilterBitmap = loadSnowFilter(originalBitmap)
            loadImage(snowFilterBitmap)

        }
    }

    private fun downloadTwoImages(tutorial: Tutorial) {
        lifecycleScope.launch {

            val deferredOne = lifecycleScope.async {
                getOriginalBitmap(tutorial)
            }

            val deferredTwo = lifecycleScope.async {
                val originalBitmap = getOriginalBitmap(tutorial)
                loadSnowFilter(originalBitmap)
            }

            try {
                loadTwoImages(deferredOne.await(), deferredTwo.await())
            } catch (e: Exception) {
                showError("Try/catch: ${e.message}")
            }
        }
    }

    //1
    private suspend fun getOriginalBitmap(tutorial: Tutorial): Bitmap =
        //2
        withContext(Dispatchers.IO) {
            //3
            URL(tutorial.imageUrl).openStream().use {
                return@withContext BitmapFactory.decodeStream(it)
            }
        }


    private suspend fun loadSnowFilter(originalBitmap: Bitmap): Bitmap =
        withContext(Dispatchers.Default)
        { SnowFilter.applySnowEffect(originalBitmap) }


    private fun loadImage(snowFilterBitmap: Bitmap) {
        binding?.let {
            with(it) {
                progressBar.visibility = View.GONE
                reload.isVisible = false
                errorMessage.isVisible = false
                snowFilterImage.setImageBitmap(snowFilterBitmap)
            }
        }
    }

    private fun loadTwoImages(imageOne: Bitmap, imageTwo: Bitmap) {
        binding?.let {
            with(it) {
                progressBar.visibility = View.GONE
                reload.isVisible = false
                errorMessage.isVisible = false
                snowFilterImage.setImageBitmap(imageOne)
                snowFilterSecondImage.setImageBitmap(imageTwo)
            }
        }
    }

    private fun showError(message: String) {
        binding?.let {
            with(it) {
                errorMessage.isVisible = true
                reload.isVisible = true
                errorMessage.text = message
                progressBar.isVisible = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    companion object {
        const val TUTORIAL_KEY = "TUTORIAL"

        fun newInstance(tutorial: Tutorial): TutorialFragment {
            return TutorialFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(TUTORIAL_KEY, tutorial)
                }
            }
        }
    }
}