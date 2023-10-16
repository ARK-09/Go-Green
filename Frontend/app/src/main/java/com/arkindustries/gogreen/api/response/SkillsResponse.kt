package com.arkindustries.gogreen.api.response

data class SkillResponse(
    val skill: Skill
)

data class Skill(
    val _id: String,
    val title: String,
)
