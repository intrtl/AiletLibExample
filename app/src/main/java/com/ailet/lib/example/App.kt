package com.ailet.lib.example

import android.app.Application
import com.ailet.lib3.api.Ailet
import com.ailet.lib3.api.feature.AiletFeature
import com.ailet.lib3.domain.install.AiletLibInstallInfo
import com.ailet.lib3.feature.installinfo.HostAppInstallInfoProviderFeature
import com.ailet.lib3.feature.stockcamera.DefaultStockCameraFeature
import com.ailet.lib3.feature.techsupport.intercom.IntercomTechSupportManager
import org.conscrypt.BuildConfig

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Токен начальной авторизации, предоставленный командой Ailet
        val accessToken = "Ailet-access-token"

        // Инициализация библиотеки с вашим токеном и выбранными модулями
        Ailet.initialize(
            context = this,
            accessToken = accessToken,
            features = initializeFeatures()
        )
    }

    /**
     * Инициализация опциональных модулей функционала библиотеки
     */
    private fun initializeFeatures(): Set<AiletFeature> {
        return setOf(
            // Модуль стоковой камеры
            DefaultStockCameraFeature(),

            // Модуль техподдержки
            IntercomTechSupportManager(context = this),

            // модуль идентификации (поможет при диагностике проблем)
            HostAppInstallInfoProviderFeature(
                application = this,
                versionName = BuildConfig.VERSION_NAME,
                versionCode = BuildConfig.VERSION_CODE,
                ailetLibInstallInfo = AiletLibInstallInfo
            ),

            // Если в приложении пользователем дано разрешение на использование камеры
            // и при вызове экрана камеры Ailet он сразу же закрывается без ошибок (exceptions),
            // то добавьте в поле features дополнительный модуль
            /*
            DefaultAiletPermissionsFeature(
                excludedPermissions = setOf(AiletPermissionsFeature.Exclude.CAMERA)
            )
            */
        )
    }
}