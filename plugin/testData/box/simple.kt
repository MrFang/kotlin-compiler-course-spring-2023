import me.mrfang.transparent.Transparent

@Transparent
@JvmInline
value class Name(private val f: String)

fun box(): String {
    val name = Name("foo")
    val s = name + "bar"

    return if (s == "foobar") "OK" else "Failure: $s"
}
