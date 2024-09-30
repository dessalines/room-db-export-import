package com.roomdbexportimport

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.sqlite.db.SupportSQLiteOpenHelper
import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.system.exitProcess

/**
 * Provides some helper functions to export and import a room database.
 */
class RoomDBExportImport(
    val db: SupportSQLiteOpenHelper,
) {
    /**
* Exports a database to a zip file
*/
    @RequiresApi(Build.VERSION_CODES.O)
    fun export(
        ctx: Context,
        backupFileUri: Uri,
    ) {
        try {
            val dbFiles = dbFiles()
            checkpoint()

            // Create a backup zip file
            ZipFile(dbFiles.backupZipFile).addFiles(listOf(dbFiles.dbFile, dbFiles.walFile, dbFiles.shmFile))

            // Copy the tmp file to the other one
            val os = ctx.contentResolver.openOutputStream(backupFileUri)!!
            Files.copy(dbFiles.backupZipFile?.toPath(), os)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Imports a database from a zip file
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun import(
        ctx: Context,
        backupFileUri: Uri,
        restart: Boolean = true,
    ) {
        try {
            val dbFiles = dbFiles()

            // Delete the backup zip file first
            dbFiles.backupZipFile?.delete()

            // Copy the input file to the tmp file
            val inputStream = ctx.contentResolver.openInputStream(backupFileUri)!!
            Files.copy(inputStream, dbFiles.backupZipFile?.toPath())

            // Delete the other DB files
            dbFiles.dbFile.delete()
            dbFiles.walFile?.delete()
            dbFiles.shmFile?.delete()

            // Extract them
            ZipFile(dbFiles.backupZipFile).extractAll(dbFiles.dbFile.parent!!)
            checkpoint()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (restart) {
            val i = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
            i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            ctx.startActivity(i)
            exitProcess(0)
        }
    }

    private fun checkpoint() {
        val db = db.writableDatabase
        db.query("PRAGMA wal_checkpoint(FULL);")
        db.query("PRAGMA wal_checkpoint(TRUNCATE);")
    }

    data class DbFiles(
        val dbFile: File,
        val walFile: File?,
        val shmFile: File?,
        val backupZipFile: File?,
    )

    private fun dbFiles() =
        DbFiles(
            dbFile = File(db.readableDatabase.path!!),
            walFile = File(db.readableDatabase.path + "-wal"),
            shmFile = File(db.readableDatabase.path + "-shm"),
            backupZipFile = File(db.readableDatabase.path + ".zip"),
        )
}
