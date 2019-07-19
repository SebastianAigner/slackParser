typealias Matcher = (String) -> Pair<Token?, String>

fun matchByRegex(remaining: String, regex: Regex, out: (String) -> Token): Pair<Token?, String> {
    var token: Token? = null
    val parsed = remaining.replace(regex) {
        token = out(it.groupValues[1])
        ""
    }
    return Pair(token, parsed)
}

tailrec fun parseByMatchers(matchers: List<Matcher>, string: String, parsed: List<Token> = emptyList()): List<Token> {
    if (string.count() == 0) return parsed

    for (matcher in matchers) {
        val (tok, rem) = matcher(string)
        if (tok != null)
            return parseByMatchers(matchers, rem, parsed + tok)
    }
    return parseByMatchers(matchers, string.drop(1), parsed + Token.Literal(string.first().toString()))
}

fun matchIssue(remaining: String) = matchByRegex(remaining, "^(\\w+-\\d+)".toRegex()) { Token.Issue(it) }
fun matchEscaped(remaining: String) = matchByRegex(remaining, "^<([^>]+)>".toRegex()) { Token.Escaped(it) }


fun String.parsedBy(m: List<Matcher>): List<Token> {
    return parseByMatchers(m, this)
}