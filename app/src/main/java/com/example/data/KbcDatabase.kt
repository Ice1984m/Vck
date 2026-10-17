package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction as RoomTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface KbcDao {
    // Accounts
    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: String): Account?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)

    @Query("UPDATE accounts SET balance = :newBalance WHERE id = :id")
    suspend fun updateAccountBalance(id: String, newBalance: Double)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    // Api Certificates
    @Query("SELECT * FROM api_certificates ORDER BY createdAt DESC")
    fun getAllCertificates(): Flow<List<ApiCertificate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: ApiCertificate)

    @Query("DELETE FROM api_certificates WHERE id = :id")
    suspend fun deleteCertificateById(id: Long)

    @Query("UPDATE api_certificates SET isActive = 0")
    suspend fun deactivateAllCertificates()

    @Query("UPDATE api_certificates SET isActive = 1 WHERE id = :id")
    suspend fun activateCertificateById(id: Long)

    @RoomTransaction
    suspend fun selectActiveCertificate(id: Long) {
        deactivateAllCertificates()
        activateCertificateById(id)
    }
}

@Database(entities = [Account::class, Transaction::class, ApiCertificate::class], version = 1, exportSchema = false)
abstract class KbcDatabase : RoomDatabase() {
    abstract fun kbcDao(): KbcDao

    companion object {
        @Volatile
        private var INSTANCE: KbcDatabase? = null

        fun getDatabase(context: Context): KbcDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KbcDatabase::class.java,
                    "kbc_banking_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
