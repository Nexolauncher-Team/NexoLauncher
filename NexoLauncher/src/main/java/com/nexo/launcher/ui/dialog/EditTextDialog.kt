package com.nexo.launcher.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.EditText
import androidx.annotation.CheckResult
import com.nexo.launcher.R
import com.nexo.launcher.databinding.DialogEditTextBinding
import com.nexo.launcher.ui.dialog.DraggableDialog.DialogInitializationListener
import com.nexo.launcher.utils.stringutils.StringUtilsKt.Companion.isEmptyOrBlank

class EditTextDialog private constructor(
    private val context: Context,
    private val title: String?,
    private val message: String?,
    private val editText: String?,
    private val hintText: String?,
    private val checkBox: String?,
    private val confirm: String?,
    private val emptyError: String?,
    private val showCheckBox: Boolean,
    private val inputType: Int,
    private val cancelListener: View.OnClickListener?,
    private val confirmListener: ConfirmListener?,
    private val required: Boolean
) : FullScreenDialog(context),
    DialogInitializationListener {
    private val binding = DialogEditTextBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setCancelable(false)
        this.setContentView(binding.root)

        init()
        DraggableDialog.initDialog(this)
    }

    private fun init() {
        binding.apply {
            title?.let { titleView.text = it }
            message?.let {
                messageView.text = it
                messageView.visibility = View.VISIBLE
            }
            editText?.let { textEdit.setText(it) }
            hintText?.let { textEdit.hint = it } ?: run {
                if (required) textEdit.setHint(R.string.generic_required)
            }

            checkHeight()

            confirm?.let { confirmButton.text = it }
            if (showCheckBox) {
                checkBox.visibility = View.VISIBLE
                checkBox.text = this@EditTextDialog.checkBox
            }
            if (inputType != -1) textEdit.inputType = inputType

            confirmListener?.let {
                confirmButton.setOnClickListener { _ ->
                    if (required) {
                        val text = textEdit.text.toString()
                        if (isEmptyOrBlank(text)) {
                            textEdit.error = emptyError ?: context.getString(R.string.generic_error_field_empty)
                            return@setOnClickListener
                        }
                    }
                    val dismissDialog = it.onConfirm(textEdit, checkBox.isChecked)
                    if (dismissDialog) dismiss()
                }
            }

            val cancelListener = cancelListener ?: View.OnClickListener { dismiss() }
            cancelButton.setOnClickListener(cancelListener)
        }
    }

    private fun checkHeight() {
        checkHeight(binding.root, binding.contentView, binding.scrollView)
    }

    override fun onInit(): Window? = window

    fun interface ConfirmListener {
        fun onConfirm(editText: EditText, checked: Boolean): Boolean
    }

    class Builder(private val context: Context) {
        private var title: String? = null
        private var message: String? = null
        private var editText: String? = null
        private var hintText: String? = null
        private var checkBox: String? = null
        private var confirm: String? = null
        private var emptyError: String? = null
        private var showCheckBox = false
        private var inputType = -1
        private var cancelListener: View.OnClickListener? = null
        private var confirmListener: ConfirmListener? = null
        private var required = false

        /**
         * è®¾ç½®å¼¹çª—çš„æ ‡é¢˜æ æ–‡æœ¬
         */
        @CheckResult
        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        /**
         * è®¾ç½®å¼¹çª—çš„æ ‡é¢˜æ æ–‡æœ¬
         */
        @CheckResult
        fun setTitle(title: Int): Builder {
            return setTitle(context.getString(title))
        }

        /**
         * è®¾ç½®å¼¹çª—çš„ä¿¡æ¯æ æ–‡æœ¬
         */
        @CheckResult
        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        /**
         * è®¾ç½®å¼¹çª—çš„ä¿¡æ¯æ æ–‡æœ¬
         */
        @CheckResult
        fun setMessage(message: Int): Builder {
            return setMessage(context.getString(message))
        }

        /**
         * è®¾ç½®è¾“å…¥æ¡†çš„æ–‡æœ¬
         */
        @CheckResult
        fun setEditText(editText: String): Builder {
            this.editText = editText
            return this
        }

        /**
         * è®¾ç½®è¾“å…¥æ¡†çš„Hintæç¤º
         */
        @CheckResult
        fun setHintText(hintText: Int): Builder {
            return setHintText(context.getString(hintText))
        }

        /**
         * è®¾ç½®è¾“å…¥æ¡†çš„Hintæç¤º
         */
        @CheckResult
        fun setHintText(hintText: String): Builder {
            this.hintText = hintText
            return this
        }

        /**
         * è®¾ç½®ç¡®è®¤æŒ‰é’®çš„æ–‡æœ¬
         */
        @CheckResult
        fun setConfirmText(text: Int): Builder {
            return setConfirmText(context.getString(text))
        }

        /**
         * è®¾ç½®ç¡®è®¤æŒ‰é’®çš„æ–‡æœ¬
         */
        @CheckResult
        fun setConfirmText(text: String): Builder {
            this.confirm = text
            return this
        }

        /**
         * éœ€è¦è®¾ç½®è¾“å…¥æ¡†ä¸ºå¿…å¡«æ—¶ï¼Œè‡ªå®šä¹‰å…¶ä¸ºç©ºæ—¶æŠ¥é”™æé†’çš„æ–‡æœ¬
         */
        @CheckResult
        fun setEmptyErrorText(text: Int): Builder {
            return setEmptyErrorText(context.getString(text))
        }

        /**
         * éœ€è¦è®¾ç½®è¾“å…¥æ¡†ä¸ºå¿…å¡«æ—¶ï¼Œè‡ªå®šä¹‰å…¶ä¸ºç©ºæ—¶æŠ¥é”™æé†’çš„æ–‡æœ¬
         */
        @CheckResult
        fun setEmptyErrorText(text: String): Builder {
            this.emptyError = text
            return this
        }

        /**
         * è®¾ç½®æ˜¯å¦å¯ç”¨å¼¹çª—çš„é€‰æ‹©æ¡†
         */
        @CheckResult
        fun setShowCheckBox(show: Boolean): Builder {
            this.showCheckBox = show
            return this
        }

        /**
         * è®¾ç½®é€‰æ‹©æ¡†çš„æ–‡æœ¬
         */
        @CheckResult
        fun setCheckBoxText(text: Int): Builder {
            return setCheckBoxText(context.getString(text))
        }

        /**
         * è®¾ç½®é€‰æ‹©æ¡†çš„æ–‡æœ¬
         */
        @CheckResult
        fun setCheckBoxText(text: String): Builder {
            this.checkBox = text
            return this
        }

        /**
         * è®¾ç½®è¾“å…¥æ¡†çš„ç±»åž‹
         */
        @CheckResult
        fun setInputType(inputType: Int): Builder {
            this.inputType = inputType
            return this
        }

        /**
         * è®¾ç½®å–æ¶ˆæŒ‰é’®çš„ç‚¹å‡»äº‹ä»¶
         */
        @CheckResult
        fun setCancelListener(cancel: View.OnClickListener): Builder {
            this.cancelListener = cancel
            return this
        }

        /**
         * è®¾ç½®ç¡®è®¤æŒ‰é’®çš„ç‚¹å‡»äº‹ä»¶
         */
        @CheckResult
        fun setConfirmListener(confirmListener: ConfirmListener): Builder {
            this.confirmListener = confirmListener
            return this
        }

        /**
         * è®¾ç½®ä¸ºå¿…å¡«ï¼Œå½“ç”¨æˆ·ç‚¹å‡»ç¡®è®¤æ—¶ï¼Œå°†æ£€æŸ¥è¾“å…¥æ¡†çš„å†…å®¹æ˜¯å¦ä¸ºç©ºï¼ˆåŒ…æ‹¬ç©ºæ ¼æ£€æŸ¥ï¼‰
         * å¦‚æžœæ˜¯ï¼Œé‚£ä¹ˆæ‹¦æˆªç‚¹å‡»äº‹ä»¶å¹¶å‘ŠçŸ¥ç”¨æˆ·
         */
        @CheckResult
        fun setAsRequired(): Builder {
            this.required = true
            return this
        }

        fun buildDialog(): EditTextDialog {
            return EditTextDialog(
                context,
                title, message, editText, hintText, checkBox, confirm, emptyError,
                showCheckBox, inputType,
                cancelListener, confirmListener,
                required
            ).apply {
                create()
            }
        }

        fun showDialog() {
            buildDialog().show()
        }
    }
}

