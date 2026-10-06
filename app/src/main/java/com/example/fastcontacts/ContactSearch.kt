package com.example.fastcontacts

class SearchQuery(raw: String) {
    val text: String = normalizeText(raw)
    val digits: String = text.filter { it.isDigit() }
    val phoneLike: Boolean = digits.isNotEmpty() && text.all {
        it.isDigit() || it == '+' || it == '-' || it == '(' || it == ')' || it == ' '
    }
}

object Ranking {
    const val NO_MATCH = Int.MAX_VALUE

    fun rank(c: Contact, q: SearchQuery): Int {
        val t = q.text
        if (t.isEmpty()) return 0
        return when {
            c.firstLc == t -> 1
            c.firstLc.startsWith(t) -> 2
            c.firstLc.contains(t) -> 3
            c.lastLc.startsWith(t) -> 4
            c.fullLc.startsWith(t) -> 5
            c.fullLc.contains(t) -> 6
            q.phoneLike && c.phoneDigits.any { it.contains(q.digits) } -> 7
            else -> NO_MATCH
        }
    }
}

class SearchEngine(contacts: List<Contact>) {
    internal class Scored(val contact: Contact, val rank: Int)

    private val all: List<Contact> = contacts.sortedWith(ALPHABETICAL)
    private var lastText: String = ""
    private var lastMatches: List<Contact> = all

    fun search(raw: String): List<Contact> {
        val q = SearchQuery(raw)
        if (q.text.isEmpty()) {
            lastText = ""
            lastMatches = all
            return all
        }
        val pool = if (lastText.isNotEmpty() && q.text.startsWith(lastText)) lastMatches else all

        val scored = ArrayList<Scored>()
        for (c in pool) {
            val r = Ranking.rank(c, q)
            if (r != Ranking.NO_MATCH) scored.add(Scored(c, r))
        }
        scored.sortWith(SCORED_ORDER)

        val result = ArrayList<Contact>(scored.size)
        for (s in scored) result.add(s.contact)
        lastText = q.text
        lastMatches = result
        return result
    }

    private companion object {
        val ALPHABETICAL = Comparator<Contact> { a, b ->
            if (a.isStarred != b.isStarred) return@Comparator if (a.isStarred) -1 else 1
            var d = a.firstLc.compareTo(b.firstLc)
            if (d == 0) d = a.lastLc.compareTo(b.lastLc)
            if (d == 0) d = a.id.compareTo(b.id)
            d
        }

        val SCORED_ORDER = Comparator<Scored> { a, b ->
            var d = a.rank.compareTo(b.rank)
            if (d == 0) {
                if (a.contact.isStarred != b.contact.isStarred) return@Comparator if (a.contact.isStarred) -1 else 1
                d = a.contact.firstLc.compareTo(b.contact.firstLc)
            }
            if (d == 0) d = a.contact.lastLc.compareTo(b.contact.lastLc)
            if (d == 0) d = a.contact.id.compareTo(b.contact.id)
            d
        }
    }
}
