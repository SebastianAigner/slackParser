fun linkTransform(t: Token): Token {
    if (t !is Token.Escaped) return t

    val link = "://".toRegex()
    if (link.containsMatchIn(t.content)) {
        return Token.Link(t.content)
    }
    return t
}

fun youtrackLinkTransform(t: Token): Token {
    if (t !is Token.Link) return t

    val youTrackRegex = "issue/(\\w+-\\d+)".toRegex()
    val matches = youTrackRegex.find(t.content)
    return matches?.groupValues?.get(1)?.let { Token.Issue(it) } ?: t
}


fun personTransform(t: Token): Token {
    if (t !is Token.Escaped) return t

    val personRegex = "@(\\w+)\\|.*".toRegex()
    val matches = personRegex.find(t.content)
    return matches?.groupValues?.get(1)?.let { Token.Person(it) } ?: t
}


fun applyTransformers(tokens: List<Token>, transformers: List<(Token) -> (Token)>): List<Token> {
    tailrec fun converge(l: List<Token>, simplify: (List<Token>) -> List<Token>): List<Token> {
        val simplified = simplify(l)
        return if (simplified == l) l else converge(simplified, simplify)
    }

    return converge(tokens) { list ->
        var simplifiedList = list
        for (transformer in transformers) {
            simplifiedList = simplifiedList.map(transformer)
        }
        simplifiedList
    }
}

fun List<Token>.transformedBy(transformers: List<(Token) -> (Token)>): List<Token> {
    return applyTransformers(this, transformers)
}