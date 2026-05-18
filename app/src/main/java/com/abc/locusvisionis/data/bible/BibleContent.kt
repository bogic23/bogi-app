package com.abc.locusvisionis.data.bible

data class BibleContentItem(
    val book: String,
    val chapter: Int,
    val verse: Int,
    val englishText: String,
    val indonesianText: String,
    val isFavorite: Boolean = false
) {
    val reference: String
        get() = "$book $chapter:$verse"
}

object BibleContent {
    val verses: List<BibleContentItem> = listOf(
        BibleContentItem(
            book = "John",
            chapter = 3,
            verse = 16,
            englishText = "For God so loved the world that he gave his one and only Son, that whoever believes in him shall not perish but have eternal life.",
            indonesianText = "Karena begitu besar kasih Allah akan dunia ini, sehingga Ia telah mengaruniakan Anak-Nya yang tunggal, supaya setiap orang yang percaya kepada-Nya tidak binasa, melainkan beroleh hidup yang kekal.",
            isFavorite = true
        ),
        BibleContentItem(
            book = "Psalm",
            chapter = 23,
            verse = 1,
            englishText = "The Lord is my shepherd, I lack nothing.",
            indonesianText = "Tuhan adalah gembalaku, takkan kekurangan aku."
        ),
        BibleContentItem(
            book = "Philippians",
            chapter = 4,
            verse = 13,
            englishText = "I can do all this through him who gives me strength.",
            indonesianText = "Segala perkara dapat kutanggung di dalam Dia yang memberi kekuatan kepadaku.",
            isFavorite = true
        ),
        BibleContentItem(
            book = "Proverbs",
            chapter = 3,
            verse = 5,
            englishText = "Trust in the Lord with all your heart and lean not on your own understanding.",
            indonesianText = "Percayalah kepada Tuhan dengan segenap hatimu, dan janganlah bersandar kepada pengertianmu sendiri."
        ),
        BibleContentItem(
            book = "Jeremiah",
            chapter = 29,
            verse = 11,
            englishText = "For I know the plans I have for you, declares the Lord, plans to prosper you and not to harm you, plans to give you hope and a future.",
            indonesianText = "Sebab Aku ini mengetahui rancangan-rancangan apa yang ada pada-Ku mengenai kamu, demikianlah firman Tuhan, yaitu rancangan damai sejahtera dan bukan rancangan kecelakaan, untuk memberikan kepadamu hari depan yang penuh harapan."
        ),
        BibleContentItem(
            book = "Romans",
            chapter = 8,
            verse = 28,
            englishText = "And we know that in all things God works for the good of those who love him, who have been called according to his purpose.",
            indonesianText = "Kita tahu sekarang, bahwa Allah turut bekerja dalam segala sesuatu untuk mendatangkan kebaikan bagi mereka yang mengasihi Dia, yaitu bagi mereka yang terpanggil sesuai dengan rencana Allah."
        )
    )
}
