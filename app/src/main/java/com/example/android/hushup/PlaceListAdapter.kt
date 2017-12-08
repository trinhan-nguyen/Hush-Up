package com.example.android.hushup

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by ngtrnhan1205 on 12/7/17.
 */
class PlaceListAdapter(private val mContext: Context) : RecyclerView.Adapter<PlaceListAdapter.PlaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        // Get the RecyclerView item layout
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.item_place_card, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {

    }


    override fun getItemCount(): Int {
        return 0
    }

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var nameTextView: TextView
        var addressTextView: TextView

        init {
            nameTextView = itemView.findViewById(R.id.name_text_view) as TextView
            addressTextView = itemView.findViewById(R.id.address_text_view) as TextView
        }

    }
}