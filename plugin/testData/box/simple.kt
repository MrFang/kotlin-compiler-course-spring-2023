import me.mrfang.transparent.Transparent

@Transparent
@JvmInline
value class Name(private val s: String)

fun box(): String {
    val name = Name("foobar")
    val s = name.subSequence(0, 3)

    return if (s == "foo") "OK"
    else "Failure: $s"
}
