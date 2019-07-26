typealias Matcher = (String) -> Pair<Token?, String>

fun matchByRegex(remaining: String, regex: Regex, out: (String) -> Token): Pair<Token?, String> {
    var token: Token? = null
    val parsed = remaining.replace(regex) {
        token = out(it.groupValues[1])
        ""
    }
    return Pair(token, parsed)
}

fun matchMultipleByRegex(remaining: String, regex: Regex, out: (MatchResult) -> Token): Pair<Token?, String> {
    var token: Token? = null
    val parsed = remaining.replace(regex) {
        token = out(it)
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

fun matchIssue(remaining: String) = matchByRegex(remaining, "^(\\w+-\\d+)".toSafeRegex()) { Token.Issue(it) }

fun matchMarkdownLink(remaining: String) =
    matchMultipleByRegex(remaining, "^\\[([^]]+)]\\(<(https?[^)]+)>\\)".toSafeRegex()) {
    Token.MarkdownLink(it.groupValues[1], it.groupValues[2])
}

fun matchEscaped(remaining: String) = matchByRegex(remaining, "^<([^>]+)>".toSafeRegex()) {
    distinguishEscaped(Token.Escaped(it))
}

fun distinguishEscaped(t: Token.Escaped): Token {
    return listOfNotNull(
        escapedYouTrackLink(t),
        escapedPerson(t)
    ).firstOrNull() ?: fallbackEscapedLink(t)
}

fun String.parsedBy(m: List<Matcher>): List<Token> {
    return parseByMatchers(m, this)
}

fun escapedYouTrackLink(t: Token.Escaped): Token? = matchByRegex(t.content, "issue/(\\w+-\\d+)".toRegex()) {
    Token.Issue(it)
}.first

fun escapedPerson(t: Token.Escaped): Token? =
    matchByRegex(t.content, "@(\\w+)\\|.*".toRegex()) {
        Token.Issue(it)
    }.first

fun fallbackEscapedLink(t: Token.Escaped): Token = matchByRegex(t.content, "(.*://.*)".toRegex()) { Token.Link(t.content) }.first ?: t

fun String.toSafeRegex(): Regex {
    if(this.startsWith("^")) return this.toRegex() else error("When consuming tokens, only accept the first character!")
}