package io.github.achmadhafid.fitnaphone

import jonathanfinerty.once.Once

//region Helper Function : Do thing only once

fun doOnce(tag: String, block: () -> Unit) {
    if (!Once.beenDone(tag)) {
        Once.markDone(tag)
        block()
    }
}

//endregion
