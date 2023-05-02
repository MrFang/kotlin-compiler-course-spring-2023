import me.mrfang.transparent.Transparent

@Transparent
@JvmInline
value class Name(<!TRANSPARENT_NULLABLE_PROPERTY!>private val s: String?<!>)
