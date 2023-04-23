# Kotlin Compiler Transparent plugin

## Usage
```kotlin
@JvmInline
@Transparent
value class Name(private val s: String)

val name = Name("fang")
name.subSequence(1, 2)
```

You can choose which methods should be generated (by default all of them)

```kotlin
@JvmInline
@Transparent(methods=["toString"])
value class Age(private val i: Int)

val age = Age(21)
age.toString() // OK
age + 12 // Error
```

## TODO
- Add ability to generate methods with value class as parameter. For example
```kotlin
@JvmInline
@Transparent(methods=["plus"])
value class Age(private val i: Int)

val a1 = Age(10)
val a2 = Age(20)
require((a1 + a2) is Age)
```

- Add ability to create presets of deriving

```kotlin
@Transparent(methods = ["toString", "plus"])
annotation class StringPlus

@JvmInline
@StringPlus
value class Name(private val s: String)

@JvmInline
@StringPlus
value class Age(private val i: Int)
```
