package com.deutschpalast.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deutschpalast.app.data.Word
import com.deutschpalast.app.data.WordDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(private val wordDao: WordDao) : ViewModel() {

    private val _wordsList = MutableStateFlow<List<Word>>(emptyList())
    val wordsList = _wordsList.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            // Check if database is empty and seed if necessary
            wordDao.getAllWords().first().let { existingWords ->
                if (existingWords.isEmpty()) {
                    seedInitialWords()
                }
            }
            // Load words to review
            loadWordsToReview()
        }
    }

    private suspend fun seedInitialWords() {
        val initialWords = listOf(
            Word(german = "Ich", arabic = "أنا", example = "Ich bin Ahmed (أنا أحمد)", reviewCount = 0, isMastered = false),
            Word(german = "Du", arabic = "أنت", example = "Wer bist du? (من أنت؟)", reviewCount = 0, isMastered = false),
            Word(german = "Sein", arabic = "يكون", example = "Das kann sein (يمكن أن يكون ذلك)", reviewCount = 0, isMastered = false),
            Word(german = "Haben", arabic = "يملك / لديه", example = "Ich habe ein Buch (لدي كتاب)", reviewCount = 0, isMastered = false),
            Word(german = "Brot", arabic = "خبز", example = "Das Brot ist lecker (الخبز لذيذ)", reviewCount = 0, isMastered = false),
            Word(german = "Wasser", arabic = "ماء", example = "Ein Glas Wasser, bitte (كأس ماء من فضلك)", reviewCount = 0, isMastered = false),
            Word(german = "Ja", arabic = "نعم", example = "Ja, bitte (نعم، من فضلك)", reviewCount = 0, isMastered = false),
            Word(german = "Nein", arabic = "لا", example = "Nein, danke (لا، شكراً)", reviewCount = 0, isMastered = false),
            Word(german = "Danke", arabic = "شكراً", example = "Vielen Dank! (شكراً جزيلاً!)", reviewCount = 0, isMastered = false),
            Word(german = "Bitte", arabic = "من فضلك / العفو", example = "Bitte schön (على الرحب والسعة)", reviewCount = 0, isMastered = false),
            Word(german = "Hallo", arabic = "مرحباً", example = "Hallo, wie geht's? (مرحباً، كيف حالك؟)", reviewCount = 0, isMastered = false),
            Word(german = "Tschüss", arabic = "وداعاً", example = "Tschüss, bis bald (وداعاً، أراك قريباً)", reviewCount = 0, isMastered = false),
            Word(german = "Guten Morgen", arabic = "صباح الخير", example = "Guten Morgen, allerseits (صباح الخير للجميع)", reviewCount = 0, isMastered = false),
            Word(german = "Gute Nacht", arabic = "تصبح على خير", example = "Gute Nacht, schlaf gut (تصبح على خير، نم جيداً)", reviewCount = 0, isMastered = false),
            Word(german = "Wie", arabic = "كيف", example = "Wie heißt du? (ما اسمك؟)", reviewCount = 0, isMastered = false),
            Word(german = "Was", arabic = "ماذا", example = "Was ist das? (ما هذا؟)", reviewCount = 0, isMastered = false),
            Word(german = "Wo", arabic = "أين", example = "Wo wohnst du? (أين تسكن؟)", reviewCount = 0, isMastered = false),
            Word(german = "Wer", arabic = "من", example = "Wer ist da? (من هناك؟)", reviewCount = 0, isMastered = false),
            Word(german = "Gut", arabic = "جيد", example = "Mir geht es gut (أنا بخير)", reviewCount = 0, isMastered = false),
            Word(german = "Schlecht", arabic = "سيء", example = "Das ist nicht schlecht (هذا ليس سيئاً)", reviewCount = 0, isMastered = false)
        )
        wordDao.insertWords(initialWords)
    }

    fun loadWordsToReview() {
        viewModelScope.launch {
            _isLoading.value = true
            wordDao.getWordsToReview().first().let { words ->
                // Shuffle words to make review random
                _wordsList.value = words.shuffled()
                _currentIndex.value = 0
            }
            _isLoading.value = false
        }
    }

    fun markAsMastered(word: Word) {
        viewModelScope.launch {
            val updatedWord = word.copy(
                isMastered = true,
                reviewCount = word.reviewCount + 1
            )
            wordDao.updateWord(updatedWord)
            moveToNextWord()
        }
    }

    fun reviewLater(word: Word) {
        viewModelScope.launch {
            val updatedWord = word.copy(
                reviewCount = word.reviewCount + 1
            )
            wordDao.updateWord(updatedWord)
            moveToNextWord()
        }
    }

    private fun moveToNextWord() {
        _currentIndex.value = _currentIndex.value + 1
    }

    fun resetAllWords() {
        viewModelScope.launch {
            _isLoading.value = true
            val allWords = wordDao.getAllWords().first()
            val resetWords = allWords.map { it.copy(isMastered = false, reviewCount = 0) }
            wordDao.insertWords(resetWords)
            loadWordsToReview()
        }
    }
}
