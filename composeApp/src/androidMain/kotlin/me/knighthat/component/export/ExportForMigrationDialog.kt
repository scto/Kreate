package me.knighthat.component.export

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.core.text.isDigitsOnly
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.Cache
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import it.fast4x.rimusic.BuildConfig
import it.fast4x.rimusic.Database
import it.fast4x.rimusic.R
import it.fast4x.rimusic.enums.ExoPlayerCacheLocation
import it.fast4x.rimusic.service.MyDownloadHelper
import it.fast4x.rimusic.service.modern.PlayerServiceModern
import it.fast4x.rimusic.utils.exoPlayerCacheLocationKey
import it.fast4x.rimusic.utils.getEnum
import it.fast4x.rimusic.utils.preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.knighthat.component.ExportToFileDialog
import me.knighthat.utils.TimeDateUtils
import me.knighthat.utils.Toaster
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ExportForMigrationDialog(
    valueState: MutableState<TextFieldValue>,
    activeState: MutableState<Boolean>,
    launcher: ManagedActivityResultLauncher<String, Uri?>
): ExportToFileDialog(valueState, activeState, launcher) {

    companion object {
        @UnstableApi
        @Composable
        operator fun invoke(
            context: Context,
            binder: PlayerServiceModern.Binder?
        ): ExportForMigrationDialog =
            ExportForMigrationDialog(
                remember {
                    mutableStateOf( TextFieldValue() )
                },
                rememberSaveable { mutableStateOf(false) },
                rememberLauncherForActivityResult(
                    ActivityResultContracts.CreateDocument("application/zip" )
                ) { uri ->
                    // [uri] must be non-null (meaning path exists) in order to work
                    uri ?: return@rememberLauncherForActivityResult
                    // Same thing with binder
                    binder ?: return@rememberLauncherForActivityResult

                    Toaster.i( "Might take a few minutes" )

                    CoroutineScope( Dispatchers.IO ).launch {

                        fun actionOnFullyCached( cache: Cache, actionOnEachSpan: (String, ByteArray) -> Unit ) =
                            cache.keys.forEach { key ->
                                val contentLength = Database.formatContentLength( key )
                                // Only take fully cached songs to save space
                                if( !cache.isCached( key, 0, contentLength ) )
                                    return@forEach

                                cache.getCachedSpans( key ).forEach cacheSpan@ { span ->
                                    val file = span.file ?: return@cacheSpan

                                    val filePrefix =
                                        file.parentFile?.let {
                                            if( it.name.isDigitsOnly() )
                                                "${it.nameWithoutExtension}/"
                                            else
                                                null
                                        } ?: ""

                                    actionOnEachSpan( "${filePrefix}${file.name}", file.readBytes() )
                                }
                            }

                        context.contentResolver
                               .openOutputStream( uri )
                               ?.use { outStream ->     // Use [use] because it closes stream on exit
                                   ZipOutputStream( outStream ).use { zipOut ->
                                       //<editor-fold desc="Save cached songs">
                                       actionOnFullyCached( binder.cache ) { fileName, data ->
                                           zipOut.putNextEntry( ZipEntry("cached/$fileName") )
                                           zipOut.write( data )
                                           zipOut.closeEntry()
                                       }

                                       val cacheLocType = context.preferences.getEnum(
                                           exoPlayerCacheLocationKey, ExoPlayerCacheLocation.System
                                       )
                                       val cacheLocation =
                                           if (cacheLocType == ExoPlayerCacheLocation.Private)
                                               context.filesDir
                                           else
                                               context.cacheDir
                                       val cacheDir = cacheLocation.resolve("rimusic_cache")
                                       if( cacheDir.exists() )
                                           cacheDir.listFiles()?.forEach {
                                               if( it.extension != "uid" ) return@forEach

                                               zipOut.putNextEntry( ZipEntry("cached/${it.name}") )
                                               zipOut.write( it.readBytes() )
                                               zipOut.closeEntry()
                                           }
                                       //</editor-fold>

                                       //<editor-fold desc="Save downloaded songs">
                                       actionOnFullyCached( binder.downloadCache ) { fileName, data ->
                                           zipOut.putNextEntry( ZipEntry("downloaded/$fileName") )
                                           zipOut.write( data )
                                           zipOut.closeEntry()
                                       }

                                       val downloadDir = MyDownloadHelper.getDownloadDirectory( context )
                                                                               .resolve( "downloads" )
                                       if( downloadDir.exists() )
                                           downloadDir.listFiles()?.forEach {
                                               if( it.extension != "uid" ) return@forEach

                                               zipOut.putNextEntry( ZipEntry("downloaded/${it.name}") )
                                               zipOut.write( it.readBytes() )
                                               zipOut.closeEntry()
                                           }
                                       //</editor-fold>

                                       //<editor-fold desc="Save databases">
                                       zipOut.putNextEntry( ZipEntry("database.db") )
                                       Database.checkpoint()
                                       FileInputStream( Database.path() ).use { inStream ->
                                           inStream.copyTo( zipOut )
                                       }
                                       zipOut.closeEntry()

                                       val exoDatabase = context.getDatabasePath( "exoplayer_internal.db" )
                                       zipOut.putNextEntry( ZipEntry("exoplayer_internal.db") )
                                       zipOut.write( exoDatabase.readBytes() )
                                       zipOut.closeEntry()
                                       //</editor-fold>

                                       //<editor-fold desc="Save settings">
                                       zipOut.putNextEntry( ZipEntry("settings.csv") )
                                       csvWriter().open( zipOut ) {
                                           writeRow("Type", "Key", "Value")
                                           flush()

                                           context.preferences
                                                  .all
                                                  .map {
                                                      val value = it.value ?: Unit
                                                      val type = value::class.simpleName ?: "null"
                                                      Triple(type, it.key, value)
                                                  }
                                                  .filter { it.first != "null" && it.third !== Unit }
                                                  .forEach {
                                                      writeRow(it.first, it.second, it.third)
                                                      flush()
                                                  }
                                       }
                                       //</editor-fold>
                                   }
                               }
                        Toaster.done()
                    }
                }
            )
    }

    override val keyboardOption: KeyboardOptions = KeyboardOptions.Default
    override val dialogTitle: String
        @Composable
        get() = stringResource( R.string.title_name_your_export )

    override var value: TextFieldValue by valueState
    override var isActive: Boolean by activeState

    override fun defaultFileName(): String =
        "${BuildConfig.APP_NAME}_cache_${TimeDateUtils.localizedDateNoDelimiter()}_${TimeDateUtils.timeNoDelimiter()}"
}