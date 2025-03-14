package me.knighthat.component.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import it.fast4x.rimusic.R
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.typography
import it.fast4x.rimusic.utils.medium
import it.fast4x.rimusic.utils.rememberPreference
import me.knighthat.component.dialog.Dialog
import me.knighthat.component.dialog.InteractiveDialog
import me.knighthat.utils.Repository

object DeprecatedVersionDialog: Dialog {

    override val dialogTitle: String
        @Composable
        get() = "This app is deprecated!"

    var isCancelled: Boolean by mutableStateOf(false)
    override var isActive: Boolean by mutableStateOf(false)

    @Composable
    fun Buttons() = Column {
        var isReminderDisabled by rememberPreference( "disableDeprecatedVersionReminder", false )

        BasicText(
            text = "Disable reminder forever",
            style = typography().xs
                                .medium
                                .copy(
                                    color = colorPalette().text,
                                    textAlign = TextAlign.Center
                                ),
            modifier = InteractiveDialog.ButtonModifier()
                                        .fillMaxWidth( .98f )
                                        .background( Color(android.graphics.Color.RED).copy(alpha = .6f) )
                                        .padding( vertical = 10.dp )
                                        .clickable {
                                            isReminderDisabled = true
                                            hideDialog()
                                        }
        )

        Spacer( Modifier.height( 5.dp ) )

        BasicText(
            text = stringResource( R.string.click_to_close ),
            style = typography().xs
                                .medium
                                .copy(
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                ),
            modifier = InteractiveDialog.ButtonModifier()
                .fillMaxWidth( .98f )
                .border(
                    width = 2.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(20)
                )
                .padding( vertical = 10.dp )
                .clickable( onClick = ::hideDialog )
        )
    }

    override fun hideDialog() {
        isCancelled = true
        super.hideDialog()
    }

    @Composable
    override fun DialogBody() {
        Column(
            Modifier.padding( all = 0.dp )
                .padding( horizontal = 20.dp )
        ) {
            BasicText(
                text = "Kreate is now published under different ID, meaning, this app won't be able to update like usual.",
                style = typography().xs.copy(
                    color = colorPalette().text
                ),
                modifier = Modifier.padding( bottom = 5.dp )
            )

            val credit = buildAnnotatedString {
                append( "Fortunately, " )

                pushStringAnnotation( tag = "credit", annotation = "goToUrl" )
                val style = SpanStyle(
                    color = colorPalette().accent,
                    fontWeight = FontWeight.Bold
                )
                withStyle(style) {
                    append( "@knighthat" )
                }
                pop()

                append( " introduced a method to migrate your data to new app effortlessly." )

                addLink(
                    url = LinkAnnotation.Url("${Repository.GITHUB}/${Repository.OWNER}"),
                    start = 14,
                    end = 23
                )
            }
            BasicText(
                text =  credit,
                style = typography().xs.copy(
                    color = colorPalette().text
                ),
                modifier = Modifier.padding( vertical = 5.dp )
            )

            val article = buildAnnotatedString {
                append( "Click " )

                pushStringAnnotation( tag = "article", annotation = "goToUrl" )
                val style = SpanStyle(
                    color = Color(30, 129, 176),
                    fontWeight = FontWeight.Bold
                )
                withStyle(style) {
                    append( "here" )
                }
                pop()

                append( " for more details about the migration procedure." )

                addLink(
                    url = LinkAnnotation.Url("https://kreate.knighthat.me/usr/how-to-migrate"),
                    start = 7,
                    end = 10
                )
            }
            BasicText(
                text =  article,
                style = typography().xs.copy(
                    color = colorPalette().text
                ),
                modifier = Modifier.padding(
                    top = 5.dp,
                    bottom = Dialog.SPACE_BETWEEN_SECTIONS.dp
                )
            )

            Buttons()
        }
    }
}