package com.nexo.launcher.context

import android.app.Activity
import android.app.Application
import android.content.Context
import android.widget.Toast
import com.nexo.launcher.task.TaskExecutors
import com.nexo.launcher.Tools
import com.nexo.launcher.lifecycle.ContextExecutorTask
import java.lang.ref.WeakReference

class ContextExecutor {
    companion object {
        private var sApplication: WeakReference<Application>? = null
        private var sActivity: WeakReference<Activity>? = null

        /**
         * Set the Application that will be used to execute tasks if the Activity won't be available.
         * @param application the application to use as the fallback
         */
        @JvmStatic
        fun setApplication(application: Application) {
            this.sApplication = WeakReference(application)
        }

        /**
         * Clear the Application previously set, so that ContextExecutor will notify the user of a critical error
         * that is executing code after the application is ended by the system.
         */
        @JvmStatic
        fun clearApplication() {
            this.sApplication?.clear()
        }

        /**
         * Set the Activity that this ContextExecutor will use for executing tasks
         * @param activity the activity to be used
         */
        @JvmStatic
        fun setActivity(activity: Activity) {
            this.sActivity = WeakReference(activity)
        }

        /**
         * Clear the Activity previously set, so the ContextExecutor won't use it to execute tasks.
         */
        @JvmStatic
        fun clearActivity() {
            this.sActivity?.clear()
        }

        /**
         * Schedules a ContextExecutorTask to be executed. For more info on tasks
         * @see ContextExecutorTask
         * @param task the task to be executed
         */
        @JvmStatic
        fun executeTask(task: ContextExecutorTask) {
            execute(
                activity = { activity ->
                    task.executeWithActivity(activity)
                },
                application = { application ->
                    task.executeWithApplication(application)
                }
            )
        }

        /**
         * å¿½ç•¥Contextæ˜¯æ¥è‡ªè°ï¼Œç›´æŽ¥ä½¿ç”¨è¿™ä¸ªContextæ‰§è¡Œä»»åŠ¡
         * @see AllContextExecutorTask
         * @param task æƒ³è¦æ‰§è¡Œçš„ä»»åŠ¡
         */
        @JvmStatic
        fun executeTaskWithAllContext(task: AllContextExecutorTask) {
            execute(
                activity = { task.execute(it) },
                application = { task.execute(it) }
            )
        }

        private fun execute(activity: (Activity) -> Unit, application: (Application) -> Unit) {
            TaskExecutors.runInUIThread {
                Tools.getWeakReference(this.sActivity)?.let {
                    activity(it)
                    return@runInUIThread
                }
                Tools.getWeakReference(this.sApplication)?.let {
                    application(it)
                    return@runInUIThread
                }
                throw RuntimeException("The Context has not been set!")
            }
        }

        /**
         * é€šè¿‡è¿™é‡Œä¿å­˜çš„ActivityèŽ·å¾—res string
         * å¦‚æžœActivityæ²¡æœ‰è®¾ç½®æˆ–è€…æ— æ³•æŸ¥æ‰¾å¯¹åº”çš„res stringï¼Œé‚£ä¹ˆå°±ä¼šæ‰¾åˆ°Applicationå°è¯•èŽ·å–
         * å¦‚æžœä»æ—§å¤±è´¥ï¼Œé‚£ä¹ˆå°±åªèƒ½æŽ¥å—æŠ¥é”™äº†
         */
        @JvmStatic
        fun getString(resId: Int): String {
            return (this.sActivity?.get()?.getString(resId) ?: this.sApplication?.get()?.getString(resId))!!
        }

        /**
         * åœ¨Javaè¯­è¨€ä¸­ï¼Œæƒ³è¦é€šè¿‡è¿™ä¸ªç±»æ¥å±•ç¤ºä¸€ä¸ªToastä¼šæ¯”è¾ƒå¤æ‚
         * è¿™ä¸ªå‡½æ•°å°±æ˜¯ç”¨æ¥è§£å†³è¿™ä¸ªç—›ç‚¹çš„XD
         * @param resId è¦å±•ç¤ºçš„æ–‡æœ¬çš„ res ID
         * @param duration æ—¶é•¿ LENGTH_SHORT LENGTH_LONGï¼Œä¸Žå®˜æ–¹ä¸€è‡´
         */
        @JvmStatic
        fun showToast(resId: Int, duration: Int) {
            executeTaskWithAllContext { context -> Toast.makeText(context, context.getString(resId), duration).show() }
        }

        /**
         * åœ¨Javaè¯­è¨€ä¸­ï¼Œæƒ³è¦é€šè¿‡è¿™ä¸ªç±»æ¥å±•ç¤ºä¸€ä¸ªToastä¼šæ¯”è¾ƒå¤æ‚
         * è¿™ä¸ªå‡½æ•°å°±æ˜¯ç”¨æ¥è§£å†³è¿™ä¸ªç—›ç‚¹çš„XD
         * @param string è¦å±•ç¤ºçš„æ–‡æœ¬
         * @param duration æ—¶é•¿ LENGTH_SHORT LENGTH_LONGï¼Œä¸Žå®˜æ–¹ä¸€è‡´
         */
        @JvmStatic
        fun showToast(string: String, duration: Int) {
            executeTaskWithAllContext { context -> Toast.makeText(context, string, duration).show() }
        }

        /**
         * å°è¯•èŽ·å–Activity
         * @throws RuntimeException å¦‚æžœActivityä¸å­˜åœ¨ï¼Œé‚£ä¹ˆå°†æŠ›å‡ºå¼‚å¸¸
         */
        @JvmStatic
        fun getActivity(): Activity {
            return this.sActivity?.get() ?: throw RuntimeException("Activity does not exist.")
        }

        /**
         * å°è¯•èŽ·å–Application
         * @throws RuntimeException å¦‚æžœApplicationä¸å­˜åœ¨ï¼Œé‚£ä¹ˆå°†æŠ›å‡ºå¼‚å¸¸
         */
        @JvmStatic
        fun getApplication(): Application {
            return this.sApplication?.get() ?: throw RuntimeException("Application does not exist.")
        }
    }

    /**
     * A AllContextExecutorTask is a task that can dynamically change its behaviour, based on the context
     * used for its execution. This can be used to implement for ex. error/finish notifications from
     * background threads that may live with the Service after the activity that started them died.
     */
    fun interface AllContextExecutorTask {
        /**
         * å°†ä¼šä¼´éšç€Activityæˆ–è€…æ˜¯Applicationçš„Contextæ‰§è¡Œçš„ä»»åŠ¡
         * @param context Activityæˆ–è€…æ˜¯Applicationçš„Context
         */
        fun execute(context: Context)
    }
}
