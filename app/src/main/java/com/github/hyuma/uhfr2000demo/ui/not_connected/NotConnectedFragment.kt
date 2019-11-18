package com.github.hyuma.uhfr2000demo.ui.not_connected

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.github.hyuma.uhfr2000demo.R
import com.github.hyuma.uhfr2000demo.databinding.FragmentNotConnectedBinding

class NotConnectedFragment: Fragment() {
    companion object {
        val TAG: String = this::class.java.simpleName
    }

    var binding: FragmentNotConnectedBinding? = null
    //private var binding:
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_not_connected, container, false)
        return requireNotNull(binding).root
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireNotNull(binding).lifecycleOwner = this
        val viewModel = ViewModelProviders.of(this).get(NotConnectedViewModel::class.java)
        requireNotNull(binding).viewModel = viewModel
    }
}