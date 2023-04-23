import me.mrfang.transparent.Transparent

@Transparent(methods=["toString"])
@JvmInline
value class Name(private val s: String)
