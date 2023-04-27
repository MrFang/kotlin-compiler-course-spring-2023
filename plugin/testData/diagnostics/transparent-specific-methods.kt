import me.mrfang.transparent.Transparent

@Transparent(methods=["plus"])
@JvmInline
value class Name(private val s: String)

val name = Name("foobar")
val s = name + "baz"
val foo = name.<!UNRESOLVED_REFERENCE!>subSequence<!>(0, 3)
