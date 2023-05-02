import me.mrfang.transparent.Transparent

@Transparent
@JvmInline
value class Name(private val s: String)

val name = Name("foobar")
val foo = name.subSequence(0, 3)
