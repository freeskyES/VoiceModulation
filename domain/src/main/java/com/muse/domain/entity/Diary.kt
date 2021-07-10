package com.muse.domain.entity

import java.util.*

data class Diary(
     override val id: String = UUID.randomUUID().toString()
): Entity
