package com.arkindustries.gogreen.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.arkindustries.gogreen.database.crossref.JobAttachmentCrossRef
import com.arkindustries.gogreen.database.crossref.JobCategoryCrossRef
import com.arkindustries.gogreen.database.crossref.JobSkillCrossRef
import com.arkindustries.gogreen.database.crossref.ProposalAttachmentCrossRef
import com.arkindustries.gogreen.database.crossref.ProposalJobCrossRef
import com.arkindustries.gogreen.database.crossref.UserJobCrossRef
import com.arkindustries.gogreen.database.crossref.UserProposalCrossRef
import com.arkindustries.gogreen.database.dao.AttachmentDao
import com.arkindustries.gogreen.database.dao.CategoryDao
import com.arkindustries.gogreen.database.dao.JobDao
import com.arkindustries.gogreen.database.dao.ProposalDao
import com.arkindustries.gogreen.database.dao.SkillDao
import com.arkindustries.gogreen.database.dao.UserDao
import com.arkindustries.gogreen.database.entites.AttachmentEntity
import com.arkindustries.gogreen.database.entites.CategoryEntity
import com.arkindustries.gogreen.database.entites.JobEntity
import com.arkindustries.gogreen.database.entites.ProposalEntity
import com.arkindustries.gogreen.database.entites.SkillEntity
import com.arkindustries.gogreen.database.entites.UserEntity
import com.arkindustries.gogreen.database.typeconvertor.LocationTypeConverter

@Database(
    entities = [
        JobEntity::class,
        ProposalEntity::class,
        CategoryEntity::class,
        SkillEntity::class,
        AttachmentEntity::class,
        UserEntity::class,
        JobAttachmentCrossRef::class,
        JobCategoryCrossRef::class,
        JobSkillCrossRef::class,
        ProposalAttachmentCrossRef::class,
        ProposalJobCrossRef::class,
        UserJobCrossRef::class,
        UserProposalCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(LocationTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun jobDao(): JobDao
    abstract fun proposalDao(): ProposalDao
    abstract fun categoryDao(): CategoryDao
    abstract fun skillDao(): SkillDao
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gogreen_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}