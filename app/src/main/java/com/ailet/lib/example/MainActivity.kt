package com.ailet.lib.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ailet.lib3.api.Ailet
import com.ailet.lib3.api.client.method.domain.getreports.AiletMethodGetReports
import com.ailet.lib3.api.client.method.domain.requestsynccatalogs.AiletMethodSyncCatalogs
import com.ailet.lib3.api.client.method.domain.start.AiletMethodStart
import com.ailet.lib3.api.data.model.auth.AiletServer
import com.ailet.lib3.api.data.model.retailTasks.AiletRetailTaskSceneTypeShort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerBroadcastReceiver()

        val coroutineScope = CoroutineScope(Dispatchers.IO)

        findViewById<Button>(R.id.btnInit).setOnClickListener {
            enableButtons(isEnabled = false)
            initMultiPortal()

//            coroutineScope.launch {
//                initMultiPortalExecuteBlocking()
//            }
        }

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            enableButtons(isEnabled = false)
            start()
        }

        findViewById<Button>(R.id.btnReports).setOnClickListener {
            enableButtons(isEnabled = false)
            getReports()
        }

        findViewById<Button>(R.id.btnShowSummaryReport).setOnClickListener {
            enableButtons(isEnabled = false)
            showSummaryReport()
        }

        findViewById<Button>(R.id.btnShowVisit).setOnClickListener {
            enableButtons(isEnabled = false)
            showVisit()
        }

        findViewById<Button>(R.id.btnFinishVisit).setOnClickListener {
            enableButtons(isEnabled = false)
            finishVisit()
        }

        findViewById<Button>(R.id.btnSetPortal).setOnClickListener {
            enableButtons(isEnabled = false)
            coroutineScope.launch {
                val userServers = getUserServersExecuteBlocking()
                userServers.firstOrNull()?.let { server ->
                    setPortal(serverName = server.getPortalName())
                }
            }
        }

        findViewById<Button>(R.id.btnSyncCatalogs).setOnClickListener {
            enableButtons(isEnabled = false)
            requestSyncCatalogs()
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            enableButtons(isEnabled = false)
            logout()
        }
    }

    private fun registerBroadcastReceiver() {
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                parseBroadcastMessage(intent)
            }
        }

        ContextCompat.registerReceiver(
            this,
            broadcastReceiver,
            IntentFilter(AILET_BROADCAST),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun init() {
        Ailet.getClient().init(
            login = AILET_USER_LOGIN,
            password = AILET_USER_PASWORD,
            externalUserId = EXTERNAL_USER_ID,
            multiPortalMode = false,
            server = null
        ).execute(
            success = {
                // Библиотека готова для работы
                Toast.makeText(
                    applicationContext,
                    "Метод init() успешно выполнен",
                    Toast.LENGTH_LONG
                ).show()
                enableButtons(isEnabled = true)
            },
            error = {
                proceedError(throwable = it)
                // Обработка ошибки
            }
        )
    }

    private fun initMultiPortal() {
        getUserServers { userServers ->
            val selectedServer = userServers.firstOrNull { it.getPortalName().contains(PORTAL) }
            selectedServer?.let { server ->
                initToServer(userServer = server)
            } ?: proceedError("Не найден портал в списке порталов")
        }
    }

    /**
     * Асинхронное выполнение ailetCall()
     */
    private fun getUserServers(onDone: (List<AiletServer>) -> Unit) {
        Ailet.getClient().getServers(
            login = AILET_USER_LOGIN,
            password = AILET_USER_PASWORD,
            externalUserId = EXTERNAL_USER_ID
        ).execute(
            success = { result ->
                // Список порталов пользователя
                val userServers = result.servers
                onDone.invoke(userServers)
            },
            error = {
                proceedError(throwable = it)
                // Обработка ошибки
            }
        )
    }

    suspend fun initMultiPortalExecuteBlocking() {
        val job = GlobalScope.launch(Dispatchers.IO) {
            val userServers = getUserServersExecuteBlocking()
            val selectedServer = userServers.firstOrNull { it.getPortalName().contains(PORTAL) }

            selectedServer?.let { server ->
                initToServer(userServer = server)
            } ?: proceedError("Не найден портал в списке порталов")
        }
        job.join();
    }

    /**
     * Синхронное выполнение ailetCall()
     */
    private fun getUserServersExecuteBlocking(): List<AiletServer> {
        return Ailet.getClient().getServers(
            login = AILET_USER_LOGIN,
            password = AILET_USER_PASWORD,
            externalUserId = EXTERNAL_USER_ID
        ).executeBlocking().servers
    }

    private fun initToServer(userServer: AiletServer) {
        Ailet.getClient().init(
            login = AILET_USER_LOGIN,
            password = AILET_USER_PASWORD,
            externalUserId = EXTERNAL_USER_ID,
            multiPortalMode = true,
            server = userServer
        ).execute(
            success = {
                // Библиотека готова для работы
                Toast.makeText(
                    applicationContext,
                    "Метод init() успешно выполнен",
                    Toast.LENGTH_LONG
                ).show()
                enableButtons(isEnabled = true)
            },
            error = {
                proceedError(throwable = it)
                // Обработка ошибки
            }
        )
    }

    private fun start(
        taskId: String? = null,
        sceneGroupId: Int? = null,
        sceneTypes: List<AiletRetailTaskSceneTypeShort> = listOf()
    ) {
        Ailet.getClient().start(
            storeId = AiletMethodStart.StoreId.External(
                externalStoreId = STORE_ID
            ),
            externalVisitId = VISIT_ID,
            taskId = taskId,
            sceneTypes = sceneTypes,
//            visitType = VISIT_TYPE_BEFORE,
            retailTaskId = null,
            retailTaskIterationUuid = null,
            retailTaskActionId = null,
            visitUuid = null,
            sfaVisitUuid = null,
            sceneGroupId = sceneGroupId,
            launchConfig = AiletMethodStart.LaunchConfig()
        ).execute(success = {
            // Открыт экран визита
            enableButtons(isEnabled = true)
        }, error = {
            proceedError(throwable = it)
            // Обработка ошибки
        })
    }

    private fun getReports() {
        Ailet.getClient().getReports(
            externalVisitId = VISIT_ID,
            taskId = TASK_ID,
//          visitType = VISIT_TYPE_BEFORE
        ).execute(success = { result ->
            if (result is AiletMethodGetReports.Result.JsonString) {
                val reportsJson = result.json
                Toast.makeText(
                    applicationContext,
                    "Отчеты по визиту в формате json получены",
                    Toast.LENGTH_LONG
                ).show()
            }
            enableButtons(isEnabled = true)
        }, error = {
            proceedError(throwable = it)
            // Обработка ошибки
        })
    }

    private fun showSummaryReport() {
        Ailet.getClient().showSummaryReport(
            externalVisitId = VISIT_ID,
            taskId = TASK_ID,
//          visitType = VISIT_TYPE_BEFORE
        ).execute(success = {
            // Открыт экран сводного отчета
            enableButtons(isEnabled = true)
        }, error = {
            proceedError(throwable = it)
            // Обработка ошибки
        })
    }

    private fun requestSyncCatalogs() {
        Ailet.getClient().requestSyncCatalogs(
            syncMode = AiletMethodSyncCatalogs.SyncMode.SOFT,
            strategy = AiletMethodSyncCatalogs.Strategy.SyncRightNow
        ).execute(success = {
            // Синхронизация необходимых для работы справочников завершена
            Toast.makeText(
                applicationContext,
                "Синхронизация необходимых для работы справочников завершена",
                Toast.LENGTH_LONG
            ).show()
            enableButtons(isEnabled = true)
        }, error = {
            proceedError(throwable = it)
            // Обработка ошибки
        })
    }

    private fun showVisit() {
        Ailet.getClient().showVisit(
            externalVisitId = VISIT_ID
        ).execute(success = {
            // Открыт экран просмотра фото визита
            enableButtons(isEnabled = true)
        }, error = {
            proceedError(throwable = it)
            // Обработка ошибки
        })
    }

    private fun finishVisit() {
        Ailet.getClient().finishVisit(
            externalVisitId = VISIT_ID
        ).execute(success = {
            // Визит завершен
            Toast.makeText(
                applicationContext,
                "Визит завершен",
                Toast.LENGTH_LONG
            ).show()
            enableButtons(isEnabled = true)
        }, error = {
            proceedError(throwable = it)
            // Обработка ошибки
        })
    }

    private fun setPortal(serverName: String) {
        Ailet.getClient().setPortal(
            portalName = serverName
        ).execute(success = {
            // Активный портал установлен
            enableButtons(isEnabled = true)
        }, error = {
            proceedError(throwable = it)
            // Обработка ошибки
        })
    }

    private fun logout() {
        Ailet.getClient().logout().execute(success = {
            // Выполнен выход пользователя
            Toast.makeText(
                applicationContext,
                "Выполнен выход пользователя",
                Toast.LENGTH_LONG
            ).show()
            enableButtons(isEnabled = true)
        }, error = {
            proceedError(throwable = it)
            // Обработка ошибки
        })
    }

    private fun parseBroadcastMessage(intent: Intent) {
        val extras = intent.extras
        val visitId = extras?.getString(VISIT_ID, NOT_SET)
        val internalVisitId = extras?.getString(INTERNAL_VISIT_ID, NOT_SET)
        val storeId = extras?.getString(STORE_ID, NOT_SET)
        val taskId = extras?.getString(TASK_ID, NOT_SET)
        val totalPhotos = extras?.getString(TOTAL_PHOTOS, NOT_SET)
        val completedPhotos = extras?.getString(COMPLETED_PHOTOS, NOT_SET)
        val result = extras?.getString(RESULT, null)

        result?.let { uriString ->
            try {
                val fileFromUri = readFromUri(Uri.parse(uriString))
                Toast.makeText(
                    applicationContext,
                    "Получен broadcast готовности отчета по визиту",
                    Toast.LENGTH_LONG
                ).show()
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    private fun readFromUri(uri: Uri): String? {
        val inputStream = contentResolver.openInputStream(uri)
        val isReader = InputStreamReader(inputStream)
        val reader = BufferedReader(isReader)
        val sb = StringBuffer()
        return try {
            var str: String?
            while (reader.readLine().also { str = it } != null) {
                sb.append(str)
            }
            sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            reader.close()
            isReader.close()
            inputStream?.close()
        }
    }

    private fun proceedError(throwable: Throwable) {
        proceedError(message = throwable.message ?: throwable.localizedMessage)
    }

    private fun proceedError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        enableButtons(isEnabled = true)
    }

    private fun enableButtons(isEnabled: Boolean) {
        findViewById<Button>(R.id.btnInit).isEnabled = isEnabled
        findViewById<Button>(R.id.btnStart).isEnabled = isEnabled
        findViewById<Button>(R.id.btnReports).isEnabled = isEnabled
        findViewById<Button>(R.id.btnShowSummaryReport).isEnabled = isEnabled
        findViewById<Button>(R.id.btnShowVisit).isEnabled = isEnabled
        findViewById<Button>(R.id.btnFinishVisit).isEnabled = isEnabled
        findViewById<Button>(R.id.btnSetPortal).isEnabled = isEnabled
        findViewById<Button>(R.id.btnSyncCatalogs).isEnabled = isEnabled
        findViewById<Button>(R.id.btnLogout).isEnabled = isEnabled
    }

    private companion object {
        // User credentials
        private const val AILET_USER_LOGIN = "Ailet_user_login"
        private const val AILET_USER_PASWORD = "Ailet_user_password"
        private const val EXTERNAL_USER_ID = "External_user_id"

        private const val PORTAL = "project"

        private const val NOT_SET = "not set"
        private const val VISIT_ID = "external_visit_id"
        private const val INTERNAL_VISIT_ID = "internal_visit_id"
        private const val STORE_ID = "external_store_id"
        private const val TASK_ID = "task_id"
        private const val TOTAL_PHOTOS = "total_photos"
        private const val COMPLETED_PHOTOS = "completed_photos"
        private const val RESULT = "result"
        private const val VISIT_TYPE_BEFORE = "before"
        private const val VISIT_TYPE_AFTER = "after"

        // Broadcast filter
        private const val AILET_BROADCAST = "com.ailet.app.BROADCAST_WIDGETS_RECEIVED"
    }
}