package com.pac.conspectus.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pac.conspectus.R
import com.pac.conspectus.tool.Alert
import com.pac.conspectus.tool.Storage
import kotlinx.android.synthetic.main.alert_dialog_paragraph_name.*
import kotlinx.android.synthetic.main.fragment_dates.view.*
import kotlinx.android.synthetic.main.fragment_history.view.*
import kotlinx.android.synthetic.main.fragment_history.view.text_empty
import kotlinx.android.synthetic.main.recycler_item.view.*

class HistoryFragment : Fragment() {

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, null)
        val recycler = view?.findViewById<RecyclerView>(R.id.recycler)
        recycler?.layoutManager = LinearLayoutManager(activity)
        recycler?.adapter = HistoryAdapter(activity ?: return null)
        view?.delete_history?.setOnClickListener {
            Storage.deleteAll()
            //refresh recycler
            recycler?.adapter = HistoryAdapter(activity ?: return@setOnClickListener)
        }
        if (Storage.isEmpty()) view.text_empty.visibility = View.VISIBLE
        else view.text_empty.visibility = View.GONE
        return view
    }

    class HistoryAdapter(private val activity: Activity) :
        RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        //get data from saved history
        private var data = Storage.getHistoryNames()

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
            //changes the paragraph to the one that was clicked
            viewHolder.itemView.setOnClickListener {
                Storage.setNowParagraph(position)
            }
            viewHolder.itemView.setOnLongClickListener {
                //show dialog to change or delete paragraph
                val alert = Alert.getChangeParagraphForm(activity)
                alert?.text?.setText(Storage.getHistoryName(position))
                alert?.yes?.setOnClickListener {
                    val text = alert.text.text.toString()
                    if (text.length > 2) {
                        Storage.setParagraphName(position, text)
                        //refresh data
                        data = Storage.getHistoryNames()
                        //refresh item
                        this.notifyItemChanged(position)
                        alert.dismiss()
                    }
                }
                alert?.delete?.setOnClickListener {
                    Storage.deleteParagraph(position)
                    //refresh data
                    data = Storage.getHistoryNames()
                    //refresh recycler
                    this.notifyItemRemoved(position)
                    alert.dismiss()
                }
                alert?.cancel?.setOnClickListener { alert.dismiss() }
                true
            }
        }

        override fun getItemCount() = data.size
    }
}