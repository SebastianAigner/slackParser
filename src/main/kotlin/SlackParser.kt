

sealed class Token(val content: String) {
    fun debugString(): String {
        return this.javaClass.name + "[$content]"
    }
    open fun out() = content

    class Literal(content: String) : Token(content)
    class Escaped(content: String) : Token(content)

    class Link(content: String) : Token(content) {
        override fun out() = "<link '$content'/>"
    }

    class Issue(content: String) : Token(content) {
        override fun out() = "<issue '$content'/>"
    }

    class Person(content: String) : Token(content) {
        override fun out() = "<person '$content'/>"
    }
}


fun main() {
    val matcherCollection = listOf<Matcher>(::matchIssue, ::matchEscaped)
    val transformers = listOf(::linkTransform, ::youtrackLinkTransform, ::personTransform)

    val input =
    "<http://link.com> <@UGLDAJJ92|sebastian.aigner620> ABCD-1234 <https://youtrack.jetbrains.com/issue/ABCD-1234> abc <yikes>"
    val parsed = input.parsedBy(matcherCollection)
    val final = parsed.transformedBy(transformers)

    println(input)
    println(final.joinToString { it.debugString() })
    println(final.joinToString(separator = "") { it.out() })
}

