package com.pac.conspectus.tool

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log.e
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.pac.conspectus.tool.data.Conspectus
import com.pac.conspectus.tool.data.History

object Storage {

    private lateinit var storage: SharedPreferences
    private val database = Firebase.database.reference.child(Registrar.getUserName())

    fun init(activity: Activity) {
        //init Shared Preferences with context and unique username
        storage = activity.getSharedPreferences(
            Registrar.getUserName(),
            MODE_PRIVATE
        )
    }

    fun saveText(text: String) {
        val history = getHistory().toMutableList()
        //generate default paragraph name using paragraph size
        val title = "Параграф ${history.size + 1}"
        //add this paragraph to the list
        history.add(title)
        //Shared Preferences sorts the items,
        //so you have to save the information as a string
        val paragraphs = history.joinToString("#")
        with(storage.edit()) {
            //save the title of the paragraph to display it to the user
            putString("now", title)
            val textHandler = TextHandler(text)
            //Shared Preferences sorts the value you have to save
            //the string, which is separated by #
            putString("Paragraphs", paragraphs)
            putString("$title Conspectus", textHandler.getConspectus())
            putString("$title Dates", textHandler.getDates())
            putString("$title Name", title)
            apply()
        }
    }

    fun getNowConspectus(): List<String> {
        val nowTitle = storage.getString("now", null) ?: return emptyList()
        val conspectus = storage.getString("$nowTitle Conspectus", null) ?: return emptyList()
        return if (conspectus.isEmpty()) emptyList() else conspectus.split("#")
    }

    fun getNowDates(): List<String> {
        val nowTitle = storage.getString("now", null) ?: return emptyList()
        val dates = storage.getString("$nowTitle Dates", null) ?: return emptyList()
        return if (dates.isEmpty()) emptyList() else dates.split("#")
    }

    fun getHistoryNames(): List<String> {
        val historyNames = mutableListOf<String>()
        for (h in getHistory()) historyNames.add(storage.getString("$h Name", "")!!)
        return historyNames
    }

    fun getHistoryName(position: Int): String =
        storage.getString("${getHistory(position)} Name", "")!!

    fun deleteAll() {
        with(storage.edit()) {
            clear()
            apply()
        }
    }

    fun setNowParagraph(position: Int) {
        with(storage.edit()) {
            putString("now", getHistory(position))
            apply()
        }
    }

    fun setParagraphName(position: Int, paragraph: String) {
        with(storage.edit()) {
            putString("${getHistory(position)} Name", paragraph)
            apply()
        }
    }

    fun deleteParagraph(position: Int) {
        //delete the last paragraph == delete all
        if (getHistory().size == 1) deleteAll()
        else {
            val title = getHistory(position)
            val paragraphs = getHistory().joinToString("#").replace("$title#", "")
            with(storage.edit()) {
                putString("Paragraphs", paragraphs)
                remove("$title Conspectus")
                remove("$title Dates")
                remove("$title Name")
                apply()
            }
        }
    }

    fun makeBackup() {
        database.removeValue()
        database.child("history").setValue(History(storage.getString("Paragraphs", "")))
        for (h in getHistory()) {
            val name = storage.getString("$h Name", "")
            val conspectus = storage.getString("$h Conspectus", "")
            val dates = storage.getString("$h Dates", "")
            database.child(h).setValue(Conspectus(name, conspectus, dates))
        }
    }

    fun uploadBackup() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                deleteAll()
                val resultMap =
                    snapshot.getValue<HashMap<String, HashMap<String, String>>>() ?: emptyMap()
                with(storage.edit()) {
                    val history = resultMap["history"]?.get("history") ?: ""
                    if (history.isNotEmpty()) putString("Paragraphs", history)
                    for (key in resultMap.keys)
                        if (key != "history") {
                            val map = resultMap.getValue(key)
                            putString("$key Conspectus", map["conspectus"])
                            putString("$key Dates", map["dates"])
                            putString("$key Name", map["name"])
                            putString("now", key)
                        }
                    apply()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                e("MyLogger", error.message)
            }
        })
    }

    fun isEmpty() = getHistory().isEmpty()

    private fun getHistory(): List<String> =
        storage.getString("Paragraphs", null)?.split("#") ?: emptyList()

    private fun getHistory(position: Int): String = getHistory()[position]
}