package com.jarves.mh.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarves.mh.R

enum class PhosphorIconType(@DrawableRes val resId: Int) {
    HOUSE(R.drawable.ph_house),
    CHAT_CIRCLE_DOTS(R.drawable.ph_chat_circle_dots),
    FOLDER_SIMPLE(R.drawable.ph_folder_simple),
    TERMINAL(R.drawable.ph_terminal),
    GEAR_SIX(R.drawable.ph_gear_six),
    PLUS(R.drawable.ph_plus),
    SPARKLE(R.drawable.ph_sparkle),
    CHECK_CIRCLE(R.drawable.ph_check_circle),
    X_CIRCLE(R.drawable.ph_x_circle),
    WARNING_CIRCLE(R.drawable.ph_warning_circle),
    PAPERCLIP(R.drawable.ph_paperclip),
    PAPER_PLANE_RIGHT(R.drawable.ph_paper_plane_right),
    ARROW_CLOCKWISE(R.drawable.ph_arrow_clockwise),
    ARROW_SQUARE_OUT(R.drawable.ph_arrow_square_out),
    GIT_BRANCH(R.drawable.ph_git_branch),
    GIT_COMMIT(R.drawable.ph_git_commit),
    HAMMER(R.drawable.ph_hammer),
    MAGNIFYING_GLASS(R.drawable.ph_magnifying_glass),
    SHIELD_CHECK(R.drawable.ph_shield_check),
    LOCK_KEY(R.drawable.ph_lock_key),
    FINGERPRINT(R.drawable.ph_fingerprint),
    PLAY(R.drawable.ph_play),
    CARET_DOWN(R.drawable.ph_caret_down),
    CARET_RIGHT(R.drawable.ph_caret_right),
    TRASH(R.drawable.ph_trash),
    PENCIL_SIMPLE(R.drawable.ph_pencil_simple),
    DOWNLOAD_SIMPLE(R.drawable.ph_download_simple),
    UPLOAD_SIMPLE(R.drawable.ph_upload_simple),
    CODE(R.drawable.ph_code),
    FILE_CODE(R.drawable.ph_file_code),
    FILE_TEXT(R.drawable.ph_file_text),
    USER_CIRCLE(R.drawable.ph_user_circle),
    GLOBE(R.drawable.ph_globe),
    CPU(R.drawable.ph_cpu),
    BELL(R.drawable.ph_bell),
    CAMERA(R.drawable.ph_camera),
    IMAGE(R.drawable.ph_image),
    BUG(R.drawable.ph_bug),
}

@Composable
fun PhosphorIcon(
    icon: PhosphorIconType,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 24.dp,
    tint: Color = LocalContentColor.current,
) {
    Icon(
        painter = painterResource(id = icon.resId),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint,
    )
}

@Composable
fun PhosphorIcon(
    @DrawableRes resId: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 24.dp,
    tint: Color = LocalContentColor.current,
) {
    Icon(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint,
    )
}
