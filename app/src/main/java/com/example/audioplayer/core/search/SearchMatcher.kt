package com.example.audioplayer.core.search

object SearchMatcher {
    fun matches(query: String, vararg fields: String?): Boolean {
        val terms = query.trim()
            .lowercase()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
        if (terms.isEmpty()) return true

        val searchable = fields
            .filterNotNull()
            .joinToString(separator = " ")
            .lowercase()

        return terms.all(searchable::contains)
    }
}