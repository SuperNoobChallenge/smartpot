package com.example.smartpot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class Fragment_main_Haunted_house : Fragment() {
    private var pageNumber: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pageNumber = it.getInt(ARG_PAGE_NUMBER)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_main_haunted_house, container, false)
        return rootView
    }

    companion object {
        private const val ARG_PAGE_NUMBER = "page_number"

        fun newInstance(pageNumber: Int): Fragment_main_Haunted_house {
            val fragment = Fragment_main_Haunted_house()
            val args = Bundle()
            args.putInt(ARG_PAGE_NUMBER, pageNumber)
            fragment.arguments = args
            return fragment
        }
    }
}