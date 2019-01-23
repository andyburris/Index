package com.andb.apps.todo

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import com.andb.apps.todo.utilities.Utilities
import com.cuneytayyildiz.onboarder.OnboarderActivity
import com.cuneytayyildiz.onboarder.OnboarderPage
import com.cuneytayyildiz.onboarder.utils.OnboarderPageChangeListener
import com.jaredrummler.cyanea.Cyanea
import java.util.*

class Onboarding : OnboarderActivity(), OnboarderPageChangeListener {

    var selectedPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val introImageTinted = getDrawable(R.drawable.ic_face_black_24dp).mutate().also { it.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP) }
        val taskImageTinted = getDrawable(R.drawable.ic_done_white_24dp).mutate()
        val tagImageTinted = getDrawable(R.drawable.ic_label_black_24dp).mutate().also { it.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP) }
        val foldersImageTinted = getDrawable(R.drawable.ic_folder_black_24dp).mutate().also { it.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP) }
        val projectImageTinted = getDrawable(R.drawable.ic_menu_slideshow).mutate().also { it.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP) }


        val pages: List<OnboarderPage> = Arrays.asList(
                defaultBuilder()
                        .imageResource(introImageTinted)
                        .titleResourceId(R.string.onboarding_intro_screen_title)
                        .descriptionResourceId(R.string.onboarding_intro_screen_description)
                        .backgroundColor(Utilities.lighterDarker(Cyanea.instance.accentDark, 0.8f))
                        .build()
                ,
                defaultBuilder()
                        .imageResource(taskImageTinted)
                        .titleResourceId(R.string.onboarding_task_screen_title)
                        .descriptionResourceId(R.string.onboarding_task_screen_description)
                        .backgroundColor(Cyanea.instance.accentDark)
                        .build()
                ,
                defaultBuilder()
                        .imageResource(tagImageTinted)
                        .titleResourceId(R.string.onboarding_tag_screen_title)
                        .descriptionResourceId(R.string.onboarding_tag_screen_description)
                        .backgroundColor(Cyanea.instance.accent)
                        .build()
                ,
                defaultBuilder()
                        .imageResource(foldersImageTinted)
                        .titleResourceId(R.string.onboarding_folders_screen_title)
                        .descriptionResourceId(R.string.onboarding_folders_screen_description)
                        .backgroundColor(Cyanea.instance.accentLight)
                        .build()
                ,
                defaultBuilder()
                        .imageResource(projectImageTinted)
                        .titleResourceId(R.string.onboarding_project_screen_title)
                        .descriptionResourceId(R.string.onboarding_project_screen_description)
                        .backgroundColor(Utilities.lighterDarker(Cyanea.instance.accentLight, 1.2f))
                        .build()
        )

        shouldDarkenButtonsLayout(true)
        setFinishButtonBackgroundColor(R.color.statusBarTransparent)
        setSkipButtonBackgroundColor(R.color.statusBarTransparent)
        setNextButtonBackgroundColor(R.color.statusBarTransparent)
        //setDividerHeight(1)

        setOnboarderPageChangeListener(this)
        initOnboardingPages(pages)
    }

    fun defaultBuilder() = OnboarderPage.Builder()
            .titleTextSize(36f)
            .descriptionTextSize(18f)
            .imageSizeDp(152, 152)
            .imageBias(.8f)
            .textPaddingBottomDp(128)

    override fun onSkipButtonPressed() {
        if (selectedPage == 0) {
            super.onSkipButtonPressed()
        } else {
            selectedPage--
            setPage(selectedPage)
        }
    }


    override fun onFinishButtonPressed() {
        finish()
    }

    override fun onPageChanged(position: Int) {
        selectedPage = position
        if (selectedPage == 0) {
            setSkipButtonTitle("Skip")
        } else {
            setSkipButtonTitle("Previous")
        }
    }
}