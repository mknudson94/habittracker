package mk.habittracker.nfc

import android.nfc.Tag
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagBus @Inject constructor() {
    private val _tags = MutableSharedFlow<Tag>(extraBufferCapacity = 1)
    val tags = _tags.asSharedFlow()

    val subscribers = _tags.subscriptionCount

    fun add(tag: Tag) {
        Log.d("nfc", "[TagBus#add] emitting tag")
        _tags.tryEmit(tag)
    }
}