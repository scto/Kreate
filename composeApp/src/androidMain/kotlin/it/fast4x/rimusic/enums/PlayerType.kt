package it.fast4x.rimusic.enums

import androidx.annotation.StringRes
import app.kreate.android.R
import me.knighthat.enums.TextView

enum class PlayerType(
    @field:StringRes override val textId: Int
): TextView {

    Essential( R.string.pcontrols_modern ),
    Modern( R.string.pcontrols_essential );
}