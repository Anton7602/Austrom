package com.colleagues.austrom.models

class Language(val languageCode: String,
               val languageName: String,) {

    companion object {
        val supportedLanguages: List<Language> = listOf(
            Language("en", "English"),
            Language("ru", "Russian")
        )
    }
}