package com.github.hyuma.uhfr2000demo.ui.tag_inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.github.hyuma.uhfr2000demo.R
import com.github.hyuma.uhfr2000demo.databinding.FragmentTagInventoryBinding

class TagInventoryFragment: Fragment() {
    companion object {
        val TAG: String = TagInventoryFragment::class.java.simpleName
    }

    var binding: FragmentTagInventoryBinding? = null
    //private var binding:
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tag_inventory, container, false)
        return requireNotNull(binding).root
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireNotNull(binding).lifecycleOwner = this
        val viewModel = ViewModelProviders.of(this).get(TagInventoryViewModel::class.java)
        requireNotNull(binding).viewModel = viewModel
    }
}