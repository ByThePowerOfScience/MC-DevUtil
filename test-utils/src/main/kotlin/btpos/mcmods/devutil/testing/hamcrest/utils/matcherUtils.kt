package btpos.mcmods.devutil.testing.hamcrest.utils

import org.hamcrest.Description
import org.hamcrest.Matcher

fun <T> Description.appendMismatchList(matchers: Iterable<Matcher<T>>, item: T, start: String = "[", separator: String = ",", end: String = "]") {
    var needsSeparator = false
    appendText(start)
    matchers.filterNot { it.matches(item) }.forEach {
        if (needsSeparator) {
            appendText(separator)
        }
        
        it.describeMismatch(item, this)
        
        needsSeparator = true
    }
    appendText(end)
}