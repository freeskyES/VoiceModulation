package com.muse.domain.entity

data class Story(
        override val id: String,
        val title: String
): Entity
