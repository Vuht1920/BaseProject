package com.mmt.extractor.data.room.converter

import androidx.room.TypeConverter
import com.mmt.extractor.data.model.CategoryApp

class CategoryConverter {
    @TypeConverter
    fun fromCategory(category: CategoryApp): String {
        return when (category) {
            is CategoryApp.All -> "all"
            is CategoryApp.Undefined -> "undefined"
            is CategoryApp.Social -> "social"
            is CategoryApp.Games -> "games"
            is CategoryApp.AudioVideo -> "audio_video"
            is CategoryApp.Productivity -> "productivity"
            is CategoryApp.Video -> "video"
            is CategoryApp.Image -> "image"
            is CategoryApp.News -> "news"
            is CategoryApp.Maps -> "maps"
            else -> "undefined"
        }
    }

    @TypeConverter
    fun toCategory(value: String): CategoryApp {
        return when (value) {
            "all" -> CategoryApp.All
            "undefined" -> CategoryApp.Undefined
            "social" -> CategoryApp.Social
            "games" -> CategoryApp.Games
            "audio_video" -> CategoryApp.AudioVideo
            "productivity" -> CategoryApp.Productivity
            "video" -> CategoryApp.Video
            "image" -> CategoryApp.Image
            "news" -> CategoryApp.News
            "maps" -> CategoryApp.Maps
            else -> CategoryApp.Undefined
        }
    }
}