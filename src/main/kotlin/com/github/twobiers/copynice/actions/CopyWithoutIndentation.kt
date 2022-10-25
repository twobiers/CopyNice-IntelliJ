package com.github.twobiers.copynice.actions

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.LanguageIndentStrategy
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.DocumentUtil
import java.awt.datatransfer.StringSelection
import java.io.BufferedReader
import java.io.StringReader


class CopyWithoutIndentation : AnAction() {
  override fun update(e: AnActionEvent) {
    super.update(e)
    e.presentation.isVisible = e.presentation.isVisible
            && !EditorUtil.contextMenuInvokedOutsideOfSelection(e)
  }

  override fun actionPerformed(e: AnActionEvent) {
    val editor = e.getData(CommonDataKeys.EDITOR) ?: return
    val project = e.project ?: return
    val document = editor.document

    val selectionStart = editor.selectionModel.selectionStart
    val selectedText = editor.selectionModel.selectedText ?: return


    val startLineNumber = document.getLineNumber(selectionStart)
    val startOffset = document.getLineStartOffset(startLineNumber)
    val indentation = selectionStart - startOffset
    val indentationText = document.getText(TextRange(startOffset, startOffset + indentation))

    if (indentationText.isNotBlank()) {
      HintManager.getInstance().showErrorHint(
        editor,
        "Caret must be at beginning of indentation"
      )
      return
    }

    val br = BufferedReader(StringReader(indentationText + selectedText))
    val sb = StringBuilder()

    br.forEachLine {
      if (it.isBlank()) {
        sb.appendLine()
      } else {
        sb.appendLine(it.substring(indentation))
      }
    }

    CopyPasteManager.getInstance().setContents(StringSelection(sb.toString()))
  }
}
