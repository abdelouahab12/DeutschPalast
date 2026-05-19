package com.deutschpalast.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    // جلب كل الكلمات المخزنة
    @Query("SELECT * FROM german_words")
    fun getAllWords(): Flow<List<Word>>

    // جلب الكلمات التي لم يتم إتقانها بعد لمراجعتها
    @Query("SELECT * FROM german_words WHERE isMastered = 0 LIMIT 100")
    fun getWordsToReview(): Flow<List<Word>>

    // إضافة كلمة جديدة أو حزمة كلمات
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<Word>)

    // تحديث حالة الكلمة (إذا حفظتها واجتزت الاختبار)
    @Update
    suspend fun updateWord(word: Word)
}
