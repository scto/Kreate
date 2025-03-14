package me.knighthat.component.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import it.fast4x.rimusic.R
import it.fast4x.rimusic.appContext
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.typography
import org.intellij.lang.annotations.MagicConstant

abstract class TextInputDialog(
    @MagicConstant(valuesFromClass = InputDialogConstraints::class)
    constraint: String
): InputDialog {

    private val constraintRegex: Regex = Regex( constraint )

    /**
     * Whether the [value] is allow to be empty
     * when [onSet] is called.
     */
    open val allowEmpty: Boolean = false

    var errorMessage: String by mutableStateOf("")

    override fun onValueChanged(newValue: String): Boolean {
        val result = newValue.matches( constraintRegex )
        if( !result )
            errorMessage = appContext().resources.getString( R.string.invalid_input )
        else
            errorMessage = ""

        return result
    }

    override fun onSet( newValue: String ) {
        if( !allowEmpty && newValue.isEmpty() )
            errorMessage = appContext().resources.getString( R.string.value_cannot_be_empty )
    }

    @Composable
    override fun DialogBody() {
        TextField(
            value = value,
            onValueChange = {
                if( onValueChanged( it.text ) )
                    value = it
            },
            placeholder = { TextPlaceholder() },
            maxLines = 1,
            keyboardOptions = keyboardOption,
            leadingIcon = { LeadingIcon() },
            trailingIcon = { TrailingIcon() },
            modifier = Modifier.fillMaxWidth(),
            colors = InputDialog.defaultTextFieldColors()
                                .copy(
                                    errorTextColor = colorPalette().text,
                                    errorContainerColor = colorPalette().background1,
                                    errorIndicatorColor = Color.Red
                                ),
            isError = errorMessage.isNotEmpty()
        )

        AnimatedVisibility(
            visible = errorMessage.isNotEmpty(),
            enter = fadeIn() + slideInHorizontally() + expandIn(),
        ) {
            BasicText(
                text = errorMessage,
                style = typography().xs.copy( color = Color(android.graphics.Color.RED) ),
                modifier = Modifier.fillMaxWidth( .7f )
                                    .padding( top = Dialog.VERTICAL_PADDING.dp )
            )
        }
    }
}