package com.pac.conspectus.tool

import java.util.Locale.ROOT
import kotlin.collections.set

class TextHandler(var text: String) {

    private var sentences: List<String>

    init {
        //replace ? or ! to .
        text = Regex("[?!]").replace(text, ".")
        //replace text wrapping
        text = Regex("(\n)|(\\[\\d+])").replace(text, "")
        //replace double spaces
        text = Regex("\\s+").replace(text, " ")
        var newText = ""
        //replace hyphenation
        for (i in 1 until text.length - 1)
            if (!((text[i] == '-' || text[i] == '—') && (text[i - 1] != ' ' && !text[i - 1].isDigit()) && (text[i + 1] != ' ' && !text[i + 1].isDigit())))
                newText += text[i]
        text = newText
        //create sentence list
        var tmp = if (text.isNotEmpty()) text[0].toString() else ""
        for (i in 1 until text.length)
        //check if point needs to be deleted
            tmp += if (text[i] == '.' && text[i - 1] != 'г' && (text[i - 1] <= 'А' || text[i - 1] >= 'Я')) '#'
            else text[i]
        sentences = tmp.split("#")
    }

    fun getConspectus(): String {
        //create regex for stemming
        val stemRegEx =
            Regex("""(((в|вши|вшись)|(ив|ивши|ившись|ыв|ывши|ывшись)|(ся|сь)|(ость|ост)|(ее|ие|ые|ое|ими|ыми|ей|ий|ый|ой|ем|им|ым|ом|его|ого|ему|ому|их|ых|ую|юю|ая|яя|ою|ею)|(ем|нн|вш|ющ|щ)|(ивш|ывш|ующ)|(ла|на|ете|йте|ли|й|л|ем|н|ло|но|ет|ют|ны|ть|ешь|нно)|(ила|ыла|ена|ейте|уйте|ите|или|ыли|ей|уй|ил|ыл|им|ым|ен|ило|ыло|ено|ят|ует|уют|ит|ыт|ены|ить|ыть|ишь|ую|ю)|(ев|ов|ие|ье|е|иями|ями|ами|еи|ии|и|ией|ей|ой|ий|й|иям|ям|ием|ем|ам|ом|о|у|ах|иях|ях|ы|ь|ию|ью|ю|ия|ья|я|а)|(и)|(нн)|(ейш|ейше|нейш|нейше)|(ь)|)\b)""")
        //replace word morphemes
        var cutText = stemRegEx.replace(text, "")
        //replace punctuation marks
        cutText = Regex("[,.-:]").replace(cutText, " ").toLowerCase(ROOT)
        val wordList = cutText.split(" ")
        //create map with word : count
        val wordMap = HashMap<String, Int>()
        var mostPopular = 0
        val popularWords = ArrayList<String>()
        val conspectus = mutableListOf<String>()
        wordList.forEach {
            if (it != "")
                if (wordMap.containsKey(it)) wordMap[it] = wordMap[it]!! + 1
                else wordMap[it] = 1
        }
        wordMap.keys.forEach {
            if (wordMap[it]!! > mostPopular && it.length > 3)
                mostPopular = wordMap[it]!!
        }
        wordMap.keys.forEach {
            if (wordMap[it]!! > mostPopular - 3 && it.length > 3)
                popularWords.add(it)
        }
        sentences.forEach {
            for (pw in popularWords)
                if (it.toLowerCase(ROOT).contains(pw) && it.length > 10) {
                    conspectus.add(it.removePrefix(" "))
                    break
                }
        }
        return conspectus.joinToString("#")
    }

    fun getDates(): String {
        val dates = mutableListOf<String>()
        sentences.forEach {
            if (Regex("\\d\\d\\d").find(it) != null && it.length > 10)
                dates.add(it.removePrefix(" "))
        }
        return dates.joinToString("#")
    }
}