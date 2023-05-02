import me.mrfang.transparent.Transparent

class Foo {
    fun _public() {}
    protected fun _protected() {}
    internal fun _internal() {}
    private fun _private() {}
}

@JvmInline
@Transparent
value class Name(private val f: Foo)

fun main() {
    val name = Name(Foo())
    name._public()
    name.<!UNRESOLVED_REFERENCE!>_internal<!>()
    name.<!UNRESOLVED_REFERENCE!>_protected<!>()
    name.<!UNRESOLVED_REFERENCE!>_private<!>()
}
