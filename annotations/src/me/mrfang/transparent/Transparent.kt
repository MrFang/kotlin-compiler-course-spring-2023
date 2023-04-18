package me.mrfang.transparent

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Transparent(
    val methods: Array<String> = []
)
