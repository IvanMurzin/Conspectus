package com.pac.conspectus.tool.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Conspectus(
    var name: String?,
    var conspectus: String?,
    var dates: String?
)
