package com.abc.locusvisionis.data.bible

object BibleContentEnglish {
    val verses: List<BibleContentItem> = listOf(
//        Matthew
        BibleContentItem(
            book = "Matthew",
            chapter = 1,
            verse = 1,
            text = "This is the genealogy of Jesus the Messiah the son of David, the son of Abraham:",
            isFavorite = false
        ),
        BibleContentItem(
            book = "Matthew",
            chapter = 1,
            verse = 2,
            text = "Abraham was the father of Issac, Issac the father of Jacob, Jacob the father of Judah and his brothers,",
            isFavorite = false
        ),
        BibleContentItem(
            book = "Matthew",
            chapter = 1,
            verse = 3,
            text = "Judah the father of Perez and Zerah, whose mother was Tamar, Perez the father of Hezron, Hezron the father of Ram,",
            isFavorite = false
        ),
//        Mark
        BibleContentItem(
            book = "Mark",
            chapter = 1,
            verse = 1,
            text = "",
            isFavorite = false
        ),
        BibleContentItem(
            book = "John",
            chapter = 3,
            verse = 16,
            text = "For God so loved the world that he gave his one and only Son, that whoever believes in him shall not perish but have eternal life.",
            isFavorite = true
        ),
        BibleContentItem(
            book = "Psalm",
            chapter = 23,
            verse = 1,
            text = "The Lord is my shepherd, I lack nothing."
        ),
        BibleContentItem(
            book = "Philippians",
            chapter = 4,
            verse = 13,
            text = "I can do all this through him who gives me strength.",
            isFavorite = true
        ),
        BibleContentItem(
            book = "Proverbs",
            chapter = 3,
            verse = 5,
            text = "Trust in the Lord with all your heart and lean not on your own understanding."
        ),
        BibleContentItem(
            book = "Jeremiah",
            chapter = 29,
            verse = 11,
            text = "For I know the plans I have for you, declares the Lord, plans to prosper you and not to harm you, plans to give you hope and a future."
        ),
        BibleContentItem(
            book = "Romans",
            chapter = 8,
            verse = 28,
            text = "And we know that in all things God works for the good of those who love him, who have been called according to his purpose."
        )
    )
}
