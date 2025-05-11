package com.example.shoppinglist.data.api

data class PexelsResponse(
    val totel_results: Int,
    val page: Int,
    val per_page: Int,
    val photos: List<PexelsPhoto>,
    val next_page: String?
)

data class PexelsPhoto(
    val id: Int,
    val width: Int,
    val heigth: Int,
    val url: String,
    val photograher: String,
    val photographer_url: String,
    val src: PexelsPhotoSrc
)

data class PexelsPhotoSrc(
    val original: String,
    val large2x: String,
    val large: String,
    val medium: String,
    val small: String,
    val portrait: String,
    val landscape: String,
    val tiny: String
)