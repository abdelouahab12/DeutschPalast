package com.deutschpalast.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "german_words")
data class Word(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val german: String,        // الكلمة بالألمانية (مثال: das Brot)
    val arabic: String,        // المعنى بالعربية (مثال: الخبز)
    val example: String = "",   // جملة توضيحية أو رابط ذهني لمساعدتك على الحفظ
    val reviewCount: Int = 0,  // كم مرة قمت بمراجعة الكلمة
    val isMastered: Boolean = false // هل تم حفظها تماماً أم تحتاج مراجعة؟
)
