import me.mrfang.transparent.Transparent

@JvmInline
@Transparent
value class Name<T: Number>(private val t: T)

fun main() {
    val name = Name<Double>(5.0)
    val i = name.toInt()
}