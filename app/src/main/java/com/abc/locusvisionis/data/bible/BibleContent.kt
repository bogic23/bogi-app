package com.abc.locusvisionis.data.bible

data class BibleContentItem(
    val book: String,
    val chapter: Int,
    val verse: Int,
    val text: String,
    val isFavorite: Boolean = false
) {
    val reference: String
        get() = "$book $chapter:$verse"
}
