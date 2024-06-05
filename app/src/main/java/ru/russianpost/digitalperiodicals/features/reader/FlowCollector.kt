package ru.russianpost.digitalperiodicals.features.reader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import ru.russianpost.digitalperiodicals.entities.LastPage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlowCollector @Inject constructor(
    readerRepository: ReaderRepository,
    bookmarkRepository: BookmarkRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    val bookmarkFlow = MutableSharedFlow<Int>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val lastPageFlow = MutableSharedFlow<LastPage>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        scope.launch {
            bookmarkFlow.debounce(2000).collect {
                try {
                    bookmarkRepository.syncAllBookmarks(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        scope.launch {
            lastPageFlow.debounce(2000).collect {
                try {
                    readerRepository.postLastPage(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}