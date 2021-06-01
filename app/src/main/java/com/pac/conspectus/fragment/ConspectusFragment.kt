package com.pac.conspectus.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pac.conspectus.R
import com.pac.conspectus.tool.Storage
import kotlinx.android.synthetic.main.fragment_dates.view.*
import kotlinx.android.synthetic.main.recycler_item.view.*

class ConspectusFragment : Fragment() {

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_conspectus, null)
        val recycler = view.recycler
        recycler?.layoutManager = LinearLayoutManager(activity)
        recycler?.adapter = ConspectusAdapter()
        if (Storage.isEmpty()) view.text_empty.visibility = View.VISIBLE
        else view.text_empty.visibility = View.GONE
        return view
    }

    class ConspectusAdapter :
        RecyclerView.Adapter<ConspectusAdapter.ViewHolder>() {

        //get data from saved conspectus
        private val data = Storage.getNowConspectus()

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text: TextView = view.text
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            //create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.recycler_item, viewGroup, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            //set each element
            viewHolder.text.text = data[position]
        }

        override fun getItemCount() = data.size
    }
}