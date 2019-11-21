package com.github.hyuma.uhfr2000demo.ui.not_connected

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.hyuma.uhfr2000demo.R
import com.github.hyuma.uhfr2000demo.databinding.FragmentNotConnectedBinding
import com.github.hyuma.uhfr2000demo.ui.MainActivity
import com.github.hyuma.uhfr2000demo.ui.tag_inventory.TagInventoryFragment

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
        val factory = NotConnectedViewModel.Factory(activity!!.application)
        val viewModel = ViewModelProviders.of(this, factory).get(NotConnectedViewModel::class.java)

        viewModel.navigateToTagInventoryFragment.observe(this, Observer { _goNextPage ->
            if(_goNextPage){
                val fragment = TagInventoryFragment()
                activity!!.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment, TagInventoryFragment.TAG)
                    .commit()
            }
        })

        viewModel.connectUHFR2000.observe(this, Observer{
            if(it){
                val mainActivity = activity!!
                if (mainActivity is MainActivity){
                    mainActivity.setUpUSBSerialConnection()
                }
            }
        })

        requireNotNull(binding).viewModel = viewModel
    }
}