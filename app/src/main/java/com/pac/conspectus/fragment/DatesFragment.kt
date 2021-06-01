package com.pac.conspectus.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pac.conspectus.R
import com.pac.conspectus.tool.Storage
import kotlinx.android.synthetic.main.fragment_dates.view.*
import kotlinx.android.synthetic.main.recycler_item.view.*

class DatesFragment : Fragment() {

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dates, null)
        val recycler = view?.findViewById<RecyclerView>(R.id.recycler)
        recycler?.layoutManager = LinearLayoutManager(activity)
        //set color for spanned dates
        val color = activity?.getColor(R.color.spanned_date) ?: Color.CYAN
        recycler?.adapter = DatesAdapter(color)
        if (Storage.isEmpty()) view.text_empty.visibility = VISIBLE
        else view.text_empty.visibility = GONE
        return view
    }

    class DatesAdapter(private val color: Int) :
        RecyclerView.Adapter<DatesAdapter.ViewHolder>() {

        //get data from saved dates
        private val data = Storage.getNowDates()

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
            //set each element with spanned date
            viewHolder.text.text = getSpannedDate(data[position])
        }

        override fun getItemCount() = data.size

        private fun getSpannedDate(string: String): SpannableString {
            val spannedText = SpannableString(string)
            //find dates with regex
            val entries = Regex("\\d(\\d)+").findAll(string)
            for (entry in entries)
                spannedText.setSpan(
                    ForegroundColorSpan(color),
                    entry.range.first, entry.range.last + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            return spannedText
        }
    }
}