<div align="center">
![GitHub tag (latest SemVer)](https://img.shields.io/github/tag/dessalines/room-db-export-import.svg)
[![GitHub issues](https://img.shields.io/github/issues-raw/dessalines/rank-my-favs.svg)](https://github.com/dessalines/room-db-export-import/issues)
[![License](https://img.shields.io/github/license/dessalines/room-db-export-import.svg)](LICENSE)
![GitHub stars](https://img.shields.io/github/stars/dessalines/room-db-export-import?style=social)
</div>

# Room-DB-Export-Import

This library provides an easy way to import and export your jetpack compose app's room database to a zip file.

## Usage

Add the dependency to your app's `build.gradle`:

`implementation 'com.github.dessalines:room-db-export-import:VERSION'`

To export, you can do:

```kt
val dbHelper = RoomDBExportImport(AppDB.getDatabase(ctx).openHelper)

val exportDbLauncher =
    rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip"),
    ) {
        it?.also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dbHelper.export(ctx, it)
                Toast.makeText(ctx, dbSavedText, Toast.LENGTH_SHORT).show()
            }
        }
    }
...
```

To import:

```kt
val importDbLauncher =
    rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) {
        it?.also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dbHelper.import(ctx, it, true)
                Toast.makeText(ctx, dbRestoredText, Toast.LENGTH_SHORT).show()
            }
        }
    }

```

## Resources

- https://medium.com/@theAndroidDeveloper/creating-and-publishing-your-own-android-library-an-in-depth-guide-26de164c96c2
- https://github.com/technophilist/branded-buttons-compose
