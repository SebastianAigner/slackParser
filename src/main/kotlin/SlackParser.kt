

sealed class Token {
    abstract fun out(): String

    class Literal(val content: String) : Token() {
        override fun out(): String = content
    }
    class Escaped(val content: String) : Token() {
        override fun out() = content
    }

    class Link(val content: String) : Token() {
        override fun out() = "<link '$content'/>"
    }

    class MarkdownLink(val name: String, val link: String): Token() {
        override fun out() = "<link $name @ $link>"
    }

    class Issue(val content: String) : Token() {
        override fun out() = "<issue '$content'/>"
    }

    class Person(val content: String) : Token() {
        override fun out() = "<person '$content'/>"
    }
}


fun main() {
    val matcherCollection = listOf<Matcher>(::matchIssue, ::matchMarkdownLink, ::matchEscaped)

    val input =
    "<http://link.com> [title](<https://oof.com>) <@UGLDAJJ92|sebastian.aigner620> ABCD-1234 <https://youtrack.jetbrains.com/issue/ABCD-1234> abc <yikes>"
    val parsed = input.parsedBy(matcherCollection)
    println(parsed.joinToString(separator = "") { it.out() })
}

